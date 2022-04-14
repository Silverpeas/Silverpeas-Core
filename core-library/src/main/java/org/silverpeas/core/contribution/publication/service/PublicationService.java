/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.publication.service;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.publication.dao.DistributionTreeCriteria;
import org.silverpeas.core.contribution.publication.dao.PublicationCriteria;
import org.silverpeas.core.contribution.publication.model.CompletePublication;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.ValidationStep;
import org.silverpeas.core.contribution.publication.social.SocialInformationPublication;
import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SilverpeasList;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service to manage the publications in Silverpeas.
 */
public interface PublicationService {

  /**
   * Gets an instance of the {@link PublicationService} interface.
   * @return a {@link PublicationService} object.
   */
  static PublicationService get() {
    return ServiceProvider.getSingleton(PublicationService.class);
  }

  /**
   * Gets the publication with the specified identifying key.
   * @param pubPK the identifying key of the publication to get.
   * @return the publication corresponding to the given key. If no such publication exists with
   * the specified key, then null is returned.
   */
  PublicationDetail getDetail(PublicationPK pubPK);

  /**
   * Creates in Silverpeas the specified publication. It persists it into the Silverpeas data
   * source and a new identifier is set for this publication. A {@link ResourceEvent.Type#CREATION}
   * event is sent once the publication is created.
   * @param detail the detail on the publication to persist.
   * @return the identifying key of the publication so that it can be retrieved later in the
   * data source by this key.
   */
  PublicationPK createPublication(PublicationDetail detail);

  /**
   * Moves the specified publication to the specified father and indexes or not it. The father can
   * be either a node in the same component instance or a node in another component instance. No
   * {@link ResourceEvent.Type#MOVE} event is sent; for doing please use one the move done the
   * {@link PublicationService#setDetail(PublicationDetail, boolean, ResourceEvent.Type)} method
   * by specifying the {@link ResourceEvent.Type#MOVE} event as last parameter.
   * @param pubPK the identifying key of the publication to move.
   * @param toFatherPK the new father of the publication.
   * @param indexIt a boolean indicating if the publication must be indexed.
   */
  void movePublication(PublicationPK pubPK, NodePK toFatherPK, boolean indexIt);

  /**
   * Change the order of the given publication among the others one for the specified father.
   * Direction less than 0 means "up", otherwise it means "down".
   *
   * @param pubPK the identifying key of the publication to reorder.
   * @param fatherPK a father of the publication.
   * @param direction a direction of the reordering: a value less than 0 means "up", otherwise it
   * means "down". Cannot be 0.
   */
  void changePublicationOrder(PublicationPK pubPK, NodePK fatherPK, int direction);

  /**
   * Removes the specified publication. The {@link ResourceEvent.Type#DELETION} event is sent once
   * the publication is removed.
   * @param pubPK the identifying key of the publication to remove.
   */
  void removePublication(PublicationPK pubPK);

  /**
   * Updates the specified publication. The update date isn't updated in given the publication
   * detail.
   * The {@link ResourceEvent.Type#UPDATE} event is sent once the publication is updated.
   * @param detail the detail of the publication from which it has to be updated.
   */
  void setDetail(PublicationDetail detail);

  /**
   * Updates the specified publication and specify if the update date property of the publication
   * detail has to be used as update date.
   * The {@link ResourceEvent.Type#UPDATE} event is sent once the publication is updated.
   * @param detail the detail of the publication from which it has to be updated.
   * @param forceUpdateDate a boolean indicating if the update date has to be set with date of this
   * update.
   */
  void setDetail(PublicationDetail detail, boolean forceUpdateDate);

  /**
   * Updates the specified publication and specify if the update date property of the publication
   * detail has to be used as update date. The kind of update is specified by the given event type:
   * it is either a simple publication update or an update issuing from a publication move
   * (some publication properties can require to be updated after a move but they aren't related to
   * a publication modification).
   * @param detail the detail of the publication from which it has to be updated.
   * @param forceUpdateDate a boolean indicating if the update date has to be set with date of this
   * update.
   * @param eventType permit to precise to listener of publication modification the type of the
   * update.
   */
  void setDetail(PublicationDetail detail, boolean forceUpdateDate, ResourceEvent.Type eventType);

