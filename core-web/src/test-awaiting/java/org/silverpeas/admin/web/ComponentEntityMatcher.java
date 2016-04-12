/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.admin.web;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.silverpeas.admin.web.ComponentEntity;

import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;

/**
 * @author Yohann Chastagnier
 */
public class ComponentEntityMatcher extends
    StructureElementEntityMatcher<ComponentEntity, ComponentInstLight> {

  private ComponentEntityMatcher(final ComponentInstLight expected) {
    super(expected);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.silverpeas.admin.web.StructureElementEntityMatcher#matches(org.apache.commons.lang3.builder
   * .EqualsBuilder, java.lang.Object, org.silverpeas.core.webapi.admin.StructureElementEntity)
   */
  @Override
  protected void matches(final EqualsBuilder matcher, final ComponentInstLight expected,
      final ComponentEntity actual) {
    matcher.append("component", actual.getType());
    matcher.appendSuper(expected.getId().endsWith(actual.getId()));
    matcher.append(expected.getName(), actual.getName());
    matcher.append(expected.getLabel(), actual.getLabel());
    matcher.append(expected.getDescription(), actual.getDescription());
    matcher.append(expected.getOrderNum(), actual.getRank());
    matcher.append(expected.getStatus() == null ? "" : expected.getStatus(), actual.getStatus());
    matcher.append(expected.isInheritanceBlocked(), actual.isInheritanceBlocked());
    matcher.append("/R" + expected.getName() + "/" + expected.getId() + "/Main", actual.getUrl());
    // URI
    matcher.appendSuper(actual
        .getParentURI()
        .toString()
        .endsWith(
            "/spaces/" + expected.getDomainFatherId().replaceFirst(Admin.SPACE_KEY_PREFIX, "")));
  }

  public static ComponentEntityMatcher matches(final ComponentInstLight expected) {
    return new ComponentEntityMatcher(expected);
  }
}
