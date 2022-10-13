/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.publication.subscription;

import org.silverpeas.core.calendar.subscription.CalendarSubscriptionResource;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.subscription.service.PKSubscriptionResource;

/**
 * @author silveryocha
 */
public class PublicationAliasSubscriptionResource extends PKSubscriptionResource {

  /**
   * A way to get an instance of a publication subscription resource.
   * @param pk a reference to a publication.
   * @return the corresponding {@link CalendarSubscriptionResource} instance.
   */
  public static PublicationAliasSubscriptionResource from(PublicationPK pk) {
    return new PublicationAliasSubscriptionResource(pk);
  }

  /**
   * A way to get an instance of a publication subscription resource.
   * @param publication a reference to a publication.
   * @return the corresponding {@link CalendarSubscriptionResource} instance.
   */
  public static PublicationAliasSubscriptionResource from(PublicationDetail publication) {
    return new PublicationAliasSubscriptionResource(publication.getPK());
  }

  /**
   * Default constructor
   * @param pk the {@link org.silverpeas.core.ResourceReference} to a publication.
   */
  protected PublicationAliasSubscriptionResource(final PublicationPK pk) {
    super(pk, PublicationSubscriptionConstants.PUBLICATION_ALIAS);
  }
}
