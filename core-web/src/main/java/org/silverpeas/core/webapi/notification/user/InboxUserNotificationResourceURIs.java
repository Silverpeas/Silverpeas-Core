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
package org.silverpeas.core.webapi.notification.user;


import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILMessage;
import org.silverpeas.core.web.SilverpeasWebResource;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static java.lang.String.valueOf;

/**
 * Base URIs from which the REST-based ressources representing message container entities are
 * defined.
 * @author silveryocha
 */
@Bean
public class InboxUserNotificationResourceURIs {

  public static final String BASE_URI = "usernotifications/inbox";

  public URI ofNotification(final SILVERMAILMessage message) {
    return getNotificationUriBuilder(message).build();
  }

  public URI ofNotificationToMarkAsRead(final SILVERMAILMessage message) {
    return getNotificationUriBuilder(message).path("read").build();
  }

  private UriBuilder getNotificationUriBuilder(final SILVERMAILMessage message) {
    return SilverpeasWebResource.getBasePathBuilder().path(BASE_URI).path(valueOf(message.getId()));
  }
}
