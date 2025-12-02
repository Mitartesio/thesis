package sctbench.cs.origin;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Copy;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.test.TestJPF;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

public class Phase01BadTest {
    Lazy01Bad test;
    public static String classPath;

    @BeforeAll
    public static void init() {
        classPath = getClassPath();
    }

    @BeforeEach
    public void setup() throws Exception {
        setStaticInt("lockStatus", 0);
        setStaticObject("x", new ReentrantLock());
        setStaticObject("y", new ReentrantLock());
    }

    //Passes everytime due to these: if (lockStatus == 1) {
    //System.out.println("Deadlock detected");
    //throw new RuntimeException();
    @RepeatedTest(100)
    public void runTest() throws Exception {
        final Exception[] t1Ex = { null };
        final Exception[] t2Ex = { null };

        Thread t1 = threadFor("thread1", t1Ex);
        Thread t2 = threadFor("thread1", t2Ex);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        boolean bugDetected = (t1Ex[0] != null) || (t2Ex[0] != null);
        Assertions.assertTrue(bugDetected, "Expected concurrency bug: Phase01Bad deadlock detected");
    }

    //Passes everytime due to these: if (lockStatus == 1) {
    //System.out.println("Deadlock detected");
    //throw new RuntimeException();
    @RepeatedTest(100)
    public void runTest2() throws Exception {
        Thread t1 = threadFor("thread1");
        Thread t2 = threadFor("thread2");

        t1.start();
        t2.start();

        // can add small delay if needed
        // Thread.sleep(10);

        int status = getStaticInt("lockStatus");
        assertTrue(status <= 1, "lockStatus should not exceed 1 (concurrency bug)");

        t1.join();
        t2.join();
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

    private void setStaticInt(String name, int value) throws Exception {
        Field f = Phase01Bad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setInt(null, value);
    }

    private int getStaticInt(String name) throws Exception {
        Field f = Phase01Bad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(null);
    }

    private void setStaticObject(String name, Object value) throws Exception {
        Field f = Phase01Bad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }

    private Thread threadFor(String methodName, Exception[] exArray) {
        return new Thread(() -> {
            try {
                Method m = Phase01Bad.class.getDeclaredMethod(methodName);
                m.setAccessible(true);
                m.invoke(null);
            } catch (Exception e) {
                exArray[0] = e;
            }
        });
    }

    private Thread threadFor(String methodName) {
        return new Thread(() -> {
            try {
                Method m = Phase01Bad.class.getDeclaredMethod(methodName);
                m.setAccessible(true);
                m.invoke(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
