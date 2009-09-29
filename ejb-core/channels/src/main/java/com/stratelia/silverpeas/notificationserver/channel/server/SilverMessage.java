package com.stratelia.silverpeas.notificationserver.channel.server;

/*
 * SilverMessage.java
 *
 * Created on 15/04/2002
 *
 * Author neysseri
 */

/**
 * 
 * @author neysseri
 * @version
 */
public class SilverMessage {

  private String m_What = null;
  private String m_Content = null;
  private String m_ID = null;

  /**
   * --------------------------------------------------------------------------
   * constructor constructor
   */
  public SilverMessage(String what, String content) {
    this.m_What = what;
    this.m_Content = content;
  }

  /**
   * --------------------------------------------------------------------------
   * constructor constructor
   */
  public SilverMessage(String what) {
    this.m_What = what;
    this.m_Content = new String("");
  }

  /**
   * --------------------------------------------------------------------------
   * getWhat return what
   */
  public String getWhat() {
    return m_What;
  };

  /**
   * --------------------------------------------------------------------------
   * getContent return content
   */
  public String getContent() {
    return m_Content;
  };

  /**
   * --------------------------------------------------------------------------
   * setContent set the content
   */
  public void setContent(String content) {
    m_Content = content;
  };

  /**
   * --------------------------------------------------------------------------
   * setContent set the ID
   */
  public void setID(String ID) {
    m_ID = ID;
  };

  /**
   * --------------------------------------------------------------------------
   * setContent get the ID
   */
  public String getID() {
    return m_ID;
  };

}