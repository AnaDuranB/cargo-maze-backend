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
import com.cargomaze.cargo_maze.services.exceptions.CargoMazeServicesException;
import com.mongodb.client.MongoClient;

import nonapi.io.github.classgraph.concurrency.SingletonMap.NewInstanceException;

import com.mongodb.client.ClientSession;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Criteria;

@Repository
public class CargoMazeDALImpl implements CargoMazeDAL {

    private MongoTemplate mongoTemplate;
    private MongoClient mongoClient;


    @Autowired
    public CargoMazeDALImpl(MongoTemplate mongoTemplate, MongoClient mongoClient) {
        this.mongoTemplate = mongoTemplate;
        this.mongoClient = mongoClient;
        GameSession baseSession = new GameSession("1");
        addSession(baseSession);
    }

    private static final String GAME_SESSION_ID = "sessionId";
    private static final String PLAYER_ID = "nickname";
    private static final String BOARD_ID = "id";
    private static final String BOX_ID = "id";

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
    public Player updatePlayerPosition(String playerId, Position currentPosition, long clientTimestamp)throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(playerId).and("lastModified").is(clientTimestamp));
        Update updatePosition = new Update().set("position", currentPosition).set("lastModified", System.currentTimeMillis());
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(false);

        return mongoTemplate.findAndModify(query, updatePosition, options, Player.class);
    }

    @Override
    public GameSession updateGameSessionById(String sessionId, GameSession session) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId));
        GameSession sessionInDataBase = mongoTemplate.findOne(query, GameSession.class);
        if (sessionInDataBase == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.GAME_SESSION_NOT_FOUND);
        }
        return mongoTemplate.save(session);
    }

    @Override
    public void updateGameSessionBoard(String sessionId, String playerId, Position direction) throws CargoMazePersistanceException {

        try (ClientSession session = mongoClient.startSession()) {
            session.startTransaction();
            try {
                GameSession gameSession = getSession(sessionId);
                Player player = getPlayerInSession(sessionId, playerId);
                if (!gameSession.getStatus().equals(GameStatus.IN_PROGRESS)) {
                        throw new CargoMazeServicesException(CargoMazeServicesException.SESSION_IS_NOT_IN_PROGRESS);
                }
                Position currentPosition = player.getPosition();
                Position newPosition = new Position(currentPosition.getX() + direction.getX(), currentPosition.getY() + direction.getY());
                Board board = gameSession.getBoard();
                if(isValidPlayerMove(currentPosition, newPosition, board)){
                    Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId));
                    long timestampCell1 = board.getCellAt(currentPosition).getLastModified();
                    long timestampCell2 = board.getCellAt(newPosition).getLastModified();
                    Cell cell1 = getCellAtWithTime(sessionId, currentPosition.getX(), currentPosition.getY(), timestampCell1);
                    Cell cell2 = getCellAtWithTime(sessionId, newPosition.getX(), newPosition.getY(), timestampCell2);
                    cell1.setState(Cell.EMPTY);
                    cell2.setState(Cell.PLAYER);
                    board.setCellAt(currentPosition, cell1);
                    board.setCellAt(newPosition, cell2);
                    gameSession.setBoard(board);
                    /*Cell cell1 = getCellAtWithTime(sessionId, currentPosition.getX(), currentPosition.getY(), timestampCell1);
                    Cell cell2 = getCellAtWithTime(sessionId, newPosition.getX(), newPosition.getY(), timestampCell2);
                    if(cell1 == null || cell2 == null){
                        throw new CargoMazePersistanceException(CargoMazePersistanceException.FAILED_TRANSACTION);
                    }*/
                    board.getCellAt(currentPosition).setLastModified(System.currentTimeMillis());
                    board.getCellAt(newPosition).setLastModified(System.currentTimeMillis());
                    Update updateBoard = new Update().set("board", board).set("lastModified", System.currentTimeMillis());
                    FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(false);
                    mongoTemplate.findAndModify(query, updateBoard, options, GameSession.class);
                    session.commitTransaction();
                    System.out.println("Papi le funcionó supuestamente POSICION ORIGINAL " +  currentPosition + " POSICION NUEVA " + newPosition);
                }
                else{
                    throw new CargoMazeServicesException(CargoMazePersistanceException.FAILED_TRANSACTION);
                }

            } catch (Exception e) {
                System.out.println("Papi no le funciono esa transaccion");
                session.abortTransaction();
                throw new CargoMazePersistanceException(CargoMazePersistanceException.FAILED_TRANSACTION);
            }
        }
    }


    @Override
    public GameSession updateGameSessionStatus(String sessionId, GameStatus status, long clientTimestamp )throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId).and("lastModified").is(clientTimestamp));
        Update updateStatus = new Update().set("status", status).set("lastModified", System.currentTimeMillis());
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(false);
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
    public Box getBoxWithTime(String gameSessionId, String boxId, long cellTimeSpam) throws CargoMazePersistanceException {
        Aggregation aggregation = Aggregation.newAggregation(
                // Filtrar por el gameSessionId
                Aggregation.match(Criteria.where("_id").is(gameSessionId)),
                // Descomponer el array 'board.boxes'
                Aggregation.unwind("$board.boxes"),
                // Filtrar por el boxId
                Aggregation.match(Criteria.where("board.boxes._id").is(boxId)),
                // Reemplazar el root por la celda encontrada
                Aggregation.replaceRoot("$board.boxes"),
                //Time
                Aggregation.match(Criteria.where("lastModified").is(cellTimeSpam)));

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
    public Box getBox(String gameSessionId, String boxId) throws CargoMazePersistanceException {
        Aggregation aggregation = Aggregation.newAggregation(
                // Filtrar por el gameSessionId
                Aggregation.match(Criteria.where("_id").is(gameSessionId)),
                // Descomponer el array 'board.boxes'
                Aggregation.unwind("$board.boxes"),
                // Filtrar por el boxId
                Aggregation.match(Criteria.where("board.boxes._id").is(boxId)),
                // Reemplazar el root por la celda encontrada
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
    public Cell getCellAtWithTime(String gameSessionId, int x, int y, long cellTimeSpam) throws CargoMazePersistanceException {
        // Crear el pipeline de agregación
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
                // Validar que el timestamp de la celda coincida con el esperado
                Aggregation.match(Criteria.where("lastModified").is(cellTimeSpam)));
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
    public Cell getCellAt(String gameSessionId, int x, int y) throws CargoMazePersistanceException {
        // Crear el pipeline de agregación
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
                Aggregation.replaceRoot("cell"));
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

    private boolean isValidBoxMove(Player player, Box box, Position currentPosition, Board board) {
        return player.getPosition().isAdjacent(box.getPosition()) &&
                board.isValidPosition(currentPosition) &&
                !board.hasWallAt(currentPosition) &&
                !board.hasBoxAt(currentPosition) &&
                !board.isPlayerAt(currentPosition);
    }

}
