/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.socialnetwork.provider;

import org.silverpeas.core.socialnetwork.model.SocialInformation;

import java.util.Date;
import java.util.List;

/**
 * A provider of social data. Any provider of information that can be shared among the users and
 * whose type is supported by the social network, as defined in the
 * {@link org.silverpeas.core.socialnetwork.model.SocialInformationType} enumeration, should
 * implement this interface.
 * It should have only one implementation of social information provider for one type of such a
 * social information.
 * @author mmoquillon
 */
public interface SocialInformationProvider {

  List<SocialInformation> getSocialInformationList(String userId, Date begin, Date end);

  List<SocialInformation> getSocialInformationListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end);
}
