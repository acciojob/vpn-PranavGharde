package com.driver.test;

import com.driver.model.*;
import com.driver.repository.*;
import com.driver.services.impl.ConnectionServiceImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

public class TestCases {

    @Mock
    private UserRepository userRepository2;

    @Mock
    private ConnectionRepository connectionRepository2;

    @Mock
    private ServiceProviderRepository serviceProviderRepository2;

    @InjectMocks
    private ConnectionServiceImpl connectionService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExample() {
        assertTrue(true); // Always passes
    }

    @Test
    public void testConnect_Success() throws Exception {
        // Create dummy user
        User user = new User();
        user.setId(1);
        user.setConnected(false);
        user.setServiceProviderList(new ArrayList<>());
        user.setConnectionList(new ArrayList<>()); // <--- 🔥 important line

// Create service provider and country
        ServiceProvider sp = new ServiceProvider();
        sp.setId(1);

        Country country = new Country();
        country.setCountryName(CountryName.IND);
        country.setCode(CountryName.IND.toCode());

        List<Country> countryList = new ArrayList<>();
        countryList.add(country);
        sp.setCountryList(countryList);

        List<ServiceProvider> serviceProviders = new ArrayList<>();
        serviceProviders.add(sp);

        user.setServiceProviderList(serviceProviders);

// mocks
        when(userRepository2.findById(1)).thenReturn(Optional.of(user));
        when(connectionRepository2.save(org.mockito.Mockito.any(Connection.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userRepository2.save(org.mockito.Mockito.any(User.class))).thenAnswer(i -> i.getArguments()[0]);

// Call
        User updatedUser = connectionService.connect(1, "IND");

        assertTrue(updatedUser.getConnected());
        assertNotNull(updatedUser.getMaskedIp());
    }
}