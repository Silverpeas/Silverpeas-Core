package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.workflow.api.WorkflowException;

/**
 * Each request must define a method called process which will process the request with a given
 * WorkflowEngine.
 * Created by Nicolas on 07/06/2017.
 */
public interface Request {

  void process() throws WorkflowException;

}
