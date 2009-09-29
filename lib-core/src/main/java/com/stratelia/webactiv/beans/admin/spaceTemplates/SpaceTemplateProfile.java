/** 
 *
 * @author  nchaix
 * @version 
 */

package com.stratelia.webactiv.beans.admin.spaceTemplates;

import java.util.Hashtable;
import java.util.Vector;

public class SpaceTemplateProfile extends Object {
  private String m_sName;
  private String m_sLabel;
  private Hashtable m_hMappedComponentsName;

  /** Creates new ProfileInst */
  public SpaceTemplateProfile() {
    m_sName = "";
    m_sLabel = "";
    m_hMappedComponentsName = new Hashtable();
  }

  public void setName(String sName) {
    m_sName = sName;
  }

  public String getName() {
    return m_sName;
  }

  public void setLabel(String sLabel) {
    m_sLabel = sLabel;
  }

  public String getLabel() {
    return m_sLabel;
  }

  public Vector getMappedComponentProfileName(String sComponentLabel) {
    return (Vector) m_hMappedComponentsName.get(sComponentLabel);
  }

  public void addMappedComponentProfile(String sComponentLabel,
      String sComponentProfileName) {
    Vector mappings = null;
    if (m_hMappedComponentsName.containsKey(sComponentLabel))
      mappings = (Vector) m_hMappedComponentsName.get(sComponentLabel);

    if (mappings == null)
      mappings = new Vector();

    if (!mappings.contains(sComponentProfileName))
      mappings.add(sComponentProfileName);

    m_hMappedComponentsName.put(sComponentLabel, mappings);
  }
}
