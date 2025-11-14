package org.example;

import gov.nasa.jpf.util.test.TestJPF;

import gov.nasa.jpf.vm.Verify;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.*;


public class StripedMapTest2 extends TestJPF {
    StripedMap<Integer, String> map;
    volatile int answer;
    volatile int x;
    AtomicInteger count;


    @BeforeEach
    public void setup() {

        map = new StripedMap<>(10);
        this.answer = 0;
        this.x = 0;
        this.count = new AtomicInteger();
    }

    @Test
    public void testMinimizationWithJpf() throws InterruptedException {
        if (verifyNoPropertyViolation(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 500",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 3",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"

        )) {  // specifies the test goal, "jpfOptions" are optional
            final StripedMap<Integer, Integer> map = new StripedMap<>(4);

            Thread t1 = new Thread(() -> {
                // Writer 1: put 0..9
                for (int i = 0; i < 10; i++) {
                    map.put(i, i);
                    // help JPF create more interleavings
                    Thread.yield();
//                   Verify.yield();
                }
            }, "t1");

            Thread t2 = new Thread(() -> {
                // Writer 2: put 10..19
                for (int i = 10; i < 20; i++) {
                    map.put(i, i);
                    Thread.yield();
                }
            }, "t2");

            Thread t3 = new Thread(() -> {
                // Reader: read all keys a couple of times
                for (int r = 0; r < 3; r++) {
                    for (int i = 0; i < 20; i++) {
                        Integer v = map.get(i);
                        // Sanity: if size says >= i+1 then key i must be there
                        // if (map.size() >= i + 1) {
                        //     // assert v != null : "Key " + i + " missing despite size >= " + (i + 1);
                        // }
                        Thread.yield();
                    }
                }
            }, "t3");

            t1.start();
            t2.start();
            t3.start();

            t1.join();
            t2.join();
            t3.join();

            // Final sanity check after all threads are done
            assert map.size() <= 20 : "Map grew beyond expected size!";
        }
    }

    @Test
    public void testMapSize() throws InterruptedException {
        if (verifyNoPropertyViolation(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 100",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 10",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"

        )) {
            final StripedMap<Integer, Integer> map = new StripedMap<>(10);
            
            Thread[] threads = new Thread[10];
            for(int i = 1; i<=threads.length; i++){
                final int mul = i * 10;
                threads[i-1] = new Thread(() -> {
                    for(int j = 1; j<=10; j++){
                        map.put(j + mul, j);
                        
                    }

                });
            }

            for(int i = 0; i<threads.length; i++){
                threads[i].start();
            }
            System.out.println(map.size());
            for(int i = 0; i<threads.length; i++){
                threads[i].join();
            }
        System.out.println("Size: " + map.size());
        assert map.size() == 100 : "Bug found";
        }
    }

    @Test
    public void testMapSize2() throws InterruptedException {
        if (verifyNoPropertyViolation(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 100",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 10",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"

        )) {
            final StripedMap<Integer, Integer> map = new StripedMap<>(10);
            
            Thread[] threads = new Thread[10];
            for(int i = 1; i<=threads.length; i++){
                final int mul = i * 10;
                threads[i-1] = new Thread(() -> {
                    for(int j = 1; j<=10; j++){
                        map.put(j + mul, j);
                        
                    }

                });
            }

            for(int i = 0; i<threads.length; i++){
                threads[i].start();
            }
            System.out.println(map.size());
            for(int i = 0; i<threads.length; i++){
                threads[i].join();
            }
            for(int i = 1; i<=100; i++){
                assert map.containsKey(i+10);
            }
        }
    }

     @Test
    public void getAndPut() throws InterruptedException {
        if (verifyNoPropertyViolation(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 500",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 3",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"

        )) {
            final StripedMap<Integer, Integer> map = new StripedMap<>(10);

            for(int i = 0; i<100; i++){
                
            }
        }
    }

}
