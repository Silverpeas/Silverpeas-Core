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
package org.silverpeas.core.personalization.service;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.dao.PersonalizationRepository;
import org.silverpeas.core.personalization.notification.UserPreferenceEventNotifier;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.List;


/**
 * Class declaration
 */
@Service
@Singleton
@Transactional
public class DefaultPersonalizationService implements PersonalizationService {

  @Inject
  private PersonalizationRepository personalizationRepository;

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
    final UserPreferences previous =
        Transaction.performInNew(() -> personalizationRepository.getById(userPreferences.getId()));
    personalizationRepository.saveAndFlush(userPreferences);
    if (previous == null) {
      UserPreferenceEventNotifier.get().notifyEventOn(ResourceEvent.Type.CREATION, userPreferences);
    } else {
      UserPreferenceEventNotifier.get()
          .notifyEventOn(ResourceEvent.Type.UPDATE, previous, userPreferences);
    }
  }

  @Override
  public void resetDefaultSpace(String spaceId) {
    List<UserPreferences> prefs = personalizationRepository.findByDefaultSpace(spaceId);
    for (UserPreferences pref : prefs) {
      pref.setPersonalWorkSpaceId(null);
    }
    personalizationRepository.save(prefs);
    personalizationRepository.flush();
  }

  @Override
  public UserPreferences getUserSettings(String userId) {
    UserPreferences preferences = personalizationRepository.getById(userId);
    if (preferences == null) {
      preferences = getDefaultUserSettings(userId);
    }
    return preferences;
  }

  private UserPreferences getDefaultUserSettings(String userId) {
    return new UserPreferences(userId, DisplayI18NHelper.getDefaultLanguage(),
        DisplayI18NHelper.getDefaultZoneId(), DEFAULT_LOOK, "", getDefaultThesaurusStatus(),
        getDefaultDragNDropStatus(), getDefaultWebDAVEditingStatus(), getDefaultMenuDisplay());
  }
}
