package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.*;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {

    @Autowired
    private UserRepository userRepository2;

    @Autowired
    private ConnectionRepository connectionRepository2;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception {
        User user = userRepository2.findById(userId).orElse(null);
        if (user == null) throw new Exception("User not found");

        if (Boolean.TRUE.equals(user.getConnected())) {
            throw new Exception("Already connected");
        }

        if (user.getOriginalCountry() != null &&
                user.getOriginalCountry().getCountryName().toString().equalsIgnoreCase(countryName)) {
            return user;
        }

        List<ServiceProvider> serviceProviders = user.getServiceProviderList();
        ServiceProvider selectedProvider = null;
        Country selectedCountry = null;
        int minId = Integer.MAX_VALUE;

        for (ServiceProvider sp : serviceProviders) {
            for (Country c : sp.getCountryList()) {
                if (c.getCountryName().toString().equalsIgnoreCase(countryName)) {
                    if (sp.getId() < minId) {
                        minId = sp.getId();
                        selectedProvider = sp;
                        selectedCountry = c;
                    }
                }
            }
        }

        if (selectedProvider == null) {
            throw new Exception("Unable to connect");
        }

        Connection connection = new Connection();
        connection.setUser(user);
        connection.setServiceProvider(selectedProvider);

        connection = connectionRepository2.save(connection);

        user.getConnectionList().add(connection);
        user.setMaskedIp(selectedCountry.getCode() + "." + user.getId() + "." + connection.getId());
        user.setConnected(true);

        userRepository2.save(user);

        return user;
    }


    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).orElse(null);

        if (user == null) {
            throw new Exception("User not found");
        }

        if (Boolean.FALSE.equals(user.getConnected()) || user.getConnected() == null) {
            throw new Exception("Already disconnected");
        }

        user.setMaskedIp(null);
        user.setConnected(false);
        user.setConnectionList(new ArrayList<>());  // clear all connections

        userRepository2.save(user);

        return user;
    }


    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).orElse(null);
        User receiver = userRepository2.findById(receiverId).orElse(null);

        if (sender == null || receiver == null) {
            throw new Exception("User not found");
        }

        String receiverCountryCode;
        if (receiver.getMaskedIp() != null) {
            receiverCountryCode = receiver.getMaskedIp().split("\\.")[0];
        } else if (receiver.getOriginalCountry() != null) {
            receiverCountryCode = receiver.getOriginalCountry().getCode();
        } else {
            throw new Exception("Receiver country information is missing");
        }

        String senderCountryCode;
        if (Boolean.TRUE.equals(sender.getConnected()) && sender.getMaskedIp() != null) {
            senderCountryCode = sender.getMaskedIp().split("\\.")[0];
        } else if (sender.getOriginalCountry() != null) {
            senderCountryCode = sender.getOriginalCountry().getCode();
        } else {
            throw new Exception("Sender country information is missing");
        }

        if (senderCountryCode.equals(receiverCountryCode)) {
            return sender;
        }

        String requiredCountry = "";
        for (CountryName cn : CountryName.values()) {
            if (cn.toCode().equals(receiverCountryCode)) {
                requiredCountry = cn.toString();
                break;
            }
        }

        if (requiredCountry.isEmpty()) {
            throw new Exception("Country code mismatch");
        }

        try {
            sender = connect(senderId, requiredCountry);
        } catch (Exception e) {
            throw new Exception("Cannot establish communication");
        }

        return sender;
    }
}
