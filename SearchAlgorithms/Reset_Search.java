package SearchAlgorithms;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
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

            System.out.println("I am running!!!");

            if (initState == null) {
                initState = vm.getRestorableState();
            }

            System.out.println("The depth is: " + trials);

            if (isEndState() || !forward()) {
                if (trials <= 0) {
                    done = true;
                    System.out.println("I am done for good!");
                    break;
                } else {
                    System.out.println("I am done once");
                    trials--;
                    vm.restoreState(initState);
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
