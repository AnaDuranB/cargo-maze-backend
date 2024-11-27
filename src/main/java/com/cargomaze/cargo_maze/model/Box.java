package com.cargomaze.cargo_maze.model;

import java.util.concurrent.locks.ReentrantLock;

public class Box {
    private String id;
    private Position position;
    private boolean isAtTarget;
    public final ReentrantLock lock = new ReentrantLock();

    public Box(String id, Position position) {
        this.id = id;
        this.position = position;
        this.isAtTarget = false;
    }

    public void move(Position newPosition){
        this.position = newPosition;
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
    
    public boolean isAtTarget() {
        return isAtTarget;
    }
}
