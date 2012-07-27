/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.node;

import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import javax.inject.Named;

/**
 * The NodeBm provider encapsulates the way the NodeBm service is accessed. It's managed by the IoC
 * container.
 * 
 * It provides a way to access the EJB 2 for bean managed by an IoC container both in a production
 * and in a test runtime environment. For the test runtime environment it provides a setter to 
 * set explicitly the NodeBm instance to use in the tests.
 */
@Named
public class NodeBmProvider {
  
  private NodeBm nodeBm;
  
  public NodeBm getNodeBm() {
    if (nodeBm == null) {
      try {
        NodeBmHome home = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
                JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
        nodeBm = home.create();
      } catch (Exception ex) {
        throw new RuntimeException(ex.getMessage(), ex);
      }
    }
    return nodeBm;
  }
  
  /**
   * This method is dedicated for tests.
   * @param nodeBm the NodeBm instance to use.
   */
  public void setNodeBm(final NodeBm nodeBm) {
    this.nodeBm = nodeBm;
  }
}
