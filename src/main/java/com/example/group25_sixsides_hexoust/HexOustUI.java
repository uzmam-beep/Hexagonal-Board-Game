package com.example.group25_sixsides_hexoust;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.scene.shape.Circle;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// main class to run the game. Sets up the board and handles user interaction.
public class HexOustUI extends Application {

    private Controller controller;
    private Layout layout;
    private Pane pane;
    private Circle dot;
    private Label playerLabel;
    private Label invalidMessage;
    private ArrayList<HexCube> cubeCoordinates = new ArrayList<>();
    private Map<Polygon, HexCube> polygonToCubeMap = new HashMap<>();


    @Override
    public void start(Stage primaryStage) {
        // choosing the size of each hex and the location for the central hex
        double size = 30.0;
        double originX = 400.0;
        double originY = 400.0;

        layout = new Layout(Layout.flat, new Point(size, size), new Point(originX, originY));

        int baseN = 6;
        ArrayList<ArrayList<Point>> grid = generateHexGrid(layout, baseN);

        controller = new Controller(cubeCoordinates);
        controller.getBoard().setBoardUpdateListener(this::repaintBoard);

        pane = new Pane();
        drawHexGrid(grid);
        createAndAddLabel(pane);
        createAndAddDot(Color.RED, pane);

        Scene scene = new Scene(pane, 800, 800);
        primaryStage.setTitle("HexGridFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // creating the dot
    private void createAndAddDot(Color dotColor, Pane pane) {
        dot = new Circle(15, dotColor);
        dot.setCenterX(100);
        dot.setCenterY(675);
        dot.setMouseTransparent(true);
        pane.getChildren().addAll(dot);

        // moving the dot as the mouse is moved
        pane.setOnMouseMoved(event -> {
            dot.setCenterX(event.getX());
            dot.setCenterY(event.getY());
        });
    }

    // creates the label that says which player's turn it is
    private void createAndAddLabel(Pane pane) {
        playerLabel = new Label();
        playerLabel.setFont(new Font("Times new roman", 20));
        playerLabel.setLayoutX(100);
        playerLabel.setLayoutY(70);
        pane.getChildren().add(playerLabel);

        // changing the label when a player's turn is over
        updatePlayerLabel();
    }

    // changes the playerLabel according to whose turn it is
    private void updatePlayerLabel() {
        if (controller.getState() == Controller.State.BLUE_TURN) {
            playerLabel.setText("Player Blue's Turn");
        } else if (controller.getState() == Controller.State.RED_TURN) {
            playerLabel.setText("Player Red's Turn");
        }
    }

    // creates a label when an invalid move is mode
    public void InvalidMoveMessage(Pane pane) {
        if (invalidMessage != null) {
            return;
        }
        Color dark_red = Color.rgb(204, 0, 0);
        invalidMessage = new Label();
        invalidMessage.setText("INVALID MOVE!\nMake another move");
        invalidMessage.setTextFill(dark_red);
        invalidMessage.setFont(new Font("Times new roman", 18));
        invalidMessage.setLayoutX(600);
        invalidMessage.setLayoutY(725);
        pane.getChildren().add(invalidMessage);
    }

    // removes everything from the screen and add a label for when a player has won the game
    private void showWinScreen(String winner) {
        pane.getChildren().clear();
        Label winLabel = new Label(winner + " wins!");
        winLabel.setFont(new Font("Times new roman", 72));
        winLabel.setEffect(new DropShadow(8, Color.GRAY));
        winLabel.setLayoutX(250);
        winLabel.setLayoutY(350);
        pane.getChildren().add(winLabel);
    }

    private ArrayList<ArrayList<Point>> generateHexGrid(Layout layout, int baseN) {
        ArrayList<ArrayList<Point>> grid = new ArrayList<>();
        for (int q = -baseN; q <= baseN; q++) {
            for (int r = Math.max(-baseN, -q - baseN); r <= Math.min(baseN, -q + baseN); r++) {
                int s = -q - r;
                HexCube h = new HexCube(q, r, s);
                cubeCoordinates.add(h);

                ArrayList<Point> corners = layout.polygonCorners(h);
                grid.add(corners);
            }
        }
        return grid;
    }

    // drawing each hexagon
    private void drawHexGrid(ArrayList<ArrayList<Point>> grid) {
        // creating a polygon for each list of six points (corner coordinates)
        for (int i = 0; i < grid.size(); i++) {
            ArrayList<Point> hexagon = grid.get(i);
            Polygon polygon = new Polygon();
            for (Point p : hexagon) {
                polygon.getPoints().addAll(p.x, p.y);
            }
            polygon.setStroke(Color.BLACK);
            polygon.setFill(Color.TRANSPARENT);

            HexCube h = cubeCoordinates.get(i);
            polygon.setId("q=" + h.q + ",r=" + h.r + ",s=" + h.s);

            polygonToCubeMap.put(polygon, h);
            polygon.setOnMouseClicked(this::handleMouseClick);

            pane.getChildren().add(polygon);
        }
    }

    // handling when the mouse is clicked (a move is made)
    private void handleMouseClick(MouseEvent event) {
        Polygon hexagon = (Polygon) event.getSource();
        HexCube move = polygonToCubeMap.get(hexagon);

        if (move == null) {
            System.out.println("Error: Hexagon not found in map.");
            return;
        }

        Controller.State previousState = controller.getState();
        Color originalColor = (controller.getState() == Controller.State.BLUE_TURN) ? Color.BLUE : Color.RED;

        boolean moveMade = controller.handleMove(move);

        if (!moveMade) {
            InvalidMoveMessage(pane);
        } else if(invalidMessage != null){
            // removing the invalid move message when a valid move has been made
            pane.getChildren().remove(invalidMessage);
            invalidMessage = null;
        }

        if (controller.getState() != previousState) {
            updateHexagonAndDotState(hexagon, originalColor);
            updatePlayerLabel();
        }

        checkForWinner();

    }

    private void updateHexagonAndDotState(Polygon hexagon, Color originalColor) {
        hexagon.setFill(originalColor);
        Color playerColor = (controller.getState() == Controller.State.BLUE_TURN) ? Color.BLUE : Color.RED;
        dot.setFill(playerColor);
    }

    // changing the winning message according to who has won
    private void checkForWinner() {
        if (controller.getState() == Controller.State.BLUE_WON) {
            showWinScreen("Blue");
        } else if (controller.getState() == Controller.State.RED_WON) {
            showWinScreen("Red");
        }
    }

    // repaints all hexagons
    private void repaintBoard() {
        for (Map.Entry<Polygon, HexCube> entry : polygonToCubeMap.entrySet()) {
            Polygon polygon = entry.getKey();
            HexCube cube = entry.getValue();

            Integer cubeIndex = controller.getBoard().getCubeToIndexMap().get(cube);
            if (cubeIndex == null) continue;

            if (controller.getBoard().getHex().getBlueHexagons()[cubeIndex] == 1) {
                polygon.setFill(Color.BLUE);
            } else if (controller.getBoard().getHex().getRedHexagons()[cubeIndex] == 1) {
                polygon.setFill(Color.RED);
            } else {
                polygon.setFill(Color.TRANSPARENT);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class Point
{
    public Point(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    public final double x;
    public final double y;
}

// Using the cube coordinate system to make a hex
class HexCube
{
    public HexCube(int q, int r, int s)
    {
        this.q = q;
        this.r = r;
        this.s = s;
        if (q + r + s != 0)
            throw new IllegalArgumentException("q + r + s must be 0");
    }

    public final int q;
    public final int r;
    public final int s;

    public HexCube add(HexCube b)
    {
        return new HexCube(q + b.q, r + b.r, s + b.s);
    }

    static public ArrayList<HexCube> directions = new ArrayList<HexCube>(){{add(new HexCube(1, 0, -1)); add(new HexCube(1, -1, 0)); add(new HexCube(0, -1, 1)); add(new HexCube(-1, 0, 1)); add(new HexCube(-1, 1, 0)); add(new HexCube(0, 1, -1));}};

    static public HexCube direction(int direction)
    {
        return HexCube.directions.get(direction);
    }

    public HexCube neighbor(int direction)
    {
        return add(HexCube.direction(direction));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HexCube hexCube = (HexCube) obj;
        return q == hexCube.q && r == hexCube.r && s == hexCube.s;
    }

    @Override
    public int hashCode() {
        return q * 31 * 31 + r * 31 + s;
    }
    @Override
    public String toString() {
        return "(" + q + ", " + r + ", " + s + ")";
    }

}

// matrices for converting cube to pixel and pixel to cube
class Orientation
{
    public Orientation(double f0, double f1, double f2, double f3,
                       double b0, double b1, double b2, double b3,
                       double start_angle)
    {
        this.f0 = f0;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.b0 = b0;
        this.b1 = b1;
        this.b2 = b2;
        this.b3 = b3;
        this.start_angle = start_angle;
    }
    public final double f0;
    public final double f1;
    public final double f2;
    public final double f3;
    public final double b0;
    public final double b1;
    public final double b2;
    public final double b3;
    public final double start_angle;
}

class Layout
{
    public Layout(Orientation orientation, Point size, Point origin)
    {
        this.orientation = orientation;
        this.size = size;
        this.origin = origin;
    }

    public final Orientation orientation;
    public final Point size;
    public final Point origin;

    // making the hexagons flat-topped
    static public Orientation flat = new Orientation(3.0 / 2.0, 0.0, Math.sqrt(3.0) / 2.0, Math.sqrt(3.0), 2.0 / 3.0,
            0.0, -1.0 / 3.0, Math.sqrt(3.0) / 3.0, 0.0);

    public Point hexToPixel(HexCube h)
    {
        Orientation M = orientation;
        double x = (M.f0 * h.q + M.f1 * h.r) * size.x;
        double y = (M.f2 * h.q + M.f3 * h.r) * size.y;
        return new Point(x + origin.x, y + origin.y);
    }

    public Point hexCornerOffset(int corner)
    {
        Orientation M = orientation;
        double angle = 2.0 * Math.PI * (M.start_angle - corner) / 6.0;
        return new Point(size.x * Math.cos(angle), size.y * Math.sin(angle));
    }

    // gives the six corner points of a hexagon
    public ArrayList<Point> polygonCorners(HexCube h)
    {
        ArrayList<Point> corners = new ArrayList<>();
        Point center = hexToPixel(h);
        for (int i = 0; i < 6; i++)
        {
            Point offset = hexCornerOffset(i);
            corners.add(new Point(center.x + offset.x, center.y + offset.y));
        }
        return corners;
    }
}