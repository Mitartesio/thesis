package sctbench.cs.origin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

public class Sync02BadTest {


    @RepeatedTest(10000)
    public void test() {

        Assertions.assertFalse(Sync02Bad.runOnce());


    }
}
