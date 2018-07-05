/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core.workflow.engine.notification;

import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.workflow.api.user.Replacement;

import java.util.Collection;
import java.util.Collections;

/**
 * A builder of notifications about a replacement between two users in a given workflow instance,
 * in the point of view of the incumbent.
 * @author silveryocha
 */
public class ToSubstituteReplacementNotificationBuilder
    extends AbstractReplacementNotificationBuilder {

  /**
   * Default constructor
   * @param resource the resource which is the object of the notification.
   */
  ToSubstituteReplacementNotificationBuilder(final Replacement resource, final NotifAction action) {
    super(resource, action);
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return Collections.singletonList(getResource().getSubstitute().getUserId());
  }

  @Override
  protected void performTemplateData(final String language, final Replacement resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    template.setAttribute("toSubstitute", true);
  }
}
  