package org.example;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

import gov.nasa.jpf.util.test.TestJPF;

public class StringNumbererTest extends TestJPF {

    public static StringNumberer stringNumberer;
    AtomicReference<Throwable> failure = new AtomicReference<>();


    @RepeatedTest(100)
    void raceBetweenFindOrAddAndFind() throws Exception {

        CyclicBarrier barrier = new CyclicBarrier(2);
        StringNumberer.setStartBarrier(barrier);

        StringNumberer sn = new StringNumberer();

        Thread writer = new Thread(() -> {
            // calls find or add, and then the barrier forces wait. Other thread then CANNOT run until we're effectively inside the lock. Needs verification.
            sn.findOrAdd("k");
        }, "writer");

        Thread reader = new Thread(() -> {
            try {
                barrier.await();
                NumberedString v = sn.find("k");

                Assertions.assertNotNull(v, "Observed null from find() while writer in findOrAdd");


                // assert v != null : "Observed null from find() while writer in findOrAdd";
            } catch (Throwable t) {
                // Catch failure on atomic reference, i think this is the way to do it.
                failure.set(t);
            }
        }, "reader");

        try {
        writer.start();
        reader.start();
        writer.join();
        reader.join();

        if (failure.get() != null) {
            throw new AssertionError("Worker thread failed", failure.get());
        }} 
        finally {
        // ensure we don’t leave a stale barrier running. 
        StringNumberer.setStartBarrier(null);
        }

    }


    @Test
    public void testStringNumbererWithJpf() throws InterruptedException {
        if (verifyAssertionError(
                "+classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/soot/app/build/classes/java/main;/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/soot/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea"
//                "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
//                "+search.class = gov.nasa.jpf.search.Reset_Search",
////                "+search_with_reset.k = 500",
//                "+search_with_reset.probabilities = 0.999 0.001",
//                "+search_with_reset.eps = 0.1",
//                "+numberOfThreads = 2",
//                "+search.multiple_errors = false",
//                "+jpf.report.console.property_violation = error",
//                "+report.console.finished = result,statistics,error",
//                "+report.unique_errors = true"

        )) {  // specifies the test goal, "jpfOptions" are optional

            StringNumberer sn = new StringNumberer();

            CountDownLatch entered = new CountDownLatch(1);

            Thread writer = new Thread(() -> {
                entered.countDown();        // we mark "writer is about to call findOrAdd"
                sn.findOrAdd("k");
            }, "writer");

            Thread reader = new Thread(() -> {
                try {
                    entered.await();        // don't run find() until writer signalled
                } catch (InterruptedException e) {
                }
                NumberedString v = sn.find("k");
                assert v != null : "Observed null from find()";
            }, "reader");

            writer.start();
            reader.start();
            writer.join();
            reader.join();

        }
    }
}
