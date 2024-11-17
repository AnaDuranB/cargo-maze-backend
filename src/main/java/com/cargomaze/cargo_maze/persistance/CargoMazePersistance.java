package com.cargomaze.cargo_maze.persistance;


import com.cargomaze.cargo_maze.model.GameSession;
import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.persistance.exceptions.*;

import java.util.List;


public interface CargoMazePersistance {

    public void addPlayer(Player player) throws CargoMazePersistanceException;

    public Player getPlayer(String playerId) throws CargoMazePersistanceException;

    public Player getPlayer(String playerId, String gameSession) throws CargoMazePersistanceException;

    public void addSession(GameSession session) throws CargoMazePersistanceException;

    public GameSession getSession(String gameSessionId) throws CargoMazePersistanceException;

    public int getPlayerCount(String gameSessionId) throws CargoMazePersistanceException;

    public List<Player> getPlayersInSession(String id) throws CargoMazePersistanceException;

    public void removePlayerFromGame(String nickname, String gameSessionId) throws CargoMazePersistanceException;


}