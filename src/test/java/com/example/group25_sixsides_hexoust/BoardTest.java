package com.example.group25_sixsides_hexoust;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;


class BoardTest {
    private Board board;
    private ArrayList<HexCube> test;

    @BeforeEach
    void setup() { //Make smaller board to test functions and how they work for Board file
        test = new ArrayList<>();
        test.add(new HexCube(0, 0, 0));
        test.add(new HexCube(1, -1, 0));
        test.add(new HexCube(1, 0, -1));
        test.add(new HexCube(0, 1, -1));
        test.add(new HexCube(-1, 1, 0));
        test.add(new HexCube(-1, 0, 1));
        test.add(new HexCube(0, -1, 1));

        board = new Board(test);
    }

    @Test
    void testRedValidMove() {
        ArrayList<HexCube> neighbors = board.getNeighbors(test.get(1));
        assertTrue(board.validateForRed(test.get(1), neighbors));
    }

    @Test
    void testBlueValidMove() {
        ArrayList<HexCube> neighbors = board.getNeighbors(test.get(2));
        assertTrue(board.validateForBlue(test.get(2), neighbors));
    }

    @Test
    void testOccupiedHexInvalid() {
        ArrayList<HexCube> neighbors = board.getNeighbors(test.get(0));
        board.validateForRed(test.get(0), neighbors);
        assertFalse(board.validateForRed(test.get(0), neighbors));
    }

    @Test
    void testCaptureLeadsToWin() {

        board.validateForRed(test.get(1), board.getNeighbors(test.get(1)));
        board.validateForBlue(test.get(0), board.getNeighbors(test.get(0)));
        board.validateForBlue(test.get(2), board.getNeighbors(test.get(2)));

        //Blue captures Red at 1 using move at 3
        assertTrue(board.validateCapturingMove(test.get(3), true));
        assertTrue(board.checkWin()); //Red has no hexes, should be true
    }
}