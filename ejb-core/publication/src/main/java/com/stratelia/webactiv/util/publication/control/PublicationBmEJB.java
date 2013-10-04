/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.publication.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;
import org.silverpeas.search.indexEngine.model.IndexManager;
import org.silverpeas.wysiwyg.control.WysiwygController;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.RecordSet;
import com.silverpeas.notation.ejb.NotationBm;
import com.silverpeas.notation.model.Notation;
import com.silverpeas.notation.model.NotationPK;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.tagcloud.ejb.TagCloudBm;
import com.silverpeas.tagcloud.model.TagCloud;
import com.silverpeas.tagcloud.model.TagCloudPK;
import com.silverpeas.tagcloud.model.TagCloudUtil;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.i18n.Translation;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.coordinates.control.CoordinatesBm;
import com.stratelia.webactiv.util.coordinates.model.Coordinate;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePK;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePoint;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
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
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.NodeTree;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationI18N;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;
import com.stratelia.webactiv.util.publication.model.ValidationStep;

/**
 * Class declaration
 *
 * @author
 */
@Stateless(name = "Publication", description = "Stateless session bean to manage publications.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class PublicationBmEJB implements PublicationBm {

  @EJB
  private NodeBm nodeBm;
  @EJB
  private CoordinatesBm coordinatesBm;
  @EJB
  private NotationBm notationBm;
  @EJB
  private TagCloudBm tagCloudBm;
  private static final long serialVersionUID = -829288807683338746L;
  private SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy/MM/dd");

  @Override
  public PublicationDetail getDetail(PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      PublicationPK publicationPk = PublicationDAO.selectByPrimaryKey(con, pubPK);
      if (publicationPk != null) {
        return loadTranslations(publicationPk.pubDetail);
      }
      return null;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getDetail()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_HEADER_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  private void setTranslations(Connection con, PublicationDetail publi) {
    try {
      PublicationI18N translation = new PublicationI18N(publi.getLanguage(),
          publi.getName(), publi.getDescription(), publi.getKeywords());
      publi.addTranslation(translation);
      List translations = PublicationI18NDAO.getTranslations(con, publi.getPK());
      publi.setTranslations(translations);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.setTranslations()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_TRANSLATIONS_FAILED", "pubId = "
          + publi.getPK().getId(), e);
    }
  }

  private void setTranslations(Connection con, Collection<PublicationDetail> publis) {
    if (publis != null && !publis.isEmpty()) {
      for (PublicationDetail publi : publis) {
        setTranslations(con, publi);
      }
    }
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public PublicationPK createPublication(PublicationDetail detail) {
    Connection con = getConnection();
    try {
      int indexOperation = detail.getIndexOperation();
      SilverTrace.info("publication", "PublicationBmEJB.createPublication()",
          "root.MSG_GEN_PARAM_VALUE", "indexOperation = " + indexOperation);
      int id = 0;

      try {
        id = DBUtil.getNextId(detail.getPK().getTableName(), "pubId");
      } catch (UtilException ex) {
        throw new PublicationRuntimeException("PublicationEJB.ejbCreate()",
            SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", ex);
      }
      detail.getPK().setId(String.valueOf(id));
      try {
        PublicationDAO.insertRow(con, detail);
      } catch (SQLException ex) {
        throw new PublicationRuntimeException("PublicationEJB.ejbCreate()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_INSERT_ENTITY_ATTRIBUTES", ex);
      }
      if (I18NHelper.isI18N) {
        try {
          createTranslations(con, detail);
        } catch (SQLException ex) {
          throw new PublicationRuntimeException("PublicationEJB.ejbCreate()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_INSERT_TRANSLATIONS", ex);
        } catch (UtilException ex) {
          throw new PublicationRuntimeException("PublicationEJB.ejbCreate()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_INSERT_TRANSLATIONS", ex);
        }
      }
      getTranslations(detail);
      detail.setIndexOperation(indexOperation);
      createIndex(detail);
      if (useTagCloud) {
        createTagCloud(detail);
      }
      detail.getPK().pubDetail = detail;
      return detail.getPK();
    } catch (Exception re) {
      throw new PublicationRuntimeException("PublicationBmEJB.createPublication()",
          SilverpeasRuntimeException.ERROR, "publication.CREATING_PUBLICATION_FAILED", "detail = "
          + detail.toString(), re);
    } finally {
      DBUtil.close(con);
    }

  }

  private void createTranslations(Connection con, PublicationDetail publication)
      throws SQLException, UtilException {
    if (publication.getTranslations() != null) {
      Iterator translations = publication.getTranslations().values().iterator();
      while (translations.hasNext()) {
        PublicationI18N translation = (PublicationI18N) translations.next();
        if (publication.getLanguage() != null
            && !publication.getLanguage().equals(translation.getLanguage())) {
          translation.setObjectId(publication.getPK().getId());
          PublicationI18NDAO.addTranslation(con, translation);
        }
      }
    }
  }

  @Override
  public void movePublication(PublicationPK pk, NodePK fatherPK, boolean indexIt) {
    Connection con = getConnection();
    try {
      deleteIndex(pk);
      PublicationDAO.changeInstanceId(con, pk, fatherPK.getInstanceId());
      pk.setComponentName(fatherPK.getInstanceId());
      PublicationFatherDAO.removeAllFather(con, pk);
      PublicationFatherDAO.addFather(con, pk, fatherPK);
      if (indexIt) {
        createIndex(pk);
      }
    } catch (SQLException re) {
      throw new PublicationRuntimeException("PublicationBmEJB.movePublication()",
          SilverpeasRuntimeException.ERROR, "publication.MOVING_PUBLICATION_FAILED", "pubId = "
          + pk.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void changePublicationsOrder(List<String> ids, NodePK nodePK) {
    if (ids == null || ids.isEmpty()) {
      return;
    }
    Connection con = getConnection();
    try {
      PublicationPK pubPK = new PublicationPK("unknown", nodePK.getInstanceId());
      for (int i = 0; i < ids.size(); i++) {
        String id = ids.get(i);
        pubPK.setId(id);
        PublicationFatherDAO.updateOrder(con, pubPK, nodePK, i);
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.changePublicationsOrder()",
          SilverpeasRuntimeException.ERROR, "publication.SORTING_PUBLICATIONS_FAILED", "pubIds = "
          + ids, e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public void changePublicationOrder(PublicationPK pubPK, NodePK nodePK, int direction) {
    // get all publications in given node
    List<PublicationDetail> publications = (List<PublicationDetail>) getDetailsByFatherPK(nodePK,
        "P.pubUpdateDate desc");
    // find given publication
    int index = getIndexOfPublication(pubPK.getId(), publications);
    // remove publication in list
    PublicationDetail publication = publications.remove(index);
    index = index + direction;
    // prevent indexOutOfBound
    if (index < 0) {
      index = 0;
    } else if (index > publications.size()) {
      index = publications.size();
    }
    // insert publication at the right place
    publications.add(index, publication);
    // change all publications order
    Connection con = getConnection();
    try {
      for (int p = 0; p < publications.size(); p++) {
        PublicationDetail publiToOrder = publications.get(p);
        PublicationFatherDAO.updateOrder(con, publiToOrder.getPK(), nodePK, p);
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.changePublicationOrder()",
          SilverpeasRuntimeException.ERROR, "publication.MOVING_PUBLICATION_FAILED", "pubId = "
          + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  private int getIndexOfPublication(String pubId, List<PublicationDetail> publications) {
    SilverTrace.debug("publication", "PublicationBmEJB.getIndexOfPublication()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
    int index = 0;
    if (publications != null) {
      for (PublicationDetail publi : publications) {
        if (pubId.equals(publi.getPK().getId())) {
          SilverTrace.debug("publication", "PublicationBmEJB.getIndexOfPublication()",
              "root.MSG_GEN_EXIT_METHOD", "index = " + index);
          return index;
        }
        index++;
      }
    }
    SilverTrace.debug("publication", "PublicationBmEJB.getIndexOfPublication()",
        "root.MSG_GEN_EXIT_METHOD", "index = " + index);
    return index;
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void removePublication(PublicationPK pk) {
    Connection con = getConnection();
    try {
      PublicationDetail publi = PublicationDAO.loadRow(con, pk);
      // delete links from another publication to removed publication
      InfoPK infoPK = new InfoPK(publi.getInfoId(), pk);
      InfoDAO.deleteInfoLinkByTargetLink(con, infoPK, pk.getId());
      SeeAlsoDAO.deleteLinksByObjectId(con, pk);
      SeeAlsoDAO.deleteLinksByTargetId(con, pk);
      // delete all info associated from database
      InfoDAO.deleteInfoDetailByInfoPK(con, infoPK);
      // delete translations
      PublicationI18NDAO.removeTranslations(con, pk);
      // delete publication from database
      PublicationDAO.deleteRow(con, pk);
      deleteIndex(pk);
    } catch (java.sql.SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.ejbRemove()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_DELETE_ENTITY",
          "PubId = " + pk.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void setDetail(PublicationDetail detail) {
    setDetail(detail, false);
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void setDetail(PublicationDetail detail, boolean forceUpdateDate) {
    Connection con = getConnection();
    try {
      PublicationDetail publi = PublicationDAO.loadRow(con, detail.getPK());
      int indexOperation = detail.getIndexOperation();
      updateDetail(detail, forceUpdateDate);
      if (detail.isRemoveTranslation()) {
        WysiwygController.deleteFile(detail.getPK().getInstanceId(), detail.getPK().getId(),
            detail.getLanguage());
        // remove xml content
        String infoId = detail.getInfoId();
        SilverTrace.info("publication", "PublicationBmEJB.setDetail()", "root.MSG_GEN_PARAM_VALUE",
            "infoId = " + infoId);
        if (StringUtil.isDefined(infoId) && !StringUtil.isInteger(infoId)) {
          String xmlFormShortName = infoId;
          PublicationTemplate pubTemplate = PublicationTemplateManager.getInstance()
              .getPublicationTemplate(detail.getPK().getInstanceId() + ':' + xmlFormShortName);
          RecordSet set = pubTemplate.getRecordSet();
          DataRecord data = set.getRecord(detail.getPK().getId(), detail.getLanguage());
          set.delete(data);
        }
      }
      SilverTrace.info("publication", "PublicationBmEJB.setDetail()", "root.MSG_GEN_PARAM_VALUE",
          "indexOperation = " + indexOperation);
      if (indexOperation == IndexManager.ADD || indexOperation == IndexManager.READD) {
        createIndex(detail.getPK(), true, indexOperation);
      } else if (indexOperation == IndexManager.REMOVE) {
        deleteIndex(detail.getPK());
      }

      if (useTagCloud) {
        updateTagCloud(detail);
      }
    } catch (Exception re) {
      throw new PublicationRuntimeException("PublicationBmEJB.setDetail()",
          SilverpeasRuntimeException.ERROR, "publication.UPDATING_PUBLICATION_HEADER_FAILED",
          "detail = " + detail, re);
    } finally {
      DBUtil.close(con);
    }
  }

  private void updateDetail(PublicationDetail pubDetail, boolean forceUpdateDate) {
    Connection con = getConnection();
    try {
      PublicationDetail publi = PublicationDAO.loadRow(con, pubDetail.getPK());
      String oldName = publi.getName();
      String oldDesc = publi.getDescription();
      String oldKeywords = publi.getKeywords();
      String oldLang = publi.getLanguage();
      if (pubDetail.getName() != null) {
        publi.setName(pubDetail.getName());
      }
      if (pubDetail.getDescription() != null) {
        publi.setDescription(pubDetail.getDescription());
      }
      if (pubDetail.getCreationDate() != null) {
        publi.setCreationDate(pubDetail.getCreationDate());
      }
      publi.setBeginDate(pubDetail.getBeginDate());
      publi.setEndDate(pubDetail.getEndDate());
      if (pubDetail.getCreatorId() != null) {
        publi.setCreatorId(pubDetail.getCreatorId());
      }
      if (pubDetail.getImportance() != 0) {
        publi.setImportance(pubDetail.getImportance());
      }
      if (pubDetail.getVersion() != null) {
        publi.setVersion(pubDetail.getVersion());
      }
      if (pubDetail.getKeywords() != null) {
        publi.setKeywords(pubDetail.getKeywords());
      }
      if (pubDetail.getContent() != null) {
        publi.setContent(pubDetail.getContent());
      }
      if (pubDetail.getStatus() != null) {
        publi.setStatus(pubDetail.getStatus());
      }
      publi.setUpdaterId(pubDetail.getUpdaterId());
      if (pubDetail.isUpdateDateMustBeSet()) {
        if (forceUpdateDate) {
          // In import case, we can force the update date to an old value
          if (pubDetail.getUpdateDate() != null) {
            publi.setUpdateDate(pubDetail.getUpdateDate());
          } else {
            publi.setUpdateDate(new Date());
          }
        } else {
          publi.setUpdateDate(new Date());
        }
      }
      if (pubDetail.getValidatorId() != null) {
        publi.setValidatorId(pubDetail.getValidatorId());
      }

      if (pubDetail.getValidateDate() != null) {
        publi.setValidateDate(new Date());
      }
      publi.setBeginHour(pubDetail.getBeginHour());
      publi.setEndHour(pubDetail.getEndHour());
      if (pubDetail.getAuthor() != null) {
        publi.setAuthor(pubDetail.getAuthor());
      }
      publi.setTargetValidatorId(pubDetail.getTargetValidatorId());

      if (pubDetail.getInfoId() != null) {
        publi.setInfoId(pubDetail.getInfoId());
      }

      publi.setCloneId(pubDetail.getCloneId());
      publi.setCloneStatus(pubDetail.getCloneStatus());
      publi.setDraftOutDate(pubDetail.getDraftOutDate());

      if (pubDetail.getLanguage() != null) {
        publi.setLanguage(pubDetail.getLanguage());
      }

      if (pubDetail.isRemoveTranslation()) {
        try {
          // Remove of a translation is required
          if (oldLang.equalsIgnoreCase(pubDetail.getLanguage())) {
            // Default language = translation
            List<PublicationI18N> translations = PublicationI18NDAO.getTranslations(con, publi
                .getPK());
            if (translations != null && !translations.isEmpty()) {
              PublicationI18N translation = translations.get(0);
              publi.setLanguage(translation.getLanguage());
              publi.setName(translation.getName());
              publi.setDescription(translation.getDescription());
              publi.setKeywords(translation.getKeywords());
              PublicationI18NDAO.removeTranslation(con, translation.getId());
            }
          } else {
            PublicationI18NDAO.removeTranslation(con, pubDetail.getTranslationId());
            publi.setName(oldName);
            publi.setDescription(oldDesc);
            publi.setKeywords(oldKeywords);
            publi.setLanguage(oldLang);
          }
        } catch (SQLException e) {
          throw new PublicationRuntimeException("PublicationEJB.setDetail()",
              SilverpeasRuntimeException.ERROR, "publication.CANNOT_MANAGE_TRANSLATIONS", e);
        }
      } else {
        // Add or update a translation
        if (pubDetail.getLanguage() != null) {
          if (oldLang == null) {
            // translation for the first time
            publi.setLanguage(I18NHelper.defaultLanguage);
          }
          if (oldLang != null && !oldLang.equalsIgnoreCase(pubDetail.getLanguage())) {
            PublicationI18N translation = new PublicationI18N(pubDetail);
            String translationId = pubDetail.getTranslationId();
            try {
              if (translationId != null && !translationId.equals("-1")) {
                PublicationI18NDAO.updateTranslation(con, translation);
              } else {
                PublicationI18NDAO.addTranslation(con, translation);
              }
            } catch (Exception e) {
              throw new PublicationRuntimeException("PublicationEJB.setDetail()",
                  SilverpeasRuntimeException.ERROR, "publication.CANNOT_MANAGE_TRANSLATIONS", e);
            }
            publi.setName(oldName);
            publi.setDescription(oldDesc);
            publi.setKeywords(oldKeywords);
            publi.setLanguage(oldLang);
          }
        }
      }
      getTranslations(publi);
      PublicationDAO.storeRow(con, publi);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.ejbStore()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "PubId = "
          + pubDetail.getPK().getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  private void getTranslations(PublicationDetail publi) {
    PublicationI18N translation = new PublicationI18N(publi.getLanguage(), publi.getName(), publi
        .getDescription(), publi.getKeywords());
    List<Translation> translations = new ArrayList<Translation>();
    translations.add(translation);
    Connection con = getConnection();
    try {
      translations.addAll(PublicationI18NDAO.getTranslations(con, publi.getPK()));
      publi.setTranslations(translations);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.getTranslations()",
          SilverpeasRuntimeException.ERROR, "publication.CANNOT_GET_TRANSLATIONS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<ValidationStep> getValidationSteps(PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      return ValidationStepsDAO.getSteps(con, pubPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getValidationSteps()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_VALIDATION_STEPS_FAILED", pubPK.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public ValidationStep getValidationStepByUser(PublicationPK pubPK, String userId) {
    Connection con = getConnection();
    try {
      return ValidationStepsDAO.getStepByUser(con, pubPK, userId);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getValidationStepByUser()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_VALIDATION_STEP_FAILED",
          pubPK.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addValidationStep(ValidationStep step) {
    Connection con = getConnection();
    try {
      ValidationStepsDAO.addStep(con, step);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.addValidationStep()",
          SilverpeasRuntimeException.ERROR, "publication.ADDING_PUBLICATION_VALIDATION_STEP_FAILED",
          step.getPubPK().toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void removeValidationSteps(PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      ValidationStepsDAO.removeSteps(con, pubPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.removeValidationSteps()",
          SilverpeasRuntimeException.ERROR,
          "publication.REMOVING_PUBLICATION_VALIDATION_STEPS_FAILED", pubPK.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addFather(PublicationPK pubPK, NodePK fatherPK) {
    SilverTrace.info("publication", "PublicationEJB.addFather()",
        "root.MSG_GEN_ENTER_METHOD", "fatherId = " + fatherPK.getId());
    Connection con = getConnection();
    try {
      PublicationFatherDAO.addFather(con, pubPK, fatherPK);
    } catch (SQLException re) {
      throw new PublicationRuntimeException("PublicationBmEJB.addFather()",
          SilverpeasRuntimeException.ERROR, "publication.ADDING_FATHER_TO_PUBLICATION_FAILED",
          "pubId = " + pubPK.getId() + " and fatherId = " + fatherPK.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void removeFather(PublicationPK pubPK, NodePK fatherPK) {
    SilverTrace.info("publication", "PublicationEJB.removeFather()", "root.MSG_GEN_ENTER_METHOD",
        "fatherId = " + fatherPK.getId());
    Connection con = getConnection();
    try {
      PublicationFatherDAO.removeFather(con, pubPK, fatherPK);
    } catch (SQLException re) {
      throw new PublicationRuntimeException("PublicationBmEJB.removeFather()",
          SilverpeasRuntimeException.ERROR,
          "publication.REMOVING_FATHER_TO_PUBLICATION_FAILED", "pubId = "
          + pubPK.getId() + " and fatherId = " + fatherPK.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void removeFather(NodePK fatherPK) {
    Connection con = getConnection();
    try {
      PublicationPK pubPK = new PublicationPK("useless", fatherPK);
      PublicationFatherDAO.removeFatherToPublications(con, pubPK, fatherPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.removeFather()",
          SilverpeasRuntimeException.ERROR, "publication.REMOVING_FATHER_TO_ALL_PUBLICATIONS_FAILED",
          "fatherId = " + fatherPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void removeFathers(PublicationPK pubPK, Collection<String> fatherIds) {
    Connection con = getConnection();
    try {
      PublicationFatherDAO.removeFathersToPublications(con, pubPK, fatherIds);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.removeFathers()",
          SilverpeasRuntimeException.ERROR,
          "publication.REMOVING_FATHERS_TO_ALL_PUBLICATIONS_FAILED",
          "fatherIds = " + fatherIds.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void removeAllFather(PublicationPK pubPK) {
    SilverTrace.info("publication", "PublicationEJB.removeAllFather()", "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      PublicationFatherDAO.removeAllFather(con, pubPK);
      deleteIndex(pubPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.removeAllFather()",
          SilverpeasRuntimeException.ERROR, "publication.REMOVING_FATHERS_TO_PUBLICATION_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getOrphanPublications(PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> pubDetails = PublicationDAO.getOrphanPublications(con, pubPK);
      if (I18NHelper.isI18N) {
        setTranslations(con, pubDetails);
      }
      return pubDetails;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getOrphanPublications()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getNotOrphanPublications(PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> pubDetails = PublicationDAO.getNotOrphanPublications(con,
          pubPK);
      if (I18NHelper.isI18N) {
        setTranslations(con, pubDetails);
      }
      return pubDetails;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getNotOrphanPublications()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteOrphanPublicationsByCreatorId(PublicationPK pubPK, String creatorId) {
    Connection con = getConnection();
    try {
      PublicationDAO.deleteOrphanPublicationsByCreatorId(con, pubPK, creatorId);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.deleteOrphanPublicationsByCreatorId()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "creatorId = " + creatorId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getUnavailablePublicationsByPublisherId(PublicationPK pubPK,
      String publisherId, String nodeId) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> pubDetails = PublicationDAO
          .getUnavailablePublicationsByPublisherId(con, pubPK, publisherId, nodeId);
      if (I18NHelper.isI18N) {
        setTranslations(con, pubDetails);
      }
      return pubDetails;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getUnavailablePublicationsByPublisherId()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "publisherId = " + publisherId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<NodePK> getAllFatherPK(PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      return PublicationFatherDAO.getAllFatherPK(con, pubPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getAllFatherPK()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_FATHERS_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<Alias> getAlias(PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      return PublicationFatherDAO.getAlias(con, pubPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getAlias()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_FATHERS_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addAlias(PublicationPK pubPK, List<Alias> aliases) {
    Connection con = getConnection();
    try {
      if (aliases != null && !aliases.isEmpty()) {
        for (Alias alias : aliases) {
          PublicationFatherDAO.addAlias(con, pubPK, alias);
          PublicationDAO.invalidateLastPublis(alias.getInstanceId());
        }
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.addAlias()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_FATHERS_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void removeAlias(PublicationPK pubPK, List<Alias> aliases) {
    Connection con = getConnection();
    try {
      if (aliases != null && !aliases.isEmpty()) {
        for (Alias alias : aliases) {
          PublicationFatherDAO.removeAlias(con, pubPK, alias);
          PublicationDAO.invalidateLastPublis(alias.getInstanceId());
        }
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.removeAlias()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_FATHERS_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK) {
    return getDetailsByFatherPK(fatherPK, null);
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting) {
    return getDetailsByFatherPK(fatherPK, sorting, true);
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting,
      boolean filterOnVisibilityPeriod) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> publis = PublicationDAO.selectByFatherPK(con, fatherPK,
          sorting, filterOnVisibilityPeriod);
      if (I18NHelper.isI18N) {
        setTranslations(con, publis);
      }
      return publis;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getDetailsByFatherPK()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", "fatherPK = "
          + fatherPK, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting,
      boolean filterOnVisibilityPeriod, String userId) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> publis = PublicationDAO.selectByFatherPK(con, fatherPK,
          sorting, filterOnVisibilityPeriod, userId);
      if (I18NHelper.isI18N) {
        setTranslations(con, publis);
      }
      return publis;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getDetailsByFatherPK()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", "fatherPK = "
          + fatherPK, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsNotInFatherPK(NodePK fatherPK) {
    return getDetailsNotInFatherPK(fatherPK, null);
  }

  @Override
  public Collection<PublicationDetail> getDetailsNotInFatherPK(NodePK fatherPK, String sorting) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> detailList = PublicationDAO.selectNotInFatherPK(con, fatherPK,
          sorting);
      if (I18NHelper.isI18N) {
        setTranslations(con, detailList);
      }
      return detailList;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getDetailsNotInFatherPK()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", "fatherPK = "
          + fatherPK, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsByBeginDateDescAndStatus(PublicationPK pk,
      String status, int nbPubs) {
    Connection con = getConnection();
    try {
      List<PublicationDetail> result = new ArrayList<PublicationDetail>(nbPubs);
      Collection<PublicationDetail> detailList = PublicationDAO.selectByBeginDateDescAndStatus(con,
          pk, status);
      Iterator<PublicationDetail> it = detailList.iterator();
      int i = 0;
      while (it.hasNext() && i < nbPubs) {
        result.add(it.next());
        i++;
      }
      if (I18NHelper.isI18N) {
        setTranslations(con, result);
      }
      return result;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getDetailsByBeginDateDescAndStatus()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", "status = "
          + status, e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public Collection<PublicationDetail> getDetailsByBeginDateDescAndStatusAndNotLinkedToFatherId(
      PublicationPK pk, String status, int nbPubs, String fatherId) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> detailList = PublicationDAO.
          selectByBeginDateDescAndStatusAndNotLinkedToFatherId(con, pk, status, fatherId, nbPubs);
      if (I18NHelper.isI18N) {
        setTranslations(con, detailList);
      }
      return detailList;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getDetailsByBeginDateDescAndStatusAndNotLinkedToFatherId()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", "fatherId = "
          + fatherId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsByBeginDateDesc(PublicationPK pk, int nbPubs) {
    Connection con = getConnection();
    try {
      List<PublicationDetail> result = new ArrayList<PublicationDetail>(nbPubs);
      Collection<PublicationDetail> detailList = PublicationDAO.selectByBeginDateDesc(con, pk);
      Iterator<PublicationDetail> it = detailList.iterator();
      int i = 0;
      while (it.hasNext() && i < nbPubs) {
        result.add(it.next());
        i++;
      }
      if (I18NHelper.isI18N) {
        setTranslations(con, result);
      }
      return result;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getDetailsByBeginDateDesc()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "nbPubs = " + nbPubs, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<ModelDetail> getAllModelsDetail() {
    Connection con = getConnection();
    try {
      return InfoDAO.getAllModelsDetail(con);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getAllModelsDetail()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_MODELS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public ModelDetail getModelDetail(ModelPK modelPK) {
    Connection con = getConnection();
    try {
      return InfoDAO.getModelDetail(con, modelPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getModelDetail()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_MODEL_FAILED", "modelPK = "
          + modelPK, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void createInfoDetail(PublicationPK pubPK, ModelPK modelPK, InfoDetail infos) {
    SilverTrace.info("publication", "PublicationEJB.createInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD", "modelId = " + modelPK.getId());
    Connection con = getConnection();
    PublicationDetail detail = getDetail(pubPK);
    try {
      InfoPK iPK = InfoDAO.createInfo(con, modelPK, pubPK);
      if (infos != null) {
        infos.setPK(iPK);
        InfoDAO.addInfoItems(con, infos);
        detail.setUpdateDate(new Date());
      }
      detail.setInfoId(iPK.getId());
      PublicationDAO.storeRow(con, detail);
      if (infos != null) {
        createIndex(pubPK, false, infos.getIndexOperation());
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.createInfoDetail()",
          SilverpeasRuntimeException.ERROR, "publication.CREATING_PUBLICATION_DETAIL_FAILED",
          "pubId = " + pubPK.getId() + ", modelPK = " + modelPK + ", infos = " + infos, e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public void createInfoModelDetail(PublicationPK pk, ModelPK modelPK, InfoDetail infos) {
    SilverTrace.info("publication", "PublicationEJB.createInfoDetail()",
        "root.MSG_GEN_ENTER_METHOD", "modelId = " + modelPK.getId());
    Connection con = getConnection();
    try {
      InfoPK iPK = InfoDAO.createInfo(con, modelPK, pk);
      PublicationDetail publi = PublicationDAO.loadRow(con, pk);
      if (infos != null) {
        infos.setPK(iPK);
        InfoDAO.addInfoItems(con, infos);
        publi.setUpdateDate(new Date());
      }
      publi.setInfoId(iPK.getId());
      PublicationDAO.storeRow(con, publi);
      if (infos != null) {
        createIndex(pk, false, infos.getIndexOperation());
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.createInfoModelDetail()",
          SilverpeasRuntimeException.ERROR, "publication.CREATING_PUBLICATION_DETAIL_FAILED",
          "pubId = " + pk.getId() + ", modelPK = " + modelPK + ", infos = " + infos, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public InfoDetail getInfoDetail(PublicationPK pubPK) {
    SilverTrace.info("publication", "PublicationEJB.getInfoDetail()", "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    PublicationDetail detail = getDetail(pubPK);
    try {
      InfoDetail result = InfoDAO.getInfoDetailByInfoPK(con, new InfoPK(detail.getInfoId(), detail
          .getPK()));
      return result;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getInfoDetail()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_DETAIL_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateInfoDetail(PublicationPK pubPK, InfoDetail infos) {
    Connection con = getConnection();
    try {
      PublicationDetail detail = getDetail(pubPK);
      InfoPK infoPK = new InfoPK(detail.getInfoId(), pubPK);
      if (StringUtil.isInteger(detail.getInfoId()) && !"0".equals(detail.getInfoId())) {
        SilverTrace.info("publication", "PublicationEJB.updateInfoDetail()",
            "root.MSG_GEN_ENTER_METHOD");
        InfoDetail old = getInfoDetail(pubPK);
        List<InfoTextDetail> newText = new ArrayList<InfoTextDetail>();
        List<InfoTextDetail> oldText = new ArrayList<InfoTextDetail>();
        InfoDetail.selectToCreateAndToUpdateItems(infos.getInfoTextList(), old.getInfoTextList(),
            newText, oldText);

        List<InfoImageDetail> newImage = new ArrayList<InfoImageDetail>();
        List<InfoImageDetail> oldImage = new ArrayList<InfoImageDetail>();
        InfoDetail.selectToCreateAndToUpdateItems(infos.getInfoImageList(), old.getInfoImageList(),
            newImage, oldImage);

        List<InfoLinkDetail> newLink = new ArrayList<InfoLinkDetail>();
        List<InfoLinkDetail> oldLink = new ArrayList<InfoLinkDetail>();
        InfoDetail.selectToCreateAndToUpdateItems(infos.getInfoLinkList(), old.getInfoLinkList(),
            newLink, oldLink);
        if ("0".equals(detail.getInfoId())) {
          createInfoDetail(pubPK, new ModelPK("0", pubPK), new InfoDetail(infoPK, newText, newImage,
              newLink, ""));
        } else {
          InfoDAO.updateInfoItems(con, new InfoDetail(infoPK, oldText, oldImage, oldLink, ""),
              infoPK);
          InfoDAO.addInfoItems(con, new InfoDetail(infoPK, newText, newImage, newLink, ""));
          detail.setUpdateDate(new Date());
          PublicationDAO.storeRow(con, detail);
        }

        if (infos != null) {
          createIndex(pubPK, false, infos.getIndexOperation());
        }
      } else {
        // XML Template
        // Only infoLinks are used
        Collection<InfoLinkDetail> links = infos.getInfoLinkList();
        if (links != null && !links.isEmpty()) {
          for (InfoLinkDetail link : links) {
            PublicationPK targetPK = new PublicationPK(link.getTargetId(), pubPK.getInstanceId());
            SeeAlsoDAO.addLink(con, pubPK, targetPK);
          }
        }
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.updateInfoDetail()",
          SilverpeasRuntimeException.ERROR, "publication.UPDATING_INFO_DETAIL_FAILED",
          "pubId = " + pubPK.getId(), e);
    } catch (UtilException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.updateInfoDetail()",
          SilverpeasRuntimeException.ERROR, "publication.UPDATING_INFO_DETAIL_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Removes links between publications and the specified publication
   *
   * @param pubPK
   * @param links list of links to remove
   * @
   */
  @Override
  public void deleteInfoLinks(PublicationPK pubPK, List<ForeignPK> links) {
    Connection con = getConnection();
    try {
      for (ForeignPK link : links) {
        PublicationPK targetPK = new PublicationPK(link.getId(), link.getInstanceId());
        SeeAlsoDAO.deleteLink(con, pubPK, targetPK);
      }
      PublicationDetail detail = PublicationDAO.loadRow(con, pubPK);
      detail.setUpdateDate(new Date());
      PublicationDAO.storeRow(con, detail);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.deleteInfoLinks()",
          SilverpeasRuntimeException.ERROR, "publication.UPDATING_INFO_DETAIL_FAILED", "pubId = "
          + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public CompletePublication getCompletePublication(PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      PublicationDetail detail = PublicationDAO.loadRow(con, pubPK);
      if (I18NHelper.isI18N) {
        setTranslations(con, detail);
      }
      InfoPK infoPK = new InfoPK(detail.getInfoId(), pubPK);
      InfoDetail infoDetail = InfoDAO.getInfoDetailByInfoPK(con, infoPK);
      ModelDetail modelDetail = InfoDAO.getModelDetail(con, infoPK);
      List<ForeignPK> links = SeeAlsoDAO.getLinks(con, pubPK);
      List<ForeignPK> reverseLinks = SeeAlsoDAO.getReverseLinks(con, pubPK);
      CompletePublication cp = new CompletePublication(detail, modelDetail, infoDetail, links,
          reverseLinks);
      cp.setValidationSteps(getValidationSteps(pubPK));
      return cp;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getCompletePublication()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_FAILED", "pubId = "
          + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> searchByKeywords(String query, PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> resultList = PublicationDAO.searchByKeywords(con, query, pubPK);
      if (I18NHelper.isI18N) {
        setTranslations(con, resultList);
      }
      return resultList;
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.searchByKeywords()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", "query = "
          + query, e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public Collection<PublicationDetail> getPublications(Collection<PublicationPK> publicationPKs) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> publications = PublicationDAO.selectByPublicationPKs(con,
          publicationPKs);
      if (I18NHelper.isI18N) {
        setTranslations(con, publications);
      }
      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getPublications()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "publicationPKs = " + publicationPKs, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getPublicationsByStatus(String status, PublicationPK pubPK) {
    Connection con = getConnection();

    try {
      Collection<PublicationDetail> publications = PublicationDAO.selectByStatus(con, pubPK,
          status);
      if (I18NHelper.isI18N) {
        setTranslations(con, publications);
      }
      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getPublicationsByStatus()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATIONS_FAILED", "status = " + status, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationPK> getPublicationPKsByStatus(String status,
      List<String> componentIds) {
    Connection con = getConnection();
    try {
      return PublicationDAO.selectPKsByStatus(con, componentIds, status);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getPublicationPKsByStatus()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", "status = "
          + status + ", componentIds = " + componentIds, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getPublicationsByStatus(String status,
      List<String> componentIds) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> publications = PublicationDAO.selectByStatus(con, componentIds,
          status);
      if (I18NHelper.isI18N) {
        setTranslations(con, publications);
      }
      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getPublicationsByStatus()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", "status = "
          + status + ", componentIds = " + componentIds, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getNbPubInFatherPKs(Collection<NodePK> fatherPKs) {
    Connection con = getConnection();
    try {
      return PublicationDAO.getNbPubInFatherPKs(con, fatherPKs);
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getNbPubInFatherPKs()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_NUMBER_OF_PUBLICATIONS_FAILED", "fatherPKs = "
          + fatherPKs.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public NodeTree getDistributionTree(String instanceId, String statusSubQuery,
      boolean checkVisibility) {
    Connection con = getConnection();
    try {
      return PublicationDAO.getDistributionTree(con, instanceId, statusSubQuery, checkVisibility);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getDistributionTree()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_NUMBER_OF_PUBLICATIONS_FAILED",
          "instanceId = " + instanceId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getNbPubByFatherPath(NodePK fatherPK, String fatherPath) {
    Connection con = getConnection();
    try {
      return PublicationDAO.getNbPubByFatherPath(con, fatherPK, fatherPath);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getNbPubByFatherPath()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_NUMBER_OF_PUBLICATIONS_FAILED",
          "fatherPath = " + fatherPath, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherIds(List<String> fatherIds,
      PublicationPK pubPK, boolean filterOnVisibilityPeriod) {
    return getDetailsByFatherIdsAndStatusList(fatherIds, pubPK, null, null,
        filterOnVisibilityPeriod);
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherIds(List<String> fatherIds,
      PublicationPK pubPK) {
    return getDetailsByFatherIdsAndStatus(fatherIds, pubPK, null, null);
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherIds(List<String> fatherIds,
      PublicationPK pubPK, String sorting) {
    return getDetailsByFatherIdsAndStatus(fatherIds, pubPK, sorting, null);
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherIdsAndStatus(List<String> fatherIds,
      PublicationPK pubPK, String sorting, String status) {
    ArrayList<String> statusList = null;
    if (status != null) {
      statusList = new ArrayList<String>();
      statusList.add(status);
    }
    return getDetailsByFatherIdsAndStatusList(fatherIds, pubPK, sorting, statusList);
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherIdsAndStatusList(List<String> fatherIds,
      PublicationPK pubPK, String sorting, List<String> status) {
    return getDetailsByFatherIdsAndStatusList(fatherIds, pubPK, sorting, status, true);
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherIdsAndStatusList(List<String> fatherIds,
      PublicationPK pubPK, String sorting, List<String> status,
      boolean filterOnVisibilityPeriod) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> detailList = PublicationDAO.selectByFatherIds(con, fatherIds,
          pubPK, sorting, status, filterOnVisibilityPeriod);
      if (I18NHelper.isI18N) {
        setTranslations(con, detailList);
      }
      return detailList;
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getDetailsByFatherIdsAndStatus()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "fatherIds = " + fatherIds, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationPK> getPubPKsInFatherPKs(Collection<WAPrimaryKey> fatherPKs) {
    Connection con = getConnection();
    try {
      return PublicationFatherDAO.getPubPKsInFatherPKs(con, fatherPKs);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getPubPKsInFatherPKs()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_PK_FAILED",
          "fatherPKs = " + fatherPKs, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationPK> getPubPKsInFatherPK(NodePK fatherPK) {
    Connection con = getConnection();
    try {
      return PublicationFatherDAO.getPubPKsInFatherPK(con, fatherPK);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getPubPKsInFatherPK()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_PK_FAILED",
          "fatherPK = " + fatherPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void processWysiwyg(PublicationPK pubPK) {
    createIndex(pubPK);
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  private Connection getConnection() {
    try {
      return DBUtil.makeConnection(JNDINames.PUBLICATION_DATASOURCE);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void updateIndexEntryWithWysiwygContent(FullIndexEntry indexEntry,
      PublicationDetail pubDetail) {
    PublicationPK pubPK = pubDetail.getPK();
    try {
      if (pubPK != null) {
        SilverTrace.info("publication", "PublicationBmEJB.updateIndexEntryWithWysiwygContent()",
            "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry + ", pubPK = " + pubPK);
        Iterator<String> languages = pubDetail.getLanguages();
        while (languages.hasNext()) {
          String language = languages.next();
          ForeignPK foreignPk = new ForeignPK(pubPK);
          List<SimpleDocument> docs = AttachmentServiceFactory.getAttachmentService()
              .listDocumentsByForeignKeyAndType(foreignPk, DocumentType.wysiwyg, language);
          if (!docs.isEmpty()) {
            String wysiwygPath = docs.get(0).getAttachmentPath();
            indexEntry.addFileContent(wysiwygPath, null, "text/html", language);
            String wysiwygContent = WysiwygController.loadContent(docs.get(0), language);
            // index embedded linked attachment (links presents in wysiwyg content)
            try {
              List<String> embeddedAttachmentIds = WysiwygController.getEmbeddedAttachmentIds(
                  wysiwygContent);
              WysiwygController.indexEmbeddedLinkedFiles(indexEntry, embeddedAttachmentIds);
            } catch (Exception e) {
              SilverTrace.warn("form", "PublicationBmEJB.updateIndexEntryWithWysiwygContent",
                  "root.MSG_GEN_ENTER_METHOD", "Unable to extract linked files from object"
                  + indexEntry.getObjectId(), e);
            }
          }
        }
      }
    } catch (Exception e) {
      // No wysiwyg associated
    }
  }

  private void updateIndexEntryWithXMLFormContent(FullIndexEntry indexEntry,
      PublicationDetail pubDetail) {
    SilverTrace.info("publication", "PublicationBmEJB.updateIndexEntryWithXMLFormContent()",
        "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString()
        + ", pubDetail.getInfoId() = " + pubDetail.getInfoId());
    if (!StringUtil.isInteger(pubDetail.getInfoId())) {
      try {
        PublicationTemplate pub = PublicationTemplateManager.getInstance().getPublicationTemplate(
            pubDetail.getPK().getInstanceId() + ':' + pubDetail.getInfoId());

        RecordSet set = pub.getRecordSet();
        set.indexRecord(pubDetail.getPK().getId(), pubDetail.getInfoId(), indexEntry);
      } catch (Exception e) {
        SilverTrace.error("publication",
            "PublicationBmEJB.updateIndexEntryWithXMLFormContent()", "", e);
      }
    }
  }

  @Override
  public void createIndex(PublicationDetail pubDetail) {
    SilverTrace.info("publication", "PublicationBmEJB.createIndex()",
        "root.MSG_GEN_ENTER_METHOD", "pubDetail.getIndexOperation() = "
        + pubDetail.getIndexOperation());
    if (pubDetail.getIndexOperation() == IndexManager.ADD
        || pubDetail.getIndexOperation() == IndexManager.READD) {
      SilverTrace.info("publication", "PublicationBmEJB.createIndex()",
          "root.MSG_GEN_PARAM_VALUE", "pubDetail = " + pubDetail.toString());
      try {
        FullIndexEntry indexEntry = getFullIndexEntry(pubDetail);
        if (indexEntry != null) {
          IndexEngineProxy.addIndexEntry(indexEntry);
        }
      } catch (Exception e) {
        SilverTrace.error("publication", "PublicationBmEJB.createIndex()",
            "root.MSG_GEN_ENTER_METHOD", "pubDetail = " + pubDetail.toString()
            + ", indexEngineBm.addIndexEntry() failed !", e);
      }
    }
  }

  @Override
  public void createIndex(PublicationPK pubPK) {
    createIndex(pubPK, true);
  }

  private void createIndex(PublicationPK pubPK, boolean processContent, int indexOperation) {
    SilverTrace.info("publication", "PublicationBmEJB.createIndex()",
        "root.MSG_GEN_ENTER_METHOD", "processContent = "
        + processContent + ", indexOperation = " + indexOperation);
    if (indexOperation == IndexManager.ADD || indexOperation == IndexManager.READD) {
      SilverTrace.info("publication", "PublicationBmEJB.createIndex()",
          "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString());
      try {
        PublicationDetail pubDetail = getDetail(pubPK);
        if (pubDetail != null) {
          // Index the Publication Header
          FullIndexEntry indexEntry = getFullIndexEntry(pubDetail);
          // Index the Publication Content
          if (processContent) {
            updateIndexEntryWithWysiwygContent(indexEntry, pubDetail);
            updateIndexEntryWithXMLFormContent(indexEntry, pubDetail);
          }
          AttachmentServiceFactory.getAttachmentService().updateIndexEntryWithDocuments(indexEntry);
          IndexEngineProxy.addIndexEntry(indexEntry);
        }
      } catch (Exception e) {
        SilverTrace.error("publication", "PublicationBmEJB.createIndex()",
            "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK.toString()
            + ", indexEngineBm.addIndexEntry() failed !", e);
      }
    }
  }

  private void createIndex(PublicationPK pubPK, boolean processWysiwygContent) {
    createIndex(pubPK, processWysiwygContent, IndexManager.ADD);
  }

  private FullIndexEntry getFullIndexEntry(PublicationDetail pubDetail) {
    FullIndexEntry indexEntry = null;

    if (pubDetail != null) {
      // Index the Publication Header
      indexEntry = new FullIndexEntry(pubDetail.getPK().getComponentName(),
          "Publication", pubDetail.getPK().getId());
      indexEntry.setIndexId(true);

      Iterator<String> languages = pubDetail.getLanguages();
      while (languages.hasNext()) {
        String language = languages.next();
        PublicationI18N translation = (PublicationI18N) pubDetail.getTranslation(language);

        indexEntry.setTitle(translation.getName(), language);
        indexEntry.setPreview(translation.getDescription(), language);
        indexEntry.setKeywords(translation.getKeywords() + " "
            + pubDetail.getAuthor(), language);
      }

      indexEntry.setLang("fr");
      indexEntry.setCreationDate(pubDetail.getCreationDate());
      indexEntry.setLastModificationDate(pubDetail.getUpdateDate());
      if (pubDetail.getBeginDate() != null) {
        indexEntry.setStartDate(formatter.format(pubDetail.getBeginDate()));
      }
      if (pubDetail.getEndDate() != null) {
        indexEntry.setEndDate(formatter.format(pubDetail.getEndDate()));
      }
      indexEntry.setCreationUser(pubDetail.getCreatorId());
      indexEntry.setLastModificationUser(pubDetail.getUpdaterId());
      // index creator's full name
      if (indexAuthorName) {
        try {
          UserDetail ud = AdminReference.getAdminService().getUserDetail(pubDetail.getCreatorId());
          if (ud != null) {
            indexEntry.addTextContent(ud.getDisplayedName());
          }
        } catch (AdminException e) {
          // unable to find user detail, ignore and don't index creator's full
          // name
        }
      }

      try {
        ThumbnailDetail thumbnail = pubDetail.getThumbnail();
        if (thumbnail != null) {
          String[] imageProps = ThumbnailController.getImageAndMimeType(thumbnail, -1, -1);
          indexEntry.setThumbnail(imageProps[0]);
          indexEntry.setThumbnailMimeType(imageProps[1]);
        }
      } catch (Exception e) {
        throw new PublicationRuntimeException("PublicationBmEJB.getFullIndexEntry()",
            SilverpeasRuntimeException.ERROR, "publication.GETTING_FULL_INDEX_ENTRY", e);
      }
      indexEntry.setThumbnailDirectory(thumbnailDirectory);
    }

    return indexEntry;
  }

  /**
   * Called on : - deletePublication()
   */
  @Override
  public void deleteIndex(PublicationPK pubPK) {
    SilverTrace.info("publication", "PublicationBmEJB.deleteIndex()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK);
    IndexEntryPK indexEntry = new IndexEntryPK(pubPK.getComponentName(), "Publication", pubPK
        .getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
    // Suppression du nuage de tags lors de la suppression de l'index (et pas
    // lors de l'envoi de la publication dans la corbeille).
    if (useTagCloud) {
      deleteTagCloud(pubPK);
    }
    // idem pour les notations
    if (useNotation) {
      deleteNotation(pubPK);
    }
  }

  /**
   * Method declaration
   *
   * @param pubPK
   * @param sorting
   * @return
   * @
   * @see
   */
  @Override
  public Collection<PublicationDetail> getAllPublications(PublicationPK pubPK, String sorting) {
    Connection con = getConnection();
    try {
      return PublicationDAO.selectAllPublications(con, pubPK, sorting);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getAllPublications()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getAllPublications(PublicationPK pubPK) {
    return getAllPublications(pubPK, null);
  }

  @Override
  public PublicationDetail getDetailByName(PublicationPK pubPK, String pubName) {
    SilverTrace.info("publication", "PublicationBmEJB.getDetailByName()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK + ", pubName = " + pubName);
    Connection con = getConnection();
    try {
      PublicationPK primary = PublicationDAO.selectByPublicationName(con, pubPK, pubName);
      if (primary != null) {
        return primary.pubDetail;
      } else {
        SilverTrace.debug("publication", "PublicationEJB.ejbFindByName()",
            "root.EX_CANT_FIND_ENTITY", "name = " + pubName);
        throw new PublicationRuntimeException("PublicationBmEJB.getDetailByName()",
            SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_HEADER_FAILED",
            "pubPK = " + pubPK + ", pubName = " + pubName);
      }
    } catch (Exception re) {
      throw new PublicationRuntimeException("PublicationBmEJB.getDetailByName()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_HEADER_FAILED",
          "pubPK = " + pubPK + ", pubName = " + pubName, re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public PublicationDetail getDetailByNameAndNodeId(PublicationPK pubPK,
      String pubName, int nodeId) {
    SilverTrace.info("publication", "PublicationBmEJB.getDetailByNameAndNodeId()",
        "root.MSG_GEN_ENTER_METHOD", "pubPK = " + pubPK + ", pubName = " + pubName + ", nodeId="
        + nodeId);
    Connection con = getConnection();
    try {
      PublicationPK primary = PublicationDAO.selectByPublicationNameAndNodeId(
          con, pubPK, pubName, nodeId);
      if (primary != null) {
        return primary.pubDetail;
      } else {
        SilverTrace.debug("publication", "PublicationEJB.getDetailByNameAndNodeId()",
            "root.EX_CANT_FIND_ENTITY", "name=" + pubName + ", nodeId=" + nodeId);
        throw new PublicationRuntimeException("PublicationBmEJB.getDetailByNameAndNodeId()",
            SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_HEADER_FAILED",
            "pubPK = " + pubPK + ", pubName = " + pubName + ", nodeId=" + nodeId);
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.ejbFindByNameAndNodeId()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_FIND_ENTITY",
          "name = " + pubName + ", parent nodeId=" + nodeId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailBetweenDate(String beginDate, String endDate,
      String instanceId) {
    Connection con = getConnection();
    try {

      Collection<PublicationDetail> detailList = PublicationDAO.selectBetweenDate(con, beginDate,
          endDate, instanceId);
      List<PublicationDetail> result = new ArrayList<PublicationDetail>(detailList);
      if (I18NHelper.isI18N) {
        setTranslations(con, result);
      }
      return result;
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getDetailBetweenDate()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", "", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private PublicationDetail loadTranslations(PublicationDetail detail) {
    PublicationI18N translation = new PublicationI18N(detail.getLanguage(), detail.getName(), detail
        .getDescription(), detail.getKeywords());
    List translations = new ArrayList();
    translations.add(translation);
    Connection con = getConnection();
    try {
      translations.addAll(PublicationI18NDAO.getTranslations(con, detail.getPK()));
      detail.setTranslations(translations);
      return detail;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.getTranslations()",
          SilverpeasRuntimeException.ERROR, "publication.CANNOT_GET_TRANSLATIONS", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Create the tagclouds corresponding to the publication detail.
   *
   * @param pubDetail The detail of the publication.
   * @
   */
  private void createTagCloud(PublicationDetail pubDetail) {
    String keywords = pubDetail.getKeywords();
    if (keywords != null) {
      TagCloud tagCloud = new TagCloud(pubDetail.getInstanceId(), pubDetail.getId(),
          TagCloud.TYPE_PUBLICATION);
      StringTokenizer st = new StringTokenizer(keywords, " ");
      String tag;
      String tagKey;
      ArrayList<String> tagList = new ArrayList<String>();
      while (st.hasMoreElements()) {
        tag = (String) st.nextElement();
        tagKey = TagCloudUtil.getTag(tag);
        if (!tagList.contains(tagKey)) {
          tagCloud.setTag(tagKey);
          tagCloud.setLabel(tag.toLowerCase());
          tagCloudBm.createTagCloud(tagCloud);
          tagList.add(tagKey);
        }
      }
    }
  }

  /**
   * Delete the tagclouds corresponding to the publication key.
   *
   * @param pubPK The primary key of the publication.
   * @
   */
  private void deleteTagCloud(PublicationPK pubPK) {
    tagCloudBm.deleteTagCloud(new TagCloudPK(pubPK.getId(), pubPK.getInstanceId()),
        TagCloud.TYPE_PUBLICATION);
  }

  /**
   * Update the tagclouds corresponding to the publication detail.
   *
   * @param pubDetail The detail of the publication.
   * @
   */
  private void updateTagCloud(PublicationDetail pubDetail) {
    deleteTagCloud(pubDetail.getPK());
    createTagCloud(pubDetail);
  }

  private void deleteNotation(PublicationPK pubPK) {
    notationBm.deleteNotation(new NotationPK(pubPK.getId(), pubPK.getInstanceId(),
        Notation.TYPE_PUBLICATION));
  }

  /**
   * Recupere les coordonnees de la publication (collection de nodePK)
   *
   * @param pubId
   * @param componentId
   * @return
   * @
   */
  @Override
  public Collection<Coordinate> getCoordinates(String pubId, String componentId) {
    SilverTrace.info("kmax", "KmeliaBmEjb.getPublicationCoordinates()",
        "root.MSG_GEN_ENTER_METHOD");
    PublicationPK pubPK = new PublicationPK(pubId, componentId);
    Collection<NodePK> fatherPKs = getAllFatherPK(pubPK);
    Iterator<NodePK> it = fatherPKs.iterator();
    ArrayList<String> coordinateIds = new ArrayList<String>();
    CoordinatePK coordinatePK = new CoordinatePK("unknown", pubPK);
    while (it.hasNext()) {
      String coordinateId = it.next().getId();
      coordinateIds.add(coordinateId);
    }
    Collection<Coordinate> coordinates = coordinatesBm.getCoordinatesByCoordinateIds(
        coordinateIds, coordinatePK);
    // Enrichit les coordonnees avec le nom du noeud
    Iterator<Coordinate> itCoordinates = coordinates.iterator();
    Iterator<CoordinatePoint> pointsIt = null;
    while (itCoordinates.hasNext()) {
      Coordinate coordinate = itCoordinates.next();
      Collection<CoordinatePoint> points = coordinate.getCoordinatePoints();
      Collection<CoordinatePoint> surePoints = new ArrayList<CoordinatePoint>();
      pointsIt = points.iterator();
      while (pointsIt.hasNext()) {
        CoordinatePoint point = pointsIt.next();
        try {
          NodeDetail node = nodeBm.getHeader(new NodePK("" + point.getNodeId(), componentId));
          point.setName(node.getName());
          point.setLevel(node.getLevel());
          point.setPath(node.getPath());
          surePoints.add(point);
        } catch (Exception e) {
          SilverTrace.info("kmelia", "KmeliaBmEJB.getPublicationCoordinates",
              "root.MSG_GEN_PARAM_VALUE", "node unfindable !");
        }
      }
      coordinate.setCoordinatePoints(surePoints);
    }
    SilverTrace.info("kmax", "KmeliaBmEJB.getPublicationCoordinates()",
        "root.MSG_GEN_EXIT_METHOD");
    return coordinates;
  }

  /**
   * Updates the publication links
   *
   * @param pubPK publication identifier which you want to update links
   * @param links list of publication to link with current.
   * @
   */
  @Override
  public void addLinks(PublicationPK pubPK, List<ForeignPK> links) {
    Connection con = getConnection();
    try {
      if (links != null) {
        // deletes existing links
        SeeAlsoDAO.deleteLinksByObjectId(con, pubPK);
        for (ForeignPK link : links) {
          PublicationPK targetPK = new PublicationPK(link.getId(), link.getInstanceId());
          SeeAlsoDAO.addLink(con, pubPK, targetPK);
        }
      }
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.addLinks()",
          SilverpeasRuntimeException.ERROR, "publication.UPDATING_INFO_DETAIL_FAILED", "pubId = "
          + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Constructor declaration
   *
   * @see
   */
  public PublicationBmEJB() {
  }

  /**
   * get my list of SocialInformationPublication according to options and number of Item and the
   * first Index
   *
   * @param userId
   * @param begin
   * @param end
   * @return List <SocialInformation>
   * @
   */
  @Override
  public List<SocialInformation> getAllPublicationsWithStatusbyUserid(String userId, Date begin,
      Date end) {
    Connection con = getConnection();
    try {
      return PublicationDAO.getAllPublicationsIDbyUserid(con, userId, begin, end);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getAllPublicationsWithStatusbyUserid",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_HEADER_FAILED",
          "userId = " + userId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get list of SocialInformationPublication of my contacts according to options and number of Item
   * and the first Index.
   *
   * @param myContactsIds
   * @param options
   * @param begin
   * @param end
   * @return
   * @
   */
  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(List<String> myContactsIds,
      List<String> options, Date begin, Date end) {
    Connection con = getConnection();
    try {
      return PublicationDAO.getSocialInformationsListOfMyContacts(con, myContactsIds, options,
          begin, end);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getSocialInformationsListOfMyContacts",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_HEADER_FAILED",
          " myContactsIds=" + myContactsIds + " options=" + options, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getPublicationsToDraftOut(boolean useClone) {
    Connection con = getConnection();
    try {
      return PublicationDAO.getPublicationsToDraftOut(con, useClone);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getPublicationsToDraftOut",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_TO_DRAFT_OUT_FAILED",
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationPK> getUpdatedPublicationPKsByStatus(String status, Date since,
      int maxSize, List<String> componentIds) {
    Connection con = getConnection();
    try {
      return PublicationDAO.selectUpdatedPublicationsSince(con, componentIds, status, since,
          maxSize);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationBmEJB.getPublicationPKsByStatus()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", "status = "
          + status + ", componentIds = " + componentIds, e);
    } finally {
      DBUtil.close(con);
    }
  }
  private static final boolean useTagCloud;
  private static final boolean useNotation;
  private static final boolean indexAuthorName;
  private static final String thumbnailDirectory;

  static {
    ResourceLocator publicationSettings = new ResourceLocator(
        "org.silverpeas.util.publication.publicationSettings", "");
    useTagCloud = publicationSettings.getBoolean("useTagCloud", false);
    useNotation = publicationSettings.getBoolean("useNotation", false);
    indexAuthorName = publicationSettings.getBoolean("indexAuthorName", false);
    thumbnailDirectory = publicationSettings.getString("imagesSubDirectory");
  }
}
