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

public class Listener_Uniform_Adapts extends PropertyListenerAdapter {
    // This map holds all threads and the number of opeartions found
    private Map<String, Integer> threadsAndOperations;
    private Map<String, Integer> liveThreads;
    private int numberOfThreads;
    private Random random;
    private boolean started;
    // Dummy set for operations
    private Set<String> threadsToCheck;

    public Listener_Uniform_Adapts(Config config) {
        numberOfThreads = Integer.parseInt(config.getString("numberOfThreads"));
        started = false;
        threadsToCheck = new HashSet<>();
        this.liveThreads = new HashMap<>();
        random = new Random();
    }

    public void start(Map<String, Integer> threadsAndOperations) {
        started = true;
        this.threadsAndOperations = threadsAndOperations;
        init();
    }

    // This method must be called at the beginning of every new run to reset the
    // number of operations per thread
    public void init() {
        System.out.println("Config");
        numberOfThreads = threadsAndOperations.size();
        for (String string : threadsAndOperations.keySet()) {
            liveThreads.put(string, threadsAndOperations.get(string));
        }
    }

    //
    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        if (started) {
            System.out.println("Starting!!!");

            if (cg instanceof ThreadChoiceGenerator) {

                ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;

                int sum = 0;

                Object[] chosenThreads = tcg.getAllChoices();

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
                int acc = random.nextInt(sum) + 1;

                sum = 0;
                String chosenThread = "";
                for (String thread : threadsToCheck) {
                    sum += liveThreads.get(thread);
                    if (sum >= acc) {
                        chosenThread = thread;
                        break;
                    }
                }

                // System.out.println("This is the chosen thread: " + chosenThread + " this is
                // the acc: " + acc);

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
                threadsToCheck.clear();
            }
        }
    }
}
