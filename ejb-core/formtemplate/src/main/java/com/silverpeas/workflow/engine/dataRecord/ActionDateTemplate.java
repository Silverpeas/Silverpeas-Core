package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.Field;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.Action;

/**
 * A ActionDateTemplate builds fields giving the title of a process instance.
 */
public class ActionDateTemplate extends ProcessInstanceFieldTemplate {
  public ActionDateTemplate(String fieldName, Action action, String role,
      String lang) {
    super(fieldName, "text", "text", Workflow.getLabel("actionDateFieldLabel",
        lang));
    actionName = action.getName();
  }

  /**
   * Returns a field built from this template and filled from the given process
   * instance.
   */
  public Field getField(ProcessInstance instance) {
    HistoryStep step = instance.getMostRecentStep(actionName);
    if (step != null) {
      return new DateRoField(step.getActionDate());
    } else {
      return new DateRoField(null);
    }
  }

  private final String actionName;
}
