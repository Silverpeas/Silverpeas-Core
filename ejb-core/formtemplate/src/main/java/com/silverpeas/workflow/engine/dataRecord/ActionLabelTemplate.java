package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.Field;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.Action;

/**
 * A ActionLabelTemplate builds fields giving the title of a process instance.
 */
public class ActionLabelTemplate extends ProcessInstanceFieldTemplate {
  public ActionLabelTemplate(String fieldName, Action action, String role,
      String lang) {
    super(fieldName, "text", "text", Workflow.getLabel("actionLabelFieldLabel",
        lang));

    label = action.getLabel(role, lang);
  }

  /**
   * Returns a field built from this template and filled from the given process
   * instance.
   */
  public Field getField(ProcessInstance instance) {
    return new TextRoField(label);
  }

  private final String label;
}
