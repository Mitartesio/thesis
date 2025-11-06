package sut;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

public class MinimizationTesting {
    MinimizationTest test;


    @BeforeEach
    public void setup() {
        test = new MinimizationTest();

    }

    @RepeatedTest(10000)
    public void runTest() throws InterruptedException {
        test.run();
    }
}
