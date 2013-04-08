/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Class declaration
 * @author
 */
public class GoToTopic extends GoTo {

  private static final long serialVersionUID = 148348921885581569L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    String componentId = req.getParameter("ComponentId");
    NodePK pk = new NodePK(objectId, componentId);
    NodeDetail node = getNodeBm().getHeader(pk);
    SilverTrace.info("peasUtil", "GoToTopic.doPost",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);
    setGefSpaceId(req, componentId);
    String gotoURL = URLManager.getURL(null, componentId) + node.getURL();
    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }

  public NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME,
          NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      displayError(null);
    }
    return nodeBm;
  }
}
