package com.silverpeas.tagcloud.model.comparator;

import java.util.Comparator;

import com.silverpeas.tagcloud.model.TagCloud;

public class TagCloudByNameComparator implements Comparator {

	public int compare(Object o1, Object o2) {
		return ((TagCloud)o1).getTag().compareTo(((TagCloud)o2).getTag());
	}

}
