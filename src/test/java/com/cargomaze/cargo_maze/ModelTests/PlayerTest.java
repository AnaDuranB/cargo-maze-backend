
package com.cargomaze.cargo_maze.ModelTests;

import com.cargomaze.cargo_maze.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    private GameSession gameSession;
    private Player player1, player2, player3, player4;
    private Board board;

    /*@BeforeEach
    public void setUp() {
        gameSession = new GameSession("session1");
        board = gameSession.getBoard();
        player1 = new Player("p1");
        player2 = new Player("p2");
        player3 = new Player("p3");
        player4 = new Player("p4");

        gameSession.addPlayer(player1);
        gameSession.addPlayer(player2);
        gameSession.addPlayer(player3);
        gameSession.addPlayer(player4);

        player1.setReady(true);
        player2.setReady(true);
        player3.setReady(true);
        player4.setReady(true);
        gameSession.startGame();
    }

    @Test
    void testValidPlayerMove() {

        Position initialPos = player1.getPosition();
        Position newPos = new Position(initialPos.getX() + 1, initialPos.getY());

        assertTrue(board.isValidPosition(newPos));
        assertFalse(board.hasWallAt(newPos));
        assertFalse(board.hasBoxAt(newPos));

        boolean moved = gameSession.movePlayer(player1, newPos);

        assertTrue(moved);
        assertEquals(newPos, player1.getPosition());


    }

    @Test
    void testValidPlayerMoveBoxUpToDown() {

        Position initialPos = player1.getPosition();
        Position newPos0 = new Position(initialPos.getX() + 1, initialPos.getY());
        assertTrue(gameSession.movePlayer(player1,newPos0));

        Position newPos1 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos1));

        Position newPos2 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos2));

        Position newPos3 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos3));

        Position newPos4 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos4));

        // verificar movimiento de la caja
        assertInstanceOf(Box.class, gameSession.getBoard().getBoxAt(new Position(newPos4.getX(), newPos4.getY() + 1)));

    }

    @Test
    void testValidPlayerMoveBoxLeftToRight() {
        
        Position newPos1 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos1));

        Position newPos2 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos2));

        Position newPos3 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos3));

        Position newPos4 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos4));

        Position newPos5 = new Position(player1.getPosition().getX() + 1, player1.getPosition().getY());
        assertTrue(gameSession.movePlayer(player1, newPos5));

        assertInstanceOf(Box.class, gameSession.getBoard().getBoxAt(new Position(newPos5.getX() + 1, newPos5.getY())));

    }

    @Test
    void testValidPlayerMoveBoxDownToUp() {
        
        Position newPos1 = new Position(player2.getPosition().getX() + 1, player2.getPosition().getY());
        assertTrue(gameSession.movePlayer(player2, newPos1));

        Position newPos2 = new Position(player2.getPosition().getX(), player2.getPosition().getY() - 1);
        assertTrue(gameSession.movePlayer(player2, newPos2));

        Position newPos3 = new Position(player2.getPosition().getX(), player2.getPosition().getY() - 1);
        assertTrue(gameSession.movePlayer(player2, newPos3));

        Position newPos4 = new Position(player2.getPosition().getX(), player2.getPosition().getY() - 1);
        assertTrue(gameSession.movePlayer(player2, newPos4));

        assertInstanceOf(Box.class, gameSession.getBoard().getBoxAt(new Position(newPos4.getX(), newPos4.getY() - 1)));

    }

    @Test
    void testValidPlayerMoveRightToLeft() {
        
        Position newPos1 = new Position(player2.getPosition().getX() + 1, player2.getPosition().getY());
        assertTrue(gameSession.movePlayer(player2, newPos1));

        Position newPos2 = new Position(player2.getPosition().getX(), player2.getPosition().getY() - 1);
        assertTrue(gameSession.movePlayer(player2, newPos2));

        Position newPos3 = new Position(player2.getPosition().getX(), player2.getPosition().getY() - 1);
        assertTrue(gameSession.movePlayer(player2, newPos3));

        Position newPos4 = new Position(player2.getPosition().getX() + 1, player2.getPosition().getY());
        assertTrue(gameSession.movePlayer(player2, newPos4));

        Position newPos5 = new Position(player2.getPosition().getX(), player2.getPosition().getY() - 1);
        assertTrue(gameSession.movePlayer(player2, newPos5));

        Position newPos6 = new Position(player2.getPosition().getX() - 1, player2.getPosition().getY());
        assertTrue(gameSession.movePlayer(player2, newPos6));

        assertInstanceOf(Box.class, gameSession.getBoard().getBoxAt(new Position(newPos6.getX() - 1, newPos6.getY())));

    }

    @Test
    void testInvalidPlayerMove() {
        
        Position initialPos = player1.getPosition();
        Position newPos = new Position(initialPos.getX() + 1, initialPos.getY() + 1);

        assertFalse(board.hasWallAt(newPos));
        assertFalse(board.hasBoxAt(newPos));

        boolean moved = gameSession.movePlayer(player1, newPos);

        assertFalse(moved);
        assertNotEquals(newPos, player1.getPosition());

        
    }

    @Test
    void testInvalidPlayerMove_Wall() {
        // try to move into a wall
        Position wallPos = new Position(0, 1);

        boolean moved = gameSession.movePlayer(player1, wallPos);

        assertFalse(moved);
        assertNotEquals(wallPos, player1.getPosition());
    }

    @Test
    void testInvalidPlayerMove_OutOfBounds() {
        // try to move outside the board
        Position outPos = new Position(-1, -1);
        boolean moved = gameSession.movePlayer(player1, outPos);

        assertFalse(moved);
        assertNotEquals(outPos, player1.getPosition());
    }

    @Test
    void testInvalidPlayerMove_OtherPlayer() {
        // try to move to another player's position
        Position player2Pos = player2.getPosition();

        
        Position newPos1 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos1));

        Position newPos2 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos2));

        Position newPos3 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos3));

        Position newPos4 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos4));

        Position newPos5 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos5));

        Position newPos6 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos6));

        Position newPos7 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertFalse(gameSession.movePlayer(player1, newPos7));

        assertEquals(newPos6, player1.getPosition());
        assertNotEquals(player2Pos, player1.getPosition());

    }

    @Test
    void testInvalidPlayerMoveBoxToWall() {
        
        Position newPos1 = new Position(player2.getPosition().getX() + 1, player2.getPosition().getY());
        assertTrue(gameSession.movePlayer(player2, newPos1));

        Position newPos2 = new Position(player2.getPosition().getX(), player2.getPosition().getY() - 1);
        assertTrue(gameSession.movePlayer(player2, newPos2));

        Position newPos3 = new Position(player2.getPosition().getX(), player2.getPosition().getY() - 1);
        assertTrue(gameSession.movePlayer(player2, newPos3));

        Position newPos4 = new Position(player2.getPosition().getX() + 1, player2.getPosition().getY());
        assertTrue(gameSession.movePlayer(player2, newPos4));

        Position newPos5 = new Position(player2.getPosition().getX(), player2.getPosition().getY() - 1);
        assertTrue(gameSession.movePlayer(player2, newPos5));

        Position newPos6 = new Position(player2.getPosition().getX() - 1, player2.getPosition().getY());
        assertTrue(gameSession.movePlayer(player2, newPos6));

        Position newPos7 = new Position(player2.getPosition().getX() - 1, player2.getPosition().getY());
        assertFalse(gameSession.movePlayer(player2, newPos7)); // No permite mover ni la caja ni el jugador a una nueva posición.

        assertInstanceOf(Box.class, gameSession.getBoard().getBoxAt(new Position(newPos6.getX() - 1, newPos6.getY())));
        assertEquals(newPos6, player2.getPosition());

    }

    @Test
    void testValidPlayerMoveBoxToAnotherBox() {
        
        Position initialPos = player1.getPosition();
        Position newPos0 = new Position(initialPos.getX() + 1, initialPos.getY());
        assertTrue(gameSession.movePlayer(player1, newPos0));

        Position newPos1 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos1));

        Position newPos2 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos2));

        Position newPos3 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos3));

        Position newPos4 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos4));

        Position newPos5 = new Position(player1.getPosition().getX() - 1, player1.getPosition().getY());
        assertTrue(gameSession.movePlayer(player1, newPos5));

        Position newPos6 = new Position(player1.getPosition().getX(), player1.getPosition().getY() + 1);
        assertTrue(gameSession.movePlayer(player1, newPos6));

        Position newPos7 = new Position(player1.getPosition().getX() + 1, player1.getPosition().getY());
        assertTrue(gameSession.movePlayer(player1, newPos7));

        Position newPos8 = new Position(player1.getPosition().getX() + 1, player1.getPosition().getY());
        assertFalse(gameSession.movePlayer(player1, newPos8)); // Se verifica que no se mueven cajas continuas

        // verificar movimiento de la caja
        assertInstanceOf(Box.class, gameSession.getBoard().getBoxAt(new Position(newPos7.getX() + 1, newPos7.getY())));
        assertEquals(newPos7, player1.getPosition());

    }*/
}

