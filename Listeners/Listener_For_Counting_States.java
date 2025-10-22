package Listeners;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/*
 * The {@code Listener_For_Counting_States} is supposed to be combined with Search_With_Reset and Listener_Uniform_Adapts in order
 * to get the URW-search through the state space of a concurrent program. This class is responsible for count the number of
 * operations per thread. It will store these in a hashMap and be terminated by calling finished and by numberOfThreads
 * being equal to 0
 */
public class Listener_For_Counting_States extends PropertyListenerAdapter {
    private int numberOfThreads;
    private Map<String, Integer> threadsAndOperations;
    private String nextThread;
    private boolean found;

    /*
     * @param Config file that is supposed to hold a field with the number of
     * threads for the current program.
     * The constructor will initialize all necessary fields for the search.
     */
    public Listener_For_Counting_States(Config config) {
        this.numberOfThreads = Integer.parseInt(config.getString("numberOfThreads"));
        this.threadsAndOperations = new HashMap<>();
        found = false;
    }

    /*
     * This method will be called each time the search resets. All the necessary
     * fields will be reset as long as
     * numberOfThreads is greater than 0
     */
    public void init() {
        System.out.println("Number of threads: " + numberOfThreads);
        if (numberOfThreads > 0) {
            nextThread = null;
            numberOfThreads--;
        }
    }

    /*
     * This method is responsible for keeping track of whether the all threads have
     * been searched through. When numberOfThreads
     * reaches 0 it will set found to true and hereby stop the search. The method
     * will return whether the listener is done or not.
     */
    public boolean finished() {
        if (numberOfThreads <= 0) {
            found = true;
            for (String string : threadsAndOperations.keySet()) {
                System.out.println("Thread: " + string + " has this many ops: " + threadsAndOperations.get(string));
            }
        }
        return numberOfThreads <= 0;
    }

    /*
     * Method for returning the map containing threads and operations
     */
    public Map<String, Integer> getThreadsAndOperations() {
        return threadsAndOperations;
    }

    /*
     * @param VM the virtual machine tied to the current state
     * 
     * @param ChoiceGenerator<?> the choiceGenerator tied to the current state
     * This method is responsible for picking a thread that insofar nextThread ==
     * null. The method will pick nextThread
     * whenever this is possible and keep count every time it is picked. The method
     * will not do anything when found is true
     */
    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        // Only search insofar found == false
        if (!found) {
            if (cg instanceof ThreadChoiceGenerator) {
                ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;

                // Threads available at the current state
                Object[] chosenThreads = tcg.getAllChoices();

                int choice = -1;

                // Loop through all available threads and put them into threadsAndOperations.
                // This way we will keep track of all live threads throughout the program
                for (int i = 0; i < chosenThreads.length; i++) {
                    ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                    // System.out.println(ti.toString());

                    if (!ti.getName().equals("main")) {
                        threadsAndOperations.putIfAbsent(ti.getName(), 0);
                    }

                    // Find the nextThread that is not the main thread and has not been picked
                    // before
                    if (nextThread == null && !ti.getName().equals("main")
                            && threadsAndOperations.get(ti.getName()) == 0) {
                        nextThread = ti.getName();
                    }

                    // Pick ti if it is equal to nextThread
                    if (ti.getName().equals(nextThread)) {
                        choice = i;
                        threadsAndOperations.put(nextThread, threadsAndOperations.get(nextThread) + 1);
                    }
                }
                // Insofar the correct thread is found pick it
                if (choice >= 0) {
                    tcg.select(choice);
                }
            }
        }
    }

}