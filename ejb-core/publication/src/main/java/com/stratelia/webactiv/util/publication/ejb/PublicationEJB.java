/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.util.publication.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.info.InfoDAO;
import com.stratelia.webactiv.util.publication.info.SeeAlsoDAO;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoImageDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoLinkDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoPK;
import com.stratelia.webactiv.util.publication.info.model.InfoTextDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationI18N;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;
import javax.ejb.ObjectNotFoundException;

/**
 * Class declaration
 * @author
 */
public class PublicationEJB implements EntityBean {

  private static final long serialVersionUID = 839570873632274272L;
  private EntityContext context;
  private PublicationPK pk;
  private InfoPK infoPK;
  private String name;
  private String description;
  private Date creationDate;
  private Date beginDate;
  private Date endDate;
  private String creatorId;
  private int importance;
  private String version;
  private String keywords;
  private String content;
  private String status;
  private Date updateDate;
  private String updaterId;
  private Date validateDate;
  private String validatorId;
  private String beginHour;
  private String endHour;
  private String author;
  private String targetValidatorId;
  private String cloneId;
  private String cloneStatus;
  private String lang;
  private Date draftOutDate;

  private List translations;

  private boolean isModified = false;

  /**
   * Constructor declaration
   * @see
   */
  public PublicationEJB() {
  }

  /**
   * Get the attributes of THIS publication
   * @return a PublicationDetail
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public PublicationDetail getDetail() throws SQLException {
    PublicationDetail pubDetail = new PublicationDetail(pk, name, description,
        creationDate, beginDate, endDate, creatorId, importance, version,
        keywords, content, status, updateDate, updaterId,
        author);
    pubDetail.setBeginHour(beginHour);
    pubDetail.setEndHour(endHour);
    pubDetail.setTargetValidatorId(targetValidatorId);
    pubDetail.setInfoId(infoPK.getId());
    pubDetail.setCloneId(cloneId);
    pubDetail.setCloneStatus(cloneStatus);
    pubDetail.setLanguage(lang);

    pubDetail.setTranslations(translations);
    pubDetail.setDraftOutDate(draftOutDate);
    pubDetail.setValidatorId(validatorId);
    pubDetail.setValidateDate(validateDate);

    return pubDetail;
  }

  /**
   * Update the attributes of the publication
   * @param pubDetail the PublicationDetail which contains updated data
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @since 1.0
   */
  public void setDetail(PublicationDetail pubDetail) {
    setDetail(pubDetail, false);
  }

