/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.pdc.subscription.service;

import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.pdc.classification.Value;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Utility class. Contains calls of PdcSubscription Ejb
 */
@Singleton
public class PdcSubscriptionManager {

  public static PdcSubscriptionManager getInstance() {
    return ServiceProvider.getService(PdcSubscriptionManager.class);
  }

  @Inject
  private PdcSubscriptionService pdcSubscriptionService;

  protected PdcSubscriptionManager() {

  }

  public PdcSubscription getPDCSubsriptionById(int id) {
    return pdcSubscriptionService.getPDCSubsriptionById(id);
  }

  public void createPDCSubsription(PdcSubscription subscription) {
    pdcSubscriptionService.createPDCSubscription(subscription);
  }

  public void updatePDCSubsription(PdcSubscription subscription) {
    pdcSubscriptionService.updatePDCSubscription(subscription);
  }

  public void checkSubscriptions(List<? extends Value> classifyValues, String componentId,
      int silverObjectid) {
    pdcSubscriptionService.checkSubscriptions(classifyValues, componentId, silverObjectid);
  }

  public void checkAxisOnDelete(int axisId, String axisName) {
    pdcSubscriptionService.checkAxisOnDelete(axisId, axisName);
  }

  public void checkValueOnDelete(int axiId, String axisName, List<String> oldPath,
      List<String> newPath, List<org.silverpeas.core.pdc.pdc.model.Value> pathInfo) {
    pdcSubscriptionService.checkValueOnDelete(axiId, axisName, oldPath, newPath, pathInfo);
  }
}
