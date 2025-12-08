package sctbench.cs.origin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WronglockBadTest {


    @RepeatedTest(100000)
    public void testWronglockBad() {

        Assertions.assertFalse(WronglockBad.runOnce());

    }
}
