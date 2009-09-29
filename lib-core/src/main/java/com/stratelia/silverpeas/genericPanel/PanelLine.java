/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.genericPanel;

public class PanelLine {
  public String m_Id = "";
  public String[] m_Values = new String[0];
  public boolean m_HighLight = false;
  public boolean m_Selected = false;

  public PanelLine(String id, String[] values, boolean highLight) {
    m_Id = id;
    m_Values = values;
    m_HighLight = highLight;
    m_Selected = false;
  }
}