  public void setDetail(PublicationDetail pubDetail, boolean forceUpdateDate) {
    if (pubDetail.getPK().equals(pk)) {
      String oldName = name;
      String oldDesc = description;
      String oldKeywords = keywords;
      String oldLang = lang;

      if (pubDetail.getName() != null) {
        name = pubDetail.getName();
      }
      if (pubDetail.getDescription() != null) {
        description = pubDetail.getDescription();
      }
      if (pubDetail.getCreationDate() != null) {
        creationDate = pubDetail.getCreationDate();
      }
      beginDate = pubDetail.getBeginDate();
      endDate = pubDetail.getEndDate();
      if (pubDetail.getCreatorId() != null) {
        creatorId = pubDetail.getCreatorId();
      }
      if (pubDetail.getImportance() != 0) {
        importance = pubDetail.getImportance();
      }
      if (pubDetail.getVersion() != null) {
        version = pubDetail.getVersion();
      }
      if (pubDetail.getKeywords() != null) {
        keywords = pubDetail.getKeywords();
      }
      if (pubDetail.getContent() != null) {
        content = pubDetail.getContent();
      }
      if (pubDetail.getStatus() != null) {
        status = pubDetail.getStatus();
      }

      /*
       * if(pubDetail.getUpdaterId() != null) { updaterId = pubDetail.getCreatorId(); }
       */
      updaterId = pubDetail.getUpdaterId();
      if (pubDetail.isUpdateDateMustBeSet()) {
        if (forceUpdateDate) {
          // In import case, we can force the update date to an old value
          if (pubDetail.getUpdateDate() != null) {
            updateDate = pubDetail.getUpdateDate();
          } else {
            updateDate = new Date();
          }
        } else {
          updateDate = new Date();
        }
      }
      isModified = true;
      if (pubDetail.getValidatorId() != null) {
        validatorId = pubDetail.getValidatorId();
      }

      if (pubDetail.getValidateDate() != null) {
        validateDate = new Date();
      }
      beginHour = pubDetail.getBeginHour();
      endHour = pubDetail.getEndHour();
      if (pubDetail.getAuthor() != null) {
        author = pubDetail.getAuthor();
      }

      targetValidatorId = pubDetail.getTargetValidatorId();

      if (pubDetail.getInfoId() != null)
        infoPK.setId(pubDetail.getInfoId());

      cloneId = pubDetail.getCloneId();
      cloneStatus = pubDetail.getCloneStatus();
      draftOutDate = pubDetail.getDraftOutDate();

      if (pubDetail.getLanguage() != null)
        lang = pubDetail.getLanguage();

      if (pubDetail.isRemoveTranslation()) {
        Connection con = getConnection();
        try {
          // Remove of a translation is required
          if (oldLang.equalsIgnoreCase(pubDetail.getLanguage())) {
            // Default language = translation
            List<PublicationI18N> translations = PublicationI18NDAO.getTranslations(con, pk);

            if (translations != null && translations.size() > 0) {
              PublicationI18N translation = translations.get(0);

              lang = translation.getLanguage();
              name = translation.getName();
              description = translation.getDescription();
              keywords = translation.getKeywords();

              PublicationI18NDAO.removeTranslation(con, translation.getId());
            }
          } else {
            PublicationI18NDAO.removeTranslation(con, pubDetail
                .getTranslationId());

            name = oldName;
            description = oldDesc;
            keywords = oldKeywords;
            lang = oldLang;
          }
        } catch (SQLException e) {
          throw new PublicationRuntimeException("PublicationEJB.setDetail()",
              SilverpeasRuntimeException.ERROR,
              "publication.CANNOT_MANAGE_TRANSLATIONS", e);
        } finally {
          freeConnection(con);
        }
      } else {
        // Add or update a translation
        if (pubDetail.getLanguage() != null) {
          if (oldLang == null) {
            // translation for the first time
            lang = I18NHelper.defaultLanguage;
          }

          if (oldLang != null
              && !oldLang.equalsIgnoreCase(pubDetail.getLanguage())) {
            PublicationI18N translation = new PublicationI18N(pubDetail);
            String translationId = pubDetail.getTranslationId();
            Connection con = getConnection();
            try {
              if (translationId != null && !translationId.equals("-1")) {
                PublicationI18NDAO.updateTranslation(con, translation);
              } else {
                PublicationI18NDAO.addTranslation(con, translation);
              }
            } catch (Exception e) {
              throw new PublicationRuntimeException(
                  "PublicationEJB.setDetail()",
                  SilverpeasRuntimeException.ERROR,
                  "publication.CANNOT_MANAGE_TRANSLATIONS", e);
            } finally {
              freeConnection(con);
            }
            name = oldName;
            description = oldDesc;
            keywords = oldKeywords;
            lang = oldLang;
          }
        }
      }

      getTranslations();
    } else {
      throw new PublicationRuntimeException("PublicationEJB.setDetail()",
          SilverpeasRuntimeException.ERROR,
          "publication.PUBDETAIL_PK_NOT_SAME_THAN_THIS_OBJECT");
    }
  }

