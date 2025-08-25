package com.example.group25_sixsides_hexoust;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class ControllerTest {
    private Controller ctrl;
    private ArrayList<HexCube> coords;

    @BeforeEach
    void setUp() {
        coords = new ArrayList<>();
        coords.add(new HexCube(0,0,0));
        coords.add(new HexCube(1,-1,0));
        coords.add(new HexCube(1,0,-1));
        coords.add(new HexCube(0,1,-1));
        ctrl = new Controller(coords);
    }

    @Test
    void testInitialState() {
        assertEquals(Controller.State.RED_TURN, ctrl.getState());
    }

    @Test
    void testNonCapturingTurnSwitch() {
        assertTrue(ctrl.handleMove(coords.get(0)));
        assertEquals(Controller.State.BLUE_TURN, ctrl.getState());
    }

    @Test
    void testInvalidOccupiedDoesNotSwitch() {
        assertTrue(ctrl.handleMove(coords.get(0)));
        assertFalse(ctrl.handleMove(coords.get(0)));
        assertEquals(Controller.State.BLUE_TURN, ctrl.getState());
    }

    @Test
    void testCaptureLeadsToWin() {
        ctrl.handleMove(coords.get(0));
        ctrl.handleMove(coords.get(1));
        ctrl.handleMove(coords.get(2));
        assertFalse(ctrl.handleMove(coords.get(0)));
        assertFalse(ctrl.handleMove(coords.get(3)));
        assertEquals(Controller.State.RED_WON, ctrl.getState());
    }
}
