/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.java.util.stream;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * @author Yohann Chastagnier
 */
@BenchmarkMethodChart
@BenchmarkOptions(benchmarkRounds = 1000, warmupRounds = 1000)
public class ForEachTest {

  @Rule
  public TestRule benchmarkRun = new BenchmarkRule();

  private List<String> simpleList = new ArrayList<>();

  @Before
  public void setup() {
    simpleList.clear();
  }

  @Test
  public void testForEachFromList() {
    int nbElementOfSource = 10000;

    List<String> sourceList = new ArrayList<>();
    StringBuilder expected = new StringBuilder("Start_");
    for (int i = 0; i < nbElementOfSource; i++) {
      sourceList.add("index_" + i);
      expected.append("index_").append(i);
    }

    StringBuilder test = new StringBuilder("Start_");
    sourceList.forEach(test::append);

    assertThat(test.toString(), is(expected.toString()));
  }

  @Test
  public void testForEachFromListCallingInternalClassMethod() {
    int nbElementOfSource = 10000;

    List<String> sourceList = new ArrayList<>();
    for (int i = 0; i < nbElementOfSource; i++) {
      sourceList.add("index_" + i);
    }

    sourceList.forEach(this::addToSimpleList);

    assertThat(simpleList, contains(sourceList.toArray(new String[sourceList.size()])));
  }

  private void addToSimpleList(String element) {
    simpleList.add(element);
  }
}
