package com.cargomaze.cargo_maze.controller;

import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.model.Position;
import com.cargomaze.cargo_maze.persistance.exceptions.*;
import com.cargomaze.cargo_maze.services.AuthServices;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.cargomaze.cargo_maze.services.CargoMazeServices;
import com.cargomaze.cargo_maze.services.exceptions.CargoMazeServicesException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class CargoMazeController {
    private static final String ERROR_KEY = "error";
    private static final String NICKNAME_KEY = "nickname";
    private static final String SESSION_ID_KEY = "sessionId";
    private static final String MESSAGE_KEY = "message";
    private final CargoMazeServices cargoMazeServices;


    @Autowired
    public CargoMazeController(CargoMazeServices cargoMazeServices) {
        this.cargoMazeServices = cargoMazeServices;
    }

    @GetMapping("/cargoMaze/resource")
    public ResponseEntity<?> getResource(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Map<String, String> claims = new HashMap<>();
        claims.put("Hola", "SI");
        return new ResponseEntity<>(claims, HttpStatus.ACCEPTED);
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/board/state", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBoardState(@PathVariable String id) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getBoardState(id), HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/state", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> getGameSessionState(@PathVariable String id) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getGameSession(id).getStatus(), HttpStatus.OK);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    // Player controller

    @GetMapping(value = "cargoMaze/players/{nickName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPlayer(@PathVariable String nickName) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getPlayerById(nickName), HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/players", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPlayers() {
        try {
            return new ResponseEntity<>(cargoMazeServices.getPlayers(), HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    /**
     * Creates a new player
     */
    @PostMapping("cargoMaze/players")
    public ResponseEntity<?> createPlayer(@RequestBody Map<String, String> nickname, HttpSession session) {
        try {
            cargoMazeServices.createPlayer(nickname.get(NICKNAME_KEY));
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (CargoMazePersistanceException | CargoMazeServicesException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/players/count", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> getPlayerCount(@PathVariable String id) {
        try {
            return new ResponseEntity<>(Integer.toString(cargoMazeServices.getPlayerCount(id)), HttpStatus.OK);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @PutMapping("cargoMaze/sessions/{id}/players")
    public ResponseEntity<?> addPlayerToGame(@RequestBody Map<String, String> requestBody, @PathVariable String id) {
        String nickname = requestBody.get(NICKNAME_KEY);
        if (nickname == null || nickname.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR_KEY, "Nickname is required and cannot be empty"));
        }
        try {
            cargoMazeServices.addNewPlayerToGame(nickname, id);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of(MESSAGE_KEY, "Player added to game session", SESSION_ID_KEY, id, NICKNAME_KEY, nickname));
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/players", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPlayersInSession(@PathVariable String id) {
        try {
            List<Player> players = cargoMazeServices.getPlayersInSession(id);
            return new ResponseEntity<>(players, HttpStatus.OK);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @PutMapping("cargoMaze/sessions/{sessionId}/players/{nickname}/move")
    public ResponseEntity<?> movePlayer(@RequestBody Position position, @PathVariable String sessionId,
            @PathVariable String nickname) {
        if (position == null) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "position is required"));
        }
        try {
            if (!cargoMazeServices.move(nickname, sessionId, position)) {
                return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Invalid move"));
            }
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of(MESSAGE_KEY, "Player moved", SESSION_ID_KEY, sessionId, NICKNAME_KEY, nickname));
        } catch (CargoMazePersistanceException | CargoMazeServicesException ex) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @DeleteMapping("cargoMaze/sessions/{id}/players/{nickname}")
    public ResponseEntity<?> removePlayerFromGame(@PathVariable String id, @PathVariable String nickname) {
        try {
            cargoMazeServices.removePlayerFromGame(nickname, id);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @PutMapping("cargoMaze/sessions/{id}/reset")
    public ResponseEntity<?> resetGameSession(@PathVariable String id) {
        try {
            cargoMazeServices.resetGameSession(id);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of(MESSAGE_KEY, "Game session reset", SESSION_ID_KEY, id));
        } catch (CargoMazePersistanceException | CargoMazeServicesException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @DeleteMapping("cargoMaze/players/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable String id) {
        try {
            cargoMazeServices.deletePlayer(id);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @DeleteMapping("cargoMaze/players")
    public ResponseEntity<?> deletePlayers() {
        try {
            cargoMazeServices.deletePlayers();
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @PutMapping("cargoMaze/session/{sessionId}/players")
    public ResponseEntity<?> removePlayersFromSession(@PathVariable String sessionId) {
        try {
            cargoMazeServices.removePlayersFromSession(sessionId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/boxes/{x}/{y}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBoxAt(@PathVariable String id, @PathVariable int x, @PathVariable int y) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getBoxAt(id, x, y), HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/cells/{x}/{y}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCellAt(@PathVariable String id, @PathVariable int x, @PathVariable int y) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getCellAt(id, x, y), HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }

    @GetMapping(value = "cargoMaze/sessions/{id}/boxes/index/{index}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBoxAtIndex(@PathVariable String id, @PathVariable int index) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getBoxAtIndex(id, index), HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_KEY, ex.getMessage()));
        }
    }
}