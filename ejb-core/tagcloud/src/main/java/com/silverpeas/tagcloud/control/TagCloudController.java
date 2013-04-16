/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.tagcloud.control;

import com.silverpeas.tagcloud.ejb.TagCloudBm;
import com.silverpeas.tagcloud.ejb.TagCloudRuntimeException;

import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class TagCloudController {

  // Home interface of session bean TagCloudBmEJB.
  private static TagCloudBm tagCloudBm = null;

  public TagCloudController() {
    getTagCloudBm();
  }

  /**
   * Getter of the home object of TagCloud EJB (initializes it if needed).
   */
  private static TagCloudBm getTagCloudBm() {
    synchronized (TagCloudController.class) {
      if (tagCloudBm == null) {
        try {
          tagCloudBm = EJBUtilitaire.getEJBObjectRef(JNDINames.TAGCLOUDBM_EJBHOME, TagCloudBm.class);
        } catch (Exception e) {
          throw new TagCloudRuntimeException("TagCloudController.initHome()",
              SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
        }
      }
    }
    return tagCloudBm;
  }

  public synchronized void close() {
    tagCloudBm = null;
  }
}