package Listeners;

import gov.nasa.jpf.vm.ThreadInfo;
import java.util.HashMap;
import java.util.Map;

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

    public Listener_Uniform_Adapts(Config config) {
        this.config = config;
        this.threadsAndOperations = new HashMap<>();
        this.initializedAll = false;
        nextThread = null;
        this.numberOfThreads = config.getInt("Listener_Uniform_Adapts.numberOfThreads");
        // this.numberOfThreads =
        // Integer.parseInt(config.getString("Listener_Uniform_Adapts.numberOfThreads").strip());
        System.out.println("Number of threads is: " + numberOfThreads);
    }

    // The purpose of initConfig is that when an end state has been reached it will
    // reinitialized the listener for a new iteration
    private void initConfig() {
        System.out.println("Config");
        if (initializedAll) {
            this.liveThreads = new HashMap<>();
            for (String string : threadsAndOperations.keySet()) {
                liveThreads.put(string, threadsAndOperations.get(string));
            }
        } else {
            numberOfThreads--;
            nextThread = null;
            if (numberOfThreads <= 0) {
                initializedAll = true;
                System.out.println("Succesfully initialized!!!");
                for (String string : threadsAndOperations.keySet()) {
                    System.out.println("Thread: " + string + " number of ops: " + threadsAndOperations.get(string));
                }
                initConfig();
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
        if (vm.getSearch().isEndState()) {
            System.out.println("Found the endState here for some reason");
        }
        if (initializedAll) {
            System.out.println("I should probably do something else here");
        } else {
            if (cg instanceof ThreadChoiceGenerator) {
                ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;

                Object[] chosenThreads = tcg.getAllChoices();

                int choice = -1;

                // System.out.println("NextThread is: " + nextThread);

                if (vm.getSearch().isEndState() || !vm.getSearch().hasNextState()) {
                    System.out.println("End State here");
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
                        System.out.println(ti.getPC().toString());
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

    // StateAdvanced has two responsibilities:
    // 1. If !initializedAll then it will backTrack insofar it reaches an end state
    // and switch to another thread to explore
    // 2. If initializedAll it will restart the listener with InitConfig
    @Override
    public void stateAdvanced(Search search) {
        // if (search.isEndState()) {
        // System.out.println("Found end state");
        // if (!initializedAll) {
        // System.out.println("Minus");
        // numberOfThreads--;
        // System.out.println("End point with the thread: " + nextThread);
        // if (numberOfThreads <= 0) {
        // initializedAll = true;
        // System.out.println("I am initialized");

        // for (String string : threadsAndOperations.keySet()) {
        // System.out.println("This is the key: " + string + " this is the number of
        // ops: "
        // + threadsAndOperations.get(string));
        // }
        // }
        // }
        // initConfig();
        // }
    }

    public void searchRestart() {
        initConfig();
    }

}
