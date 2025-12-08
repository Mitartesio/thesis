package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.WrongMaps.*;
import org.junit.jupiter.api.Test;

import gov.nasa.jpf.util.test.TestJPF;

public class WrongStripedMapJPFTest extends TestJPF {
    int count = 0;
    Map<String, String> log = new HashMap<>();
    int totalNumberOfMaps = 6;
    String[] namesOfMaps = new String[]{"WrongMap", "WrongMap2", "WrongMap3", "WrongMap4", "WrongMap5", "WrongMap6"};

    public OurMap<Integer, String> getMap(String name) {

        int lockCount = 5;

        if (name.equals("Hello")) {
            return new StripedMap<Integer, String>(lockCount);
        }

        if (name.equals("WrongMap")) {
            return new WrongStripedMap<Integer, String>(lockCount);
        } else if (name.equals("WrongMap2")) {
            return new WrongStripedMap2<>(lockCount);
        } else if (name.equals("WrongMap3")) {
            return new WrongStripedMap3<>(lockCount);
        } else if (name.equals("WrongMap4")) {
            return new WrongStripedMap4<>(lockCount);
        } else if (name.equals("WrongMap5")) {
            return new WrongStripedMap5<>(lockCount);
        } else if (name.equals("WrongMap6")) {
            return new WrongStripedMap6<>(lockCount);
        } else {
            return new WrongStripedMap7<>(lockCount);
        }
    }

    @Test
    public void runAll() throws InterruptedException {
        testReallocation2("");
        // testReallocation2("WrongMap");


        // assert log.isEmpty() : readLog();
    }

    private String readLog() {
        StringBuilder bobTheBuilder = new StringBuilder();

        for (String key : log.keySet()) {
            bobTheBuilder.append(log.get(key) + "\n");
        }

        return bobTheBuilder.toString();
    }

    public void testReallocation2(String name) throws InterruptedException {
//         if (verifyNoPropertyViolation(
//                 "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
// //                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
//                 "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
//                 "+search.class = gov.nasa.jpf.search.Reset_Search",
//                 "+search_with_reset.k = 1000",
//                 // "+search_with_reset.probabilities = 0.99 0.01",
//                 // "+search_with_reset.eps = 0.5",
//                 "+numberOfThreads = 5",
//                 "+search.multiple_errors = false",
//                 "+jpf.report.console.property_violation = error",
//                 "+report.console.finished = result,statistics,error",
//                 "+report.unique_errors = true",
//                 "+report.console.property_violation = true",
//                 "+report.console.start = true"
//         )) {
        for (int count = 0; count < 100; count++) {
            OurMap<Integer, String> map = getMap(name);

            int threadCount = 5;

            int operations = 100;

            Thread[] threads = new Thread[threadCount];

            final int mul = 100;

            for (int i = 0; i < threads.length; i++) {
                final int toAdd = i * mul;
                threads[i] = new Thread(() -> {
                    for (int k = 0; k < operations; k++) {
                        map.put(k + toAdd, k + "");
                    }
                });
            }

            for (int i = 0; i < threads.length; i++) threads[i].start();

            for (int i = 0; i < threads.length; i++) threads[i].join();

            for (int i = 0; i < threads.length; i++) {
                final int toAdd = i * mul;

                for (int k = 0; k < operations; k++) {
                    assert map.containsKey(k + toAdd) : "Map failed: " + map.getClass().getName();
                }
            }
        }
    }


    public void testReallocation() throws InterruptedException {
        if (verifyNoPropertyViolation(
//                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
                "+classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 10",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 5",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"
        )) {
            // for(int j = 0; j<100000; j++){

            int threadCount = 5;

            int operations = 25;

            Thread[] threads = new Thread[threadCount];

            final int mul = 100;

            WrongStripedMap<Integer, String> map = new WrongStripedMap<>(5);


            for (int i = 0; i < threads.length; i++) {
                final int toAdd = i * mul;
                threads[i] = new Thread(() -> {
                    for (int k = 0; k < operations; k++) {
                        map.put(k + toAdd, k + "");
                    }
                });
            }

            for (int i = 0; i < threads.length; i++) threads[i].start();

            for (int i = 0; i < threads.length; i++) threads[i].join();

            for (int i = 0; i < threads.length; i++) {
                final int toAdd = i * mul;

                for (int k = 0; k < operations; k++) {
                    assert map.containsKey(k + toAdd);
                }
            }
        }
    }


