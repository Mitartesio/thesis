package org.example;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002 Ondrej Lhotak
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

/**
 * A class that numbers strings, so they can be placed in bitsets.
 *
 * @author Ondrej Lhotak
 */

public class StringNumberer extends ArrayNumberer<NumberedString> {

    private final Map<String, NumberedString> stringToNumbered = new HashMap<String, NumberedString>(1024);

    // ADDED FOR TEST SYNCHRONIZATION
//    ****************
    private static volatile CyclicBarrier startBarrier;

    public static void setStartBarrier(CyclicBarrier barrier) {
        startBarrier = barrier;
    }

    private static void awaitBarrierIfSet() {
        CyclicBarrier b = startBarrier;
        if (b != null) {
            try {
                b.await();
            } catch (Exception e) {
                // In tests we can just wrap or ignore
                throw new RuntimeException(e);
            }
        }
    }
    // ***************


    public synchronized NumberedString findOrAdd(String s) {
        // ADDED FOR TEST SYNCHRONIZATION
        // ****************
        // At this point we have ensured that barrier holds until findOrAdd has been called
        awaitBarrierIfSet();  // <-- This is the barrier signal for the other thread to be allowed to start. We ensure
        // ****************


        NumberedString ret = stringToNumbered.get(s);
        if (ret == null) {
            ret = new NumberedString(s);
            stringToNumbered.put(s, ret);
            add(ret);
        }
        return ret;
    }

    public NumberedString find(String s) {
        return stringToNumbered.get(s);
    }
}
