package sctbench.cs.origin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

public class Wronglock3BadTest {


    @BeforeEach
    public void setup() {
        Wronglock3Bad.setiNum1(1);
        Wronglock3Bad.setiNum2(3);
        Wronglock3Bad.setBoolean(false);
    }

    @RepeatedTest(100000)
    public void testWronglock3Bad() {

        Assertions.assertFalse(Wronglock3Bad.runOnce());

    }
}
