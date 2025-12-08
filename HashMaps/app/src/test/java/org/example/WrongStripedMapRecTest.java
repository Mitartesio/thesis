package org.example;

import gov.nasa.jpf.util.test.TestJPF;

import java.util.HashMap;
import java.util.Map;

import org.example.WrongMaps.WrongStripedMap;
import org.example.WrongMaps.WrongStripedMap2;
import org.example.WrongMaps.WrongStripedMap3;
import org.example.WrongMaps.WrongStripedMap4;
import org.example.WrongMaps.WrongStripedMap6;
import org.junit.jupiter.api.Test;

import gov.nasa.jpf.util.test.TestJPF;

public class WrongStripedMapRecTest extends TestJPF{
    
    // @Test
    public void testGetWrongMap1() throws InterruptedException{
        if (verifyAssertionError(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 1000",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 10",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"
        )) {


            // for(int count = 0; count<10000; count++){

            OurMap<Integer, String> map = new WrongStripedMap<>(2);


            int operations = 200;
            for(int i = 0; i<operations; i++){
                map.put(i, i+"");
            }

            Thread[] threads = new Thread[10];

            for(int i = 0; i<threads.length / 2; i++){
                final int mul = i * (operations +1);
                threads[i] = new Thread(() -> {
                    for(int k = operations; k<operations; k++){
                            map.put(k + mul, k+"");
                        }
                });
            }

            for(int i = threads.length/2; i<threads.length; i++){
                threads[i] = new Thread(() -> {
                    for(int k = 0; k<operations; k++){
                        assert map.get(k).equals(k+"");
                    }
                });
            }

            for(int i = 0; i<threads.length; i++)threads[i].start();

            for(int i = 0; i<threads.length; i++)threads[i].join();
        }
    }

    @Test
    public void testReallocationWrongMap2() throws Exception{
        if(verifyAssertionError(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 1500",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 10",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true",
                "+log.warning=gov.nasa.jpf",
                "+log.info=gov.nasa.jpf"
        )) {

            OurMap<Integer, String> map = new WrongStripedMap4<>(4);

            Thread[] threads = new Thread[10];
            

            for(int i = 0; i<threads.length; i++){
                final int mul = i * 100;
                threads[i] = new Thread(() -> {
                    for(int k = 0; k<50; k++){
                        map.put(k+mul, k + "");
                    }
                });
            }

            for(int i = 0; i<threads.length; i++)threads[i].start();

            for(int i = 0; i<threads.length; i++)threads[i].join();

            for(int i = 0; i<threads.length; i++){
                final int mul = i * 100;

                for(int k = 0; k<50; k++){
                    assert map.containsKey(k+mul);
                }
            }
        }
    }

