/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.subscribe;

import com.silverpeas.util.Default;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.node.model.NodePK;
import java.util.Collection;
import javax.inject.Named;

/**
 *
 * @author ehugonnet
 */

@Named("subscriptionService")
@Default
public class MockableSubscriptionService implements SubscriptionService {
  private SubscriptionService realService;
  
  public void setImplementation(SubscriptionService service) {
    this.realService = service;
  }

  @Override
  public void subscribe(Subscription subscription) {
    realService.subscribe(subscription);
  }

  @Override
  public void unsubscribe(Subscription subscription) {
    realService.unsubscribe(subscription);
  }

  @Override
  public void unsubscribe(String userId) {
    realService.unsubscribe(userId);
  }

  @Override
  public void unsubscribeByPath(NodePK node, String path) {
    realService.unsubscribeByPath(node, path);
  }

  @Override
  public Collection<? extends Subscription> getUserSubscriptions(String userId) {
    return realService.getUserSubscriptions(userId);
  }

  @Override
  public Collection<? extends Subscription> getUserSubscriptionsByComponent(String userId,
          String componentName) {
    return realService.getUserSubscriptionsByComponent(userId, componentName);
  }

  @Override
  public Collection<String> getSubscribers(WAPrimaryKey pk) {
    return realService.getSubscribers(pk);
  }

  @Override
  public boolean isSubscribedToComponent(String user, String componentName) {
    return realService.isSubscribedToComponent(user, componentName);
  }
  
}
