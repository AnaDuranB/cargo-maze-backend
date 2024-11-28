package com.cargomaze.cargo_maze.services;

import com.cargomaze.cargo_maze.persistance.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cargomaze.cargo_maze.model.Board;
import com.cargomaze.cargo_maze.model.Box;
import com.cargomaze.cargo_maze.model.Cell;
import com.cargomaze.cargo_maze.model.GameSession;
import com.cargomaze.cargo_maze.model.GameStatus;
import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.model.Position;
import com.cargomaze.cargo_maze.repository.CargoMazeDAL;
import com.cargomaze.cargo_maze.services.exceptions.CargoMazeServicesException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CargoMazeServicesImpl implements CargoMazeServices {

    private final CargoMazeDAL persistance;

    @Autowired
    public CargoMazeServicesImpl(CargoMazeDAL persistance) {
        this.persistance = persistance;
    }

    @Override
    public void createPlayer(String nickname) throws CargoMazePersistanceException {
        if (nickname == null || nickname.isEmpty()) {
            throw new CargoMazePersistanceException("Invalid nickname");
        }

        Player player = new Player(nickname);
        persistance.addPlayer(player);
    }

    @Override
    public void addNewPlayerToGame(String nickname, String gameSessionId) throws CargoMazePersistanceException {
        GameSession session = persistance.getSession(gameSessionId);
        Player player = persistance.getPlayer(nickname);
        if (player == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_FOUND);
        }
        if (!session.getStatus().equals(GameStatus.WAITING_FOR_PLAYERS)) {
            throw new CargoMazePersistanceException("Session is not waiting for players");
        }
        if (session.getPlayers().size() >= 4) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.FULL_SESSION_EXCEPTION);
        }
        if (player.getIndex() != -1) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_ALREADY_IN_SESSION);
        }
        session.addPlayer(player);

        if (session.getPlayers().size() == 4 && session.getPlayers().stream().allMatch(Player::isReady)) {
            session.setStatus(GameStatus.IN_PROGRESS);
        }
        persistance.updateGameSession(session);
        persistance.updatePlayer(player);
    }

    @Override
    public void removePlayerFromGame(String nickname, String gameSessionId) throws CargoMazePersistanceException {
        Player player = getPlayerById(nickname);
        GameSession session = persistance.getSession(gameSessionId);
        if (!player.getGameSession().equals(gameSessionId)) {
            session.removePlayer(player);
            if (session.getPlayerCount() == 0) {
                if (!session.getStatus().equals(GameStatus.RESETING_GAME)) {
                    session.resetGame();
                }
                session.setStatus(GameStatus.WAITING_FOR_PLAYERS);
            }
            persistance.updateGameSession(session);
            persistance.updatePlayer(player);
        }
        else{
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_IN_SESSION);
        }
    }

    @Override
    public void resetGameSession(String gameSessionId) throws CargoMazePersistanceException, CargoMazeServicesException {
        GameSession session = persistance.getSession(gameSessionId);
        if (!session.getStatus().equals(GameStatus.COMPLETED)) {
            throw new CargoMazeServicesException(CargoMazeServicesException.SESSION_IS_NOT_FINISHED);
        }
        session.resetGame();
        persistance.updateGameSession(session);
    }

    @Override
    public List<Player> getPlayersInSession(String gameSessionId) throws CargoMazePersistanceException {
        return persistance.getPlayersInSession(gameSessionId);
    }

    @Override
    public void createSession(String sessionId) throws CargoMazePersistanceException {
        GameSession session = new GameSession(sessionId);
        persistance.addSession(session);
    }

    @Override
    public GameSession getGameSession(String gameSessionId) throws CargoMazePersistanceException {
        return persistance.getSession(gameSessionId);
    }

    @Override
    public Player getPlayerById(String playerId) throws CargoMazePersistanceException {
        return persistance.getPlayer(playerId);
    }

    @Override
    public int getPlayerCount(String gameSessionId) throws CargoMazePersistanceException {
        return persistance.getPlayerCount(gameSessionId);
    }

    @Override
    public String[][] getBoardState(String gameSessionId) throws CargoMazePersistanceException {
        return persistance.getSession(gameSessionId).getBoardState();
    }

    @Override
    public boolean move(String playerId, String gameSessionId, Position direction) throws CargoMazePersistanceException, CargoMazeServicesException {
        Player player = persistance.getPlayerInSession(playerId, gameSessionId);
        GameSession gameSession = persistance.getSession(gameSessionId);
        if (!gameSession.getStatus().equals(GameStatus.IN_PROGRESS)) {
            throw new CargoMazeServicesException(CargoMazeServicesException.SESSION_IS_NOT_IN_PROGRESS);
        }
        Position newPosition = new Position(player.getPosition().getX() + direction.getX(), player.getPosition().getY() + direction.getY());
        boolean moved = movePlayer(player, newPosition, gameSession);
        if(moved){
            persistance.updateGameSession(gameSession);
            persistance.updatePlayer(player);
            return true;
        }
        return false;
    }

    private boolean movePlayer(Player player, Position newPosition, GameSession gameSession) {
        Position currentPos = player.getPosition();
        Board board = gameSession.getBoard();
        if (isValidPlayerMove(currentPos, newPosition, board)) {
            if (board.hasBoxAt(newPosition)) {
                boolean moveBox = moveBox(player, currentPos, newPosition, board, gameSession);
                if (!moveBox) {
                    return false;
                }
            }
            ReentrantLock lock = board.getCellAt(newPosition).lock;
            if (lock.tryLock()) { // se bloquea la celda a donde se va a mover el jugador por si alguno otro
                                  // intenta acceder a este.
                try {
                    player.updatePosition(newPosition);
                    board.getCellAt(currentPos).setState(Cell.EMPTY); // se
                    board.getCellAt(newPosition).setState(Cell.PLAYER);
                } finally {
                    lock.unlock();
                }
                return true;
            }
        }
        return false;
    }

    private boolean moveBox(Player player, Position playerPosition, Position boxPosition, Board board,
            GameSession gameSession) {
        Position boxNewPosition = getPositionFromMovingABox(boxPosition, playerPosition); // Validates all postions (in
                                                                                          // theory);
        Box box = board.getBoxAt(boxPosition);
        if (isValidBoxMove(player, box, boxNewPosition, board)) {
            if (box.lock.tryLock() && board.getCellAt(boxNewPosition).lock.tryLock()) { // Lockeamos tanto la caja a
                                                                                        // mover y la celda a donde se
                                                                                        // va mover la caja
                try {
                    box.move(boxNewPosition); // se cambia el lugar donde esta la caja
                    if (board.isTargetAt(boxNewPosition)) {
                        box.setAtTarget(true);
                        boolean allOtherBoxesAtTarget = board.getBoxes().stream()
                                .filter(b -> !b.equals(box))
                                .allMatch(Box::isAtTarget);
                        if (allOtherBoxesAtTarget) {
                            gameSession.setStatus(GameStatus.COMPLETED);
                        }
                    } // si la caja esta en un target
                    else if (board.isTargetAt(boxPosition)) {
                        box.setAtTarget(false);
                    }
                    board.getCellAt(boxNewPosition).setState(Cell.BOX); // se cambia el estado de la celda
                } finally {
                    box.lock.unlock(); // se desbloquean los elementos accedidos
                    board.getCellAt(boxNewPosition).lock.unlock();
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
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
