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

package org.silverpeas.java.util;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit test to benchmark the performance of regexp to fetch a substring.
 * @author mmoquillon
 */
@BenchmarkMethodChart
@BenchmarkOptions(benchmarkRounds = 1000000, warmupRounds = 10000)
public class RegExp2Test {

  @Rule
  public TestRule benchmarkRun = new BenchmarkRule();

  private static final String NAME = "silverpeasWebCoreThorLogging.properties";
  private static final String EXPECTED_NAME = "silverpeasWebCoreThor";
  private static final String SUFFIX = "Logging.properties";
  private static final Pattern patternName =
      Pattern.compile("(\\w+)Logging.properties$", Pattern.CASE_INSENSITIVE);

  @Test
  public void benchFetchNamespaceByStringSplit() {
    String p = NAME;
    if (p.endsWith(SUFFIX)) {
      String[] path = p.split(SUFFIX);
      p = path[0];
    }
    assertThat(p, is(EXPECTED_NAME));
  }

  @Test
  public void benchFetchNamespaceByRegExp() {
    String p = NAME;
    Matcher matcher = patternName.matcher(p);
    if (matcher.matches()) {
      p = matcher.group(1);
    }
    assertThat(p, is(EXPECTED_NAME));
  }

  @Test
  public void benchFetchNamespaceBySubstring() {
    String p = NAME;
    if (p.endsWith(SUFFIX)) {
      p = p.substring(0, p.indexOf(SUFFIX));
    }
    assertThat(p, is(EXPECTED_NAME));
  }
  
}
