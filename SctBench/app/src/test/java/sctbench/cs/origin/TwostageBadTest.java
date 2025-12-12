package sctbench.cs.origin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

public class TwostageBadTest {

    @BeforeEach
    public void setup() {
        TwostageBad.setBoolean(false);
    }

    @RepeatedTest(100000)
    public void twostageBadTest() {
        Assertions.assertFalse(TwostageBad.runOnce());
    }


}
