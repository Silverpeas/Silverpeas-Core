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

import org.silverpeas.core.web.util.servlet.GoTo;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

public class GoToPublication extends GoTo {

  private static final long serialVersionUID = -5940054543777929024L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {
    PublicationPK pubPK = new PublicationPK(objectId);
    PublicationDetail pub = getPublicationBm().getDetail(pubPK);

    String componentId = req.getParameter("ComponentId"); // in case of an
    // alias, componentId is given
    if (!StringUtil.isDefined(componentId)) {
      componentId = pub.getPK().getInstanceId();
    }
    // Set GEF and look helper space identifier
    setGefSpaceId(req, componentId);

    String gotoURL = URLUtil.getURL(null, componentId) + pub.getURL();
    return "goto=" + URLEncoder.encode(gotoURL, CharEncoding.UTF_8);
  }

  private PublicationService getPublicationBm() {
    try {
      return ServiceProvider.getService(PublicationService.class);
    } catch (Exception e) {
      displayError(null);
    }
    return null;
  }
}