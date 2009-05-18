package com.stratelia.webactiv.searchEngine.model;

import java.util.Comparator;

/**
 * Comparator used to sort the results set.
 */
public class ScoreComparator implements Comparator
{
	static public ScoreComparator comparator = new ScoreComparator();

	/**
	 * A matching index entry is greater another if his score is higher.
	 * 
	 * This result is reversed as we want a descending sort.
	 */
	public int compare(Object o1, Object o2)
	{
		MatchingIndexEntry r1 = (MatchingIndexEntry) o1;
		MatchingIndexEntry r2 = (MatchingIndexEntry) o2;

		if (r1.getScore() < r2.getScore())
		{
			return 1;
		}
		else if (r1.getScore() == r2.getScore())
		{
			return 0;
		}
		else
		{
			return -1;
		}
	}

	/**
	 * This comparator equals self only.
	 * 
	 * Use the shared comparator ScoreComparator.comparator
	 * if multiples comparators are used.
	 */
	public boolean equals(Object o)
	{
		return o == this;
	}

}