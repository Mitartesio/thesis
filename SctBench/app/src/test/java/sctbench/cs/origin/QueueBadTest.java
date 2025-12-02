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

public class QueueBadTest {
    QueueBad test;
    public static String classPath;

    @BeforeAll
    public static void init() {
        classPath = getClassPath();
    }

    @BeforeEach
    public void setup() throws Exception {
        // Reset static fields
        setObject("mutex", new ReentrantLock());
        setObject("queue", new QueueBad.QType());
        setObject("stored_elements", new int[QueueBad.SIZE]);
        setStaticBoolean("enqueue_flag", true);
        setStaticBoolean("dequeue_flag", false);

        // Initialize queue
        Method init = QueueBad.class.getDeclaredMethod("init", QueueBad.QType.class);
        init.setAccessible(true);
        init.invoke(null, getObject("queue"));
    }

    //passes if it finds any exception or assertion error such as the ones in the class itself
    @RepeatedTest(10)
    public void runTest() throws Exception {
        final Exception[] t1Exception = { null };
        final Exception[] t2Exception = { null };

        Thread t1 = new Thread(() -> {
            try {
                Method thread1 = QueueBad.class.getDeclaredMethod("main", String[].class);
                thread1.setAccessible(true);
                thread1.invoke(null, (Object) new String[] {});
            } catch (Exception e) {
                t1Exception[0] = e;
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                Method thread1 = QueueBad.class.getDeclaredMethod("main", String[].class);
                thread1.setAccessible(true);
                thread1.invoke(null, (Object) new String[] {});
            } catch (Exception e) {
                t2Exception[0] = e;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        boolean bugDetected = (t1Exception[0] != null) || (t2Exception[0] != null);
        Assertions.assertTrue(bugDetected, "Expected concurrency bug: queue assertion failed");
    }

    // Here, the threads are run normally, and you check the post-condition after all threads join.

    //The assertion is outside the threads, comparing some final state 
    // of shared data.
    @RepeatedTest(10)
    public void runTest2() throws Exception {
        Thread t1 = threadFor("main");
        Thread t2 = threadFor("main");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        int[] stored = (int[]) getObject("stored_elements");
        QueueBad.QType q = (QueueBad.QType) getObject("queue");

        // checks that first element should match
        Assertions.assertEquals(stored[0], q.element[0], "Queue bug detected");
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
    private void setStaticBoolean(String name, boolean value) throws Exception {
        Field f = QueueBad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setBoolean(null, value);
    }

    private void setObject(String name, Object value) throws Exception {
        Field f = QueueBad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }

    private Object getObject(String name) throws Exception {
        Field f = QueueBad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(null);
    }

    private Thread threadFor(String methodName) {
        return new Thread(() -> {
            try {
                Method m = QueueBad.class.getDeclaredMethod(methodName, String[].class);
                m.setAccessible(true);
                m.invoke(null, (Object) new String[] {});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
