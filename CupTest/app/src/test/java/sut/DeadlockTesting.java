package sut;

import static gov.nasa.jpf.util.test.TestJPF.*;

import org.junit.Ignore;
import org.junit.jupiter.api.*;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;

public class DeadlockTesting extends TestJPF {
    DeadlockExample test;

    public static String classPath;
    public static DeadlockExample d;
    boolean bug;


    @BeforeAll
    public static void init() {
        d = new DeadlockExample();
    }

    @BeforeEach
    public void beforeEach() {
        bug = false;

    }

    @RepeatedTest(10)
    public void runTest() throws InterruptedException {
        bug = d.runForDeadlock();
        System.out.println("RESULT:" + bug);
        Assertions.assertFalse(bug);
    }


//    @Test
//    public void testDeadlockExampleJpf() throws InterruptedException {
//        if (verifyNoPropertyViolation(
//                //target = sut.DeadlockExample

    /// /                "+classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/CupTest/app/build/classes/java/test;/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/CupTest/app/build/classes/java/main",
//                "+classpath =" + classPath,
//                //# native_classpath = out
//
//                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
//                "+search.class = gov.nasa.jpf.search.Reset_Search",
//
//                //"+search_with_reset.k = 500",
//                "+search_with_reset.probabilities = 0.999 0.001",
//                "+search_with_reset.eps = 0.1",
//                "+numberOfThreads = 2",
//                "+search.multiple_errors = false",
//                "+jpf.report.console.property_violation = error",
//                "+report.console.finished = result,statistics,error",
//                "+report.unique_errors = true"
//        )) {
//            test = new DeadlockExample();
//            test.runDemo();
//        }
//    }
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
