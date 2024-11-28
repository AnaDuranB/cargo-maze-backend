package com.cargomaze.cargo_maze.services;

import java.util.List;

import com.cargomaze.cargo_maze.model.GameSession;
import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.model.Position;
import com.cargomaze.cargo_maze.persistance.exceptions.CargoMazePersistanceException;
import com.cargomaze.cargo_maze.services.exceptions.CargoMazeServicesException;

public interface CargoMazeServices {
    void createPlayer(String nickname) throws CargoMazePersistanceException;

    Player getPlayerById(String playerId) throws CargoMazePersistanceException;

    List<Player> getPlayersInSession(String gameSessionId) throws CargoMazePersistanceException;

    int getPlayerCount(String gameSessionId) throws CargoMazePersistanceException;

    void addNewPlayerToGame(String nickname, String gameSessionId) throws CargoMazePersistanceException;

    void removePlayerFromGame(String nickname, String gameSessionId) throws CargoMazePersistanceException;

    void createSession(String sessionId) throws CargoMazePersistanceException;

    GameSession getGameSession(String gameSessionId) throws CargoMazePersistanceException;

    void resetGameSession(String gameSessionId) throws CargoMazePersistanceException, CargoMazeServicesException;

    String[][] getBoardState(String gameSessionId) throws CargoMazePersistanceException;

    boolean move(String playerId, String gameSessionId, Position direction) throws CargoMazePersistanceException, CargoMazeServicesException;
    
    
}
