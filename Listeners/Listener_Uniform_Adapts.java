package Listeners;

import gov.nasa.jpf.vm.ThreadInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.VM;

//The purpose of this listener is to twofold:
//1. It is meant to guide the search at each choicegenerator by 
//a uniform random choice between the available threads (excluding the main thread)

//2.Purpose 1 entails that this listener must dynamically keep track of number of operations per thread

public class Listener_Uniform_Adapts extends PropertyListenerAdapter {
    private Config config;
    // This map holds all threads and the number of opeartions found
    private Map<String, Integer> threadsAndOperations;
    private Map<String, Integer> liveThreads;
    private boolean initializedAll;
    private String nextThread;
    private int numberOfThreads;
    private Random random;

    public Listener_Uniform_Adapts(Config config) {
        this.config = config;
        this.threadsAndOperations = new HashMap<>();
        this.initializedAll = false;
        nextThread = null;
        this.numberOfThreads = config.getInt("Listener_Uniform_Adapts.numberOfThreads");
        // this.numberOfThreads =
        // Integer.parseInt(config.getString("Listener_Uniform_Adapts.numberOfThreads").strip());
        System.out.println("Number of threads is: " + numberOfThreads);
        this.random = new Random();
    }

    // This method will called from the listener it is attached to every time
    // it reaches an end point or is unable to continue search
    public void searchRestart() {
        System.out.println("Config");
        if (initializedAll) {
            for (String string : threadsAndOperations.keySet()) {
                liveThreads.put(string, threadsAndOperations.get(string));
            }

        } else {
            numberOfThreads--;
            nextThread = null;
            if (numberOfThreads <= 0) {
                initializedAll = true;
                // Debugging
                System.out.println("Succesfully initialized!!!");
                System.out.println("THis is the size: " + threadsAndOperations.size());
                for (String string : threadsAndOperations.keySet()) {
                    System.out.println("Thread: " + string + " number of ops: " + threadsAndOperations.get(string));
                }
                // Debugging
                this.liveThreads = new HashMap<>();
                for (String string : threadsAndOperations.keySet()) {
                    liveThreads.put(string, threadsAndOperations.get(string));
                }
            }

        }
    }

    // choiceGeneratorAdvanced has two responisibilites:
    // 1. If !initializedAll it will keep making sure that all availbable thread are
    // put into threadsAndOperations if they
    // have not already been put in there.

    // 2.If initializedAll it will uniform randomly choose a thread to pick next. It
    // will also keep
    // track of whether the current path is longer for a thread and increment in
    // threadsAndOperations for the next iteration
    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        if (cg instanceof ThreadChoiceGenerator) {
            ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;
            if (initializedAll) {
                int sum = 0;

                for (String thread : liveThreads.keySet()) {
                    sum += liveThreads.get(thread);
                }

                int acc = random.nextInt(sum) + 1;

                sum = 0;
                String chosenThread = "";
                for (String thread : liveThreads.keySet()) {
                    sum += liveThreads.get(thread);
                    if (sum >= acc) {
                        chosenThread = thread;
                        break;
                    }
                }

                System.out.println("This is the chosen thread: " + chosenThread + " this is the acc: " + acc);

                Object[] chosenThreads = tcg.getAllChoices();
                if (numberOfThreads <= 0 || chosenThreads.length == 1) {
                    for (int i = 0; i < chosenThreads.length; i++) {
                        ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                        if (ti.getName().equals("main")) {
                            tcg.select(i);
                            return;
                        }
                    }
                }

                for (int i = 0; i < chosenThreads.length; i++) {
                    ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                    if (ti.getName().equals(chosenThread)) {
                        int val = liveThreads.get(chosenThread);
                        if (val > 1) {
                            liveThreads.put(chosenThread, val - 1);
                        }
                        tcg.select(i);
                        break;
                    }
                }
            } else {
                if (cg instanceof ThreadChoiceGenerator) {

                    Object[] chosenThreads = tcg.getAllChoices();

                    int choice = -1;

                    // System.out.println("NextThread is: " + nextThread);

                    if (vm.getSearch().isEndState() || !vm.getSearch().hasNextState()) {
                        // System.out.println("End State here");
                    }

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

}
