package com.stratelia.webactiv.util.statistic.control;

import java.util.Comparator;
import java.util.Date;

import com.stratelia.webactiv.util.statistic.model.HistoryByUser;

public class LastAccessComparatorDesc implements Comparator {
  static public LastAccessComparatorDesc comparator = new LastAccessComparatorDesc();

  public int compare(Object o1, Object o2) {
    HistoryByUser historyUser1 = (HistoryByUser) o1;
    HistoryByUser historyUser2 = (HistoryByUser) o2;

    Date dateUser1 = historyUser1.getLastAccess();
    Date dateUser2 = historyUser2.getLastAccess();

    int compareResult = 0;

    if (dateUser1 != null && dateUser2 != null) {
      compareResult = dateUser1.compareTo(dateUser2);
    } else {
      if (dateUser1 == null && dateUser2 != null) {
        compareResult = -1;
      }
      if (dateUser1 != null && dateUser2 == null) {
        compareResult = 1;
      }
    }

    return 0 - compareResult;
  }

  public boolean equals(Object o) {
    return o == this;
  }
}
