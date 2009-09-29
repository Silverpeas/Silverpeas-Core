package com.stratelia.webactiv.beans.admin.instance.control;

import java.io.Serializable;

/**
 * Class storing profile information for WAComponent
 */
public class SPProfile implements Serializable {
  private String m_strName;
  private String m_strLabel;

  /**
   * Constructs and initialises the object
   * 
   * @param strName
   *          name
   * @param strLabel
   *          label
   */
  public SPProfile(String strName, String strLabel) {
    m_strName = strName;
    m_strLabel = strLabel;
  }

  /**
   * @return the name
   */
  public String getName() {
    return m_strName;
  }

  /**
   * @param strName
   *          the name to set
   */
  public void setName(String strName) {
    m_strName = strName;
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return m_strLabel;
  }

  /**
   * @param strLabel
   *          the label to set
   */
  public void setLabel(String strLabel) {
    m_strLabel = strLabel;
  }

}
