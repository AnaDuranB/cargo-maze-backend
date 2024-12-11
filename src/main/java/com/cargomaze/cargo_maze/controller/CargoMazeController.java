package com.cargomaze.cargo_maze.controller;

import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.model.Position;
import com.cargomaze.cargo_maze.persistance.exceptions.*;
import com.cargomaze.cargo_maze.services.AuthServices;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.cargomaze.cargo_maze.services.CargoMazeServices;
import com.cargomaze.cargo_maze.services.exceptions.CargoMazeServicesException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class CargoMazeController {

    private final CargoMazeServices cargoMazeServices;
    private final AuthServices authServices;

    private static final Logger logger = LoggerFactory.getLogger(CargoMazeController.class);

    @Autowired
    public CargoMazeController(CargoMazeServices cargoMazeServices, AuthServices authServices) {
        this.cargoMazeServices = cargoMazeServices;
        this.authServices = authServices;
    }


    @GetMapping()
    public ResponseEntity<?> getWelcomeMessage() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Session controller

    /**
     * Reurns the base lobby
     * 
     * @return
     */
    @GetMapping(value = "cargoMaze/sessions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getGameSession(@PathVariable String id) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getGameSession(id), HttpStatus.OK);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/board/state", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBoardState(@PathVariable String id) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getBoardState(id), HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/state", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getGameSessionState(@PathVariable String id) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getGameSession(id).getStatus(), HttpStatus.OK);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    // Player controller

    @GetMapping(value = "cargoMaze/players/{nickName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPlayer(@PathVariable String nickName) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getPlayerById(nickName), HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/players", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPlayers() {
        try {
            return new ResponseEntity<>(cargoMazeServices.getPlayers(), HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Creates a new player
     */
    @PostMapping("cargoMaze/players")
    public ResponseEntity<?> createPlayer(@RequestBody Map<String, String> nickname, HttpSession session) {
        try {
            cargoMazeServices.createPlayer(nickname.get("nickname"));
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (CargoMazePersistanceException | CargoMazeServicesException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/players/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPlayerCount(@PathVariable String id) {
        try {
            int playerCount = cargoMazeServices.getPlayerCount(id);
            Map<String, Integer> count = Map.of("count", playerCount);
            
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("cargoMaze/sessions/{id}/players")
    public ResponseEntity<?> addPlayerToGame(@RequestBody Map<String, String> requestBody, @PathVariable String id) {
        String nickname = requestBody.get("nickname");
        if (nickname == null || nickname.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Nickname is required and cannot be empty"));
        }
        try {
            cargoMazeServices.addNewPlayerToGame(nickname, id);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("message", "Player added to game session", "sessionId", id, "nickname", nickname));
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/players", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPlayersInSession(@PathVariable String id) {
        try {
            List<Player> players = cargoMazeServices.getPlayersInSession(id);
            return new ResponseEntity<>(players, HttpStatus.OK);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("cargoMaze/sessions/{sessionId}/players/{nickname}/move")
    public ResponseEntity<?> movePlayer(@RequestBody Position position, @PathVariable String sessionId,
            @PathVariable String nickname) {
        if (position == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "position is required"));
        }
        try {
            if (!cargoMazeServices.move(nickname, sessionId, position)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid move"));
            }
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("message", "Player moved", "sessionId", sessionId, "nickname", nickname));
        } catch (CargoMazePersistanceException | CargoMazeServicesException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("cargoMaze/sessions/{id}/players/{nickname}")
    public ResponseEntity<?> removePlayerFromGame(@PathVariable String id, @PathVariable String nickname) {
        try {
            cargoMazeServices.removePlayerFromGame(nickname, id);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("cargoMaze/sessions/{id}/reset")
    public ResponseEntity<?> resetGameSession(@PathVariable String id) {
        try {
            cargoMazeServices.resetGameSession(id);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("message", "Game session reset", "sessionId", id));
        } catch (CargoMazePersistanceException | CargoMazeServicesException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("cargoMaze/players/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable String id) {
        try {
            cargoMazeServices.deletePlayer(id);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("cargoMaze/players")
    public ResponseEntity<?> deletePlayers() {
        try {
            cargoMazeServices.deletePlayers();
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("cargoMaze/session/{sessionId}/players")
    public ResponseEntity<?> removePlayersFromSession(@PathVariable String sessionId) {
        try {
            cargoMazeServices.removePlayersFromSession(sessionId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    /*
     * @GetMapping("/sessions/{id}/boxes/{boxId}")
     * public ResponseEntity<?> getBox(@PathVariable String id, @PathVariable String
     * boxId) {
     * try {
     * return new ResponseEntity<>(cargoMazeServices.getBox(id, boxId),
     * HttpStatus.ACCEPTED);
     * } catch (CargoMazePersistanceException ex) {
     * return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error",
     * ex.getMessage()));
     * }
     * }
     */

    @GetMapping(value = "cargoMaze/sessions/{id}/boxes/{x}/{y}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBoxAt(@PathVariable String id, @PathVariable int x, @PathVariable int y) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getBoxAt(id, x, y), HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/cells/{x}/{y}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCellAt(@PathVariable String id, @PathVariable int x, @PathVariable int y) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getCellAt(id, x, y), HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/boxes/index/{index}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBoxAtIndex(@PathVariable String id, @PathVariable int index) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getBoxAtIndex(id, index), HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }
}