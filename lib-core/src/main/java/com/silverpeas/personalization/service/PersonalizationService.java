/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

import com.silverpeas.personalization.UserMenuDisplay;
import com.silverpeas.personalization.UserPreferences;

public interface PersonalizationService {

  public static final String DEFAULT_LOOK = "Initial";

  public static final UserMenuDisplay DEFAULT_USERMENU_DISPLAY_MODE = UserMenuDisplay.DISABLE;

  public void setFavoriteLanguage(String userId, String languages);

  public String getFavoriteLanguage(String userId);

  public String getFavoriteLook(String userId);

  public void setFavoriteLook(String userId, String look);

  public void setPersonalWorkSpace(String userId, String spaceId);

  public String getPersonalWorkSpace(String userId);

  public void setThesaurusStatus(String userId, boolean thesaurusStatus);

  public boolean getThesaurusStatus(String userId);

  public void setDragAndDropStatus(String userId, boolean dragAndDropStatus);

  public boolean getDragAndDropStatus(String userId);

  public void setWebdavEditingStatus(String userId, boolean webdavEditingStatus);

  public boolean getWebdavEditingStatus(String userId);

  public void saveUserSettings(UserPreferences userPreferences);

  public UserPreferences getUserSettings(String userId);

}