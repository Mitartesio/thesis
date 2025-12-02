package sctbench.cs.origin;
import org.junit.jupiter.api.*;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

public class Deadlock01BadTest {
    Deadlock01Bad test;
    public static String classPath;

    @BeforeAll
    public static void init() {
        classPath = getClassPath();
    }

    @BeforeEach
    public void setup() throws Exception {
        setStaticInt("counter", 1);
        setStaticField("a", new ReentrantLock());
        setStaticField("b", new ReentrantLock());
    }

    @RepeatedTest(10)
    public void runTest() throws Exception {
        final Exception[] t1Exception = { null };
        final Exception[] t2Exception = { null };

        Thread t1 = new Thread(() -> {
            try {
                invokeStaticMethod("thread1"); //Deadlock01Bad.thread1();
            } catch (Exception e) {
                t1Exception[0] = e;
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                invokeStaticMethod("thread2");//Deadlock01Bad.thread2();
            } catch (Exception e) {
                t2Exception[0] = e;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // Did either thread throw an exception?
        boolean deadlockDetected = (t1Exception[0] != null) || (t2Exception[0] != null);
        Assertions.assertTrue(deadlockDetected, "Expected concurrency bug: deadlock should occur");
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

    // --- Helper reflection methods ---
    private static void setStaticInt(String fieldName, int value) throws Exception {
        Field f = Deadlock01BadTest.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.setInt(null, value);
    }

    private static void setStaticField(String fieldName, Object value) throws Exception {
        Field f = Deadlock01BadTest.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }

    private static int getStaticInt(String fieldName) throws Exception {
        Field f = Deadlock01BadTest.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.getInt(null);
    }

    private void invokeStaticMethod(String methodName) throws Exception {
        Method m = Deadlock01BadTest.class.getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(null);
    }

    private void setStaticLock(String fieldName, ReentrantLock value) throws Exception {
        Field f = Deadlock01BadTest.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }
}
