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
package com.stratelia.webactiv.personalization.control.ejb;

import com.silverpeas.personalization.dao.PersonalizationDetailDao;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.personalization.model.PersonalizeDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import org.springframework.transaction.annotation.Transactional;

import static com.silverpeas.ui.DisplayI18NHelper.getDefaultLanguage;

/**
 * Class declaration
 *
 * @author
 */
@Service
@Transactional
public class PersonalizationBmEJB implements PersonalizationBmBusinessSkeleton {

  public static final String DEFAULT_LOOK = "Initial";
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
   * @param userId 
   * @param language
   */
  @Override
  public void setLanguages(String userId, String language) {
    PersonalizeDetail user = dao.readByPrimaryKey(userId);
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
  public String getLanguages(String userId) {
    PersonalizeDetail user = dao.readByPrimaryKey(userId);
    String language = null;

    if (user != null) {
      return user.getLanguage();
    }
    language = DisplayI18NHelper.getDefaultLanguage();
    user = new PersonalizeDetail(userId, language, DEFAULT_LOOK, "", false, false, false,
        getDefaultWebDAVEditingStatus());
    dao.saveAndFlush(user);
    return language;
  }

  @Override
  @Transactional(readOnly = true)
  public String getFavoriteLanguage(String userId) {
    String languages = getLanguages(userId);
    String favoriteLanguage = getDefaultLanguage();
    if (StringUtil.isDefined(languages)) {
      favoriteLanguage = languages;
    }
    return favoriteLanguage;
  }

  @Override
  @Transactional(readOnly = true)
  public String getFavoriteLook(String userId) {
    String favoriteLook = DEFAULT_LOOK;
    PersonalizeDetail personalizeDetail = dao.readByPrimaryKey(userId);

    if (personalizeDetail != null && personalizeDetail.getLook() != null) {
      favoriteLook = personalizeDetail.getLook();
    }

    return favoriteLook;
  }

  @Override
  public void setFavoriteLook(String userId, String look) {
    PersonalizeDetail user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = new PersonalizeDetail(userId, DisplayI18NHelper.getDefaultLanguage(), look, "",
          false, false, false, getDefaultWebDAVEditingStatus());
    } else {
      user.setLook(look);
    }
    dao.saveAndFlush(user);
  }

  @Override
  @Transactional(readOnly = true)
  public String getPersonalWorkSpace(String userId) {
    PersonalizeDetail personalizeDetail = dao.readByPrimaryKey(userId);
    if (personalizeDetail != null) {
      return personalizeDetail.getCollaborativeWorkSpaceId();
    }
    return "";
  }

  @Override
  public void setPersonalWorkSpace(String userId, String spaceId) {
    PersonalizeDetail user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = new PersonalizeDetail(userId, DisplayI18NHelper.getDefaultLanguage(), DEFAULT_LOOK,
          spaceId,
          false, false, false, getDefaultWebDAVEditingStatus());
    } else {
      user.setCollaborativeWorkSpaceId(spaceId);
    }
    dao.saveAndFlush(user);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean getThesaurusStatus(String userId) {
    PersonalizeDetail personalizeDetail = dao.readByPrimaryKey(userId);
    if (personalizeDetail != null) {
      return personalizeDetail.getThesaurusStatus();
    }
    return false;
  }

  @Override
  public void setThesaurusStatus(String userId, boolean thesaurusStatus) {
    PersonalizeDetail user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = new PersonalizeDetail(userId, DisplayI18NHelper.getDefaultLanguage(), DEFAULT_LOOK,
          "", thesaurusStatus, false, false, getDefaultWebDAVEditingStatus());
    } else {
      user.setThesaurusStatus(thesaurusStatus);
    }
    dao.saveAndFlush(user);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean getDragAndDropStatus(String userId) {
    PersonalizeDetail personalizeDetail = dao.readByPrimaryKey(userId);
    if (personalizeDetail != null) {
      return personalizeDetail.getDragAndDropStatus();
    }
    return false;
  }

  @Override
  public void setDragAndDropStatus(String userId, boolean dragAndDropStatus) {
    PersonalizeDetail user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = new PersonalizeDetail(userId, DisplayI18NHelper.getDefaultLanguage(), DEFAULT_LOOK,
          "", false, dragAndDropStatus, false, getDefaultWebDAVEditingStatus());
    } else {
      user.setDragAndDropStatus(dragAndDropStatus);
    }
    dao.saveAndFlush(user);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean getOnlineEditingStatus(String userId) {
    PersonalizeDetail personalizeDetail = dao.readByPrimaryKey(userId);
    if (personalizeDetail != null) {
      return personalizeDetail.getOnlineEditingStatus();
    }
    return false;
  }

  @Override
  public void setOnlineEditingStatus(String userId, boolean onlineEditingStatus) {
    PersonalizeDetail user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = new PersonalizeDetail(userId, DisplayI18NHelper.getDefaultLanguage(), DEFAULT_LOOK,
          "", false, false, onlineEditingStatus, getDefaultWebDAVEditingStatus());
    } else {
      user.setOnlineEditingStatus(onlineEditingStatus);
    }
    dao.saveAndFlush(user);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean getWebdavEditingStatus(String userId) {
    PersonalizeDetail personalizeDetail = dao.readByPrimaryKey(userId);
    if (personalizeDetail != null) {
      return personalizeDetail.isWebdavEditingStatus();
    }
    return getDefaultWebDAVEditingStatus();
  }

  @Override
  public void setWebdavEditingStatus(String userId, boolean webdavEditingStatus) {
    PersonalizeDetail user = dao.readByPrimaryKey(userId);
    if (user == null) {
      user = new PersonalizeDetail(userId, DisplayI18NHelper.getDefaultLanguage(), DEFAULT_LOOK,
          "", false, false, false, webdavEditingStatus);
    } else {
      user.setWebdavEditingStatus(webdavEditingStatus);
    }
    dao.saveAndFlush(user);
  }
}
