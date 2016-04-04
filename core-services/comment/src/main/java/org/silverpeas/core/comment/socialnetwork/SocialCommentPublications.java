/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.comment.socialnetwork;

import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.AccessControllerProvider;
import org.silverpeas.core.date.Date;
import org.silverpeas.core.comment.service.CommentServiceProvider;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.provider.SocialCommentPublicationsInterface;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.exception.SilverpeasException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Singleton
public class SocialCommentPublications implements SocialCommentPublicationsInterface {

  @Inject
  private PublicationService publicationService;

  private List<String> getListResourceType() {
    List<String> listResourceType = new ArrayList<String>();
    listResourceType.add(PublicationDetail.getResourceType()); //kmelia and blog components
    return listResourceType;
  }

  private PublicationService getService() {
    return publicationService;
  }

  @SuppressWarnings("unchecked")
  private List<SocialInformation> decorate(List<SocialInformationComment> listSocialInformation) {
    for (SocialInformationComment socialInformation : listSocialInformation) {
      String resourceId = socialInformation.getComment().getForeignKey().getId();
      String instanceId = socialInformation.getComment().getComponentInstanceId();
      PublicationPK pubPk = new PublicationPK(resourceId, instanceId);
      PublicationDetail pubDetail = getService().getDetail(pubPk);

      //set URL, title and description of the publication
      socialInformation.setUrl(URLUtil
          .getSimpleURL(URLUtil.URL_PUBLI, pubDetail.getId(), pubDetail.getComponentInstanceId(),
              false));
      socialInformation.setTitle(pubDetail.getTitle());
    }

    return (List) listSocialInformation;
  }

  /**
   * get list of SocialInformation
   * @param userId
   * @param begin
   * @param end
   * @return List<SocialInformation>
   * @throws SilverpeasException
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, Date begin, Date end)
      throws SilverpeasException {

    List<SocialInformationComment> listSocialInformation =
        CommentServiceProvider.getCommentService()
            .getSocialInformationCommentsListByUserId(getListResourceType(), userId,
                Period.from(begin, end));

    return decorate(listSocialInformation);
  }

  /**
   * get list of socialInformation of my contacts according to ids of my contacts
   * @param myId
   * @param myContactsIds
   * @param begin
   * @param end
   * @return List<SocialInformation>
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) throws SilverpeasException {

    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
    List<String> instanceIds = new ArrayList<String>();
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "kmelia")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "blog")));

    List<SocialInformationComment> socialComments =
        CommentServiceProvider.getCommentService()
            .getSocialInformationCommentsListOfMyContacts(getListResourceType(), myContactsIds,
                instanceIds, Period.from(begin, end));

    // Even if the data has been found by filtering on instanceIds that the user can access, it
    // could exists more precise right rules to apply.
    Iterator<SocialInformationComment> socialCommentIt = socialComments.iterator();
    while (socialCommentIt.hasNext()) {
      SocialInformationComment socialComment = socialCommentIt.next();
      String instanceId = socialComment.getComment().getComponentInstanceId();

      if (!myId.equals(socialComment.getAuthor()) && instanceId.startsWith("kmelia")) {

        // On Kmelia application, if the user has not right access to the publication, then the
        // associated comments are removed from the result

        AccessController<PublicationPK> publicationAccessController =
            AccessControllerProvider.getAccessController(PublicationAccessControl.class);
        if (!publicationAccessController.isUserAuthorized(myId,
            new PublicationPK(socialComment.getComment().getForeignKey().getId(), instanceId))) {
          socialCommentIt.remove();
        }
      }
    }

    return decorate(socialComments);
  }
}