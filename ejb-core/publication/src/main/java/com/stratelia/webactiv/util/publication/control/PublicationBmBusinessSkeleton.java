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
package com.stratelia.webactiv.util.publication.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.publication.socialNetwork.SocialInformationPublication;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.coordinates.model.Coordinate;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationWithStatus;
import com.stratelia.webactiv.util.publication.model.ValidationStep;
import java.lang.reflect.Array;

/**
 * Interface declaration
 * @author
 */
public interface PublicationBmBusinessSkeleton {

  /**
   * get details on the publication specified by the primary key given in pubPK parameter
   */
  public PublicationDetail getDetail(PublicationPK pubPK)
      throws RemoteException;

  /**
   * Create a new publication A new line will be added in publication table. The "id" in "detail" is
   * not used (a new one will be computed). The "ed" is used to know the table name.
   */
  public PublicationPK createPublication(PublicationDetail detail)
      throws RemoteException;

  public void movePublication(PublicationPK pubPK, NodePK nodePK,
      boolean indexIt) throws RemoteException;

  /**
   * Change order of the given publication identified by pubPK in the given nodePK. direction less
   * than 0 means "up" else it means "down"
   * @param pubPK the publication's id to move
   * @param nodePK the publication's place
   * @param direction must be different to 0
   * @throws RemoteException
   */
  public void changePublicationOrder(PublicationPK pubPK, NodePK nodePK,
      int direction) throws RemoteException;

  /**
   * remove the publication designed by pubPK parameter.
   */
  public void removePublication(PublicationPK pubPK) throws RemoteException;

  /**
   * update the publication content.
   */
  public void setDetail(PublicationDetail detaile) throws RemoteException;

  public void setDetail(PublicationDetail detaile, boolean forceUpdateDate) throws RemoteException;

  /**
   * add a new father (designed by "fatherPK") to a publication ("pubPK") The publication will be
   * visible from its new father node.
   */
  public void addFather(PublicationPK pubPK, NodePK fatherPK)
      throws RemoteException;

  /**
   * remove a father (designed by "fatherPK") from a publication ("pubPK") The publication won't be
   * visible from its old father node.
   */
  public void removeFather(PublicationPK pubPK, NodePK fatherPK)
      throws RemoteException;

  /**
   * remove a father (designed by "fatherPK") from a publication ("pubPK") The publication won't be
   * visible from its old father node.
   */
  public void removeFather(NodePK fatherPK) throws RemoteException;

  /**
   * remove a father (designed by "fatherPK") from a publication ("pubPK") The publication won't be
   * visible from its old father node.
   */
  public void removeFathers(PublicationPK pubPK, Collection<String> fatherIds)
      throws RemoteException;

  /**
   * remove all father from a publication ("pubPK") The publication won't be visible.
   */
  public void removeAllFather(PublicationPK pubPK) throws RemoteException;

  /**
   * return the Detail of publication which are not linked to a father
   */
  public Collection<PublicationDetail> getOrphanPublications(PublicationPK pubPK)
      throws RemoteException;

  /**
   * return the Detail of publication which are linked to at least one father
   */
  public Collection<PublicationDetail> getNotOrphanPublications(PublicationPK pubPK)
      throws RemoteException;

  /**
   * Method declaration
   * @param pubPK
   * @param creatorId
   * @throws RemoteException
   * @see
   */
  public void deleteOrphanPublicationsByCreatorId(PublicationPK pubPK,
      String creatorId) throws RemoteException;

  /**
   * return the publications : - which take place in the basket - which are out of the visibility
   * period
   */
  public Collection<PublicationDetail> getUnavailablePublicationsByPublisherId(
      PublicationPK pubPK, String publisherId, String nodeId)
      throws RemoteException;

  /**
   * return a collection, containing all node primary key from where the publication is visible
   */
  public Collection<NodePK> getAllFatherPK(PublicationPK pubPK) throws RemoteException;

  /**
   * return the publication's collection of Alias
   */
  public Collection<Alias> getAlias(PublicationPK pubPK) throws RemoteException;

  public void addAlias(PublicationPK pubPK, List<Alias> alias) throws RemoteException;

  public void removeAlias(PublicationPK pubPK, List<Alias> alias)
      throws RemoteException;

