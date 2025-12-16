package sctbench.cs.origin;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.jupiter.api.*;

import java.io.File;

public class Twostage10BadTest extends TestJPF {

    public static String classPath;


    @BeforeAll
    public static void init() {
        classPath = getClassPath();
    }


    @BeforeEach
    public void setup() {
        TwostageBad.setBoolean(false);
    }

    @RepeatedTest(100000)
    public void test() {
        Assertions.assertFalse(Twostage10Bad.runOnce());
    }

    @Test
    public void twoStage10BadWithJpf() throws InterruptedException {
        if (verifyNoPropertyViolation(
                "+classpath=" + classPath,
                "+vm.args=-ea"

        )) {  // specifies the test goal, "jpfOptions" are optional
            Twostage100Bad.runOnce();


        }
    }

    @Test
    public void twoStage10BadWithWeightedRandomWalk() throws InterruptedException {
        if (verifyNoPropertyViolation(
                "+classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/SctBench/app/build/classes/java/test:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/SctBench/app/build/classes/java/main",
                "+classpath=" + classPath,
                "+vm.args=-ea",
                "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.probabilities = 0.99999 0.00001",
                "+search_with_reset.eps = 0.1",
                "+numberOfThreads = 10",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"

        )) {  // specifies the test goal, "jpfOptions" are optional
            Twostage10Bad.runOnce();


        }
    }


    public static String getClassPath() {
        String userDir = System.getProperty("user.dir");   //
//        System.out.println(userDir);
        String fs = File.separator;                   // For / or \ for win/osx, neet help which works, as \ escapes space in absolutep aths on osx. This is where our problem is if we have problem on windows.
        String ps = File.pathSeparator;

        String testClasses = userDir + fs + "build" + fs + "classes" + fs + "java" + fs + "test";
        String mainClasses = userDir + fs + "build" + fs + "classes" + fs + "java" + fs + "main";

        return testClasses + ps + mainClasses;
    }

}
