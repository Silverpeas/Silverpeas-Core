/*
 * WAAttributeValuePair.java
 *
 * Created on 3 octobre 2000, 17:14
 */

package com.stratelia.webactiv.util;

/**
 * 
 * @author jpouyadou
 * @version
 */
public class WAAttributeValuePair extends Object {
  public static final int SEARCH_MODE_PARTIAL = 0x0001;
  public static final int SEARCH_MODE_STARTSWITH = 0x0002;
  public static final int SEARCH_MODE_EXACT = ~(SEARCH_MODE_PARTIAL | SEARCH_MODE_STARTSWITH);
  private String m_Value = null;
  private String m_Name = null;
  private int m_SearchMode = SEARCH_MODE_STARTSWITH;

  /** Creates new WAAttributeValuePair */
  public WAAttributeValuePair(String m, String v) {
    m_Name = m;
    m_Value = v;
  }

  public String getName() {
    return (m_Name);
  }

  public String getValue() {
    return (m_Value);
  }

  public void setName(String n) {
    m_Name = n;
  }

  public void setValue(String v) {
    m_Value = v;
  }

  public void setSearchMode(int mode) {
    m_SearchMode = mode;
  }

  public int getSearchMode() {
    return (m_SearchMode);
  }
}