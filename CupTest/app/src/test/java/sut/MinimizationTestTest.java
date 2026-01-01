package sut;

import org.junit.Ignore;
import org.junit.jupiter.api.*;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;


public class MinimizationTestTest extends TestJPF {
    MinimizationTest test;
    MinimizationTestWithNoise testWithNoise;
    public static String classPath;

    boolean bug;


    @BeforeAll
    public static void init() {
        classPath = getClassPath();
    }


    @BeforeEach
    public void setup() {
        test = new MinimizationTest();
        testWithNoise = new MinimizationTestWithNoise();
        bug = false;
    }

    @RepeatedTest(200000)
    public void runTest() throws InterruptedException {
        bug = test.run();
        Assertions.assertFalse(bug);
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
