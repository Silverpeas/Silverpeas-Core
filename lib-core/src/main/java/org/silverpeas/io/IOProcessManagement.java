package org.silverpeas.io;

import org.silverpeas.io.process.AbstractIOProcess;

public interface IOProcessManagement {

  /**
   * All IO process have to call this method
   * @param ioProcess
   */
  public abstract <R> R execute(AbstractIOProcess<R> process) throws Exception;
}