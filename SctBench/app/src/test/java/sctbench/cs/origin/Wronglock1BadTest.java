package sctbench.cs.origin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

public class Wronglock1BadTest {

    @BeforeEach
    public void setup() {
        Wronglock1Bad.setiNum1(1);
        Wronglock1Bad.setiNum2(3);
        Wronglock1Bad.setBoolean(false);
    }

    @RepeatedTest(100000)
    public void testWronglock1Bad() {

        Assertions.assertFalse(Wronglock1Bad.runOnce());

    }
}
