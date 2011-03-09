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

import com.silverpeas.personalization.dao.PersonalizationDao;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.personalization.model.PersonalizeDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;

import static com.silverpeas.ui.DisplayI18NHelper.getDefaultLanguage;

/**
 * Class declaration
 *
 * @author
 */
public class PersonalizationBmEJB implements PersonalizationBmBusinessSkeleton, SessionBean {

  private static final long serialVersionUID = 6776141343859788723L;

  private static final PersonalizationDao dao = new JdbcPersonalizationDao();
  private String currentUserId;
  private String defaultLanguage = null;
  private String defaultLook = "Initial";
  private String defaultPersonalWSId = null;
  private boolean defaultThesaurusStatus = false;
  private boolean defaultDragDropStatus = true;
  private boolean defaultOnlineEditingStatus = true;
  private ResourceLocator settings = null;

  /**
   * Constructor declaration
   *
   * @see
   */
  public PersonalizationBmEJB() {
  }

  /**
   * Method declaration
   *
   * @param user
   * @throws RemoteException
   * @see
   */
  public void setActor(String userId) throws RemoteException {
    this.currentUserId = userId;
  }

  /**
   * Method declaration
   *
   * @return
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


  /**
   * Method declaration
   *
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  private PersonalizeDetail getPersonalizeDetail() throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getPersonalizeDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    PersonalizeDetail personalizeDetail = null;

    try {
      personalizeDetail = dao.getPersonalizeDetail(con,
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
   * @param languages
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public void setLanguages(String languages) throws RemoteException {
    SilverTrace.info("personalization", "PersonalizationBmEJB.setLanguage()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();

    try {
      dao.setLanguage(con, this.currentUserId, languages);
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.setLanguage()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_SET_LANGUAGE", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Method declaration
   *
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public String getLanguages() throws RemoteException {
    SilverTrace.info("personalization", "PersonalizationBmEJB.getLanguage()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    String languages = null;

    try {
      PersonalizeDetail personalizeDetail = getPersonalizeDetail();

      if (personalizeDetail != null) {
        languages = personalizeDetail.getLanguage();
      } else {
        // the user has not defined favorite language
        languages = getDefaultLanguage();

        // insert a new record in database
        dao.insertPersonalizeDetail(con, this.currentUserId,
            languages, this.defaultLook, defaultPersonalWSId,
            this.defaultThesaurusStatus, this.defaultDragDropStatus,
            this.defaultOnlineEditingStatus, getDefaultWebDAVEditingStatus());
      }
    } catch (Exception e) {
      throw new PersonalizationRuntimeException(
          "PersonalizationBmEJB.getLanguage()",
          SilverpeasRuntimeException.ERROR,
          "personalization.EX_CANT_GET_LANGUAGE", e);
    } finally {
      freeConnection(con);
    }
    return languages;
  }

  private boolean getDefaultWebDAVEditingStatus() {

    if (settings == null) {
      settings = new ResourceLocator(
          "com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings",
          "");
    }

    if (settings != null) {
      return settings.getBoolean("DefaultWebDAVEditingStatus", true);
    }
    return true;
  }

  /**
   * Method declaration
   *
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public String getFavoriteLanguage() throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getFavoriteLanguage()",
        "root.MSG_GEN_ENTER_METHOD");
    String languages = getLanguages();
    String favoriteLanguage = getDefaultLanguage();

    if (StringUtil.isDefined(languages)) {
      favoriteLanguage = languages;
    }
    return favoriteLanguage;
  }

  /**
   * Method declaration
   *
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public String getFavoriteLook()  {
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
   * @param look
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public void setFavoriteLook(String look) throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.setFavoriteLook()", "root.MSG_GEN_ENTER_METHOD",
        "look = " + look);
    Connection con = getConnection();

    try {
      PersonalizeDetail personalizeDetail = getPersonalizeDetail();
      dao.setFavoriteLook(con, this.currentUserId, look);
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
      dao.setPersonalWorkSpace(con, this.currentUserId, spaceId);
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
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
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
      if (personalizeDetail != null) {
        thesaurusStatus = personalizeDetail.getThesaurusStatus();
      }
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
   * @param thesaurusStatus
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public void setThesaurusStatus(boolean thesaurusStatus)
      throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.setThesaurusStatus()",
        "root.MSG_GEN_PARAM_VALUE", "thesaurusStatus = " + thesaurusStatus);
    Connection con = getConnection();

    try {
      dao.setThesaurusStatus(con, this.currentUserId,
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
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
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
      if (personalizeDetail != null) {
        dragAndDropStatus = personalizeDetail.getDragAndDropStatus();
      }
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
   * @param dragAndDropStatus
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public void setDragAndDropStatus(boolean dragAndDropStatus)
      throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.setDragAndDropStatus()",
        "root.MSG_GEN_PARAM_VALUE", "dragAndDropStatus = " + dragAndDropStatus);
    Connection con = getConnection();

    try {
      dao.setDragAndDropStatus(con, this.currentUserId,
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
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
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
      if (personalizeDetail != null) {
        onlineEditingStatus = personalizeDetail.getOnlineEditingStatus();
      }
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
   * @param onlineEditingStatus
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
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
      dao.setOnlineEditingStatus(con, this.currentUserId,
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
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public boolean getWebdavEditingStatus() throws RemoteException {
    SilverTrace.info("personalization",
        "PersonalizationBmEJB.getWebdavEditingStatus()",
        "root.MSG_GEN_ENTER_METHOD");
    boolean webdavEditingStatus = getDefaultWebDAVEditingStatus();
    Connection con = getConnection();

    try {
      PersonalizeDetail personalizeDetail = getPersonalizeDetail();
      if (personalizeDetail != null) {
        webdavEditingStatus = personalizeDetail.isWebdavEditingStatus();
      }
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
   * @param webdavEditingStatus
   * @throws RemoteException
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
      dao.setWebdavEditingStatus(con, this.currentUserId,
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
   * @throws java.rmi.RemoteException
   * @throws javax.ejb.EJBException
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
   * @throws java.rmi.RemoteException
   * @throws javax.ejb.EJBException
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
   * @throws java.rmi.RemoteException
   * @throws javax.ejb.EJBException
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