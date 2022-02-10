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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.subscription.bean;

import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResourceType;

import java.util.Collection;
import java.util.List;

/**
 * This interface defines some services for which it exists one default implementations and
 * potentially one per {@link SubscriptionResourceType} instances.
 * @author silveryocha
 */
public interface SubscriptionBeanService {

  /**
   * Gets the label of a list of the given {@link SubscriptionResourceType} instance.
   * @param type a type of subscription.
   * @param language the aimed language.
   * @return the label
   */
  String getSubscriptionTypeListLabel(final SubscriptionResourceType type, final String language);

  /**
   * Gets the list of subscription of a user.
   * @param subscriptions the subscriptions to convert.
   * @param language the aimed language.
   * @return a list of {@link AbstractSubscriptionBean}.
   */
  List<AbstractSubscriptionBean> toSubscriptionBean(final Collection<Subscription> subscriptions,
      final String language);
}
