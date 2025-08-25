package com.example.group25_sixsides_hexoust;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;

public class HexOustTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        new HexOustUI().start(stage);
    }

    // integration test to test multiple methods in HexOust and Controller
    @Test
    void clickCenterRendersBlueAndSwitchesTurn() {
        clickOn("#q=0,r=0,s=0");
        Polygon hex = lookup("#q=0,r=0,s=0").queryAs(Polygon.class);
        Assertions.assertEquals(Color.RED, hex.getFill());
        FxAssert.verifyThat(".label", LabeledMatchers.hasText("Player Blue's Turn"));
        sleep(1000);
    }
}

