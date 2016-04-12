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

package org.silverpeas.web.workflowdesigner.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane;
import org.silverpeas.core.web.util.viewgenerator.html.buttons.Button;

/**
 * Class implementing the tag &lt;buttonPane&gt; from workflowEditor.tld
 */
public class ProcessModelButtonPane extends TagSupport {

  private static final long serialVersionUID = 2771341684220021139L;
  private String strCancelAction;

  /**
   * @return the current tab name
   */
  public String getCancelAction() {
    return strCancelAction;
  }

  /**
   * @param cancelAction the current Tab name to set
   */
  public void setCancelAction(String cancelAction) {
    strCancelAction = cancelAction;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
   */
  public int doStartTag() throws JspException {
    GraphicElementFactory gef;
    MultiSilverpeasBundle resource;
    ButtonPane buttonPane;
    Button validateButton;
    Button cancelButton;

    gef = (GraphicElementFactory) pageContext.getSession().getAttribute(
        "SessionGraphicElementFactory");
    buttonPane = gef.getButtonPane();
    resource = (MultiSilverpeasBundle) pageContext.getRequest().getAttribute(
        "resources");
    validateButton = (Button) gef.getFormButton(resource
        .getString("GML.validate"), "javascript:sendData();", false);
    cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"),
        strCancelAction, false);

    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);

    try {
      pageContext.getOut().println(
          "<BR><center>" + buttonPane.print() + "</center><BR>");
    } catch (IOException e) {
      throw new JspException("Error when printing the Workflow Designer tabs",
          e);
    }
    return super.doStartTag();
  }

}
