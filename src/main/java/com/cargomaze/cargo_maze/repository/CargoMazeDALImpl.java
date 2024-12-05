package com.cargomaze.cargo_maze.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

import com.cargomaze.cargo_maze.model.*;
import com.cargomaze.cargo_maze.persistance.exceptions.CargoMazePersistanceException;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Criteria;

@Repository
public class CargoMazeDALImpl implements CargoMazeDAL {

    private MongoTemplate mongoTemplate;

    private static final String GAME_SESSION_ID = "sessionId";
    private static final String PLAYER_ID = "nickname";
    private static final String BOARD_ID = "id";
    private static final String BOX_ID = "id";

    @Autowired
    public CargoMazeDALImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        GameSession baseSession = new GameSession("1");
        addSession(baseSession);
    }

    @Override
    public int getPlayerCount(String gameSessionId) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(gameSessionId));
        GameSession session = mongoTemplate.findOne(query, GameSession.class);

        if (session == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.GAME_SESSION_NOT_FOUND);
        }
        int playerCount = session.getPlayerCount();
        return playerCount;
    }

    @Override
    public Player getPlayerInSession(String sessionId, String playerId) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(playerId));
        Player player = mongoTemplate.findOne(query, Player.class);
        if (player == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_FOUND);
        }
        if (player.getGameSession() == null || !player.getGameSession().equals(sessionId)) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_IN_SESSION);
        }
        return player;

    }

    @Override
    public Player getPlayer(String playerId) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(playerId));
        Player player = mongoTemplate.findOne(query, Player.class);
        if (player == null) {
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
    public Player getPlayerInSessionBlockingIt(String sessionId, String playerId) throws CargoMazePersistanceException {
        Query lockQuery = new Query(Criteria.where(PLAYER_ID).is(playerId).and("locked").is(false));
        Update lockUpdate = new Update().set("locked", true);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(false);
        Player lockedPlayer = mongoTemplate.findAndModify(lockQuery, lockUpdate, options, Player.class);

        if (lockedPlayer == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_FOUND);
        }
        if (lockedPlayer.getGameSession() == null || !lockedPlayer.getGameSession().equals(sessionId)) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_NOT_IN_SESSION);
        }
        return lockedPlayer;

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
    public GameSession getSession(String sessionId) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId));
        GameSession sessionInDB = mongoTemplate.findOne(query, GameSession.class);
        if (sessionInDB == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.GAME_SESSION_NOT_FOUND);
        }
        return sessionInDB;
    }

    @Override
    public GameSession getSessionBlockingIt(String sessionId) throws CargoMazePersistanceException {
        Query lockQuery = new Query(Criteria.where("_id").is(sessionId).and("locked").is(false));
        Update lockUpdate = new Update().set("locked", true);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(false);
        GameSession lockedSession = mongoTemplate.findAndModify(lockQuery, lockUpdate, options, GameSession.class);

        if (lockedSession == null) {
            throw new CargoMazePersistanceException("Game session is locked or does not exist.");
        }

        return lockedSession;
    }

    @Override
    public Player addPlayer(Player player) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(player.getNickname()));
        Player playerInDataBase = mongoTemplate.findOne(query, Player.class);
        if (playerInDataBase != null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.PLAYER_ALREADY_EXISTS);
        } else {
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
        } else {
            return mongoTemplate.save(playerInDataBase);
        }
    }

    @Override
    public Player updatePlayer(Player player) throws CargoMazePersistanceException {
        return mongoTemplate.save(player);
    }

    @Override
    public Player updatePlayerPosition(String playerId, Position newPosition) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(playerId));
        Update updatePosition = new Update().set("position", newPosition).set("locked", false);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);
        return mongoTemplate.findAndModify(query, updatePosition, options, Player.class);
    }

    @Override
    public Player updatePlayerLocked(String playerId, boolean locked) throws CargoMazePersistanceException{
        Query query = new Query(Criteria.where(PLAYER_ID).is(playerId));
        Update updateLocked = new Update().set("locked", locked);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);
        return mongoTemplate.findAndModify(query, updateLocked, options, Player.class);
    }

    @Override
    public GameSession updateGameSessionById(String sessionId) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId));
        GameSession sessionInDataBase = mongoTemplate.findOne(query, GameSession.class);
        if (sessionInDataBase == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.GAME_SESSION_NOT_FOUND);
        } else {
            return mongoTemplate.save(sessionInDataBase);
        }
    }

    @Override
    public GameSession updateGameSession(GameSession session) throws CargoMazePersistanceException {
        return mongoTemplate.save(session);
    }

    @Override
    public GameSession updateGameSessionBoard(String sessionId, Board board) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId));
        Update updateBoard = new Update().set("board", board).set("locked", false);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);
        return mongoTemplate.findAndModify(query, updateBoard, options, GameSession.class);
    }

    @Override
    public GameSession updateGameSessionLocked(String sessionId, boolean locked) throws CargoMazePersistanceException{
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId));
        Update updateLocked = new Update().set("locked", locked);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);
        return mongoTemplate.findAndModify(query, updateLocked, options, GameSession.class);
    }

    @Override
    public GameSession updateGameSessionStatus(String sessionId, GameStatus status)
            throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId));
        Update updateStatus = new Update().set("status", status);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);
        return mongoTemplate.findAndModify(query, updateStatus, options, GameSession.class);
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

    @Override
    public Box getBox(String gameSessionId, String boxId) throws CargoMazePersistanceException {
        Aggregation aggregation = Aggregation.newAggregation(
                // Filtrar por el gameSessionId
                Aggregation.match(Criteria.where("_id").is(gameSessionId)),
                // Descomponer el array 'board.boxes'
                Aggregation.unwind("$board.boxes"),
                // Filtrar por el boxId
                Aggregation.match(Criteria.where("board.boxes._id").is(boxId)),
                // Reemplazar la raíz con el objeto de la caja
                Aggregation.replaceRoot("$board.boxes"));

        // Ejecutar la consulta
        AggregationResults<Box> result = mongoTemplate.aggregate(aggregation, "gameSession", Box.class);
        // Obtener el resultado único
        Box box = result.getUniqueMappedResult();
        // Si no se encuentra el box, lanzamos una excepción personalizada
        if (box == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.BOX_NOT_FOUND);
        }

        return box;
    }

    @Override
    public List<Box> getBoxes(String gameSessionId) throws CargoMazePersistanceException {
        // Validación de parámetros
        if (gameSessionId == null || gameSessionId.isEmpty()) {
            throw new IllegalArgumentException("El gameSessionId no puede ser nulo ni vacío.");
        }

        // Pipeline de Aggregation para obtener todas las boxes de la sesión
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where(GAME_SESSION_ID).is(gameSessionId)), // Filtra por sessionId
                Aggregation.unwind("$board.boxes"), // Descompone el array "boxes" en documentos individuales
                Aggregation.replaceRoot("$board.boxes") // Cambia la raíz del resultado a "board.boxes"
        );

        // Ejecutar la consulta
        AggregationResults<Box> result = mongoTemplate.aggregate(aggregation, "gameSession", Box.class);

        // Obtener los resultados en una lista
        List<Box> boxes = result.getMappedResults();

        // Si no se encuentran cajas, lanzamos una excepción personalizada
        if (boxes.isEmpty()) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.BOXES_NOT_FOUND);
        }

        return boxes;
    }

    @Override
    public Cell getCellAt(String gameSessionId, int x, int y) throws CargoMazePersistanceException {
        Aggregation aggregation = Aggregation.newAggregation(
                // Filtrar por el ID de la sesión
                Aggregation.match(Criteria.where("_id").is(gameSessionId)),
                // Proyectar para obtener el primer elemento de "board.cells"
                Aggregation.project()
                        .and(ArrayOperators.ArrayElemAt.arrayOf("$board.cells").elementAt(x)).as("cells"),
                // Proyectar el primer elemento de "cells" (en este caso la celda que queremos)
                Aggregation.project()
                        .and(ArrayOperators.ArrayElemAt.arrayOf("$cells").elementAt(y)).as("cell"),
                // Reemplazar el root por la celda encontrada
                Aggregation.replaceRoot("cell"),

                Aggregation.match(Criteria.where("locked").is(false)));

        // Ejecutar la agregación sobre la colección "gameSession" y obtener el
        // resultado mapeado a Cell
        AggregationResults<Cell> result = mongoTemplate.aggregate(aggregation, "gameSession", Cell.class);
        // Obtener el único resultado mapeado
        Cell cell = result.getUniqueMappedResult();
        // Si no se encuentra la celda, lanzar una excepción
        if (cell == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.CELL_NOT_FOUND);
        }
        return cell;
    }

    @Override
    public Box getBoxAt(String gameSessionId, Position boxPosition) throws CargoMazePersistanceException {
        Aggregation aggregation = Aggregation.newAggregation(
                // Filtrar por el gameSessionId
                Aggregation.match(Criteria.where("_id").is(gameSessionId)),
                // Descomponer el array 'board.boxes'
                Aggregation.unwind("$board.boxes"),
                // Filtrar por el boxId
                Aggregation.match(Criteria.where("board.boxes.position.x").is(boxPosition.getX())
                        .and("board.boxes.position.y").is(boxPosition.getY())),
                // Reemplazar la raíz con el objeto de la caja
                Aggregation.replaceRoot("$board.boxes"));

        // Ejecutar la consulta
        AggregationResults<Box> result = mongoTemplate.aggregate(aggregation, "gameSession", Box.class);
        // Obtener el resultado único
        Box box = result.getUniqueMappedResult();
        // Si no se encuentra el box, lanzamos una excepción personalizada
        if (box == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.BOX_NOT_FOUND);
        }

        return box;

    }
}