  /**
   * Adds the specified father to the given publication. The publication will be then visible from
   * this new father.
   * @param pubPK the identifying key of the publication.
   * @param fatherPK the the identifying key of the new father.
   */
  void addFather(PublicationPK pubPK, NodePK fatherPK);

  /**
   * Removes the specified publication from the specified father. The publication won't be any more
   * attached to the given father and hence it won't be visible any more from this father.
   * @param pubPK the identifying key of the publication
   * @param fatherPK the identifying key of the father to detach.
   */
  void removeFather(PublicationPK pubPK, NodePK fatherPK);

  /**
   * Removes the specified publication from all the specified father. The publication won't be any
   * more attached to the given fathers and hence it won't be visible any more from these fathers.
   * @param pubPK the identifying key of the publication
   * @param fatherIds a collection of identifying key of the fathers to detach.
   */
  void removeFathers(PublicationPK pubPK, Collection<String> fatherIds);

  /**
   * Removes the specified publication from all fathers. This means the publication will become
   * an orphan and it won't be visible from any fathers.
   * @param pubPK the identifying key of the publication
   */
  void removeAllFathers(PublicationPK pubPK);

  /**
   * Gets all the publications that aren't attached to any father in the specified component
   * instance.
   * @param componentId the unique identifier of a component instance/
   * @return a collection of orphan publications.
   */
  Collection<PublicationDetail> getOrphanPublications(String componentId);

  /**
   * Gets the unique identifying key of all of the fathers of the specified publication and in the
   * same component instance.
   * @param pubPK the identifying key of the publication.
   * @return a collection of {@link NodePK} instances, each of them identifying a node.
   * @deprecated use instead the renamed
   * {@link #getAllFatherPKInSamePublicationComponentInstance(PublicationPK)}. This signature has
   * been kept in case of external uses.
   */
  @Deprecated
  Collection<NodePK> getAllFatherPK(PublicationPK pubPK);

  /**
   * Gets the unique identifying key of all of the fathers of the specified publication and in the
   * same component instance.
   * @param pubPK the identifying key of the publication.
   * @return a collection of {@link NodePK} instances, each of them identifying a node.
   */
  List<NodePK> getAllFatherPKInSamePublicationComponentInstance(PublicationPK pubPK);

  /**
   * Selects massively simple data about all locations (main or aliases).
   * <p>
   *   This method is designed for process performance needs.
   * </p>
   * @param ids the instance ids aimed.
   * @return a list of {@link Location} instances.
   */
  Map<String, List<Location>> getAllLocationsByPublicationIds(Collection<String> ids);

  /**
   * Gets all the locations of the specified publication whatever the component instance in which
   * they are. By default, the original location of the publication is returned along with all of
   * its aliases.
   * @param pubPK the identifying key of the publication.
   * @return a collection of the locations of the publication.
   * @see org.silverpeas.core.contribution.publication.model.Location
   */
  List<Location> getAllLocations(PublicationPK pubPK);

  /**
   * Gets the locations of the specified publication in the given component instance.
   * @param pubPK the identifying key of the publication.
   * @param instanceId the unique identifier of a component instance.
   * @return a collection of {@link Location} objects or none if the publication has no locations
   * in the given component instance.
   * @see org.silverpeas.core.contribution.publication.model.Location
   */
  List<Location> getLocationsInComponentInstance(PublicationPK pubPK, String instanceId);

  /**
   * Gets the main location of the specified publication. A publication has always one original
   * location and any other locations should be an alias.
   * @param pubPK the identifying key of the publication.
   * @return the main location of the specified publication or nothing is the publication is
   * orphaned (not attached to a father).
   * @see org.silverpeas.core.contribution.publication.model.Location
   */
  Optional<Location> getMainLocation(PublicationPK pubPK);

  /**
   * Gets all the aliases of the specified publication. The original location isn't returned among
   * the aliases; to get also the original location, please look at the
   * {@link PublicationService#getAllLocations(PublicationPK)} method.
   * @param pubPK the identifying key of the publication.
   * @see org.silverpeas.core.contribution.publication.model.Location
   */
  List<Location> getAllAliases(PublicationPK pubPK);

