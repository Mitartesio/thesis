package Listeners;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class Listener_For_Counting_States extends PropertyListenerAdapter {
    private int numberOfThreads;
    private Map<String, Integer> threadsAndOperations;
    private String nextThread;
    private boolean found;

    public Listener_For_Counting_States(Config config) {
        this.numberOfThreads = Integer.parseInt(config.getString("numberOfThreads"));
        this.threadsAndOperations = new HashMap<>();
        found = false;
    }

    public void init() {
        System.out.println("Number of threads: " + numberOfThreads);
        if (numberOfThreads > 0) {
            nextThread = null;
            numberOfThreads--;
        }
    }

    public boolean finished() {
        if (numberOfThreads <= 0) {
            found = true;
            for (String string : threadsAndOperations.keySet()) {
                System.out.println("Thread: " + string + " has this many ops: " + threadsAndOperations.get(string));
            }
        }
        return numberOfThreads <= 0;
    }

    public Map<String, Integer> getThreadsAndOperations() {
        return threadsAndOperations;
    }

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        if (!found) {
            System.out.println("Running");
            if (cg instanceof ThreadChoiceGenerator) {
                ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;
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

                    // Insofar
                    if (nextThread == null && !ti.getName().equals("main")
                            && threadsAndOperations.get(ti.getName()) == 0) {
                        nextThread = ti.getName();
                    }

                    if (ti.getName().equals(nextThread)) {
                        // System.out.println(ti.getPC().toString());
                        choice = i;
                        threadsAndOperations.put(nextThread, threadsAndOperations.get(nextThread) + 1);
                    }
                }
                // System.out.println("This many ops: " + threadsAndOperations.get(nextThread));
                // System.out.println("Choice is: " + choice);
                if (choice >= 0) {
                    tcg.select(choice);
                }
            }
        }
    }

}