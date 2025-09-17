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
    private boolean allThreadsStarted = false;

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

        if (!allThreadsStarted) {
            if (cg instanceof ThreadChoiceGenerator) {
                ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;

                Object[] chosenThreads = tcg.getAllChoices();

                int count = 0;

                int main = -1;

                for (int i = 0; i < chosenThreads.length; i++) {
                    ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                    if (ti.getName().equals("main")) {
                        main = i;
                    } else if (threads.containsKey(ti.getName())) {
                        count++;
                    }
                }
                if (count == threads.size()) {
                    allThreadsStarted = true;
                } else {
                    tcg.select(main);
                    // System.out.println("Picking main");
                }
            }
        }

        else if (allThreadsStarted) {
            if (cg instanceof ThreadChoiceGenerator) {

                ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;

                Object[] chosenThreads = tcg.getAllChoices();

                int actualSum = 0;
                Map<String, Integer> myMap = new LinkedHashMap<>();

                for (int i = 0; i < chosenThreads.length; i++) {
                    ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                    // System.out.println(ti.getName());
                    if (threads.containsKey(ti.getName())) {
                        // System.out.println("Found thread: " + ti.getName());
                        myMap.put(ti.getName(), threads.get(ti.getName()));
                        actualSum += myMap.get(ti.getName());
                    }
                    // else if (ti.getName().equals("main")) {
                    // System.out.println("Found main and execution main");
                    // tcg.select(i);
                    // }
                }

                // System.out.println("The actual sum is: " + actualSum);

                if (actualSum > 0) {
                    int choice = random.nextInt(actualSum) + 1;
                    int cumulative = 0;
                    String nextThread = "";
                    for (String name : myMap.keySet()) {
                        cumulative += myMap.get(name);
                        if (cumulative >= choice) {
                            nextThread = name;
                            if (threads.get(name) > 1) {
                                threads.put(name, threads.get(name) - 1);
                                sum--;
                            }
                            // System.out.println(nextThread);
                            break;

                        }
                    }

                    // System.out.println("The instruction taken is: " + cg.getInsn() + " by: " +
                    // vm.getCurrentThread());

                    for (int i = 0; i < chosenThreads.length; i++) {
                        if (chosenThreads[i] instanceof ThreadInfo) {
                            ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                            if (ti.getName().equals(nextThread)) {
                                // System.out.println(ti);
                                tcg.select(i);
                            }
                        }
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
