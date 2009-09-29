package com.silverpeas.form;

import com.stratelia.webactiv.util.exception.*;

/**
 * Thrown by the form components.
 */
public class FormException extends SilverpeasException {
  /**
   * Returns the module name (as known by SilverTrace).
   */
  public String getModule() {
    return "form";
  }

  /**
   * Set the caller and the error message
   */
  public FormException(String caller, String message) {
    super(caller, SilverpeasException.ERROR, message);
  }

  /**
   * Set the caller, the error message and the nested exception.
   */
  public FormException(String caller, String message, Exception nestedException) {
    super(caller, SilverpeasException.ERROR, message, nestedException);
  }

  /**
   * Set the caller, infos and the error message
   */
  public FormException(String caller, String message, String infos) {
    super(caller, SilverpeasException.ERROR, message, infos);
  }

  /**
   * Set the caller, the error message, infos and the nested exception.
   */
  public FormException(String caller, String message, String infos,
      Exception nestedException) {
    super(caller, SilverpeasException.ERROR, message, infos, nestedException);
  }

  /**
   * Set the caller, the level and the error message.
   * 
   * Used only by the FatalFormException
   */
  protected FormException(String caller, int level, String message) {
    super(caller, level, message);
  }

  /**
   * Set the caller, the level, the error message and the nested exception.
   * 
   * Used only by the FatalFormException
   */
  protected FormException(String caller, int level, String message,
      Exception nestedException) {
    super(caller, level, message, nestedException);
  }

  /**
   * Set the caller, the level, the error message and infos.
   * 
   * Used only by the FatalFormException
   */
  protected FormException(String caller, int level, String message, String infos) {
    super(caller, level, message);
  }

  /**
   * Set the caller, the level, the error message, infos and the nested
   * exception.
   * 
   * Used only by the FatalFormException
   */
  protected FormException(String caller, int level, String message,
      String infos, Exception nestedException) {
    super(caller, level, message, nestedException);
  }
}
