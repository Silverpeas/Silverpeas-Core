package com.stratelia.silverpeas.contentManager;

import java.util.*;

public class GSCNameComparatorDesc implements Comparator {
  static public GSCNameComparatorDesc comparator = new GSCNameComparatorDesc();

  /**
   * A matching index entry is greater another if his score is higher.
   * 
   * This result is reversed as we want a descending sort.
   */
  public int compare(Object o1, Object o2) {
    GlobalSilverContent gsc1 = (GlobalSilverContent) o1;
    GlobalSilverContent gsc2 = (GlobalSilverContent) o2;

    return 0 - gsc1.getName().compareTo(gsc2.getName());
  }

  /**
   * This comparator equals self only.
   * 
   * Use the shared comparator GSCNameComparator.comparator if multiples
   * comparators are used.
   */
  public boolean equals(Object o) {
    return o == this;
  }
}