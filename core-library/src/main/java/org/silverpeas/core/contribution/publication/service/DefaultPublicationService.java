/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.publication.service;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.publication.dao.PublicationDAO;
import org.silverpeas.core.contribution.publication.dao.PublicationFatherDAO;
import org.silverpeas.core.contribution.publication.dao.PublicationI18NDAO;
import org.silverpeas.core.contribution.publication.dao.SeeAlsoDAO;
import org.silverpeas.core.contribution.publication.dao.ValidationStepsDAO;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationI18N;
import org.silverpeas.core.contribution.publication.model.PublicationLink;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.PublicationRuntimeException;
import org.silverpeas.core.contribution.publication.model.ValidationStep;
import org.silverpeas.core.contribution.publication.notification.PublicationEventNotifier;
import org.silverpeas.core.contribution.publication.social.SocialInformationPublication;
import org.silverpeas.core.contribution.rating.model.ContributionRatingPK;
import org.silverpeas.core.contribution.rating.service.RatingService;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.io.media.image.thumbnail.control.ThumbnailController;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.coordinates.model.CoordinatePK;
import org.silverpeas.core.node.coordinates.model.CoordinatePoint;
import org.silverpeas.core.node.coordinates.service.CoordinatesService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;

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
  private PublicationEventNotifier notifier;

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
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public PublicationDetail getDetail(PublicationPK pubPK) {
    try (Connection con = getConnection()) {
      PublicationDetail publicationDetail = PublicationDAO.selectByPrimaryKey(con, pubPK);
      if (publicationDetail != null) {
        return loadTranslations(publicationDetail);
      }
      return null;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  private void setTranslations(Connection con, Collection<PublicationDetail> publis) {
    if (publis != null && !publis.isEmpty()) {
      final List<String> publicationIds =
          publis.stream().map(PublicationDetail::getId).collect(Collectors.toList());
      try {
        final Map<String, List<PublicationI18N>> translations = PublicationI18NDAO
            .getIndexedTranslations(con, publicationIds);
        publis.forEach(p -> {
          PublicationI18N translation = new PublicationI18N(p.getLanguage(), p.getName(),
              p.getDescription(), p.getKeywords());
          p.addTranslation(translation);
          p.setTranslations(translations.get(p.getId()));
        });
      } catch (SQLException e) {
        throw new PublicationRuntimeException(e);
      }
    }
  }

  @Override
  @Transactional
  public PublicationPK createPublication(PublicationDetail detail) {
    try (Connection con = getConnection()) {
      int indexOperation = detail.getIndexOperation();
      int id;
      id = DBUtil.getNextId(detail.getPK().getTableName(), "pubId");
      detail.getPK().setId(String.valueOf(id));
      PublicationDAO.insertRow(con, detail);
      if (I18NHelper.isI18nContentActivated) {
        createTranslations(con, detail);
      }
      loadTranslations(detail);
      detail.setIndexOperation(indexOperation);
      createIndex(detail, false);
      return detail.getPK();
    } catch (Exception re) {
      throw new PublicationRuntimeException(re);
    }

  }

  private void createTranslations(Connection con, PublicationDetail publication)
      throws SQLException {
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
  @Transactional
  public void movePublication(PublicationPK pk, NodePK toFatherPK, boolean indexIt) {
    try (Connection con = getConnection()) {
      deleteIndex(pk);
      PublicationDAO.changeInstanceId(con, pk, toFatherPK.getInstanceId());
      moveRating(pk, toFatherPK.getInstanceId());
      pk.setComponentName(toFatherPK.getInstanceId());
      PublicationFatherDAO.removeAllFathers(con, pk);
      PublicationFatherDAO.addFather(con, pk, toFatherPK);
      if (indexIt) {
        createIndex(pk);
      }
    } catch (SQLException re) {
      throw new PublicationRuntimeException(re);
    }
  }

  @Override
  @Transactional
  public void changePublicationsOrder(List<String> ids, NodePK nodePK) {
    if (ids == null || ids.isEmpty()) {
      return;
    }
    try (Connection con = getConnection()) {
      PublicationPK pubPK = new PublicationPK("unknown", nodePK.getInstanceId());
      for (int i = 0; i < ids.size(); i++) {
        String id = ids.get(i);
        pubPK.setId(id);
        PublicationFatherDAO.updateOrder(con, pubPK, nodePK, i);
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }

  }

  @Override
  @Transactional
  public void changePublicationOrder(PublicationPK pubPK, NodePK fatherPK, int direction) {
    // get all publications in given node
    List<PublicationDetail> publications =
        (List<PublicationDetail>) getDetailsByFatherPK(fatherPK, "P.pubUpdateDate desc");
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
    try (Connection con = getConnection()) {
      for (int p = 0; p < publications.size(); p++) {
        PublicationDetail publiToOrder = publications.get(p);
        PublicationFatherDAO.updateOrder(con, publiToOrder.getPK(), fatherPK, p);
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
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
    try (Connection con = getConnection()) {
      PublicationDetail publi = PublicationDAO.loadRow(con, pk);
      // delete links from another publication to removed publication
      SeeAlsoDAO.deleteLinksByObjectId(con, pk);
      SeeAlsoDAO.deleteLinksByTargetId(con, new ResourceReference(pk.getId(), pk.getInstanceId()));
      // delete translations
      PublicationI18NDAO.removeTranslations(con, pk);

      deleteRating(pk);

      deleteIndex(pk);

      notifier.notifyEventOn(ResourceEvent.Type.DELETION, publi);

      // delete publication from database
      PublicationDAO.deleteRow(con, pk);
    } catch (java.sql.SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public void setDetail(PublicationDetail detail) {
    setDetail(detail, false);
  }

  @Override
  @Transactional
  public void setDetail(PublicationDetail detail, boolean forceUpdateDate) {
    try (Connection con = getConnection()) {
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
          set.delete(detail.getPK().getId(), detail.getLanguage());
        }
      }

      if (indexOperation == IndexManager.ADD || indexOperation == IndexManager.READD) {
        createIndex(detail.getPK(), true, indexOperation);
      } else if (indexOperation == IndexManager.REMOVE) {
        deleteIndex(detail.getPK());
      }
    } catch (SQLException | FormException | PublicationTemplateException re) {
      throw new PublicationRuntimeException(re);
    }
  }

  private void updateDetail(PublicationDetail pubDetail, boolean forceUpdateDate) {
    try (Connection con = getConnection()) {
      PublicationDetail publi = PublicationDAO.loadRow(con, pubDetail.getPK());
      PublicationDetail before = publi.copy();
      String oldName = publi.getName();
      String oldDesc = publi.getDescription();
      String oldKeywords = publi.getKeywords();
      String oldLang = publi.getLanguage();
      copyPublicationDetail(pubDetail, publi, forceUpdateDate);

      if (pubDetail.isRemoveTranslation()) {
        // Remove of a translation is required
        if (oldLang.equalsIgnoreCase(pubDetail.getLanguage())) {
          // Default language = translation
          loadTranslation(con, publi);
        } else {
          PublicationI18NDAO.removeTranslation(con, pubDetail.getTranslationId());
          publi.setName(oldName);
          publi.setDescription(oldDesc);
          publi.setKeywords(oldKeywords);
          publi.setLanguage(oldLang);
        }
      } else {
        // Add or update a translation
        if (pubDetail.getLanguage() != null) {
          if (oldLang == null) {
            // translation for the first time
            publi.setLanguage(I18NHelper.defaultLanguage);
          }
          if (oldLang != null && !oldLang.equalsIgnoreCase(pubDetail.getLanguage())) {
            addOrUpdateTranslation(con, pubDetail);
            publi.setName(oldName);
            publi.setDescription(oldDesc);
            publi.setKeywords(oldKeywords);
            publi.setLanguage(oldLang);
          }
        }
      }
      loadTranslations(publi);
      PublicationDAO.storeRow(con, publi);
      notifier.notifyEventOn(ResourceEvent.Type.UPDATE, before, publi);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  private void loadTranslation(final Connection con, final PublicationDetail publi)
      throws SQLException {
    List<PublicationI18N> translations = PublicationI18NDAO.getTranslations(con, publi.getPK());
    if (translations != null && !translations.isEmpty()) {
      PublicationI18N translation = translations.get(0);
      publi.setLanguage(translation.getLanguage());
      publi.setName(translation.getName());
      publi.setDescription(translation.getDescription());
      publi.setKeywords(translation.getKeywords());
      PublicationI18NDAO.removeTranslation(con, translation.getId());
    }
  }

  private void addOrUpdateTranslation(final Connection con, final PublicationDetail pubDetail) {
    PublicationI18N translation = new PublicationI18N(pubDetail);
    String translationId = pubDetail.getTranslationId();
    try {
      if (translationId != null && !translationId.equals("-1")) {
        PublicationI18NDAO.updateTranslation(con, translation);
      } else {
        PublicationI18NDAO.addTranslation(con, translation);
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  private void copyPublicationDetail(final PublicationDetail pubDetail,
      final PublicationDetail publi, final boolean forceUpdateDate) {
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
    if (pubDetail.getContentPagePath() != null) {
      publi.setContentPagePath(pubDetail.getContentPagePath());
    }
    if (pubDetail.getStatus() != null) {
      publi.setStatus(pubDetail.getStatus());
    }
    publi.setUpdaterId(pubDetail.getUpdaterId());
    if (pubDetail.isUpdateDateMustBeSet()) {
      copyUpdateDate(pubDetail, publi, forceUpdateDate);
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
  }

  private void copyUpdateDate(final PublicationDetail pubDetail, final PublicationDetail publi,
      final boolean forceUpdateDate) {
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

  @Override
  public List<ValidationStep> getValidationSteps(PublicationPK pubPK) {
    try (Connection con = getConnection()) {
      return ValidationStepsDAO.getSteps(con, pubPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public ValidationStep getValidationStepByUser(PublicationPK pubPK, String userId) {
    try (Connection con = getConnection()) {
      return ValidationStepsDAO.getStepByUser(con, pubPK, userId);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public void addValidationStep(ValidationStep step) {
    try (Connection con = getConnection()) {
      ValidationStepsDAO.addStep(con, step);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public void removeValidationSteps(PublicationPK pubPK) {
    try (Connection con = getConnection()) {
      ValidationStepsDAO.removeSteps(con, pubPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public void addFather(PublicationPK pubPK, NodePK fatherPK) {
    try (Connection con = getConnection()) {
      PublicationFatherDAO.addFather(con, pubPK, fatherPK);
    } catch (SQLException re) {
      throw new PublicationRuntimeException(re);
    }
  }

  @Override
  @Transactional
  public void removeFather(PublicationPK pubPK, NodePK fatherPK) {
    try (Connection con = getConnection()) {
      PublicationFatherDAO.removeFather(con, pubPK, fatherPK);
    } catch (SQLException re) {
      throw new PublicationRuntimeException(re);
    }
  }

  @Override
  @Transactional
  public void removeFathers(PublicationPK pubPK, Collection<String> fatherIds) {
    try (Connection con = getConnection()) {
      PublicationFatherDAO.removeFathersToPublications(con, pubPK, fatherIds);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public void removeAllFathers(PublicationPK pubPK) {
    try (Connection con = getConnection()) {
      deleteIndex(pubPK);
      PublicationFatherDAO.removeAllFathers(con, pubPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<PublicationDetail> getOrphanPublications(final String componentId) {
    try (Connection con = getConnection()) {
      Collection<PublicationDetail> pubDetails =
          PublicationDAO.getOrphanPublications(con, componentId);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, pubDetails);
      }
      return pubDetails;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<NodePK> getAllFatherPK(final PublicationPK pubPK) {
    return getAllFatherPKInSamePublicationComponentInstance(pubPK);
  }

  @Override
  public List<NodePK> getAllFatherPKInSamePublicationComponentInstance(PublicationPK pubPK) {
    try (Connection con = getConnection()) {
      return PublicationFatherDAO.getAllFatherPKInSamePublicationComponentInstance(con, pubPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<Location> getAllLocations(PublicationPK pubPK) {
    try (Connection con = getConnection()) {
      return PublicationFatherDAO.getLocations(con, pubPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public List<Location> getLocationsInComponentInstance(final PublicationPK pubPK,
      final String instanceId) {
    try (Connection con = getConnection()) {
      return PublicationFatherDAO.getLocations(con, pubPK, instanceId);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Optional<Location> getMainLocation(final PublicationPK pubPK) {
    try (Connection con = getConnection()) {
      return Optional.ofNullable(PublicationFatherDAO.getMainLocation(con, pubPK));
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public List<Location> getAllAliases(PublicationPK pubPK) {
    try (Connection con = getConnection()) {
      return PublicationFatherDAO.getAliases(con, pubPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public Pair<Collection<Location>, Collection<Location>> setAliases(PublicationPK pubPK,
      List<Location> aliases) {
    Collection<Location> previousAliases = getAllAliases(pubPK);
    Collection<Location> removedAliases = previousAliases.stream().filter(l -> !aliases.contains(l))
        .collect(Collectors.toList());
    Collection<Location> newAliases =
        aliases.stream().filter(l -> !previousAliases.contains(l)).collect(Collectors.toList());

    try (final Connection connection = getConnection()) {
      addAlias(connection, pubPK, newAliases);
      removeAndUnindexAlias(connection, pubPK, removedAliases);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }

    if (!newAliases.isEmpty() || !removedAliases.isEmpty()) {
      // aliases have changed... index it
      indexAliases(pubPK, null);
    }

    return Pair.of(newAliases, removedAliases);
  }

  private void addAlias(final Connection connection, final PublicationPK pubPK,
      final Collection<Location> aliases) throws SQLException {
    for (Location location : aliases) {
      PublicationFatherDAO.addAlias(connection, pubPK, location);
      PublicationDAO.invalidateLastPublis(location.getInstanceId());
    }
  }

  @Override
  @Transactional
  public void addAliases(PublicationPK pubPK, List<Location> aliases) {
    if (aliases != null && !aliases.isEmpty()) {
      try (Connection con = getConnection()) {
        addAlias(con, pubPK, aliases);
      } catch (SQLException e) {
        throw new PublicationRuntimeException(e);
      }
      indexAliases(pubPK, null);
    }
  }

  private void removeAndUnindexAlias(final Connection connection, final PublicationPK pubPK,
      final Collection<Location> aliases) throws SQLException {
    for (Location location : aliases) {
      PublicationFatherDAO.removeAlias(connection, pubPK, location);
      PublicationDAO.invalidateLastPublis(location.getInstanceId());
      unindexAlias(pubPK, location);
    }
  }

  @Override
  @Transactional
  public void removeAliases(PublicationPK pubPK, Collection<Location> aliases) {
    try (final Connection con = getConnection()) {
      if (aliases != null && !aliases.isEmpty()) {
        removeAndUnindexAlias(con, pubPK, aliases);
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
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
    try (Connection con = getConnection()) {
      Collection<PublicationDetail> publis =
          PublicationDAO.selectByFatherPK(con, fatherPK, sorting, filterOnVisibilityPeriod);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, publis);
      }
      return publis;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting,
      boolean filterOnVisibilityPeriod, String userId) {

    try (Connection con = getConnection()) {
      Collection<PublicationDetail> publis =
          PublicationDAO.selectByFatherPK(con, fatherPK, sorting, filterOnVisibilityPeriod, userId);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, publis);
      }
      return publis;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsNotInFatherPK(NodePK fatherPK) {
    return getDetailsNotInFatherPK(fatherPK, null);
  }

  @Override
  public Collection<PublicationDetail> getDetailsNotInFatherPK(NodePK fatherPK, String sorting) {
    try (Connection con = getConnection()) {
      Collection<PublicationDetail> detailList =
          PublicationDAO.selectNotInFatherPK(con, fatherPK, sorting);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, detailList);
      }
      return detailList;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsByBeginDateDescAndStatusAndNotLinkedToFatherId(
      NodePK fatherPK, String status, int nbPubs) {
    try (Connection con = getConnection()) {
      Collection<PublicationDetail> detailList = PublicationDAO.
          selectByBeginDateDescAndStatusAndNotLinkedToFatherId(con, fatherPK, status, nbPubs);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, detailList);
      }
      return detailList;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  @Transactional
  public void deleteLink(String id) {
    try {
      SeeAlsoDAO.deleteLink(id);
    } catch (Exception e) {
      throw new org.silverpeas.core.SilverpeasRuntimeException("Can't delete seeAlso "+id, e);
    }
  }

  @Override
  public CompletePublication getCompletePublication(PublicationPK pubPK) {
    try (Connection con = getConnection()) {
      PublicationDetail detail = PublicationDAO.loadRow(con, pubPK);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, singletonList(detail));
      }
      List<PublicationLink> links = SeeAlsoDAO.getLinks(con, pubPK);
      List<PublicationLink> reverseLinks = SeeAlsoDAO.getReverseLinks(con, pubPK);
      CompletePublication cp = new CompletePublication(detail, links, reverseLinks);
      cp.setValidationSteps(getValidationSteps(pubPK));
      return cp;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public List<PublicationDetail> getPublications(Collection<PublicationPK> publicationPKs) {
    return getByIds(publicationPKs.stream().map(PublicationPK::getId).collect(Collectors.toList()));
  }

  @Override
  public List<PublicationDetail> getByIds(final Collection<String> publicationIds) {
    try (Connection con = getConnection()) {
      final List<PublicationDetail> publications = PublicationDAO.getByIds(con, publicationIds);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, publications);
      }
      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<PublicationDetail> getPublicationsByStatus(String status, String instanceId) {
    try (Connection con = getConnection()) {
      Collection<PublicationDetail> publications =
          PublicationDAO.selectByStatus(con, instanceId, status);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, publications);
      }
      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public SilverpeasList<PublicationPK> getPublicationPKsByStatus(final String status,
      final List<String> componentIds, final PaginationPage pagination) {
    try (final Connection con = getConnection()) {
      return PublicationDAO.selectPKsByStatus(con, componentIds, status,
          pagination != null && pagination.getPageSize() > 0 ? pagination.asCriterion() : null);
    } catch (Exception e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<PublicationDetail> getPublicationsByStatus(String status,
      List<String> componentIds) {
    try (Connection con = getConnection()) {
      Collection<PublicationDetail> publications =
          PublicationDAO.selectByStatus(con, componentIds, status);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, publications);
      }
      return publications;
    } catch (Exception e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Map<String, Integer> getDistributionTree(String instanceId, String statusSubQuery,
      boolean checkVisibility) {
    try (Connection con = getConnection()) {
      return PublicationDAO.getDistributionTree(con, instanceId, statusSubQuery, checkVisibility);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public int getNbPubByFatherPath(NodePK fatherPK, String fatherPath) {
    try (Connection con = getConnection()) {
      return PublicationDAO.getNbPubByFatherPath(con, fatherPK, fatherPath);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherIds(List<String> fatherIds,
      String instanceId, boolean filterOnVisibilityPeriod) {
    return getDetailsByFatherIdsAndStatusList(fatherIds, instanceId, null, null,
        filterOnVisibilityPeriod);
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherIdsAndStatus(List<String> fatherIds,
      String instanceId, String sorting, String status) {
    ArrayList<String> statusList = null;
    if (status != null) {
      statusList = new ArrayList<>(1);
      statusList.add(status);
    }
    return getDetailsByFatherIdsAndStatusList(fatherIds, instanceId, sorting, statusList);
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherIdsAndStatusList(List<String> fatherIds,
      String instanceId, String sorting, List<String> status) {
    return getDetailsByFatherIdsAndStatusList(fatherIds, instanceId, sorting, status, true);
  }

  @Override
  public Collection<PublicationDetail> getDetailsByFatherIdsAndStatusList(List<String> fatherIds,
      String instanceId, String sorting, List<String> status, boolean filterOnVisibilityPeriod) {
    try (Connection con = getConnection()) {
      Collection<PublicationDetail> detailList = PublicationDAO
          .selectByFatherIds(con, fatherIds, instanceId, sorting, status, filterOnVisibilityPeriod);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, detailList);
      }
      return detailList;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<PublicationPK> getPubPKsInFatherPK(NodePK fatherPK) {
    try (Connection con = getConnection()) {
      return PublicationFatherDAO.getPubPKsInFatherPK(con, fatherPK);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  /**
   * Method declaration
   * @return a connection
   */
  private Connection getConnection() {
    try {
      return DBUtil.openConnection();
    } catch (Exception e) {
      throw new PublicationRuntimeException(e);
    }
  }

  private void updateIndexEntryWithWysiwygContent(FullIndexEntry indexEntry,
      PublicationDetail pubDetail) {
    PublicationPK pubPK = pubDetail.getPK();
    try {
      if (pubPK != null) {

        Collection<String> languages = pubDetail.getLanguages();
        languages.forEach(l ->
          WysiwygController.addToIndex(indexEntry, new ResourceReference(pubPK), l));
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
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
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  @Override
  public void createIndex(PublicationDetail pubDetail) {
    createIndex(pubDetail, true);
  }

  private void createIndex(PublicationDetail pubDetail, boolean processContent) {
    if (pubDetail.getIndexOperation() == IndexManager.ADD ||
        pubDetail.getIndexOperation() == IndexManager.READD) {
      try {
        FullIndexEntry indexEntry = getFullIndexEntry(pubDetail, processContent);
        IndexEngineProxy.addIndexEntry(indexEntry);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
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
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  private void createIndex(PublicationPK pubPK, boolean processWysiwygContent) {
    createIndex(pubPK, processWysiwygContent, IndexManager.ADD);
  }

  private FullIndexEntry getFullIndexEntry(PublicationDetail pubDetail) {
    final FullIndexEntry indexEntry;
    if (pubDetail != null) {
      // Index the Publication Header
      indexEntry = new FullIndexEntry(
          getIndexEntryPK(pubDetail.getPK().getComponentName(), pubDetail.getPK().getId()));
      indexEntry.setIndexId(true);

      fillIndexEntryWithTranslations(indexEntry, pubDetail);
      setIndexEntryFromPubDetail(indexEntry, pubDetail);
      // index creator's full name
      if (indexAuthorName) {
        setIndexEntryWithAuthorName(indexEntry, pubDetail);
      }
      setIndexEntryWithPubPath(indexEntry, pubDetail);

      setIndexEntryWithThumbnail(indexEntry, pubDetail);
    } else {
      indexEntry = null;
    }

    return indexEntry;
  }

  private void setIndexEntryWithThumbnail(final FullIndexEntry indexEntry,
      final PublicationDetail pubDetail) {
    try {
      ThumbnailDetail thumbnail = pubDetail.getThumbnail();
      if (thumbnail != null) {
        String[] imageProps = ThumbnailController.getImageAndMimeType(thumbnail);
        indexEntry.setThumbnail(imageProps[0]);
        indexEntry.setThumbnailMimeType(imageProps[1]);
      }
    } catch (Exception e) {
      throw new PublicationRuntimeException(e);
    }
    indexEntry.setThumbnailDirectory(thumbnailDirectory);
  }

  private void setIndexEntryWithPubPath(final FullIndexEntry indexEntry,
      final PublicationDetail pubDetail) {
    // set path(s) to publication into the index
    if (!pubDetail.getPK().getInstanceId().startsWith("kmax")) {
      final List<String> mainLocation = new ArrayList<>();
      getMainLocation(pubDetail.getPK())
          .map(l -> nodeService.getDetail(l).getFullPath())
          .ifPresent(mainLocation::add);
      indexEntry.setPaths(mainLocation.isEmpty() ? null : mainLocation);
    }
  }

  private void setIndexEntryWithAuthorName(final FullIndexEntry indexEntry,
      final PublicationDetail pubDetail) {
    try {
      UserDetail ud =
          AdministrationServiceProvider.getAdminService().getUserDetail(pubDetail.getCreatorId());
      if (ud != null) {
        indexEntry.addTextContent(ud.getDisplayedName());
      }
    } catch (AdminException e) {
      // unable to find user detail, ignore and don't index creator's full
      // name
    }
  }

  private void setIndexEntryFromPubDetail(final FullIndexEntry indexEntry,
      final PublicationDetail pubDetail) {
    indexEntry.setLang("fr");
    indexEntry.setCreationDate(pubDetail.getCreationDate());
    indexEntry.setLastModificationDate(pubDetail.getUpdateDate());
    if (pubDetail.getBeginDate() != null) {
      indexEntry.setStartDate(pubDetail.getBeginDate());
    }
    if (pubDetail.getEndDate() != null) {
      indexEntry.setEndDate(pubDetail.getEndDate());
    }
    indexEntry.setCreationUser(pubDetail.getCreatorId());
    indexEntry.setLastModificationUser(pubDetail.getUpdaterId());
  }

  private void fillIndexEntryWithTranslations(final FullIndexEntry indexEntry,
      final PublicationDetail pubDetail) {
    Collection<String> languages = pubDetail.getLanguages();
    for (final String language : languages) {
      PublicationI18N translation = pubDetail.getTranslation(language);

      indexEntry.setTitle(translation.getName(), language);
      indexEntry.setPreview(translation.getDescription(), language);
      indexEntry.setKeywords(translation.getKeywords() + " " + pubDetail.getAuthor(), language);
    }
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

  private void unindexAlias(PublicationPK pk, Location location) {
    IndexEngineProxy.removeIndexEntry(getIndexEntryPK(location.getInstanceId(), pk.getId()));
  }

  private void unindexAlias(PublicationPK pk) {
    // get all apps where alias are and remove publication index in these apps
    Collection<Location> aliases = getAllAliases(pk);
    for (Location alias : aliases) {
      IndexEngineProxy.removeIndexEntry(getIndexEntryPK(alias.getInstanceId(), pk.getId()));
    }
  }

  /**
   * Indexes the alias of publication represented by the given {@link PublicationPK}.
   * <p>
   * If given {@link FullIndexEntry} is null, the index of aimed publication is fully processed and
   * indexes of aliases on other component instances are also processed.
   * </p>
   * <p>
   * If given {@link FullIndexEntry} is already initialized (which MUST concern the main one of
   * the publication), only indexes of aliases on other component instances are processed.
   * </p>
   * <p>
   * Into context of indexation, an index concerning the component instance host MUST NEVER be
   * checked as an alias one.
   * </p>
   * @param pubPK the identifier of the publication.
   * @param addedMainIndexEntry the optionally initialized and registered index of publication.
   */
  private void indexAliases(PublicationPK pubPK, FullIndexEntry addedMainIndexEntry) {
    Objects.requireNonNull(pubPK);
    final FullIndexEntry index;
    final boolean indexMainAndAliases = addedMainIndexEntry == null;
    if (indexMainAndAliases) {
      final PublicationDetail publi = getDetail(pubPK);
      index = getFullIndexEntry(publi, true);
    } else {
      index = addedMainIndexEntry;
    }

    Objects.requireNonNull(index);
    final Map<IndexEntryKey, List<String>> pathsByIndex = new HashMap<>();
    if (indexMainAndAliases) {
      // case where index of main location has not been yet updated, it MUST be
      pathsByIndex.put(index.getPK(), index.getPaths() != null
          ? new ArrayList<>(index.getPaths())
          : new ArrayList<>());
    }
    getAllAliases(pubPK).forEach(l -> {
      final IndexEntryKey pk = getIndexEntryPK(l.getInstanceId(), pubPK.getId());
      final List<String> paths = pathsByIndex.computeIfAbsent(pk, k ->
          // case where index of main location has been already indexed, on same index key
          // main path MUST be kept
          k.equals(index.getPK()) && index.getPaths() != null
              ? new ArrayList<>(index.getPaths())
              : new ArrayList<>());
      try {
        final NodeDetail node = nodeService.getDetail(new NodePK(l.getId(), l.getInstanceId()));
        paths.add(node.getFullPath());
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .warn("Alias target {0} in component {1} no more exists", l.getId(), l.getInstanceId());
      }
    });

    for (Map.Entry<IndexEntryKey, List<String>> entry : pathsByIndex.entrySet()) {
      final FullIndexEntry aliasIndexEntry = index.clone();
      final IndexEntryKey indexEntryKey = entry.getKey();
      aliasIndexEntry.setPK(indexEntryKey);
      aliasIndexEntry.setPaths(entry.getValue());
      aliasIndexEntry.setAlias(!indexEntryKey.getComponent().equals(pubPK.getInstanceId()));
      IndexEngineProxy.addIndexEntry(aliasIndexEntry);
    }
  }

  @Override
  public Collection<PublicationDetail> getAllPublications(String instanceId, String sorting) {
    try (Connection con = getConnection()) {
      return PublicationDAO.selectAllPublications(con, instanceId, sorting);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<PublicationDetail> getAllPublications(String instanceId) {
    return getAllPublications(instanceId, null);
  }

  @Override
  public PublicationDetail getDetailByName(PublicationPK pubPK, String pubName) {
    try (Connection con = getConnection()) {
      PublicationDetail publicationDetail =
          PublicationDAO.selectByPublicationName(con, pubPK, pubName);
      if (publicationDetail != null) {
        return publicationDetail;
      } else {
        throw new PublicationRuntimeException(failureOnGetting("publication", pubPK.getId()));
      }
    } catch (PublicationRuntimeException | SQLException re) {
      throw new PublicationRuntimeException(re);
    }
  }

  @Override
  public PublicationDetail getDetailByNameAndNodeId(PublicationPK pubPK, String pubName,
      int nodeId) {
    try (Connection con = getConnection()) {
      PublicationDetail publicationDetail =
          PublicationDAO.selectByPublicationNameAndNodeId(con, pubPK, pubName, nodeId);
      if (publicationDetail != null) {
        return publicationDetail;
      } else {
        throw new PublicationRuntimeException(
            failureOnGetting("publication", pubPK.getId()) + " on node " + nodeId);
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<PublicationDetail> getDetailBetweenDate(String beginDate, String endDate,
      String instanceId) {
    try (Connection con = getConnection()) {

      Collection<PublicationDetail> detailList =
          PublicationDAO.selectBetweenDate(con, beginDate, endDate, instanceId);
      List<PublicationDetail> result = new ArrayList<>(detailList);
      if (I18NHelper.isI18nContentActivated) {
        setTranslations(con, result);
      }
      return result;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  private PublicationDetail loadTranslations(PublicationDetail detail) {
    PublicationI18N translation =
        new PublicationI18N(detail.getLanguage(), detail.getName(), detail.getDescription(),
            detail.getKeywords());
    List<PublicationI18N> translations = new ArrayList<>();
    translations.add(translation);
    if (I18NHelper.isI18nContentActivated) {
      try (Connection con = getConnection()) {
        translations.addAll(PublicationI18NDAO.getTranslations(con, detail.getPK()));
      } catch (SQLException e) {
        throw new PublicationRuntimeException(e);
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

  private boolean isRatingEnabled(PublicationPK pk) {
    WAComponent componentDefinition = WAComponent.getByInstanceId(pk.getInstanceId())
        .orElseThrow(() -> new org.silverpeas.core.SilverpeasRuntimeException(
            "The component instance '" + pk.getInstanceId() + " doesn't exit!"));
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
    PublicationPK pubPK = new PublicationPK(pubId, componentId);
    Collection<NodePK> fatherPKs = getAllFatherPKInSamePublicationComponentInstance(pubPK);
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
          SilverLogger.getLogger(this).error(e);
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
  public void addLinks(PublicationPK pubPK, List<ResourceReference> links) {
    try (Connection con = getConnection()) {
      if (links != null) {
        // deletes existing links
        SeeAlsoDAO.deleteLinksByObjectId(con, pubPK);
        for (ResourceReference link : links) {
          SeeAlsoDAO.addLink(con, pubPK, link);
        }
      }
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
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
    try (Connection con = getConnection()) {
      return PublicationDAO.getAllPublicationsIDbyUserid(con, userId, begin, end);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
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
    try (Connection con = getConnection()) {
      return PublicationDAO
          .getSocialInformationsListOfMyContacts(con, myContactsIds, options, begin, end);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<PublicationDetail> getPublicationsToDraftOut(boolean useClone) {
    try (Connection con = getConnection()) {
      return PublicationDAO.getPublicationsToDraftOut(con, useClone);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public SilverpeasList<PublicationPK> getUpdatedPublicationPKsByStatus(String status, Date since,
      List<String> componentIds, PaginationPage pagination) {
    try (final Connection con = getConnection()) {
      return PublicationDAO.selectPKsByStatusAndUpdatedSince(con, componentIds, status, since,
          pagination != null && pagination.getPageSize() > 0 ? pagination.asCriterion() : null);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public Collection<PublicationDetail> getDraftsByUser(String userId) {
    try (Connection con = getConnection()) {
      return PublicationDAO.getDraftsByUser(con, userId);
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
    }
  }

  @Override
  public List<PublicationDetail> removeUserFromTargetValidators(String userId) {
    try (Connection con = getConnection()) {
      List<PublicationDetail> publications = PublicationDAO.getByTargetValidatorId(con, userId);
      for (PublicationDetail publication : publications) {
        // remove given user to users list
        String[] userIds = StringUtil.split(publication.getTargetValidatorId(), ',');
        String[] newUserIds = ArrayUtil.removeElement(userIds, userId);
        if (newUserIds != null && !ArrayUtil.isEmpty(newUserIds)) {
          publication.setTargetValidatorId(StringUtil.join(newUserIds, ','));
        } else {
          publication.setTargetValidatorId(null);
        }

        // store updated data (without given user)
        PublicationDAO.updateTargetValidatorIds(con, publication);
      }

      return publications;
    } catch (SQLException e) {
      throw new PublicationRuntimeException(e);
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
