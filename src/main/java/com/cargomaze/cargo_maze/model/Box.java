package com.cargomaze.cargo_maze.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class Box {
    @Id
    private String id;

    private long lastModified;
    private Position position;
    private boolean isAtTarget;
    

    public Box(String id, Position position) {
        this.id = id;
        this.position = position;
        this.isAtTarget = false;
        lastModified = System.currentTimeMillis();
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
        if(obj == null || obj.getClass() != this.getClass()){
            return false;
        }
        Box box = (Box) obj;
        return box.getId().equals(this.id);
    }
}