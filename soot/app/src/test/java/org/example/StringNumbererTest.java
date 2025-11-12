package org.example;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import gov.nasa.jpf.util.test.TestJPF;

public class StringNumbererTest extends TestJPF {

    public static StringNumberer stringNumberer;


    @RepeatedTest(100)
    void concurrencCallToFindOrAddFind() throws Exception {
        StringNumberer sn = new StringNumberer();
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<NumberedString> seen = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();


        Thread t1 = new Thread(() -> {
//            await(start);
            sn.findOrAdd("k");
        }, "t1");
        Thread t2 = new Thread(() -> {
            try {
//                await(start);
                seen.set(sn.find("k"));
                Assertions.assertNotNull(seen.get(),
                        "Observed null from find()");
            } catch (Throwable t) {
                failure.set(t);
            }
        }, "t2");

        t1.start();
        t2.start();
//        start.countDown();
        t1.join();
        t2.join();

        if (failure.get() != null) {
            throw new AssertionError("worker thread failed", failure.get());
        }
    }

    private static void await(CountDownLatch l) {
        try {
            l.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testStringNumbererWithJpf() throws InterruptedException {
        if (verifyAssertionError(
                "+classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/soot/app/build/classes/java/main;/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/soot/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
//                "+search_with_reset.k = 500",
                "+search_with_reset.probabilities = 0.999 0.001",
                "+search_with_reset.eps = 0.1",
                "+numberOfThreads = 2",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"

        )) {  // specifies the test goal, "jpfOptions" are optional

            StringNumberer sn = new StringNumberer();
            CountDownLatch start = new CountDownLatch(1);
            AtomicReference<NumberedString> seen = new AtomicReference<>();

            Thread t1 = new Thread(() -> {
                await(start);
                sn.findOrAdd("k");
            }, "t1");
            Thread t2 = new Thread(() -> {
                await(start);
                seen.set(sn.find("k"));
                if (seen.get() == null)
                    throw new AssertionError("race: We found null from calling StringNumberer.find() & findOrAdd() concurrently");
            }, "t2");

            t1.start();
            t2.start();
            start.countDown();
            t1.join();
            t2.join();

        }
    }
}
