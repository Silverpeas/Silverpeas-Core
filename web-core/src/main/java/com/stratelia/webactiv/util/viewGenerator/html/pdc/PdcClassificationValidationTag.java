/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.stratelia.webactiv.util.viewGenerator.html.pdc;

import javax.servlet.jsp.JspException;
import org.apache.ecs.ElementContainer;

/**
 * A tag that renders the use of the JQuery PdC plugin to get the possibly new classification for
 * a new content in Silverpeas.
 */
public class PdcClassificationValidationTag extends BaseClassificationPdCTag {

  private static final long serialVersionUID = 3377113335947703561L;
  
  private String jsFunctionToCall;
  
  public String getFunctionToCall() {
    return jsFunctionToCall;
  }
  
  public void setFunctionToCall(String function) {
    this.jsFunctionToCall = function;
  }
  

  @Override
  public int doStartTag() throws JspException {
    ElementContainer xhtmlcontainer = new ElementContainer();
    String script = "if ($('#" + PDC_CLASSIFICATION_WIDGET_TAG_ID +
            "').pdc('isClassificationValid')) { var pdcClassification = $('#" +
            PDC_CLASSIFICATION_WIDGET_TAG_ID + "').pdc('positions'); " + getFunctionToCall() +
            "($.toJSON(pdcClassification)) } else { " + getFunctionToCall() + "(null); window.alert(\"" +
            getResources().getString("pdcPeas.theContent") + " " +
              getResources().getString("pdcPeas.MustContainsMandatoryAxis") + "\") }";
    xhtmlcontainer.addElement(script);
    xhtmlcontainer.output(pageContext.getOut());
    return SKIP_BODY;
  }
  
}
