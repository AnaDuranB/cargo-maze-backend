package com.cargomaze.cargo_maze.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.mockito.Mockito.*;

@SpringBootTest
class CustomCorsFilterTest {

    @Autowired
    private CustomCorsFilter customCorsFilter;

    @Test
    void testDoFilter() throws ServletException, IOException {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        FilterChain mockChain = mock(FilterChain.class);

        when(mockRequest.getHeader("Origin")).thenReturn("http://localhost:4200");

        customCorsFilter.doFilter(mockRequest, mockResponse, mockChain);

        verify(mockResponse).setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        verify(mockResponse).setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        verify(mockResponse).setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Cache-Control");
        verify(mockResponse).setHeader("Access-Control-Allow-Credentials", "true");
    }
}
