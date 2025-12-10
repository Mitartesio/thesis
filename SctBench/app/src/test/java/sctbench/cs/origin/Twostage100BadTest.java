package sctbench.cs.origin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

public class Twostage100BadTest {

    @BeforeEach
    public void setup() {
        TwostageBad.setBoolean(false);

    }

    @RepeatedTest(100000)
    public void test() {
        Assertions.assertFalse(Twostage100Bad.runOnce());
    }

}
