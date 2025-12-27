package gov.nasa.jpf.listener;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Reset_Search;
import gov.nasa.jpf.search.Search;

public class Listener_For_Testing extends PropertyListenerAdapter{
    Map<String, Integer> threadOperations;
    
    public Listener_For_Testing(Config config){
        threadOperations = new HashMap<>();
            String[] ops = config.getString("operations").split(" ");
            String[] threads = config.getString("threads").split(" ");
            
            if(ops.length != threads.length){
                throw new IllegalArgumentException("Number of operations needs to match number of threads");
            }

            for(int i = 0; i<ops.length; i++){
                threadOperations.put(threads[i], Integer.parseInt(ops[i]));
            }
        
    }

    @Override
    public void searchFinished(Search search){
        System.out.println("Finished!!!! and number of ops: " + threadOperations.size());
        if(search instanceof Reset_Search){
            Reset_Search sut = (Reset_Search) search;

            for(String thread : threadOperations.keySet()){
                System.out.println("Number of ops1: " + sut.getThreadsAndOperations().get(thread) + " number2: " + threadOperations.get(thread));
                if(sut.getThreadsAndOperations().get(thread) == null 
                || !sut.getThreadsAndOperations().get(thread).equals(threadOperations.get(thread))){
                    throw new Error("Number of operations do not match each other");
                }
            }
        }
    }
}
