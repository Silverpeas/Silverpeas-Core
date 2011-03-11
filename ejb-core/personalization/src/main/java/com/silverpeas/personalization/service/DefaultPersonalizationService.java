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
  private ResourceLocator settings = new ResourceLocator(
      "com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings", "");

  private boolean getDefaultWebDAVEditingStatus() {
    return settings.getBoolean("DefaultWebDAVEditingStatus", true);
  }

  /**
   * Update the favorite language of the user.
   *
   * @param userId
   * @param language
   */
  @Override
  public void setFavoriteLanguage(String userId, String language) {
    UserPreferences user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = getDefaultUserSettings(userId);
    }
    user.setLanguage(language);
    dao.saveAndFlush(user);
  }

  /**
   * Method declaration
   *
   * @param userId
   * @return
   * @see
   */
  @Override
  @Transactional(readOnly = true)
  public String getFavoriteLanguage(String userId) {
    UserPreferences user = dao.readByPrimaryKey(userId);
    if (user != null) {
      return user.getLanguage();
    }
    user = new UserPreferences(userId, DisplayI18NHelper.getDefaultLanguage(), DEFAULT_LOOK, "",
        false,
        false, false, getDefaultWebDAVEditingStatus());
    dao.saveAndFlush(user);
    return user.getLanguage();
  }

  @Override
  @Transactional(readOnly = true)
  public String getFavoriteLook(String userId) {
    String favoriteLook = DEFAULT_LOOK;
    UserPreferences userPreferences = dao.readByPrimaryKey(userId);
    if (userPreferences != null && userPreferences.getLook() != null) {
      favoriteLook = userPreferences.getLook();
    }
    return favoriteLook;
  }

  @Override
  public void setFavoriteLook(String userId, String look) {
    UserPreferences user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = getDefaultUserSettings(userId);
    }
    user.setLook(look);
    dao.saveAndFlush(user);
  }

  @Override
  @Transactional(readOnly = true)
  public String getPersonalWorkSpace(String userId) {
    UserPreferences userPreferences = dao.readByPrimaryKey(userId);
    if (userPreferences != null) {
      return userPreferences.getCollaborativeWorkSpaceId();
    }
    return "";
  }

  @Override
  public void setPersonalWorkSpace(String userId, String spaceId) {
    UserPreferences user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = getDefaultUserSettings(userId);
    }
    user.setCollaborativeWorkSpaceId(spaceId);
    dao.saveAndFlush(user);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean getThesaurusStatus(String userId) {
    UserPreferences userPreferences = dao.readByPrimaryKey(userId);
    if (userPreferences != null) {
      return userPreferences.isThesaurusEnabled();
    }
    return false;
  }

  @Override
  public void setThesaurusStatus(String userId, boolean thesaurusStatus) {
    UserPreferences user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = getDefaultUserSettings(userId);
    }
    user.enableThesaurus(thesaurusStatus);
    dao.saveAndFlush(user);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean getDragAndDropStatus(String userId) {
    UserPreferences userPreferences = dao.readByPrimaryKey(userId);
    if (userPreferences != null) {
      return userPreferences.isDragAndDropEnabled();
    }
    return false;
  }

  @Override
  public void setDragAndDropStatus(String userId, boolean dragAndDropStatus) {
    UserPreferences user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = getDefaultUserSettings(userId);
    }
    user.enableDragAndDrop(dragAndDropStatus);

    dao.saveAndFlush(user);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean getOnlineEditingStatus(String userId) {
    UserPreferences userPreferences = dao.readByPrimaryKey(userId);
    if (userPreferences != null) {
      return userPreferences.isOnlineEditionEnalbled();
    }
    return false;
  }

  @Override
  public void setOnlineEditingStatus(String userId, boolean onlineEditingStatus) {
    UserPreferences user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = getDefaultUserSettings(userId);
    }
    user.enableOnlineEdition(onlineEditingStatus);
    dao.saveAndFlush(user);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean getWebdavEditingStatus(String userId) {
    UserPreferences userPreferences = dao.readByPrimaryKey(userId);
    if (userPreferences != null) {
      return userPreferences.isWebdavEditionEnabled();
    }
    return getDefaultWebDAVEditingStatus();
  }

  @Override
  public void setWebdavEditingStatus(String userId, boolean webdavEditingStatus) {
    UserPreferences user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = getDefaultUserSettings(userId);
    }
    user.enableWebdavEdition(webdavEditingStatus);
    dao.saveAndFlush(user);
  }

  @Override
  public void saveUserSettings(UserPreferences userPreferences) {
    try {
      dao.saveAndFlush(userPreferences);
    } catch (Throwable t) {
      t.printStackTrace();
      printTrace(t);
    }
  }

  private void printTrace(Throwable t) {
    if (t instanceof java.sql.BatchUpdateException) {
      ((java.sql.BatchUpdateException) t).getNextException().printStackTrace();
    } else {
      printTrace(t.getCause());
    }
  }

  @Override
  public UserPreferences getUserSettings(String userId) {
    return dao.readByPrimaryKey(userId);
  }

  private UserPreferences getDefaultUserSettings(String userId) {
    return new UserPreferences(userId, DisplayI18NHelper.getDefaultLanguage(), DEFAULT_LOOK, "",
        false,
        false, false, getDefaultWebDAVEditingStatus());
  }
}
