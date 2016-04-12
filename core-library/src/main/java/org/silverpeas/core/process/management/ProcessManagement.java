package org.silverpeas.core.process.management;

import org.silverpeas.core.process.SilverpeasProcess;
import org.silverpeas.core.process.util.ProcessList;

/**
 * Interface representing services that provide execution of one or several processes (tasks in
 * other words) in one time.
 * User data and component instance id have to be passed at each call.
 * The different steps of execution of the chaining tasks are the followings :
 * <ul>
 * <li>Step 1 : initializing a session and a file handler both shared by all processes</li>
 * <li>Step 2 : executing the 'process' method of each process of the chain</li>
 * <li>Step 3 : applying global validations on output of the step 1</li>
 * <li>Step 4 : executing the 'onSuccessful' method of each process of the chain</li>
 * <li>Step 5 : committing on file system all files handled (if any) by processes</li>
 * </ul>
 * If an exception is thrown during step 2, 3 or 4, then 'onFailure" method is called on each
 * Silverpeas process whose 'process' method has been called and finally all manipulated files (if
 * any) are not committed on file system .
 * @author Yohann Chastagnier
 */
public interface ProcessManagement {

  /**
   * Executing one process with an execution context
   * @param process
   * @param processExecutionContext
   */
  public abstract <C extends ProcessExecutionContext> void execute(SilverpeasProcess<C> process,
      C processExecutionContext) throws Exception;

  /**
   * Executing several processes with a shared execution context
   * @param processes
   * @param processExecutionContext
   */
  public abstract <C extends ProcessExecutionContext> void execute(ProcessList<C> processes,
      C processExecutionContext) throws Exception;
}