package com.cargomaze.cargo_maze.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.cargomaze.cargo_maze.model.GameSession;
import com.cargomaze.cargo_maze.model.Player;
import com.cargomaze.cargo_maze.persistance.exceptions.CargoMazePersistanceException;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Criteria;

@Repository
public class CargoMazeDALImpl implements CargoMazeDAL {

    private MongoTemplate mongoTemplate;

    private static final String GAME_SESSION_ID = "sessionId";
    private static final String PLAYER_ID = "nickname";


    @Autowired
    public CargoMazeDALImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        GameSession baseSession = new GameSession("1");
        addSession(baseSession);
    }

    @Override
    public int getPlayerCount(String gameSessionId) throws CargoMazePersistanceException{
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(gameSessionId));
        GameSession session = mongoTemplate.findOne(query, GameSession.class);

        if (session == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.GAME_SESSION_NOT_FOUND);
        }
        int playerCount = session.getPlayerCount();
        System.out.println("Player count: " + playerCount);
        return playerCount;
    }

    @Override
    public Player getPlayerInSession(String sessionId, String playerId) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(playerId));
        Player player = mongoTemplate.findOne(query, Player.class);
        if(player == null){
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_FOUND);
        }
        System.out.println("Player session: " + player.getGameSession() + " Session ID: " + sessionId);
        if(player.getGameSession() == null || !player.getGameSession().equals(sessionId)){
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_IN_SESSION);
        }
        return player;

    }

    @Override
    public Player getPlayer(String playerId) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(playerId));
        Player player = mongoTemplate.findOne(query, Player.class);
        if(player == null){
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_FOUND);
        }
        return player;
    }

    @Override
    public List<Player> getPlayers() {
        return mongoTemplate.findAll(Player.class);
    }

    @Override
    public List<Player> getPlayersInSession(String sessionId) {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId));
        GameSession session = mongoTemplate.findOne(query, GameSession.class);

        if (session == null) {
            throw new IllegalArgumentException("Session not found with ID: " + sessionId);
        }

        return session.getPlayers(); 
    }

    @Override
    public GameSession addSession(GameSession session) {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(session.getSessionId()));
        GameSession sessionInDataBase = mongoTemplate.findOne(query, GameSession.class);
        if (sessionInDataBase == null) {
            mongoTemplate.save(session); 
        }
        return sessionInDataBase; 
    }

    @Override
    public GameSession getSession(String sessionId) {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId));
        return mongoTemplate.findOne(query, GameSession.class);
    }

    @Override
    public Player addPlayer(Player player) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(player.getNickname()));
        Player playerInDataBase = mongoTemplate.findOne(query, Player.class);
        if (playerInDataBase != null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_ALREADY_EXISTS);
        }
        else{
            return mongoTemplate.save(player);
        }
    }

    @Override
    public void deletePlayer(Player player) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(player.getNickname()));
        Player playerInDataBase = mongoTemplate.findOne(query, Player.class);
        if (playerInDataBase == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_FOUND);
        }
        mongoTemplate.remove(query, Player.class);
    }

    @Override
    public void deletePlayers() throws CargoMazePersistanceException {
        mongoTemplate.dropCollection(Player.class);
    }

    @Override
    public Player updatePlayerById(String playerId) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(playerId));
        Player playerInDataBase = mongoTemplate.findOne(query, Player.class);
        if (playerInDataBase == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_FOUND);
        }
        else{
            return mongoTemplate.save(playerInDataBase);
        }
    }

    @Override
    public Player updatePlayer(Player player) throws CargoMazePersistanceException{
        return mongoTemplate.save(player);
    }

    @Override
    public GameSession updateGameSessionById(String sessionId) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId));
        GameSession sessionInDataBase = mongoTemplate.findOne(query, GameSession.class);
        if (sessionInDataBase == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.GAME_SESSION_NOT_FOUND);
        }
        else{
            return mongoTemplate.save(sessionInDataBase);
        }
    }

    @Override
    public GameSession updateGameSession(GameSession gameSession) throws CargoMazePersistanceException{
        return mongoTemplate.save(gameSession);
    }

    @Override
    public void deletePlayer(String playerId) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(playerId));
        Player player = mongoTemplate.findOne(query, Player.class);
        if (player == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_FOUND);
        }
        mongoTemplate.remove(query, Player.class);
    }

    @Override
    public void removePlayerFromSession(String playerId, String sessionId) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(playerId));
        Player player = mongoTemplate.findOne(query, Player.class);
        if (player == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_FOUND);
        }
        if (player.getGameSession() == null || !player.getGameSession().equals(sessionId)) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_IN_SESSION);
        }
        player.setGameSession(null);
        mongoTemplate.save(player);
    }
}

