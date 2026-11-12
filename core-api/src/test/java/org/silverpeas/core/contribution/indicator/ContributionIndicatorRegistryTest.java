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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.indicator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.kernel.util.Pair;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the getting of the {@link NewContributionIndicator} matching a given contribution.
 * @author mmoquillon
 */
class ContributionIndicatorRegistryTest {

  private static final String PUBLICATION = "Publication";

  private final NewContributionIndicator kmeliaIndicator = new NewContributionIndicator() {
    @Override
    public Pair<String, String> relatedToComponentAndResourceType() {
      return Pair.of("kmelia", PUBLICATION);
    }

    @Override
    public boolean isNew(final Instant lastUpdateInstant) {
      return true;
    }
  };

  private ContributionIndicatorRegistry registry;

  @BeforeEach
  void setUpRegistry() {
    registry = new ContributionIndicatorRegistry();
    registry.addNewContributionIndicator(kmeliaIndicator);
  }

  @Test
  void theIndicatorRelatedToTheComponentAndTheTypeOfTheContributionIsFound() {
    ContributionIdentifier cId = ContributionIdentifier.from("kmelia12", "42", PUBLICATION);
    Optional<NewContributionIndicator> indicator = registry.getNewContributionIndicatorBy(cId);
    assertThat(indicator.orElseThrow(), is(kmeliaIndicator));
  }

  @Test
  void noIndicatorIsFoundForAnotherTypeOfContribution() {
    ContributionIdentifier cId = ContributionIdentifier.from("kmelia12", "42", "Node");
    Optional<NewContributionIndicator> indicator = registry.getNewContributionIndicatorBy(cId);
    assertThat(indicator.isEmpty(), is(true));
  }

  @Test
  void noIndicatorIsFoundForAnotherComponent() {
    ContributionIdentifier cId = ContributionIdentifier.from("almanach3", "42", PUBLICATION);
    Optional<NewContributionIndicator> indicator = registry.getNewContributionIndicatorBy(cId);
    assertThat(indicator.isEmpty(), is(true));
  }

  /**
   * Some indexed resources, like the spaces, aren't related to any component instance: their
   * component instance identifier is a pseudo identifier.
   */
  @Test
  void noIndicatorIsFoundForASpace() {
    ContributionIdentifier cId = ContributionIdentifier.from("Spaces", "WA3", "Space");
    Optional<NewContributionIndicator> indicator = registry.getNewContributionIndicatorBy(cId);
    assertThat(indicator.isEmpty(), is(true));
  }

  @Test
  void noIndicatorIsFoundForAnInvalidComponentInstanceIdentifier() {
    ContributionIdentifier cId = ContributionIdentifier.from("kmelia0", "42", PUBLICATION);
    Optional<NewContributionIndicator> indicator = registry.getNewContributionIndicatorBy(cId);
    assertThat(indicator.isEmpty(), is(true));
  }
}
