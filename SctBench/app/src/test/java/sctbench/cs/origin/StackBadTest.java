package sctbench.cs.origin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class StackBadTest {

    @RepeatedTest(100000)
    public void test() {
        Assertions.assertFalse(StackBad.runOnce());
    }

}
