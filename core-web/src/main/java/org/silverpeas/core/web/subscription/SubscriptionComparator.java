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
package org.silverpeas.core.web.subscription;

import org.silverpeas.core.util.comparator.AbstractComplexComparator;
import org.silverpeas.core.web.subscription.bean.AbstractSubscriptionBean;

/**
 * User: Yohann Chastagnier
 * Date: 13/03/13
 */
public class SubscriptionComparator extends AbstractComplexComparator<AbstractSubscriptionBean> {
  private static final long serialVersionUID = -6495926353223079742L;

  @Override
  protected ValueBuffer getValuesToCompare(final AbstractSubscriptionBean subscription) {
    ValueBuffer valuesToCompare = new ValueBuffer();
    valuesToCompare.append(subscription.getSpace().getOrderNum());
    valuesToCompare.append(subscription.getSpace().getName());
    valuesToCompare.append(subscription.getComponent().getOrderPosition());
    valuesToCompare.append(subscription.getComponent().getName());
    valuesToCompare.append(subscription.getPath());
    return valuesToCompare;
  }
}