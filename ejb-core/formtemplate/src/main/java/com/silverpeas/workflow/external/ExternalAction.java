package com.silverpeas.workflow.external;

import com.silverpeas.workflow.api.event.GenericEvent;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.Trigger;

public interface ExternalAction {

  public void setProcessInstance(ProcessInstance process);

  public void setEvent(GenericEvent event);

  public void setTrigger(Trigger trigger);

  public void execute();

}
