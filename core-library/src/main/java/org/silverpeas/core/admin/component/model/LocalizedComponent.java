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

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.ui.DisplayI18NHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ehugonnet
 */
public class LocalizedComponent {

  private String lang;
  private WAComponent realComponent;

  public LocalizedComponent(WAComponent component, String lang) {
    this.realComponent = component;
    this.lang = lang;
  }

  public String getDescription() {
    if (realComponent.getDescription().containsKey(lang)) {
      return realComponent.getDescription().get(lang);
    }
    return realComponent.getDescription().get(DisplayI18NHelper.getDefaultLanguage());
  }

  public String getLabel() {
    if (realComponent.getLabel().containsKey(lang)) {
      return realComponent.getLabel().get(lang);
    }
    return realComponent.getLabel().get(DisplayI18NHelper.getDefaultLanguage());
  }

  public String getName() {
    return realComponent.getName();
  }

  public List<LocalizedProfile> getProfiles() {
    List<LocalizedProfile> localizedProfiles = new ArrayList<>();
    for (Profile profile : realComponent.getProfiles()) {
      localizedProfiles.add(new LocalizedProfile(profile, lang));
    }
    return localizedProfiles;
  }

  public String getRouter() {
    return realComponent.getRouter();
  }

  public String getSuite() {
    if (realComponent.getSuite().containsKey(lang)) {
      return realComponent.getSuite().get(lang);
    }
    return realComponent.getSuite().get(DisplayI18NHelper.getDefaultLanguage());
  }

  public boolean isPortlet() {
    return realComponent.isPortlet();
  }

  public boolean isVisible() {
    return realComponent.isVisible();
  }

  public boolean isVisibleInPersonalSpace() {
    return realComponent.isVisibleInPersonalSpace();
  }
}
