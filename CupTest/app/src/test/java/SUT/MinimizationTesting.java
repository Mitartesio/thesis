package SUT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

public class MinimizationTesting {
    MinimizationTest test;


    @BeforeEach
    public void setup() {
        test = new MinimizationTest();

    }

    @RepeatedTest(100)
    public void runTest() throws InterruptedException {
        test.run();
    }
}
