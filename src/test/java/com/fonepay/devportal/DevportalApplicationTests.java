package com.fonepay.devportal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fonepay.devportal.common.config.EnvLoader;

@SpringBootTest
class DevportalApplicationTests {

    @BeforeAll
    static void setUp() {
        EnvLoader.load();
    }

    @Test
    void contextLoads() {
    }

}
