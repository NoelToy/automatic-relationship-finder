package io.github.arf.lib.util;

import java.util.HashSet;
import java.util.Set;

public class JaccardIndex {
    public static <T>double getSimilarity(Set<T> a, Set<T> b){
       Set<T> union =   new HashSet<>(a);
       union.addAll(b);

       Set<T> intersection = new HashSet<>(a);
       intersection.retainAll(b);

       double unionSize = union.size();
       double intersectionSize = intersection.size();

       return intersectionSize/unionSize;
    }

    public static <T>double getDistance(Set<T> a, Set<T> b){
       Set<T> union =   new HashSet<>(a);
       union.addAll(b);

       Set<T> intersection = new HashSet<>(a);
       intersection.retainAll(b);

       double unionSize = union.size();
       double intersectionSize = intersection.size();

       return 1-(intersectionSize/unionSize);
    }
}
