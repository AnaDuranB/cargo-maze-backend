package com.cargomaze.cargo_maze.model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class GameSession {
    private String sessionId;
    private List<Player> players;
    private GameStatus status;
    private Board board;
    private LinkedList<Integer> indexes = new LinkedList<>(Arrays.asList(0, 1, 2, 3));

    public GameSession(String sessionId) {
        this.sessionId = sessionId;
        players = new ArrayList<>();
        status = GameStatus.WAITING_FOR_PLAYERS;
        board = new Board(); //Sera una instancia inyectada (para más mapas en el futuro)
    }

    public void verifyStartingCondition(){
        if(players.size() == 4 && players.stream().allMatch(Player::isReady)){
            status = GameStatus.IN_PROGRESS;
        }
    }

    public boolean addPlayer(Player player) {
        if (players.size() >= 4 || status != GameStatus.WAITING_FOR_PLAYERS || player.getIndex() != -1) {
            return false;
        }
        player.setIndex(indexes.poll());
        player.setGameSession(sessionId);
        player.setReady(true);
        players.add(player);
        assignPlayerStartPosition(player);
        verifyStartingCondition();
        return true;
    }

    public int getPlayerCount() {
        return players.size();
    }

    private void assignPlayerStartPosition(Player player) {
        Position startPosition = board.getPlayerStartPosition(player.getIndex());
        player.updatePosition(startPosition);
        board.setPlayerInBoard(startPosition);
    }

    public boolean moveBox(Player player, Position playerPosition, Position boxPosition) {
        Position boxNewPosition = getPositionFromMovingABox(boxPosition, playerPosition); // Validates all postions (in theory);
        Box box = board.getBoxAt(boxPosition);
        if (isValidBoxMove(player, box, boxNewPosition)) {
            if (box.lock.tryLock() && board.getCellAt(boxNewPosition).lock.tryLock()) { // Lockeamos tanto la caja a mover y la celda a donde se va mover la caja
                try {
                    box.move(boxNewPosition); // se cambia el lugar donde esta la caja
                    if(board.isTargetAt(boxNewPosition)) {
                        box.setAtTarget(true);
                        boolean allOtherBoxesAtTarget = board.getBoxes().stream()
                        .filter(b -> !b.equals(box))
                        .allMatch(Box::isAtTarget);
                        if(allOtherBoxesAtTarget){
                            status = GameStatus.COMPLETED;
                        }
                    } // si la caja esta en un target
                    else if(board.isTargetAt(boxPosition)){
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


    private boolean isValidPlayerMove(Position currentPosition, Position newPosition){
        return currentPosition.isAdjacent(newPosition) && board.isValidPosition(newPosition) && !board.hasWallAt(newPosition) && !board.isPlayerAt(newPosition);
    }

    public boolean movePlayer(Player player, Position newPosition) {
        if (status != GameStatus.IN_PROGRESS) {
            return false;
        }

        Position currentPos = player.getPosition();

        if (isValidPlayerMove(currentPos, newPosition)){
            if(board.hasBoxAt(newPosition)){
                boolean moveBox = moveBox(player, currentPos, newPosition);
                if(!moveBox){
                    return false;
                }
            }
            ReentrantLock lock = board.getCellAt(newPosition).lock;
            if(lock.tryLock()){ // se bloquea la celda a donde se va a mover el jugador por si alguno otro intenta acceder a este.
                try{
                    player.updatePosition(newPosition);
                    board.getCellAt(currentPos).setState(Cell.EMPTY); //se
                    board.getCellAt(newPosition).setState(Cell.PLAYER);
                }
                finally{
                    lock.unlock();
                }
                return true;
            }
        }
        return false;
    }

    private Position getPositionFromMovingABox(Position boxPosition, Position playerPosition) {
        //Eje y del jugador es menor al de la caja
        if(playerPosition.getY() < boxPosition.getY()){
            return new Position(boxPosition.getX(), boxPosition.getY() + 1);
        }
        //Eje y del jugador es mayor al de la caja
        else if(playerPosition.getY() > boxPosition.getY()){
            return new Position(boxPosition.getX(), boxPosition.getY() - 1);
        }
        //Eje x del jugador es menor al de la caja
        else if(playerPosition.getX() < boxPosition.getX()){
            return new Position(boxPosition.getX() + 1, boxPosition.getY());
        }
        return  new Position(boxPosition.getX() - 1, boxPosition.getY());
    }

    private boolean isValidBoxMove(Player player, Box box, Position newPosition) {
        return player.getPosition().isAdjacent(box.getPosition()) &&
                board.isValidPosition(newPosition) &&
                !board.hasWallAt(newPosition) &&
                !board.hasBoxAt(newPosition) &&
                !board.isPlayerAt(newPosition);
    }

    public void startGame() {
        if (players.size() == 4 && players.stream().allMatch(Player::isReady)) {
            status = GameStatus.IN_PROGRESS;
        }
    }

    

    public void updateGameStatus() {
        if (board.isComplete()) {
            status = GameStatus.COMPLETED;
        }
    }

    public Player findPlayerByIndex(Player player) {
        if(players.isEmpty()){
            return null;
        }
        return players.get(player.getIndex());
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public GameStatus getStatus() {
        return status;
    }

    public Board getBoard() {
        return board;
    }

    public String[][] getBoardState(){
        return board.getBoardState();
    }

    public void removePlayer(Player player) {
        board.setCellState(player.getPosition(), Cell.EMPTY);
        players.remove(player);
        if(status.equals(GameStatus.WAITING_FOR_PLAYERS)){
            indexes.add(player.getIndex());
        }
        player.setIndex(-1);
        player.updatePosition(null);
        if(GameStatus.RESETING_GAME.equals(status) && players.isEmpty()){
            status = GameStatus.WAITING_FOR_PLAYERS;
        }

        else if(players.isEmpty() && GameStatus.IN_PROGRESS.equals(status)){
            board.reset();
            status = GameStatus.WAITING_FOR_PLAYERS;
        }
    }

    public void resetGame(){
        status = GameStatus.RESETING_GAME;
        board.reset();
        indexes.addAll(Arrays.asList(0, 1, 2, 3));
    }
}

