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
package com.silverpeas.socialNetwork.provider;

import com.silverpeas.socialNetwork.model.SocialInformation;
import com.silverpeas.socialNetwork.model.SocialInformationType;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bensalem Nabil
 */
public class ProviderSwitch implements ProviderSwitchInterface {

  private SocialEventsInterface socialEventsInterface;
  private SocialGalleryInterface socialGalleryInterface;
  private SocialPublicationsInterface socialPublicationsInterface;
  private SocialStatusInterface socialStatusInterface;

  @Override
  public SocialEventsInterface getSocialEventsInterface() {
    return socialEventsInterface;
  }

  @Override
  public void setSocialEventsInterface(SocialEventsInterface socialEventsInterface) {
    this.socialEventsInterface = socialEventsInterface;
  }

  @Override
  public SocialGalleryInterface getSocialGalleryInterface() {
    return socialGalleryInterface;
  }

  @Override
  public void setSocialGalleryInterface(SocialGalleryInterface socialGalleryInterface) {
    this.socialGalleryInterface = socialGalleryInterface;
  }

  @Override
  public List getSocialInformationsList(SocialInformationType socialInformationType, String userId,
      String classification, int limit, int offset) {

    List<SocialInformation> list = new ArrayList<SocialInformation>();
    try {
      switch (socialInformationType) {
        case EVENT:
          list = getSocialEventsInterface().getSocialInformationsList(userId, classification, limit,
              offset);
          break;
        case PHOTO:

          list = getSocialGalleryInterface().getSocialInformationsList(userId, limit, offset);
          break;
        case PUBLICATION:
          list = getSocialPublicationsInterface().getSocialInformationsList(userId, limit, offset);

          break;
          case STATUS:
          list = getSocialStatusInterface().getSocialInformationsList(userId, limit, offset);

          break;
        case ALL:
          for (SocialInformationType type : SocialInformationType.values()) {
            if (socialInformationType.ALL != type && socialInformationType.EVENT != type) {
              List<SocialInformation> listAll = getSocialInformationsList(type, userId,
                  classification,
                  limit, offset);
              if (!(listAll == null)) {
                list.addAll(listAll);
              }
            }
          }

          break;
        default:
        //    list = new SocialEvent().getSocialInformationsList(getUserId(), null, limit, offset);
      }

    } catch (SilverpeasException ex) {
      Logger.getLogger(ProviderSwitch.class.getName()).log(Level.SEVERE, null, ex);
    }
    return list;

  }

  @Override
  public SocialPublicationsInterface getSocialPublicationsInterface() {
    return socialPublicationsInterface;
  }

  @Override
  public void setSocialPublicationsInterface(SocialPublicationsInterface socialPublicationsInterface) {
    this.socialPublicationsInterface = socialPublicationsInterface;
  }

  @Override
  public SocialStatusInterface getSocialStatusInterface() {
    return socialStatusInterface;
  }

  @Override
  public void setSocialStatusInterface(SocialStatusInterface socialStatusInterface) {
   this.socialStatusInterface  =socialStatusInterface;
  }
}
