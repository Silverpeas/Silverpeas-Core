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
package org.silverpeas.core.admin.component.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ehugonnet
 */
public class LocalizedWAComponent extends LocalizedComponent {

  private final WAComponent realComponent;

  public LocalizedWAComponent(WAComponent component, String lang) {
    super(component, lang);
    this.realComponent = component;
  }

  public List<LocalizedProfile> getProfiles() {
    List<LocalizedProfile> localizedProfiles = new ArrayList<>();
    for (Profile profile : realComponent.getProfiles()) {
      localizedProfiles.add(new LocalizedProfile(this, profile));
    }
    return localizedProfiles;
  }

  public LocalizedProfile getProfile(String name) {
    List<LocalizedProfile> profiles = getProfiles();
    for (LocalizedProfile profile : profiles) {
      if (name.equals(profile.getName())) {
        return profile;
      }
    }
    return null;
  }

  public String getRouter() {
    return realComponent.getRouter();
  }

  public String getSuite() {
    return getLocalized("suite", realComponent.getSuite());
  }

  public boolean isPortlet() {
    return realComponent.isPortlet();
  }

  @SuppressWarnings("unused")
  public boolean isVisibleInPersonalSpace() {
    return realComponent.isVisibleInPersonalSpace();
  }
}
