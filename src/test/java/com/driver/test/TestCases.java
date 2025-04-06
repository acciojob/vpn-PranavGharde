package com.driver.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TestCases {

    @Test
    public void sampleTest() {
        assertEquals(4, 2 + 2);
    }
}
