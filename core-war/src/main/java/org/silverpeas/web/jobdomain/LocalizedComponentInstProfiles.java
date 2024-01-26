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
package org.silverpeas.web.jobdomain;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.LocalizedProfile;
import org.silverpeas.core.admin.component.model.LocalizedWAComponent;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class LocalizedComponentInstProfiles {

  private final ComponentInstLight component;
  private final LocalizedWAComponent localizedWAComponent;
  private final String language;
  private final List<String> profilesName = new ArrayList<>();
  private SpaceInstLight space;

  public LocalizedComponentInstProfiles(ComponentInstLight component,
      LocalizedWAComponent localizedWAComponent, final String language) {
    this.component = component;
    this.localizedWAComponent = localizedWAComponent;
    this.language = language;
  }

  public ComponentInstLight getComponent() {
    return component;
  }

  public SpaceInstLight getSpace() {
    return space;
  }

  public void setSpace(final SpaceInstLight space) {
    this.space = space;
  }

  public void addProfile(ProfileInst profile) {
    if (!profilesName.contains(profile.getName())) {
      profilesName.add(profile.getName());
    }
  }

  public String getLocalizedProfilesName() {
    return profilesName.stream()
        .map(p -> ofNullable(localizedWAComponent)
            .map(l -> l.getProfile(p))
            .map(LocalizedProfile::getLabel)
            .orElse(p))
        .collect(Collectors.joining(", "));
  }

  public String getLocalizedSpaceLabel() {
    return space.getName(language);
  }

  public String getLocalizedInstanceLabel() {
    return component.getLabel(language);
  }

  public String getLocalizedComponentLabel() {
    return ofNullable(localizedWAComponent)
        .map(LocalizedWAComponent::getLabel)
        .orElse(StringUtil.EMPTY);
  }
}