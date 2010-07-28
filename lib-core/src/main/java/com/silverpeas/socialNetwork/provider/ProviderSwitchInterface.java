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

import com.silverpeas.socialNetwork.model.SocialInformationType;
import java.util.List;

/**
 *
 * @author Bensalem Nabil
 */
public interface ProviderSwitchInterface {

  /*
   * get the List of social Information according the type of social information
   * and the UserId
   *
   * @param:SocialInformationType socialInformationType, String userId,String classification, int limit  ,int offset
   */
  public List getSocialInformationsList(SocialInformationType socialInformationType, String userId,
      String classification, int limit, int offset);

  public SocialEventsInterface getSocialEventsInterface();

  public void setSocialEventsInterface(SocialEventsInterface socialEventsInterface);

  public SocialGalleryInterface getSocialGalleryInterface();

  public void setSocialGalleryInterface(SocialGalleryInterface socialGalleryInterface);

  public SocialPublicationsInterface getSocialPublicationsInterface();

  public void setSocialPublicationsInterface(SocialPublicationsInterface socialPublicationsInterface);

   public SocialStatusInterface getSocialStatusInterface();

  public void setSocialStatusInterface(SocialStatusInterface socialStatusInterface);
}