  /**
   * Sets the aliases of the specified publication. They replace the existing aliases of the
   * publication. The {@link IllegalArgumentException} is throw if one of the location isn't an
   * alias.
   * @param pubPK the identifying key of the publication.
   * @param aliases the new aliases.
   * @return a pair made up of firstly the added aliases and of secondly the removed aliases.
   * @see org.silverpeas.core.contribution.publication.model.Location
   */
  Pair<Collection<Location>, Collection<Location>> setAliases(PublicationPK pubPK,
      List<Location> aliases);

  /**
   * Adds the specified aliases of the specified publication. The {@link IllegalArgumentException}
   * is throw if one of the location isn't an  alias.
   * @param pubPK the identifying key of the publication.
   * @param aliases the aliases to add to the existing ones.
   * @see org.silverpeas.core.contribution.publication.model.Location
   */
  void addAliases(PublicationPK pubPK, List<Location> aliases);

  /**
   * Removes the specified aliases of the specified publication. The
   * {@link IllegalArgumentException} is throw if one of the location isn't an  alias.
   * Prefer in this case the {@link PublicationService#removeFather(PublicationPK, NodePK)} method.
   * @param pubPK the identifying key of the publication.
   * @param aliases the aliases to remove.
   * @see org.silverpeas.core.contribution.publication.model.Location
   */
  void removeAliases(PublicationPK pubPK, Collection<Location> aliases);

