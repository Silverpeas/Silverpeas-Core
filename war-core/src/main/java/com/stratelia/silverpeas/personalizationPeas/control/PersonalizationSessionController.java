/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.personalizationPeas.control;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import com.silverpeas.jobDomainPeas.JobDomainSettings;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.authentication.AuthenticationException;
import com.stratelia.silverpeas.authentication.LoginPasswordAuthentication;
import com.stratelia.silverpeas.notificationManager.NotificationManager;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.PeasCoreException;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/*
 * CVS Informations
 *
 * $Id: PersonalizationSessionController.java,v 1.17 2007/08/01 15:50:56
 * sfariello Exp $
 *
 * $Log: PersonalizationSessionController.java,v $
 * Revision 1.18  2008/05/20 12:32:42  neysseri
 * no message
 *
 * Revision 1.17.4.2  2008/05/06 09:38:59  ehugonnet
 * Ajout du champ webdavEditingStatus
 *
 * Revision 1.17.4.1  2008/05/06 08:14:39  ehugonnet
 * Ajout du champ webdavEditingStatus
 * Revision 1.17 2007/08/01
 * 15:50:56 sfariello Externalisation des langues
 *
 * Revision 1.16 2007/06/12 07:52:26 neysseri no message
 *
 * Revision 1.15.2.1 2007/05/04 10:22:06 cbonin Personnalisation de l'activation
 * de l'applet de drag and drop et de l'active X d'édition de documents Office
 * en ligne
 *
 * Revision 1.14 2007/05/04 09:42:23 cbonin Personnalisation de l'activation de
 * l'applet de drag and drop et de l'active X d'édition de documents Office en
 * ligne
 *
 * Revision 1.13 2007/04/20 14:24:40 neysseri no message
 *
 * Revision 1.12.2.1 2007/03/16 15:53:08 cbonin *** empty log message ***
 *
 * Revision 1.12 2007/02/27 15:42:03 neysseri no message
 *
 * Revision 1.11 2007/02/02 10:25:46 neysseri no message
 *
 * Revision 1.10 2007/01/04 09:32:03 cbonin Modif de la méthode modifyUser pour
 * ajouter les infos custom de l'utilisateur
 *
 * Revision 1.9.2.1 2007/01/29 08:26:15 neysseri no message
 *
 * Revision 1.9 2005/07/25 16:07:15 neysseri Ajout de l'onglet "Identité"
 *
 * Revision 1.8.2.2 2005/07/25 14:25:58 neysseri no message
 *
 * Revision 1.8.2.1 2005/06/02 17:03:37 sdevolder *** empty log message ***
 *
 * Revision 1.8 2004/12/29 09:32:15 dlesimple Modification mot de passe
 *
 * Revision 1.7 2004/12/15 13:45:37 dlesimple Modification mot de passe
 * utilisateur
 *
 * Revision 1.6 2004/11/30 17:01:24 neysseri no message
 *
 * Revision 1.5 2003/07/01 22:50:59 cbonin Enlever les colonnes Type et Usage,
 * faire des messages de confirmation de suppression, bug liste des KMServices
 * en double
 *
 * Revision 1.4 2002/12/20 13:35:16 neysseri no message
 *
 * Revision 1.3 2002/12/19 09:21:40 neysseri ThesaurusInPreference branch
 * merging
 *
 * Revision 1.2.10.1 2002/12/17 15:15:39 dlesimple ThesaurusInPreference
 *
 * Revision 1.2 2002/10/09 07:55:39 neysseri no message
 *
 * Revision 1.1.1.1.6.7 2002/10/04 16:02:17 pbialevich no message
 *
 * Revision 1.1.1.1.6.6 2002/10/04 15:03:37 pbialevich no message
 *
 * Revision 1.1.1.1.6.5 2002/10/04 13:59:52 pbialevich no message
 *
 * Revision 1.1.1.1.6.4 2002/09/28 16:38:36 gshakirin no message
 *
 * Revision 1.1.1.1.6.3 2002/09/28 14:38:21 gshakirin no message
 *
 * Revision 1.1.1.1.6.2 2002/09/27 16:30:54 abudnikau Personalization task
 *
 * Revision 1.1.1.1.6.1 2002/09/27 16:04:36 abudnikau Personalization task
 *
 * Revision 1.1.1.1 2002/08/06 14:47:55 nchaix no message
 *
 * Revision 1.2 2002/03/29 12:16:17 neysseri Avertit le MainSessionController
 * que la langue de l'utilisateur a changé
 *
 * Revision 1.1 2002/01/30 11:07:43 tleroi Move Bus peas to BusIHM
 *
 * Revision 1.1 2002/01/28 14:44:05 tleroi Split clipboard and personalization
 *
 * Revision 1.17 2002/01/22 09:29:38 tleroi Use URLManager
 *
 * Revision 1.16 2002/01/21 10:37:38 tleroi Change EJB management
 *
 * Revision 1.15 2002/01/18 18:04:07 tleroi Centralize URLS + Stabilisation Lot
 * 2 - SilverTrace et Exceptions
 *
 * Revision 1.14 2002/01/16 10:58:05 tleroi Lot 2 Request Routers
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class PersonalizationSessionController extends
    AbstractComponentSessionController {
  private String favoriteLanguage = null;
  private String favoriteLook = null;
  private Boolean thesaurusStatus = null;
  private Boolean dragAndDropStatus = null;
  private Boolean onlineEditingStatus = null;
  private Boolean webdavEditingStatus = null;
  private AdminController m_AdminCtrl = null;
  private NotificationManager notificationManager = null;
  private long domainActions = -1;

  ResourceLocator resources = new ResourceLocator(
      "com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings",
      "");

  /**
   * Constructor declaration
   * 
   * 
   * @param mainSessionCtrl
   * @param componentContext
   * 
   * @see
   */
  public PersonalizationSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(
        mainSessionCtrl,
        componentContext,
        "com.stratelia.silverpeas.personalizationPeas.multilang.personalizationBundle",
        "com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasIcons");
    setComponentRootName(URLManager.CMP_PERSONALIZATION);
    notificationManager = new NotificationManager(getLanguage());
    m_AdminCtrl = new AdminController(getUserId());
  }

  public int getMinLengthPwd() {
    return JobDomainSettings.m_MinLengthPwd;
  }

  public boolean isBlanksAllowedInPwd() {
    return JobDomainSettings.m_BlanksAllowedInPwd;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public synchronized String getFavoriteLanguage() {
    try {
      if (this.favoriteLanguage == null) {
        this.favoriteLanguage = getPersonalization().getFavoriteLanguage();
      }
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.getFavoriteLanguage()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      return getFavoriteLanguage();
    } catch (Exception e) {
      SilverTrace.error("personalizationPeas",
          "PersonalizationSessionController.getFavoriteLanguage()",
          "personalizationPeas.EX_CANT_GET_FAVORITE_LANGUAGE", e);
    }
    return this.favoriteLanguage;
  }

  public synchronized List getAllLanguages() {
    List allLanguages = new ArrayList();
    try {
      StringTokenizer st = new StringTokenizer(
          resources.getString("languages"), ",");
      SilverTrace.debug("personalizationPeas",
          "PersonalizationSessionController.getAllLanguages()", "langues = "
              + resources.getString("languages").toString());
      while (st.hasMoreTokens()) {
        String langue = st.nextToken();
        SilverTrace.debug("personalizationPeas",
            "PersonalizationSessionController.getAllLanguages()", "langue = "
                + langue);
        allLanguages.add(langue);
      }
    } catch (Exception e) {
      SilverTrace.error("personalizationPeas",
          "PersonalizationSessionController.getAllLanguages()",
          "personalizationPeas.EX_CANT_GET_FAVORITE_LANGUAGE", e);
    }
    return allLanguages;
  }

  /**
   * Method declaration
   * 
   * 
   * @param languages
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * 
   * @see
   */
  public void setLanguages(Vector languages) throws PeasCoreException {
    try {
      getPersonalization().setLanguages(languages);
      this.favoriteLanguage = (String) languages.firstElement();

      // Change language in MainSessionController
      setLanguageToMainSessionController(favoriteLanguage);
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setLanguages()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setLanguages(languages);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setLanguages()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_LANGUAGE", e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * 
   * @see
   */
  public Vector getLanguages() throws PeasCoreException {
    try {
      return getPersonalization().getLanguages();
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.getLanguages()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      return getLanguages();
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getLanguages()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_LANGUAGE", e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * 
   * @see
   */
  public String getFavoriteLook() throws PeasCoreException {
    if (this.favoriteLook == null) {
      try {
        if (favoriteLook == null)
          this.favoriteLook = getPersonalization().getFavoriteLook();
      } catch (NoSuchObjectException nsoe) {
        initPersonalization();
        SilverTrace.warn("personalizationPeas",
            "PersonalizationSessionController.getFavoriteLook()",
            "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
        return getFavoriteLook();
      } catch (Exception e) {
        throw new PeasCoreException(
            "PersonalizationSessionController.getFavoriteLook()",
            SilverpeasException.ERROR,
            "personalizationPeas.EX_CANT_GET_FAVORITE_LOOK", e);
      }
    }
    return this.favoriteLook;
  }

  /**
   * Method declaration
   * 
   * 
   * @param look
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * 
   * @see
   */
  public void setFavoriteLook(String look) throws PeasCoreException {
    try {
      getPersonalization().setFavoriteLook(look);
      this.favoriteLook = look;
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setFavoriteLook()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setFavoriteLook(look);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setFavoriteLook()",
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
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setFavoriteLook()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setPersonalWorkSpace(personalWS);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setFavoriteLook()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_FAVORITE_COLLWS", personalWS, e);
    }
  }

  // ******************* Methods for Thesaurus *************************
  public boolean getThesaurusStatus() throws PeasCoreException {
    try {
      if (thesaurusStatus == null)
        thesaurusStatus = new Boolean(getPersonalization().getThesaurusStatus());
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.getThesaurusStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      return getThesaurusStatus();
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getThesaurusStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_THESAURUS_STATUS", e);
    }

    return thesaurusStatus.booleanValue();
  }

  public void setThesaurusStatus(boolean thesaurusStatus)
      throws PeasCoreException {
    try {
      getPersonalization().setThesaurusStatus(thesaurusStatus);
      this.thesaurusStatus = new Boolean(thesaurusStatus);
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setThesaurusStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setThesaurusStatus(thesaurusStatus);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setThesaurusStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_THESAURUS_STATUS", e);
    }
  }

  // ******************* Methods for Drag And Drop *************************
  public boolean getDragAndDropStatus() throws PeasCoreException {
    try {
      if (dragAndDropStatus == null)
        dragAndDropStatus = new Boolean(getPersonalization()
            .getDragAndDropStatus());
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.getDragAndDropStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      return getDragAndDropStatus();
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getDragAndDropStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_DRAGDROP_STATUS", e);
    }

    return dragAndDropStatus.booleanValue();
  }

  public void setDragAndDropStatus(boolean dragAndDropStatus)
      throws PeasCoreException {
    try {
      getPersonalization().setDragAndDropStatus(dragAndDropStatus);
      this.dragAndDropStatus = new Boolean(dragAndDropStatus);
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setDragAndDropStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setDragAndDropStatus(dragAndDropStatus);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setDragAndDropStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_DRAGDROP_STATUS", e);
    }
  }

  // ******************* Methods for Online Editing Office document
  // *************************
  public boolean getOnlineEditingStatus() throws PeasCoreException {
    try {
      if (onlineEditingStatus == null)
        onlineEditingStatus = new Boolean(getPersonalization()
            .getOnlineEditingStatus());
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.getOnlineEditingStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      return getOnlineEditingStatus();
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getOnlineEditingStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_ONLINE_EDITING_STATUS", e);
    }

    return onlineEditingStatus.booleanValue();
  }

  public void setOnlineEditingStatus(boolean onlineEditingStatus)
      throws PeasCoreException {
    try {
      getPersonalization().setOnlineEditingStatus(onlineEditingStatus);
      this.onlineEditingStatus = new Boolean(onlineEditingStatus);
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setOnlineEditingStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setOnlineEditingStatus(onlineEditingStatus);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setOnlineEditingStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_ONLINE_EDITING_STATUS", e);
    }
  }

  // ******************* Methods for Webdav Editing Office / OpenOffice document
  // *************************
  public boolean getWebdavEditingStatus() throws PeasCoreException {
    try {
      if (webdavEditingStatus == null)
        webdavEditingStatus = new Boolean(getPersonalization()
            .getWebdavEditingStatus());
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.getWebdavEditingStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      return getWebdavEditingStatus();
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getWebdavEditingStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_WEBDAV_EDITING_STATUS", e);
    }
    return webdavEditingStatus.booleanValue();
  }

  public void setWebdavEditingStatus(boolean webdavEditingStatus)
      throws PeasCoreException {
    try {
      getPersonalization().setWebdavEditingStatus(webdavEditingStatus);
      this.webdavEditingStatus = new Boolean(webdavEditingStatus);
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      SilverTrace.warn("personalizationPeas",
          "PersonalizationSessionController.setWebdavEditingStatus()",
          "root.EX_CANT_GET_REMOTE_OBJECT", nsoe);
      setWebdavEditingStatus(webdavEditingStatus);
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setWebdavEditingStatus()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_WEBDAV_EDITING_STATUS", e);
    }
  }

  // ******************* Methods for Notification *************************

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws PeasCoreException
   * 
   * @see
   */
  public ArrayList getNotificationAddresses() throws PeasCoreException {
    int userId = Integer.parseInt(getUserId());
    try {
      return notificationManager.getNotificationAddresses(userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getNotificationAddresses()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_NOTIFICATION_ADDRESSES", e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param aNotificationAddressId
   * 
   * @return
   * 
   * @throws PeasCoreException
   * 
   * @see
   */
  public Properties getNotificationAddress(String aNotificationAddressId)
      throws PeasCoreException {
    int id = Integer.parseInt(aNotificationAddressId);
    int userId = Integer.parseInt(getUserId());

    try {
      return notificationManager.getNotificationAddress(id, userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getNotificationAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_NOTIFICATION_ADDRESS", "Id="
              + aNotificationAddressId, e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws PeasCoreException
   * 
   * @see
   */
  public ArrayList getNotifChannels() throws PeasCoreException {
    try {
      return notificationManager.getNotifChannels();
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getNotifChannels()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_NOTIFICATION_CHANNELS", e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param aPreferenceId
   * 
   * @return
   * 
   * @throws PeasCoreException
   * 
   * @see
   */
  public Properties getNotifPreference(String aPreferenceId)
      throws PeasCoreException {
    int id = Integer.parseInt(aPreferenceId);
    int userId = Integer.parseInt(getUserId());

    try {
      return notificationManager.getNotifPreference(id, userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getNotifPreference()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_NOTIFICATION_PREFERENCE", "Id="
              + aPreferenceId, e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws PeasCoreException
   * 
   * @see
   */
  public ArrayList getNotifPreferences() throws PeasCoreException {
    int userId = Integer.parseInt(getUserId());

    try {
      return notificationManager.getNotifPreferences(userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getNotifPreferences()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_NOTIFICATION_PREFERENCES", e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param aNotifAddressId
   * @param aNotifName
   * @param aChannelId
   * @param aAddress
   * @param aUsage
   * 
   * @throws PeasCoreException
   * 
   * @see
   */
  public void saveNotifAddress(String aNotifAddressId, String aNotifName,
      String aChannelId, String aAddress, String aUsage)
      throws PeasCoreException {
    int notifAddressId;

    if (aNotifAddressId.equalsIgnoreCase("")
        || aNotifAddressId.equalsIgnoreCase("null")) {
      notifAddressId = -1;
    } else {
      notifAddressId = Integer.parseInt(aNotifAddressId);
    }
    int userId = Integer.parseInt(getUserId());
    int channelId = Integer.parseInt(aChannelId);

    try {
      notificationManager.saveNotifAddress(notifAddressId, userId, aNotifName,
          channelId, aAddress, aUsage);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.saveNotifAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_NOTIFICATION_ADDRESS",
          "aNotifAddressId=" + aNotifAddressId + "|aNotifName=" + aNotifName
              + "|aChannelId=" + aChannelId + "|aAddress=" + aAddress
              + "|aUsage=" + aUsage, e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param aNotifAddressId
   * 
   * @throws PeasCoreException
   * 
   * @see
   */
  public void deleteNotifAddress(String aNotifAddressId)
      throws PeasCoreException {
    int notifAddressId = Integer.parseInt(aNotifAddressId);

    try {
      notificationManager.deleteNotifAddress(notifAddressId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.deleteNotifAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_DELETE_NOTIFICATION_ADDRESS",
          "aNotifAddressId=" + aNotifAddressId, e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param aPreferenceId
   * 
   * @throws PeasCoreException
   * 
   * @see
   */
  public void deletePreference(String aPreferenceId) throws PeasCoreException {
    try {
      notificationManager.deletePreference(Integer.parseInt(aPreferenceId));
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.deletePreference()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_DELETE_NOTIFICATION_PREFERENCE",
          "aPreferenceId=" + aPreferenceId, e);
    }
  }

  /**
   * Send a test message to the given notification address Id
   * 
   * @param id
   *          of the table ST_NotifAddress row to send notification to.
   */
  public void testNotifAddress(String aNotifAddressId) throws PeasCoreException {
    // Get the current userId
    int userId = Integer.parseInt(getUserId());
    int notifAddressId = Integer.parseInt(aNotifAddressId);

    try {
      notificationManager.testNotifAddress(notifAddressId, userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.testNotifAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_TEST_NOTIFICATION_ADDRESS",
          "aNotifAddressId=" + aNotifAddressId, e);
    }
  }

  /**
   * Get the default Notification address
   */
  public Properties getDefaultAddress() throws PeasCoreException {
    int userId = Integer.parseInt(getUserId());

    try {
      return notificationManager.getNotificationAddress(notificationManager
          .getDefaultAddress(userId), userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getDefaultAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_DEFAULT_ADDRESS", e);
    }
  }

  /**
   * Set the default Notification address Id
   * 
   * @param id
   *          of the table ST_NotifAddress row to send notification to.
   */
  public void setDefaultAddress(String aNotifAddressId)
      throws PeasCoreException {
    // Get the current userId
    int userId = Integer.parseInt(getUserId());
    int notifAddressId = Integer.parseInt(aNotifAddressId);

    try {
      notificationManager.setDefaultAddress(notifAddressId, userId);
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.setDefaultAddress()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_SET_DEFAULT_ADDRESS", "aNotifAddressId="
              + aNotifAddressId, e);
    }
  }

  /**
   * Method declaration
   * 
   * 
   * @param componentId
   * @param priorityId
   * @param notificationId
   * 
   * @throws PeasCoreException
   * 
   * @see
   */
  public void addPreference(String componentId, String priorityId,
      String notificationId) throws PeasCoreException {
    int userId = Integer.parseInt(getUserId());

    try {
      notificationManager.savePreferences(userId,
          Integer.parseInt(componentId), -1, Integer.parseInt(notificationId));
    } catch (NotificationManagerException e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.addPreference()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_ADD_NOTIFICATION_PREFERENCE",
          "componentId=" + componentId + "|priorityId=" + priorityId
              + "|notificationId=" + notificationId, e);
    }
  }

  /**
   * Supprime les doublons
   */
  private ArrayList getDistinctInstanceIds(String[] givenInstancesIds)
      throws PeasCoreException {
    ArrayList instancesIds = new ArrayList();
    String instanceId = null;
    ArrayList intermed = new ArrayList();

    for (int i = 0; i < givenInstancesIds.length; i++) {
      instanceId = givenInstancesIds[i];
      intermed.add(instanceId);
    }

    for (int i = 0; i < givenInstancesIds.length; i++) {
      instanceId = givenInstancesIds[i];
      if (intermed.lastIndexOf((String) instanceId) == i)
        instancesIds.add(instanceId);
    }

    return instancesIds;
  }

  /**
   * Retourne la liste des composants
   */
  public ArrayList getInstanceList() throws PeasCoreException {

    // Liste des instances triés par nom de composants
    ArrayList sortedComponentList;
    // Get the id list of all available Instances for this user
    String[] instancesIds = getUserAvailComponentIds();

    // Create the final ArrayList
    ArrayList ar = new ArrayList(instancesIds.length);

    try {
      // supprime les doublons
      ArrayList arrayInstancesIds = getDistinctInstanceIds(instancesIds);

      String instanceId = null;
      Properties p = null;
      // for each instanceId
      for (int i = 0; i < arrayInstancesIds.size(); i++) {
        instanceId = (String) arrayInstancesIds.get(i);

        // ComponentInst instance =
        // getOrganizationController().getComponentInst(instanceId);
        p = new Properties();

        p.setProperty("instanceId", extractLastNumber(instanceId));
        // p.setProperty("instance", instance.getLabel()) ;
        // p.setProperty("component", instance.getName()) ;
        // p.setProperty("description", instance.getDescription()) ;
        p.setProperty("fullName", notificationManager
            .getComponentFullName(instanceId));
        ar.add(p);
      }
      Properties[] componentList = (Properties[]) ar.toArray(new Properties[0]);

      Arrays.sort(componentList, new Comparator() {

        /**
         * Method declaration
         * 
         * 
         * @param o1
         * @param o2
         * 
         * @return
         * 
         * @see
         */
        public int compare(Object o1, Object o2) {
          return (((Properties) o1).getProperty("fullName"))
              .compareTo(((Properties) o2).getProperty("fullName"));
        }

        /**
         * Method declaration
         * 
         * 
         * @param o
         * 
         * @return
         * 
         * @see
         */
        public boolean equals(Object o) {
          return false;
        }

      });
      sortedComponentList = new ArrayList(componentList.length);

      for (int i = 0; i < componentList.length; i++) {
        Properties pp = new Properties();

        pp.setProperty("name", componentList[i].getProperty("fullName"));
        pp.setProperty("id", componentList[i].getProperty("instanceId"));
        sortedComponentList.add(pp);
      }
      return sortedComponentList;
    } catch (Exception e) {
      throw new PeasCoreException(
          "PersonalizationSessionController.getInstanceList()",
          SilverpeasException.ERROR,
          "personalizationPeas.EX_CANT_GET_INSTANCE_LIST", e);
    }
  }

  /**
   * Extract the last number from the string
   * 
   * @param chaine
   *          The String to clean
   * @return the clean String Example 1 : kmelia47 -> 47 Example 2 : b2b34 -> 34
   * 
   */
  static String extractLastNumber(String chaine) {
    String s = "";

    for (int i = 0; i < chaine.length(); i++) {
      char car = chaine.charAt(i);

      switch (car) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          s = s + car;
          break;
        default:
          s = "";
      }
    }
    return s;
  }

  public String buildOptions(ArrayList ar, String selectValue, String selectText) {
    return buildOptions(ar, selectValue, selectText, false);
  }

  public String buildOptions(ArrayList ar, String selectValue,
      String selectText, boolean bSorted) {
    StringBuffer valret = new StringBuffer();
    Properties elmt = null;
    String selected;
    ArrayList arToDisplay = ar;
    int i;

    if (selectText != null) {
      if ((selectValue == null) || (selectValue.length() <= 0)) {
        selected = "SELECTED";
      } else {
        selected = "";
      }
      valret.append("<option value=\"\" " + selected + ">"
          + EncodeHelper.javaStringToHtmlString(selectText) + "</option>\n");
    }
    if (bSorted) {
      Properties[] theList = (Properties[]) ar.toArray(new Properties[0]);
      Arrays.sort(theList, new Comparator() {
        public int compare(Object o1, Object o2) {
          return (((Properties) o1).getProperty("name")).toUpperCase()
              .compareTo(((Properties) o2).getProperty("name").toUpperCase());
        }

        public boolean equals(Object o) {
          return false;
        }
      });
      arToDisplay = new ArrayList(theList.length);
      for (i = 0; i < theList.length; i++) {
        arToDisplay.add(theList[i]);
      }
    }
    if (arToDisplay != null) {
      for (i = 0; i < arToDisplay.size(); i++) {
        elmt = (Properties) arToDisplay.get(i);
        if (elmt.getProperty("id").equalsIgnoreCase(selectValue)) {
          selected = "SELECTED";
        } else {
          selected = "";
        }
        valret.append("<option value=\"" + elmt.getProperty("id") + "\" "
            + selected + ">"
            + EncodeHelper.javaStringToHtmlString(elmt.getProperty("name"))
            + "</option>\n");
      }
    }
    return valret.toString();
  }

  public List getSpaceTreeview() {
    return getOrganizationController().getSpaceTreeview(getUserId());
  }

  public UserFull getTargetUserFull() {
    UserFull valret = null;
    String IdUserCur = getUserId();

    if ((IdUserCur != null) && (IdUserCur.length() > 0)) {
      valret = getOrganizationController().getUserFull(IdUserCur);
      // if (valret == null)
      // {
      // throw new
      // personalizationPeasException("PersonalizationSessionController.getTargetUserFull()",SilverpeasException.ERROR,"personalizationPeas.EX_USER_NOT_AVAILABLE","UserId="+IdUserCur);
      // }

    }
    return valret;
  }

  public void modifyUser(String idUser, String userLastName,
      String userFirstName, String userEMail, String userAccessLevel,
      String oldPassword, String newPassword, HashMap properties)
      throws PeasCoreException, AuthenticationException {
    UserFull theModifiedUser = null;
    String idRet = null;

    SilverTrace.info("personalizationPeas",
        "PersonalizationPeasSessionController.modifyUser()",
        "root.MSG_GEN_ENTER_METHOD", "UserId=" + idUser + " userLastName="
            + userLastName + " userFirstName=" + userFirstName + " userEMail="
            + userEMail + " userAccessLevel=" + userAccessLevel);

    theModifiedUser = m_AdminCtrl.getUserFull(idUser);
    if (theModifiedUser == null)
      throw new PeasCoreException(
          "PersonalizationPeasSessionController.modifyUser()",
          SilverpeasException.ERROR, "admin.EX_ERR_UNKNOWN_USER");

    if (isUserDomainRW()) {
      theModifiedUser.setLastName(userLastName);
      theModifiedUser.setFirstName(userFirstName);
      theModifiedUser.seteMail(userEMail);

      // Si l'utilisateur n'a pas entré de nouveau mdp, on ne le change pas
      if (newPassword != null && newPassword.length() != 0) {
        // In this case, this method checks if oldPassword and actual password
        // match !
        changePassword(theModifiedUser.getLogin(), oldPassword, newPassword,
            theModifiedUser.getDomainId());

        theModifiedUser.setPassword(newPassword);
      }

      // process extra properties
      Set keys = properties.keySet();
      Iterator iKeys = keys.iterator();
      String key = null;
      String value = null;
      while (iKeys.hasNext()) {
        key = (String) iKeys.next();
        value = (String) properties.get(key);

        theModifiedUser.setValue(key, value);
      }

      idRet = m_AdminCtrl.updateUserFull(theModifiedUser);
      if (idRet == null || idRet.length() <= 0) {
        throw new PeasCoreException(
            "PersonalizationPeasSessionController.modifyUser()",
            SilverpeasException.ERROR, "admin.EX_ERR_UPDATE_USER", "UserId="
                + idUser);
      }
    } else {
      if (newPassword != null && newPassword.length() != 0) {
        changePassword(theModifiedUser.getLogin(), oldPassword, newPassword,
            theModifiedUser.getDomainId());
      }
    }
  }

  private void changePassword(String login, String oldPassword,
      String newPassword, String domainId) throws AuthenticationException {
    LoginPasswordAuthentication auth = new LoginPasswordAuthentication();
    auth.changePassword(login, oldPassword, newPassword, domainId);
  }

  public boolean isUserDomainRW() {
    return (getDomainActions() & AbstractDomainDriver.ACTION_CREATE_USER) != 0;
  }

  public long getDomainActions() {
    if (domainActions == -1) {
      domainActions = m_AdminCtrl.getDomainActions(getUserDetail()
          .getDomainId());
    }
    return domainActions;
  }
}