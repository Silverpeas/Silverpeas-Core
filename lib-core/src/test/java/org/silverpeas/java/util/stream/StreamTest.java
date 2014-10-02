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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.apache.commons.lang.time.DurationFormatUtils.formatDurationHMS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
public class StreamTest {

  private Logger log = Logger.getLogger(StreamTest.class.getName());

  @Test
  public void testStreamCallingInternalClassMethod() {
    int nbElementOfSource = 10000;

    // Dummy performed in order to perform greatly the performance tests
    List<Pair<String, Integer>> dummyPairList = new ArrayList<>(nbElementOfSource);
    for (int i = 0; i < (nbElementOfSource * 10); i++) {
      dummyPairList.add(Pair.of("index_" + i, i));
    }
    List<Map<String, String>> dummyMapFromPairList =
        dummyPairList.stream().map(pair -> convertPairToMap(pair.getRight()))
            .collect(Collectors.toList());
    assertThat(dummyMapFromPairList, hasSize(nbElementOfSource * 10));

    // Test
    List<Pair<String, Integer>> pairList = new ArrayList<>(nbElementOfSource);
    for (int i = 0; i < nbElementOfSource; i++) {
      pairList.add(Pair.of("index_" + i, i));
    }

    long startStream = System.currentTimeMillis();

    List<Map<String, String>> mapFromPairList =
        pairList.stream().map(pair -> convertPairToMap(pair.getRight()))
            .collect(Collectors.toList());
    assertThat(mapFromPairList, hasSize(nbElementOfSource));

    long streamPerformance = System.currentTimeMillis() - startStream;

    // Verifying that data have been processed
    Iterator<Pair<String, Integer>> pairIterator = pairList.iterator();
    for (Map<String, String> mapToTest : mapFromPairList) {
      Pair<String, Integer> expected = pairIterator.next();
      assertThat(mapToTest.size(), is(1));
      assertThat(mapToTest, hasEntry(expected.getLeft(), expected.getRight() + "_value"));
    }

    List<Pair<String, Integer>> pairListForStatement = new ArrayList<>(nbElementOfSource);
    for (int i = 0; i < nbElementOfSource; i++) {
      pairListForStatement.add(Pair.of("index_" + i, i));
    }

    long startForStatement = System.currentTimeMillis();

    List<Map<String, String>> mapFromPairListForStatement = new ArrayList<>();
    //noinspection Convert2streamapi
    for (Pair<String, Integer> pair : pairListForStatement) {
      mapFromPairListForStatement.add(convertPairToMap(pair.getRight()));
    }
    assertThat(mapFromPairListForStatement, hasSize(nbElementOfSource));

    long forPerformance = System.currentTimeMillis() - startForStatement;

    // Verifying that data have been processed
    assertThat(mapFromPairListForStatement, contains(mapFromPairList.toArray()));

    // Verifying performances between stream api and for statement
    double ratio = (streamPerformance / Math.max(1, forPerformance));

    log.info("Stream performance :  : " + formatDurationHMS(streamPerformance));
    log.info("For statement performance : " + formatDurationHMS(forPerformance));
    log.info("Ratio (stream / for) : " + ratio);
    assertThat("Stream perform is less accurate than for statement ...", ratio,
        lessThanOrEqualTo(1D));
  }

  /**
   * Converts a {@link Pair} instance into a {@link Map} one.<br/>
   * (That is idiot but it is for testing...)
   * @param index a string index
   * @return a map with one couple initialized from given string index.
   */
  private Map<String, String> convertPairToMap(Integer index) {
    Map<String, String> result = new HashMap<>();
    result.put("index_" + index, index + "_value");
    return result;
  }

  @Test
  public void testMapKeysToArrayWithStreamApi() {
    int nbElementOfSource = 10000;

    Map<String, Integer> sourceMap = new HashMap<>();
    for (int i = 0; i < nbElementOfSource; i++) {
      sourceMap.put("index_" + i, i);
    }

    Integer[] keys = sourceMap.keySet().stream().map(sourceMap::get).toArray(Integer[]::new);
    assertThat(keys,
        arrayContainingInAnyOrder(sourceMap.values().toArray(new Integer[sourceMap.size()])));
  }
}
