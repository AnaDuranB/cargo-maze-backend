package com.cargomaze.cargo_maze.model;

public class Player{
    private int index;
    private String nickname;
    private Position position;
    private boolean isReady;
    private String gameSessionId = null; // no se sabe si es necesario (si se crean servicios directos del game session en teoria no)

    public Player(String nickname) {
        this.nickname = nickname;
        this.isReady = false;
        this.index = -1;
    }

    public void setGameSession(String newGameSessionId) {
        this.gameSessionId = newGameSessionId;
    }

    public void setReady(boolean ready) {
        this.isReady = ready;
    }

    public void updatePosition(Position newPosition){
        position = newPosition;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    // getters :)
    public String getNickname() {
        return nickname;
    }
    public Position getPosition() {
        return position;
    }
    public boolean isReady() {
        return isReady;
    }

    public int getIndex() {
        return index;
    }

    public String getGameSession() {
        return gameSessionId;
    }
}