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
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.Process;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.reflect.FieldUtils.readField;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class SpaceInstLazyDataLoaderTest {

  private long lastLogTime = -1;
  private SpaceInstLazyDataLoader4Test test;
  private final static Object LOAD_MUTEX = new Object();

  @TestManagedMock
  private Administration adminMock;

  @TestManagedMock
  private Transaction transaction;

  @BeforeEach
  void setup() throws Exception {
    test = new SpaceInstLazyDataLoader4Test(this);
    assertDataNotLoaded(test);
    when(adminMock.getSpaceInstById(anyString())).thenAnswer(i -> {
      final String id = i.getArgument(0);
      final SpaceInst subSpace = new SpaceInst();
      subSpace.setLocalId(Integer.parseInt(id.replace(SpaceInst.SPACE_KEY_PREFIX, "")));
      return subSpace;
    });
    when(adminMock.getComponentInst(anyString())).thenAnswer(i -> {
      final String id = i.getArgument(0);
      final ComponentInst componentInst = new ComponentInst();
      final String name = id.replaceAll("[0-9]", "");
      componentInst.setLocalId(Integer.parseInt(id.replace(name, "")));
      componentInst.setName(name);
      return componentInst;
    });
  }

  @Test
  void testSafeRead() {
    final String result = test.safeRead(d -> "readDone");
    assertThat(result, is("readDone"));
    assertDataLoadedOneTime(test);
    IntStream.rangeClosed(1, 10).forEach(i -> {
      final String r = test.safeRead(d -> "readDone" + i);
      assertThat(r, is("readDone" + i));
    });
    assertDataLoadedOneTime(test);
  }

  @Test
  void testSafeWrite() {
    final Mutable<Boolean> performed = Mutable.of(false);
    test.safeWrite(d -> performed.set(true));
    assertDataLoadedOneTime(test);
    assertThat(performed.orElse(false), is(true));
    IntStream.rangeClosed(1, 10).forEach(i -> {
      performed.set(false);
      assertThat(performed.orElse(false), is(false));
      test.safeWrite(d -> performed.set(true));
      assertThat(performed.orElse(false), is(true));
    });
    assertDataLoadedOneTime(test);
  }

  @Test
  void testManualWrite() {
    final Mutable<Boolean> performed = Mutable.of(false);
    test.manualWrite(d -> {
      performed.set(true);
      test.setData();
    });
    assertDataWrittenManually(test, 1);
    assertThat(performed.orElse(false), is(true));
    IntStream.rangeClosed(1, 10).forEach(i -> {
      performed.set(false);
      assertThat(performed.orElse(false), is(false));
      test.safeWrite(d -> {
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
    test.safeWrite(d -> performed.set(true));
    assertThat(performed.orElse(false), is(true));
    test.reset();
    assertDataHasBeenResetAfterTwoLoadings(test);
  }

  @Test
  void testReadingOnlyCopiedLists() {
    final List<ComponentInst> testComponents = test.safeRead(
        SpaceInstLazyDataLoader.SafeDataAccessor::getCopyOfComponents);
    assertThat(testComponents, not(sameInstance(test.getComponentIds())));
    assertThat(
        testComponents.stream().map(ComponentInst::getId).collect(Collectors.toList()),
        is(test.getComponentIds()));
    final List<SpaceInst> testSubSpaceIds = test.safeRead(
        SpaceInstLazyDataLoader.SafeDataAccessor::getCopyOfSubSpaces);
    assertThat(
        testSubSpaceIds.stream().map(SpaceInst::getId).collect(Collectors.toUnmodifiableList()),
        is(test.getSubSpaceIds()));
    final List<SpaceProfileInst> testProfiles = test.safeRead(
        SpaceInstLazyDataLoader.SafeDataAccessor::getCopyOfProfiles);
    assertThat(testProfiles, not(sameInstance(test.getProfiles())));
    assertThat(testProfiles, is(test.getProfiles()));
    assertDataLoadedOneTime(test);
  }

  @Test
  void testCopyUnloadedData() {
    OtherSpaceInstLazyDataLoader4Test otherTest = new OtherSpaceInstLazyDataLoader4Test(this);
    assertDataNotLoaded(test);
    assertDataNotLoaded(otherTest);
    test.copy(otherTest);
    assertBothAreEqual(test, otherTest);
    assertDataLoadedOneTime(otherTest);
  }

  @Test
  void testCopyUnloadedDataIntoLoadedOne() {
    test.safeRead(d -> "readDone");
    final OtherSpaceInstLazyDataLoader4Test otherTest = new OtherSpaceInstLazyDataLoader4Test(this);
    assertDataLoadedOneTime(test);
    assertDataNotLoaded(otherTest);
    test.copy(otherTest);
    assertBothAreEqual(test, otherTest);
    assertDataLoadedOneTime(otherTest);
  }

  @Test
  void testCopyLoadedDataIntoUnloadedOne() {
    final OtherSpaceInstLazyDataLoader4Test otherTest = new OtherSpaceInstLazyDataLoader4Test(this);
    otherTest.safeRead(d -> "readDone");
    assertDataNotLoaded(test);
    assertDataLoadedOneTime(otherTest);
    test.copy(otherTest);
    assertBothAreEqual(test, otherTest);
    assertDataLoadedOneTime(otherTest);
  }

  @Test
  void testCopyLoadedData() {
    // before copy, safe read data are identical to test instance lists
    final List<ComponentInst> testComponents = test.safeRead(
        SpaceInstLazyDataLoader.SafeDataAccessor::getCopyOfComponents);
    assertThat(
        testComponents.stream().map(ComponentInst::getId).collect(Collectors.toList()),
        is(test.getComponentIds()));
    final List<SpaceInst> testSubSpaceIds = test.safeRead(
        SpaceInstLazyDataLoader.SafeDataAccessor::getCopyOfSubSpaces);
    assertThat(
        testSubSpaceIds.stream().map(SpaceInst::getId).collect(Collectors.toList()),
        is(test.getSubSpaceIds()));
    final List<SpaceProfileInst> testProfiles = test.safeRead(
        SpaceInstLazyDataLoader.SafeDataAccessor::getCopyOfProfiles);
    assertThat(testProfiles, is(test.getProfiles()));
    assertDataLoadedOneTime(test);

    final OtherSpaceInstLazyDataLoader4Test otherTest = new OtherSpaceInstLazyDataLoader4Test(this);
    assertDataNotLoaded(otherTest);
    otherTest.safeRead(d -> "readDone");
    assertDataLoadedOneTime(otherTest);
    test.copy(otherTest);
    assertDataLoadedOneTime(test);
    assertDataLoadedOneTime(otherTest);

    // after copy, previous safe read data are not identical to test instance lists
    assertThat(testComponents, not(is(test.getComponentIds())));
    assertThat(
        testSubSpaceIds.stream().map(SpaceInst::getId).collect(Collectors.toList()),
        not(is(test.getSubSpaceIds())));
    assertThat(testProfiles, not(is(test.getProfiles())));
  }

  @Test
  void testCopyConcurrency() throws Exception {
    final int nbThreadsPerType = 1000;
    final CountDownLatch latch = new CountDownLatch(1);
    final List<Thread> threads = new ArrayList<>(4 * nbThreadsPerType);
    final OtherSpaceInstLazyDataLoader4Test otherTest = new OtherSpaceInstLazyDataLoader4Test(this);
    IntStream.range(0, nbThreadsPerType).forEach(i -> {
      // Profile 1: copy other test into test (both not loaded)
      threads.add(new Thread(() -> {
        try {
          latch.await();
          test.copy(otherTest);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
      // Profile 2: copy other test into test (test loaded)
      threads.add(new Thread(() -> {
        try {
          latch.await();
          test.safeRead(d -> "readDone");
          test.copy(otherTest);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
      // Profile 3: copy other test into test (other test loaded)
      threads.add(new Thread(() -> {
        try {
          latch.await();
          otherTest.safeRead(d -> "readDone");
          test.copy(otherTest);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
      // Profile 4: copy other test into test (both loaded)
      threads.add(new Thread(() -> {
        try {
          latch.await();
          test.safeRead(d -> "readDone");
          otherTest.safeRead(d -> "readDone");
          test.copy(otherTest);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
      // Profile 5: copy test into other test (both not loaded)
      threads.add(new Thread(() -> {
        try {
          latch.await();
          otherTest.copy(test);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
      // Profile 6: copy test into other test (test loaded)
      threads.add(new Thread(() -> {
        try {
          latch.await();
          test.safeRead(d -> "readDone");
          otherTest.copy(test);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
      // Profile 7: copy test into other test (other test loaded)
      threads.add(new Thread(() -> {
        try {
          latch.await();
          otherTest.safeRead(d -> "readDone");
          otherTest.copy(test);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
      // Profile 8: copy test into other test (both loaded)
      threads.add(new Thread(() -> {
        try {
          latch.await();
          test.safeRead(d -> "readDone");
          otherTest.safeRead(d -> "readDone");
          otherTest.copy(test);
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
    assertThat(test.getNbLoad(), lessThanOrEqualTo(1));
    assertThat(otherTest.getNbLoad(), lessThanOrEqualTo(1));
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
          final long tId = Thread.currentThread().getId();
          latch.await();
          test.safeRead(d -> {
            final int count = safeReads.incrementAndGet();
            log(String.format("safeRead n°%s (tId=%s)", count, tId));
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
          final long tId = Thread.currentThread().getId();
          latch.await();
          test.safeWrite(d ->
              log(String.format("safeWrite n°%s (tId=%s)", safeWrites.incrementAndGet(), tId)));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
      // Profile 3: manualWrite (including a reset)
      threads.add(new Thread(() -> {
        try {
          final long tId = Thread.currentThread().getId();
          latch.await();
          test.manualWrite(d -> {
            test.reset();
            log(String.format("manualWrite n°%s (tId=%s)", manualWritesIncludingReset.incrementAndGet(), tId));
          });
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }));
      // Profile 4: reset
      threads.add(new Thread(() -> {
        try {
          final long tId = Thread.currentThread().getId();
          latch.await();
          test.reset();
          log(String.format("reset n°%s (tId=%s)", resets.incrementAndGet(), tId));
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
    await().pollDelay(1, SECONDS).until(() -> true);
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
    assertThat(data.getSubSpaceIds(), hasSize(0));
    assertThat(data.getComponentIds(), hasSize(0));
  }

  private static void assertDataLoadedOneTime(SpaceInstLazyDataLoader4Test data) {
    assertThat(data.isLoaded(), is(true));
    assertThat(data.getNbLoad(), is(1));
    assertThat(data.getProfiles(), hasSize(1));
    assertThat(data.getSubSpaceIds(), hasSize(1));
    assertThat(data.getComponentIds(), hasSize(2));
  }

  private static void assertDataHasBeenResetAfterOneLoading(SpaceInstLazyDataLoader4Test data) {
    assertThat(data.isLoaded(), is(false));
    assertThat(data.getNbLoad(), is(1));
    assertThat(data.getProfiles(), hasSize(0));
    assertThat(data.getSubSpaceIds(), hasSize(0));
    assertThat(data.getComponentIds(), hasSize(0));
  }

  private static void assertDataHasBeenResetAfterTwoLoadings(SpaceInstLazyDataLoader4Test data) {
    assertThat(data.isLoaded(), is(false));
    assertThat(data.getNbLoad(), is(2));
    assertThat(data.getProfiles(), hasSize(0));
    assertThat(data.getSubSpaceIds(), hasSize(0));
    assertThat(data.getComponentIds(), hasSize(0));
  }

  private static void assertDataWrittenManually(SpaceInstLazyDataLoader4Test data,
      final int nbWrites) {
    assertThat(data.isLoaded(), is(true));
    assertThat(data.getNbLoad(), is(0));
    assertThat(data.getProfiles(), hasSize(nbWrites));
    assertThat(data.getSubSpaceIds(), hasSize(nbWrites));
    assertThat(data.getComponentIds(), hasSize(2 * nbWrites));
  }

  private static void assertBothAreEqual(final SpaceInstLazyDataLoader4Test test,
      final SpaceInstLazyDataLoader4Test otherTest) {
    assertThat(test.isLoaded(), is(otherTest.isLoaded()));
    assertThat(test.getComponentIds(), not(sameInstance(otherTest.getComponentIds())));
    assertThat(test.getComponentIds(), is(otherTest.getComponentIds()));
    assertThat(test.getSubSpaceIds(), not(sameInstance(otherTest.getSubSpaceIds())));
    assertThat(test.getSubSpaceIds(), is(otherTest.getSubSpaceIds()));
    assertThat(test.getProfiles(), not(sameInstance(otherTest.getProfiles())));
    assertThat(test.getProfiles().stream().map(SpaceProfileInst::getId).collect(toList()),
        is(otherTest.getProfiles().stream().map(SpaceProfileInst::getId).collect(toList())));
  }

  static class SpaceInstLazyDataLoader4Test extends SpaceInstLazyDataLoader {
    private static final long serialVersionUID = 3154262870399771057L;

    private final SpaceInstLazyDataLoaderTest testInstance;
    private int nbLoad;

    SpaceInstLazyDataLoader4Test(SpaceInstLazyDataLoaderTest testInstance) {
      super(null);
      this.testInstance = testInstance;
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

    public List<String> getSubSpaceIds() {
      return extract("subSpaceIds");
    }

    public List<String> getComponentIds() {
      return extract("componentIds");
    }

    @SuppressWarnings("unchecked")
    private <T> T extract(final String fieldName) {
      try {
        return (T) readField(this, fieldName, true);
      } catch (IllegalAccessException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void load() {
      synchronized (LOAD_MUTEX) {
        Mockito.reset(testInstance.transaction);
        final int nbLoadCall = isLoaded() ? 0 : 1;
        super.load();
        verify(testInstance.transaction, times(nbLoadCall)).perform(
            ArgumentMatchers.any(Process.class));
        if (nbLoadCall == 1) {
          nbLoad++;
          setData();
          testInstance.log(String.format("LOAD %s (tId=%s, nbLoad=%s)", getClass().getSimpleName(),
              Thread.currentThread().getId(), nbLoad));
        }
      }
    }

    void setData() {
      final SpaceProfileInst profile = new SpaceProfileInst();
      profile.setId("2601");
      profile.setSpaceFatherId("26");
      getProfiles().add(profile);
      getSubSpaceIds().add(SpaceInst.SPACE_KEY_PREFIX + "38");
      getComponentIds().add("blog1");
      getComponentIds().add("kmelia2");
    }
  }

  static class OtherSpaceInstLazyDataLoader4Test extends SpaceInstLazyDataLoader4Test {
    private static final long serialVersionUID = 3555179349924453546L;

    OtherSpaceInstLazyDataLoader4Test(SpaceInstLazyDataLoaderTest testInstance) {
      super(testInstance);
    }

    @Override
    void setData() {
      final SpaceProfileInst profile = new SpaceProfileInst();
      profile.setId("3801");
      profile.setSpaceFatherId("38");
      getProfiles().add(profile);
      getSubSpaceIds().add(SpaceInst.SPACE_KEY_PREFIX + "69");
      getComponentIds().add("kmelia3");
      getComponentIds().add("almanach4");
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