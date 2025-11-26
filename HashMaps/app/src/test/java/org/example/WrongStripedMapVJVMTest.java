package org.example;

import java.util.ArrayList;
import java.util.List;

import org.example.WrongMaps.WrongStripedMap;
import org.example.WrongMaps.WrongStripedMap2;
import org.example.WrongMaps.WrongStripedMap3;
import org.example.WrongMaps.WrongStripedMap4;
import org.example.WrongMaps.WrongStripedMap5;
import org.example.WrongMaps.WrongStripedMap6;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.nasa.jpf.util.test.TestJPF;

public class WrongStripedMapVJVMTest extends TestJPF{
    WrongStripedMap<Integer, String> wrongMap1;
    WrongStripedMap2<Integer, String> wrongMap2;
    WrongStripedMap3<Integer, String> wrongMap3;
    WrongStripedMap4<Integer, String> wrongMap4;
    WrongStripedMap5<Integer, String> wrongMap5;
    WrongStripedMap6<Integer, String> wrongMap6;
    List<OurMap<Integer, String>> maps;

    @BeforeEach
    public void setup(){
        wrongMap1 = new WrongStripedMap<>(5);
        wrongMap2 = new WrongStripedMap2<>(5);
        wrongMap3 = new WrongStripedMap3<>(5);
        wrongMap4 = new WrongStripedMap4<>(5);
        wrongMap5 = new WrongStripedMap5<>(5);
        wrongMap6 = new WrongStripedMap6<>(5);

        maps = new ArrayList<>();

        maps.add(wrongMap1);
    }

    // @Test
    //     public void runAll() throws Exception{
    //         for(OurMap<Integer, String> map : maps){
    //             try{
    //                 testReallocation(map, 5, 50);
    //             }catch(AssertionError e){
    //                 System.out.println(e.getMessage());
    //             }
                
    //             // try{
    //             //     testMapConcurrent(5, 50, 7, map);
    //             // }catch(AssertionError e){
    //             //     System.out.println(e.getMessage());
    //             // }

    //             try{
    //                 testOverwrite(map, 5, 50);
    //             }catch(AssertionError e){
    //                 System.out.println(e.getMessage());
    //             }
                
                
    //         }
    //     }

    
    public void testReallocation(OurMap<Integer, String> map, int threadCount, int operations) throws InterruptedException{
        for(int count = 0; count <500; count++) {
            Thread[] threads = new Thread[threadCount];

            final int mul = 100;
            

            for(int i = 0; i<threads.length; i++){
                final int toAdd = i * mul;
                threads[i] = new Thread(() -> {
                    for(int k = 0; k<operations; k++){
                        map.put(k+toAdd, k + "");
                    }
                });
            }

            for(int i = 0; i<threads.length; i++)threads[i].start();

            for(int i = 0; i<threads.length; i++)threads[i].join();

            for(int i = 0; i<threads.length; i++){
                final int toAdd = i * mul;

                for(int k = 0; k<operations; k++){
                    assert map.containsKey(k+toAdd);
                }
            }
        }
    }

    
    public void testOverwrite(OurMap<Integer, String> map, int threadCount, int operations) throws InterruptedException{
        for(int count = 0; count < 500; count++) {
            final int range = 7;

            final int[] addedFinalBy = new int[threadCount];

            final Thread[] threads = new Thread[threadCount];
            for(int i = 0; i<threads.length; i++){

                String name = "thread " + i;
                threads[i] = new Thread(() -> {
                    final int[] addedBy = new int[threadCount];
                    for(int j = 0; j<operations; j++){
                        int key = j % range;

                        String oldThread = map.put(key, name);   

                        if(oldThread != null){
                        addedBy[Integer.parseInt(oldThread.substring(oldThread.length()-1))]--;}
                        
                        addedBy[Integer.parseInt(name.substring(name.length()-1))]++;
                    
                    }

                    synchronized(this){
                        for(int j = 0; j<addedBy.length; j++){
                            addedFinalBy[j] += addedBy[j];
                        }
                    }
                });
            }

            for(int i = 0; i<threadCount; i++){
                threads[i].start();
            }

            for(int i = 0; i<threadCount; i++){
                threads[i].join();
            }

            int[] actualIncrements = new int[threadCount];
            map.forEach((key, value) -> actualIncrements[Integer.parseInt(value.substring(value.length()-1))]++);

            for(int i = 0; i<threadCount; i++){
                assert actualIncrements[i] == addedFinalBy[i];
            }
        }        
    }

    
//     private void testMapConcurrent(final int threadCount, int perThread, int range,
//                                           final OurMap<Integer, String> map)
//             throws Exception {


//         for(int count = 0; count < 500; count++){
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
//             try{
//             int madeBy = Integer.parseInt(v.substring(0, 2));}
//             catch(Exception e){
//                 throw new Error("The value of the integer is: " + v);
//             }
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
