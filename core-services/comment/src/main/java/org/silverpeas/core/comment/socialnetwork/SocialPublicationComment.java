/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.comment.socialnetwork;

import jakarta.inject.Inject;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.comment.service.CommentServiceProvider;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.socialnetwork.provider.SocialPublicationCommentProvider;
import org.silverpeas.core.util.URLUtil;

import java.util.*;

@Provider
public class SocialPublicationComment
    implements SocialPublicationCommentProvider<SocialInformationComment> {

  @Inject
  private PublicationService publicationService;

  private List<String> getListResourceType() {
    List<String> listResourceType = new ArrayList<>();
    listResourceType.add(PublicationDetail.getResourceType()); //kmelia and blog components
    return listResourceType;
  }

  private PublicationService getService() {
    return publicationService;
  }

  private List<SocialInformationComment> decorate(
      List<SocialInformationComment> listSocialInformation) {
    for (SocialInformationComment socialInformation : listSocialInformation) {
      String resourceId = socialInformation.getComment().getResourceReference().getLocalId();
      String instanceId = socialInformation.getComment().getComponentInstanceId();
      PublicationPK pubPk = new PublicationPK(resourceId, instanceId);
      PublicationDetail pubDetail = getService().getDetail(pubPk);

      //set URL, title and description of the publication
      socialInformation.setUrl(
          URLUtil.getSimpleURL(URLUtil.URL_PUBLI, pubDetail.getId(), pubDetail.getInstanceId(),
              false));
      socialInformation.setTitle(pubDetail.getTitle());
    }

    return listSocialInformation;
  }

  @Override
  public List<SocialInformationComment> getSocialInformationList(String userId, Date begin,
      Date end) {
    List<SocialInformationComment> listSocialInformation =
        CommentServiceProvider.getCommentService()
            .getSocialInformationCommentsListByUserId(getListResourceType(), userId,
                Period.between(begin.toInstant(), end.toInstant()));

    return decorate(listSocialInformation);
  }

  @Override
  public List<SocialInformationComment> getSocialInformationListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) {
    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
    List<String> instanceIds = new ArrayList<>();
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "kmelia")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "blog")));

    List<SocialInformationComment> socialComments = CommentServiceProvider.getCommentService()
        .getSocialInformationCommentsListOfMyContacts(getListResourceType(), myContactsIds,
            instanceIds, Period.between(begin.toInstant(), end.toInstant()));

    // Even if the data has been found by filtering on instanceIds that the user can access, it
    // could exist more precise right rules to apply.
    Iterator<SocialInformationComment> socialCommentIt = socialComments.iterator();
    while (socialCommentIt.hasNext()) {
      SocialInformationComment socialComment = socialCommentIt.next();
      String instanceId = socialComment.getComment().getComponentInstanceId();

      if (!myId.equals(socialComment.getAuthor()) && instanceId.startsWith("kmelia") &&
          !PublicationAccessControl.get()
              .isUserAuthorized(myId,
                  new PublicationPK(socialComment.getComment().getResourceReference().getLocalId(),
                      instanceId))) {

        // On Kmelia application, if the user has not access right to the publication, then the
        // associated comments are removed from the result
        socialCommentIt.remove();
      }
    }

    return decorate(socialComments);
  }
}