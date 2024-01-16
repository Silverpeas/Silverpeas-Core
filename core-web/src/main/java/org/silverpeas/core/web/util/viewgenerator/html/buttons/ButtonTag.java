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
package org.silverpeas.core.web.util.viewgenerator.html.buttons;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPaneTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class ButtonTag extends TagSupport {

  private static final long serialVersionUID = 1L;
  private String label = "";
  private String action = "#";
  private String classes;
  private boolean disabled = false;
  private String actionPreProcessing = "";

  @Override
  public int doEndTag() throws JspException {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession().getAttribute(
        GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    Button button = gef.getFormButton(label, action, disabled);
    button.setClasses(classes);
    button.setActionPreProcessing(actionPreProcessing);
    ButtonPaneTag buttonPane = (ButtonPaneTag) findAncestorWithClass(this, ButtonPaneTag.class);
    if (buttonPane != null) {
      buttonPane.addButton(button);
      return EVAL_PAGE;
    }
    try {
      pageContext.getOut().println(button.print());
    } catch (IOException e) {
      throw new JspException("ButtonTag Tag", e);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    actionPreProcessing = "";
    return EVAL_BODY_INCLUDE;
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /**
   * @param label the label to set
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * @return the action
   */
  public String getAction() {
    return action;
  }

  /**
   * @param action the action to set
   */
  public void setAction(String action) {
    this.action = action;
  }

  public void setClasses(final String classes) {
    this.classes = classes;
  }

  /**
   * @return the disabled
   */
  public boolean isDisabled() {
    return disabled;
  }

  /**
   * @param disabled the disabled to set
   */
  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public void setActionPreProcessing(final String actionPreProcessing) {
    this.actionPreProcessing = actionPreProcessing;
  }
}
