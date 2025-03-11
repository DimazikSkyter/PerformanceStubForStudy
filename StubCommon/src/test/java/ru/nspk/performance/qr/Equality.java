package ru.nspk.performance.qr;

import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

public class Equality {

    public static void main(String[] args) {
        HashSet<Number> hSet = new HashSet<>();
        TreeSet<Integer> tSet = new TreeSet<>();
        for (int i = 0; i < 100; i++) {
            hSet.add(i);
            tSet.add(i);
        }

        System.out.println("Result: " + equals(hSet, tSet));
    }


    private static boolean equals(Collection<? extends Number> coll, Collection<? extends Integer> coll2) {
        if ((coll == null && coll2 == null) || (coll == null && coll2 != null) || (coll != null && coll2 == null)) {
            return false;
        }

        if (coll.size() != coll2.size()) {
            return false;
        }

        return coll.stream().allMatch(v -> coll2.contains(v));
    }
}
