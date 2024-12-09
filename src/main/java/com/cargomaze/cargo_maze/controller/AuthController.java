package com.cargomaze.cargo_maze.controller;

import com.cargomaze.cargo_maze.services.AuthServices;
import com.cargomaze.cargo_maze.services.CargoMazeServices;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/authC")
public class AuthController {
    private final AuthServices authServices;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    public AuthController(AuthServices authServices){
        this.authServices = authServices;
    }
    @GetMapping("/microsoft")
    public ResponseEntity<?> getToken(
            @RegisteredOAuth2AuthorizedClient("aad") OAuth2AuthorizedClient authorizedClient,
            HttpServletResponse response) {
        try {
            String token = authorizedClient.getAccessToken().getTokenValue();
            System.out.println(token);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://graph.microsoft.com/v1.0/me"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> responseGraph = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseGraph.body());

            String displayName = jsonNode.path("displayName").asText();
            String userPrincipalName = jsonNode.path("userPrincipalName").asText();

            if (userPrincipalName.isEmpty()) {
                userPrincipalName = authServices.getEmailFromToken(token);
                String[] data = userPrincipalName.split("@");
                displayName = data[0];
            }
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("displayName", displayName);
            responseBody.put("userPrincipalName", userPrincipalName);
            responseBody.put("token", token);

            response.sendRedirect("http://localhost:4200/sessionMenu.html?token=" + URLEncoder.encode(token, "UTF-8") + "&displayName=" + URLEncoder.encode(displayName, "UTF-8"));

            return ResponseEntity.ok().body(responseBody);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
