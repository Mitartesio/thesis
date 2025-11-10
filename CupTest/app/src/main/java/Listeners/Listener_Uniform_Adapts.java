package Listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/*
 * The {@code Listener_Uniform_Adapts} is supposed to be combined with Search_With_Reset and Listener_For_Counting in order
 * to get the URW-search through the state space of a concurrent program. This class has the responsibility of uniform
 * randomly pick the next thread to execute
 */

//This listener is supposed to be paired with Listener_For_Counting_States and Search_With_Reset to have a 
public class Listener_Uniform_Adapts extends PropertyListenerAdapter {
    // This map holds all threads and the number of opeartions found
    private Map<String, Integer> threadsAndOperations;
    private Map<String, Integer> liveThreads;
    private int numberOfThreads;
    private Random random;
    private boolean started;
    // Dummy set for operations
    private Set<String> threadsToCheck;

    //The max depth that can be reached
    private int maxDepth;

    //Current depth
    private int currentDepth;

    /*
     * @param takes a Config file
     * This method instantiates the necessary variables for the search
     */
    public Listener_Uniform_Adapts(Config config) {
        numberOfThreads = Integer.parseInt(config.getString("numberOfThreads"));
        started = false;
        threadsToCheck = new HashSet<>();
        this.liveThreads = new HashMap<>();
        random = new Random();

        if(config.hasValue("maxDepth")){
        maxDepth = config.getInt("maxDepth");
        }else{
        maxDepth = Integer.MAX_VALUE;    
        }

        System.out.println(maxDepth);

        currentDepth = 0;
    }

    /*
     * @param takes a Map<String, Integer> that is the number of operations per
     * thread
     */
    public void start(Map<String, Integer> threadsAndOperations) {
        started = true;
        this.threadsAndOperations = threadsAndOperations;
        init();
    }

    /*
     * This method is called each time the search resets and will reinstantiate the
     * liveThreads
     * map with the initial number of operations per thread.
     */
    public void init() {
//        System.out.println("Config");
        numberOfThreads = threadsAndOperations.size();
        for (String string : threadsAndOperations.keySet()) {
            liveThreads.put(string, threadsAndOperations.get(string));
        }
        currentDepth = 0;
    }

    /*
     * @param VM the current vm to the choiceGenerator point
     *
     * @param ChoiceGenerator<?> the choiceGenerator for the current state
     * This method is the main component of the class and is what bears the
     * responsibility of searching through the state space.
     * The main idea is that each thread has the number of operations left / out of
     * total number of operations left of being
     * chosen. The method will only run insofar the start method has been called and
     * hereby started set to true
     */
    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        if (started && currentDepth < maxDepth) {

            if (cg instanceof ThreadChoiceGenerator) {

                currentDepth++;

                ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;

                int sum = 0;

                Object[] chosenThreads = tcg.getAllChoices();

                // Check that all threads have been activated by the main thread.
                if (numberOfThreads > 0) {
                    for (int i = 0; i < chosenThreads.length; i++) {
                        ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                        if (ti.getName().equals("main")) {
                            tcg.select(i);
                            numberOfThreads--;
                            return;
                        }
                    }
                }

                // Find all activated threads and add them to liveThreads.
                // Sum will be equal to the number of operations per active thread
                for (Object thread : chosenThreads) {
                    ThreadInfo ti = (ThreadInfo) thread;
                    if (!ti.getName().equals("main")) {
                        threadsToCheck.add(ti.getName());
                        sum += liveThreads.get(ti.getName());
                    }
                }
                if (sum == 0) {
                    return;
                }

                // Random variable equal to the sum of all threads operations + 1
                int acc = random.nextInt(sum) + 1;

                // Pick the thread to choose next
                sum = 0;
                String chosenThread = "";
                for (String thread : threadsToCheck) {
                    sum += liveThreads.get(thread);
                    if (sum >= acc) {
                        chosenThread = thread;
                        break;
                    }
                }
                // System.out.println("Chosen thread is " + chosenThread);
                // Find the thread in chosenThreads
                for (int i = 0; i < chosenThreads.length; i++) {
                    ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                    if (ti.getName().equals(chosenThread)) {
                        int val = liveThreads.get(chosenThread);

                        // Decrement the chosen threads number of operations with 1 insofar this will
                        // not lead the threads
                        // number of operations to be 0
                        if (val > 1) {
                            liveThreads.put(chosenThread, val - 1);
                        }
                        // Select the chosen thread
                        tcg.select(i);
                        break;
                    }
                }
                threadsToCheck.clear();
            }
        }
    }

}