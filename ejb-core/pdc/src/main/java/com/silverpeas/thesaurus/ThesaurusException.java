package com.silverpeas.thesaurus;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ThesaurusException extends SilverpeasException {

  // constructors
  /**
   * Constructor which calls the super constructor
   * 
   * @param callingClass
   *          (String) the name of the module which catchs the Exception
   * @param errorLevel
   *          (int) the level error of the exception
   * @param message
   *          (String) the level of the exception label
   * @param extraParams
   *          (String) the generic exception message
   * @param nested
   *          (Exception) the exception catched
   */
  public ThesaurusException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public ThesaurusException(String callingClass, int errorLevel,
      String message, String extraParams) {
    this(callingClass, errorLevel, message, extraParams, null);
  }

  public ThesaurusException(String callingClass, int errorLevel,
      String message, Exception nested) {
    this(callingClass, errorLevel, message, "", nested);
  }

  public ThesaurusException(String callingClass, int errorLevel, String message) {
    this(callingClass, errorLevel, message, "", null);
  }

  //
  // public methods
  //

  /**
   * Returns the name of this jobPeas
   * 
   * @return the name of this module
   */
  public String getModule() {
    return "Thesaurus";
  }

}
