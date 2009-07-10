package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.Item;

/**
 * A ItemTemplate builds fields giving the title of a process instance.
 */
public class ItemTemplate extends ProcessInstanceFieldTemplate
{
	public ItemTemplate(String fieldName, Item item, String role, String lang)
	{
		super(
			fieldName,
			item.getType(),
			item.getType(),
			item.getLabel(role, lang));
	}

	/**
	 * Returns a field built from this template
		* and filled from the given process instance.
		*/
	public Field getField(ProcessInstance instance) throws FormException
	{
		try
		{
			String shortFieldName = getFieldName();

			if (shortFieldName.indexOf("instance.") != -1
				&& shortFieldName.substring(0, 9).equals("instance."))
				shortFieldName =
					shortFieldName.substring(9, shortFieldName.length());

			else if (
				shortFieldName.indexOf("folder.") != -1
					&& shortFieldName.substring(0, 7).equals("folder."))
				shortFieldName =
					shortFieldName.substring(7, shortFieldName.length());

			Field returnedField = instance.getField(shortFieldName);

			if (returnedField != null)
				return returnedField;
			else
				throw new FormException(
				"ItemTemplate",
				"form.EXP_UNKNOWN_FIELD",
				getFieldName());		
		}
		catch (WorkflowException e)
		{
			throw new FormException(
				"ItemTemplate",
				"form.EXP_UNKNOWN_FIELD",
				getFieldName());
		}
	}
}
