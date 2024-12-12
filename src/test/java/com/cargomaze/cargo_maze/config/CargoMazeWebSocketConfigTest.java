package com.cargomaze.cargo_maze.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import static org.mockito.Mockito.*;

@SpringBootTest
class CargoMazeWebSocketConfigTest {

    @Autowired
    private CargoMazeWebSocketConfig cargoMazeWebSocketConfig;

    @Test
    void testConfigureMessageBroker() {
        MessageBrokerRegistry mockConfig = mock(MessageBrokerRegistry.class);

        cargoMazeWebSocketConfig.configureMessageBroker(mockConfig);

        verify(mockConfig).enableSimpleBroker("/topic");
        verify(mockConfig).setApplicationDestinationPrefixes("/app");
    }

}
