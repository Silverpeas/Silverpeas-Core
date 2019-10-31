/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.subscription;

import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.notification.system.ResourceEvent;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Abstract listener of deletion events to remove all subscriptions on the deleted resource.
 * It does really the unsubscription and delegates the getting of the subscription resource to the
 * concrete implementors.
 * @author mmoquillon
 */
public abstract class AbstractProfiledResourceSubscriptionListener<R extends Serializable,
    T extends ResourceEvent<R>> extends CDIResourceEventListener<T> {

  @Inject
  private SubscriptionService subscriptionService;

  @Override
  public void onDeletion(final T event) throws Exception {
    final R object = event.getTransition().getBefore();
    final SubscriptionResource resource = getSubscriptionResource(object);
    subscriptionService.unsubscribeByResource(resource);
  }

  protected abstract SubscriptionResource getSubscriptionResource(final R resource);

}
  