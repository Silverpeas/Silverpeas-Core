/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.personalization.service;

import java.util.List;

import com.silverpeas.personalization.UserMenuDisplay;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.dao.PersonalizationDetailDao;
import com.silverpeas.ui.DisplayI18NHelper;
import com.stratelia.webactiv.util.ResourceLocator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Class declaration
 *
 * @author
 */
@Service
@Transactional
public class DefaultPersonalizationService implements PersonalizationService {

  @Inject
  private PersonalizationDetailDao dao;
  private static final long serialVersionUID = 6776141343859788723L;
  private final ResourceLocator settings = new ResourceLocator(
      "com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings", "");

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
    return UserMenuDisplay.valueOf(settings.getString("DefaultMenuDisplay",
        DEFAULT_MENU_DISPLAY_MODE.name()));
  }

  @Override
  public void saveUserSettings(UserPreferences userPreferences) {
    dao.saveAndFlush(userPreferences);
  }
  
  public void resetDefaultSpace(String spaceId) {
    List<UserPreferences> prefs = dao.findByDefaultSpace(spaceId);
    for (UserPreferences pref : prefs) {
      pref.setPersonalWorkSpaceId(null);
    }
    dao.save(prefs);
    dao.flush();
  }

  @Override
  public UserPreferences getUserSettings(String userId) {
    UserPreferences preferences = dao.readByPrimaryKey(userId);
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
