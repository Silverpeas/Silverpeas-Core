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

package org.silverpeas.core.personalization.service;

import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.dao.PersonalizationManager;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.List;


/**
 * Class declaration
 */
@Singleton
@Transactional
public class DefaultPersonalizationService implements PersonalizationService {

  @Inject
  private PersonalizationManager personalizationManager;

  private final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.personalization.settings.personalizationPeasSettings");

  private boolean getDefaultWebDAVEditingStatus() {
    return settings.getBoolean("DefaultWebDAVEditingStatus", true);
  }

  private boolean getDefaultDragNDropStatus() {
    return settings.getBoolean("DefaultDragNDropStatus", true);
  }

  private boolean getDefaultThesaurusStatus() {
    return settings.getBoolean("DefaultThesaurusStatus", false);
  }

  private UserMenuDisplay getDefaultMenuDisplay() {
    return UserMenuDisplay
        .valueOf(settings.getString("DefaultMenuDisplay", DEFAULT_MENU_DISPLAY_MODE.name()));
  }

  @Override
  public void saveUserSettings(UserPreferences userPreferences) {
    personalizationManager.saveAndFlush(userPreferences);
  }

  @Override
  public void resetDefaultSpace(String spaceId) {
    List<UserPreferences> prefs = personalizationManager.findByDefaultSpace(spaceId);
    for (UserPreferences pref : prefs) {
      pref.setPersonalWorkSpaceId(null);
    }
    personalizationManager.save(prefs);
    personalizationManager.flush();
  }

  @Override
  public UserPreferences getUserSettings(String userId) {
    UserPreferences preferences = personalizationManager.getById(userId);
    if (preferences == null) {
      preferences = getDefaultUserSettings(userId);
    }
    return preferences;
  }

  private UserPreferences getDefaultUserSettings(String userId) {
    return new UserPreferences(userId, DisplayI18NHelper.getDefaultLanguage(), DEFAULT_LOOK, "",
        getDefaultThesaurusStatus(), getDefaultDragNDropStatus(), getDefaultWebDAVEditingStatus(),
        getDefaultMenuDisplay());
  }
}
