package com.cargomaze.cargo_maze.repository;

import java.util.List;

import com.cargomaze.cargo_maze.model.*;
import com.cargomaze.cargo_maze.persistance.exceptions.CargoMazePersistanceException;

public class CargoMazeRepositoryImpl implements CargoMazeRepository {
    
        @Override
        public void addPlayer(Player player) throws CargoMazePersistanceException {
            // TODO Auto-generated method stub
    
        }
    
        @Override
        public Player getPlayer(String playerId) throws CargoMazePersistanceException {
            // TODO Auto-generated method stub
            return null;
        }
    
        @Override
        public Player getPlayer(String playerId, String gameSession) throws CargoMazePersistanceException {
            // TODO Auto-generated method stub
            return null;
        }
    
        @Override
        public void addSession(GameSession session) throws CargoMazePersistanceException {
            // TODO Auto-generated method stub
    
        }
    
        @Override
        public GameSession getSession(String gameSessionId) throws CargoMazePersistanceException {
            // TODO Auto-generated method stub
            return null;
        }
    
        @Override
        public int getPlayerCount(String gameSessionId) throws CargoMazePersistanceException {
            // TODO Auto-generated method stub
            return 0;
        }
    
        @Override
        public List<Player> getPlayersInSession(String id) throws CargoMazePersistanceException {
            // TODO Auto-generated method stub
            return null;
        }
    
}
