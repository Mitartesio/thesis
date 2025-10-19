package SearchAlgorithms;

import Listeners.Listener_Uniform_Adapts;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListener;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.RestorableVMState;

//This class should be paired with a non-deterministic listener to choose what 

public class Reset_Search extends Search {

    private int trials;
    private RestorableVMState initState;

    public Reset_Search(Config config, VM vm) {
        super(config, vm);
        trials = Integer.parseInt(config.getString("search_With_Reset.k"));
        initState = null; // This will be initialized in the beginning of search.
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
                    for (SearchListener pt : this.listeners) {
                        if (pt instanceof Listener_Uniform_Adapts) {
                            Listener_Uniform_Adapts myListener = (Listener_Uniform_Adapts) pt;
                            myListener.searchRestart();
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
