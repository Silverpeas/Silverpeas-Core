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

package org.silverpeas.core.web.util.viewgenerator.html.pdc;

import javax.servlet.jsp.JspException;
import org.apache.ecs.ElementContainer;
import static org.silverpeas.core.web.util.viewgenerator.html.pdc.PdcClassificationTagOperation.*;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A tag that renders the classification of a content on the PdC configured for the Silverpeas
 * component instance.
 */
public class PdcClassificationTag extends BaseClassificationPdCTag {

  private static final long serialVersionUID = 3377113335947703561L;
  private boolean editable = false;

  /**
   * Is the classification on the PdC can be edited?
   * @return true if the classification of the content can be edited (to add a new position, to
   * update or to delete an existing position. False otherwise.
   */
  public boolean isEditable() {
    return editable;
  }

  /**
   * Sets the edition mode of the PdC classification.
   * @param editable true or false. If true the classification on the PdC can be edited, otherwise
   * it will be read-only rendered.
   */
  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  @Override
  public void doTag() throws JspException {
    ElementContainer container;
    if (isDefined(getContentId())) {
      if (isEditable()) {
        container = invoke(OPEN_CLASSIFICATION);
      } else {
        container = invoke(READ_CLASSIFICATION);
      }
    } else {
      container = invoke(PREDEFINE_CLASSIFICATION);
    }
    container.output(getOut());
  }
}
