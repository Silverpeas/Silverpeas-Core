package com.silverpeas.tagcloud.model.comparator;

import java.util.Comparator;

import com.silverpeas.tagcloud.model.TagCloud;

public class TagCloudByCountComparator implements Comparator {

	public int compare(Object o1, Object o2) {
		return ((TagCloud)o2).getCount() - ((TagCloud)o1).getCount();
	}

}
