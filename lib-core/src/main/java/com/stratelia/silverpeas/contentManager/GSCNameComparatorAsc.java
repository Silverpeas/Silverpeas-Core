package com.stratelia.silverpeas.contentManager;

import java.util.*;

public class GSCNameComparatorAsc implements Comparator
{
	static public GSCNameComparatorAsc comparator = new GSCNameComparatorAsc();

	public int compare(Object o1, Object o2)
	{
		GlobalSilverContent gsc1 = (GlobalSilverContent) o1;
		GlobalSilverContent gsc2 = (GlobalSilverContent) o2;

		return gsc1.getName().compareTo(gsc2.getName());
	}

	/**
	 * This comparator equals self only.
	 * 
	 * Use the shared comparator GSCNameComparator.comparator
	 * if multiples comparators are used.
	 */
	public boolean equals(Object o)
	{
		return o == this;
	}
}