    // @Test
    public void testReallocationWrongMap4() throws Exception{
        if(verifyAssertionError(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 1000",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 10",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true",
                "+log.warning=gov.nasa.jpf",
                "+log.info=gov.nasa.jpf"
        )) {

            OurMap<Integer, String> map = new WrongStripedMap4<>(4);
            Thread[] threads = new Thread[10];
            

            for(int i = 0; i<threads.length; i++){
                final int mul = i * 100;
                threads[i] = new Thread(() -> {
                    for(int k = 0; k<100; k++){
                        map.put(k+mul, k + "");
                    }
                });
            }

            for(int i = 0; i<threads.length; i++)threads[i].start();

            for(int i = 0; i<threads.length; i++)threads[i].join();

            for(int i = 0; i<threads.length; i++){
                final int mul = i * 100;

                for(int k = 0; k<100; k++){
                    assert map.containsKey(k+mul);
                }
            }
        }
    }

//    @Test
    public void bigTestWrongMap4() throws Exception{
        if (verifyAssertionError(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 1000",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 10",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"
        )) {

            OurMap<Integer, String> map = new WrongStripedMap4<>(4);


            final int threadCount = 10;
            final int perThread = 50;
            final int range = 7;
            
            final Thread[] threads = new Thread[threadCount];
            final long[] keySumPerThread = new long[threadCount];
            final int[][] addedByPerThread = new int[threadCount][threadCount];


            
        for (int t = 0; t < threadCount; t++) {
            final int myThread = t;
            threads[t] = new Thread(() -> {

//                System.out.println("hello");


                final int[] addedBy = new int[threadCount];
                // Sum of keys added, minus sum of keys removed, by the thread in question
                long keySum = 0;


                for (int i = 0; i < perThread; i++) {

                    // Make them all operate on the x-range so there's potential for higher contention
                    int key = (myThread + i) % range;

                    final String value = String.format("%02d:%d", myThread, key);
                    // 	  System.out.print(value + " ");
                    if (!map.containsKey(key)) {
                        String oldValue = map.put(key, value);
                        if (oldValue == null) {
                            // add to the sum if it wasnt in the map previously
                            keySum += key;
                            addedBy[myThread]++;

                        } else {
                            // if it is already there, remove old value, add new
                            int oldThread = Integer.parseInt(oldValue.substring(0, 2));
                            addedBy[oldThread]--;
                            addedBy[myThread]++;
                        }
                    } else {

                        String v = map.get(key);
                        if (v != null) {
                            final int valueKey = Integer.parseInt(v.substring(3));
                            assert key == valueKey : "mid-run key/value mismatch";
                        }

                    }


                    if ((i & 7) == 0) { // so we churn the set eery 8th operation
                        String old = map.remove(key);
                        if (old != null) {
                            keySum -= key;
                            int oldT = Integer.parseInt(old.substring(0, 2));
                            addedBy[oldT]--;
                        }
                    }
                }
                keySumPerThread[myThread] = keySum;
                System.arraycopy(addedBy, 0, addedByPerThread[myThread], 0, threadCount);


            }, "t" + (myThread + 1)); // names: t1,t2,... (so we can use it with uniform scheduler names);
        }


        // Start & join (JPF mode: no barrier needed; JVM stress: optionally add a latch like peter did)
        for (Thread th : threads) th.start();
        for (Thread th : threads) th.join();

        long totalKeySum = 0L;
        int[] totalAddedBy = new int[threadCount];
        for (int t = 0; t < threadCount; t++) {
            totalKeySum += keySumPerThread[t];
            for (int u = 0; u < threadCount; u++) {
                totalAddedBy[u] += addedByPerThread[t][u];
            }
        }


        final long[] actualKeySum = new long[1],
                actualSize = new long[1];

        final int[] actualAddedBy = new int[threadCount];


        map.forEach((k, v) -> {
            actualKeySum[0] += k;
            actualSize[0]++;
            int madeBy = Integer.parseInt(v.substring(0, 2));
            int valueKey = Integer.parseInt(v.substring(3));
            assert k == valueKey : "end key/value mismatch";
            actualAddedBy[madeBy]++;
        });


        assert actualSize[0] == map.size() : "Actual size mismatch";

        assert actualKeySum[0] == totalKeySum : "Keysum mismatch";

        for (int t = 0; t < threadCount; t++) {
            assert totalAddedBy[t] == actualAddedBy[t] : "addedBy mismatch t=" + t;
        }
    }
}

//    @Test
    public void bigTestWrongMap6() throws Exception{
        if (verifyAssertionError(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 1000",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 10",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"
        )) {

            OurMap<Integer, String> map = new WrongStripedMap6<>(4);


            final int threadCount = 10;
            final int perThread = 50;
            final int range = 7;
            
            final Thread[] threads = new Thread[threadCount];
            final long[] keySumPerThread = new long[threadCount];
            final int[][] addedByPerThread = new int[threadCount][threadCount];


            
        for (int t = 0; t < threadCount; t++) {
            final int myThread = t;
            threads[t] = new Thread(() -> {

//                System.out.println("hello");


                final int[] addedBy = new int[threadCount];
                // Sum of keys added, minus sum of keys removed, by the thread in question
                long keySum = 0;


                for (int i = 0; i < perThread; i++) {

                    // Make them all operate on the x-range so there's potential for higher contention
                    int key = (myThread + i) % range;

                    final String value = String.format("%02d:%d", myThread, key);
                    // 	  System.out.print(value + " ");
                    if (!map.containsKey(key)) {
                        String oldValue = map.put(key, value);
                        if (oldValue == null) {
                            // add to the sum if it wasnt in the map previously
                            keySum += key;
                            addedBy[myThread]++;

                        } else {
                            // if it is already there, remove old value, add new
                            int oldThread = Integer.parseInt(oldValue.substring(0, 2));
                            addedBy[oldThread]--;
                            addedBy[myThread]++;
                        }
                    } else {

                        String v = map.get(key);
                        if (v != null) {
                            final int valueKey = Integer.parseInt(v.substring(3));
                            assert key == valueKey : "mid-run key/value mismatch";
                        }

                    }


                    if ((i & 7) == 0) { // so we churn the set eery 8th operation
                        String old = map.remove(key);
                        if (old != null) {
                            keySum -= key;
                            int oldT = Integer.parseInt(old.substring(0, 2));
                            addedBy[oldT]--;
                        }
                    }
                }
                keySumPerThread[myThread] = keySum;
                System.arraycopy(addedBy, 0, addedByPerThread[myThread], 0, threadCount);


            }, "t" + (myThread + 1)); // names: t1,t2,... (so we can use it with uniform scheduler names);
        }


        // Start & join (JPF mode: no barrier needed; JVM stress: optionally add a latch like peter did)
        for (Thread th : threads) th.start();
        for (Thread th : threads) th.join();

        long totalKeySum = 0L;
        int[] totalAddedBy = new int[threadCount];
        for (int t = 0; t < threadCount; t++) {
            totalKeySum += keySumPerThread[t];
            for (int u = 0; u < threadCount; u++) {
                totalAddedBy[u] += addedByPerThread[t][u];
            }
        }


        final long[] actualKeySum = new long[1],
                actualSize = new long[1];

        final int[] actualAddedBy = new int[threadCount];


        map.forEach((k, v) -> {
            actualKeySum[0] += k;
            actualSize[0]++;
            int madeBy = Integer.parseInt(v.substring(0, 2));
            int valueKey = Integer.parseInt(v.substring(3));
            assert k == valueKey : "end key/value mismatch";
            actualAddedBy[madeBy]++;
        });


        assert actualSize[0] == map.size() : "Actual size mismatch";

        assert actualKeySum[0] == totalKeySum : "Keysum mismatch";

        for (int t = 0; t < threadCount; t++) {
            assert totalAddedBy[t] == actualAddedBy[t] : "addedBy mismatch t=" + t;
        }
    }
}
}
