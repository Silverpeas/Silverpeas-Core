package com.stratelia.webactiv.searchEngine.model;

/**
 * Thrown when a search query is ill formed.
 */
public class ParseException extends SearchEngineException {
  /**
   * Set the caller and the nested exception.
   */
  public ParseException(String caller, Exception nestedException) {
    super(caller, "searchEngine.EXP_PARSE_EXCEPTION", nestedException);
  }
}
