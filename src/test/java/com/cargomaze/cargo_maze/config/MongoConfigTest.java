package com.cargomaze.cargo_maze.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.MongoTransactionManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MongoConfigTest {

    @Autowired
    private MongoTransactionManager transactionManager;

    @Test
    void testTransactionManager() {
        assertNotNull(transactionManager);
    }
}
