package com.cargomaze.cargo_maze.controller;

import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.model.Position;
import com.cargomaze.cargo_maze.persistance.exceptions.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cargomaze.cargo_maze.services.CargoMazeServicesImpl;
import com.cargomaze.cargo_maze.services.exceptions.CargoMazeServicesException;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/cargoMaze")
public class CargoMazeController {

    private final CargoMazeServicesImpl cargoMazeServices;
    
    @Autowired
    public CargoMazeController(CargoMazeServicesImpl cargoMazeServices){
        this.cargoMazeServices = cargoMazeServices;
    }

    //Session controller

    /**
     * Reurns the base lobby
     * @return 
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<?> getGameSession(@PathVariable String id) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getGameSession(id),HttpStatus.OK);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }        
    }


    @GetMapping("/sessions/{id}/board/state")
    public ResponseEntity<?> getBoardState(@PathVariable String id) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getBoardState(id),HttpStatus.ACCEPTED);
        } catch ( CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }        
    }

    @GetMapping("/sessions/{id}/state")
    public ResponseEntity<?> getGameSessionState(@PathVariable String id){
        try{
            return new ResponseEntity<>(cargoMazeServices.getGameSession(id).getStatus(), HttpStatus.OK);
        } catch (CargoMazePersistanceException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    //Player controller
    
    @GetMapping("/players/{nickName}")

    public ResponseEntity<?> getPlayer(@PathVariable String nickName) {
        try {
            return new ResponseEntity<>(cargoMazeServices.getPlayerById(nickName),HttpStatus.ACCEPTED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }        
    }

    /**
     * Creates a new player
     */
    @PostMapping("/players")
    public ResponseEntity<?> createPlayer(@RequestBody Map<String, String> nickname, HttpSession session) {
        try {
            cargoMazeServices.createPlayer(nickname.get("nickname"));
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }


    @GetMapping("/sessions/{id}/players/count")
    public ResponseEntity<?> getPlayerCount(@PathVariable String id) {
        try {
            int playerCount = cargoMazeServices.getPlayerCount(id);
            return new ResponseEntity<>(playerCount, HttpStatus.OK);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/sessions/{id}/players")
    public ResponseEntity<?> addPlayerToGame(@RequestBody Map<String, String> requestBody, @PathVariable String id) {
        String nickname = requestBody.get("nickname");
        if (nickname == null || nickname.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Nickname is required and cannot be empty"));
        }
        try {
            cargoMazeServices.addNewPlayerToGame(nickname, id);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("message", "Player added to game session", "sessionId", id, "nickname", nickname));
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }
    @GetMapping("/sessions/{id}/players")
    public ResponseEntity<?> getPlayersInSession(@PathVariable String id) {
        try {
            List<Player> players = cargoMazeServices.getPlayersInSession(id);
            return new ResponseEntity<>(players, HttpStatus.OK);
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }
    
    @PutMapping("/sessions/{sessionId}/players/{nickname}/move")
    public ResponseEntity<?> movePlayer(@RequestBody Position position, @PathVariable String sessionId, @PathVariable String nickname) {
        if (position == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "position is required"));
        }
        try {
            if(!cargoMazeServices.move(nickname, sessionId, position)){
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid move"));
            }
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("message", "Player moved", "sessionId", sessionId, "nickname", nickname));
        } catch (CargoMazePersistanceException | CargoMazeServicesException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }


    @DeleteMapping("/sessions/{id}/players/{nickname}")
    public ResponseEntity<?> removePlayerFromGame(@PathVariable String id, @PathVariable String nickname) {
        try {
            cargoMazeServices.removePlayerFromGame(nickname, id);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (CargoMazePersistanceException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/sessions/{id}/reset")
    public ResponseEntity<?> resetGameSession(@PathVariable String id) {
        try {
            cargoMazeServices.resetGameSession(id);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("message", "Game session reset", "sessionId", id));
        } catch (CargoMazePersistanceException | CargoMazeServicesException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }


}