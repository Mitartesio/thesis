package gov.nasa.jpf.listener;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;

import java.util.*;

/**
 * Collects all distinct thread scheduling sequences (interleavings)
 * for a run, and also records which ones hit an assertion violation.
 */
public class InterleavingCollector extends PropertyListenerAdapter {

    private final Set<String> allSchedules = new HashSet<>();
    private final Set<String> buggySchedules = new HashSet<>();

    // Stack of prefixes representing the current schedule along the search tree
    private final Deque<StringBuilder> prefixStack = new ArrayDeque<>();

    @Override
    public void searchStarted(Search search) {
        prefixStack.clear();
        prefixStack.push(new StringBuilder());
    }

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        if (cg instanceof ThreadChoiceGenerator) {
            ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;
            ThreadInfo ti = tcg.getNextChoice();

            StringBuilder prev = prefixStack.peek();
            StringBuilder next = new StringBuilder(prev);
            if (next.length() > 0) {
                next.append(',');
            }
            // Use thread name ("t1"/"t2") or id, up to you
            next.append(ti.getName());

            prefixStack.push(next);
        }
    }

    @Override
    public void stateBacktracked(Search search) {
        // Undo last scheduling step when search backtracks
        if (prefixStack.size() > 1) {
            prefixStack.pop();
        }
    }

    @Override
    public void stateAdvanced(Search search) {
        if (search.isEndState()) {
            String schedule = prefixStack.peek().toString();
            allSchedules.add(schedule);
        }
    }

    @Override
    public void propertyViolated(Search search) {
        // Assertion or other property violation on this path
        String schedule = prefixStack.peek().toString();
        buggySchedules.add(schedule);
    }

    @Override
    public void searchFinished(Search search) {
        System.out.println("=== InterleavingCollector report ===");
        System.out.println("Total distinct schedules (interleavings): " + allSchedules.size());
        System.out.println("Schedules that hit a property violation: " + buggySchedules.size());

        for (String s : buggySchedules) {
            System.out.println("BUGGY SCHEDULE: " + s);
        }
    }
}
