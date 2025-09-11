package Listeners;

import java.util.ArrayList;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class Listener_Force extends PropertyListenerAdapter {
    // git push --force origin main style

    private boolean t1Done = false;
    private boolean t2Done = false;
    private int countT1;
    private int countT2;
    private ArrayList<String> threads;
    private String nextThread;
    private int pick;
    private boolean setBackTrack = false;

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        ThreadInfo[] aliveThreads = vm.getLiveThreads();

        // if (setBackTrack) {
        // Search thisSearch = vm.getSearch();
        // if (thisSearch.getDepth() > 0) {
        // thisSearch.requestBacktrack();
        // } else {
        // System.out.println("Sucessfully backTracked!");
        // setBackTrack = false;
        // }
        // }

        System.out.println("Doing a run with the thread: " + nextThread);

        if (threads == null) {
            threads = new ArrayList<>();
            threads.add("t1");
            threads.add("t2");
            nextThread = threads.get(pick);
        }

        if (cg instanceof ThreadChoiceGenerator) {
            ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;
            if (!t2Done && !t1Done) {
                int totalChoices = tcg.getTotalNumberOfChoices();
                System.out.println(nextThread);

                for (int i = 0; i < totalChoices; i++) {
                    ThreadInfo thread = tcg.getChoice(i);
                    if (thread.getName().equals(nextThread)) {
                        if (nextThread.equals("t1")) {
                            countT1++;
                        } else {
                            countT2++;
                        }
                        tcg.select(i);
                    }
                }
            }
        }
    }

    @Override
    public void stateAdvanced(Search search) {
        // if (search.isEndState()) {
        // ThreadInfo[] liveThreads = search.getVM().getLiveThreads();
        // boolean found = false;
        // for (ThreadInfo thread : liveThreads) {
        // if (thread.getName().equals(nextThread)) {
        // found = true;
        // break;
        // }
        // }
        // if (!found) {
        if (search.isEndState()) {
            pick++;
            System.out.println("Thread.size = " + threads.size());
            // System.out.println("The next thread is: " + threads.get(pick));
            if (pick >= threads.size()) {
                System.out.println("Sucessfully found all threads, happy happy happy");
                search.terminate();
            } else {
                nextThread = threads.get(pick);
                if (pick >= 2) {
                    System.out.println("somethings wrong");
                }
                System.out.println("BackTracking once");
                search.requestBacktrack();
            }
        }
    }

    // }

    @Override
    public void searchFinished(Search search) {
        System.out.println("The search is done!");
        System.out.println("Number of operations t1: " + countT1);
        System.out.println("Number of operations t2: " + countT2);
    }

}
