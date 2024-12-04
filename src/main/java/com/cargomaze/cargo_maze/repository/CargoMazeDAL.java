package com.cargomaze.cargo_maze.repository;

import java.util.List;

import com.cargomaze.cargo_maze.model.*;
import com.cargomaze.cargo_maze.persistance.exceptions.CargoMazePersistanceException;
import com.mongodb.client.ClientSession;

public interface CargoMazeDAL {

    Player addPlayer(Player player) throws CargoMazePersistanceException;

    Player getPlayer(String playerId) throws CargoMazePersistanceException;

    List<Player> getPlayers() throws CargoMazePersistanceException;

    void deletePlayer(Player player) throws CargoMazePersistanceException;

    void deletePlayers() throws CargoMazePersistanceException;

    Player getPlayerInSession(String playerId, String gameSessionId) throws CargoMazePersistanceException;

    GameSession addSession(GameSession session) throws CargoMazePersistanceException;

    GameSession getSession(String gameSessionId) throws CargoMazePersistanceException;

    int getPlayerCount(String gameSessionId) throws CargoMazePersistanceException;

    List<Player> getPlayersInSession(String id) throws CargoMazePersistanceException;

    Player updatePlayerById(String playerId) throws CargoMazePersistanceException;

    Player updatePlayer(Player player) throws CargoMazePersistanceException;

    Player updatePlayerPosition(String playerId, Position position, long clientTimestamp) throws CargoMazePersistanceException;

    GameSession updateGameSessionById( String sessionId, GameSession gameSession) throws CargoMazePersistanceException;

    void updateGameSessionBoard(String sessionId, Board board, long cell1Time, long cell2Time, Position cell1Position, Position cell2Position) throws CargoMazePersistanceException;

    GameSession updateGameSessionStatus(String sessionId, GameStatus status, long clientTimestamp) throws CargoMazePersistanceException;

    void deletePlayer(String playerId) throws CargoMazePersistanceException;

    void removePlayerFromSession(String playerId, String sessionId) throws CargoMazePersistanceException;

    Box getBox(String gameSessionId, String boxId) throws CargoMazePersistanceException;

    Box getBoxWithTime(String gameSessionId, String boxId, long boxTimeSpam) throws CargoMazePersistanceException;

    List<Box> getBoxes(String gameSessionId) throws CargoMazePersistanceException;

    Cell getCellAt(String gameSessionId, int x, int y) throws CargoMazePersistanceException;

    Cell getCellAtWithTime(String gameSessionId, int x, int y, long cellTimeSpam) throws CargoMazePersistanceException;
                                        
}