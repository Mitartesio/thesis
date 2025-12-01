package sctbench.cs.origin;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Copy;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ArithmetricProgBadTest extends TestJPF{
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
        // Reset all static fields
        setStaticInt("num", 0);
        setStaticLong("total", 0L);
        setStaticBoolean("flag", false);
    }

    // for a small example like this with 2 threads and n = 3.
    // the locking and small N constrain the possible interleavings, giving the
    // appearance of deterministic behavior.
    // It is a property of how the threads and locks interact: the locking logic and
    // the small N make the execution almost completely predictable.
    @RepeatedTest(10) // if assertion is before join then always pass, if assertion after join then always fail
    public void runTest() throws Exception {
        Thread t1 = threadFor("thread1");
        Thread t2 = threadFor("thread2");

        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
        // JUnit assertion to expose the concurrency bug
        // Check final state
        long total = getStaticLong("total");
        boolean flag = getStaticBoolean("flag");
        long expected = (N * (N + 1)) / 2; // 6 for N=3
        if (flag) {
            Assertions.assertNotEquals(expected, total, "Expected concurrency bug: total should not equal " + expected);
        }

        System.out.println("Final state: total=" + total + ", flag=" + flag);

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

//Reflection methods created to handle the private fields and methods of the tested class
    private void setStaticInt(String name, int value) throws Exception {
        Field f = ArithmeticProgBad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setInt(null, value);
    }

    private void setStaticLong(String name, long value) throws Exception {
        Field f = ArithmeticProgBad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setLong(null, value);
    }

    private void setStaticBoolean(String name, boolean value) throws Exception {
        Field f = ArithmeticProgBad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setBoolean(null, value);
    }

    private int getStaticInt(String name) throws Exception {
        Field f = ArithmeticProgBad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(null);
    }

    private long getStaticLong(String name) throws Exception {
        Field f = ArithmeticProgBad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getLong(null);
    }

    private boolean getStaticBoolean(String name) throws Exception {
        Field f = ArithmeticProgBad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getBoolean(null);
    }

    private Thread threadFor(String methodName) {
        return new Thread(() -> {
            try {
                Method m = ArithmeticProgBad.class.getDeclaredMethod(methodName);
                m.setAccessible(true);
                m.invoke(null); // null because static
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
