package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.engine.instance.HistoryStepRepository;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceImpl;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceRepository;

import javax.inject.Inject;

/**
 * Created by Nicolas on 07/06/2017.
 */
public abstract class AbstractRequest implements AbstractRequestTask.Request {

  @Inject
  private ProcessInstanceRepository repository;

  @Inject
  private HistoryStepRepository historyStepRepository;

  protected ProcessInstanceRepository getProcessInstanceRepository() {
    return repository;
  }

  protected HistoryStepRepository getHistoryStepRepository() {
    return historyStepRepository;
  }

  protected void saveError(UpdatableProcessInstance instance, GenericEvent event, Exception we) {
    WorkflowHub.getErrorManager().saveError(instance, event, we);
    setInstanceInError(instance.getInstanceId());
  }

  protected void setInstanceInError(String id) {
    UpdatableProcessInstance processInstance = repository.getById(id);

    // set errorStatus to true
    processInstance.setErrorStatus(true);

    repository.save((ProcessInstanceImpl) processInstance);
  }
}