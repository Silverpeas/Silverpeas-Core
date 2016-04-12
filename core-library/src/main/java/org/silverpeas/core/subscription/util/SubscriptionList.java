/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.subscription.util;

import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.admin.user.model.UserDetail;

import java.util.ArrayList;
import java.util.Iterator;

import static org.silverpeas.core.subscription.util.SubscriptionUtil.isSameVisibilityAsTheCurrentRequester;

/**
 * @author Yohann Chastagnier
 */
public class SubscriptionList extends ArrayList<Subscription> {
  private static final long serialVersionUID = -2314233862350010699L;

  /**
   * Removes from this list the subscription that the linked subscriber has not the same
   * domain visibility as the one of the given user.
   * @param user the user that represents the visibility to verify.
   */
  public SubscriptionList filterOnDomainVisibilityFrom(final UserDetail user) {
    if (user.isDomainRestricted()) {
      Iterator<Subscription> itOfSubscriptions = this.iterator();
      while (itOfSubscriptions.hasNext()) {
        Subscription subscription = itOfSubscriptions.next();
        if (!isSameVisibilityAsTheCurrentRequester(subscription.getSubscriber(), user)) {
          itOfSubscriptions.remove();
        }
      }
    }
    return this;
  }
}
