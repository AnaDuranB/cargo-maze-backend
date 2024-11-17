package com.cargomaze.cargo_maze.services;

import com.cargomaze.cargo_maze.persistance.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cargomaze.cargo_maze.model.GameSession;
import com.cargomaze.cargo_maze.model.GameStatus;
import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.model.Position;
import com.cargomaze.cargo_maze.persistance.impl.InMemoryCargoMazePersistance;
import com.cargomaze.cargo_maze.services.exceptions.CargoMazeServicesException;

import java.util.List;


@Service
public class CargoMazeServices {
    InMemoryCargoMazePersistance persistance;

    @Autowired
    @Qualifier("inMemoryCargoMazePersistance")
    public void setPersistance(InMemoryCargoMazePersistance persistance) {
        this.persistance = persistance;
    }

    public void createPlayer(String nickname) throws CargoMazePersistanceException {
        if (nickname == null || nickname.isEmpty()) {
            throw new CargoMazePersistanceException("Invalid nickname");
        }

        Player player = new Player(nickname);
        try {
            persistance.addPlayer(player);
        } catch (CargoMazePersistanceException e) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_ALREADY_EXISTS);
        }
    }

    public void addNewPlayerToGame(String nickname, String gameSessionId) throws CargoMazePersistanceException{
        GameSession session = persistance.getSession(gameSessionId);
        Player player = persistance.getPlayer(nickname);
        if (player == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_FOUND);
        }
        if(!session.getStatus().equals(GameStatus.WAITING_FOR_PLAYERS)){
            throw new CargoMazePersistanceException("Session is not waiting for players");
        }
        if (session.getPlayers().size() >= 4) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.FULL_SESSION_EXCEPTION);
        }
        session.addPlayer(player);
    }

    public void removePlayerFromGame(String nickname, String gameSessionId) throws CargoMazePersistanceException{
        persistance.removePlayerFromGame(nickname,gameSessionId);

    }

    public List<Player> getPlayersInSession(String gameSessionId) throws CargoMazePersistanceException {
        return persistance.getPlayersInSession(gameSessionId);
    }
    public void createSession(String sessionId) throws CargoMazePersistanceException{
        GameSession session = new GameSession(sessionId);
        persistance.addSession(session);
    }

    public GameSession getGameSession(String gameSessionId) throws CargoMazePersistanceException {
        return persistance.getSession(gameSessionId);
    }

    public Player getPlayer(String playerId) throws CargoMazePersistanceException {
        return persistance.getPlayer(playerId);
    }


    public int getPlayerCount(String gameSessionId) throws CargoMazePersistanceException{
        return persistance.getPlayerCount(gameSessionId);
    }

    public String[][] getBoardState(String gameSessionId) throws CargoMazePersistanceException {
        return persistance.getSession(gameSessionId).getBoardState();
    }


    public boolean movePlayer(String playerId, String gameSessionId, Position direction) throws CargoMazePersistanceException {
        Player player = persistance.getPlayer(playerId, gameSessionId);
        GameSession gameSession = persistance.getSession(gameSessionId); 
        Position newPosition = new Position(player.getPosition().getX() + direction.getX(), player.getPosition().getY() + direction.getY());
        return gameSession.movePlayer(player, newPosition); 
    }


    public void resetGameSession(String gameSessionId) throws CargoMazePersistanceException, CargoMazeServicesException {
        GameSession session = persistance.getSession(gameSessionId);
        if(!session.getStatus().equals(GameStatus.COMPLETED)){
            throw new CargoMazeServicesException(CargoMazeServicesException.SESSION_IS_NOT_FINISHED);
        }
        session.resetGame();

    }
}

