package com.movie.ticket.booking.system.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeatSignatureUtil {

    /**
     * Generates a consistent signature for a list of seats,
     * regardless of the order they were sent in.
     * ["B9","B8"] and ["B8","B9"] both produce "B8,B9"
     */
    public static String generate(List<String> seatsSelected) {
        if (seatsSelected == null || seatsSelected.isEmpty()) {
            return "";
        }
        List<String> sorted = new ArrayList<>(seatsSelected);
        Collections.sort(sorted);
        return String.join(",", sorted);
    }
}