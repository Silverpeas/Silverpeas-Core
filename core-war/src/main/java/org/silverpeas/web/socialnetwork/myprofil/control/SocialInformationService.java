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
package org.silverpeas.web.socialnetwork.myprofil.control;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.socialnetwork.provider.SocialInformationProviderSwitcher;
import org.silverpeas.core.socialnetwork.provider.SocialInformationProviderSwitcher.SocialInfoContext;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

/**
 * @author Bensalem Nabil;
 */
@Service
@Singleton
public class SocialInformationService {

  @Inject
  private SocialInformationProviderSwitcher switchInterface;

  /**
   * get the List of social Information of my according the type of social information and the
   * UserId
   * @return: List<SocialInformation>
   * @param socialInformationType
   * @param userId
   * @param classification
   * @param period
   */

  public List<SocialInformation> getSocialInformationsList(
      SocialInformationType socialInformationType, String userId,
      String classification, Period period) {
    try {
      final SocialInfoContext ctx = new SocialInfoContext(userId, period)
          .withClassification(classification);
      return switchInterface.getSocialInformationsList(socialInformationType, ctx);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
    }
    return Collections.emptyList();
  }

  /**
   * get the List of social Information of my according the type of social information and the
   * UserId
   * @return: List<SocialInformation>
   * @param socialInformationType
   * @param myId
   * @param myContactIds
   * @param period
   */

  public List<SocialInformation> getSocialInformationsListOfMyContact(
      SocialInformationType socialInformationType, String myId,
      List<String> myContactIds, Period period) {
    try {
      final SocialInfoContext ctx = new SocialInfoContext(myId, period)
        .withContactIds(myContactIds);
      return switchInterface.getSocialInformationsListOfMyContacts(socialInformationType, ctx);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
    }
    return Collections.emptyList();
  }

}