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

package org.silverpeas.core.web.calendar.subscription.bean;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProviderByInstance;
import org.silverpeas.core.web.subscription.bean.AbstractSubscriptionBean;

/**
 * @author silveryocha
 */
public class CalendarSubscriptionBean extends AbstractSubscriptionBean {

  private final Calendar calendar;

  protected CalendarSubscriptionBean(final Subscription subscription, final Calendar calendar,
      final SilverpeasComponentInstance component, final String language) {
    super(subscription, component, language);
    this.calendar = calendar;
  }

  @Override
  public String getPath() {
    final String path = super.getPath();
    return calendar.isMain() ? path : path + " > " + calendar.getTitle();
  }

  @Override
  public String getLink() {
    return ComponentInstanceRoutingMapProviderByInstance.get()
        .getByInstanceId(calendar.getComponentInstanceId()).relativeToSilverpeas().getHomePage()
        .toString();
  }
}
