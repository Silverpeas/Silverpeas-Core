/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.stratelia.webactiv.publication.social;

import com.silverpeas.calendar.Date;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.comment.socialnetwork.SocialInformationComment;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.provider.SocialCommentPublicationsInterface;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SocialCommentPublications implements SocialCommentPublicationsInterface {

  private List<String> getListResourceType() {
    List<String> listResourceType = new ArrayList<String>();
    listResourceType.add(Comment.PUBLICATION_RESOURCETYPE); //kmelia and blog components
    return listResourceType;
  }

  private PublicationBm getEJB() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBm.class);
    } catch (Exception e) {
      throw new PublicationRuntimeException("SocialCommentPublications.getEJB()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private List<SocialInformation> fillOtherSocialInformation(
      List<SocialInformation> listSocialInformation) {
    for (SocialInformation socialInformation : listSocialInformation) {
      SocialInformationComment socialCommentPublication =
          (SocialInformationComment) socialInformation;
      String resourceId = socialCommentPublication.getResourceId();
      String instanceId = socialCommentPublication.getInstanceId();
      PublicationPK pubPk = new PublicationPK(resourceId, instanceId);
      PublicationDetail pubDetail = getEJB().getDetail(pubPk);

      //set URL, title and description of the publication
      socialCommentPublication.setUrl(
          URLManager.getSimpleURL(URLManager.URL_PUBLI, pubDetail.getId(), pubDetail.getComponentInstanceId(), false));
      socialCommentPublication.setTitle(pubDetail.getTitle());
    }

    return listSocialInformation;
  }

  /**
   * get list of SocialInformation
   * @param userId
   * @param begin
   * @param end
   * @return List<SocialInformation>
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, Date begin, Date end)
      throws SilverpeasException {

    List<String> listResourceType = getListResourceType();

    List<SocialInformation> listSocialInformation =
        CommentServiceFactory.getFactory().getCommentService()
            .getSocialInformationCommentsListByUserId(listResourceType, userId, begin, end);

    listSocialInformation = fillOtherSocialInformation(listSocialInformation);
    return listSocialInformation;
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

    List<String> listResourceType = getListResourceType();

    // getting all components (that manage publications and comments in publications) allowed to me
    OrganisationController oc = OrganisationControllerFactory.getOrganisationController();
    List<String> options = new ArrayList<String>();
    options.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "kmelia")));
    options.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "blog")));

    List<SocialInformation> listSocialInformation =
        CommentServiceFactory.getFactory().getCommentService()
            .getSocialInformationCommentsListOfMyContacts(listResourceType, myContactsIds, options,
                begin, end);

    listSocialInformation = fillOtherSocialInformation(listSocialInformation);
    return listSocialInformation;
  }
}