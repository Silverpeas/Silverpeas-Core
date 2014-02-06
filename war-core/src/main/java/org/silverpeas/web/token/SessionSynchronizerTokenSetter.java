/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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
package org.silverpeas.web.token;

import com.silverpeas.session.SessionInfo;
import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionManagementFactory;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * A setter of a session token to the new spawned user session. A user session is really created in
 * Silverpeas when a MainSessionController is instanciated and set to the current HTTP session.
 * Nevertheless, this session token is set for each HTTP session created by the underlying web
 * container so that it can be used with some credentials management function (password reseting,
 * new registration, ...) whereas the user isn't authentified; in this last case, the session token
 * is used as an anti-fuzzing token.
 * <p/>
 * The aim of the session token is to protect the current user session from attempt of intrusively
 * use of it by anyone other that the user himself.
 * <p/>
 * Because the web pages in Silverpeas has a deep use of HTML frames and of page relocation/reload,
 * in order the requests sent by these elements can be correctly taken in charge in the token
 * validation process, a cookie is created and valued with the session token.
 *
 * @author mmoquillon
 */
public class SessionSynchronizerTokenSetter implements HttpSessionListener {

  @Override
  public void sessionCreated(HttpSessionEvent se) {
    HttpSession httpSession = se.getSession();
    SessionManagement sessionManagement = SessionManagementFactory.getFactory().
        getSessionManagement();
    SessionInfo session = sessionManagement.getSessionInfo(httpSession.getId());
    if (session.isDefined()) {
      SynchronizerTokenService tokenservice = SynchronizerTokenServiceFactory.
          getSynchronizerTokenService();
      tokenservice.setUpSessionTokens(session);
    }
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {

  }
}
