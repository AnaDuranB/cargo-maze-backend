package com.cargomaze.cargo_maze.model;

import java.util.concurrent.locks.ReentrantLock;

public class Box {
    private String id;
    private Position position;
    private boolean isAtTarget;
    private Player currentMover;
    public final ReentrantLock lock = new ReentrantLock();

    public Box(String id, Position position) {
        this.id = id;
        this.position = position;
        this.isAtTarget = false;
    }

    public void move(Position newPosition){
        this.position = newPosition;
    }

    public void setCurrentMover(Player currentMover) {
        this.currentMover = currentMover;
    }
    public void cleanCurrentMover(){
        this.currentMover = null;
    }

    public void setAtTarget(boolean atTarget) {
        isAtTarget = atTarget;
    }

    // getters :)
    public String getId() {
        return id;
    }
    public Position getPosition() {
        return position;
    }
    
    
    public Player getCurrentMover() {
        return currentMover;
    }


    public boolean isAtTarget() {
        return isAtTarget;
    }
}
