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
package com.silverpeas.socialNetwork.myProfil.control;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import com.silverpeas.jobDomainPeas.JobDomainSettings;
import com.silverpeas.socialNetwork.SocialNetworkException;
import com.silverpeas.socialNetwork.relationShip.RelationShipService;
import com.stratelia.silverpeas.authentication.AuthenticationException;
import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.PeasCoreException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 *
 * @author Bensalem Nabil
 */
public class MyProfilSessionController extends AbstractComponentSessionController {

  private AdminController m_AdminCtrl = null;
  private RelationShipService relationShipService = new RelationShipService();
  private long domainActions = -1;
  
  private String favoriteLanguage = null;
  private String favoriteLook = null;
  private Boolean thesaurusStatus = null;
  private Boolean dragAndDropStatus = null;
  private Boolean webdavEditingStatus = null;
  
  ResourceLocator resources = new ResourceLocator(
      "com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings",
      "");

  public MyProfilSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl,
        componentContext,
        "com.silverpeas.socialNetwork.multilang.socialNetworkBundle",
        "com.silverpeas.socialNetwork.settings.socialNetworkIcons",
        "com.silverpeas.socialNetwork.settings.socialNetworkSettings");
    m_AdminCtrl = new AdminController(getUserId());
    
    try {
      thesaurusStatus = getActiveThesaurusByUser();
    } catch (Exception e) {
      thesaurusStatus = false;
    }
  }

  /**
   * get all  RelationShips ids for this user
   * @return:List<String>
   * @param: int myId
   *
   */
  public List<String> getContactsIdsForUser(String userId) {
    try {
      return relationShipService.getMyContactsIds(Integer.parseInt(userId));
    } catch (SQLException ex) {
      SilverTrace.error("MyContactProfilSessionController",
          "MyContactProfilSessionController.getContactsForUser", "", ex);
    }
    return new ArrayList<String>();
  }
