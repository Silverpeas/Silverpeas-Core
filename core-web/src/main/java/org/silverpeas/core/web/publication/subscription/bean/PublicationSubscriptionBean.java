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

package org.silverpeas.core.web.publication.subscription.bean;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationPath;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.web.subscription.bean.AbstractSubscriptionBean;

/**
 * @author silveryocha
 */
public class PublicationSubscriptionBean extends AbstractSubscriptionBean {

  private final PublicationPath path;

  protected PublicationSubscriptionBean(final Subscription subscription, final PublicationPath path,
      final SilverpeasComponentInstance component, final String language) {
    super(subscription, component, language);
    this.path = path;
  }

  @Override
  public String getPath() {
    return path.format(getLanguage());
  }

  @Override
  public String getLink() {
    return path.getContribution().getPermalink();
  }

  @Override
  protected boolean isUserCanAccess(final String userId, final SubscriptionResource resource) {
    final Location location = path.getLocation();
    return !location.isUndefined() && NodeAccessControl.get().isUserAuthorized(userId, location);
  }

  @Override
  protected boolean isGroupCanAccess(final String groupId, final SubscriptionResource resource) {
    final Location location = path.getLocation();
    return !location.isUndefined() && NodeAccessControl.get().isGroupAuthorized(groupId, location);
  }
}
