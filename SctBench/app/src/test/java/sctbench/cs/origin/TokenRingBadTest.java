package sctbench.cs.origin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

public class TokenRingBadTest {

    @RepeatedTest(10000)
    public void TokenRingBadTestRun() {
        Assertions.assertFalse(TokenRingBad.runOnce());
    }
}
