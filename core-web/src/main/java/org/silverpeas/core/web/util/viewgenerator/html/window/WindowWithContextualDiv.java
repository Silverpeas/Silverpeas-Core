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

package org.silverpeas.core.web.util.viewgenerator.html.window;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.StringUtil;

import java.util.List;

/**
 * The default implementation of Window interface
 * @author neysseri
 * @version 1.0
 */
public class WindowWithContextualDiv extends AbstractWindow {

  /**
   * Constructor declaration
   * @see
   */
  public WindowWithContextualDiv() {
    super();
  }

  public String getContextualDiv() {
    String spaceIds = "";
    String componentId = getGEF().getComponentIdOfCurrentRequest();
    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
    if (StringUtil.isDefined(componentId)) {
      List<SpaceInst> spaces = oc.getSpacePathToComponent(componentId);

      for (SpaceInst spaceInst : spaces) {
        String spaceId = spaceInst.getId();
        spaceIds += spaceId + " ";
      }
    }

    if (StringUtil.isDefined(spaceIds)) {
      ComponentInstLight component = oc.getComponentInstLight(componentId);
      return "<div class=\"" + spaceIds + component.getName() + " " + componentId + "\">";
    }
    return null;
  }
}