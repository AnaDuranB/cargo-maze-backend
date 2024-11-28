package com.cargomaze.cargo_maze.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.cargomaze.cargo_maze.model.GameSession;
import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.persistance.exceptions.CargoMazePersistanceException;


public interface CargoMazeDAL {

    public Player addPlayer(Player player) throws CargoMazePersistanceException;

    public Player getPlayer(String playerId) throws CargoMazePersistanceException;

    public List<Player> getPlayers() throws CargoMazePersistanceException;

    public void deletePlayer(Player player) throws CargoMazePersistanceException;

    public void deletePlayers() throws CargoMazePersistanceException;

    public Player getPlayerInSession(String playerId, String gameSessionId) throws CargoMazePersistanceException;

    public GameSession addSession(GameSession session) throws CargoMazePersistanceException;

    public GameSession getSession(String gameSessionId) throws CargoMazePersistanceException;

    public int getPlayerCount(String gameSessionId) throws CargoMazePersistanceException;

    public List<Player> getPlayersInSession(String id) throws CargoMazePersistanceException;

    public Player updatePlayerById(String playerId) throws CargoMazePersistanceException;

    public Player updatePlayer(Player player) throws CargoMazePersistanceException;

    public GameSession updateGameSessionById(String sessionId) throws CargoMazePersistanceException;

    public GameSession updateGameSession(GameSession session) throws CargoMazePersistanceException;

    public void deletePlayer(String playerId) throws CargoMazePersistanceException;   

    public void removePlayerFromSession(String playerId, String sessionId) throws CargoMazePersistanceException;

}