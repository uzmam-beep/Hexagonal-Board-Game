package com.example.group25_sixsides_hexoust;
import java.util.ArrayList;

/**
 * The Controller class is responsible for managing the game state and handling player moves.
 * It validates moves, checks for captures, updates the game state, and determines if a player has won.
 * This class interacts with the Board to manipulate the game state and manage turns.
 */
public class Controller {

    /**
     * Enum representing the possible states of the game.
     */
    public enum State { BLUE_TURN, RED_TURN, BLUE_WON, RED_WON }

    private State state = State.RED_TURN;
    private Board board;

    /**
     * Constructs a Controller instance with a given set of cube coordinates.
     * Initializes the game board and sets game state.
     *
     * @param cubeCoordinates A list of hex coordinates representing the game board.
     */
    public Controller(ArrayList<HexCube> cubeCoordinates) {
        board = new Board(cubeCoordinates);
    }

    /**
     * @return The Board object representing the current state of the game.
     */
    public Board getBoard() {
        return board;
    }

    /**
     * @return The current State of the game.
     */
    public State getState() {
        return state;
    }

    /**
     * Handles a player move by validating it, updating the board, and changing the game state.
     * It checks for valid CP and NCP and updates the game accordingly.
     *
     * @param move The hex the player wants to take.
     * @return true if the move was valid, false if the move was invalid.
     */
    public boolean handleMove(HexCube move) {
        if (board.isHexagonOccupied(move)) {
            return false;
        }

        boolean isBlue = (state == State.BLUE_TURN);
        ArrayList<HexCube> neighbors = board.getNeighbors(move);
        boolean moveMade = false;

        //Check for CP
        moveMade = board.validateCapturingMove(move, isBlue);
        if (moveMade) {
            handleCapturingMove(isBlue);
        } else {
            moveMade = handleNonCapturingMove(move, isBlue, neighbors);
        }

        if (moveMade) {
            board.updateBoard();
        }
        return moveMade;
    }

    /**
     * Handles CP, checks for win and changes turn after.
     *
     * @param isBlue Indicates if the current player is Blue.
     */
    private void handleCapturingMove(boolean isBlue) {
        if (board.checkWin()) {
            state = isBlue ? State.BLUE_WON : State.RED_WON;
        }
    }

    /**
     * Handles a NCP, validates the move and changes turns after.
     *
     * @param move The Hex the player wants to occupy.
     * @param isBlue Indicates if the current player is Blue.
     * @param neighbors A list of neighboring hexes.
     * @return true if the move is valid, false otherwise.
     */
    private boolean handleNonCapturingMove(HexCube move, boolean isBlue, ArrayList<HexCube> neighbors) {
        boolean moveMade = false;
        if (isBlue) {
            moveMade = board.validateForBlue(move, neighbors);
        } else {
            moveMade = board.validateForRed(move, neighbors);
        }
        if (moveMade) {
            state = isBlue ? State.RED_TURN : State.BLUE_TURN;
        }
        return moveMade;
    }
}
