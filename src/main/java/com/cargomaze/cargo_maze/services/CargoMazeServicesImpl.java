package com.cargomaze.cargo_maze.services;

import com.cargomaze.cargo_maze.persistance.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cargomaze.cargo_maze.model.Box;
import com.cargomaze.cargo_maze.model.Cell;
import com.cargomaze.cargo_maze.model.GameSession;
import com.cargomaze.cargo_maze.model.GameStatus;
import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.model.Position;
import com.cargomaze.cargo_maze.repository.CargoMazeDAL;
import com.cargomaze.cargo_maze.services.exceptions.CargoMazeServicesException;

import java.util.LinkedList;
import java.util.List;

@Service
public class CargoMazeServicesImpl implements CargoMazeServices {

    private final CargoMazeDAL persistance;
    private final TransactionsServices transactionsServices;

    @Autowired
    public CargoMazeServicesImpl(CargoMazeDAL persistance, TransactionsServices transactionsServices) {
        this.persistance = persistance;
        this.transactionsServices = transactionsServices;
    }

    @Override
    public Player createPlayer(String nickname) throws CargoMazePersistanceException, CargoMazeServicesException {
        if (nickname == null || nickname.isEmpty()) {
            throw new CargoMazeServicesException(CargoMazeServicesException.INVALID_NICKNAME);
        }
        Player player = new Player(nickname);
        return persistance.addPlayer(player);
    }

    @Override
    public void deletePlayer(String playerId) throws CargoMazePersistanceException {
        Player player = persistance.getPlayer(playerId);
        String playerSession = player.getGameSession();
        if (playerSession != null) {
            GameSession session = persistance.getSession(playerSession);
            session.removePlayer(player);
            if (session.getPlayerCount() == 0) {
                if (!session.getStatus().equals(GameStatus.RESETING_GAME)) {
                    session.resetGame();
                }
                session.setStatus(GameStatus.WAITING_FOR_PLAYERS);
            }
            persistance.updateGameSession(session);
        }
        persistance.deletePlayer(player);
    }

    @Override
    public void deletePlayers() throws CargoMazePersistanceException {
        for (Player p : persistance.getPlayers()) {
            deletePlayer(p.getNickname());
        }
    }

    @Override
    public void removePlayersFromSession(String sessionId) throws CargoMazePersistanceException {
        GameSession session = persistance.getSession(sessionId);
        List<Player> players = session.getPlayers();
        session.setPlayers(new LinkedList<>());
        for (Player p : players) {
            removePlayerFromGame(p.getNickname(), session.getSessionId());
        }

    }

    @Override
    public Player addNewPlayerToGame(String nickname, String gameSessionId) throws CargoMazePersistanceException {
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
        return persistance.updatePlayer(player);
    }

    @Override
    public Player removePlayerFromGame(String nickname, String gameSessionId) throws CargoMazePersistanceException {
        Player player = getPlayerById(nickname);
        GameSession session = persistance.getSession(gameSessionId);
        if (player.getGameSession().equals(gameSessionId)) {
            session.removePlayer(player);
            if (session.getPlayerCount() == 0) {
                if (!session.getStatus().equals(GameStatus.RESETING_GAME)) {
                    session.resetGame();
                }
                session.setStatus(GameStatus.WAITING_FOR_PLAYERS);
            }
            persistance.updateGameSession(session);
            return persistance.updatePlayer(player);
        } else {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_IN_SESSION);
        }
    }

    @Override
    public GameSession resetGameSession(String gameSessionId)
            throws CargoMazePersistanceException, CargoMazeServicesException {
        GameSession session = persistance.getSession(gameSessionId);
        if (!session.getStatus().equals(GameStatus.COMPLETED)) {
            throw new CargoMazeServicesException(CargoMazeServicesException.SESSION_IS_NOT_FINISHED);
        }
        session.resetGame();
        return persistance.updateGameSession(session);
    }

    @Override
    public List<Player> getPlayersInSession(String gameSessionId) throws CargoMazePersistanceException {
        return persistance.getPlayersInSession(gameSessionId);
    }

    @Override
    public boolean isGameFinished(String gameSessionid) throws CargoMazePersistanceException {
        return persistance.getSession(gameSessionid).getStatus().equals(GameStatus.COMPLETED);
    }

    @Override
    public GameSession createSession(String sessionId) throws CargoMazePersistanceException {
        GameSession session = new GameSession(sessionId);
        return persistance.addSession(session);
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
    public List<Player> getPlayers() throws CargoMazePersistanceException {
        return persistance.getPlayers();
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
        Player player = persistance.getPlayerInSession(gameSessionId, playerId);
        GameSession gameSession = persistance.getSession(gameSessionId);
        if (!gameSession.getStatus().equals(GameStatus.IN_PROGRESS)) {
            throw new CargoMazeServicesException(CargoMazeServicesException.SESSION_IS_NOT_IN_PROGRESS);
        }
        Position newPosition = new Position(player.getPosition().getX() + direction.getX(),player.getPosition().getY() + direction.getY());
        return transactionsServices.movePlayer(playerId, newPosition, gameSessionId);
    }
}
