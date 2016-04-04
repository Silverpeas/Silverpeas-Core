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

/**
 * A tag that renders an HTML/javascript section with which a classification onto the PdC can be
 * created for a new/already existing content in a given component instance. The positions that are
 * created for the classification of the underlying content are not done automatically (as the
 * content can be not already existed). To set them, please use in conjonction with this tag the
 * PdcClassificationValidationTag one.
 */
public class PdcNewContentClassificationTag extends BaseClassificationPdCTag {

  private static final long serialVersionUID = 3377113335947703561L;

  @Override
  public void doTag() throws JspException {
    ElementContainer container = invoke(CREATE_CLASSIFICATION);
    container.output(getOut());
  }

}
