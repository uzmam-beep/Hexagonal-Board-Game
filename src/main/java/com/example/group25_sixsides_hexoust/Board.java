package com.example.group25_sixsides_hexoust;
import java.util.*;

interface BoardUpdateListener {
    void onBoardUpdated();
}

/**
 * The Board class is responsible for managing the game board state, validating moves,
 * and checking if a hexagon is occupied.
 * It contains methods to validate player moves, capture groups, and check game end.
 */
public class Board {

    private Hex hex = new Hex(200);
    private ArrayList<HexCube> cubeCoordinates;
    private Map<HexCube, Integer> cubeToIndexMap = new HashMap<>(); //Map for quick lookup of hex indexes
    private BoardUpdateListener updateListener;  //Listener for board updates

    /**
     * Constructs a Board instance initializes game.
     * @param cubeCoordinates A list of HexCube objects representing the hexagonal grid.
     */
    public Board(ArrayList<HexCube> cubeCoordinates) {
        this.cubeCoordinates = cubeCoordinates;
        for (int i = 0; i < cubeCoordinates.size(); i++) {
            cubeToIndexMap.put(cubeCoordinates.get(i), i);  // Mapping HexCube to index
        }
    }

    /**
     * @param listener The listener to be notified on board updates.
     */
    public void setBoardUpdateListener(BoardUpdateListener listener) {
        this.updateListener = listener;
    }

    /**
     * @return The Hex object representing the current state of the hexagons.
     */
    public Hex getHex() {
        return hex;
    }

    /**
     * Validates if a blue player can make a valid move on this hex.
     *
     * @param move The hex the player wants to occupy.
     * @param neighbors A list of neighboring hexes.
     * @return true if the move is valid for the blue player, false otherwise.
     */
    public boolean validateForBlue(HexCube move, ArrayList<HexCube> neighbors) {
        return validateForPlayer(move, neighbors, true);
    }

    /**
     * Validates if a red player can make a valid move on this hex.
     *
     * @param move The hex the player wants to occupy.
     * @param neighbors A list of neighboring hexes.
     * @return true if the move is valid for the red player, false otherwise.
     */
    public boolean validateForRed(HexCube move, ArrayList<HexCube> neighbors) {
        return validateForPlayer(move, neighbors, false);
    }

    /**
     * Validates if a player can make a valid move on this hex.
     * This is used for blue and red players
     *
     * @param move The hex the player wants to occupy.
     * @param neighbors A list of neighboring hexes.
     * @param isBlue Indicates if the current player is Blue.
     * @return true if the move is valid for the player, false otherwise.
     */
    public boolean validateForPlayer(HexCube move, ArrayList<HexCube> neighbors, boolean isBlue) {
        Integer moveIndex = cubeToIndexMap.get(move);
        if (moveIndex == null || hex.freeHexagons[moveIndex] != 0) return false;  //Check if the hex is free

        for (HexCube neighbor : neighbors) {
            Integer neighborIndex = cubeToIndexMap.get(neighbor);
            if (neighborIndex != null && ((isBlue && hex.BlueHexagons[neighborIndex] == 1) ||
                    (!isBlue && hex.RedHexagons[neighborIndex] == 1))) {
                return false;
            }
        }

        //Mark the move as occupied by the current player
        hex.freeHexagons[moveIndex] = 1;
        if (isBlue) {
            hex.BlueHexagons[moveIndex] = 1;
        } else {
            hex.RedHexagons[moveIndex] = 1;
        }

        return true;
    }

    /**
     * Validates a capturing move for the current player.
     *
     * @param move The hex the players want to occupy.
     * @param isBlue Indicates if the current player is Blue.
     * @return true if the move is a valid capture, false otherwise.
     */
    public boolean validateCapturingMove(HexCube move, boolean isBlue) {
        Integer moveIndex = cubeToIndexMap.get(move);
        if (moveIndex == null || hex.freeHexagons[moveIndex] != 0) {
            return false;
        }

        //Place the player's stone on the hex
        hex.freeHexagons[moveIndex] = 1;
        if (isBlue) {
            hex.BlueHexagons[moveIndex] = 1;
        } else {
            hex.RedHexagons[moveIndex] = 1;
        }

        ArrayList<Integer> playerGroup = collectGroup(move, isBlue);  //Collect the player's group
        Set<Integer> visited = new HashSet<>();
        ArrayList<ArrayList<Integer>> opponentGroups = new ArrayList<>();

        //Loop to capture opponent groups
        for (int playerHexIndex : playerGroup) {
            HexCube cube = cubeCoordinates.get(playerHexIndex);
            for (HexCube neighbor : getNeighbors(cube)) {
                Integer neighborIndex = cubeToIndexMap.get(neighbor);

                if (neighborIndex != null) {
                    boolean isOpponentStone = (isBlue && hex.RedHexagons[neighborIndex] == 1) ||
                            (!isBlue && hex.BlueHexagons[neighborIndex] == 1);

                    if (isOpponentStone) {
                        ArrayList<Integer> opponentGroup = collectGroup(neighbor, !isBlue);
                        visited.addAll(opponentGroup);
                        opponentGroups.add(opponentGroup);
                    }
                }
            }
        }

        //If no opponent groups were found, the move is not valid
        if (opponentGroups.isEmpty()) {
            resetHex(moveIndex);
            return false;
        }

        int mySize = playerGroup.size();
        //Ensure opponent's group is smaller than the player's group to proceed with the capture
        for (ArrayList<Integer> opponentGroup : opponentGroups) {
            if (opponentGroup.size() >= mySize) {
                resetHex(moveIndex);
                return false;
            }
        }

        //Capture the opponent's groups
        for (ArrayList<Integer> opponentGroup : opponentGroups) {
            captureGroup(opponentGroup, isBlue);
        }

        return true;
    }

