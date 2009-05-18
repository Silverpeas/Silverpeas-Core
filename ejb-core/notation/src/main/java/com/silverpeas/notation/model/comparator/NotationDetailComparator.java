package com.silverpeas.notation.model.comparator;

import java.util.Comparator;

import com.silverpeas.notation.model.NotationDetail;

public class NotationDetailComparator
	implements Comparator
{

	public int compare(Object o1, Object o2)
	{
		NotationDetail notation1 = (NotationDetail)o1;
		NotationDetail notation2 = (NotationDetail)o2;
		int result = (new Float(notation2.getGlobalNote())).compareTo(
			new Float(notation1.getGlobalNote()));
		if (result == 0)
		{
			result = notation2.getNotesCount() - notation1.getNotesCount();
		}
		return result;
	}

}
