package Listeners;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

    boolean first = false;
    boolean second = false;

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
                    return;
                }
            }
        }

        else if (allThreadsStarted) {
            if (cg instanceof ThreadChoiceGenerator) {

                ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;

                Object[] chosenThreads = tcg.getAllChoices();

                if (!first) {
                    for (int i = 0; i < chosenThreads.length; i++) {
                        // System.out.println("Chose t1");
                        ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                        System.out.println(ti.getName());
                        if (ti.getName().equals("t1")) {
                            threads.put("t1", threads.get("t1") - 1);
                            first = true;
                            tcg.select(i);
                            return;
                        }
                    }
                }

                if (!second) {
                    for (int i = 0; i < chosenThreads.length; i++) {
                        ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                        if (ti.getName().equals("t2")) {
                            // System.out.println("Chose t2");
                            second = true;
                            threads.put("t2", threads.get("t2") - 1);
                            tcg.select(i);
                            return;
                        }
                    }
                }

                for (int i = 0; i < chosenThreads.length; i++) {
                    ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                    // System.out.println(ti.getPC().toString());
                    if (ti.getPC().toString().contains("endAtomic")) {
                        // System.out.println("Thread: " + ti.getName());
                        // System.out.println(ti.getPC().toString());
                        // System.out.println("old value for: " + ti.getName() + " is: " +
                        // threads.get(ti.getName()));
                        // System.out.println(threads.get(ti.getName()));
                        threads.put(ti.getName(), threads.get(ti.getName()) - 1);

                        // System.out.println("new value for: " + ti.getName() + " is: " +
                        // threads.get(ti.getName()));
                        // System.out.println("The thread now has this many operations left: " +
                        // threads.get(ti.getName()));
                        System.out.println("I chose the thread: " + ti.getName() + "With this op: " +
                                ti.getPC());
                        tcg.select(i);
                        return;
                    }
                }

                int actualSum = 0;
                Map<String, Integer> myMap = new LinkedHashMap<>();

                for (int i = 0; i < chosenThreads.length; i++) {
                    ThreadInfo ti = (ThreadInfo) chosenThreads[i];
                    if (threads.containsKey(ti.getName())) {
                        myMap.put(ti.getName(), threads.get(ti.getName()));
                        actualSum += myMap.get(ti.getName());
                    }
                }

                // System.out.println("t1: " + threads.get("t1"));
                // System.out.println("t2: " + threads.get("t2"));

                // System.out.println("Actual sum: " + actualSum);

                if (actualSum > 0) {
                    int choice = random.nextInt(actualSum) + 1;
                    // System.out.println("choice is: " + choice);
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
                                System.out.println("picked: " + ti.getName());
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
