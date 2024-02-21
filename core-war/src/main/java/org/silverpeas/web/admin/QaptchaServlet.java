/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.web.admin;

import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class QaptchaServlet extends HttpServlet {

  /**
   *
   */
  private static final long serialVersionUID = -3745690351420954550L;
  private static final String QAPTCHA_KEY = "qaptcha_key";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    processRequest(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    HttpSession session = req.getSession(true);

    String action = req.getParameter("action");
    String key = req.getParameter(QAPTCHA_KEY);
    boolean error;
    if ("qaptcha".equals(action) && StringUtil.isDefined(key)) {
      session.setAttribute(QAPTCHA_KEY, key);
      error = false;
    }
    else {
      session.removeAttribute(QAPTCHA_KEY);
      error = true;
    }
    String result = JSONCodec.encodeObject(o -> o.put("error", error));
    try {
      resp.getWriter().append(result);
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

}
