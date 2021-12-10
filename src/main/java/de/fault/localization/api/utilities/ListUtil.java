package de.fault.localization.api.utilities;

import java.util.List;

/**
 * contains methods for dealing with lists
 */
public class ListUtil {

    private ListUtil(){

    }

    /**
     * @param listA - the first list
     * @param listB - the second list
     * @param <K>   - the generic type
     * @return true if the lists are identical to their intersection
     */
    public static <K> boolean isUnorderedEqual(final List<K> listA, final List<K> listB) {
        return listA.containsAll(listB) && listB.containsAll(listA);
    }
}
