/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.web.servlets;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.servlet.GoTo;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.core.util.ServiceProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

/**
 * Class declaration
 *
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

    setGefSpaceId(req, componentId);
    String gotoURL = URLUtil.getURL(null, componentId) + node.getURL();
    return "goto=" + URLEncoder.encode(gotoURL, CharEncoding.UTF_8);
  }

  public NodeService getNodeBm() {
    try {
      return ServiceProvider.getService(NodeService.class);
    } catch (Exception e) {
      displayError(null);
    }
    return null;
  }
}
