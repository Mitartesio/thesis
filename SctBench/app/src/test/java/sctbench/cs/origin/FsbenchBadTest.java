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

public class FsbenchBadTest {
    FsbenchBad test;
    public static String classPath;

    @BeforeAll
    public static void init() {
        classPath = getClassPath();
    }

    @BeforeEach
    public void setup() throws Exception {
        int numBlocks = getStaticInt("NUMBLOCKS");
        int numInode = getStaticInt("NUMINODE");
        int numThreads = getStaticInt("NUM_THREADS");

        setStaticLockArray("locki", new ReentrantLock[numBlocks]);
        setStaticLockArray("lockb", new ReentrantLock[numBlocks]);
        setStaticIntArray("busy", new int[numBlocks]);
        setStaticIntArray("inode", new int[numInode]);
        setStaticThreadArray("threads", new Thread[numThreads]);

        // Initialize the locks
        for (int i = 0; i < numBlocks; i++) {
            getStaticLockArray("locki")[i] = new ReentrantLock();
            getStaticLockArray("lockb")[i] = new ReentrantLock();
        }
    }

    // 6 out of 32 threads will deterministically fail this assertion every single
    // time, no concurrency needed.
    // 26 numblocks, 32numinode, num_threads 27.
    // assertions might make it too easy to find
    // It is a logic bug that happens inside a multi-threaded context. Not a true race condition
    // Might be due to the translation from Fray.
    @RepeatedTest(100)
    public void runTest() throws Exception {
        final Exception[] exceptions = new Exception[getStaticInt("NUM_THREADS")];

        for (int i = 0; i < getStaticInt("NUM_THREADS"); i++) {
            final int tid = i;
            Thread t = new Thread(() -> {
                try {
                    invokeStaticMethod("threadRoutine", int.class, tid);
                } catch (Exception e) {
                    exceptions[tid] = e;
                }
            });
            getStaticThreadArray("threads")[i] = t;
            t.start();
        }

        for (int i = 0; i < getStaticInt("NUM_THREADS"); i++) {
            getStaticThreadArray("threads")[i].join();
        }

        boolean bugDetected = false;
        for (Exception e : exceptions) {
            if (e != null) {
                bugDetected = true;
                break;
            }
        }

        Assertions.assertTrue(bugDetected, "Expected concurrency bug: FsbenchBad should trigger assertion failure");
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

    // --- Reflection helper methods --- Fields and methods are private in Fsbench
    private void invokeStaticMethod(String methodName, Class<?> paramType, Object param) throws Exception {
        Method m = FsbenchBad.class.getDeclaredMethod(methodName, paramType);
        m.setAccessible(true);
        m.invoke(null, param);
    }

    private int getStaticInt(String fieldName) throws Exception {
        Field f = FsbenchBad.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.getInt(null);
    }

    private ReentrantLock[] getStaticLockArray(String fieldName) throws Exception {
        Field f = FsbenchBad.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (ReentrantLock[]) f.get(null);
    }

    private Thread[] getStaticThreadArray(String fieldName) throws Exception {
        Field f = FsbenchBad.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (Thread[]) f.get(null);
    }

    private void setStaticLockArray(String fieldName, ReentrantLock[] value) throws Exception {
        Field f = FsbenchBad.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }

    private void setStaticIntArray(String fieldName, int[] value) throws Exception {
        Field f = FsbenchBad.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }

    private void setStaticThreadArray(String fieldName, Thread[] value) throws Exception {
        Field f = FsbenchBad.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }
}
