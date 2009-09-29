package com.silverpeas.form;

import com.stratelia.webactiv.util.exception.*;

/**
 * Thrown when a fatal error occured in a form component.
 */
public class FormFatalException extends FormException {
  /**
   * Set the caller and the error message
   */
  public FormFatalException(String caller, String message) {
    super(caller, SilverpeasException.ERROR, message);
  }

  /**
   * Set the caller, the error message and the nested exception.
   */
  public FormFatalException(String caller, String message,
      Exception nestedException) {
    super(caller, SilverpeasException.ERROR, message, nestedException);
  }

  /**
   * Set the caller, infos and the error message
   */
  public FormFatalException(String caller, String message, String infos) {
    super(caller, SilverpeasException.ERROR, message, infos);
  }

  /**
   * Set the caller, the error message, infos and the nested exception.
   */
  public FormFatalException(String caller, String message, String infos,
      Exception nestedException) {
    super(caller, SilverpeasException.ERROR, message, infos, nestedException);
  }
}