  private void getTranslations() {
    PublicationI18N translation = new PublicationI18N(lang, name, description,
        keywords);
    translations = new ArrayList();
    translations.add(translation);
    Connection con = getConnection();
    try {
      translations.addAll(PublicationI18NDAO.getTranslations(con, pk));
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.getTranslations()",
          SilverpeasRuntimeException.ERROR,
          "publication.CANNOT_GET_TRANSLATIONS", e);
    } finally {
      freeConnection(con);
    }
  }

  private void createTranslations(Connection con, PublicationDetail publication)
      throws SQLException, UtilException {
    if (publication.getTranslations() != null) {
      Iterator translations = publication.getTranslations().values().iterator();
      PublicationI18N translation = null;
      while (translations.hasNext()) {
        translation = (PublicationI18N) translations.next();
        if (publication.getLanguage() != null
            && !publication.getLanguage().equals(translation.getLanguage())) {
          translation.setObjectId(publication.getPK().getId());
          PublicationI18NDAO.addTranslation(con, translation);
        }
      }
    }
  }

  /**
   * Add a new father to this publication
   * @param fatherPK the father NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void addFather(NodePK fatherPK) throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.addFather()",
        "root.MSG_GEN_ENTER_METHOD", "fatherId = " + fatherPK.getId());
    Connection con = getConnection();

    try {
      PublicationFatherDAO.addFather(con, pk, fatherPK);
    } finally {
      freeConnection(con);
    }
  }

  public void move(NodePK fatherPK) throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.move()",
        "root.MSG_GEN_ENTER_METHOD", "fatherId = " + fatherPK.getId());
    Connection con = getConnection();

    try {
      PublicationDAO.changeInstanceId(con, pk, fatherPK.getInstanceId());

      pk.setComponentName(fatherPK.getInstanceId());

      PublicationFatherDAO.removeAllFather(con, pk);

      PublicationFatherDAO.addFather(con, pk, fatherPK);

      isModified = true;
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Remove a father to this publication
   * @param fatherPK the father NodePK to remove
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void removeFather(NodePK fatherPK) throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.removeFather()",
        "root.MSG_GEN_ENTER_METHOD", "fatherId = " + fatherPK.getId());
    Connection con = getConnection();
    try {
      PublicationFatherDAO.removeFather(con, pk, fatherPK);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Remove all fathers to this publication - this publication will be linked to no Node
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public void removeAllFather() throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.removeAllFather()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();

    try {
      PublicationFatherDAO.removeAllFather(con, pk);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Get all fathers of this publication
   * @return A collection of NodePK
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see java.util.Collection
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public Collection<NodePK> getAllFatherPK() throws SQLException {
    return getAllFatherPK(null);
  }

  public Collection<NodePK> getAllFatherPK(String sorting) throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.getAllFatherPK()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();

    try {
      return PublicationFatherDAO.getAllFatherPK(con, pk, sorting);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Create or update info to this publication
   * @param modelPK The modelPk corresponding to the choosen model
   * @param infos An InfoDetail which contains info to add to the publication
   * @see com.stratelia.webactiv.util.publication.info.model.ModelPK
   * @see com.stratelia.webactiv.util.publication.info.model.InfoDetail
   * @throws java.sql.SQLException
   * @since 1.0
   */
  public void createInfoDetail(ModelPK modelPK, InfoDetail infos)
      throws SQLException {

    SilverTrace.info("publication", "PublicationEJB.createInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD", "modelId = " + modelPK.getId());
    Connection con = getConnection();

    try {
      InfoPK iPK = InfoDAO.createInfo(con, modelPK, this.pk);

      if (infos != null) {
        infos.setPK(iPK);
        InfoDAO.addInfoItems(con, infos);
        updateDate = new Date();
      }
      infoPK = iPK;
      isModified = true;
      ejbStore();
    } catch (UtilException e) {
      throw new PublicationRuntimeException("PublicationEJB.createInfoDetail()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_INSERT_TRANSLATIONS", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Create only info associated to a model
   * @param modelPK The modelPk corresponding to the choosen model
   * @param infos An InfoDetail which contains info to add to the publication
   * @see com.stratelia.webactiv.util.publication.info.model.ModelPK
   * @see com.stratelia.webactiv.util.publication.info.model.InfoDetail
   * @throws java.sql.SQLException
   * @throws UtilException
   * @since 1.0
   */
  public void createInfoModelDetail(ModelPK modelPK, InfoDetail infos)
      throws SQLException {

    SilverTrace.info("publication", "PublicationEJB.createInfoModelDetail()",
        "root.MSG_GEN_ENTER_METHOD", "modelId = " + modelPK.getId());
    Connection con = getConnection();

    try {
      // The publication have already some info attached (model, attached files,
      // links...)
      if (!infoPK.getId().equals("0")) {
        // Update the model
        InfoDAO.updateInfo(con, modelPK, infoPK);
        if (infos != null) {
          infos.setPK(infoPK);
          // Add info
          InfoDAO.addInfoItems(con, infos);
          updateDate = new Date();
          isModified = true;
        }
      } else {
        // Creation from A to Z
        createInfoDetail(modelPK, infos);
      }
    } catch (UtilException e) {
      throw new PublicationRuntimeException("PublicationEJB.createInfoModelDetail()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_INSERT_TRANSLATIONS", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Get all info associated to this publication
   * @return All info are in a InfoDetail object
   * @see com.stratelia.webactiv.util.publication.info.model.InfoDetail
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public InfoDetail getInfoDetail() throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.getInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();

    try {
      InfoDetail result = InfoDAO.getInfoDetailByInfoPK(con, infoPK);
      return result;
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Update info associated to this publication
   * @param infos An InfoDetail which contains info to update to the publication
   * @see com.stratelia.webactiv.util.publication.info.model.InfoDetail
   * @throws java.sql.SQLException
   * @since 1.0
   */
  public void updateInfoDetail(InfoDetail infos) throws SQLException {

    SilverTrace.info("publication", "PublicationEJB.updateInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    InfoDetail old = getInfoDetail();

    // update or create textDetail ?
    ArrayList<InfoTextDetail> newText = new ArrayList<InfoTextDetail>();
    ArrayList<InfoTextDetail> oldText = new ArrayList<InfoTextDetail>();

    InfoDetail.selectToCreateAndToUpdateItems(infos.getInfoTextList(), old
        .getInfoTextList(), newText, oldText);

    // update or create imageDetail ?
    ArrayList<InfoImageDetail> newImage = new ArrayList<InfoImageDetail>();
    ArrayList<InfoImageDetail> oldImage = new ArrayList<InfoImageDetail>();

    InfoDetail.selectToCreateAndToUpdateItems(infos.getInfoImageList(), old
        .getInfoImageList(), newImage, oldImage);

    // update or create linkDetail ?
    // update or create imageDetail ?
    ArrayList<InfoLinkDetail> newLink = new ArrayList<InfoLinkDetail>();
    ArrayList<InfoLinkDetail> oldLink = new ArrayList<InfoLinkDetail>();

    InfoDetail.selectToCreateAndToUpdateItems(infos.getInfoLinkList(), old
        .getInfoLinkList(), newLink, oldLink);

    Connection con = getConnection();

    try {
      if (this.infoPK.getId().equals("0")) {
        createInfoDetail(new ModelPK("0", this.pk), new InfoDetail(infoPK,
            newText, newImage, newLink, ""));
      } else {
        InfoDAO.updateInfoItems(con, new InfoDetail(infoPK, oldText, oldImage,
            oldLink, ""), infoPK);
        InfoDAO.addInfoItems(con, new InfoDetail(infoPK, newText, newImage,
            newLink, ""));
        updateDate = new Date();
        isModified = true;
      }
    } catch (UtilException e) {
      throw new PublicationRuntimeException("PublicationEJB.createInfoModelDetail()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_INSERT_TRANSLATIONS", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Removes links between publications in the current publication
   * @param links list of links to remove
   * @throws SQLException
   */
  public void deleteInfoLinks(List<ForeignPK> links) throws SQLException {
    Connection con = getConnection();

    try {
      PublicationPK targetPK = null;
      for (ForeignPK link : links) {
        targetPK = new PublicationPK(link.getId(), link.getInstanceId());
        SeeAlsoDAO.deleteLink(con, this.pk, targetPK);
      }
      isModified = true;
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Get all info on publication (parameters, model and info)
   * @return A completePublication
   * @see com.stratelia.webactiv.util.publication.model.CompletePublication
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public CompletePublication getCompletePublication() throws SQLException {
    SilverTrace.info("publication", "PublicationEJB.getCompletePublication()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();

    try {
      // get detail
      PublicationDetail pubDetail = getDetail();

      // get infos
      InfoDetail infoDetail = InfoDAO.getInfoDetailByInfoPK(con, infoPK);
      // get model
      ModelDetail modelDetail = InfoDAO.getModelDetail(con, infoPK);

      // Get links
      List<ForeignPK> links = SeeAlsoDAO.getLinks(con, pubDetail.getPK());

      // gets reverse links
      List<ForeignPK> reverseLinks = SeeAlsoDAO.getReverseLinks(con, pubDetail.getPK());

      return new CompletePublication(pubDetail, modelDetail, infoDetail, links, reverseLinks);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Create a new Publication object
   * @param pubDetail the PublicationDetail which contains data
   * @return the PublicationPK of the new Publication
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @exception javax.ejb.CreateException
   * @since 1.0
   */
  public PublicationPK ejbCreate(PublicationDetail pubDetail) throws CreateException {

    SilverTrace.info("publication", "PublicationEJB.ejbCreate()",
        "root.MSG_GEN_ENTER_METHOD", "pubDetail = " + pubDetail.toString());
    Connection con = getConnection();

    try {
      int id = 0;

      try {
        id = DBUtil.getNextId(pubDetail.getPK().getTableName(), "pubId");
      } catch (Exception ex) {
        throw new PublicationRuntimeException("PublicationEJB.ejbCreate()",
            SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", ex);
      }
      pubDetail.getPK().setId(String.valueOf(id));
      try {
        PublicationDAO.insertRow(con, pubDetail);
      } catch (Exception ex) {
        throw new PublicationRuntimeException("PublicationEJB.ejbCreate()",
            SilverpeasRuntimeException.ERROR,
            "root.EX_CANT_INSERT_ENTITY_ATTRIBUTES", ex);
      }
      if (I18NHelper.isI18N) {
        try {
          createTranslations(con, pubDetail);
        } catch (Exception ex) {
          throw new PublicationRuntimeException("PublicationEJB.ejbCreate()",
              SilverpeasRuntimeException.ERROR,
              "root.EX_CANT_INSERT_TRANSLATIONS", ex);
        }
      }
    } finally {
      freeConnection(con);
    }

    pk = pubDetail.getPK();
    if (pubDetail.getInfoId() == null || pubDetail.getInfoId().length() == 0)
      infoPK = new InfoPK("0", pk);
    else
      infoPK = new InfoPK(pubDetail.getInfoId(), pk);
    name = pubDetail.getName();
    description = pubDetail.getDescription();
    creationDate = pubDetail.getCreationDate();
    beginDate = pubDetail.getBeginDate();
    endDate = pubDetail.getEndDate();
    creatorId = pubDetail.getCreatorId();
    importance = pubDetail.getImportance();
    version = pubDetail.getVersion();
    keywords = pubDetail.getKeywords();
    content = pubDetail.getContent();
    status = pubDetail.getStatus();
    updateDate = new Date();
    if (!StringUtil.isDefined(pubDetail.getUpdaterId())) {
      updaterId = pubDetail.getCreatorId();
    } else {
      updaterId = pubDetail.getUpdaterId();
    }
    beginHour = pubDetail.getBeginHour();
    endHour = pubDetail.getEndHour();
    author = pubDetail.getAuthor();
    targetValidatorId = pubDetail.getTargetValidatorId();
    cloneId = pubDetail.getCloneId();
    cloneStatus = pubDetail.getCloneStatus();
    lang = pubDetail.getLanguage();
    draftOutDate = pubDetail.getDraftOutDate();

    getTranslations();

    return pk;
  }

  /**
   * Method declaration
   * @param pubDetail
   * @throws CreateException
   * @see
   */
  public void ejbPostCreate(PublicationDetail pubDetail) throws CreateException {
  }

  /**
   * Create an instance of a Publication object.
   * @param pk the PK of the Publication to instanciate
   * @return the PublicationPK of the instanciated Publication if it exists in database
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @exception javax.ejb.FinderException
   * @since 1.0
   */
  public PublicationPK ejbFindByPrimaryKey(PublicationPK pk) throws
      FinderException {

    Connection con = getConnection();

    try {
      PublicationPK primary = PublicationDAO.selectByPrimaryKey(con, pk);
      if (primary != null) {
        return primary;
      } else {
        SilverTrace.debug("publication", "PublicationEJB.ejbFindByPrimaryKey()",
            "root.EX_CANT_FIND_ENTITY", "PubId = "
            + pk.getId());
        throw new ObjectNotFoundException("Cannot find publication ID: " + pk);
      }
    } catch (SQLException e) {
      /* SQLException is a real runtime error */
      throw new PublicationRuntimeException(
          "PublicationEJB.ejbFindByPrimaryKey()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_FIND_ENTITY", "PubId = " + pk.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Create an instance of a Publication object.
   * @param pk the PK where the Publication is instanciated
   * @param name the publication's name to instanciate
   * @return the PublicationPK of the instanciated Publication if it exists in database
   * @see com.stratelia.webactiv.util.publication.model.PublicationDetail
   * @exception javax.ejb.FinderException
   * @since 1.0
   */
  public PublicationPK ejbFindByName(PublicationPK pk, String name)
      throws FinderException {

    Connection con = getConnection();

    try {
      PublicationPK primary = PublicationDAO.selectByPublicationName(con, pk,
          name);
      if (primary != null) {
        return primary;
      } else {
        SilverTrace.debug("publication", "PublicationEJB.ejbFindByName()",
            "root.EX_CANT_FIND_ENTITY", "name = " + name);
        throw new ObjectNotFoundException(
            "Cannot find publication named: " + name);
      }
    } catch (SQLException e) {
      /* SQLException is a real runtime error */
      throw new PublicationRuntimeException("PublicationEJB.ejbFindByName()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_FIND_ENTITY", "name = " + name, e);
    } finally {
      freeConnection(con);
    }
  }

  public PublicationPK ejbFindByNameAndNodeId(PublicationPK pk, String name,
      int nodeId) throws FinderException {

    Connection con = getConnection();

    try {
      PublicationPK primary = PublicationDAO.selectByPublicationNameAndNodeId(
          con, pk, name, nodeId);
      if (primary != null) {
        return primary;
      } else {
        SilverTrace.debug("publication",
            "PublicationEJB.ejbFindByNameAndNodeId()",
            "root.EX_CANT_FIND_ENTITY", "name="
            + name + ", nodeId=" + nodeId);
        throw new ObjectNotFoundException(
            "Cannot find publication named: " + name + ", and parent node ID: " + nodeId);
      }
    } catch (SQLException e) {
      /* SQLException is a real runtime error */
      throw new PublicationRuntimeException(
          "PublicationEJB.ejbFindByNameAndNodeId()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_FIND_ENTITY",
          "name = " + name + ", parent nodeId=" + nodeId, e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Load publication attributes from database
   * @since 1.0
   */
  @Override
  public void ejbLoad() {
    SilverTrace.info("publication", "PublicationEJB.ejbLoad()",
        "root.MSG_GEN_ENTER_METHOD");
    if (pk == null) {
      return;
    }
    PublicationDetail pubDetail = null;
    Connection con = null;

    try {
      if (pk.pubDetail != null) {
        pubDetail = pk.pubDetail;
      } else {
        con = getConnection();
        pubDetail = PublicationDAO.loadRow(con, pk);
      }
      infoPK = new InfoPK(pubDetail.getInfoId(), pk);
      name = pubDetail.getName();
      description = pubDetail.getDescription();
      creationDate = pubDetail.getCreationDate();
      beginDate = pubDetail.getBeginDate();
      endDate = pubDetail.getEndDate();
      creatorId = pubDetail.getCreatorId();
      importance = pubDetail.getImportance();
      version = pubDetail.getVersion();
      keywords = pubDetail.getKeywords();
      content = pubDetail.getContent();
      status = pubDetail.getStatus();
      updateDate = pubDetail.getUpdateDate();
      updaterId = pubDetail.getUpdaterId();
      beginHour = pubDetail.getBeginHour();
      endHour = pubDetail.getEndHour();
      author = pubDetail.getAuthor();
      targetValidatorId = pubDetail.getTargetValidatorId();
      cloneId = pubDetail.getCloneId();
      cloneStatus = pubDetail.getCloneStatus();
      lang = pubDetail.getLanguage();
      draftOutDate = pubDetail.getDraftOutDate();
      validatorId = pubDetail.getValidatorId();
      validateDate = pubDetail.getValidateDate();

      getTranslations();

      isModified = false;
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationEJB.ejbLoad()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_LOAD_ENTITY_ATTRIBUTES", "PubId = " + pk.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Store publication attributes into database
   * @since 1.0
   */
  @Override
  public void ejbStore() {
    if (!isModified) {
      return;
    }
    if (pk == null) {
      return;
    }
    PublicationDetail detail = new PublicationDetail(pk, name, description,
        creationDate, beginDate, endDate, creatorId, importance, version,
        keywords, content, status, updateDate, updaterId,
        validateDate, validatorId, author);
    detail.setBeginHour(beginHour);
    detail.setEndHour(endHour);
    detail.setTargetValidatorId(targetValidatorId);
    detail.setCloneId(cloneId);
    detail.setCloneStatus(cloneStatus);
    detail.setLanguage(lang);
    detail.setInfoId(infoPK.getId());
    detail.setDraftOutDate(draftOutDate);

    Connection con = getConnection();
    try {
      PublicationDAO.storeRow(con, detail);
      isModified = false;
    } catch (java.sql.SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.ejbStore()",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "PubId = " + pk.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * Delete this Publication and all info associated
   * @since 1.0
   */
  @Override
  public void ejbRemove() {
    SilverTrace.info("publication", "PublicationEJB.ejbRemove()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();

    try {
      // delete links from another publication to removed publication
      InfoDAO.deleteInfoLinkByTargetLink(con, infoPK, pk.getId());

      SeeAlsoDAO.deleteLinksByObjectId(con, pk);
      SeeAlsoDAO.deleteLinksByTargetId(con, pk);

      // delete all info associated from database
      InfoDAO.deleteInfoDetailByInfoPK(con, infoPK);

      // delete translations
      PublicationI18NDAO.removeTranslations(con, pk);

      // delete publication from database
      PublicationDAO.deleteRow(con, pk);
    } catch (java.sql.SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.ejbRemove()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_DELETE_ENTITY",
          "PubId = " + pk.getId(), e);
    } finally {
      freeConnection(con);
    }
  }

  @Override
  public void ejbActivate() {
    pk = (PublicationPK) context.getPrimaryKey();
  }

  @Override
  public void ejbPassivate() {
    pk = null;
  }

  @Override
  public void setEntityContext(EntityContext ec) {
    context = ec;
  }

  @Override
  public void unsetEntityContext() {
    context = null;
  }

  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(JNDINames.PUBLICATION_DATASOURCE);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("publication", "PublicationEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  public String getValidatorId() {
    return validatorId;
  }

  public Date getValidateDate() {
    return validateDate;
  }
}