    public void testOverwrite(OurMap<Integer, String> map, int threadCount, int operations) throws InterruptedException {
        if (verifyNoPropertyViolation(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 500",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = " + threadCount,
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"
        )) {
            final int range = 7;

            final int[] addedFinalBy = new int[threadCount];

            final Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threads.length; i++) {

                String name = "thread " + i;
                threads[i] = new Thread(() -> {
                    final int[] addedBy = new int[threadCount];
                    for (int j = 0; j < operations; j++) {
                        int key = j % range;

                        String oldThread = map.put(key, name);

                        if (oldThread != null) {
                            addedBy[Integer.parseInt(oldThread.substring(oldThread.length() - 1))]--;
                        }

                        addedBy[Integer.parseInt(name.substring(name.length() - 1))]++;

                    }

                    synchronized (this) {
                        for (int j = 0; j < addedBy.length; j++) {
                            addedFinalBy[j] += addedBy[j];
                        }
                    }
                });
            }

            for (int i = 0; i < threadCount; i++) {
                threads[i].start();
            }

            for (int i = 0; i < threadCount; i++) {
                threads[i].join();
            }

            int[] actualIncrements = new int[threadCount];

            map.forEach((key, value) -> actualIncrements[Integer.parseInt(value.substring(value.length() - 1))]++);

            for (int i = 0; i < threadCount; i++) {
                assert actualIncrements[i] == addedFinalBy[i];
            }
        }
    }


//     private void testMapConcurrent(final int threadCount, int perThread, int range,
//                                           final OurMap<Integer, String> map)
//             throws Exception {

//         if (verifyNoPropertyViolation(
//                 "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
// //                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
//                 "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
//                 "+search.class = gov.nasa.jpf.search.Reset_Search",
//                 "+search_with_reset.k = 500",
//                 // "+search_with_reset.probabilities = 0.99 0.01",
//                 // "+search_with_reset.eps = 0.5",
//                 "+numberOfThreads = " + threadCount,
//                 "+search.multiple_errors = false",
//                 "+jpf.report.console.property_violation = error",
//                 "+report.console.finished = result,statistics,error",
//                 "+report.unique_errors = true"
//         )) {        

//         final Thread[] threads = new Thread[threadCount];
//         final long[] keySumPerThread = new long[threadCount];
//         final int[][] addedByPerThread = new int[threadCount][threadCount];


//         for (int t = 0; t < threadCount; t++) {
//             final int myThread = t;
//             threads[t] = new Thread(() -> {

// //                System.out.println("hello");


//                 final int[] addedBy = new int[threadCount];
//                 // Sum of keys added, minus sum of keys removed, by the thread in question
//                 long keySum = 0;


//                 for (int i = 0; i < perThread; i++) {

//                     // Make them all operate on the x-range so there's potential for higher contention
//                     int key = (myThread + i) % range;

//                     final String value = String.format("%02d:%d", myThread, key);
//                     // 	  System.out.print(value + " ");
//                     if (!map.containsKey(key)) {
//                         String oldValue = map.put(key, value);
//                         if (oldValue == null) {
//                             // add to the sum if it wasnt in the map previously
//                             keySum += key;
//                             addedBy[myThread]++;

//                         } else {
//                             // if it is already there, remove old value, add new
//                             int oldThread = Integer.parseInt(oldValue.substring(0, 2));
//                             addedBy[oldThread]--;
//                             addedBy[myThread]++;
//                         }
//                     } else {

//                         String v = map.get(key);
//                         if (v != null) {
//                             final int valueKey = Integer.parseInt(v.substring(3));
//                             assert key == valueKey : "mid-run key/value mismatch";
//                         }

//                     }


//                     if ((i & 7) == 0) { // so we churn the set eery 8th operation
//                         String old = map.remove(key);
//                         if (old != null) {
//                             keySum -= key;
//                             int oldT = Integer.parseInt(old.substring(0, 2));
//                             addedBy[oldT]--;
//                         }
//                     }
//                 }
//                 keySumPerThread[myThread] = keySum;
//                 System.arraycopy(addedBy, 0, addedByPerThread[myThread], 0, threadCount);


//             }, "t" + (myThread + 1)); // names: t1,t2,... (so we can use it with uniform scheduler names);
//         }


//         // Start & join (JPF mode: no barrier needed; JVM stress: optionally add a latch like peter did)
//         for (Thread th : threads) th.start();
//         for (Thread th : threads) th.join();

//         long totalKeySum = 0L;
//         int[] totalAddedBy = new int[threadCount];
//         for (int t = 0; t < threadCount; t++) {
//             totalKeySum += keySumPerThread[t];
//             for (int u = 0; u < threadCount; u++) {
//                 totalAddedBy[u] += addedByPerThread[t][u];
//             }
//         }


//         final long[] actualKeySum = new long[1],
//                 actualSize = new long[1];

//         final int[] actualAddedBy = new int[threadCount];


//         map.forEach((k, v) -> {
//             actualKeySum[0] += k;
//             actualSize[0]++;
//             int madeBy = Integer.parseInt(v.substring(0, 2));
//             int valueKey = Integer.parseInt(v.substring(3));
//             assert k == valueKey : "end key/value mismatch";
//             actualAddedBy[madeBy]++;
//         });


//         assert actualSize[0] == map.size() : "Actual size mismatch";

//         assert actualKeySum[0] == totalKeySum : "Keysum mismatch";

//         for (int t = 0; t < threadCount; t++) {
//             assert totalAddedBy[t] == actualAddedBy[t] : "addedBy mismatch t=" + t;
//         }

//     }

// }

}
