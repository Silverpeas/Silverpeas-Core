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
package org.silverpeas.core.pdc.subscription.service;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.pdc.classification.Value;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.kernel.test.UnitTest;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests of the business rules of the PdC that are specific to the subscriptions, and that
 * don't require any access neither to the data source nor to the subscription API.
 * @author mmoquillon
 */
@UnitTest
class DefaultPdcSubscriptionServiceTest {

  private static final int AXIS = 3;
  private static final int ANOTHER_AXIS = 8;

  private final DefaultPdcSubscriptionService service = new DefaultPdcSubscriptionService();

  private static PdcSubscriptionPositionCriteria positions(final AxisValueCriterion... positions) {
    return new PdcSubscriptionPositionCriteria("1", "position criteria", List.of(positions));
  }

  @Test
  void aClassificationOnTheExactSamePositionMatches() {
    final PdcSubscriptionPositionCriteria resource = positions(new AxisValueCriterion(AXIS, "/12/45/"));

    assertThat(service.isCorrespondingSubscription(resource,
        List.of(new Value(AXIS, "/12/45/"))), is(true));
  }

  @Test
  void aClassificationOnAPositionBelowTheSubscribedOneMatches() {
    final PdcSubscriptionPositionCriteria resource = positions(new AxisValueCriterion(AXIS, "/12/"));

    assertThat(service.isCorrespondingSubscription(resource,
        List.of(new Value(AXIS, "/12/45/78/"))), is(true));
  }

  @Test
  void aClassificationOnAPositionAboveTheSubscribedOneDoesntMatch() {
    final PdcSubscriptionPositionCriteria resource = positions(new AxisValueCriterion(AXIS, "/12/45/"));

    assertThat(service.isCorrespondingSubscription(resource,
        List.of(new Value(AXIS, "/12/"))), is(false));
  }

  @Test
  void aClassificationOnAnotherAxisDoesntMatch() {
    final PdcSubscriptionPositionCriteria resource = positions(new AxisValueCriterion(AXIS, "/12/"));

    assertThat(service.isCorrespondingSubscription(resource,
        List.of(new Value(ANOTHER_AXIS, "/12/"))), is(false));
  }

  @Test
  void allThePositionsOfTheSetMustBeSatisfied() {
    final PdcSubscriptionPositionCriteria resource =
        positions(new AxisValueCriterion(AXIS, "/12/"), new AxisValueCriterion(ANOTHER_AXIS, "/33/"));

    assertThat(service.isCorrespondingSubscription(resource,
        List.of(new Value(AXIS, "/12/45/"), new Value(ANOTHER_AXIS, "/33/99/"))), is(true));
    assertThat(service.isCorrespondingSubscription(resource,
        List.of(new Value(AXIS, "/12/45/"), new Value(ANOTHER_AXIS, "/34/"))), is(false));
  }

  @Test
  void anEmptySetOfPositionsNeverMatches() {
    assertThat(service.isCorrespondingSubscription(positions(),
        List.of(new Value(AXIS, "/12/"))), is(false));
  }

  @Test
  void aSetOfPositionsNeverMatchesAnEmptyOrUndefinedClassification() {
    final PdcSubscriptionPositionCriteria resource = positions(new AxisValueCriterion(AXIS, "/12/"));

    assertThat(service.isCorrespondingSubscription(resource, List.of()), is(false));
    assertThat(service.isCorrespondingSubscription(resource, null), is(false));
  }

  @Test
  void aValueIsRemovedWhenItIsNoMoreInTheNewPathOfTheAxis() {
    assertThat(service.checkValuesRemove("/12/45/", List.of("/12/45/"), List.of("/12/")), is(true));
  }

  @Test
  void aValueIsKeptWhenItIsStillInTheNewPathOfTheAxis() {
    assertThat(service.checkValuesRemove("/12/45/", List.of("/12/45/"), List.of("/12/45/")),
        is(false));
  }

  @Test
  void onlyTheLastValueOfThePositionIsTakenIntoAccount() {
    // the deletion of the value 12, the position becoming /45/, doesn't remove a position ending
    // by 45
    assertThat(service.checkValuesRemove("/12/45/", List.of("/12/"), List.of("/")), is(false));
  }

  @Test
  void aPositionWithoutTrailingSeparatorIsHandled() {
    assertThat(service.checkValuesRemove("/12/45", List.of("/12/45/"), List.of("/12/")), is(true));
  }

  @Test
  void aValueIsFoundWhateverItsDepthInThePath() {
    assertThat(service.checkValueInPath("/3/", List.of("/2/3/4/5/")), is(true));
    assertThat(service.checkValueInPath("/5/", List.of("/2/3/4/5/")), is(true));
    assertThat(service.checkValueInPath("/8/", List.of("/2/3/4/5/")), is(false));
  }

  @Test
  void anUndefinedPathMeansTheRootOfTheAxis() {
    assertThat(service.checkValueInPath("/0/", Collections.singletonList(null)), is(true));
    assertThat(service.checkValueInPath("/3/", Collections.singletonList(null)), is(false));
  }
}
