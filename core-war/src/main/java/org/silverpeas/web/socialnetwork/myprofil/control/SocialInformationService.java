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

import jakarta.inject.Inject;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.socialnetwork.provider.SocialInformationProviderSwitcher;
import org.silverpeas.core.socialnetwork.provider.SocialInformationProviderSwitcher.SocialInfoContext;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.Collections;
import java.util.List;

/**
 * @author Bensalem Nabil;
 */
@Service
public class SocialInformationService {

  @Inject
  private SocialInformationProviderSwitcher switchInterface;

  /**
   * Gets the List of social information according their type and that were published by the
   * specified user.
   *
   * @param socialInformationType the type of social information to get.
   * @param userId the unique identifier of the user having published the social information.
   * @param classification classification of the social information
   * @param period the period in time the information was published.
   * @return a list of the user's social information
   */
  public <T extends SocialInformation> List<T> getSocialInformationsList(
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
   * Gets the List of social information according their type and that were published by the
   * given contacts of the specified user.
   *
   * @param socialInformationType the type of social information to get.
   * @param userId the unique identifier of the user related by the social information.
   * @param contactIds a list with the unique identifiers of users
   * @param period the period in time the information was published.
   * @return a list of the user's social information
   */
  public <T extends SocialInformation> List<T> getSocialInformationsListOfMyContact(
      SocialInformationType socialInformationType, String userId,
      List<String> contactIds, Period period) {
    try {
      final SocialInfoContext ctx = new SocialInfoContext(userId, period)
          .withContactIds(contactIds);
      return switchInterface.getSocialInformationsListOfMyContacts(socialInformationType, ctx);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
    }
    return Collections.emptyList();
  }

}