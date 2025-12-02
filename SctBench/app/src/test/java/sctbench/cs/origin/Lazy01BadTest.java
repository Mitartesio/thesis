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
import java.util.concurrent.locks.ReentrantLock;

public class Lazy01BadTest {
    Lazy01Bad test;
    public static String classPath;

    @BeforeAll
    public static void init() {
        classPath = getClassPath();
    }

    @BeforeEach
    public void setup() throws Exception {
        setStaticInt("data", 0);
        setStaticObject("mutex", new ReentrantLock());
    }

    //Lazy01Bad - Lock ordered state bug. Often results in sequential consistency ater starting the threads
    //passes if it finds gets the exception from thread 3
    @RepeatedTest(100)
    public void runTest() throws Exception {
        final Exception[] t1Ex = { null };
        final Exception[] t2Ex = { null };
        final Exception[] t3Ex = { null };

        Thread t1 = new Thread(() -> {
            try {
                Lazy01Bad.class.getDeclaredMethod("thread1").invoke(null);
            } catch (Exception e) {
                t1Ex[0] = e;
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                Lazy01Bad.class.getDeclaredMethod("thread2").invoke(null);
            } catch (Exception e) {
                t2Ex[0] = e;
            }
        });

        Thread t3 = new Thread(() -> {
            try {
                Lazy01Bad.class.getDeclaredMethod("thread3").invoke(null);
            } catch (Exception e) {
                t3Ex[0] = e;
            }
        });

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        boolean bugDetected = (t1Ex[0] != null) ||
                (t2Ex[0] != null) ||
                (t3Ex[0] != null);

        Assertions.assertTrue(
                bugDetected,
                "Concurrency bug expected in Lazy01Bad but no thread failed."); 
    }

    //state assertion finds the bug if it fails the test
    @RepeatedTest(100)
    public void runTest2() throws Exception {
        Thread t1 = threadFor("thread1");
        Thread t2 = threadFor("thread2");
        Thread t3 = threadFor("thread3");

        t1.start();
        t2.start();
        t3.start();

        int dataWhileRunning = getStaticInt("data");
        Assertions.assertTrue(dataWhileRunning < 3,
                "Intermediate state: data should be less than 3 before all threads finish");

        t1.join();
        t2.join();
        t3.join();

        int finalData = getStaticInt("data");
        Assertions.assertTrue(finalData < 3,
                "Expected concurrency bug: final data should be less than 3 if race occurred");

        System.out.println("Final data: " + finalData);
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
        Field f = Lazy01Bad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setInt(null, value);
    }

    private int getStaticInt(String name) throws Exception {
        Field f = Lazy01Bad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(null);
    }

    private void setStaticObject(String name, Object value) throws Exception {
        Field f = Lazy01Bad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }

    private Thread threadFor(String methodName) {
        return new Thread(() -> {
            try {
                Method m = Lazy01Bad.class.getDeclaredMethod(methodName);
                m.setAccessible(true);
                m.invoke(null); // static method → null target
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
