package com.cargomaze.cargo_maze.persistance.exceptions;

public class CargoMazePersistanceException extends Exception {

    public static final String FULL_SESSION_EXCEPTION = "The session is full";
    public static final String GAME_SESSION_ALREADY_EXISTS = "The game session already exists";
    public static final String GAME_SESSION_NOT_FOUND = "The game session was not found";
    public static final String INVALID_NICKNAME_EXCEPTION = "The nickname is invalid";
    public static final String PLAYER_ALREADY_EXISTS = "The player already exists";
    public static final String PLAYER_NOT_FOUND = "The player was not found";
    public static final String PLAYER_NOT_IN_SESSION = "The player is not in the session";
    public static final String PLAYER_ALREADY_IN_SESSION = "The player is already in a session";

    public CargoMazePersistanceException(String message) {
        super(message);
    }

    public CargoMazePersistanceException(String message, Throwable cause) {
        super(message, cause);
    }
}