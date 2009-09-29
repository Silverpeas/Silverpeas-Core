package com.stratelia.webactiv.searchEngine.model;

import com.stratelia.webactiv.util.exception.*;

/**
 * Thrown by the searchEngine
 */
public class SearchEngineException extends SilverpeasException {
  /**
   * Returns the module name (as known by SilverTrace).
   */
  public String getModule() {
    return "searchEngine";
  }

  /**
   * Set the caller and the error message
   */
  public SearchEngineException(String caller, String message) {
    super(caller, SilverpeasException.ERROR, message);
  }

  /**
   * Set the caller, the error message and the nested exception.
   */
  public SearchEngineException(String caller, String message,
      Exception nestedException) {
    super(caller, SilverpeasException.ERROR, message, nestedException);
  }
}
