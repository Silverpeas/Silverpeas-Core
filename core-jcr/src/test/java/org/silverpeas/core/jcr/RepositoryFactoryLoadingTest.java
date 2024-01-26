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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.jcr.impl.oak.OakRepositoryFactory;
import org.silverpeas.kernel.test.UnitTest;

import javax.jcr.RepositoryFactory;
import java.util.ServiceLoader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Test the {@link OakRepositoryFactory} is well loaded when using the
 * Java Service Provider API.
 * @author mmoquillon
 */
@UnitTest
class RepositoryFactoryLoadingTest {

  @Test
  @DisplayName("OakRepositoryFactory should be loaded when using the Java Service Provider API")
  void loadOakRepositoryFactory() {
    boolean found = ServiceLoader.load(RepositoryFactory.class).stream()
        .map(ServiceLoader.Provider::get)
        .anyMatch(f -> f instanceof OakRepositoryFactory);
    assertThat(found, is(true));
  }

  @Test
  @DisplayName("The Java Service Provider API should load at least 2 RepositoryFactory " +
      "instances: one for Oak itself, another one for Silverpeas")
  void loadAtLeastTwoRepositoryFactories() {
    long factoriesCount = ServiceLoader.load(RepositoryFactory.class).stream()
        .map(ServiceLoader.Provider::get)
        .count();
    assertThat(factoriesCount, greaterThanOrEqualTo(2L));
  }
}