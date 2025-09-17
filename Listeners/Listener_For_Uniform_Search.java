package Listeners;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.Config;

public class Listener_For_Uniform_Search extends PropertyListenerAdapter {
    private Map<String, Integer> threads;
    private int sum;
    private Random random;

    public Listener_For_Uniform_Search(Config config) {
        threads = new LinkedHashMap<>();
        String[] threadNames = config.getString("uniformSearch.Thread_names").split(" ");
        String[] threadOps = config.getString("uniformSearch.Thread_operations").split(" ");
        random = new Random();

        if (threadNames.length != threadOps.length) {
            throw new IllegalArgumentException(
                    "The number of threads needs to be equal to the number of thread operations");
        }

        try {
            for (int i = 0; i < threadNames.length; i++) {
                threads.put(threadNames[i], Integer.parseInt(threadOps[i]));
                sum += Integer.parseInt(threadOps[i]);
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The number of threads operations is not put in correctly");
        }
    }

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        if (cg instanceof ThreadChoiceGenerator) {
            int choice = random.nextInt(sum) + 1;
            int cumulative = 0;
            String nextThread = "";
            for (String name : threads.keySet()) {
                cumulative += threads.get(name);
                if (cumulative >= choice) {
                    nextThread = name;
                    if (threads.get(name) > 1) {
                        threads.put(name, threads.get(name) - 1);
                        sum--;
                    }
                    break;

                }
            }

            ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;

            Object[] chosenThreads = tcg.getAllChoices();

            for (int i = 0; i < chosenThreads.length; i++) {
                if (chosenThreads[i] instanceof ThreadInfo) {
                    ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                    if (ti.getName().equals(nextThread)) {
                        tcg.select(i);
                    }
                }
            }
        }
    }

    @Override
    public void stateAdvanced(Search search) {
        if (search.isEndState()) {
            search.terminate();
        }
    }

}
