package org.example;

import gov.nasa.jpf.util.test.TestJPF;

import gov.nasa.jpf.vm.Verify;
import org.junit.jupiter.api.*;


public class StripedMapTest extends TestJPF {
    StripedMap<Integer, String> map;


    @BeforeEach
    public void setup() {

        map = new StripedMap<>(10);

    }

    @Test
    public void testMinimizationWithJpf() throws InterruptedException {
        if (verifyNoPropertyViolation(
                "+classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/HashMaps/app/build/classes/java/test:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/HashMaps/app/build/classes/java/main",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
//                "+search_with_reset.k = 500",
                "+search_with_reset.probabilities = 0.999 0.001",
                "+search_with_reset.eps = 0.1",
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
                        if (map.size() >= i + 1) {
                            assert v != null : "Key " + i + " missing despite size >= " + (i + 1);
                        }
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


}
