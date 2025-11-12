package org.example;
// Sequential and concurrent test of hash map implementations
// sestoft@itu.dk * 2014-10-21, 2025-05-27

// Based on 2014 TestStripedMapTestSolution.java

// Modified 2025 by FREK, ANMV, TOLH for JPF concurrency testing

// Run with assertions enabled for sequential functional test of the maps:
//   java -ea HashMapTest


public class HashMapTest {
    public static void main(String[] args) throws Exception {
//        SystemInfo();
//        testAllMapsSequential();
        testAllMapsConcurrent();
    }

    private static void testAllMapsSequential() {
//        testMapSequential(new SynchronizedMap<Integer, String>());
//        testMapSequential(new StripedMap<Integer, String>(5));
//        testMapSequential(new StripedWriteMap<Integer, String>(5));
//        testMapSequential(new StripedWriteMapPadded<Integer, String>(5));
//        testMapSequential(new StripedLevelWriteMap<Integer, String>(5));
//        testMapSequential(new HashTrieMap<Integer, String>());
//        testMapSequential(new WrapConcurrentHashMap<Integer, String>());
    }

    private static void testAllMapsConcurrent() throws Exception {
        final int lockCount = 7, threadCount = 4;
        final int perThread = 1_000;
        final int range = 64;
//        testMapConcurrent(threadCount, perThread, range,
//                new SynchronizedMap<Integer, String>());
//        testMapConcurrent(threadCount, perThread, range,
//                new StripedMap<Integer, String>(lockCount));
//        testMapConcurrent(threadCount, perThread, range,
//                new StripedMapPadded<Integer, String>(lockCount));
        testMapConcurrent(threadCount, perThread, range,
                new StripedWriteMap<Integer, String>(lockCount));
//        testMapConcurrent(threadCount, perThread, range,
//                new StripedWriteMapPadded<Integer, String>(lockCount));
//        testMapConcurrent(threadCount, perThread, range,
//                new StripedLevelWriteMap<Integer, String>(lockCount));
//        testMapConcurrent(threadCount, perThread, range,
//                new HashTrieMap<Integer, String>());
//        testMapConcurrent(threadCount, perThread, range,
//                new WrapConcurrentHashMap<Integer, String>());
    }

    private static void testMapSequential(final OurMap<Integer, String> map) {
        System.out.printf("%nSequential test: %s%n", map.getClass());
        assert map.size() == 0;
        assert !map.containsKey(117);
        assert !map.containsKey(-2);
        assert map.get(117) == null;
        assert map.put(117, "A") == null;
        assert map.containsKey(117);
        assert map.get(117).equals("A");
        assert map.put(17, "B") == null;
        assert map.size() == 2;
        assert map.containsKey(17);
        assert map.get(117).equals("A");
        assert map.get(17).equals("B");
        assert map.put(117, "C").equals("A");
        assert map.containsKey(117);
        assert map.get(117).equals("C");
        assert map.size() == 2;
        map.forEach((k, v) -> System.out.printf("%10d maps to %s%n", k, v));
        assert map.remove(117).equals("C");
        assert !map.containsKey(117);
        assert map.get(117) == null;
        assert map.size() == 1;
        // assert map.putIfAbsent(17, "D").equals("B");
        assert map.get(17).equals("B");
        assert map.size() == 1;
        assert map.containsKey(17);
        // assert map.putIfAbsent(217, "E") == null;
        assert map.put(217, "E") == null;  // Was putIfAbsent
        assert map.get(217).equals("E");
        assert map.size() == 2;
        assert map.containsKey(217);
        // assert map.putIfAbsent(34, "F") == null;
        assert map.put(34, "F") == null;   // Was putIfAbsent
        map.forEach((k, v) -> System.out.printf("%10d maps to %s%n", k, v));
        assert map.size() == 3;
        assert map.get(17).equals("B") && map.containsKey(17);
        assert map.get(217).equals("E") && map.containsKey(217);
        assert map.get(34).equals("F") && map.containsKey(34);
        map.forEach((k, v) -> System.out.printf("%10d maps to %s%n", k, v));
        assert map.size() == 3;
        assert map.get(17).equals("B") && map.containsKey(17);
        assert map.get(217).equals("E") && map.containsKey(217);
        assert map.get(34).equals("F") && map.containsKey(34);
        map.forEach((k, v) -> System.out.printf("%10d maps to %s%n", k, v));
    }

    private static void testMapConcurrent(final int threadCount, int perThread, int range,
                                          final OurMap<Integer, String> map)
            throws Exception {
        System.out.printf("%nConcurrent test: %s%n", map.getClass());

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

    public static void SystemInfo() {
        System.out.printf("# OS:   %s; %s; %s%n",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
        System.out.printf("# JVM:  %s; %s%n",
                System.getProperty("java.vendor"),
                System.getProperty("java.version"));
        // This line works only on MS Windows:
        System.out.printf("# CPU:  %s%n", System.getenv("PROCESSOR_IDENTIFIER"));
        java.util.Date now = new java.util.Date();
        System.out.printf("# Date: %s%n",
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(now));
    }
}
