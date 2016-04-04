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

import org.silverpeas.core.admin.component.model.Profile;
import org.silverpeas.core.ui.DisplayI18NHelper;

/**
 * @author ehugonnet
 */
public class LocalizedProfile {

  private final Profile realProfile;
  private final String lang;

  LocalizedProfile(Profile realProfile, String lang) {
    this.realProfile = realProfile;
    this.lang = lang;
  }

  public String getName() {
    return realProfile.getName();
  }

  public String getHelp() {
    if (realProfile.getHelp().containsKey(lang)) {
      return realProfile.getHelp().get(lang);
    }
    return realProfile.getHelp().get(DisplayI18NHelper.getDefaultLanguage());
  }

  public String getLabel() {
    if (realProfile.getLabel().containsKey(lang)) {
      return realProfile.getLabel().get(lang);
    }
    return realProfile.getLabel().get(DisplayI18NHelper.getDefaultLanguage());
  }

}
