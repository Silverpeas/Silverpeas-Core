package org.silverpeas.io.check;

import org.silverpeas.io.file.FileHandler;
import org.silverpeas.io.session.IOSession;

public interface IOChecker {

  /**
   * Register a check
   * @param check
   */
  public abstract void register(IOCheck check);

  /**
   * Unregister a check
   * @param check
   */
  public abstract void unregister(IOCheck check);

  /**
   * @param session
   * @param fileHandler
   * @throws Exception
   */
  public abstract void checks(IOSession session, FileHandler fileHandler) throws Exception;

}