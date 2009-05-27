package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.Field;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.model.State;

/**
 * A StateTemplate builds fields giving the current state of a process instance.
 */
public class StateTemplate extends ProcessInstanceFieldTemplate
{
  public StateTemplate(String fieldName,
                       ProcessModel processModel,
                       String role,
						     String lang)
  {
     super(fieldName,"text","text",Workflow.getLabel("stateFieldLabel", lang));
	  this.processModel = processModel;
	  this.role = role;
	  this.lang = lang;
  }

  /**
   * Returns a field built from this template
	* and filled from the given process instance.
	*/
  public Field getField(ProcessInstance instance)
  {
	  StringBuffer stateLabels = new StringBuffer();
     String stateNames[] = instance.getActiveStates();
	  State state = null;
	  for (int i=0 ; i<stateNames.length ; i++)
	  {
	     state = processModel.getState(stateNames[i]);
		  if (state != null)
		  {
		     if (stateLabels.length()>0) stateLabels.append(" - ");
		     stateLabels.append(state.getLabel(role, lang));
		  }
	  }
	  return new TextRoField(stateLabels.toString());
  }

  private final ProcessModel processModel;
  private final String role;
  private final String lang;
}
