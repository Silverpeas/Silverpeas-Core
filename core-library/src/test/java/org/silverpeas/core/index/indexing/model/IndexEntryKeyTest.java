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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.index.indexing.model;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.silverpeas.core.test.unit.UnitTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author silveryocha
 */
@UnitTest
@BenchmarkMethodChart
@BenchmarkOptions(benchmarkRounds = 100000, warmupRounds = 100000)
public class IndexEntryKeyTest {

  private static final String INDEX_KEY_AS_STRING = "instanceId|resourceType|resourceId|";
  private static final String EMPTY_INDEX_KEY_AS_STRING = "|||";

  @Rule
  public TestRule benchmarkRun = new BenchmarkRule();

  @Test
  public void createAndToString() {
    IndexEntryKey key = IndexEntryKey.create(INDEX_KEY_AS_STRING);
    assertThat(key.getComponentId(), is("instanceId"));
    assertThat(key.getObjectType(), is("resourceType"));
    assertThat(key.getObjectId(), is("resourceId"));
    assertThat(key.toString(), is(INDEX_KEY_AS_STRING));
    key = IndexEntryKey.create(EMPTY_INDEX_KEY_AS_STRING);
    assertThat(key.getComponentId(), is(emptyString()));
    assertThat(key.getObjectType(), is(emptyString()));
    assertThat(key.getObjectId(), is(emptyString()));
    assertThat(key.toString(), is(EMPTY_INDEX_KEY_AS_STRING));
  }
}