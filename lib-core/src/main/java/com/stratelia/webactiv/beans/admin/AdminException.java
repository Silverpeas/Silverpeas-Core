/*
 * @author Norbert CHAIX
 * @version 1.0
 */

package com.stratelia.webactiv.beans.admin;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class AdminException extends SilverpeasException {
  private boolean m_bAlreadyPrinted = false;

  public AdminException(boolean bAlreadyPrinted) {
    super("NoClass", SilverpeasException.ERROR, "");
    m_bAlreadyPrinted = bAlreadyPrinted;
  }

  public AdminException(String sMessage, boolean bAlreadyPrinted) {
    super("NoClass", SilverpeasException.ERROR, sMessage);
    m_bAlreadyPrinted = bAlreadyPrinted;
  }

  public AdminException(Exception e, boolean bAlreadyPrinted) {
    super("NoClass", SilverpeasException.ERROR, "", e);
    m_bAlreadyPrinted = bAlreadyPrinted;
  }

  public boolean isAlreadyPrinted() {
    return m_bAlreadyPrinted;
  }

  /**
   * constructor
   */
  public AdminException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public AdminException(String callingClass, int errorLevel, String message,
      String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public AdminException(String callingClass, int errorLevel, String message,
      Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public AdminException(String callingClass, int errorLevel, String message,
      String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  /**
   * getModule
   */
  public String getModule() {
    return "admin";
  }
}
