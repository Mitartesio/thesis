package SearchAlgorithms;

import Listeners.Listener_For_Counting_States;
import Listeners.Listener_Uniform_Adapts;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.RestorableVMState;

//This class should be paired with a non-deterministic listener to choose what 

public class Reset_Search extends Search {

    private int trials;
    private RestorableVMState initState;
    private boolean searching;

    public Reset_Search(Config config, VM vm) {
        super(config, vm);
        trials = Integer.parseInt(config.getString("search_With_Reset.k"));
        initState = null; // This will be initialized in the beginning of search.
        searching = false;
    }

    @Override
    public void search() {

        while (!done) {

            // System.out.println("I am running!!!");

            if (initState == null) {
                // System.out.println("Found state");
                initState = vm.getRestorableState();
            }

            // System.out.println("The depth is: " + trials);

            // System.out.println("depth: " + depth);

            if (isEndState() || !forward()) {
                System.out.println("Found end state!");
                if (trials <= 0) {
                    done = true;
                    // System.out.println("I am done for good!");
                    break;
                } else {
                    // System.out.println("Restart state");
                    trials--;
                    vm.restoreState(initState);
                    if (searching) {
                        for (int i = 0; i < this.listeners.length; i++) {
                            if (this.listeners[i] instanceof Listener_Uniform_Adapts) {
                                Listener_Uniform_Adapts searchListener = (Listener_Uniform_Adapts) this.listeners[i];
                                searchListener.init();
                            }
                        }
                    } else {
                        for (int i = 0; i < this.listeners.length; i++) {
                            if (this.listeners[i] instanceof Listener_For_Counting_States) {
                                Listener_For_Counting_States countListener = (Listener_For_Counting_States) this.listeners[i];
                                if (countListener.finished()) {
                                    searching = true;
                                    for (int j = 0; j < this.listeners.length; j++) {
                                        if (this.listeners[j] instanceof Listener_Uniform_Adapts) {
                                            Listener_Uniform_Adapts searchListener = (Listener_Uniform_Adapts) this.listeners[j];
                                            searchListener.start(countListener.getThreadsAndOperations());
                                        }
                                    }
                                } else {
                                    countListener.init();
                                }
                            }
                        }
                    }
                }
            }

            if (forward()) {
                notifyStateAdvanced();

                if (hasPropertyTermination()) {
                    break;
                } else {
                    notifyStateProcessed();
                }
            }

        }

    }

    @Override
    public boolean requestBacktrack() {
        doBacktrack = true;

        return true;
    }

}
