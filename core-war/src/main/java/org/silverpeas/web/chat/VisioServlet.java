/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.chat;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.chat.ChatSettings;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasHttpServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.URLUtil.getFullApplicationURL;
import static org.silverpeas.core.util.file.FileServerUtils.getImageURL;

/**
 * This servlet permits to get more control over the initialization and the management of a visio
 * conference.
 * <p>
 *   For now, this servlet is compatible only with jitsi features.
 * </p>
 * @author silveryocha
 */
public class VisioServlet extends SilverpeasHttpServlet {
  private static final long serialVersionUID = 623455510891123710L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    final ChatSettings chatSettings = ChatSettings.get();
    if (!chatSettings.isVisioEnabled()) {
      throwHttpForbiddenError();
      return;
    }
    final String roomId = defaultStringIfNotDefined(req.getPathInfo()).replace("/", "");
    if (StringUtil.isNotDefined(roomId)) {
      throwHttpForbiddenError();
      return;
    }
    req.setAttribute("roomId", roomId);
    req.setAttribute("ownVisioUrl", chatSettings.getVisioUrl());
    req.setAttribute("domain", chatSettings.getVisioDomainServer());
    req.setAttribute("jwt", chatSettings.getVisioJwt());
    final User user = User.getCurrentRequester();
    if (user != null) {
      req.setAttribute("userName", user.getDisplayedName());
      req.setAttribute("userAvatarUrl",
          getFullApplicationURL(req) + getImageURL(user.getAvatar(), "240x240"));
    }
    try {
      redirectOrForwardService(req, res, "/chat/jsp/jitsi.jsp");
    } catch (ServletException | IOException e) {
      throwHttpNotFoundError();
    }
  }
}
