package org.example;

import java.util.HashMap;
import java.util.Map;

import org.example.WrongMaps.WrongStripedMap;
import org.example.WrongMaps.WrongStripedMap2;
import org.example.WrongMaps.WrongStripedMap3;
import org.example.WrongMaps.WrongStripedMap4;
import org.example.WrongMaps.WrongStripedMap6;
import org.junit.jupiter.api.Test;

import gov.nasa.jpf.util.test.TestJPF;

public class WrongStripedMapTests extends TestJPF{
    Map<String, String> logPassedTests = new HashMap<>();
    Map<String, String> logFailedTests = new HashMap<>();
    int mapChoice;

    
    public void test() throws Exception{
    // testReallocationJPF();
            
            bigTest(); //Fucking long shit

            testGet();

            testOverwrite();

        assert logPassedTests.size() == 7 : convertMap();
    }

    private void log(boolean passed, String name){
        if(passed){
            logPassedTests.putIfAbsent("map " + mapChoice, "");
            
            logPassedTests.put("map " + mapChoice, logPassedTests.get("map " + mapChoice) + "\n" + name);
        }else{
            logFailedTests.putIfAbsent("map " + mapChoice, "");

            logFailedTests.put("map " + mapChoice, logPassedTests.get("map " + mapChoice) + "\n" + name);
        }
    }

    private String convertMap(){
        String fail = "Failed tests: \n";

        for(String failedTest : logFailedTests.keySet()){
            fail += failedTest + logFailedTests.get(failedTest) + "\n";
        }

        fail += "Passed tests: \n";
        
        if(logPassedTests.isEmpty()){
            fail += "No passed tests";
        }else{
        for(String passedTest : logPassedTests.keySet()){
            fail += passedTest + logPassedTests.get(passedTest) + "\n";
        }
    }
        
        return fail;
    }
    // @Test
    public void testReallocationWrongMap4() throws Exception{
        // System.out.println("This is the map used: " + map.getClass().getName());
        if(verifyNoPropertyViolation(
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

            // if(!logPassedTests.containsKey("map" + name)){
            //     logPassedTests.put("map" + name, "");
            // }
            // logPassedTests.put("map" + name, logPassedTests.get("map" + name) + "Reallocation passed for map: " + name + "\n");
        }
    }
    // catch(Error e){
    //     if(!logFailedTests.containsKey("map" + name)){
    //         logFailedTests.put("map" + name, "");
    //     }
    //     logFailedTests.put("map" + name, logFailedTests.get("map" + name) + "Failed to find bug in test Reallocation " + name + "failed\n");
    //     }
    // }
// }

    // @Test
    public void bigTest() throws Exception{
        if (verifyNoPropertyViolation(
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
            // for(int a = 0; a<10000; a++){

            OurMap<Integer, String> map = new WrongStripedMap<>(4);


            final int threadCount = 10;
            final int perThread = 25;
            final int range = 10;
            // OurMap<Integer, String> map;

            // if(name == 0){
            // map = new WrongStripedMap<>(5);
            // }else if(name == 1){
            //     map = new WrongStripedMap2<>(5);
            // }
            // else if(name == 2){
            //     map = new WrongStripedMap3<>(5);
            // }
            // else if(name == 3){
            //     map = new WrongStripedMap4<>(5);
            // }else if(name == 4){
            //     map = new WrongStripedMap4<>(5);
            // }
            // else{
            //     map = new WrongStripedMap6<>(5);
            // }

            
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
    @Test
    public void testOverwrite() throws InterruptedException{

       
        if (verifyNoPropertyViolation(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 1000",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 5",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"
        )) {
            final int range = 7;

            final int threadCount = 5;

            final int operations = 50;

            final int[] addedFinalBy = new int[threadCount];

            final Thread[] threads = new Thread[threadCount];

            // OurMap<Integer, String> map;

            // if(name == 0){
            // map = new WrongStripedMap<>(5);
            // }else if(name == 1){
            //     map = new WrongStripedMap2<>(5);
            // }
            // else if(name == 2){
            //     map = new WrongStripedMap3<>(5);
            // }
            // else if(name == 3){
            //     map = new WrongStripedMap4<>(5);
            // }else if(name == 4){
            //     map = new WrongStripedMap4<>(5);
            // }
            // else{
            //     map = new WrongStripedMap6<>(5);
            // }

            
            OurMap<Integer, String> map = new WrongStripedMap<>(4);



            for(int i = 0; i<threads.length; i++){

                String threadName = "thread " + i;
                threads[i] = new Thread(() -> {
                    final int[] addedBy = new int[threadCount];
                    for(int j = 0; j<operations; j++){
                        int key = j % range;

                        String oldThread = map.put(key, threadName);   

                        if(oldThread != null){
                        addedBy[Integer.parseInt(oldThread.substring(oldThread.length()-1))]--;}
                        
                        addedBy[Integer.parseInt(threadName.substring(threadName.length()-1))]++;
                    
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
            
    
    @Test
    public void testGet(){
        if (verifyNoPropertyViolation(
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

            // OurMap<Integer, String> map;

            // if(name == 0){
            // map = new WrongStripedMap<>(5);
            // }else if(name == 1){
            //     map = new WrongStripedMap2<>(5);
            // }
            // else if(name == 2){
            //     map = new WrongStripedMap3<>(5);
            // }
            // else if(name == 3){
            //     map = new WrongStripedMap4<>(5);
            // }else if(name == 4){
            //     map = new WrongStripedMap4<>(5);
            // }
            // else{
            //     map = new WrongStripedMap6<>(5);
            // }

            OurMap<Integer, String> map = new WrongStripedMap<>(4);


            int operations = 200;
            for(int i = 0; i<operations; i++){
                map.put(i, i+"");
            }

            Thread[] threads = new Thread[10];

            for(int i = 0; i<threads.length; i++){
                final int choice = i;
                threads[i] = new Thread(() -> {
                    if(choice % 2 == 0){
                    for(int k = 0; k<operations; k++){
                        assert map.get(k).equals(k+"");
                    }}else{
                        for(int k = operations; k<operations*3; k++){
                            map.put(k, k+"");
                        }
                    }
                });
            }
        }
    }
}

