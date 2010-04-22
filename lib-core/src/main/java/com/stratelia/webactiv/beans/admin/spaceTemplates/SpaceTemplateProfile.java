/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
  private Hashtable<String, Vector<String>> m_hMappedComponentsName;

  /** Creates new ProfileInst */
  public SpaceTemplateProfile() {
    m_sName = "";
    m_sLabel = "";
    m_hMappedComponentsName = new Hashtable<String, Vector<String>>();
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

  public Vector<String> getMappedComponentProfileName(String sComponentLabel) {
    return m_hMappedComponentsName.get(sComponentLabel);
  }

  public void addMappedComponentProfile(String sComponentLabel,
      String sComponentProfileName) {
    Vector<String> mappings = null;
    if (m_hMappedComponentsName.containsKey(sComponentLabel))
      mappings = m_hMappedComponentsName.get(sComponentLabel);

    if (mappings == null)
      mappings = new Vector<String>();

    if (!mappings.contains(sComponentProfileName))
      mappings.add(sComponentProfileName);

    m_hMappedComponentsName.put(sComponentLabel, mappings);
  }
}
