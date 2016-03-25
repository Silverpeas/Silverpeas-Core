/*
 * Copyright (C) 2000-2014 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.text.MessageFormat;

import static org.silverpeas.core.web.mvc.controller.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;

/**
 * A tag to render the pane of management of the attachments.
 * @author mmoquillon
 */
public class AttachmentPaneTag extends TagSupport {
  private static final long serialVersionUID = -7120227728777549004L;

  private static final String PANE_URI = "/attachment/jsp/displayAttachedFiles.jsp?Id={0}"
      + "&Profile={1}&ComponentId={2}&Context=attachment&addFileMenu=true";
  private String resourceId;
  private String componentId;
  private boolean readOnly = false;

  @Override
  public int doEndTag() throws JspException {
    try {
      pageContext.getOut().flush();
      String uri = MessageFormat.format(PANE_URI, getResourceId(), getUserRole(), getComponentId());
      pageContext.getServletContext().getRequestDispatcher(uri).include(pageContext.getRequest(),
          pageContext.getResponse());
      return EVAL_PAGE;
    } catch (Exception ex) {
      throw new JspException(ex.getMessage(), ex);
    }
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
  }

  private String getResourceId() {
    return this.resourceId;
  }

  private String getUserRole() {
    String role = SilverpeasRole.user.getName();
    if (!isReadOnly()) {
      String userId = getCurrentUserIdInSession();
      if (StringUtil.isDefined(userId)) {
        String[] roles = OrganizationControllerProvider.getOrganisationController()
            .getUserProfiles(userId, getComponentId());
        if (roles.length > 0) {
          role = SilverpeasRole.getGreaterFrom(SilverpeasRole.from(roles)).getName();
        }
      }
    }
    return role;
  }

  private String getCurrentUserIdInSession() {
    MainSessionController session = (MainSessionController) pageContext.getAttribute(
        MAIN_SESSION_CONTROLLER_ATT, PageContext.SESSION_SCOPE);
    if (session != null) {
      return session.getUserId();
    }
    return null;
  }

  private String getComponentId() {
    return this.componentId;
  }

  private boolean isReadOnly() {
    return readOnly;
  }
}
