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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.publication.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
import com.stratelia.webactiv.util.publication.info.model.ModelPK;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.ValidationStep;

/*
 * CVS Informations
 *
 * $Id: PublicationBmBusinessSkeleton.java,v 1.24 2008/10/10 11:22:37 neysseri Exp $
 *
 * $Log: PublicationBmBusinessSkeleton.java,v $
 * Revision 1.24  2008/10/10 11:22:37  neysseri
 * Ajout méthode getPublicationPKsByStatus(String status, List componentIds)
 * Optimisations suite visite DSR
 *
 * Revision 1.23.4.1  2008/10/10 11:18:49  neysseri
 * no message
 *
 * Revision 1.23  2008/08/29 14:11:21  neysseri
 * Ajout de la méthode changePublicationsOrder(List ids, NodePK nodePK)
 *
 * Revision 1.22  2008/05/22 12:43:59  neysseri
 * no message
 *
 * Revision 1.21  2008/05/16 10:50:39  neysseri
 * no message
 *
 * Revision 1.20.2.2  2008/05/16 10:46:30  neysseri
 * no message
 *
 * Revision 1.20.2.1  2008/05/09 14:38:04  neysseri
 * no message
 *
 * Revision 1.20  2008/04/16 13:09:55  neysseri
 * no message
 *
 * Revision 1.19.2.1  2008/04/03 09:10:24  neysseri
 * no message
 *
 * Revision 1.19  2008/03/26 13:15:50  neysseri
 * no message
 *
 * Revision 1.18  2008/03/11 15:44:15  neysseri
 * no message
 *
 * Revision 1.17  2007/12/03 14:53:38  neysseri
 * no message
 *
 * Revision 1.16  2007/08/13 08:04:19  neysseri
 * no message
 *
 * Revision 1.15  2007/08/08 14:55:55  neysseri
 * no message
 *
 * Revision 1.14  2007/04/20 14:17:15  neysseri
 * no message
 *
 * Revision 1.13  2007/02/27 08:36:31  neysseri
 * Ajout méthode getDetailBetweenDate()
 *
 * Revision 1.12.12.1  2007/02/16 16:36:00  sfariello
 * modif pour utilisation dans le Blog
 *
 * Revision 1.12.10.1  2006/12/20 17:37:34  dlesimple
 * Modif méthode getDetailsNotInFatherPK
 *
 * Revision 1.12  2005/12/02 13:59:27  neysseri
 * Ajout de deux méthodes :
 * - removeImage(PublicationPK pubPK)
 * - getPublicationsByStatus(String status, List componentIds)
 *
 * Revision 1.11  2005/05/19 14:54:16  neysseri
 * Possibilité de supprimer les Voir Aussi
 *
 * Revision 1.10  2005/04/14 18:13:45  neysseri
 * no message
 *
 * Revision 1.9  2005/02/23 19:13:55  neysseri
 * intégration Import/Export
 *
 * Revision 1.8.2.1  2005/02/08 18:00:22  tleroi
 * *** empty log message ***
 *
 * Revision 1.8  2004/02/06 18:47:04  neysseri
 * Now, the module publication is responsible of the wysiwyg indexation.
 * It is used (like kmelia).
 *
 * Revision 1.7  2003/12/05 15:36:49  neysseri
 * no message
 *
 * Revision 1.6  2003/11/25 09:56:37  neysseri
 * no message
 *
 * Revision 1.5  2003/08/26 09:39:27  neysseri
 * New method added.
 * This method permits to know if a publication already exists in a given instance.
 * This is a based-name search.
 *
 * Revision 1.4  2003/06/30 23:52:53  neysseri
 * no message
 *
 * Revision 1.3  2003/02/28 15:50:44  neysseri
 * no message
 *
 * Revision 1.2  2002/11/28 16:49:25  neysseri
 * QuickInfo in PDC merging
 *
 * Revision 1.1.1.1.18.3  2002/11/28 11:59:53  abudnikau
 * no message
 *
 * Revision 1.1.1.1.18.2  2002/11/27 13:38:21  abudnikau
 * no message
 *
 * Revision 1.1.1.1.18.1  2002/11/26 13:33:30  abudnikau
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.7  2002/07/30 07:26:00  nchaix
 * Merge branche B200006
 *
 * Revision 1.6.30.1  2002/07/22 10:04:29  mnikolaenko
 * no message
 *
 * Revision 1.6  2002/02/04 12:06:44  neysseri
 * Ajout d'une méthode permettant d'obtenir la liste
 * des publications référencées par au moins un père
 *
 * Revision 1.5  2002/01/11 12:40:30  neysseri
 * Stabilisation Lot 2 : Exceptions et Silvertrace
 *
 */

/**
 * Interface declaration
 * 
 * 
 * @author
 */
public interface PublicationBmBusinessSkeleton {

