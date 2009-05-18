package com.stratelia.silverpeas.contentManager;

import java.util.*;

public class GSCCreationDateComparatorAsc implements Comparator
{
	static public GSCCreationDateComparatorAsc comparator = new GSCCreationDateComparatorAsc();

	/**
	 * A matching index entry is greater another if his score is higher.
	 * 
	 * This result is reversed as we want a descending sort.
	 */
	public int compare(Object o1, Object o2)
	{
		GlobalSilverContent gsc1 = (GlobalSilverContent) o1;
		GlobalSilverContent gsc2 = (GlobalSilverContent) o2;
		
		int compareResult = gsc1.getCreationDate().compareTo(gsc2.getCreationDate());
		if (compareResult == 0) {
			//both objects have been created on the same date
			compareResult = gsc1.getId().compareTo(gsc2.getId());
		}

		return compareResult;
	}

	/**
	 * This comparator equals self only.
	 * 
	 * Use the shared comparator GSCDateComparatorAsc.comparator
	 * if multiples comparators are used.
	 */
	public boolean equals(Object o)
	{
		return o == this;
	}
}