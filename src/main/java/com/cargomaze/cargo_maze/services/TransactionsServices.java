package com.cargomaze.cargo_maze.services;

import java.util.List;
import com.cargomaze.cargo_maze.model.Board;
import com.cargomaze.cargo_maze.model.Box;
import com.cargomaze.cargo_maze.model.Cell;
import com.cargomaze.cargo_maze.model.GameSession;
import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.model.Position;
import com.cargomaze.cargo_maze.persistance.exceptions.CargoMazePersistanceException;


public interface TransactionsServices {
    boolean movePlayer(Player player, Position newPosition, GameSession gameSessionId) throws CargoMazePersistanceException;
    
    boolean moveBox(Player player, Position playerPosition, Position boxPosition, Board board, Box box,GameSession session) throws CargoMazePersistanceException;

    void updateGameSession(GameSession gameSession) throws CargoMazePersistanceException;
    
    void updatePlayer(Player player) throws CargoMazePersistanceException;

    Box getBox(String gameSessionId, String boxId) throws CargoMazePersistanceException;

    List<Box> getBoxes(String gameSessionId) throws CargoMazePersistanceException;

    Cell getCellAt(String gameSessionId, int x, int y) throws CargoMazePersistanceException;
    
}
