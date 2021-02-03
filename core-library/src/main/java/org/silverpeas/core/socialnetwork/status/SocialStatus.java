/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.socialnetwork.status;

import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.provider.SocialStatusProvider;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@Provider
public class SocialStatus implements SocialStatusProvider {

  @Inject
  private StatusService statusService;

  private StatusService getStatusService() {
    return statusService;
  }

  protected SocialStatus() {

  }

  @Override
  public List<SocialInformation> getSocialInformationList(String userid, Date begin, Date end) {
    return getStatusService().getAllStatus(Integer.parseInt(userid), begin, end);
  }

  @Override
  public List<SocialInformation> getSocialInformationListOfMyContacts(final String myId,
      final List<String> myContactsIds, final Date begin, final Date end) {
    return getStatusService().getSocialInformationListOfMyContacts(myContactsIds, begin, end);
  }

}