  /**
   * get details on the publication specified by the primary key given in pubPK
   * parameter
   */
  public PublicationDetail getDetail(PublicationPK pubPK)
      throws RemoteException;

  /**
   * Create a new publication A new line will be added in publication table. The
   * "id" in "detail" is not used (a new one will be computed). The "ed" is used
   * to know the table name.
   */
  public PublicationPK createPublication(PublicationDetail detail)
      throws RemoteException;

  public void movePublication(PublicationPK pubPK, NodePK nodePK,
      boolean indexIt) throws RemoteException;

  /**
   * Change order of the given publication identified by pubPK in the given
   * nodePK. direction less than 0 means "up" else it means "down"
   * 
   * @param pubPK
   *          the publication's id to move
   * @param nodePK
   *          the publication's place
   * @param direction
   *          must be different to 0
   * @throws RemoteException
   */
  public void changePublicationOrder(PublicationPK pubPK, NodePK nodePK,
      int direction) throws RemoteException;

  /**
   * remove the publication designed by pubPK parameter.
   */
  public void removePublication(PublicationPK pubPK) throws RemoteException;

  /**
   * remove the image associated to publication designed by pubPK parameter.
   */
  public void removeImage(PublicationPK pubPK) throws RemoteException;

  /**
   * update the publication content.
   */
  public void setDetail(PublicationDetail detaile) throws RemoteException;

  /**
   * add a new father (designed by "fatherPK") to a publication ("pubPK") The
   * publication will be visible from its new father node.
   */
  public void addFather(PublicationPK pubPK, NodePK fatherPK)
      throws RemoteException;

  /**
   * remove a father (designed by "fatherPK") from a publication ("pubPK") The
   * publication won't be visible from its old father node.
   */
  public void removeFather(PublicationPK pubPK, NodePK fatherPK)
      throws RemoteException;

  /**
   * remove a father (designed by "fatherPK") from a publication ("pubPK") The
   * publication won't be visible from its old father node.
   */
  public void removeFather(NodePK fatherPK) throws RemoteException;

  /**
   * remove a father (designed by "fatherPK") from a publication ("pubPK") The
   * publication won't be visible from its old father node.
   */
  public void removeFathers(PublicationPK pubPK, Collection fatherIds)
      throws RemoteException;

  /**
   * remove all father from a publication ("pubPK") The publication won't be
   * visible.
   */
  public void removeAllFather(PublicationPK pubPK) throws RemoteException;

  /**
   * remove all links between publications and node N N is a descendant of the
   * node designed by originPK
   */
  // public void removeAllIssue(NodePK originPK, PublicationPK pubPK) throws
  // RemoteException;

  /**
   * return the Detail of publication which are not linked to a father
   */
  public Collection getOrphanPublications(PublicationPK pubPK)
      throws RemoteException;

  /**
   * return the Detail of publication which are linked to at least one father
   */
  public Collection getNotOrphanPublications(PublicationPK pubPK)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pubPK
   * @param creatorId
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void deleteOrphanPublicationsByCreatorId(PublicationPK pubPK,
      String creatorId) throws RemoteException;

  /**
   * return the publications : - which take place in the basket - which are out
   * of the visibility period
   */
  public Collection getUnavailablePublicationsByPublisherId(
      PublicationPK pubPK, String publisherId, String nodeId)
      throws RemoteException;

  /**
   * return a collection, containing all node primary key from where the
   * publication is visible
   */
  public Collection getAllFatherPK(PublicationPK pubPK) throws RemoteException;

  /**
   * return the publication's collection of Alias
   */
  public Collection getAlias(PublicationPK pubPK) throws RemoteException;

  public void addAlias(PublicationPK pubPK, List alias) throws RemoteException;

  public void removeAlias(PublicationPK pubPK, List alias)
      throws RemoteException;

  /**
   * return a PublicationDetail collection of all publication visible from the
   * node identified by "fatherPK" parameter
   */
  public Collection getDetailsByFatherPK(NodePK fatherPK)
      throws RemoteException;

  public Collection getDetailsByFatherPK(NodePK fatherPK, String sorting)
      throws RemoteException;

  public Collection getDetailsByFatherPK(NodePK fatherPK, String sorting,
      boolean filterOnVisibilityPeriod) throws RemoteException;

  public Collection getDetailsByFatherPK(NodePK fatherPK, String sorting,
      boolean filterOnVisibilityPeriod, String userId) throws RemoteException;

  /**
   * return a PublicationDetail collection of all publications not in the node
   * identified by "fatherPK" parameter
   */
  public Collection getDetailsNotInFatherPK(NodePK fatherPK)
      throws RemoteException;

  public Collection getDetailsNotInFatherPK(NodePK fatherPK, String sorting)
      throws RemoteException;

  /**
   * return a PublicationDetail collection of x last publications
   */
  public Collection getDetailsByBeginDateDescAndStatus(PublicationPK pk,
      String status, int nbPubs) throws RemoteException;

