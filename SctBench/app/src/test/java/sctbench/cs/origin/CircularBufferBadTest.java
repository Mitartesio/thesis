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

public class CircularBufferBadTest {
    CircularBufferBad test;
    public static String classPath;

    @BeforeAll
    public static void init() {
        classPath = getClassPath();
    }

    @BeforeEach
    public void setup() throws Exception {
        setStaticInt("first", 0);
        setStaticInt("next", 0);
        setStaticInt("buffer_size", 10);
        setStaticBoolean("send", true);
        setStaticBoolean("receive", false);
        
        // Initialize buffer array
        Field bufferField = CircularBufferBad.class.getDeclaredField("buffer");
        bufferField.setAccessible(true);
        bufferField.set(null, new char[10]);

        // Reset lock
        Field lockField = CircularBufferBad.class.getDeclaredField("m");
        lockField.setAccessible(true);
        lockField.set(null, new ReentrantLock());
    }

    @RepeatedTest(10)
    public void runTest() throws Exception {
        final Exception[] t1Exception = { null };
        final Exception[] t2Exception = { null };

        Thread t1 = new Thread(() -> {
            try {
                CircularBufferBad.t1();
            } catch (Exception e) {
                t1Exception[0] = e;
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                CircularBufferBad.t2();
            } catch (Exception e) {
                t2Exception[0] = e;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // If either thread throws an exception, test fails
        boolean bugDetected = (t1Exception[0] != null) || (t2Exception[0] != null);
        Assertions.assertTrue(bugDetected, "Expected concurrency bug: circular buffer assertion failed");
    }

    //Not sure this makes sense to do. As JVM already has a hard time with deadlocks
    //I htink the throw exception catching might just be better.
    @RepeatedTest(10)
    public void runTest2() throws Exception {

        Thread t1 = new Thread(CircularBufferBad::t1);
        Thread t2 = new Thread(CircularBufferBad::t2);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        int first = getStaticInt("first");
        int next = getStaticInt("next");
        boolean send = getStaticBoolean("send");
        boolean receive = getStaticBoolean("receive");

        // If race condition happened, the buffer would be inconsistent
        // t2 assertion might have failed internally, we check logical inconsistency
        boolean inconsistencyDetected = first != next || send || receive;

        Assertions.assertTrue(inconsistencyDetected,
                "Expected concurrency bug: CircularBuffer state inconsistent after threads");
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

    private void setStaticInt(String name, int value) throws Exception {
        Field f = CircularBufferBad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setInt(null, value);
    }

    private void setStaticBoolean(String name, boolean value) throws Exception {
        Field f = CircularBufferBad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setBoolean(null, value);
    }

    private void setStaticObject(String name, Object value) throws Exception {
        Field f = CircularBufferBad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }

    private int getStaticInt(String name) throws Exception {
        Field f = CircularBufferBad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(null);
    }

    private boolean getStaticBoolean(String name) throws Exception {
        Field f = CircularBufferBad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getBoolean(null);
    }

    private char getStaticChar(String name) throws Exception {
        Field f = CircularBufferBad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getChar(null);
    }

    private Object getStaticObject(String name) throws Exception {
        Field f = CircularBufferBad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(null);
    }

}
