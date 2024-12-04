package com.cargomaze.cargo_maze.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document
public class Player{
    @Id
    private String nickname;
    private int index;
    private Position position;
    private boolean isReady;
    private String gameSessionId = null; // no se sabe si es necesario (si se crean servicios directos del game session en teoria no)
    private long lastModified;

    public Player(String nickname) {
        this.nickname = nickname;
        this.isReady = false;
        this.index = -1;
        lastModified = System.currentTimeMillis();
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

    
    public void setLastModified(Long time){
        lastModified = time;
    }

    public long getLastModified(){
        return lastModified;
    }

    @Override 
    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(!(obj instanceof Player)){
            return false;
        }
        Player player = (Player) obj;
        return player.getNickname().equals(nickname);
    }
}