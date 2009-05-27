package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.Field;
import com.silverpeas.form.FormException;
import com.silverpeas.form.fieldType.TextFieldImpl;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.Item;
import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.api.user.UserInfo;
import com.silverpeas.workflow.api.user.UserSettings;

/**
 * A UserInfoTemplate builds fields giving information about user
 */
public class UserInfoTemplate extends ProcessInstanceFieldTemplate
{
	private String role;
	private String lang;
	private Item item;

	 public UserInfoTemplate(String fieldName,
							 Item item,
							 String role,
							 String lang)
	{
		super(fieldName, item.getType(), item.getType(), item.getLabel(role, lang));
		this.role = role;
		this.lang = lang;
		this.item = item;
	}

	/**
	 * Returns a field built from this template
	 * and filled from the given process instance.
	 */
	public Field getField(ProcessInstance instance)
		throws FormException
	{
		Field field = null;
		
		try
		{
			String shortFieldName = getFieldName();
			int index = shortFieldName.lastIndexOf(".actor.");

			String actionName = shortFieldName.substring(7, index);
			HistoryStep step = instance.getMostRecentStep(actionName);
			if (step != null)
			{
				shortFieldName = shortFieldName.substring(index+7);

				if (item.getMapTo() != null && item.getMapTo().length()!=0)
				{
					User user = Workflow.getUserManager().getUser(step.getUser().getUserId());

					field = new TextFieldImpl();
					if (user!=null)
						field.setStringValue(user.getInfo(item.getMapTo()));
				}

				else
				{
					UserSettings settings = Workflow.getUserManager().getUserSettings(step.getUser().getUserId(), instance.getModelId());
					UserInfo info = settings.getUserInfo(shortFieldName);

					field = instance.getProcessModel().getUserInfos().toRecordTemplate(role, lang, false).getEmptyRecord().getField(shortFieldName);
					if (field!=null && info!=null)
						field.setStringValue(info.getValue());
				}
			}
		
			return field;
		}
		catch (WorkflowException e)
		{
			throw new FormException("UserInfoTemplate",
                                 "form.EXP_UNKNOWN_FIELD",
                                 getFieldName());
		}
	}
}