  /**
   * Gets all the publications attached to the specified father.
   * @param fatherPK the identifying key of the father.
   * @return a collection of {@link PublicationDetail} instances.
   */
  Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK);

  /**
   * Gets all the publications attached to the specified father ordered as indicated by the sorting
   * directive.
   * @param fatherPK the identifying key of the father.
   * @param sorting a sorting directive. Must be in the form of
   * "P.[publication detail attribute] (DESC|ASC)"
   * @return a collection of {@link PublicationDetail} instances.
   */
  Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting);

  /**
   * Gets all the publications attached to the specified father, ordered as indicated by the sorting
   * directive, according to the visibility.
   * @param fatherPK the identifying key of the father.
   * @param sorting a sorting directive. Must be in the form of
   * "P.[publication detail attribute] (DESC|ASC)"
   * @param filterOnVisibilityPeriod is the publications to get must be today visible.
   * @return a collection of {@link PublicationDetail} instances.
   */
  Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting, boolean filterOnVisibilityPeriod);

  /**
   * Gets all the publications attached to the specified father, ordered as indicated by the sorting
   * directive, according to the visibility, and that was authored or updated by the specified
   * user.
   * @param fatherPK the identifying key of the father.
   * @param sorting a sorting directive. Must be in the form of
   * "P.[publication detail attribute] (DESC|ASC)"
   * @param filterOnVisibilityPeriod is the publications to get must be today visible.
   * @param userId the unique identifier of the author or of an updater.
   * @return a collection of {@link PublicationDetail} instances.
   */
  Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting, boolean filterOnVisibilityPeriod, String userId);

  /**
   * Gets all the publications that aren't attached to the specified father.
   * @param fatherPK the identifying key of the father.
   * @return a collection of {@link PublicationDetail} instances.
   */
  Collection<PublicationDetail> getDetailsNotInFatherPK(NodePK fatherPK);

  /**
   * Gets all the publications that aren't attached to the specified father and ordered by the
   * specified sorting directive.
   * @param fatherPK the identifying key of the father.
   * @param sorting a sorting directive. Must be in the form of
   * "P.[publication detail attribute] (DESC|ASC)"
   * @return a collection of {@link PublicationDetail} instances.
   */
  Collection<PublicationDetail> getDetailsNotInFatherPK(NodePK fatherPK, String sorting);

  /**
   * Deletes the specified link between two publications.
   * @param id the unique identifier of a link between two publications.
   */
  void deleteLink(String id);

  /**
   * Gets the complete detail about the specified publication.
   * @param pubPK the identifying key of the publication.
   * @return a {@link CompletePublication} instance.
   */
  CompletePublication getCompletePublication(PublicationPK pubPK);

  /**
   * Gets all the asked publications.
   * @param publicationPKs a collection of identifying key of the publications to get.
   * @return a list of {@link PublicationDetail} instances.
   */
  List<PublicationDetail> getPublications(Collection<PublicationPK> publicationPKs);

  /**
   * Gets publications from given identifiers.
   * @param publicationIds list of identifiers of publications
   * @return a list of {@link PublicationDetail}.
   */
  List<PublicationDetail> getByIds(Collection<String> publicationIds);

  /**
   * Gets all the publications according to given criteria.
   * @param criteria the criteria to process.
   * @return a list of {@link PublicationDetail} instances
   */
  SilverpeasList<PublicationDetail> getPublicationsByCriteria(final PublicationCriteria criteria);

  /**
   * Gets a list of authorized publications by applying given criteria.
   * @param userId the identifier of the user for which access control MUST be verified.
   * @param criteria the criteria.
   * @return a list of publications
   */
  SilverpeasList<PublicationDetail> getAuthorizedPublicationsForUserByCriteria(final String userId,
      final PublicationCriteria criteria);

  int getNbPubByFatherPath(NodePK fatherPK, String fatherPath);

  /**
   * Gets the tree of nodes with the number of publication per node.
   * @param criteria criteria for delimiting the scope of the request.
   * @return the tree of nodes with the number of publication per node
   * @
   */
  Map<String, Integer> getDistributionTree(DistributionTreeCriteria criteria);

  Collection<PublicationDetail> getDetailsByFatherIds(List<String> fatherIds,
      String instanceId, boolean filterOnVisibilityPeriod);

  Collection<PublicationDetail> getDetailsByFatherIdsAndStatus(List<String> fatherIds,
      String instanceId, String sorting, String status);

  Collection<PublicationDetail> getDetailsByFatherIdsAndStatusList(List<String> fatherIds,
      String instanceId, String sorting, List<String> status);

  Collection<PublicationDetail> getDetailsByFatherIdsAndStatusList(List<String> fatherIds,
      String instanceId, String sorting, List<String> status, boolean filterOnVisibilityPeriod);

  Collection<PublicationPK> getPubPKsInFatherPK(NodePK fatherPK);

  void createIndex(PublicationPK pubPK);

  void createIndex(PublicationDetail pubDetail);

  void deleteIndex(PublicationPK pubPK);


  /**
   * Selects massively simple data about publications.
   * <p>
   * For now, only the following data are retrieved:
   *   <ul>
   *     <li>pubId</li>
   *     <li>pubStatus</li>
   *     <li>pubCloneId</li>
   *     <li>pubCloneStatus</li>
   *     <li>instanceId</li>
   *     <li>pubBeginDate</li>
   *     <li>pubEndDate</li>
   *     <li>pubBeginHour</li>
   *     <li>pubEndHour</li>
   *     <li>pubcreatorid</li>
   *     <li>pubupdaterid</li>
   *   </ul>
   *   This method is designed for process performance needs.<br/>
   *   The result is not necessarily into same ordering as the one of given parameter.
   * </p>
   * @param ids the instance ids aimed.
   * @return a list of {@link PublicationDetail} instances.
   */
  List<PublicationDetail> getMinimalDataByIds(Collection<PublicationPK> ids);

  Collection<PublicationDetail> getAllPublications(String instanceId);

  Collection<PublicationDetail> getAllPublications(String instanceId, String sorting);

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

  Collection<PublicationDetail> getDetailBetweenDate(String beginDate, String endDate,
      String instanceId);

  List<ValidationStep> getValidationSteps(PublicationPK pubPK);

  ValidationStep getValidationStepByUser(PublicationPK pubPK,
      String userId);

  void addValidationStep(ValidationStep step);

  void removeValidationSteps(PublicationPK pubPK);

  void changePublicationsOrder(List<String> ids, NodePK nodePK);

  void resetPublicationsOrder(NodePK nodePK);

  Collection<Coordinate> getCoordinates(String pubId, String componentId);

  /**
   * Updates the publication links
   *
   * @param pubPK publication identifier which you want to update links
   * @param links list of publication to link with current.
   * @
   */
  void addLinks(PublicationPK pubPK, List<ResourceReference> links);

  List<SocialInformationPublication> getAllPublicationsWithStatusbyUserid(String userId,
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
