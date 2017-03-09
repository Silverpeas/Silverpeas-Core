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

import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.contribution.publication.model.Alias;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.ValidationStep;
import org.silverpeas.core.contribution.publication.social.SocialInformationPublication;
import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Interface declaration
 *
 * @author
 */
public interface PublicationService {

  static PublicationService get() {
    return ServiceProvider.getService(PublicationService.class);
  }

  /**
   * get details on the publication specified by the primary key given in pubPK parameter
   */
  PublicationDetail getDetail(PublicationPK pubPK);

  /**
   * Create a new publication A new line will be added in publication table. The "id" in "detail" is
   * not used (a new one will be computed). The "ed" is used to know the table name.
   */
  PublicationPK createPublication(PublicationDetail detail);

  void movePublication(PublicationPK pubPK, NodePK nodePK, boolean indexIt);

  /**
   * Change order of the given publication identified by pubPK in the given nodePK. direction less
   * than 0 means "up" else it means "down"
   *
   * @param pubPK the publication's id to move
   * @param nodePK the publication's place
   * @param direction must be different to 0
   * @
   */
  void changePublicationOrder(PublicationPK pubPK, NodePK nodePK, int direction);

  /**
   * remove the publication designed by pubPK parameter.
   */
  void removePublication(PublicationPK pubPK);

  /**
   * update the publication content.
   */
  void setDetail(PublicationDetail detaile);

  void setDetail(PublicationDetail detaile, boolean forceUpdateDate);

  /**
   * add a new father (designed by "fatherPK") to a publication ("pubPK") The publication will be
   * visible from its new father node.
   */
  void addFather(PublicationPK pubPK, NodePK fatherPK);

  /**
   * remove a father (designed by "fatherPK") from a publication ("pubPK") The publication won't be
   * visible from its old father node.
   */
  void removeFather(PublicationPK pubPK, NodePK fatherPK);

  /**
   * remove a father (designed by "fatherPK") from a publication ("pubPK") The publication won't be
   * visible from its old father node.
   */
  void removeFather(NodePK fatherPK);

  /**
   * remove a father (designed by "fatherPK") from a publication ("pubPK") The publication won't be
   * visible from its old father node.
   */
  void removeFathers(PublicationPK pubPK, Collection<String> fatherIds);

  /**
   * remove all father from a publication ("pubPK") The publication won't be visible.
   */
  void removeAllFather(PublicationPK pubPK);

  /**
   * return the Detail of publication which are not linked to a father
   */
  Collection<PublicationDetail> getOrphanPublications(PublicationPK pubPK);

  /**
   * return the Detail of publication which are linked to at least one father
   */
  Collection<PublicationDetail> getNotOrphanPublications(PublicationPK pubPK);

  /**
   * Method declaration
   *
   * @param pubPK
   * @param creatorId
   * @
   * @see
   */
  void deleteOrphanPublicationsByCreatorId(PublicationPK pubPK, String creatorId);

  /**
   * return the publications : - which take place in the basket - which are out of the visibility
   * period
   */
  Collection<PublicationDetail> getUnavailablePublicationsByPublisherId(
      PublicationPK pubPK, String publisherId, String nodeId);

  /**
   * return a collection, containing all node primary key from where the publication is visible
   */
  Collection<NodePK> getAllFatherPK(PublicationPK pubPK);

  /**
   * return the publication's collection of Alias
   */
  Collection<Alias> getAlias(PublicationPK pubPK);

  List<Alias> setAlias(PublicationPK pubPK, List<Alias> alias);

  void addAlias(PublicationPK pubPK, List<Alias> alias);

  void removeAlias(PublicationPK pubPK, List<Alias> alias);

