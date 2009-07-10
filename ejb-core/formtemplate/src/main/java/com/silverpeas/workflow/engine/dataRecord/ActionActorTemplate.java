package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.Field;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.Action;

/**
 * A ActionActorTemplate builds fields giving the title of a process instance.
 */
public class ActionActorTemplate extends ProcessInstanceFieldTemplate
{
  public ActionActorTemplate(String fieldName,
                       Action action,
                       String role,
						     String lang)
  {
     super(fieldName,"text","text",Workflow.getLabel("actionActorFieldLabel", lang));
	  actionName = action.getName();
  }

  /**
   * Returns a field built from this template
	* and filled from the given process instance.
	*/
  public Field getField(ProcessInstance instance)
  {
     HistoryStep step = instance.getMostRecentStep(actionName);
	  if (step != null)
	  {
	      try
			{
	         return new TextRoField(step.getUser().getFullName());
			}
			catch (WorkflowException e)
			{
	         return new TextRoField(null);
			}
	  }
	  else
	  {
	      return new TextRoField(null);
	  }
  }

  private final String actionName;
}
