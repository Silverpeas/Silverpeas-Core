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
package org.silverpeas.core.security.authorization;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * This interface extends access controller for a Publication resource.
 * @author Yohann Chastagnier
 */
public interface PublicationAccessControl extends AccessController<PublicationPK> {

  static PublicationAccessControl get() {
    return ServiceProvider.getSingleton(PublicationAccessControl.class);
  }

  /**
   * Using this method avoid to use perform database request in order to retrieve publication data.
   */
  default Stream<PublicationDetail> filterAuthorizedByUser(final String userId,
      final Collection<PublicationDetail> pubs) {
    return filterAuthorizedByUser(userId, pubs, AccessControlContext.init());
  }

  /**
   * Using this method avoid to use perform database request in order to retrieve publication data.
   */
  Stream<PublicationDetail> filterAuthorizedByUser(final String userId,
      final Collection<PublicationDetail> pubs, final AccessControlContext context);

  /**
   * Using this method avoid to use perform database request in order to retrieve publication data.
   */
  boolean isUserAuthorized(final String userId, final PublicationDetail pubDetail);

  /**
   * Using this method avoid to use perform database request in order to retrieve publication data.
   */
  boolean isUserAuthorized(final String userId, final PublicationDetail pubDetail,
      final AccessControlContext context);
}
