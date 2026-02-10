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

package org.silverpeas.web.jobstartpage.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.kernel.test.UnitTest;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.web.jobstartpage.DisplaySorted;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.synchronizedSortedSet;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;
import static org.silverpeas.core.util.JSONCodec.encodeObject;
import static org.silverpeas.web.jobstartpage.DisplaySorted.TYPE_SPACE;
import static org.silverpeas.web.jobstartpage.DisplaySorted.TYPE_SUBSPACE;

@UnitTest
class TreeSetCacheTest {

  private final ExecutorService executor = Executors.newFixedThreadPool(3);
  private TreeSetCache cache;

  @BeforeEach
  void initCache() {
    cache = new TreeSetCache();
    cache.init(10, 2);
  }

  @Test
  void updateCacheWhileBrowsingItsContent() throws InterruptedException, ExecutionException {
    var future = executor.submit(() -> {
      System.out.println("Encode the tree...");
      long start = System.nanoTime();
      var json = encodeTree(cache.getCache());
      long end = System.nanoTime();
      long duration = (end - start) / 1_000_000;
      System.out.printf("Tree encoded in %dms\n", duration);
      return json;
    });

    executor.submit(() -> {
      awaitUntil(200, MILLISECONDS);
      System.out.println("Modify the tree!");
      cache.init(2, 2);
    });

    executor.shutdown();
    boolean done = executor.awaitTermination(5, SECONDS);
    assertThat(done, is(true));

    String json = future.get();
    assertThat(json, not(emptyOrNullString()));
    assertThat(json, hasLength(83262));

    System.out.println("DONE");
  }

  private static String encodeTree(SortedSet<DisplaySorted> theSet) {
    synchronized (theSet) {
      return encodeObject(o -> {
        o.putJSONArray("spaces", a -> {
          theSet.stream()
              .filter(DisplaySorted::isVisible)
              .map(s -> Pair.of(s, true))
              .forEach(
                  p ->
                      a.addJSONObject(so -> encodeSpace(p, so)));
          return a;
        });
        return o;
      });
    }
  }

  private static JSONCodec.JSONObject encodeSpace(final Pair<DisplaySorted, Boolean> space,
      final JSONCodec.JSONObject jsonObject) {
    return jsonObject
        .put("id", space.getFirst().getId())
        .put("type", TRUE.equals(space.getSecond()) ? TYPE_SPACE : TYPE_SUBSPACE)
        .put("label", space.getFirst().getName());
  }

  static class TreeSetCache {

    private final SortedSet<DisplaySorted> sortedData = synchronizedSortedSet(new TreeSet<>());

    public void init(int count, int deep) {
      if (!sortedData.isEmpty()) {
        clear();
      }
      recursiveSetFilling(count, deep);
    }

    public SortedSet<DisplaySorted> getCache() {
      return sortedData;
    }

    public void clear() {
      sortedData.clear();
    }

    private void recursiveSetFilling(int count, int deep) {
      for (int i = 0; i < count; i++) {
        DisplaySorted sorted = new DisplaySorted();
        sorted.setId(UUID.randomUUID().toString());
        sorted.setName("Space-" + deep + " " + i);
        sorted.setType(deep != 0 ? DisplaySorted.TYPE_SUBSPACE : DisplaySorted.TYPE_SPACE);
        sorted.setDeep(deep);
        sorted.setOrderNum(i);
        sorted.setVisible(true);
        sortedData.add(sorted);
        if (deep > 0) {
          recursiveSetFilling(count, deep - 1);
        }
      }
    }
  }
}
  