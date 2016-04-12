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
 * The base tag for all concrete tags on the PdC classification of a content.
 */
public class PdcClassificationPositionsTag extends BaseClassificationPdCTag {

  private static final long serialVersionUID = -562523990230139481L;
  private String setIn;

  public PdcClassificationPositionsTag() {
  }

  public String getSetIn() {
    return setIn;
  }

  public void setSetIn(String setIn) {
    this.setIn = setIn;
  }

  @Override
  public void doTag() throws JspException {
    if (isPdcUsed()) {
      ElementContainer xhtmlcontainer = new ElementContainer();
      String script = getSetIn() + " = $.toJSON( $('#" + getId()
          + "').pdcClassification('positions') );";
      xhtmlcontainer.addElement(script);
      xhtmlcontainer.output(getOut());
    }
  }
}
