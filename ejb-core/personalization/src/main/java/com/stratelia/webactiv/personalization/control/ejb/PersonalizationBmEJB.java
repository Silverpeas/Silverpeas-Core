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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import java.util.*;
import javax.ejb.*;
import javax.naming.NamingException;
import java.sql.*;
import java.rmi.RemoteException;
import com.stratelia.webactiv.util.*;

import com.stratelia.webactiv.util.exception.*;
import com.stratelia.silverpeas.silvertrace.*;

import com.stratelia.webactiv.personalization.model.PersonalizeDetail;

/*
 * CVS Informations
 *
 * $Id: PersonalizationBmEJB.java,v 1.8 2008/05/20 12:31:58 neysseri Exp $
 *
 * $Log: PersonalizationBmEJB.java,v $
 * Revision 1.8  2008/05/20 12:31:58  neysseri
 * no message
 *
 * Revision 1.7.2.1  2008/05/13 08:01:18  ehugonnet
 * Ajout de la possibilité de configurer l'edition en ligne openoffice
 * Revision 1.7 2007/06/12 07:52:18 neysseri
 * no message
 *
 * Revision 1.6.2.1 2007/05/04 09:41:18 cbonin Personnalisation de l'activation
 * de l'applet de drag and drop et de l'active X d'édition de documents Office
 * en ligne
 *
 * Revision 1.6 2005/10/05 16:00:42 neysseri no message
 *
 * Revision 1.5 2003/11/10 16:05:13 neysseri Add some traces on clipboard calls
 *
 * Revision 1.4 2002/12/23 07:28:33 tleroi *** empty log message ***
 *
 * Revision 1.3 2002/12/19 09:21:32 neysseri ThesaurusInPreference branch
 * merging
 *
 * Revision 1.2.10.1 2002/12/17 15:14:06 dlesimple ThesaurusInPreference
 *
 * Revision 1.2 2002/10/09 07:41:03 neysseri no message
 *
 * Revision 1.1.1.1.6.4 2002/10/04 11:41:08 pbialevich throw fixed
 *
 * Revision 1.1.1.1.6.3 2002/09/28 16:38:16 gshakirin no message
 *
 * Revision 1.1.1.1.6.2 2002/09/28 14:37:13 gshakirin no message
 *
 * Revision 1.1.1.1.6.1 2002/09/27 16:06:00 abudnikau Personalization task
 *
 * Revision 1.1.1.1 2002/08/06 14:47:52 nchaix no message
 *
 * Revision 1.5 2002/01/28 14:36:32 tleroi Split clipboard and personalization
 *
 * Revision 1.6 2002/01/18 18:20:56 tleroi Centralize URLS + Stabilisation Lot 2 -
 * SilverTrace et Exceptions
 *
 * Revision 1.5 2002/01/18 18:04:07 tleroi Centralize URLS + Stabilisation Lot 2 -
 * SilverTrace et Exceptions
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class PersonalizationBmEJB implements PersonalizationBmBusinessSkeleton,
    SessionBean {

  private String currentUserId;
  private String defaultLanguage = null;
  private String defaultLook = "Initial";
  private String defaultPersonalWSId = null;
  private boolean defaultThesaurusStatus = false;
  private boolean defaultDragDropStatus = true;
  private boolean defaultOnlineEditingStatus = true;
  private boolean defaultWebdavEditingStatus = false;
  private ResourceLocator settings = null;

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public PersonalizationBmEJB() {
  }

  /**
   * Method declaration
   * 
   * 
   * @param user
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void setActor(String userId) throws RemoteException {
    this.currentUserId = userId;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  private Connection getConnection() {
    try {
      Connection con = DBUtil
          .makeConnection(JNDINames.PERSONALIZATION_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED",
          "Datasource=" + JNDINames.PERSONALIZATION_DATASOURCE, e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("personalization",
            "PersonalizationBmEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  private String getDefaultLanguage() {
    if (settings == null)
      settings = new ResourceLocator(
          "com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings",
          "");

    if (settings != null && defaultLanguage == null)
      defaultLanguage = settings.getString("DefaultLanguage", "fr");

    if (defaultLanguage == null)
      defaultLanguage = "fr";

    return defaultLanguage;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * 
   * @see
   */
  private PersonalizeDetail getPersonalizeDetail() throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getPersonalizeDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    PersonalizeDetail personalizeDetail = null;

    try {
      personalizeDetail = PersonalizationDAO.getPersonalizeDetail(con,
          this.currentUserId);
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.getPersonalizeDetail()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_GET_PERSONALIZE_DETAIL", e);
    } finally {
      freeConnection(con);
    }
    if (personalizeDetail != null) {
      SilverTrace.info("personalization",
          "PersonalizationBmEJB.getPersonalizeDetail()",
          "root.MSG_GEN_PARAM_VALUE", "thesaurusStatus = "
              + personalizeDetail.getThesaurusStatus());
    }
    return personalizeDetail;
  }

  /**
   * Method declaration
   * 
   * 
   * @param languages
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * 
   * @see
   */
  public void setLanguages(Vector languages) throws RemoteException {
    SilverTrace.info("personalization", "PersonalizationBmEJB.setLanguages()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();

    try {
      PersonalizationDAO.setLanguages(con, this.currentUserId, languages);
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.setLanguages()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_SET_LANGUAGE", e);
    } finally {
      freeConnection(con);
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
   * @throws SQLException
   * 
   * @see
   */
  public Vector getLanguages() throws RemoteException {
    SilverTrace.info("personalization", "PersonalizationBmEJB.getLanguages()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    Vector languages = null;

    try {
      PersonalizeDetail personalizeDetail = getPersonalizeDetail();

      if (personalizeDetail != null) {
        languages = personalizeDetail.getLanguages();
      } else {
        // the user has not defined favorite language
        languages = new Vector();
        languages.add(getDefaultLanguage());

        // insert a new record in database
        PersonalizationDAO.insertPersonalizeDetail(con, this.currentUserId,
            languages, this.defaultLook, defaultPersonalWSId,
            this.defaultThesaurusStatus, this.defaultDragDropStatus,
            this.defaultOnlineEditingStatus, this.defaultWebdavEditingStatus);
      }
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.getLanguages()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_GET_LANGUAGE", e);
    } finally {
      freeConnection(con);
    }
    return languages;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * 
   * @see
   */
  public String getFavoriteLanguage() throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getFavoriteLanguage()",
        "root.MSG_GEN_ENTER_METHOD");
    Vector languages = getLanguages();
    String favoriteLanguage = getDefaultLanguage();

    if (languages != null) {
      if (!languages.isEmpty()) {
        favoriteLanguage = (String) languages.firstElement();
      }
    }
    return favoriteLanguage;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * 
   * @see
   */
  public String getFavoriteLook() throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getFavoriteLook()", "root.MSG_GEN_ENTER_METHOD");
    String favoriteLook = defaultLook;
    Connection con = getConnection();

    try {
      PersonalizeDetail personalizeDetail = getPersonalizeDetail();

      if (personalizeDetail != null) {
        if (personalizeDetail.getLook() != null) {
          favoriteLook = personalizeDetail.getLook();
        }
      }
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.getFavoriteLook()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_GET_FAVORITE_LOOK", e);
    } finally {
      freeConnection(con);
    }
    return favoriteLook;
  }

  /**
   * Method declaration
   * 
   * 
   * @param look
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * 
   * @see
   */
  public void setFavoriteLook(String look) throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.setFavoriteLook()", "root.MSG_GEN_ENTER_METHOD",
        "look = " + look);
    Connection con = getConnection();

    try {
      PersonalizeDetail personalizeDetail = getPersonalizeDetail();
      PersonalizationDAO.setFavoriteLook(con, this.currentUserId, look);
      personalizeDetail.setLook(look);
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.setFavoriteLook()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_SET_FAVORITE_LOOK", "Look=" + look, e);
    } finally {
      freeConnection(con);
    }
  }

  public void setPersonalWorkSpace(String spaceId) throws RemoteException {
    Connection con = getConnection();
    try {
      PersonalizationDAO.setPersonalWorkSpace(con, this.currentUserId, spaceId);
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.setPersonalWorkSpace()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_SET_PERSONALWORKSPACE", e);
    } finally {
      freeConnection(con);
    }
  }

  public String getPersonalWorkSpace() throws RemoteException {
    Connection con = getConnection();
    String wsId = null;

    try {
      PersonalizeDetail personalizeDetail = getPersonalizeDetail();

      if (personalizeDetail != null) {
        wsId = personalizeDetail.getPersonalWorkSpaceId();
      }
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.getPersonalWorkSpace()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_GET_PERSONALWORKSPACE", e);
    } finally {
      freeConnection(con);
    }
    return wsId;

  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * 
   * @see
   */
  public boolean getThesaurusStatus() throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getThesaurusStatus()",
        "root.MSG_GEN_ENTER_METHOD");
    boolean thesaurusStatus = defaultThesaurusStatus;
    Connection con = getConnection();

    try {
      PersonalizeDetail personalizeDetail = getPersonalizeDetail();
      if (personalizeDetail != null)
        thesaurusStatus = personalizeDetail.getThesaurusStatus();
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.getThesaurusStatus()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_GET_THESAURUS_STATUS", e);
    } finally {
      freeConnection(con);
    }
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getThesaurusStatus()",
        "root.MSG_GEN_PARAM_VALUE", "thesaurusStatus = " + thesaurusStatus);
    return thesaurusStatus;
  }

  /**
   * Method declaration
   * 
   * 
   * @param thesaurusStatus
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * 
   * @see
   */
  public void setThesaurusStatus(boolean thesaurusStatus)
      throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.setThesaurusStatus()",
        "root.MSG_GEN_PARAM_VALUE", "thesaurusStatus = " + thesaurusStatus);
    Connection con = getConnection();

    try {
      PersonalizationDAO.setThesaurusStatus(con, this.currentUserId,
          thesaurusStatus);
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.setThesaurusStatus()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_SET_THESAURUS_STATUS", "thesaurusStatus="
              + thesaurusStatus, e);
    } finally {
      freeConnection(con);
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
   * @throws SQLException
   * 
   * @see
   */
  public boolean getDragAndDropStatus() throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getDragAndDropStatus()",
        "root.MSG_GEN_ENTER_METHOD");
    boolean dragAndDropStatus = defaultDragDropStatus;
    Connection con = getConnection();

    try {
      PersonalizeDetail personalizeDetail = getPersonalizeDetail();
      if (personalizeDetail != null)
        dragAndDropStatus = personalizeDetail.getDragAndDropStatus();
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.getDragAndDropStatus()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_GET_DRAGDROP_STATUS", e);
    } finally {
      freeConnection(con);
    }
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getDragAndDropStatus()",
        "root.MSG_GEN_PARAM_VALUE", "dragAndDropStatus = " + dragAndDropStatus);
    return dragAndDropStatus;
  }

  /**
   * Method declaration
   * 
   * 
   * @param dragAndDropStatus
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * 
   * @see
   */
  public void setDragAndDropStatus(boolean dragAndDropStatus)
      throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.setDragAndDropStatus()",
        "root.MSG_GEN_PARAM_VALUE", "dragAndDropStatus = " + dragAndDropStatus);
    Connection con = getConnection();

    try {
      PersonalizationDAO.setDragAndDropStatus(con, this.currentUserId,
          dragAndDropStatus);
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.setDragAndDropStatus()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_SET_DRAGDROP_STATUS", "dragAndDropStatus="
              + dragAndDropStatus, e);
    } finally {
      freeConnection(con);
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
   * @throws SQLException
   * 
   * @see
   */
  public boolean getOnlineEditingStatus() throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getOnlineEditingStatus()",
        "root.MSG_GEN_ENTER_METHOD");
    boolean onlineEditingStatus = defaultOnlineEditingStatus;
    Connection con = getConnection();

    try {
      PersonalizeDetail personalizeDetail = getPersonalizeDetail();
      if (personalizeDetail != null)
        onlineEditingStatus = personalizeDetail.getOnlineEditingStatus();
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.getOnlineEditingStatus()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_GET_ONLINE_EDITING_STATUS", e);
    } finally {
      freeConnection(con);
    }
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getOnlineEditingStatus()",
        "root.MSG_GEN_PARAM_VALUE", "onlineEditingStatus = "
            + onlineEditingStatus);
    return onlineEditingStatus;
  }

  /**
   * Method declaration
   * 
   * 
   * @param onlineEditingStatus
   * 
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * 
   * @see
   */
  public void setOnlineEditingStatus(boolean onlineEditingStatus)
      throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.setOnlineEditingStatus()",
        "root.MSG_GEN_PARAM_VALUE", "onlineEditingStatus = "
            + onlineEditingStatus);
    Connection con = getConnection();

    try {
      PersonalizationDAO.setOnlineEditingStatus(con, this.currentUserId,
          onlineEditingStatus);
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.setOnlineEditingStatus()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_SET_ONLINE_EDITING_STATUS",
          "onlineEditingStatus=" + onlineEditingStatus, e);
    } finally {
      freeConnection(con);
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
   * @throws SQLException
   * 
   * @see
   */
  public boolean getWebdavEditingStatus() throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getWebdavEditingStatus()",
        "root.MSG_GEN_ENTER_METHOD");
    boolean webdavEditingStatus = defaultWebdavEditingStatus;
    Connection con = getConnection();

    try {
      PersonalizeDetail personalizeDetail = getPersonalizeDetail();
      if (personalizeDetail != null)
        webdavEditingStatus = personalizeDetail.isWebdavEditingStatus();
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.getWebdavEditingStatus()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_GET_ONLINE_EDITING_STATUS", e);
    } finally {
      freeConnection(con);
    }
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getWebdavEditingStatus()",
        "root.MSG_GEN_PARAM_VALUE", "webdavEditingStatus = "
            + webdavEditingStatus);
    return webdavEditingStatus;
  }

  /**
   * Method declaration
   * 
   * 
   * @param webdavEditingStatus
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void setWebdavEditingStatus(boolean webdavEditingStatus)
      throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.setWebdavEditingStatus()",
        "root.MSG_GEN_PARAM_VALUE", "webdavEditingStatus = "
            + webdavEditingStatus);
    Connection con = getConnection();

    try {
      PersonalizationDAO.setWebdavEditingStatus(con, this.currentUserId,
          webdavEditingStatus);
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.setWebdavEditingStatus()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_SET_ONLINE_EDITING_STATUS",
          "webdavEditingStatus=" + webdavEditingStatus, e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * ejb methods
   */
  public void ejbCreate() throws CreateException {
    SilverTrace.debug("personalization", "PersonalizationBmEJB.ejbCreate()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  /**
   * Method declaration
   * 
   * 
   * @throws java.rmi.RemoteException
   * @throws javax.ejb.EJBException
   * 
   * @see
   */
  public void ejbRemove() throws javax.ejb.EJBException,
      java.rmi.RemoteException {
    SilverTrace.debug("personalization", "PersonalizationBmEJB.ejbRemove()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  /**
   * Method declaration
   * 
   * 
   * @throws java.rmi.RemoteException
   * @throws javax.ejb.EJBException
   * 
   * @see
   */
  public void ejbActivate() throws javax.ejb.EJBException,
      java.rmi.RemoteException {
    SilverTrace.debug("personalization", "PersonalizationBmEJB.ejbActivate()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  /**
   * Method declaration
   * 
   * 
   * @throws java.rmi.RemoteException
   * @throws javax.ejb.EJBException
   * 
   * @see
   */
  public void ejbPassivate() throws javax.ejb.EJBException,
      java.rmi.RemoteException {
    SilverTrace.debug("personalization", "PersonalizationBmEJB.ejbPassivate()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  public void setSessionContext(final javax.ejb.SessionContext p1)
      throws javax.ejb.EJBException, java.rmi.RemoteException {
    SilverTrace
        .debug("personalization", "PersonalizationBmEJB.setSessionContext()",
            "root.MSG_GEN_ENTER_METHOD");
  }

}