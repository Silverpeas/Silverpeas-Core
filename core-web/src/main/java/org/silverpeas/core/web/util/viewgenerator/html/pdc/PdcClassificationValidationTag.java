/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.pdc;

import javax.servlet.jsp.JspException;

import org.apache.ecs.ElementContainer;

/**
 * A tag that renders the use of the JQuery PdC plugin to get the possibly new classification for a
 * new content in Silverpeas.
 */
public class PdcClassificationValidationTag extends BaseClassificationPdCTag {

  private String errorMessager;
  private boolean errorWebRender = false;
  private String errorCounter;

  public String getErrorCounter() {
    return errorCounter;
  }

  public void setErrorCounter(String errorCounter) {
    this.errorCounter = errorCounter;
  }

  public String getErrorMessager() {
    return errorMessager;
  }

  public void setErrorMessager(String errorMessage) {
    this.errorMessager = errorMessage;
  }

  public boolean isErrorWebRender() {
    return errorWebRender;
  }

  public void setErrorWebRender(final boolean errorWebRender) {
    this.errorWebRender = errorWebRender;
  }

  @Override
  public void doTag() throws JspException {
    if (isPdcUsed()) {
      ElementContainer xhtmlcontainer = new ElementContainer();
      String script = "if (!$('#" + getId() + "').pdcClassification('isClassificationValid')) { " +
          getErrorMessager() + " += \"" + (isErrorWebRender() ? "<li>" : " - ") +
          getResources().getString("pdcPeas.theContent") + " " +
          getResources().getString("pdcPeas.MustContainsMandatoryAxis") +
          (isErrorWebRender() ? "</li>" : "\\n") + "\"; " + getErrorCounter() + "++; }";
      xhtmlcontainer.addElement(script);
      xhtmlcontainer.output(getOut());
    }
  }
}
