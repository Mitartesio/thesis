package sctbench.cs.origin;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Copy;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/*
JUnit + normal JVM execution is fast and usually executes these threads in a way that avoids the deadlock 
the scheduler just happens to run one thread fully before the other grabs the second lock. 
That is why the assertions pass: the “bad state” that triggers the exception is never reached.

The assertion in the original class is not actually an assert in Carter01Bad; 
the “violation” of the program is the RuntimeException thrown when the deadlock is detected. 
If threads never interleave in the problematic way, the exception never occurs.
 */

public class Carter01BadTest {
    Carter01Bad test;
    public static String classPath;

    @BeforeAll
    public static void init() {
        classPath = getClassPath();
    }

    @BeforeEach
    public void setup() throws Exception {
        test = new Carter01Bad();
    }

    @RepeatedTest(1000)
    public void testboolean() {
        // failing the assertion means it finds the bug. Expecting false, but gets true.
        Assertions.assertFalse(Carter01Bad.run());
    }


    public static String getClassPath() {
        String userDir = System.getProperty("user.dir"); //
        // System.out.println(userDir);
        String fs = File.separator; // For / or \ for win/osx, neet help which works, as \ escapes space in
                                    // absolutep aths on osx. This is where our problem is if we have problem on
                                    // windows.
        String ps = File.pathSeparator;

        String testClasses = userDir + fs + "build" + fs + "classes" + fs + "java" + fs + "test";
        String mainClasses = userDir + fs + "build" + fs + "classes" + fs + "java" + fs + "main";

        return testClasses + ps + mainClasses;
    }
}