    /**
     * @param moveIndex The index of the hexagon to reset.
     */
    private void resetHex(int moveIndex) {
        hex.freeHexagons[moveIndex] = 0;
        hex.BlueHexagons[moveIndex] = 0;
        hex.RedHexagons[moveIndex] = 0;
    }

    /**
     * Collects a group of connected hexes starting from the given hex and color.
     *
     * @param start The starting hex.
     * @param isBlue Indicates if the current player is Blue.
     * @return The group of connected hexes.
     */
    public ArrayList<Integer> collectGroup(HexCube start, boolean isBlue) {
        Integer startIndex = cubeToIndexMap.get(start);
        if (startIndex == null) return new ArrayList<>();

        boolean[] visited = new boolean[hex.freeHexagons.length];
        ArrayList<Integer> group = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.add(startIndex);
        visited[startIndex] = true;

        //Add hexes to be captured to the group
        while (!queue.isEmpty()) {
            int current = queue.poll();
            group.add(current);

            HexCube currentCube = cubeCoordinates.get(current);
            for (HexCube neighbor : getNeighbors(currentCube)) {
                Integer neighborIndex = cubeToIndexMap.get(neighbor);
                if (neighborIndex != null && !visited[neighborIndex] && hex.freeHexagons[neighborIndex] == 1) {
                    if ((isBlue && hex.BlueHexagons[neighborIndex] == 1) ||
                            (!isBlue && hex.RedHexagons[neighborIndex] == 1)) {
                        queue.add(neighborIndex);
                        visited[neighborIndex] = true;
                    }
                }
            }
        }
        return group;
    }

    /**
     * Captures a group of opponent hexes and marks them as free.
     *
     * @param group The group of opponent hexes to capture.
     * @param toBlue Indicates the color to capture the hexes as (blue or red).
     */
    public void captureGroup(ArrayList<Integer> group, boolean toBlue) {
        for (int index : group) {
            hex.BlueHexagons[index] = 0;
            hex.RedHexagons[index] = 0;
            hex.freeHexagons[index] = 0;
        }
    }

    /**
     * @param move The HexCube to check.
     * @return true if the hexagon is occupied, false otherwise.
     */
    public boolean isHexagonOccupied(HexCube move) {
        Integer moveIndex = cubeToIndexMap.get(move);
        return moveIndex != null && hex.freeHexagons[moveIndex] == 1;
    }

    /**
     * @param move The hex whose neighbour we want.
     * @return A list of neighboring hexes.
     */
    public ArrayList<HexCube> getNeighbors(HexCube move) {
        ArrayList<HexCube> neighbors = new ArrayList<>();

        for (int direction = 0; direction < 6; direction++) {
            HexCube neighbor = move.neighbor(direction);
            if (cubeToIndexMap.containsKey(neighbor)) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    /**
     * Updates the board by notifying the registered update listener.
     */
    public void updateBoard() {
        if (updateListener != null) {
            updateListener.onBoardUpdated();
        }
    }

    /**
     * @return A Map containing hexes mapped to their index values.
     */
    public Map<HexCube, Integer> getCubeToIndexMap() {
        return cubeToIndexMap;
    }


    /**
     * Checks if either player has won the game.
     * The game ends when a player has no hexes left on the board.
     *
     * @return true if either blue or red has no hexes left, false otherwise.
     */
    public boolean checkWin() {
        boolean blueHasHexes = false;
        for (int i = 0; i < hex.BlueHexagons.length; i++) {
            if (hex.BlueHexagons[i] == 1) {
                blueHasHexes = true;
                break;
            }
        }

        boolean redHasHexes = false;
        for (int i = 0; i < hex.RedHexagons.length; i++) {
            if (hex.RedHexagons[i] == 1) {
                redHasHexes = true;
                break;
            }
        }
        return !blueHasHexes || !redHasHexes;  //If either has no hexes, the game ends
    }


    /**
     * The Hex class contains the state of hexagons on the board.
     * It has the arrays for free, blue, and red hexagons.
     * It also contains the logic for initializing and updating these states.
     */
    public class Hex {

        private int[] freeHexagons;
        private int[] BlueHexagons;
        private int[] RedHexagons;

        /**
         * Constructs a Hex instance for the given grid size.
         * Initializes arrays for tracking the state of each hexagon (free, blue, or red).
         *
         * @param gridSize The size of the hex grid.
         */
        public Hex(int gridSize) {
            int totalHexagons = gridSize * gridSize;
            freeHexagons = new int[totalHexagons];
            BlueHexagons = new int[totalHexagons];
            RedHexagons = new int[totalHexagons];

            //Initialize all hexagons as free
            for (int i = 0; i < totalHexagons; i++) {
                freeHexagons[i] = 0;
            }
        }

        /**
         * @return The array of blue hexagons.
         */
        public int[] getBlueHexagons() {
            return BlueHexagons;
        }

        /**
         * @return The array of red hexagons.
         */
        public int[] getRedHexagons() {
            return RedHexagons;
        }
    }
}