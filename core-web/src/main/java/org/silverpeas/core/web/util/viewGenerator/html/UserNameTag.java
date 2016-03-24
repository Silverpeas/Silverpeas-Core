/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html;

import static org.silverpeas.core.util.StringUtil.isDefined;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import static org.silverpeas.core.web.mvc.controller.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.span;

/**
 * This tag prints out the full name of a given user.
 *
 * Yet, it prints the user name and uses the Silverpeas userzoom javascript plugin to render its
 * status and to provide a way for interacting with him.
 *
 * If the user is the current one behind the HTTP session, then by default the user zoom isn't
 * activated.
 *
 * @author mmoquillon
 */
public class UserNameTag extends SimpleTagSupport {

  private String userId;
  private boolean zoom = true;

  /**
   * Gets the unique identifier of the user concerned by this tag.
   *
   * @return the user unique identifier.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the unique identifier of the user whose the name has to be printed out.
   *
   * @param userId the user unique identifier.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Is the user zoom to be used with this user? By default the user zoom is used. This method is to
   * force the deactivation of the user zoom feature.
   *
   * @return true if the user zoom is activated, false otherwise.
   */
  public boolean isZoom() {
    return zoom;
  }

  /**
   * Sets explicitly the use or not of the user zoom.
   *
   * @param zoom true if the user zoom has to be used (default behaviour), false otherwise.
   */
  public void setZoom(boolean zoom) {
    this.zoom = zoom;
  }

  @Override
  public void doTag() throws JspException, IOException {
    if (isDefined(userId)) {
      span userName = UserNameGenerator.generate(userId, getCurrentUserIdInSession());
      ElementContainer container = new ElementContainer();
      container.addElement(userName);
      container.output(getOut());
    }
  }

  protected String getCurrentUserIdInSession() {
    MainSessionController session = (MainSessionController) getJspContext().getAttribute(
            MAIN_SESSION_CONTROLLER_ATT, PageContext.SESSION_SCOPE);
    if (session != null) {
      return session.getUserId();
    }
    return null;
  }

  protected JspWriter getOut() {
    return getJspContext().getOut();
  }
}
