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
package org.silverpeas.core.pdc.pdc.service;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.subscription.service.PdcSubscriptionService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the alerting of the subscribers on the PdC when a contribution is classified on a
 * position or when one of its positions is modified. The applications that notify themselves their
 * subscribers (those on the PdC included) about a contribution ask for no alert when they classify
 * it, so that the subscribers on the PdC aren't notified twice.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class PdcSubscribersAlertOnClassificationTest {

  private static final String COMPONENT_ID = "kmelia42";
  private static final int SILVER_CONTENT_ID = 1024;
  private static final int NO_POSITION = -1;

  private final PdcClassifyManager pdcClassifyManager = mock(PdcClassifyManager.class);
  private final PdcSubscriptionService pdcSubscriptionService = mock(PdcSubscriptionService.class);
  private GlobalPdcManager pdcManager;

  private final ClassifyPosition position =
      new ClassifyPosition(List.of(new ClassifyValue(3, "/12/45/")));

  @BeforeEach
  void theContributionIsNotYetClassifiedOnThePosition() throws Exception {
    // the axis of the PdC used by the application are got from the data source: the PdC manager
    // is spied to do without them
    pdcManager = spy(new GlobalPdcManager());
    FieldUtils.writeField(pdcManager, "pdcClassifyManager", pdcClassifyManager, true);
    FieldUtils.writeField(pdcManager, "pdcSubscriptionService", pdcSubscriptionService, true);
    when(pdcClassifyManager.isPositionAlreadyExists(SILVER_CONTENT_ID, position)).thenReturn(
        NO_POSITION);
    doReturn(new ArrayList<>()).when(pdcManager)
        .getUsedAxisToClassify(COMPONENT_ID, SILVER_CONTENT_ID);
  }

  @Test
  void theSubscribersAreAlertedByDefaultAboutANewPositionOfAContribution() throws Exception {
    pdcManager.addPosition(SILVER_CONTENT_ID, position, COMPONENT_ID);

    verify(pdcSubscriptionService).notifyClassification(position.getValues(), COMPONENT_ID,
        SILVER_CONTENT_ID);
  }

  @Test
  void theSubscribersAreAlertedOnDemandAboutANewPositionOfAContribution() throws Exception {
    pdcManager.addPosition(SILVER_CONTENT_ID, position, COMPONENT_ID, true);

    verify(pdcSubscriptionService).notifyClassification(position.getValues(), COMPONENT_ID,
        SILVER_CONTENT_ID);
  }

  @Test
  void theSubscribersAreNotAlertedAboutANewPositionWhenNoAlertIsAsked() throws Exception {
    pdcManager.addPosition(SILVER_CONTENT_ID, position, COMPONENT_ID, false);

    verify(pdcClassifyManager).addPosition(SILVER_CONTENT_ID, position, COMPONENT_ID);
    verify(pdcSubscriptionService, never()).notifyClassification(anyList(), anyString(), anyInt());
  }

  @Test
  void theSubscribersAreNotAlertedAboutAPositionTheContributionHasAlready() throws Exception {
    when(pdcClassifyManager.isPositionAlreadyExists(SILVER_CONTENT_ID, position)).thenReturn(7);

    pdcManager.addPosition(SILVER_CONTENT_ID, position, COMPONENT_ID, true);

    verify(pdcClassifyManager, never()).addPosition(anyInt(), any(), anyString());
    verify(pdcSubscriptionService, never()).notifyClassification(anyList(), anyString(), anyInt());
  }

  @Test
  void theSubscribersAreAlertedByDefaultAboutAModifiedPositionOfAContribution() throws Exception {
    pdcManager.updatePosition(position, COMPONENT_ID, SILVER_CONTENT_ID);

    verify(pdcSubscriptionService).notifyClassification(position.getValues(), COMPONENT_ID,
        SILVER_CONTENT_ID);
  }

  @Test
  void theSubscribersAreNotAlertedAboutAModifiedPositionWhenNoAlertIsAsked() throws Exception {
    pdcManager.updatePosition(position, COMPONENT_ID, SILVER_CONTENT_ID, false);

    verify(pdcClassifyManager).updatePosition(position);
    verify(pdcSubscriptionService, never()).notifyClassification(anyList(), anyString(), anyInt());
  }

  private static ClassifyPosition any() {
    return org.mockito.ArgumentMatchers.any(ClassifyPosition.class);
  }
}
