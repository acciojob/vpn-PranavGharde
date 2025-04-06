package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception {
        User user = userRepository2.findById(userId).get();

        if (user.getConnected()) {
            throw new Exception("Already connected");
        }

        if (user.getOriginalCountry().getCountryName().toString().equalsIgnoreCase(countryName)) {
            // No need to connect if already in the country
            return user;
        }

        List<ServiceProvider> serviceProviders = user.getServiceProviderList();
        ServiceProvider selectedProvider = null;
        Country selectedCountry = null;

        int minId = Integer.MAX_VALUE;

        for (ServiceProvider sp : serviceProviders) {
            for (Country country : sp.getCountryList()) {
                if (country.getCountryName().toString().equalsIgnoreCase(countryName)) {
                    if (sp.getId() < minId) {
                        minId = sp.getId();
                        selectedProvider = sp;
                        selectedCountry = country;
                    }
                }
            }
        }

        if (selectedProvider == null) {
            throw new Exception("Unable to connect");
        }

        // Create connection
        Connection connection = new Connection();
        connection.setUser(user);
        connection.setServiceProvider(selectedProvider);
        user.getConnectionList().add(connection);

        user.setMaskedIp(selectedCountry.getCode() + "." + user.getId());
        user.setConnected(true);

        userRepository2.save(user);
        return user;
    }

    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();

        if (!user.getConnected()) {
            throw new Exception("Already disconnected");
        }

        user.setMaskedIp(null);
        user.setConnected(false);

        userRepository2.save(user);
        return user;
    }

    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();

        String receiverIp = receiver.getMaskedIp();
        String receiverCountryCode = null;

        if (receiverIp != null) {
            receiverCountryCode = receiverIp.substring(0, 3);
        } else {
            receiverCountryCode = receiver.getOriginalCountry().getCode();
        }

        String senderCountryCode = null;
        if (sender.getConnected() && sender.getMaskedIp() != null) {
            senderCountryCode = sender.getMaskedIp().substring(0, 3);
        } else {
            senderCountryCode = sender.getOriginalCountry().getCode();
        }

        if (senderCountryCode.equals(receiverCountryCode)) {
            return sender;
        }

        String requiredCountry = "";
        for (CountryName countryName : CountryName.values()) {
            if (countryName.toCode().equals(receiverCountryCode)) {
                requiredCountry = countryName.toString();
                break;
            }
        }

        try {
            sender = connect(senderId, requiredCountry);
        } catch (Exception e) {
            throw new Exception("Cannot establish communication");
        }

        return sender;
    }
}