  /**
   * return a PublicationDetail collection of all publication visible from the node identified by
   * "fatherPK" parameter
   */
  public Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK)
      throws RemoteException;

  public Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting)
      throws RemoteException;

  public Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting,
      boolean filterOnVisibilityPeriod) throws RemoteException;

  public Collection<PublicationDetail> getDetailsByFatherPK(NodePK fatherPK, String sorting,
      boolean filterOnVisibilityPeriod, String userId) throws RemoteException;

  /**
   * return a PublicationDetail collection of all publications not in the node identified by
   * "fatherPK" parameter
   */
  public Collection<PublicationDetail> getDetailsNotInFatherPK(NodePK fatherPK)
      throws RemoteException;

  public Collection<PublicationDetail> getDetailsNotInFatherPK(NodePK fatherPK, String sorting)
      throws RemoteException;

  /**
   * return a PublicationDetail collection of x last publications
   */
  public Collection<PublicationDetail> getDetailsByBeginDateDescAndStatus(PublicationPK pk,
      String status, int nbPubs) throws RemoteException;

  /**
   * return a PublicationDetail collection of x last publications
   */
  public Collection<PublicationDetail> getDetailsByBeginDateDesc(PublicationPK pk, int nbPubs)
      throws RemoteException;

  /**
   * Method declaration
   * @param pk
   * @param status
   * @param nbPubs
   * @param fatherId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<PublicationDetail> getDetailsByBeginDateDescAndStatusAndNotLinkedToFatherId(
      PublicationPK pk, String status, int nbPubs, String fatherId)
      throws RemoteException;

  /**
   *
   */
  public Collection<ModelDetail> getAllModelsDetail( /* PublicationPK pubPK */)
      throws RemoteException;

  /**
   *
   */
  public ModelDetail getModelDetail(ModelPK modelPK) throws RemoteException;

  /**
   *
   */
  public void createInfoDetail(PublicationPK pubPK, ModelPK modelPK,
      InfoDetail infos) throws RemoteException;

  /**
   *
   */
  public void createInfoModelDetail(PublicationPK pubPK, ModelPK modelPK,
      InfoDetail infos) throws RemoteException;

  /**
   *
   */
  public InfoDetail getInfoDetail(PublicationPK pubPK) throws RemoteException;

  /**
   *
   */
  public void updateInfoDetail(PublicationPK pubPK, InfoDetail infos)
      throws RemoteException;

  public void deleteInfoLinks(PublicationPK pubPK, List<ForeignPK> links)
      throws RemoteException;

  /**
   *
   */
  public CompletePublication getCompletePublication(PublicationPK pubPK)
      throws RemoteException;

  /**
   *
   */
  public Collection<PublicationDetail> getPublications(Collection<PublicationPK> publicationPKs)
      throws RemoteException;

  /**
   * Method declaration
   * @param status
   * @param pubPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<PublicationDetail> getPublicationsByStatus(String status, PublicationPK pubPK)
      throws RemoteException;

  public Collection<PublicationPK> getPublicationPKsByStatus(String status,
      List<String> componentIds)
      throws RemoteException;

  public Collection<PublicationDetail> getPublicationsByStatus(String status,
      List<String> componentIds)
      throws RemoteException;

  /**
   * Method declaration
   * @param query
   * @param pubPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<PublicationDetail> searchByKeywords(String query, PublicationPK pubPK)
      throws RemoteException;

  /**
   * Method declaration
   * @param fatherPKs
   * @return
   * @throws RemoteException
   * @see
   */
  public int getNbPubInFatherPKs(Collection<NodePK> fatherPKs) throws RemoteException;

  /**
   * Method declaration
   * @param fatherPK
   * @param fatherPath
   * @return
   * @throws RemoteException
   * @see
   */
  public int getNbPubByFatherPath(NodePK fatherPK, String fatherPath)
      throws RemoteException;

  public Hashtable<String, Integer> getDistribution(String instanceId, String statusSubQuery,
      boolean checkVisibility) throws RemoteException;

  /**
   * Method declaration
   * @param fatherIds
   * @param pubPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<PublicationDetail> getDetailsByFatherIds(ArrayList<String> fatherIds,
      PublicationPK pubPK) throws RemoteException;

  public Collection<PublicationDetail> getDetailsByFatherIds(ArrayList<String> fatherIds,
      PublicationPK pubPK, boolean filterOnVisibilityPeriod)
      throws RemoteException;

  public Collection<PublicationDetail> getDetailsByFatherIds(ArrayList<String> fatherIds,
      PublicationPK pubPK, String sorting) throws RemoteException;

  public Collection<PublicationDetail> getDetailsByFatherIdsAndStatus(ArrayList<String> fatherIds,
      PublicationPK pubPK, String sorting, String status)
      throws RemoteException;

  public Collection<PublicationDetail> getDetailsByFatherIdsAndStatusList(
      ArrayList<String> fatherIds,
      PublicationPK pubPK, String sorting, ArrayList<String> status)
      throws RemoteException;

  public Collection<PublicationDetail> getDetailsByFatherIdsAndStatusList(
      ArrayList<String> fatherIds,
      PublicationPK pubPK, String sorting, ArrayList<String> status,
      boolean filterOnVisibilityPeriod) throws RemoteException;

  /**
   * Method declaration
   * @param fatherPKs
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<PublicationPK> getPubPKsInFatherPKs(Collection<WAPrimaryKey> fatherPKs)
      throws RemoteException;

  /**
   * Method declaration
   * @param fatherPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<PublicationPK> getPubPKsInFatherPK(NodePK fatherPK) throws RemoteException;

  /**
   * Method declaration
   * @param pubPK
   * @throws RemoteException
   * @see
   */
  public void createIndex(PublicationPK pubPK) throws RemoteException;

  public void createIndex(PublicationDetail pubDetail) throws RemoteException;

  /**
   * Method declaration
   * @param pubPK
   * @throws RemoteException
   * @see
   */
  public void deleteIndex(PublicationPK pubPK) throws RemoteException;

  /**
   * Method declaration
   * @param pubPK
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<PublicationDetail> getAllPublications(PublicationPK pubPK)
      throws RemoteException;

  public Collection<PublicationDetail> getAllPublications(PublicationPK pubPK, String sorting)
      throws RemoteException;

  /**
   * Looking for, in the instance identified by pubPK, a publication named pubName.
   * @param pubPK the identifier of the instance
   * @param pubName the publication's name we are looking for
   * @return the pubId if a publication already exists in this component's instance. null otherwise.
   * @throws RemoteException
   */
  public PublicationDetail getDetailByName(PublicationPK pubPK, String pubName)
      throws RemoteException;

  public PublicationDetail getDetailByNameAndNodeId(PublicationPK pubPK,
      String pubName, int nodeId) throws RemoteException;

  /**
   * A wysiwyg's content has been added or modified to a publication. Its content must be added to
   * the indexed content of the publication
   * @param pubPK the identifier of the publication associated to the wysiwyg
   * @throws RemoteException
   */
  public void processWysiwyg(PublicationPK pubPK) throws RemoteException;

  /**
   * @param beginDate
   * @param endDate
   * @param instanceId
   * @return Collection of PublicationDetail.
   * @throws RemoteException
   */
  public Collection<PublicationDetail> getDetailBetweenDate(String beginDate, String endDate,
      String instanceId) throws RemoteException;

  public List<ValidationStep> getValidationSteps(PublicationPK pubPK) throws RemoteException;

  public ValidationStep getValidationStepByUser(PublicationPK pubPK,
      String userId) throws RemoteException;

  public void addValidationStep(ValidationStep step) throws RemoteException;

  public void removeValidationSteps(PublicationPK pubPK) throws RemoteException;

  public void changePublicationsOrder(List<String> ids, NodePK nodePK)
      throws RemoteException;

  public Collection<Coordinate> getCoordinates(String pubId, String componentId)
      throws RemoteException;

  /**
   * Updates the publication links
   * @param pubPK publication identifier which you want to update links
   * @param links list of publication to link with current.
   * @throws RemoteException
   */
  public void addLinks(PublicationPK pubPK, List<ForeignPK> links) throws RemoteException;

  /**
   *
   **/
  public List<SocialInformationPublication> getAllPublicationsWithStatusbyUserid(String userId,
      int firstIndex, int nbElement) throws RemoteException;

  /**
   * gets the available component for a given users list
   * @param:Array usersId,int firstIndex
   * @return a list of ComponentName
   *
   */
  public List<String> getAvailableComponents(String myId, List<String> myContactsId) throws
      RemoteException;

  /**
   * get list of socialInformation of my contacts according to options and number of Item and the first Index
   * @return: List <SocialInformation>
   * @param:  myId
   * @param :List<String> myContactsIds
   * @param :List<String> options list of Available Components name
   * @param int numberOfElement, int firstIndex
   */
  public List<SocialInformationPublication> getSocialInformationsListOfMyContacts(
      List<String> myContactsIds, List<String> options, int numberOfElement,
      int firstIndex) throws RemoteException;
}