/**
 * get this user with full information
 * @param userId
 * @return UserFull
 */
  public UserFull getUserFul(String userId) {
    return this.getOrganizationController().getUserFull(userId);
  }
  /**
   * update the properties of user
   * @param idUser
   * @param properties
   * @throws SocialNetworkException
   */

  public void modifyUser(String idUser, Hashtable<String, String> properties) throws
      SocialNetworkException {
    UserFull theModifiedUser = null;
    String idRet = null;

    SilverTrace.info("personalizationPeas",
        "PersonalizationPeasSessionController.modifyUser()",
        "root.MSG_GEN_ENTER_METHOD", "UserId=" + idUser);

    theModifiedUser = m_AdminCtrl.getUserFull(idUser);
    if (theModifiedUser == null) {
      throw new SocialNetworkException(
          "MyProfilSessionController.modifyUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_USER");
    }
    // process extra properties
    Set<String> keys = properties.keySet();
    Iterator<String> iKeys = keys.iterator();
    String key = null;
    String value = null;
    while (iKeys.hasNext()) {
      key = iKeys.next();
      value = properties.get(key);

      theModifiedUser.setValue(key, value);
    }

    idRet = m_AdminCtrl.updateUserFull(theModifiedUser);
    if (idRet == null || idRet.length() <= 0) {
      throw new SocialNetworkException(
          "MyProfilSessionController.modifyUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", "UserId="
          + idUser);
    }

  }
  
  public boolean isUserDomainRW() {
    return (getDomainActions() & AbstractDomainDriver.ACTION_CREATE_USER) != 0;
  }

  public long getDomainActions() {
    if (domainActions == -1) {
      domainActions = m_AdminCtrl.getDomainActions(getUserDetail().getDomainId());
    }
    return domainActions;
  }
  
  public int getMinLengthPwd() {
    return JobDomainSettings.m_MinLengthPwd;
  }

  public boolean isBlanksAllowedInPwd() {
    return JobDomainSettings.m_BlanksAllowedInPwd;
  }
  
  public void modifyUser(String idUser,
      String userLastName,
      String userFirstName,
      String userEMail,
      String userAccessLevel,
      String oldPassword,
      String newPassword,
      String userLoginQuestion,
      String userLoginAnswer,
      HashMap<String, String> properties)
      throws AuthenticationException {
    UserFull theModifiedUser = null;

    SilverTrace.info("personalizationPeas",
        "PersonalizationPeasSessionController.modifyUser()",
        "root.MSG_GEN_ENTER_METHOD", "UserId=" + idUser + " userLastName="
        + userLastName + " userFirstName=" + userFirstName + " userEMail="
        + userEMail + " userAccessLevel=" + userAccessLevel);

    theModifiedUser = m_AdminCtrl.getUserFull(idUser);

    if (isUserDomainRW()) {
      theModifiedUser.setLastName(userLastName);
      theModifiedUser.setFirstName(userFirstName);
      theModifiedUser.seteMail(userEMail);
      theModifiedUser.setLoginQuestion(userLoginQuestion);
      theModifiedUser.setLoginAnswer(userLoginAnswer);

      // Si l'utilisateur n'a pas entré de nouveau mdp, on ne le change pas
      if (newPassword != null && newPassword.length() != 0) {
        // In this case, this method checks if oldPassword and actual password
        // match !
        changePassword(theModifiedUser.getLogin(), oldPassword, newPassword,
            theModifiedUser.getDomainId());

        theModifiedUser.setPassword(newPassword);
      }

      // process extra properties
      Set<String> keys = properties.keySet();
      Iterator<String> iKeys = keys.iterator();
      String key = null;
      String value = null;
      while (iKeys.hasNext()) {
        key = iKeys.next();
        value = properties.get(key);

        theModifiedUser.setValue(key, value);
      }

      m_AdminCtrl.updateUserFull(theModifiedUser);

    } else {
      if (newPassword != null && newPassword.length() != 0) {
        changePassword(theModifiedUser.getLogin(), oldPassword, newPassword,
            theModifiedUser.getDomainId());
      }
    }
  }
  
  private void changePassword(String login,
      String oldPassword,
      String newPassword,
      String domainId) throws AuthenticationException {
    LoginPasswordAuthentication auth = new LoginPasswordAuthentication();
    auth.changePassword(login, oldPassword, newPassword, domainId);
  }
  
  /**
   * Method declaration
   * @return
   * @see
   */
  public synchronized String getFavoriteLanguage() {
    try {
      if (favoriteLanguage == null) {
        favoriteLanguage = getPersonalization().getFavoriteLanguage();
      }
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      return getFavoriteLanguage();
    } catch (Exception e) {
      SilverTrace.error("personalizationPeas",
          "MyProfileSessionController.getFavoriteLanguage()",
          "personalizationPeas.EX_CANT_GET_FAVORITE_LANGUAGE", e);
    }
    return this.favoriteLanguage;
  }

  public synchronized List<String> getAllLanguages() {
    List<String> allLanguages = new ArrayList<String>();
    try {
      StringTokenizer st = new StringTokenizer(
          resources.getString("languages"), ",");
      while (st.hasMoreTokens()) {
        allLanguages.add(st.nextToken());
      }
    } catch (Exception e) {
      SilverTrace.error("personalizationPeas",
          "MyProfileSessionController.getAllLanguages()",
          "personalizationPeas.EX_CANT_GET_FAVORITE_LANGUAGE", e);
    }
    return allLanguages;
  }

  /**
   * Method declaration
   * @param languages
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public void setLanguages(List<String> languages) throws PeasCoreException {
    try {
      getPersonalization().setLanguages(new Vector<String>(languages));
      favoriteLanguage = languages.get(0);

      // Change language in MainSessionController
      setLanguageToMainSessionController(favoriteLanguage);
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      setLanguages(languages);
    } catch (Exception e) {
      throw new PeasCoreException(
          "MyProfileSessionController.setLanguages()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_LANGUAGE", e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public List<String> getLanguages() throws PeasCoreException {
    try {
      return getPersonalization().getLanguages();
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      return getLanguages();
    } catch (Exception e) {
      throw new PeasCoreException(
          "MyProfileSessionController.getLanguages()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_LANGUAGE", e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public String getFavoriteLook() throws PeasCoreException {
    if (favoriteLook == null) {
      try {
          favoriteLook = getPersonalization().getFavoriteLook();
      } catch (NoSuchObjectException nsoe) {
        initPersonalization();
        return getFavoriteLook();
      } catch (Exception e) {
        throw new PeasCoreException(
            "MyProfileSessionController.getFavoriteLook()",
            SilverpeasException.ERROR,
            "personalizationPeas.EX_CANT_GET_FAVORITE_LOOK", e);
      }
    }
    return favoriteLook;
  }

  /**
   * Method declaration
   * @param look
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public void setFavoriteLook(String look) throws PeasCoreException {
    try {
      getPersonalization().setFavoriteLook(look);
      favoriteLook = look;
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      setFavoriteLook(look);
    } catch (Exception e) {
      throw new PeasCoreException(
          "MyProfileSessionController.setFavoriteLook()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_FAVORITE_LOOK", "Look=" + look, e);
    }
  }

  public void setPersonalWorkSpace(String personalWS) throws PeasCoreException {
    try {
      getPersonalization().setPersonalWorkSpace(personalWS);

      // Change language in MainSessionController
      setFavoriteSpaceToMainSessionController(personalWS);
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      setPersonalWorkSpace(personalWS);
    } catch (Exception e) {
      throw new PeasCoreException(
          "MyProfileSessionController.setFavoriteLook()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_FAVORITE_COLLWS", personalWS, e);
    }
  }

  // ******************* Methods for Thesaurus *************************
  private synchronized boolean getActiveThesaurusByUser() throws PeasCoreException, RemoteException {
    try {
      return getPersonalization().getThesaurusStatus();
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      return getPersonalization().getThesaurusStatus();
    } catch (Exception e) {
      throw new PeasCoreException("PdcSearchSessionController.getActiveThesaurusByUser()",
          SilverpeasException.ERROR, "personalizationPeas.EX_CANT_GET_THESAURUS_STATUS", "", e);
    }
  }
  
  public boolean getThesaurusStatus() {
    return thesaurusStatus;
  }

  public void setThesaurusStatus(boolean thesaurusStatus)
      throws PeasCoreException {
    try {
      getPersonalization().setThesaurusStatus(thesaurusStatus);
      this.thesaurusStatus = thesaurusStatus;
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      setThesaurusStatus(thesaurusStatus);
    } catch (Exception e) {
      throw new PeasCoreException(
          "MyProfileSessionController.setThesaurusStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_THESAURUS_STATUS", e);
    }
  }

  // ******************* Methods for Drag And Drop *************************
  public boolean getDragAndDropStatus() throws PeasCoreException {
    try {
      if (dragAndDropStatus == null) {
        dragAndDropStatus = getPersonalization().getDragAndDropStatus();
      }
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      return getDragAndDropStatus();
    } catch (Exception e) {
      throw new PeasCoreException(
          "MyProfileSessionController.getDragAndDropStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_DRAGDROP_STATUS", e);
    }

    return dragAndDropStatus.booleanValue();
  }

  public void setDragAndDropStatus(boolean dragAndDropStatus)
      throws PeasCoreException {
    try {
      getPersonalization().setDragAndDropStatus(dragAndDropStatus);
      this.dragAndDropStatus = dragAndDropStatus;
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      setDragAndDropStatus(dragAndDropStatus);
    } catch (Exception e) {
      throw new PeasCoreException(
          "MyProfileSessionController.setDragAndDropStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_DRAGDROP_STATUS", e);
    }
  }

  // ******************* Methods for Webdav Editing Office / OpenOffice document
  // *************************
  public boolean getWebdavEditingStatus() throws PeasCoreException {
    try {
      if (webdavEditingStatus == null) {
        webdavEditingStatus = getPersonalization().getWebdavEditingStatus();
      }
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      return getWebdavEditingStatus();
    } catch (Exception e) {
      throw new PeasCoreException(
          "MyProfileSessionController.getWebdavEditingStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_WEBDAV_EDITING_STATUS", e);
    }
    return webdavEditingStatus.booleanValue();
  }

  public void setWebdavEditingStatus(boolean webdavEditingStatus)
      throws PeasCoreException {
    try {
      getPersonalization().setWebdavEditingStatus(webdavEditingStatus);
      this.webdavEditingStatus = webdavEditingStatus;
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      setWebdavEditingStatus(webdavEditingStatus);
    } catch (Exception e) {
      throw new PeasCoreException(
          "MyProfileSessionController.setWebdavEditingStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_WEBDAV_EDITING_STATUS", e);
    }
  }
  
  public List<SpaceInstLight> getSpaceTreeview() {
    return getOrganizationController().getSpaceTreeview(getUserId());
  }
  
}