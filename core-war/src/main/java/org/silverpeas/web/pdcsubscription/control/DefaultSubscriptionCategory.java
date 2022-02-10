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

package org.silverpeas.web.pdcsubscription.control;

import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.subscription.bean.SubscriptionBeanProvider;

import java.util.List;

/**
 * The default implementation is directly mapped to a {@link SubscriptionResourceType} instance.
 * @author silveryocha
 */
public class DefaultSubscriptionCategory extends SubscriptionCategory {

  private final SubscriptionResourceType type;

  public DefaultSubscriptionCategory(final PdcSubscriptionSessionController ctrl,
      final SubscriptionResourceType type) {
    super(ctrl);
    this.type = type;
  }

  @Override
  public String getId() {
    return type.getName();
  }

  @Override
  public int priority() {
    return type.priority();
  }

  @Override
  public String getLabel() {
    return SubscriptionBeanProvider.getSubscriptionTypeListLabel(type, getCtrl().getLanguage());
  }

  @Override
  public String getResourceTypeLabel() {
    return StringUtil.EMPTY;
  }

  @Override
  public List<SubscriptionResourceType> getHandledTypes() {
    return List.of(type);
  }
}
