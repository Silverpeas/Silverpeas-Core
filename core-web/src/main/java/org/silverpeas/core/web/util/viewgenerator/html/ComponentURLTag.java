/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.util.URLUtil;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import static org.silverpeas.core.util.StringUtil.*;

/**
 * A tag to print out an URL of a Silverpeas component instance.
 */
public class ComponentURLTag extends TagSupport {
  private static final long serialVersionUID = 6131959083404837559L;

  private String componentName = null;
  private String componentId = null;

  @Override
  public int doStartTag() throws JspException {
    String url = URLUtil.getApplicationURL();
    if (isDefined(componentName)) {
      url += URLUtil.getURL(componentName, null, componentId);
    } else {
      url += URLUtil.getURL(null, componentId);
    }
    try {
      pageContext.getOut().print(url);
    } catch (IOException e) {
      throw new JspException("Silverpeas Resource URL Tag", e);
    }
    return EVAL_PAGE;
  }

  /**
   * Gets the identifier of the Silverpeas component instance for which the URL must be printed out.
   * @return the component instance identifier.
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * Sets the identifier of the Silverpeas component instance for which the URL must be printed out.
   * @param componentId the component identifier.
   */
  public void setComponentId(final String componentId) {
    this.componentId = componentId;
  }

  /**
   * Gets the name of the Silverpeas component instance for which the URL must be printed out.
   * @return the component name.
   */
  public String getComponentName() {
    return componentName;
  }

  /**
   * Sets the name of the Silverpeas component instance for which the URL must be printed out.
   * @param componentName the name of the component.
   */
  public void setComponentName(final String componentName) {
    this.componentName = componentName;
  }

}
