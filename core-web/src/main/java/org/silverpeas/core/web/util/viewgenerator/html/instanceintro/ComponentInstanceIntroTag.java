/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html.instanceintro;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class ComponentInstanceIntroTag extends TagSupport {

  private String componentId;
  private String language;

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(final String language) {
    this.language = language;
  }

  @Override
  public int doEndTag() throws JspException {
    try {
      String intro = WysiwygController.load(getComponentId(), "Intro", getLanguage());
      if (getComponentId().startsWith("classifieds")) {
        intro = WysiwygController.load(getComponentId(), "Node_0", getLanguage());
      } else {
        ComponentInstLight component = OrganizationController.get().getComponentInstLight(getComponentId());
        if (component.isWorkflow()) {
          intro = WysiwygController.load(getComponentId(), getComponentId(), getLanguage());
        }
      }
      if (StringUtil.isNotDefined(intro)) {
        return EVAL_PAGE;
      }
      pageContext.getOut().println("<div silverpeas-toggle originalClass=\"componentInstanceIntro\">"+intro+"</div>");
    } catch (IOException e) {
      throw new JspException("ComponentInstanceIntro tag", e);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    return EVAL_BODY_INCLUDE;
  }

}