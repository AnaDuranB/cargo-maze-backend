package com.cargomaze.cargo_maze.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.cargomaze.cargo_maze.model.GameSession;
import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.persistance.exceptions.CargoMazePersistanceException;


public interface CargoMazeDAL {

    public void addPlayer(Player player) throws CargoMazePersistanceException;

    public Player getPlayer(String playerId) throws CargoMazePersistanceException;

    public Player getPlayerInSession(String playerId, String gameSessionId) throws CargoMazePersistanceException;

    public void addSession(GameSession session) throws CargoMazePersistanceException;

    public GameSession getSession(String gameSessionId) throws CargoMazePersistanceException;

    public int getPlayerCount(String gameSessionId) throws CargoMazePersistanceException;

    public List<Player> getPlayersInSession(String id) throws CargoMazePersistanceException;

    public void updatePlayerById(String playerId) throws CargoMazePersistanceException;

    public void updatePlayer(Player player) throws CargoMazePersistanceException;

    public void updateGameSessionById(String sessionId) throws CargoMazePersistanceException;

    public void updateGameSession(GameSession session) throws CargoMazePersistanceException;

    public void deletePlayer(String playerId) throws CargoMazePersistanceException;   

}