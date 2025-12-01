package sctbench.cs.origin;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Copy;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
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

        setStaticInt("A", 0);
        setStaticInt("B", 0);
        setStaticBoolean("mLockedBy1", false);
        setStaticBoolean("mLockedBy2", false);
        setStaticBoolean("lLockedBy1", false);
        setStaticBoolean("lLockedBy2", false);

        setStaticField("m", new java.util.concurrent.locks.ReentrantLock());
        setStaticField("l", new java.util.concurrent.locks.ReentrantLock());

    }

    @RepeatedTest(10)
    public void runtTest() throws Exception{
        Thread t1 = threadForStatic("t1");
        Thread t2 = threadForStatic("t2");
        Thread t3 = threadForStatic("t3");
        Thread t4 = threadForStatic("t4");

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        
        int A = getStaticInt("A");
        int B = getStaticInt("B");
        
        t1.join();
        t2.join();
        t3.join();
        t4.join();


        Assertions.assertEquals(0, A, "Expected A to be reset to 0");
        Assertions.assertEquals(0, B, "Expected B to be reset to 0");
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

    // Reflection methods created to handle the private fields and methods of the
    // tested class
    private Thread threadForStatic(String methodName) {
        return new Thread(() -> {
            try {
                Method m = Carter01Bad.class.getDeclaredMethod(methodName);
                m.setAccessible(true);
                m.invoke(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setStaticInt(String name, int value) throws Exception {
        Field f = Carter01Bad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setInt(null, value);
    }

    private void setStaticBoolean(String name, boolean value) throws Exception {
        Field f = Carter01Bad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setBoolean(null, value);
    }

    private void setStaticField(String name, Object value) throws Exception {
        Field f = Carter01Bad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }

    private int getStaticInt(String name) throws Exception {
        Field f = Carter01Bad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(null);
    }

    private boolean getStaticBoolean(String name) throws Exception {
        Field f = Carter01Bad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getBoolean(null);
    }
}
