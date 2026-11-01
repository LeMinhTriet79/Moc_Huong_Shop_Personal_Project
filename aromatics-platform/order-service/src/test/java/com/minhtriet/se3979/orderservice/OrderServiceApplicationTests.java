package com.minhtriet.se3979.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none"
})
@ActiveProfiles("test")
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
