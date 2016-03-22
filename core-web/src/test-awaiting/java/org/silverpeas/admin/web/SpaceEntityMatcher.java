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

import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import org.apache.commons.lang3.builder.EqualsBuilder;

import static org.silverpeas.admin.web.AdminResourceURIs.*;

/**
 * @author Yohann Chastagnier
 */
public class SpaceEntityMatcher extends StructureElementEntityMatcher<SpaceEntity, SpaceInstLight> {

  private SpaceEntityMatcher(final SpaceInstLight expected) {
    super(expected);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.silverpeas.admin.web.StructureElementEntityMatcher#matches(org.apache.commons.lang3.builder
   * .EqualsBuilder, java.lang.Object, org.silverpeas.core.webapi.admin.StructureElementEntity)
   */
  @Override
  protected void matches(final EqualsBuilder matcher, final SpaceInstLight expected,
      final SpaceEntity actual) {
    matcher.append("space", actual.getType());
    matcher.append(expected.getShortId(), actual.getId());
    matcher.append(expected.getName(), actual.getLabel());
    matcher.append(expected.getDescription(), actual.getDescription());
    matcher.append(expected.getOrderNum(), actual.getRank());
    matcher.append(expected.getLevel(), actual.getLevel());
    matcher.append(expected.getStatus() == null ? "" : expected.getStatus(), actual.getStatus());
    matcher.append(expected.isDisplaySpaceFirst(), actual.isSpaceDisplayedAtFirst());
    matcher.append(expected.isInheritanceBlocked(), actual.isInheritanceBlocked());
    // URI
    if (!expected.isRoot()) {
      matcher.appendSuper(actual.getParentURI().toString()
          .endsWith("/spaces/" + expected.getFatherId()));
    } else {
      matcher.appendSuper(actual.getParentURI() == null ||
          actual.getParentURI().toString().equals(""));
    }
    matcher
        .append(
            true,
            actual
                .getSpacesURI()
                .toString()
                .endsWith(
                    "/" + SPACES_BASE_URI + "/" + expected.getShortId() + "/" +
                        SPACES_SPACES_URI_PART));
    matcher.append(
        true,
        actual
            .getComponentsURI()
            .toString()
            .endsWith(
                "/" + SPACES_BASE_URI + "/" + expected.getShortId() + "/" +
                    SPACES_COMPONENTS_URI_PART));
    matcher
        .append(
            true,
            actual
                .getContentURI()
                .toString()
                .endsWith(
                    "/" + SPACES_BASE_URI + "/" + expected.getShortId() + "/" +
                        SPACES_CONTENT_URI_PART));
    matcher.append(
        true,
        actual
            .getAppearanceURI()
            .toString()
            .endsWith(
                "/" + SPACES_BASE_URI + "/" + expected.getShortId() + "/" +
                    SPACES_APPEARANCE_URI_PART));
  }

  public static SpaceEntityMatcher matches(final SpaceInstLight expected) {
    return new SpaceEntityMatcher(expected);
  }
}
