package sctbench.cs.origin;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Copy;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ArithmeticProgBadTest extends TestJPF{
    ArithmeticProgBad test;
    public static String classPath;
    public static final int N = 3;

    @BeforeAll
    public static void init() {
        classPath = getClassPath();
    }

    @BeforeEach
    public void setup() throws Exception {
        test = new ArithmeticProgBad();
        //ArithmeticProgBad.setBoolean(false);
        
    }

    @RepeatedTest(10000)
    public void booleantest() {
        // bug is found if test fails
        Assertions.assertFalse(ArithmeticProgBad.run());
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
