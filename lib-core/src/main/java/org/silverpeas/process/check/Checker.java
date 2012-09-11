package org.silverpeas.process.check;

import java.util.Set;

import org.silverpeas.process.management.ProcessExecutionContext;
import org.silverpeas.process.session.Session;

/**
 * Interface representing services that provide data verifications.
 * All verification classes have to implement the <code>Check</code> interface (@see {@link Check})
 * and have to call <code>Checker.register</code> to be registred and used by verification services.
 * @author Yohann Chastagnier
 */
public interface Checker {

  /**
   * Register a check
   * @param check
   */
  public abstract void register(Check check);

  /**
   * Unregister a check
   * @param check
   */
  public abstract void unregister(Check check);

  /**
   * Contains the execution treatment of all verifications classes which implement
   * <code>Check</code> interface (@see {@link Check}).The parameter
   * <code>checkTypesToProcess</code> indicates types of verification that have to be done.
   * @param processExecutionProcess
   * @param session
   * @param checkTypesToProcess
   * @throws Exception
   */
  public abstract void checks(ProcessExecutionContext processExecutionProcess, Session session,
      Set<CheckType> checkTypesToProcess) throws Exception;
}