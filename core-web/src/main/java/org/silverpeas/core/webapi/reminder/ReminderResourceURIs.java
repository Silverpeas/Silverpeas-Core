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

package org.silverpeas.core.webapi.reminder;


import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.SilverpeasWebResource;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Base URIs from which the REST-based resources representing reminder are defined.
 * @author silveryocha
 */
@Bean
public class ReminderResourceURIs {

  static final String REMINDER_BASE_URI = "reminder";

  public static ReminderResourceURIs get() {
    return ServiceProvider.getService(ReminderResourceURIs.class);
  }

  /**
   * Centralizes the build of a reminder URI.
   * @param reminder the aimed reminder.
   * @return the URI of specified reminder.
   */
  public URI ofReminder(final Reminder reminder) {
    return getReminderUriBuilder(reminder).build();
  }

  private UriBuilder getReminderUriBuilder(final Reminder reminder) {
    final ContributionIdentifier cId = reminder.getContributionId();
    return UriBuilder.fromPath(URLUtil.getApplicationURL()).path(SilverpeasWebResource.BASE_PATH)
        .path(REMINDER_BASE_URI)
        .path(cId.getComponentInstanceId())
        .path(cId.getType())
        .path(cId.getLocalId())
        .path(reminder.getId());
  }
}
