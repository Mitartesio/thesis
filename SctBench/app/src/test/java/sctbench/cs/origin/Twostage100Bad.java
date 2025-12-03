package sctbench.cs.origin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

public class Twostage100Bad {

    @BeforeEach
    public void setup() {
        TwostageBad.setBoolean(false);

    }

    @RepeatedTest(100000)
    public void twostageBad100Test() {
        Assertions.assertFalse(TwostageBad.runOnce());
    }

}
