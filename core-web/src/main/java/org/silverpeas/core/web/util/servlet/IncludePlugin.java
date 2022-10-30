/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.servlet;

import org.apache.ecs.ElementContainer;
import org.silverpeas.core.html.WebPlugin;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

public class IncludePlugin extends SilverpeasAuthenticatedHttpServlet {
  private static final long serialVersionUID = 5315718492441997694L;

  @Inject
  protected SilverpeasWebUtil util;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    res.setContentType(MediaType.TEXT_HTML);
    res.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    res.setHeader("Pragma", "no-cache");
    res.setDateHeader("Expires", -1);
    getPluginName(req)
        .map(h -> WebPlugin.get().getHtml(h, util.getUserLanguage(req)))
        .map(ElementContainer::toString)
        .ifPresentOrElse(
            h -> sendHtml(res, h),
            () -> throwHttpForbiddenError("plugin not found"));
  }

  protected void sendHtml(final HttpServletResponse res, final String html) {
    try {
      final PrintWriter writer = res.getWriter();
      writer.write(html);
      writer.flush();
    } catch (IOException e) {
      throw new WebApplicationException(e);
    }
  }

  protected Optional<String> getPluginName(HttpServletRequest request) {
    return Optional.ofNullable(request.getPathInfo()).map(p -> p.substring(1));
  }
}
