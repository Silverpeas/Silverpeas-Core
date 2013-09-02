/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.util.viewGenerator.html.browseBars;

import com.silverpeas.look.LookHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.viewGenerator.html.NeedWindowTag;
import com.stratelia.webactiv.util.viewGenerator.html.window.Window;

import javax.servlet.jsp.JspException;

public class BrowseBarTag extends NeedWindowTag {

  private static final long serialVersionUID = 2496136938371562945L;
  private BrowseBar browseBar;
  private String extraInformations;
  private String path;
  private String componentId;
  private boolean ignoreComponentLink = true;
  private boolean clickable = true;

  public void setExtraInformations(String extraInformations) {
    this.extraInformations = extraInformations;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public void setIgnoreComponentLink(boolean ignoreComponentLink) {
    this.ignoreComponentLink = ignoreComponentLink;
  }

  public void setClickable(boolean clickable) {
    this.clickable = clickable;
  }

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  public void addElement(BrowseBarElement element) {
    browseBar.addElement(element);
  }

  @Override
  public int doStartTag() throws JspException {
    Window window = getWindow();
    browseBar = window.getBrowseBar();
    browseBar.setLook((LookHelper) pageContext.getSession().getAttribute(LookHelper.SESSION_ATT));
    if (extraInformations != null) {
      browseBar.setExtraInformation(extraInformations);
    }
    if (StringUtil.isDefined(path)) {
      browseBar.setPath(path);
    }
    if (StringUtil.isDefined(componentId)) {
      browseBar.setComponentId(componentId);
    }
    browseBar.setIgnoreComponentLink(ignoreComponentLink);
    browseBar.setClickable(clickable);
    return EVAL_BODY_INCLUDE;
  }
}
