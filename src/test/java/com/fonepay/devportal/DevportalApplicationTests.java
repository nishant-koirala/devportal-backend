package com.fonepay.devportal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fonepay.devportal.common.config.EnvLoader;

@SpringBootTest
class DevportalApplicationTests {

    static {
        EnvLoader.load();
    }

    @Test
    void contextLoads() {
    }

}
