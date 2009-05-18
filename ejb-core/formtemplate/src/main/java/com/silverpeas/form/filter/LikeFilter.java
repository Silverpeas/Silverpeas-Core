package com.silverpeas.form.filter;

import com.silverpeas.form.Field;

/**
 * A LikeFilter test if a given field is less then a reference field.
 *
 * @see Field
 * @see FieldDisplayer
 */
public class LikeFilter implements FieldFilter
{
   /**
	 * A Like Filter is built upon a reference field
	 */
	public LikeFilter(Field reference)
	{
	   String simplifiedRef = reference.getValue("");
		if (simplifiedRef != null)
		{
		   simplifiedRef = simplifiedRef.trim().toLowerCase();
			if (simplifiedRef.equals("")) simplifiedRef = null;
		}

	   this.reference = simplifiedRef;
	}

   /**
	 * Returns true if the given field contains the reference field.
	 */
	public boolean match(Field tested)
	{
	   if (reference == null) return true;

		String normalized = tested.getValue("");
		if (normalized == null) return false;
		else normalized = normalized.trim().toLowerCase();

		return normalized.indexOf(reference) != -1;
	}

	/**
	 * The reference value against which tests will be performed.
	 */
	private final String reference;
}
