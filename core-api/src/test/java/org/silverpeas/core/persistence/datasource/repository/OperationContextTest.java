/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.persistence.datasource.repository;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.persistence.datasource.OperationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.persistence.datasource.OperationContext.State.EXPORT;
import static org.silverpeas.core.persistence.datasource.OperationContext.State.IMPORT;

/**
 * @author Yohann Chastagnier
 */
@BenchmarkMethodChart
@BenchmarkOptions(benchmarkRounds = 1000000, warmupRounds = 10000)
public class OperationContextTest {

  @Rule
  public TestRule benchmarkRun = new BenchmarkRule();

  @Before
  public void clearCacheData() {
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
    assertThat(OperationContext.statesOf(IMPORT), is(false));
    assertThat(OperationContext.statesOf(EXPORT), is(false));
  }

  @Test
  public void addStateShouldWork() {
    assertThat(OperationContext.statesOf(IMPORT), is(false));
    OperationContext.addStates(IMPORT);
    assertThat(OperationContext.statesOf(IMPORT), is(true));
    OperationContext.addStates(IMPORT);
    assertThat(OperationContext.statesOf(IMPORT), is(true));
  }

  @Test
  public void removeStateShouldWork() {
    OperationContext.addStates(IMPORT);
    OperationContext.addStates(IMPORT);
    assertThat(OperationContext.statesOf(IMPORT), is(true));
    OperationContext.removeStates(IMPORT);
    assertThat(OperationContext.statesOf(IMPORT), is(false));
    OperationContext.addStates(IMPORT, EXPORT);
    assertThat(OperationContext.statesOf(IMPORT), is(true));
    OperationContext.removeStates(IMPORT);
    assertThat(OperationContext.statesOf(IMPORT), is(false));
  }

  @Test
  public void handleSeveralStatesAtSameCallShouldWork() {
    assertThat(OperationContext.statesOf(IMPORT), is(false));
    assertThat(OperationContext.statesOf(EXPORT), is(false));
    OperationContext.addStates(IMPORT, EXPORT);
    assertThat(OperationContext.statesOf(IMPORT), is(true));
    assertThat(OperationContext.statesOf(EXPORT), is(true));
    OperationContext.removeStates(IMPORT, EXPORT);
    assertThat(OperationContext.statesOf(IMPORT), is(false));
    assertThat(OperationContext.statesOf(EXPORT), is(false));
  }
}