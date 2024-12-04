package com.cargomaze.cargo_maze.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

import com.cargomaze.cargo_maze.model.*;
import com.cargomaze.cargo_maze.persistance.exceptions.CargoMazePersistanceException;
import com.cargomaze.cargo_maze.services.exceptions.CargoMazeServicesException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

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
    public Player updatePlayerPosition(String playerId, Position newPosition)
            throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(PLAYER_ID).is(playerId));
        Update updatePosition = new Update().set("position", newPosition);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

        return mongoTemplate.findAndModify(query, updatePosition, options, Player.class);
    }

    @Override
    public GameSession updateGameSessionById(String sessionId, GameSession session)
            throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId));
        GameSession sessionInDataBase = mongoTemplate.findOne(query, GameSession.class);
        if (sessionInDataBase == null) {
            throw new CargoMazePersistanceException(CargoMazePersistanceException.GAME_SESSION_NOT_FOUND);
        }
        return mongoTemplate.save(session);
    }

    @Override
    public GameSession updateGameSessionBoard(String sessionId, Board board) throws CargoMazePersistanceException {
        Query query = new Query(Criteria.where(GAME_SESSION_ID).is(sessionId));
        Update updateBoard = new Update().set("board", board);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);
        return mongoTemplate.findAndModify(query, updateBoard, options, GameSession.class);
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

    @Override
    public boolean movePlayerTrasactionally(String playerId, String sessionId, Position playerPosition,
            Position newPosition) throws CargoMazePersistanceException {
        try (ClientSession session = mongoClient.startSession()) {
            session.startTransaction();
            try {
                Player player = getPlayerInSession(sessionId, playerId);
                GameSession gameSession = getSession(sessionId);
                Board board = gameSession.getBoard();
                Cell currentCell = getCellAt(sessionId, playerPosition.getX(), playerPosition.getY());
                Cell newCell = getCellAt(sessionId, newPosition.getX(), newPosition.getY());

                /*if (!gameSession.getStatus().equals(GameStatus.IN_PROGRESS)) {
                    session.abortTransaction();
                    throw new CargoMazeServicesException(CargoMazeServicesException.SESSION_IS_NOT_IN_PROGRESS);
                }

                if (!player.getPosition().equals(playerPosition)) {
                    session.abortTransaction();
                }*/


                player.updatePosition(newPosition);
                updatePlayerPosition(playerId, newPosition);

                currentCell.setState(Cell.EMPTY);

                newCell.setState(Cell.PLAYER);

                board.setCellAt(playerPosition, currentCell);
                board.setCellAt(newPosition, newCell);
                updateGameSessionBoard(sessionId, board);

                session.commitTransaction();
                return true;
            } catch (Exception e) {
                session.abortTransaction(); // Anular la transacción en caso de error
                return false;
            }
        }
    }

    @Override
    public boolean movePlayerWithBoxTransactionally(String playerId, String sessionId, Position playerPosition,
        Position newPlayerPosition, Position newBoxPosition) throws CargoMazePersistanceException {
        try (ClientSession clientSession = mongoClient.startSession()) {
            clientSession.startTransaction();
            try {
                Player player = getPlayerInSession(sessionId, playerId);
                GameSession session = getSession(sessionId);
                Board board = session.getBoard();
                Box box = board.getBoxAt(newPlayerPosition);
                if(box == null){
                    clientSession.abortTransaction();
                    return false;
                }
                box.move(newBoxPosition); // se cambia el lugar donde esta la caja
                player.updatePosition(newPlayerPosition); // se cambia el lugar donde esta el jugador
                if (board.isTargetAt(newBoxPosition)) {
                    box.setAtTarget(true);
                    boolean allOtherBoxesAtTarget = board.getBoxes().stream()
                            .filter(b -> !b.equals(box))
                            .allMatch(Box::isAtTarget);
                    if (allOtherBoxesAtTarget) {
                        session.setStatus(GameStatus.COMPLETED);

                    }
                } 
                else if (board.isTargetAt(newPlayerPosition)) {
                    box.setAtTarget(false);
                }
                Cell cell1 = getCellAt(sessionId, newBoxPosition.getX(), newBoxPosition.getY());
                cell1.setState(Cell.BOX);
                board.setCellAt(newBoxPosition, cell1);

                Cell cell2 = getCellAt(sessionId, newPlayerPosition.getX(), newPlayerPosition.getY());
                cell2.setState(Cell.PLAYER);
                board.setCellAt(newPlayerPosition, cell2);
                session.setBoard(board);

                updatePlayerPosition(playerId, newPlayerPosition);
                updateGameSessionBoard(session.getSessionId(), board);
                clientSession.commitTransaction();
                return true;
            } catch (Exception e) {
                clientSession.abortTransaction();
                return false;
            }
        }

    }

}
