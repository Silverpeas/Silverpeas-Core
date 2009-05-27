package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.Field;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.ProcessModel;

/**
 * A TitleTemplate builds fields giving the title of a process instance.
 */
public class TitleTemplate extends ProcessInstanceFieldTemplate
{
  public TitleTemplate(String fieldName,
                       ProcessModel processModel,
                       String role,
						     String lang)
  {
     super(fieldName,"text","text",Workflow.getLabel("titleFieldLabel", lang));
	  this.role = role;
	  this.lang = lang;
  }

  /**
   * Returns a field built from this template
	* and filled from the given process instance.
	*/
  public Field getField(ProcessInstance instance)
  {
	  return new TextRoField(instance.getTitle(role, lang));
  }

  private final String role;
  private final String lang;
}
