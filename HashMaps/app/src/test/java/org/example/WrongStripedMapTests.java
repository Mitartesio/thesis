package org.example;

import org.example.WrongMaps.WrongStripedMap2;
import org.example.WrongMaps.WrongStripedMap4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.nasa.jpf.util.test.TestJPF;

public class WrongStripedMapTests extends TestJPF{
    WrongStripedMap2<Integer, String> map;
    WrongStripedMap4<Integer, String> map2;

    @BeforeEach
    public void setup(){
        map = new WrongStripedMap2<>(10);
        map2 = new WrongStripedMap4<>(10);
    }

//     @Test
//     public void testOverwrite() throws InterruptedException{
//         if (verifyNoPropertyViolation(
//                 "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
// //                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
//                 "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
//                 "+search.class = gov.nasa.jpf.search.Reset_Search",
//                 "+search_with_reset.k = 10000",
//                 // "+search_with_reset.probabilities = 0.99 0.01",
//                 // "+search_with_reset.eps = 0.5",
//                 "+numberOfThreads = 10",
//                 "+search.multiple_errors = false",
//                 "+jpf.report.console.property_violation = error",
//                 "+report.console.finished = result,statistics,error",
//                 "+report.unique_errors = true"
//         )) {
//             // for(int k = 0; k<10000; k++){
//             map = new WrongStripedMap2<>(10);

//             final int threadCount = 10;

//             final int opsPerThread = 25;

//             final int range = 7;

//             final int[] addedFinalBy = new int[threadCount];

//             final Thread[] threads = new Thread[threadCount];
//             for(int i = 0; i<threads.length; i++){

//                 String name = "thread " + i;
//                 threads[i] = new Thread(() -> {
//                     final int[] addedBy = new int[threadCount];
//                     for(int j = 0; j<opsPerThread; j++){
//                         int key = j % range;

//                         String oldThread = map.put(key, name);   

//                         if(oldThread != null){
//                         addedBy[Integer.parseInt(oldThread.substring(oldThread.length()-1))]--;}
                        
//                         addedBy[Integer.parseInt(name.substring(name.length()-1))]++;
                    
//                     }

//                     synchronized(this){
//                         for(int j = 0; j<addedBy.length; j++){
//                             addedFinalBy[j] += addedBy[j];
//                         }
//                     }
//                 });
//             }

//             for(int i = 0; i<threadCount; i++){
//                 threads[i].start();
//             }

//             for(int i = 0; i<threadCount; i++){
//                 threads[i].join();
//             }

//             int[] actualIncrements = new int[threadCount];
//             map.forEach((key, value) -> actualIncrements[Integer.parseInt(value.substring(value.length()-1))]++);

//             for(int i = 0; i<threadCount; i++){
//                 assert actualIncrements[i] == addedFinalBy[i];
//             }
//         }        
//     }

//     @Test 
//     public void testGet() throws InterruptedException{
//         if (verifyNoPropertyViolation(
//                 "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
// //                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
//                 "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
//                 "+search.class = gov.nasa.jpf.search.Reset_Search",
//                 "+search_with_reset.k = 10000",
//                 // "+search_with_reset.probabilities = 0.99 0.01",
//                 // "+search_with_reset.eps = 0.5",
//                 "+depth = 100",
//                 "+numberOfThreads = 10",
//                 "+search.multiple_errors = false",
//                 "+jpf.report.console.property_violation = error",
//                 "+report.console.finished = result,statistics,error",
//                 "+report.unique_errors = true"
//         )) {

//             map = new WrongStripedMap2<>(10);

//             // for(int k = 0; k<10000; k++){
//             Thread[] threads = new Thread[10];

//             for(int i = 0; i<threads.length; i++){
//                 final int mul = i*100;
//                 threads[i] = new Thread(() -> {
//                     for(int j = 0; j<100; j++){
//                         map.put(mul+j, "" + mul);
//                     }
//                 });
//             }

//             for(int i = 0; i<threads.length; i++)threads[i].start();

//             for(int i = 0; i<threads.length; i++)threads[i].join();

//             for(int i = 0; i<threads.length; i++){
//                 final int mul = i * 100;

//                 for(int j = 0; j<100; j++){
//                     assert map.get(mul+j) != null;
//                 }
//             }
//         }
//     }

    @Test
    public void testReallocation() throws InterruptedException{
        if (verifyNoPropertyViolation(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 10000",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 10",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"
        )) {
            // for(int j = 0; j<100000; j++){
            Thread[] threads = new Thread[10];
            map2 = new WrongStripedMap4<>(10);

            for(int i = 0; i<threads.length; i++){
                final int mul = i * 100;
                threads[i] = new Thread(() -> {
                    for(int k = 0; k<100; k++){
                        map2.put(k+mul, k + "");
                    }
                });
            }

            for(int i = 0; i<threads.length; i++)threads[i].start();

            for(int i = 0; i<threads.length; i++)threads[i].join();

            for(int i = 0; i<threads.length; i++){
                final int mul = i * 100;

                for(int k = 0; k<100; k++){
                    assert map2.containsKey(k+mul);
                }
            }
        }
    }
}
