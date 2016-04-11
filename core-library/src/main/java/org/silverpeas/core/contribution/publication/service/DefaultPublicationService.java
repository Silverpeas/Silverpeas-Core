/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.contribution.publication.service;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.rating.service.RatingService;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.io.media.image.thumbnail.control.ThumbnailController;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.contribution.publication.dao.PublicationDAO;
import org.silverpeas.core.contribution.publication.dao.PublicationFatherDAO;
import org.silverpeas.core.contribution.publication.dao.PublicationI18NDAO;
import org.silverpeas.core.contribution.publication.dao.ValidationStepsDAO;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.coordinates.service.CoordinatesService;
import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.coordinates.model.CoordinatePK;
import org.silverpeas.core.node.coordinates.model.CoordinatePoint;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.publication.dao.SeeAlsoDAO;
import org.silverpeas.core.contribution.publication.model.Alias;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.NodeTree;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationI18N;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.PublicationRuntimeException;
import org.silverpeas.core.contribution.publication.model.ValidationStep;
import org.silverpeas.core.contribution.publication.social.SocialInformationPublication;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.contribution.publication.notification.PublicationEventNotifier;
import org.silverpeas.core.contribution.rating.model.ContributionRatingPK;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.admin.component.ComponentHelper;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@code PublicationService} to manage the publications in Silverpeas.
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultPublicationService implements PublicationService, ComponentInstanceDeletion {

  @Inject
  private NodeService nodeService;
  @Inject
  private CoordinatesService coordinatesService;
  @Inject
  private RatingService ratingService;
  @Inject
  private ComponentHelper componentHelper;
  @Inject
  private PublicationEventNotifier notifier;

  private SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy/MM/dd");

  @Override
  @Transactional
  public void delete(final String componentInstanceId) {
    try {
      ValidationStepsDAO.deleteComponentInstanceData(componentInstanceId);
      PublicationI18NDAO.deleteComponentInstanceData(componentInstanceId);
      PublicationFatherDAO.deleteComponentInstanceData(componentInstanceId);
      SeeAlsoDAO.deleteComponentInstanceData(componentInstanceId);
      PublicationDAO.deleteComponentInstanceData(componentInstanceId);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.delete()",
          SilverpeasRuntimeException.ERROR,
          "publication.DELETING_COMPONENT_INSTANCE_PUBLICATIONS_FAILED",
          "instanceId = " + componentInstanceId, e);
    }
  }

  @Override
  public PublicationDetail getDetail(PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      PublicationDetail publicationDetail = PublicationDAO.selectByPrimaryKey(con, pubPK);
      if (publicationDetail != null) {
        return loadTranslations(publicationDetail);
      }
      return null;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getDetail()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_HEADER_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  private void setTranslations(Connection con, PublicationDetail publi) {
    try {
      PublicationI18N translation =
          new PublicationI18N(publi.getLanguage(), publi.getName(), publi.getDescription(),
              publi.getKeywords());
      publi.addTranslation(translation);
      List translations = PublicationI18NDAO.getTranslations(con, publi.getPK());
      publi.setTranslations(translations);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.setTranslations()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_TRANSLATIONS_FAILED",
          "pubId = " + publi.getPK().getId(), e);
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
  @Transactional
  public PublicationPK createPublication(PublicationDetail detail) {
    Connection con = getConnection();
    try {
      int indexOperation = detail.getIndexOperation();
      int id;
      try {
        id = DBUtil.getNextId(detail.getPK().getTableName(), "pubId");
      } catch (SQLException ex) {
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
      if (I18NHelper.isI18nContentActivated) {
        try {
          createTranslations(con, detail);
        } catch (SQLException | UtilException ex) {
          throw new PublicationRuntimeException("PublicationEJB.ejbCreate()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_INSERT_TRANSLATIONS", ex);
        }
      }
      loadTranslations(detail);
      detail.setIndexOperation(indexOperation);
      createIndex(detail);
      return detail.getPK();
    } catch (Exception re) {
      throw new PublicationRuntimeException("DefaultPublicationService.createPublication()",
          SilverpeasRuntimeException.ERROR, "publication.CREATING_PUBLICATION_FAILED",
          "detail = " + detail.toString(), re);
    } finally {
      DBUtil.close(con);
    }

  }

  private void createTranslations(Connection con, PublicationDetail publication)
      throws SQLException, UtilException {
    if (publication.getTranslations() != null) {
      for (final PublicationI18N translation : publication.getTranslations().values()) {
        if (publication.getLanguage() != null &&
            !publication.getLanguage().equals(translation.getLanguage())) {
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
      moveRating(pk, fatherPK.getInstanceId());
      pk.setComponentName(fatherPK.getInstanceId());
      PublicationFatherDAO.removeAllFather(con, pk);
      PublicationFatherDAO.addFather(con, pk, fatherPK);
      if (indexIt) {
        createIndex(pk);
      }
    } catch (SQLException re) {
      throw new PublicationRuntimeException("DefaultPublicationService.movePublication()",
          SilverpeasRuntimeException.ERROR, "publication.MOVING_PUBLICATION_FAILED",
          "pubId = " + pk.getId(), re);
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
      throw new PublicationRuntimeException("DefaultPublicationService.changePublicationsOrder()",
          SilverpeasRuntimeException.ERROR, "publication.SORTING_PUBLICATIONS_FAILED",
          "pubIds = " + ids, e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public void changePublicationOrder(PublicationPK pubPK, NodePK nodePK, int direction) {
    // get all publications in given node
    List<PublicationDetail> publications =
        (List<PublicationDetail>) getDetailsByFatherPK(nodePK, "P.pubUpdateDate desc");
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
      throw new PublicationRuntimeException("DefaultPublicationService.changePublicationOrder()",
          SilverpeasRuntimeException.ERROR, "publication.MOVING_PUBLICATION_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  private int getIndexOfPublication(String pubId, List<PublicationDetail> publications) {
    int index = 0;
    if (publications != null) {
      for (PublicationDetail publi : publications) {
        if (pubId.equals(publi.getPK().getId())) {
          return index;
        }
        index++;
      }
    }
    return index;
  }

  @Override
  @Transactional
  public void removePublication(PublicationPK pk) {
    Connection con = getConnection();
    try {
      PublicationDetail publi = PublicationDAO.loadRow(con, pk);
      // delete links from another publication to removed publication
      SeeAlsoDAO.deleteLinksByObjectId(con, pk);
      SeeAlsoDAO.deleteLinksByTargetId(con, pk);
      // delete translations
      PublicationI18NDAO.removeTranslations(con, pk);

      deleteRating(pk);

      deleteIndex(pk);

      notifier.notifyEventOn(ResourceEvent.Type.DELETION, publi);

      // delete publication from database
      PublicationDAO.deleteRow(con, pk);
    } catch (java.sql.SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.ejbRemove()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_DELETE_ENTITY", "PubId = " + pk.getId(),
          e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void setDetail(PublicationDetail detail) {
    setDetail(detail, false);
  }

  @Override
  @Transactional
  public void setDetail(PublicationDetail detail, boolean forceUpdateDate) {
    Connection con = getConnection();
    try {
      int indexOperation = detail.getIndexOperation();
      updateDetail(detail, forceUpdateDate);
      if (detail.isRemoveTranslation()) {
        WysiwygController.deleteFile(detail.getPK().getInstanceId(), detail.getPK().getId(),
            detail.getLanguage());
        // remove xml content
        String infoId = detail.getInfoId();

        if (StringUtil.isDefined(infoId) && !StringUtil.isInteger(infoId)) {
          PublicationTemplate pubTemplate = PublicationTemplateManager.getInstance()
              .getPublicationTemplate(detail.getPK().getInstanceId() + ':' + infoId);
          RecordSet set = pubTemplate.getRecordSet();
          DataRecord data = set.getRecord(detail.getPK().getId(), detail.getLanguage());
          set.delete(data);
        }
      }

      if (indexOperation == IndexManager.ADD || indexOperation == IndexManager.READD) {
        createIndex(detail.getPK(), true, indexOperation);
      } else if (indexOperation == IndexManager.REMOVE) {
        deleteIndex(detail.getPK());
      }
    } catch (FormException | PublicationTemplateException re) {
      throw new PublicationRuntimeException("DefaultPublicationService.setDetail()",
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
            List<PublicationI18N> translations =
                PublicationI18NDAO.getTranslations(con, publi.getPK());
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
            } catch (UtilException | SQLException e) {
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
      loadTranslations(publi);
      PublicationDAO.storeRow(con, publi);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("PublicationEJB.ejbStore()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_STORE_ENTITY_ATTRIBUTES",
          "PubId = " + pubDetail.getPK().getId(), e);
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
      throw new PublicationRuntimeException("DefaultPublicationService.getValidationSteps()",
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
      throw new PublicationRuntimeException("DefaultPublicationService.getValidationStepByUser()",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_VALIDATION_STEP_FAILED", pubPK.toString(), e);
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
      throw new PublicationRuntimeException("DefaultPublicationService.addValidationStep()",
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
      throw new PublicationRuntimeException("DefaultPublicationService.removeValidationSteps()",
          SilverpeasRuntimeException.ERROR,
          "publication.REMOVING_PUBLICATION_VALIDATION_STEPS_FAILED", pubPK.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void addFather(PublicationPK pubPK, NodePK fatherPK) {

    Connection con = getConnection();
    try {
      PublicationFatherDAO.addFather(con, pubPK, fatherPK);
    } catch (SQLException re) {
      throw new PublicationRuntimeException("DefaultPublicationService.addFather()",
          SilverpeasRuntimeException.ERROR, "publication.ADDING_FATHER_TO_PUBLICATION_FAILED",
          "pubId = " + pubPK.getId() + " and fatherId = " + fatherPK.getId(), re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void removeFather(PublicationPK pubPK, NodePK fatherPK) {

    Connection con = getConnection();
    try {
      PublicationFatherDAO.removeFather(con, pubPK, fatherPK);
    } catch (SQLException re) {
      throw new PublicationRuntimeException("DefaultPublicationService.removeFather()",
          SilverpeasRuntimeException.ERROR, "publication.REMOVING_FATHER_TO_PUBLICATION_FAILED",
          "pubId = " + pubPK.getId() + " and fatherId = " + fatherPK.getId(), re);
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
      throw new PublicationRuntimeException("DefaultPublicationService.removeFather()",
          SilverpeasRuntimeException.ERROR,
          "publication.REMOVING_FATHER_TO_ALL_PUBLICATIONS_FAILED",
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
      throw new PublicationRuntimeException("DefaultPublicationService.removeFathers()",
          SilverpeasRuntimeException.ERROR,
          "publication.REMOVING_FATHERS_TO_ALL_PUBLICATIONS_FAILED",
          "fatherIds = " + fatherIds.toString(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void removeAllFather(PublicationPK pubPK) {
    SilverTrace
        .info("publication", "PublicationEJB.removeAllFather()", "root.MSG_GEN_ENTER_METHOD");
    Connection con = getConnection();
    try {
      PublicationFatherDAO.removeAllFather(con, pubPK);
      deleteIndex(pubPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.removeAllFather()",
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
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, pubDetails);
      }
      return pubDetails;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getOrphanPublications()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getNotOrphanPublications(PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> pubDetails =
          PublicationDAO.getNotOrphanPublications(con, pubPK);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, pubDetails);
      }
      return pubDetails;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getNotOrphanPublications()",
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
      throw new PublicationRuntimeException(
          "DefaultPublicationService.deleteOrphanPublicationsByCreatorId()",
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
      Collection<PublicationDetail> pubDetails =
          PublicationDAO.getUnavailablePublicationsByPublisherId(con, pubPK, publisherId, nodeId);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, pubDetails);
      }
      return pubDetails;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(
          "DefaultPublicationService.getUnavailablePublicationsByPublisherId()",
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
      throw new PublicationRuntimeException("DefaultPublicationService.getAllFatherPK()",
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
      throw new PublicationRuntimeException("DefaultPublicationService.getAlias()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_FATHERS_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public List<Alias> setAlias(PublicationPK pubPK, List<Alias> alias) {
    List<Alias> oldAliases = (List<Alias>) getAlias(pubPK);
    List<Alias> newAliases = new ArrayList<>(alias.size());
    List<Alias> remAliases = new ArrayList<>(oldAliases.size());
    // Compute the remove list
    for (Alias a : oldAliases) {
      if (!alias.contains(a)) {
        remAliases.add(a);
      }
    }
    // Compute the add and stay list
    for (Alias a : alias) {
      if (!oldAliases.contains(a)) {
        newAliases.add(a);
      }
    }
    addAlias(pubPK, newAliases);
    removeAlias(pubPK, remAliases);

    if (!newAliases.isEmpty() || !remAliases.isEmpty()) {
      // aliases have changed... index it
      indexAliases(pubPK, null);
    }

    return newAliases;
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
      throw new PublicationRuntimeException("DefaultPublicationService.addAlias()",
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
          unindexAlias(pubPK, alias);
        }
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.removeAlias()",
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
      Collection<PublicationDetail> publis =
          PublicationDAO.selectByFatherPK(con, fatherPK, sorting, filterOnVisibilityPeriod);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, publis);
      }
      return publis;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getDetailsByFatherPK()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "fatherPK = " + fatherPK, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting,
      boolean filterOnVisibilityPeriod, String userId) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> publis =
          PublicationDAO.selectByFatherPK(con, fatherPK, sorting, filterOnVisibilityPeriod, userId);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, publis);
      }
      return publis;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getDetailsByFatherPK()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "fatherPK = " + fatherPK, e);
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
      Collection<PublicationDetail> detailList =
          PublicationDAO.selectNotInFatherPK(con, fatherPK, sorting);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, detailList);
      }
      return detailList;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getDetailsNotInFatherPK()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "fatherPK = " + fatherPK, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsByBeginDateDescAndStatus(PublicationPK pk,
      String status, int nbPubs) {
    Connection con = getConnection();
    try {
      List<PublicationDetail> result = new ArrayList<>(nbPubs);
      Collection<PublicationDetail> detailList =
          PublicationDAO.selectByBeginDateDescAndStatus(con, pk, status);
      Iterator<PublicationDetail> it = detailList.iterator();
      int i = 0;
      while (it.hasNext() && i < nbPubs) {
        result.add(it.next());
        i++;
      }
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, result);
      }
      return result;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(
          "DefaultPublicationService.getDetailsByBeginDateDescAndStatus()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "status = " + status, e);
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
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, detailList);
      }
      return detailList;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(
          "DefaultPublicationService.getDetailsByBeginDateDescAndStatusAndNotLinkedToFatherId()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "fatherId = " + fatherId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsByBeginDateDesc(PublicationPK pk, int nbPubs) {
    Connection con = getConnection();
    try {
      List<PublicationDetail> result = new ArrayList<>(nbPubs);
      Collection<PublicationDetail> detailList = PublicationDAO.selectByBeginDateDesc(con, pk);
      Iterator<PublicationDetail> it = detailList.iterator();
      int i = 0;
      while (it.hasNext() && i < nbPubs) {
        result.add(it.next());
        i++;
      }
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, result);
      }
      return result;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getDetailsByBeginDateDesc()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "nbPubs = " + nbPubs, e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Removes links between publications and the specified publication
   * @param pubPK a publication identifier
   * @param links list of links to remove
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
      throw new PublicationRuntimeException("DefaultPublicationService.deleteInfoLinks()",
          SilverpeasRuntimeException.ERROR, "publication.UPDATING_INFO_DETAIL_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public CompletePublication getCompletePublication(PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      PublicationDetail detail = PublicationDAO.loadRow(con, pubPK);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, detail);
      }
      List<ForeignPK> links = SeeAlsoDAO.getLinks(con, pubPK);
      List<ForeignPK> reverseLinks = SeeAlsoDAO.getReverseLinks(con, pubPK);
      CompletePublication cp = new CompletePublication(detail, links, reverseLinks);
      cp.setValidationSteps(getValidationSteps(pubPK));
      return cp;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getCompletePublication()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> searchByKeywords(String query, PublicationPK pubPK) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> resultList = PublicationDAO.searchByKeywords(con, query, pubPK);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, resultList);
      }
      return resultList;
    } catch (Exception e) {
      throw new PublicationRuntimeException("DefaultPublicationService.searchByKeywords()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "query = " + query, e);
    } finally {
      DBUtil.close(con);
    }

  }

  @Override
  public Collection<PublicationDetail> getPublications(Collection<PublicationPK> publicationPKs) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> publications =
          PublicationDAO.selectByPublicationPKs(con, publicationPKs);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, publications);
      }
      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getPublications()",
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
      Collection<PublicationDetail> publications =
          PublicationDAO.selectByStatus(con, pubPK, status);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, publications);
      }
      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getPublicationsByStatus()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "status = " + status, e);
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
      throw new PublicationRuntimeException("DefaultPublicationService.getPublicationPKsByStatus()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "status = " + status + ", componentIds = " + componentIds, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getPublicationsByStatus(String status,
      List<String> componentIds) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> publications =
          PublicationDAO.selectByStatus(con, componentIds, status);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, publications);
      }
      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getPublicationsByStatus()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "status = " + status + ", componentIds = " + componentIds, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public int getNbPubInFatherPKs(Collection<NodePK> fatherPKs) {
    Connection con = getConnection();
    try {
      return PublicationDAO.getNbPubInFatherPKs(con, fatherPKs);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getNbPubInFatherPKs()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_NUMBER_OF_PUBLICATIONS_FAILED",
          "fatherPKs = " + fatherPKs, e);
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
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getDistributionTree()",
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
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getNbPubByFatherPath()",
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
      statusList = new ArrayList<>(1);
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
      PublicationPK pubPK, String sorting, List<String> status, boolean filterOnVisibilityPeriod) {
    Connection con = getConnection();
    try {
      Collection<PublicationDetail> detailList = PublicationDAO
          .selectByFatherIds(con, fatherIds, pubPK, sorting, status, filterOnVisibilityPeriod);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, detailList);
      }
      return detailList;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(
          "DefaultPublicationService.getDetailsByFatherIdsAndStatus()",
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
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getPubPKsInFatherPKs()",
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
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getPubPKsInFatherPK()",
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
   * @return a connection
   */
  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void updateIndexEntryWithWysiwygContent(FullIndexEntry indexEntry,
      PublicationDetail pubDetail) {
    PublicationPK pubPK = pubDetail.getPK();
    try {
      if (pubPK != null) {

        Iterator<String> languages = pubDetail.getLanguages();
        while (languages.hasNext()) {
          WysiwygController.addToIndex(indexEntry, new ForeignPK(pubPK), languages.next());
        }
      }
    } catch (Exception e) {
      SilverTrace.error("publication",
          "DefaultPublicationService.updateIndexEntryWithWysiwygContent", "", e);
    }
  }

  private void updateIndexEntryWithXMLFormContent(FullIndexEntry indexEntry,
      PublicationDetail pubDetail) {
    if (!StringUtil.isInteger(pubDetail.getInfoId())) {
      try {
        PublicationTemplate pub = PublicationTemplateManager.getInstance().getPublicationTemplate(
            pubDetail.getPK().getInstanceId() + ':' + pubDetail.getInfoId());
        RecordSet set = pub.getRecordSet();
        set.indexRecord(pubDetail.getPK().getId(), pubDetail.getInfoId(), indexEntry);
      } catch (FormException | PublicationTemplateException e) {
        SilverTrace.error("publication",
            "DefaultPublicationService.updateIndexEntryWithXMLFormContent", "", e);
      }
    }
  }

  @Override
  public void createIndex(PublicationDetail pubDetail) {

    if (pubDetail.getIndexOperation() == IndexManager.ADD ||
        pubDetail.getIndexOperation() == IndexManager.READD) {

      try {
        FullIndexEntry indexEntry = getFullIndexEntry(pubDetail);
        if (indexEntry != null) {
          IndexEngineProxy.addIndexEntry(indexEntry);
        }
      } catch (Exception e) {
        SilverTrace.error("publication", "DefaultPublicationService.createIndex()",
            "root.MSG_GEN_ENTER_METHOD",
                "pubDetail = " + pubDetail.toString() + ", indexEngineBm.addIndexEntry() failed !",
                e);
      }
    }
  }

  @Override
  public void createIndex(PublicationPK pubPK) {
    createIndex(pubPK, true);
  }

  private FullIndexEntry getFullIndexEntry(PublicationDetail publi, boolean processContent) {
    FullIndexEntry indexEntry = null;
    if (publi != null) {
      // Index the Publication Header
      indexEntry = getFullIndexEntry(publi);
      // Index the Publication Content
      if (processContent) {
        updateIndexEntryWithWysiwygContent(indexEntry, publi);
        updateIndexEntryWithXMLFormContent(indexEntry, publi);
      }
      AttachmentServiceProvider.getAttachmentService().updateIndexEntryWithDocuments(indexEntry);
    }
    return indexEntry;
  }

  private void createIndex(PublicationPK pubPK, boolean processContent, int indexOperation) {

    if (indexOperation == IndexManager.ADD || indexOperation == IndexManager.READD) {

      try {
        PublicationDetail pubDetail = getDetail(pubPK);
        if (pubDetail != null) {
          // Index the Publication Header
          FullIndexEntry indexEntry = getFullIndexEntry(pubDetail, processContent);
          IndexEngineProxy.addIndexEntry(indexEntry);

          // process aliases
          indexAliases(pubPK, indexEntry);
        }
      } catch (Exception e) {
        SilverTrace.error("publication", "DefaultPublicationService.createIndex()",
            "root.MSG_GEN_ENTER_METHOD",
                "pubPK = " + pubPK.toString() + ", indexEngineBm.addIndexEntry() failed !", e);
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
      indexEntry = new FullIndexEntry(
          getIndexEntryPK(pubDetail.getPK().getComponentName(), pubDetail.getPK().getId()));
      indexEntry.setIndexId(true);

      Iterator<String> languages = pubDetail.getLanguages();
      while (languages.hasNext()) {
        String language = languages.next();
        PublicationI18N translation = pubDetail.getTranslation(language);

        indexEntry.setTitle(translation.getName(), language);
        indexEntry.setPreview(translation.getDescription(), language);
        indexEntry.setKeywords(translation.getKeywords() + " " + pubDetail.getAuthor(), language);
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
          UserDetail ud = AdministrationServiceProvider.getAdminService()
              .getUserDetail(pubDetail.getCreatorId());
          if (ud != null) {
            indexEntry.addTextContent(ud.getDisplayedName());
          }
        } catch (AdminException e) {
          // unable to find user detail, ignore and don't index creator's full
          // name
        }
      }

      // set path(s) to publication into the index
      Collection<NodePK> fathers = getAllFatherPK(pubDetail.getPK());
      List<String> paths = new ArrayList<>();
      for (NodePK father : fathers) {
        paths.add(nodeService.getDetail(father).getFullPath());
      }
      indexEntry.setPaths(paths);

      try {
        ThumbnailDetail thumbnail = pubDetail.getThumbnail();
        if (thumbnail != null) {
          String[] imageProps = ThumbnailController.getImageAndMimeType(thumbnail);
          indexEntry.setThumbnail(imageProps[0]);
          indexEntry.setThumbnailMimeType(imageProps[1]);
        }
      } catch (Exception e) {
        throw new PublicationRuntimeException("DefaultPublicationService.getFullIndexEntry()",
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

    IndexEntryKey indexEntry = getIndexEntryPK(pubPK.getComponentName(), pubPK.getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
    unindexAlias(pubPK);
  }

  private IndexEntryKey getIndexEntryPK(String instanceId, String publiId) {
    return new IndexEntryKey(instanceId, "Publication", publiId);
  }

  private void unindexAlias(PublicationPK pk, Alias alias) {
    IndexEngineProxy.removeIndexEntry(getIndexEntryPK(alias.getInstanceId(), pk.getId()));
  }

  private void unindexAlias(PublicationPK pk) {
    // get all apps where alias are
    Collection<Alias> aliases = getAlias(pk);
    Set<String> componentIds = new HashSet<>();
    for (Alias alias : aliases) {
      if (!alias.getInstanceId().equals(pk.getInstanceId())) {
        //it's a true alias
        componentIds.add(alias.getInstanceId());
      }
    }
    // remove publication index in these apps
    for (String componentId : componentIds) {
      IndexEngineProxy.removeIndexEntry(getIndexEntryPK(componentId, pk.getId()));
    }
  }

  private void indexAliases(PublicationPK pubPK, FullIndexEntry indexEntry) {
    if (indexEntry == null) {
      PublicationDetail publi = getDetail(pubPK);
      indexEntry = getFullIndexEntry(publi, true);
    }

    Collection<Alias> aliases = getAlias(pubPK);
    Map<IndexEntryKey, List<String>> pathsByIndex = new HashMap<>();
    for (Alias alias : aliases) {
      if (!alias.getInstanceId().equals(pubPK.getInstanceId())) {
        //it's a true alias
        IndexEntryKey pk = getIndexEntryPK(alias.getInstanceId(), pubPK.getId());
        if (pathsByIndex.get(pk) == null) {
          pathsByIndex.put(pk, new ArrayList<String>());
        }
        pathsByIndex.get(pk).add(
            nodeService.getDetail(new NodePK(alias.getId(), alias.getInstanceId())).getFullPath());
      }
    }

    for (IndexEntryKey indexEntryKey : pathsByIndex.keySet()) {
      FullIndexEntry aliasIndexEntry = indexEntry.clone();
      aliasIndexEntry.setPK(indexEntryKey);
      aliasIndexEntry.setPaths(pathsByIndex.get(indexEntryKey));
      aliasIndexEntry.setAlias(true);
      IndexEngineProxy.addIndexEntry(aliasIndexEntry);
    }
  }

  /**
   * Method declaration
   * @param pubPK a publication identifier
   * @param sorting
   * @return collection of publication details
   */
  @Override
  public Collection<PublicationDetail> getAllPublications(PublicationPK pubPK, String sorting) {
    Connection con = getConnection();
    try {
      return PublicationDAO.selectAllPublications(con, pubPK, sorting);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getAllPublications()",
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
    Connection con = getConnection();
    try {
      PublicationDetail publicationDetail =
          PublicationDAO.selectByPublicationName(con, pubPK, pubName);
      if (publicationDetail != null) {
        return publicationDetail;
      } else {
        throw new PublicationRuntimeException("DefaultPublicationService.getDetailByName()",
            SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_HEADER_FAILED",
            "pubPK = " + pubPK + ", pubName = " + pubName);
      }
    } catch (PublicationRuntimeException | SQLException re) {
      throw new PublicationRuntimeException("DefaultPublicationService.getDetailByName()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_HEADER_FAILED",
          "pubPK = " + pubPK + ", pubName = " + pubName, re);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public PublicationDetail getDetailByNameAndNodeId(PublicationPK pubPK, String pubName,
      int nodeId) {
    Connection con = getConnection();
    try {
      PublicationDetail publicationDetail =
          PublicationDAO.selectByPublicationNameAndNodeId(con, pubPK, pubName, nodeId);
      if (publicationDetail != null) {
        return publicationDetail;
      } else {
        throw new PublicationRuntimeException(
            "DefaultPublicationService.getDetailByNameAndNodeId()",
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

      Collection<PublicationDetail> detailList =
          PublicationDAO.selectBetweenDate(con, beginDate, endDate, instanceId);
      List<PublicationDetail> result = new ArrayList<PublicationDetail>(detailList);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, result);
      }
      return result;
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getDetailBetweenDate()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED", "", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private PublicationDetail loadTranslations(PublicationDetail detail) {
    PublicationI18N translation =
        new PublicationI18N(detail.getLanguage(), detail.getName(), detail.getDescription(),
            detail.getKeywords());
    List<PublicationI18N> translations = new ArrayList<>();
    translations.add(translation);
    if (I18NHelper.isI18nContentActivated) {
      Connection con = getConnection();
      try {
        translations.addAll(PublicationI18NDAO.getTranslations(con, detail.getPK()));
      } catch (SQLException e) {
        throw new PublicationRuntimeException("PublicationEJB.getTranslations()",
            SilverpeasRuntimeException.ERROR, "publication.CANNOT_GET_TRANSLATIONS", e);
      } finally {
        DBUtil.close(con);
      }
    }
    detail.setTranslations(translations);
    return detail;
  }

  private void moveRating(PublicationPK pubPK, String componentInstanceId) {
    if (isRatingEnabled(pubPK)) {
      ratingService.moveRating(
          new ContributionRatingPK(pubPK.getId(), pubPK.getInstanceId(), PublicationDetail.TYPE),
          componentInstanceId);
    }
  }

  private void deleteRating(PublicationPK pubPK) {
    if (isRatingEnabled(pubPK)) {
      ratingService.deleteRating(
          new ContributionRatingPK(pubPK.getId(), pubPK.getInstanceId(), PublicationDetail.TYPE));
    }
  }

  private boolean isRatingEnabled(WAPrimaryKey pk) {
    WAComponent componentDefinition = componentHelper.getWAComponent(pk.getInstanceId());
    return componentDefinition.hasParameterDefined("publicationRating");
  }

  /**
   * Recupere les coordonnees de la publication (collection de nodePK)
   * @param pubId a publication identifier
   * @param componentId a component identifier
   * @return
   */
  @Override
  public Collection<Coordinate> getCoordinates(String pubId, String componentId) {
    SilverTrace
        .info("kmax", "KmeliaBmEjb.getPublicationCoordinates()", "root.MSG_GEN_ENTER_METHOD");
    PublicationPK pubPK = new PublicationPK(pubId, componentId);
    Collection<NodePK> fatherPKs = getAllFatherPK(pubPK);
    Iterator<NodePK> it = fatherPKs.iterator();
    List<String> coordinateIds = new ArrayList<>();
    CoordinatePK coordinatePK = new CoordinatePK("unknown", pubPK);
    while (it.hasNext()) {
      String coordinateId = it.next().getId();
      coordinateIds.add(coordinateId);
    }
    Collection<Coordinate> coordinates =
        coordinatesService.getCoordinatesByCoordinateIds(coordinateIds, coordinatePK);
    // Enrichit les coordonnees avec le nom du noeud
    Iterator<Coordinate> itCoordinates = coordinates.iterator();
    Iterator<CoordinatePoint> pointsIt;
    while (itCoordinates.hasNext()) {
      Coordinate coordinate = itCoordinates.next();
      Collection<CoordinatePoint> points = coordinate.getCoordinatePoints();
      Collection<CoordinatePoint> surePoints = new ArrayList<>();
      pointsIt = points.iterator();
      while (pointsIt.hasNext()) {
        CoordinatePoint point = pointsIt.next();
        try {
          NodeDetail node = nodeService.getHeader(new NodePK("" + point.getNodeId(), componentId));
          point.setName(node.getName());
          point.setLevel(node.getLevel());
          point.setPath(node.getPath());
          surePoints.add(point);
        } catch (Exception e) {
          SilverTrace
              .info("kmelia", "KmeliaBmEJB.getPublicationCoordinates", "root.MSG_GEN_PARAM_VALUE",
                  "node unfindable !");
        }
      }
      coordinate.setCoordinatePoints(surePoints);
    }

    return coordinates;
  }

  /**
   * Updates the publication links
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
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.addLinks()",
          SilverpeasRuntimeException.ERROR, "publication.UPDATING_INFO_DETAIL_FAILED",
          "pubId = " + pubPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Constructor declaration
   */
  protected DefaultPublicationService() {
  }

  /**
   * get my list of SocialInformationPublication according to options and number of Item and the
   * first Index
   * @param userId a user identifier
   * @param begin date
   * @param end date
   * @return List <SocialInformation>
   */
  @Override
  public List<SocialInformation> getAllPublicationsWithStatusbyUserid(String userId, Date begin,
      Date end) {
    Connection con = getConnection();
    try {
      return PublicationDAO.getAllPublicationsIDbyUserid(con, userId, begin, end);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(
          "DefaultPublicationService.getAllPublicationsWithStatusbyUserid",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATION_HEADER_FAILED",
          "userId = " + userId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Get list of SocialInformationPublication of my contacts according to options and number of
   * Item
   * and the first Index.
   * @param myContactsIds
   * @param options
   * @param begin
   * @param end
   * @return
   */
  @Override
  public List<SocialInformationPublication> getSocialInformationsListOfMyContacts(
      List<String> myContactsIds, List<String> options, Date begin, Date end) {
    Connection con = getConnection();
    try {
      return PublicationDAO
          .getSocialInformationsListOfMyContacts(con, myContactsIds, options, begin, end);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(
          "DefaultPublicationService.getSocialInformationsListOfMyContacts",
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
      throw new PublicationRuntimeException("DefaultPublicationService.getPublicationsToDraftOut",
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
      return PublicationDAO
          .selectUpdatedPublicationsSince(con, componentIds, status, since, maxSize);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getPublicationPKsByStatus()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_PUBLICATIONS_FAILED",
          "status = " + status + ", componentIds = " + componentIds, e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public Collection<PublicationDetail> getDraftsByUser(String userId) {
    Connection con = getConnection();
    try {
      return PublicationDAO.getDraftsByUser(con, userId);
    } catch (SQLException e) {
      throw new PublicationRuntimeException("DefaultPublicationService.getDraftsByUser()",
          SilverpeasRuntimeException.ERROR, "publication.GETTING_DRAFTS_FAILED",
          "userId = " + userId, e);
    } finally {
      DBUtil.close(con);
    }
  }

  private boolean indexAuthorName;
  private String thumbnailDirectory;

  @PostConstruct
  protected void init() {
    SettingBundle publicationSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.publication.publicationSettings");
    indexAuthorName = publicationSettings.getBoolean("indexAuthorName", false);
    thumbnailDirectory = publicationSettings.getString("imagesSubDirectory");
  }

}
