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
package org.silverpeas.core.contribution.publication.model;

import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.security.authorization.PublicationAccessControl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This object contains the description of a complete publication (publication parameter, info)
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class CompletePublication implements Serializable {

  private static final long serialVersionUID = 7644813195325660580L;

  private PublicationDetail pubDetail;

  /**
   * The publications linked to the current publication
   */
  private List<PublicationLink> linkList;

  /**
   * The publications which are a reference to the current publication
   */
  private List<PublicationLink> reverseLinkList;

  private List<ValidationStep> validationSteps = null;

  /**
   * @param pubDetail The main information of the publication
   * @param linkList The publications linked to the current publication
   * @param reverseLinkList The publications which are a reference to the current publication
   */
  public CompletePublication(PublicationDetail pubDetail, List<PublicationLink> linkList,
      List<PublicationLink> reverseLinkList) {
    this.pubDetail = pubDetail;
    this.linkList = linkList;
    this.reverseLinkList = reverseLinkList;
  }

  /**
   * Get the publication parameters
   * @return a PublicationDetail - the publication parameters
   * @since 1.0
   */
  public PublicationDetail getPublicationDetail() {
    return pubDetail;
  }

  /**
   * @return the linkList
   */
  public List<PublicationLink> getLinkList() {
    return linkList;
  }

  /**
   * @return the reverseLinkList
   */
  public List<PublicationLink> getReverseLinkList() {
    return reverseLinkList;
  }

  public void setValidationSteps(List<ValidationStep> validationSteps) {
    this.validationSteps = validationSteps;
  }

  public List<ValidationStep> getValidationSteps() {
    return validationSteps;
  }

  public List<PublicationLink> getLinkedPublications(String userId) {
    List<PublicationLink> publications = getAuthorizedLinks(userId, linkList);
    // remove reverse link linked by the same publication
    List<PublicationLink> reverseLinkListWithoutDuplicates = new ArrayList<>();
    for (PublicationLink reverseLink : reverseLinkList) {
      if (!isReverseLinkADuplication(publications, reverseLink)) {
        reverseLinkListWithoutDuplicates.add(reverseLink);
      }
    }
    publications.addAll(getAuthorizedLinks(userId, reverseLinkListWithoutDuplicates));
    return publications;
  }

  private boolean isReverseLinkADuplication(List<PublicationLink> links, PublicationLink linkToTest) {
    for (PublicationLink link : links) {
      if (link.getTarget().equals(linkToTest.getTarget())) {
        return true;
      }
    }
    return false;
  }

  private List<PublicationLink> getAuthorizedLinks(String userId, List<PublicationLink> links) {
    PublicationService publicationService = PublicationService.get();
    PublicationAccessControl accessController = PublicationAccessControl.get();
    List<PublicationLink> authorizedLinks = new ArrayList<>();
    for (PublicationLink link : links) {
      PublicationPK pk = new PublicationPK(link.getTarget().getLocalId(),
          link.getTarget().getComponentInstanceId());
      if (accessController.isUserAuthorized(userId, pk)) {
        PublicationDetail publi = publicationService.getDetail(pk);
        if (publi != null) {
          link.setPub(publi);
          authorizedLinks.add(link);
        }
      }
    }
    return authorizedLinks;
  }
}