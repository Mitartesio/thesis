package sut;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.jupiter.api.Test;


public class MinimizationTesting extends TestJPF {
    MinimizationTest test;


    @BeforeEach
    public void setup() {

        test = new MinimizationTest();

    }

    @RepeatedTest(100000)
    public void runTest() throws InterruptedException {
        Assertions.assertTrue(test.run());
    }


    @Test
    public void testMinimizationWithJpf() throws InterruptedException {
        if (verifyNoPropertyViolation(
                "+classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/CupTest/app/build/classes/java/test;/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/CupTest/app/build/classes/java/main",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
//                "+search_with_reset.k = 500",
                "+search_with_reset.probabilities = 0.999 0.001",
                "+search_with_reset.eps = 0.1",
                "+numberOfThreads = 2",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"

        )) {  // specifies the test goal, "jpfOptions" are optional
            test = new MinimizationTest();
            test.run();
//            gov.nasa.jpf.JPF.main(new String[]{"../../configs/MinimizationTest.jpf"});
        }
    }
}
