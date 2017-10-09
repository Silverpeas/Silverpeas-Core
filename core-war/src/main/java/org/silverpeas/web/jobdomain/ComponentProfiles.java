/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.jobdomain;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.LocalizedComponent;
import org.silverpeas.core.admin.component.model.LocalizedProfile;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ComponentProfiles {

  private ComponentInstLight component;
  private LocalizedComponent localizedComponent;
  private SpaceInstLight space;
  private List<String> profilesName = new ArrayList<>();

  public ComponentProfiles(ComponentInstLight component, LocalizedComponent localizedComponent) {
    this.component = component;
    this.localizedComponent = localizedComponent;
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
    List<String> localizedProfilesName = new ArrayList<>();
    if (localizedComponent != null) {
      for (String profileName : profilesName) {
        LocalizedProfile localizedProfile = localizedComponent.getProfile(profileName);
        if (localizedProfile != null) {
          localizedProfilesName.add(localizedProfile.getLabel());
        } else {
          localizedProfilesName.add(profileName);
        }
      }
    } else {
      localizedProfilesName = profilesName;
    }
    return StringUtil.join(localizedProfilesName, ", ");
  }

  public String getLocalizedSpaceLabel() {
    if (localizedComponent != null) {
      return space.getName(localizedComponent.getLanguage());
    }
    return space.getName();
  }

  public String getLocalizedInstanceLabel() {
    if (localizedComponent != null) {
      return component.getLabel(localizedComponent.getLanguage());
    }
    return "";
  }

  public String getLocalizedComponentLabel() {
    if (localizedComponent != null) {
      return localizedComponent.getLabel();
    }
    return "";
  }
}