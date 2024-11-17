package com.cargomaze.cargo_maze.model;

import java.util.concurrent.locks.ReentrantLock;

public class Cell {
    public static final String EMPTY =  "EMPTY";
    public static final String TARGET =  "TARGET";
    public static final String WALL =  "WALL";
    public static final String PLAYER =  "PLAYER";
    public static final String BOX =  "BOX";
    public static final String BOX_ON_TARGET =  "BOX_ON_TARGET";
    public static final String PLAYER_ON_TARGET =  "PLAYER_ON_TARGET";
    private String state = "";
    public final ReentrantLock lock = new ReentrantLock();

    public Cell(String state){
        this.state = state;
    }

    public void setState(String newState){
        if(state.equals(Cell.TARGET) && newState.equals(Cell.BOX)){
            state = Cell.BOX_ON_TARGET;
        }
        else if((state.equals(Cell.TARGET) && newState.equals(Cell.PLAYER)) || (state.equals(Cell.BOX_ON_TARGET) && newState.equals(Cell.PLAYER)) ){
            state = Cell.PLAYER_ON_TARGET; 
        }
        else if((state.equals(Cell.BOX_ON_TARGET) || state.equals(Cell.PLAYER_ON_TARGET))&& newState.equals(Cell.EMPTY)){
            state = Cell.TARGET;
        }
        else{
            state = newState;
        }
    }

    public String getState(){
        return state;
    }

}