  /**
   * return a PublicationDetail collection of x last publications
   */
  public Collection getDetailsByBeginDateDesc(PublicationPK pk, int nbPubs)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pk
   * @param status
   * @param nbPubs
   * @param fatherId
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getDetailsByBeginDateDescAndStatusAndNotLinkedToFatherId(
      PublicationPK pk, String status, int nbPubs, String fatherId)
      throws RemoteException;

  /**
     *
     */
  public Collection getAllModelsDetail( /* PublicationPK pubPK */)
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

  public void deleteInfoLinks(PublicationPK pubPK, List pubIds)
      throws RemoteException;

  /**
     *
     */
  public CompletePublication getCompletePublication(PublicationPK pubPK)
      throws RemoteException;

  /**
     *
     */
  public Collection getPublications(Collection publicationPKs)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param status
   * @param pubPK
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getPublicationsByStatus(String status, PublicationPK pubPK)
      throws RemoteException;

  public Collection getPublicationPKsByStatus(String status, List componentIds)
      throws RemoteException;

  public Collection getPublicationsByStatus(String status, List componentIds)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param query
   * @param pubPK
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection searchByKeywords(String query, PublicationPK pubPK)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param fatherPKs
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public int getNbPubInFatherPKs(Collection fatherPKs) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param fatherPK
   * @param fatherPath
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public int getNbPubByFatherPath(NodePK fatherPK, String fatherPath)
      throws RemoteException;

  public Hashtable getDistribution(String instanceId, String statusSubQuery,
      boolean checkVisibility) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param fatherIds
   * @param pubPK
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getDetailsByFatherIds(ArrayList fatherIds,
      PublicationPK pubPK) throws RemoteException;

  public Collection getDetailsByFatherIds(ArrayList fatherIds,
      PublicationPK pubPK, boolean filterOnVisibilityPeriod)
      throws RemoteException;

  public Collection getDetailsByFatherIds(ArrayList fatherIds,
      PublicationPK pubPK, String sorting) throws RemoteException;

  public Collection getDetailsByFatherIdsAndStatus(ArrayList fatherIds,
      PublicationPK pubPK, String sorting, String status)
      throws RemoteException;

  public Collection getDetailsByFatherIdsAndStatusList(ArrayList fatherIds,
      PublicationPK pubPK, String sorting, ArrayList status)
      throws RemoteException;

  public Collection getDetailsByFatherIdsAndStatusList(ArrayList fatherIds,
      PublicationPK pubPK, String sorting, ArrayList status,
      boolean filterOnVisibilityPeriod) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param fatherPKs
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getPubPKsInFatherPKs(Collection fatherPKs)
      throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param fatherPK
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getPubPKsInFatherPK(NodePK fatherPK) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pubPK
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void createIndex(PublicationPK pubPK) throws RemoteException;

  public void createIndex(PublicationDetail pubDetail) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pubPK
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public void deleteIndex(PublicationPK pubPK) throws RemoteException;

  /**
   * Method declaration
   * 
   * 
   * @param pubPK
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getAllPublications(PublicationPK pubPK)
      throws RemoteException;

  public Collection getAllPublications(PublicationPK pubPK, String sorting)
      throws RemoteException;

  /**
   * Looking for, in the instance identified by pubPK, a publication named
   * pubName.
   * 
   * @param pubPK
   *          the identifier of the instance
   * @param pubName
   *          the publication's name we are looking for
   * 
   * @return the pubId if a publication already exists in this component's
   *         instance. null otherwise.
   * 
   * @throws RemoteException
   * 
   */
  public PublicationDetail getDetailByName(PublicationPK pubPK, String pubName)
      throws RemoteException;

  public PublicationDetail getDetailByNameAndNodeId(PublicationPK pubPK,
      String pubName, int nodeId) throws RemoteException;

  /**
   * A wysiwyg's content has been added or modified to a publication. Its
   * content must be added to the indexed content of the publication
   * 
   * @param pubPK
   *          the identifier of the publication associated to the wysiwyg
   * 
   * @throws RemoteException
   * 
   */
  public void processWysiwyg(PublicationPK pubPK) throws RemoteException;

  /**
   * 
   * @param beginDate
   * @param endDate
   * @param instanceId
   * 
   * @return Collection of PublicationDetail.
   * 
   * @throws RemoteException
   * 
   */
  public Collection getDetailBetweenDate(String beginDate, String endDate,
      String instanceId) throws RemoteException;

  public List getValidationSteps(PublicationPK pubPK) throws RemoteException;

  public ValidationStep getValidationStepByUser(PublicationPK pubPK,
      String userId) throws RemoteException;

  public void addValidationStep(ValidationStep step) throws RemoteException;

  public void removeValidationSteps(PublicationPK pubPK) throws RemoteException;

  public void changePublicationsOrder(List ids, NodePK nodePK)
      throws RemoteException;

}