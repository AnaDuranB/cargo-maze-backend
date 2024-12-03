package com.cargomaze.cargo_maze.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cargomaze.cargo_maze.model.Board;
import com.cargomaze.cargo_maze.model.Box;
import com.cargomaze.cargo_maze.model.Cell;
import com.cargomaze.cargo_maze.model.GameSession;
import com.cargomaze.cargo_maze.model.GameStatus;
import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.model.Position;
import com.cargomaze.cargo_maze.persistance.exceptions.CargoMazePersistanceException;
import com.cargomaze.cargo_maze.repository.CargoMazeDAL;

@Service
public class TransactionsServicesImpl implements TransactionsServices {
    private final CargoMazeDAL persistance;

    @Autowired
    public TransactionsServicesImpl(CargoMazeDAL persistance) {
        this.persistance = persistance;
    }
    
    @Override
    public boolean movePlayer(String playerId, Position newPosition, String gameSessionId) throws CargoMazePersistanceException {
        Player player = persistance.getPlayerInSession(gameSessionId, playerId);
        Position currentPos = player.getPosition();
        Board board = persistance.getSession(gameSessionId).getBoard();
        if (isValidPlayerMove(currentPos, newPosition, board)) {
            if (board.hasBoxAt(newPosition)) {
                boolean moveBox = moveBox(player, currentPos, newPosition, board, gameSessionId);
                if (!moveBox) {
                    return false;
                }
            }
            return movePlayer(player, board, newPosition, currentPos, gameSessionId);
        }
        return false;
    }

    @Transactional
    public boolean movePlayer(Player player, Board board, Position newPosition, Position currentPos, String gameSessionId) {
        try {  
            Cell cell1 = getCellAt(gameSessionId, currentPos.getX(), currentPos.getY());
            cell1.setState(Cell.EMPTY);
            Cell cell2 = getCellAt(gameSessionId, newPosition.getX(), newPosition.getY());
            cell2.setState(Cell.PLAYER);

            player.updatePosition(newPosition);
            updatePlayer(player);

            board.setCellAt(currentPos, cell1);
            board.setCellAt(newPosition, cell2);

            persistance.getSession(gameSessionId).setBoard(board);
            updateGameSession(persistance.getSession(gameSessionId));

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Transactional
    public boolean moveBox(Player player, Position playerPosition, Position boxPosition, Board board, String gameSessionId) throws CargoMazePersistanceException {
        Position boxNewPosition = getPositionFromMovingABox(boxPosition, playerPosition); // Validates all postions (in
        Box box = persistance.getBox(gameSessionId, persistance.getSession(gameSessionId).getBoard().getBoxAt(boxPosition).getId());
        if (isValidBoxMove(player, box, boxNewPosition, board)) { // va mover la caja
            try {
                box.move(boxNewPosition); // se cambia el lugar donde esta la caja
                if (board.isTargetAt(boxNewPosition)) {
                    box.setAtTarget(true);
                    boolean allOtherBoxesAtTarget = board.getBoxes().stream()
                            .filter(b -> !b.equals(box))
                            .allMatch(Box::isAtTarget);
                    if (allOtherBoxesAtTarget) {
                        persistance.getSession(gameSessionId).setStatus(GameStatus.COMPLETED);
                    }
                } // si la caja esta en un target
                else if (board.isTargetAt(boxPosition)) {
                    box.setAtTarget(false);
                }
                Cell cell1 = getCellAt(gameSessionId, boxNewPosition.getX(), boxNewPosition.getY());
                cell1.setState(Cell.BOX);
                board.setCellAt(boxNewPosition, cell1);
                persistance.getSession(gameSessionId).setBoard(board);

                updateGameSession(persistance.getSession(gameSessionId));
            } 
            catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void updateGameSession(GameSession gameSession) throws CargoMazePersistanceException {
        persistance.updateGameSession(gameSession);
    }

    @Override
    public void updatePlayer(Player player) throws CargoMazePersistanceException {
        persistance.updatePlayer(player);
    }

    @Override
    public Box getBox(String gameSessionId, String boxId) throws CargoMazePersistanceException {
        return persistance.getBox(gameSessionId, boxId);
    }

    @Override
    public List<Box> getBoxes(String gameSessionId) throws CargoMazePersistanceException {
        return persistance.getBoxes(gameSessionId);
    }

    @Override
    public Cell getCellAt(String gameSessionId, int x, int y) throws CargoMazePersistanceException {
        return persistance.getCellAt(gameSessionId, x, y);
    }

    private boolean isValidPlayerMove(Position currentPosition, Position newPosition, Board board) {
        return currentPosition.isAdjacent(newPosition) && board.isValidPosition(newPosition)
                && !board.hasWallAt(newPosition) && !board.isPlayerAt(newPosition);
    }

    private Position getPositionFromMovingABox(Position boxPosition, Position playerPosition) {
        // Eje y del jugador es menor al de la caja
        if (playerPosition.getY() < boxPosition.getY()) {
            return new Position(boxPosition.getX(), boxPosition.getY() + 1);
        }
        // Eje y del jugador es mayor al de la caja
        else if (playerPosition.getY() > boxPosition.getY()) {
            return new Position(boxPosition.getX(), boxPosition.getY() - 1);
        }
        // Eje x del jugador es menor al de la caja
        else if (playerPosition.getX() < boxPosition.getX()) {
            return new Position(boxPosition.getX() + 1, boxPosition.getY());
        }
        return new Position(boxPosition.getX() - 1, boxPosition.getY());
    }

    private boolean isValidBoxMove(Player player, Box box, Position newPosition, Board board) {
        return player.getPosition().isAdjacent(box.getPosition()) &&
                board.isValidPosition(newPosition) &&
                !board.hasWallAt(newPosition) &&
                !board.hasBoxAt(newPosition) &&
                !board.isPlayerAt(newPosition);
    }

}
