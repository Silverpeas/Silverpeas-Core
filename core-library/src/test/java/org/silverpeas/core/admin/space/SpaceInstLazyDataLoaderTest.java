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

package org.silverpeas.core.admin.space;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.reflect.FieldUtils.readField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class SpaceInstLazyDataLoaderTest {

  private long lastLogTime = -1;
  private SpaceInstLazyDataLoader4Test test;

  @BeforeEach
  void setup() {
    test = new SpaceInstLazyDataLoader4Test();
    assertDataNotLoaded(test);
  }

  @Test
  void testSafeRead() {
    final String result = test.safeRead((p, s, c) -> "readDone");
    assertThat(result, is("readDone"));
    assertDataLoadedOneTime(test);
    IntStream.rangeClosed(1, 10).forEach(i -> {
      final String r = test.safeRead((p, s, c) -> "readDone" + i);
      assertThat(r, is("readDone" + i));
    });
    assertDataLoadedOneTime(test);
  }

  @Test
  void testSafeWrite() {
    final Mutable<Boolean> performed = Mutable.of(false);
    test.safeWrite((p, s, c) -> performed.set(true));
    assertDataLoadedOneTime(test);
    assertThat(performed.orElse(false), is(true));
    IntStream.rangeClosed(1, 10).forEach(i -> {
      performed.set(false);
      assertThat(performed.orElse(false), is(false));
      test.safeWrite((p, s, c) -> performed.set(true));
      assertThat(performed.orElse(false), is(true));
    });
    assertDataLoadedOneTime(test);
  }

  @Test
  void testManualWrite() {
    final Mutable<Boolean> performed = Mutable.of(false);
    test.manualWrite((p, s, c) -> {
      performed.set(true);
      test.setData();
    });
    assertDataWrittenManually(test, 1);
    assertThat(performed.orElse(false), is(true));
    IntStream.rangeClosed(1, 10).forEach(i -> {
      performed.set(false);
      assertThat(performed.orElse(false), is(false));
      test.safeWrite((p, s, c) -> {
        performed.set(true);
        test.setData();
      });
      assertThat(performed.orElse(false), is(true));
    });
    assertDataWrittenManually(test, 11);
  }

  @Test
  void testReset() {
    testSafeWrite();
    assertDataLoadedOneTime(test);
    test.reset();
    assertDataHasBeenResetAfterOneLoading(test);
    final Mutable<Boolean> performed = Mutable.of(false);
    test.safeWrite((p, s, c) -> performed.set(true));
    assertThat(performed.orElse(false), is(true));
    test.reset();
    assertDataHasBeenResetAfterTwoLoadings(test);
  }

  @Test
  void testConcurrency() throws Exception {
    final int nbThreadsPerType = 1000;
    final CountDownLatch latch = new CountDownLatch(1);
    final List<Thread> threads = new ArrayList<>(4 * nbThreadsPerType);
    final AtomicInteger safeReads = new AtomicInteger(0);
    final AtomicInteger safeWrites = new AtomicInteger(0);
    final AtomicInteger manualWritesIncludingReset = new AtomicInteger(0);
    final AtomicInteger resets = new AtomicInteger(0);
    IntStream.range(0, nbThreadsPerType).forEach(i -> {
      // Profile 1: safeRead
      threads.add(new Thread(() -> {
        try {
          latch.await();
          test.safeRead((p, s, c) -> {
            final int count = safeReads.incrementAndGet();
            log(String.format("safeRead n°%s", count));
            return count;
          });
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
      // Profile 2: safeWrite
      threads.add(new Thread(() -> {
        try {
          latch.await();
          test.safeWrite((p, s, c) ->
              log(String.format("safeWrite n°%s", safeWrites.incrementAndGet())));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
      // Profile 3: manualWrite (including a reset)
      threads.add(new Thread(() -> {
        try {
          latch.await();
          test.manualWrite((p, s, c) -> {
            test.reset();
            log(String.format("manualWrite n°%s", manualWritesIncludingReset.incrementAndGet()));
          });
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
      // Profile 4: reset
      threads.add(new Thread(() -> {
        try {
          latch.await();
          test.reset();
          log(String.format("reset n°%s", resets.incrementAndGet()));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
    });

    log("STARTING THREADS...");
    Collections.shuffle(threads);
    for (Thread thread : threads) {
      thread.start();
    }
    log(threads.size() + " THREADS STARTED");
    log("WAITING 1s...");
    awaitUntil(1, SECONDS);
    latch.countDown();

    log("WAITING ENDING OF THREADS...");
    for (Thread thread : threads) {
      thread.join(60000);
    }
    log(threads.size() + " THREADS STOPPED");
    assertThat(test.getNbLoad(), greaterThan(1));
    assertThat(safeReads.get(), is(nbThreadsPerType));
    assertThat(safeWrites.get(), is(nbThreadsPerType));
    assertThat(manualWritesIncludingReset.get(), is(nbThreadsPerType));
    assertThat(resets.get(), is(nbThreadsPerType));
  }

  private static void assertDataNotLoaded(SpaceInstLazyDataLoader4Test data) {
    assertThat(data.isLoaded(), is(false));
    assertThat(data.getNbLoad(), is(0));
    assertThat(data.getProfiles(), hasSize(0));
    assertThat(data.getSubSpaces(), hasSize(0));
    assertThat(data.getComponents(), hasSize(0));
  }

  private static void assertDataLoadedOneTime(SpaceInstLazyDataLoader4Test data) {
    assertThat(data.isLoaded(), is(true));
    assertThat(data.getNbLoad(), is(1));
    assertThat(data.getProfiles(), hasSize(1));
    assertThat(data.getSubSpaces(), hasSize(1));
    assertThat(data.getComponents(), hasSize(2));
  }

  private static void assertDataHasBeenResetAfterOneLoading(SpaceInstLazyDataLoader4Test data) {
    assertThat(data.isLoaded(), is(false));
    assertThat(data.getNbLoad(), is(1));
    assertThat(data.getProfiles(), hasSize(0));
    assertThat(data.getSubSpaces(), hasSize(0));
    assertThat(data.getComponents(), hasSize(0));
  }

  private static void assertDataHasBeenResetAfterTwoLoadings(SpaceInstLazyDataLoader4Test data) {
    assertThat(data.isLoaded(), is(false));
    assertThat(data.getNbLoad(), is(2));
    assertThat(data.getProfiles(), hasSize(0));
    assertThat(data.getSubSpaces(), hasSize(0));
    assertThat(data.getComponents(), hasSize(0));
  }

  private static void assertDataWrittenManually(SpaceInstLazyDataLoader4Test data,
      final int nbWrites) {
    assertThat(data.isLoaded(), is(true));
    assertThat(data.getNbLoad(), is(0));
    assertThat(data.getProfiles(), hasSize(nbWrites));
    assertThat(data.getSubSpaces(), hasSize(nbWrites));
    assertThat(data.getComponents(), hasSize(2 * nbWrites));
  }

  static class SpaceInstLazyDataLoader4Test extends SpaceInstLazyDataLoader {

    private int nbLoad;

    SpaceInstLazyDataLoader4Test() {
      super(null);
    }

    public boolean isLoaded() {
      return extract("loaded");
    }

    public int getNbLoad() {
      return nbLoad;
    }

    public List<SpaceProfileInst> getProfiles() {
      return extract("spaceProfiles");
    }

    public List<SpaceInst> getSubSpaces() {
      return extract("subSpaces");
    }

    public List<ComponentInst> getComponents() {
      return extract("components");
    }

    @SuppressWarnings("unchecked")
    private <T> T extract(final String fieldName) {
      try {
        return (T) readField(this, fieldName, true);
      } catch (IllegalAccessException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }

    @Override
    protected void load() {
      nbLoad++;
      setData();
    }

    private void setData() {
      final SpaceProfileInst profile = new SpaceProfileInst();
      profile.setId("2601");
      profile.setSpaceFatherId("26");
      getProfiles().add(profile);
      final SpaceInst subSpace = new SpaceInst();
      subSpace.setLocalId(38);
      getSubSpaces().add(subSpace);
      final ComponentInst component1 = new ComponentInst();
      component1.setLocalId(1);
      getComponents().add(component1);
      final ComponentInst component2 = new ComponentInst();
      component2.setLocalId(2);
      getComponents().add(component2);
    }
  }

  /*
  Tool methods
   */

  private void log(String message) {
    long currentTime = System.currentTimeMillis();
    if (lastLogTime < 0) {
      lastLogTime = currentTime;
    }
    System.out.println(
        StringUtil.leftPad(String.valueOf(currentTime - lastLogTime), 6, " ") + " ms -> " +
            message);
  }
}