package org.example;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;

import org.junit.jupiter.api.*;


public class StripedMapTest extends TestJPF {
    StripedMap<Integer, String> map;
    private static int x;
    public static String classPath;
    

    @BeforeEach
    public void setup() {
        classPath = getClassPath();
        map = new StripedMap<>(5);
        x = 0;

    }

    @Test
    public void testContains() throws InterruptedException{
        if (verifyNoPropertyViolation(
                "+classpath=" + classPath,
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                // "+search_with_reset.k = 10",
                "+search_with_reset.probabilities = 0.999 0.001",
                "+search_with_reset.eps = 0.1",
                "+numberOfThreads = 10",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"
        )) {
        OurMap<Integer, String> map = new StripedMap<>(4);

        Thread[] threads = new Thread[10];


        for (int i = 0; i < threads.length; i++) {
            final int mul = i * 100;
            threads[i] = new Thread(() -> {
                for (int k = 0; k < 50; k++) {
                    map.put(k + mul, k + "");
                    assert map.containsKey(k + mul);
                }
            });
        }

        for (int i = 0; i < threads.length; i++) threads[i].start();

        for (int i = 0; i < threads.length; i++) threads[i].join();

        for (int i = 0; i < threads.length; i++) {
            final int mul = i * 100;

            for (int k = 0; k < 50; k++) {
                assert map.containsKey(k + mul);
            }
        }
        }
    }       
    
    @Test
    public void testGet() throws InterruptedException{
        if (verifyNoPropertyViolation(
                "+classpath=" + classPath,
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.probabilities = 0.999 0.001",
                "+search_with_reset.eps = 0.1",
                "+numberOfThreads = 10",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"
        )) {

        Thread[] threads = new Thread[5];
        StripedMap<Integer, String> map = new StripedMap<>(4);

        for (int i = 0; i < threads.length; i++) {
            final int mul = i * 100;
            final int indicator = i;
            threads[i] = new Thread(() -> {

                for (int j = 0; j < 100; j++) {
                    map.put(j + mul, "Thread" + indicator);
                    String v = map.get(j + mul);
                    assert v != null : "The value is null";
                    assert v.equals("Thread" + indicator) : "Wrong value";
                }
            });
        }

        for (int i = 0; i < threads.length; i++) threads[i].start();
        for (int i = 0; i < threads.length; i++) threads[i].join();
        }
    
}

    public static String getClassPath() {
        String userDir = System.getProperty("user.dir");   //
//        System.out.println(userDir);
        String fs = File.separator;                   // For / or \ for win/osx, neet help which works, as \ escapes space in absolutep aths on osx. This is where our problem is if we have problem on windows.
        String ps = File.pathSeparator;

        String testClasses = userDir + fs + "build" + fs + "classes" + fs + "java" + fs + "test";
        String mainClasses = userDir + fs + "build" + fs + "classes" + fs + "java" + fs + "main";

        return testClasses + ps + mainClasses;
    }
    }

    