  /**
   * return a PublicationDetail collection of all publication visible from the node identified by
   * "fatherPK" parameter
   */
  Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK);

  Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting);

  Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting,
      boolean filterOnVisibilityPeriod);

  Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting,
      boolean filterOnVisibilityPeriod, String userId);

  /**
   * return a PublicationDetail collection of all publications not in the node identified by
   * "fatherPK" parameter
   */
  Collection<PublicationDetail> getDetailsNotInFatherPK(NodePK fatherPK);

  Collection<PublicationDetail> getDetailsNotInFatherPK(NodePK fatherPK, String sorting);

  /**
   * return a PublicationDetail collection of x last publications
   */
  Collection<PublicationDetail> getDetailsByBeginDateDescAndStatus(PublicationPK pk,
      String status, int nbPubs);

  /**
   * return a PublicationDetail collection of x last publications
   */
  Collection<PublicationDetail> getDetailsByBeginDateDesc(PublicationPK pk, int nbPubs);

  /**
   * Method declaration
   *
   * @param pk
   * @param status
   * @param nbPubs
   * @param fatherId
   * @return
   * @
   * @see
   */
  Collection<PublicationDetail> getDetailsByBeginDateDescAndStatusAndNotLinkedToFatherId(
      PublicationPK pk, String status, int nbPubs, String fatherId);

  void deleteInfoLinks(PublicationPK pubPK, List<ForeignPK> links);

  /**
   * @param pubPK
   * @return
   * @
   */
  CompletePublication getCompletePublication(PublicationPK pubPK);

  /**
   * @param publicationPKs
   * @return
   * @
   */
  Collection<PublicationDetail> getPublications(Collection<PublicationPK> publicationPKs);

  /**
   * Method declaration
   *
   * @param status
   * @param pubPK
   * @return
   * @
   * @see
   */
  Collection<PublicationDetail> getPublicationsByStatus(String status, PublicationPK pubPK);

  Collection<PublicationPK> getPublicationPKsByStatus(String status,
      List<String> componentIds);

  /**
   * Return the list of publications with a maxSize as specified, each publication has the specified
   * status and has been updated since the specified date
   *
   * @param status : the publications status.
   * @param since : the last update of the publication
   * @param maxSize : the maximum size of the list. If 0 is specified, the limit is not used.
   * @param componentIds
   * @return a list of publications with the specified maxSize or none if 0 or less is specified.
   * @
   */
  Collection<PublicationPK> getUpdatedPublicationPKsByStatus(String status, Date since,
      int maxSize, List<String> componentIds);

  Collection<PublicationDetail> getPublicationsByStatus(String status,
      List<String> componentIds);

  /**
   * Method declaration
   *
   * @param query
   * @param pubPK
   * @return
   * @
   * @see
   */
  Collection<PublicationDetail> searchByKeywords(String query, PublicationPK pubPK);

  /**
   * Method declaration
   *
   * @param fatherPKs
   * @return
   * @
   * @see
   */
  int getNbPubInFatherPKs(Collection<NodePK> fatherPKs);

  /**
   * Method declaration
   *
   * @param fatherPK
   * @param fatherPath
   * @return
   * @
   * @see
   */
  int getNbPubByFatherPath(NodePK fatherPK, String fatherPath);

  /**
   * * Return the tree of nodes with the number of publication per node.
   *
   * @param instanceId
   * @param statusSubQuery
   * @param checkVisibility
   * @return the tree of nodes with the number of publication per node
   * @
   */
  Map<String, Integer> getDistributionTree(String instanceId, String statusSubQuery,
      boolean checkVisibility);

  /**
   * Method declaration
   *
   * @param fatherIds
   * @param pubPK
   * @return
   * @
   * @see
   */
  Collection<PublicationDetail> getDetailsByFatherIds(List<String> fatherIds,
      PublicationPK pubPK);

  Collection<PublicationDetail> getDetailsByFatherIds(List<String> fatherIds,
      PublicationPK pubPK, boolean filterOnVisibilityPeriod);

  Collection<PublicationDetail> getDetailsByFatherIds(List<String> fatherIds,
      PublicationPK pubPK, String sorting);

  Collection<PublicationDetail> getDetailsByFatherIdsAndStatus(List<String> fatherIds,
      PublicationPK pubPK, String sorting, String status);

  Collection<PublicationDetail> getDetailsByFatherIdsAndStatusList(List<String> fatherIds,
      PublicationPK pubPK, String sorting, List<String> status);

  Collection<PublicationDetail> getDetailsByFatherIdsAndStatusList(List<String> fatherIds,
      PublicationPK pubPK, String sorting, List<String> status, boolean filterOnVisibilityPeriod);

  /**
   * Method declaration
   *
   * @param fatherPKs
   * @return
   * @
   * @see
   */
  Collection<PublicationPK> getPubPKsInFatherPKs(Collection<WAPrimaryKey> fatherPKs);

  /**
   * Method declaration
   *
   * @param fatherPK
   * @return
   * @
   * @see
   */
  Collection<PublicationPK> getPubPKsInFatherPK(NodePK fatherPK);

  /**
   * Method declaration
   *
   * @param pubPK
   * @
   * @see
   */
  void createIndex(PublicationPK pubPK);

  void createIndex(PublicationDetail pubDetail);

  /**
   * Method declaration
   *
   * @param pubPK
   * @
   * @see
   */
  void deleteIndex(PublicationPK pubPK);

  /**
   * Method declaration
   *
   * @param pubPK
   * @return
   * @
   * @see
   */
  Collection<PublicationDetail> getAllPublications(PublicationPK pubPK);

  Collection<PublicationDetail> getAllPublications(PublicationPK pubPK, String sorting);

  /**
   * Looking for, in the instance identified by pubPK, a publication named pubName.
   *
   * @param pubPK the identifier of the instance
   * @param pubName the publication's name we are looking for
   * @return the pubId if a publication already exists in this component's instance. null otherwise.
   * @
   */
  PublicationDetail getDetailByName(PublicationPK pubPK, String pubName);

  PublicationDetail getDetailByNameAndNodeId(PublicationPK pubPK, String pubName, int nodeId);

  /**
   * A wysiwyg's content has been added or modified to a publication. Its content must be added to
   * the indexed content of the publication
   *
   * @param pubPK the identifier of the publication associated to the wysiwyg
   * @
   */
  void processWysiwyg(PublicationPK pubPK);

  /**
   * @param beginDate
   * @param endDate
   * @param instanceId
   * @return Collection of PublicationDetail.
   * @
   */
  Collection<PublicationDetail> getDetailBetweenDate(String beginDate, String endDate,
      String instanceId);

  List<ValidationStep> getValidationSteps(PublicationPK pubPK);

  ValidationStep getValidationStepByUser(PublicationPK pubPK,
      String userId);

  void addValidationStep(ValidationStep step);

  void removeValidationSteps(PublicationPK pubPK);

  void changePublicationsOrder(List<String> ids, NodePK nodePK);

  Collection<Coordinate> getCoordinates(String pubId, String componentId);

  /**
   * Updates the publication links
   *
   * @param pubPK publication identifier which you want to update links
   * @param links list of publication to link with current.
   * @
   */
  void addLinks(PublicationPK pubPK, List<ForeignPK> links);

  List<SocialInformation> getAllPublicationsWithStatusbyUserid(String userId,
      Date begin, Date end);

  /**
   * get list of socialInformation of my contacts according to options and number of Item and the
   * first Index
   *
   * @return: List <SocialInformation>
   * @param myContactsIds
   * @param options
   * @param begin
   * @param end
   * @return
   */
  List<SocialInformationPublication> getSocialInformationsListOfMyContacts(
      List<String> myContactsIds, List<String> options, Date begin, Date end);

  Collection<PublicationDetail> getPublicationsToDraftOut(boolean useClone);

  /**
   * get all publications of given user in state 'Draft'. It returns simple publications in state
   * 'Draft' and cloned publications with a clone in state 'Draft'.
   * @param userId
   * @return all PublicationDetail in state 'Draft' according to given userId
   */
  Collection<PublicationDetail> getDraftsByUser(String userId);

  /**
   * Remove given userId from publication validators where it appears.
   * @param userId id of the user to remove
   * @return a List of PublicationPK on which userId have been removed to.
   */
  List<PublicationDetail> removeUserFromTargetValidators(String userId);
}
