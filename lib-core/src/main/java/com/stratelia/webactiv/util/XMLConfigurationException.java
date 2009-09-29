/*
 * XMLConfigurationException.java
 *
 * Created on 5 mars 2001, 10:57
 */

package com.stratelia.webactiv.util;

/**
 * 
 * @author jpouyadou
 * @version
 */
public class XMLConfigurationException extends Exception {
  public final static int KEY_NOT_FOUND = 1;
  public final static int INVALID_VALUE = 2;
  public int m_Code = 0;

  /** Creates new XMLConfigurationException */
  static String getMessageForCode(int code) {
    String msg;
    switch (code) {
      case KEY_NOT_FOUND:
        msg = "The requested key was not found in the configuration store";
        break;
      case INVALID_VALUE:
        msg = "The value for the requested key is invalid";
        break;
      default:
        msg = "";
        break;
    }
    return (msg);
  }

  public XMLConfigurationException(int code) {
    super(getMessageForCode(code));
    m_Code = code;
  }

  public XMLConfigurationException(int code, String msg) {
    super(getMessageForCode(code) + ":" + msg);
    m_Code = code;
  }

  public int getCode() {
    return (m_Code);
  }
}
