package org.example;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.jupiter.api.*;


public class StripedMapTest extends TestJPF {
    StripedMap<Integer, String> map;


    @BeforeEach
    public void setup() {

        map = new StripedMap<>(10);

    }

    @Test
    public void testReallocationJPF() throws Exception{
        if(verifyNoPropertyViolation(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 10",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 5",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true",
                "+log.warning=gov.nasa.jpf",
                "+log.info=gov.nasa.jpf"
        )) {

            map = new StripedMap<>(5);
            // testReallocation(map);

            Thread[] threads = new Thread[5];
            

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

    @Test
    public void testOverwrite() throws InterruptedException{
        if (verifyNoPropertyViolation(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
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
            final int range = 7;

            final int threadCount = 5;

            final int operations = 50;

            final int[] addedFinalBy = new int[threadCount];

            final Thread[] threads = new Thread[threadCount];

            map = new StripedMap<>(5);


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
                "+search_with_reset.k = 10",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 10",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"
        )) {

            map = new StripedMap<>(5);

            for(int i = 0; i<1000; i++){
                map.put(i, i+"");
            }

            Thread[] threads = new Thread[10];

            for(int j = 0; j<threads.length; j++){
                int decision = j;
                threads[j] = new Thread(() -> {
                    if(decision % 2 == 0){
                        final int mul = (decision + 1) * 1000;

                        for(int k = mul; k<mul + 500; k++){
                            map.put(k, k+"");
                        }
                    }else{
                        for(int k = 0; k<1000; k++){
                            assert map.containsKey(k);
                        }
                    }
                });
            }
        }
    
}
    }

