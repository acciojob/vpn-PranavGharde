package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;

    @Autowired
    ServiceProviderRepository serviceProviderRepository3;

    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception {
        countryName = countryName.toUpperCase();

        CountryName countryNameEnum;
        try {
            countryNameEnum = CountryName.valueOf(countryName);
        } catch (Exception e) {
            throw new Exception("Country not found");
        }

        // Create new User
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setConnected(false);
        user.setMaskedIp(null);
        user.setServiceProviderList(new ArrayList<>());
        user.setConnectionList(new ArrayList<>());

        // Save user first to generate ID
        User savedUser = userRepository3.save(user);

        // Now create Country
        Country country = new Country();
        country.setCountryName(countryNameEnum);
        country.setCode(countryNameEnum.toCode());
        country.setUser(savedUser); // Set user

        savedUser.setOriginalCountry(country);

        // Now set originalIp after ID assigned
        savedUser.setOriginalIp(country.getCode() + "." + savedUser.getId());

        // Save again after setting country and originalIp
        userRepository3.save(savedUser);

        return savedUser;
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        User user = userRepository3.findById(userId).get();
        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();

        user.getServiceProviderList().add(serviceProvider);
        serviceProvider.getUsers().add(user);

        serviceProviderRepository3.save(serviceProvider);
        return user;
    }
}
