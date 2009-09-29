/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.selection;

import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.util.ResourceLocator;

public class SelectionSettings extends SilverpeasSettings {
  public static int m_ElementsByPage = 20;

  static {
    ResourceLocator rs = new ResourceLocator(
        "com.stratelia.silverpeas.selection.settings.selectionSettings", "");

    m_ElementsByPage = readInt(rs, "ElementsByPage", m_ElementsByPage);
  }
}
