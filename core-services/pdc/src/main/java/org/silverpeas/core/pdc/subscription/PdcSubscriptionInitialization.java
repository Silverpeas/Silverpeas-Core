/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.pdc.subscription;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.core.subscription.SubscriptionFactory;

import static org.silverpeas.core.pdc.subscription.model.PdcSubscriptionConstants.PDC;

/**
 * Registers the position criteria on the PdC as a type of resources that can be aimed by a
 * subscription. Once registered, the subscriptions on the PdC are taken in charge by the
 * subscription API of Silverpeas, whatever the way they have been created (by the subscriber
 * himself or forced by a manager of the PdC).
 * @author mmoquillon
 */
@Service
public class PdcSubscriptionInitialization implements Initialization {

  @Override
  public void init() {
    SubscriptionFactory.get()
        .register(PDC, (r, s, i) -> PdcSubscriptionPositionCriteria.from(r),
            (s, r, c) -> new PdcSubscription(s, (PdcSubscriptionPositionCriteria) r, c));
  }
}
