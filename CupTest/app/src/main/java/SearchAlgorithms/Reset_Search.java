package SearchAlgorithms;

import Listeners.Listener_For_Counting_States;
import Listeners.Listener_Uniform_Adapts;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;
import utils.Ccp;
import gov.nasa.jpf.vm.RestorableVMState;

/*
 *  The {@code Reset_Search} is supposed to be combined with Search_With_Reset and Listener_For_Counting in order
 * to get the URW-search through the state space of a concurrent program. This class is responsible for searching through the
 * state space and notify the listeners when an end-state has been reached
 */
public class Reset_Search extends Search {

    private int trials;
    private RestorableVMState initState;
    private boolean searching;
    private int originalk;
    private boolean violationFound;

    /*
     * @param Config is supposed to hold a value "search_With_Reset.k" that is the
     * number of trials that should be run
     *
     * @param Vm the virtual machine tied to the search
     * The constructor will initialize all necessary fields
     */
    public Reset_Search(Config config, VM vm) {
        super(config, vm);
        //The user can give a specific k value
        try {
            trials = Integer.parseInt(config.getString("search_with_reset.k"));
            originalk = trials;
            //If they do not provide trial they will have to provide epsilon of double and probabilities
        } catch (Exception e) {
            Ccp calc = new Ccp();

            //Get the string[] of probabilies from the file and convert to a double array
            String[] probabilities = config.getString("search_with_reset.probabilities").split(" ");
            double[] probabilitiesDoubles = new double[probabilities.length];
            for (int i = 0; i < probabilities.length; i++) {
                probabilitiesDoubles[i] = Double.parseDouble(probabilities[i]);
            }
            double eps = config.getDouble("search_with_reset.eps");
            this.trials = calc.calcCcp(probabilitiesDoubles.length, probabilitiesDoubles, eps);
            originalk = this.trials;
        }
        System.out.println("k: " + originalk);
        initState = null; // This will be initialized in the beginning of search.
        searching = false;
    }

    /*
     * This method is responsible for searching through the state space k times. It
     * will catch the initial state at the first
     * possible state. When an endstate has been reached it will notify the
     * listeners. Insofar the Listener_For_Counting_States is
     * done it will get the map with threads and operations and instruct
     * Listener_Uniform_Adapts to start searching.
     */
    @Override
    public void search() {

        notifySearchStarted();
        int count = 0;
        while (!done) {
            // Initialize initial state if not initialized yet
            if (initState == null) {
                initState = vm.getRestorableState();
            }

            // If the current state is an end state or forward() returns false
            if (isEndState() || !forward()) {
                //System.out.println(count);
                count++;
                // If trials is 0 stop the search else restart from initial state
                if (trials <= 0) {
                    done = true;
                    break;
                } else {
                    // decrement trials
                    trials--;

                    // restore initial state
                    vm.restoreState(initState);

                    // If searching call init on Listener_Uniform_Adapts
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
                                // Check if Listener_For_Counting_States is done
                                if (countListener.finished()) {
                                    // Set searching to true
                                    searching = true;
                                    for (int j = 0; j < this.listeners.length; j++) {
                                        if (this.listeners[j] instanceof Listener_Uniform_Adapts) {
                                            // Initialize Listener_Uniform_Adapts with threads and number of operations
                                            Listener_Uniform_Adapts searchListener = (Listener_Uniform_Adapts) this.listeners[j];
                                            searchListener.start(countListener.getThreadsAndOperations());
                                        }
                                    }
                                } else {
                                    // Initialize Listener_For_Counting_States for its next run
                                    countListener.init();
                                }
                            }
                        }
                    }
                }
            }

            if (forward()) {
                notifyStateAdvanced();

                // If an error has occured
                if (currentError != null) {
                    notifyPropertyViolated();
                    violationFound = true;
                    System.out.println(currentError.getDetails());

                    if (hasPropertyTermination()) {
                        break;
                    }
                    // for search.multiple_errors we go on and treat this as a new state
                    // but hasPropertyTermination() will issue a backtrack request
                }

                // If memory limit has been reached
                if (!checkStateSpaceLimit()) {
                    notifySearchConstraintHit("memory limit reached: " + minFreeMemory);
                    // can't go on, we exhausted our memory
                    break;
                }

                if (hasPropertyTermination()) {
                    break;
                } else {
                    notifyStateProcessed();
                }
            }

        }
        System.out.println("violated " + violationFound);
    }

    @Override
    public boolean requestBacktrack() {
        doBacktrack = true;

        return true;
    }

}
