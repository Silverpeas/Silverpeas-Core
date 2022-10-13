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

package org.silverpeas.core.process.annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.management.ProcessManagement;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.test.extention.TestManagedMock;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.silverpeas.core.node.model.NodePK.ROOT_NODE_ID;
import static org.silverpeas.core.process.annotation.SimulationActionProcessProcessor.SIMULATION_PROCESS_PERFORMED;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class SimulationActionProcessProcessorTest {

  private static final String TARGET_INSTANCE_ID = "kmelia26";
  private static final String OTHER_TARGET_INSTANCE_ID = "kmelia07";
  private final static NodeDetail TARGET = new NodeDetail(new NodePK("26", TARGET_INSTANCE_ID), "", "", 0, ROOT_NODE_ID);
  private final static NodeDetail OTHER_TARGET = new NodeDetail(new NodePK("7", OTHER_TARGET_INSTANCE_ID), "", "", 0, ROOT_NODE_ID);

  @TestManagedMock
  private ProcessManagement processManagement;

  @TestManagedBean
  private SimulationActionProcessProcessor processor;

  @BeforeEach
  public void setup() {
    CacheServiceProvider.clearAllThreadCaches();
  }

  @DisplayName("Process is executed without any simulation when no source and no target specified")
  @Test
  void processedDirectlyWhenNoSourceAndNoTarget() {
    final boolean result = processor
        .withContext(s -> {})
          .listElementsWith(SimulationElementLister4Test::new)
          .byAction(() -> ActionType.COPY)
        .toTargets(t -> {})
        .setLanguage(() -> null)
        .execute(this::isSimulationProcessPerformed);
    assertFalse(processor.isSimulationProcessPerforming());
    assertFalse(result);
  }

  @DisplayName("Process is executed without any simulation when no source an no target specified")
  @Test
  void processedDirectlyWhenNoSource() {
    final boolean result = processor
        .withContext(s -> { })
          .listElementsWith(SimulationElementLister4Test::new)
          .byAction(() -> ActionType.COPY)
        .toTargets(t -> t.getTargetPKs().add(TARGET.getNodePK()))
        .setLanguage(() -> null)
        .execute(this::isSimulationProcessPerformed);
    assertFalse(processor.isSimulationProcessPerforming());
    assertFalse(result);
  }

  @DisplayName("Process is executed with simulation when source PK specified")
  @Test
  void processedWitheSimulationWhenSourcePK() {
    final SimulationElementLister4Test test = new SimulationElementLister4Test();
    final boolean result = processor
        .withContext(s -> s.getSourcePKs().add(new ResourceReference("38", "kmelia38")))
          .listElementsWith(() -> test)
          .byAction(() -> ActionType.COPY)
        .toTargets(t -> t.getTargetPKs().add(TARGET.getNodePK()))
        .setLanguage(() -> null)
        .execute(() -> {
          assertFalse(processor.isSimulationProcessPerforming());
          return isSimulationProcessPerformed();
        });
    assertFalse(processor.isSimulationProcessPerforming());
    assertTrue(result);
    assertThat(test.getSourcesPKs(), hasSize(1));
    assertThat(test.getSources(), empty());
    final Captures captures = getSimulationProcessCaptures(1);
    assertThat(captures.elementCaptor.getValue().actionType, is(ActionType.COPY));
    assertThat(captures.elementCaptor.getValue().target, is(TARGET.getNodePK()));
    assertThat(captures.contextCaptor.getValue().getComponentInstanceId(), is(TARGET_INSTANCE_ID));
  }

  @DisplayName("Process is executed with simulation when simple source specified")
  @Test
  void processedWitheSimulationWhenSimpleSource() {
    final SimulationElementLister4Test test = new SimulationElementLister4Test();
    final boolean result = processor
        .withContext(s -> s.getSourceObjects().add(new ResourceReference("69", "kmelia69")))
          .listElementsWith(() -> test)
          .byAction(() -> ActionType.MOVE)
        .toTargets(t -> t.getTargetPKs().add(TARGET.getNodePK()))
        .setLanguage(() -> null)
        .execute(() -> {
          assertFalse(processor.isSimulationProcessPerforming());
          return isSimulationProcessPerformed();
        });
    assertFalse(processor.isSimulationProcessPerforming());
    assertTrue(result);
    assertThat(test.getSourcesPKs(), empty());
    assertThat(test.getSources(), hasSize(1));
    final Captures captures = getSimulationProcessCaptures(1);
    assertThat(captures.elementCaptor.getValue().actionType, is(ActionType.MOVE));
    assertThat(captures.elementCaptor.getValue().target, is(TARGET.getNodePK()));
    assertThat(captures.contextCaptor.getValue().getComponentInstanceId(), is(TARGET_INSTANCE_ID));
  }

  @DisplayName("Process is executed with simulation when source PKS and simple source are specified")
  @Test
  void processedWitheSimulationWhenSimplePKsAnsSimpleSources() {
    final SimulationElementLister4Test test = new SimulationElementLister4Test();
    final boolean result = processor
        .withContext(s -> {
            s.getSourcePKs().add(new ResourceReference("68", "kmelia68"));
            s.getSourcePKs().add(new ResourceReference("77", "kmelia77"));
            s.getSourcePKs().add(new ResourceReference("13", "kmelia13"));
            s.getSourceObjects().add(new ResourceReference("83", "kmelia83"));
            s.getSourceObjects().add(new ResourceReference("84", "kmelia84"));
          })
          .listElementsWith(() -> test)
          .byAction(() -> ActionType.MOVE)
        .toTargets(t -> t.getTargetPKs().add(TARGET.getNodePK()))
        .setLanguage(() -> null)
        .execute(() -> {
          assertFalse(processor.isSimulationProcessPerforming());
          return isSimulationProcessPerformed();
        });
    assertFalse(processor.isSimulationProcessPerforming());
    assertTrue(result);
    assertThat(test.getSourcesPKs(), hasSize(3));
    assertThat(test.getSources(), hasSize(2));
    final Captures captures = getSimulationProcessCaptures(1);
    assertThat(captures.elementCaptor.getValue().actionType, is(ActionType.MOVE));
    assertThat(captures.elementCaptor.getValue().target, is(TARGET.getNodePK()));
    assertThat(captures.contextCaptor.getValue().getComponentInstanceId(), is(TARGET_INSTANCE_ID));
  }

  @DisplayName("Process is executed with simulation when several targets specified")
  @Test
  void processedWitheSimulationWhenSeveralTargets() {
    final SimulationElementLister4Test test = new SimulationElementLister4Test();
    final boolean result = processor
        .withContext(s -> s.getSourcePKs().add(new ResourceReference("6", "kmelia06")))
          .listElementsWith(() -> test)
          .byAction(() -> ActionType.CREATE)
        .toTargets(t -> {
          t.getTargetPKs().add(TARGET.getNodePK());
          t.getTargetPKs().add(OTHER_TARGET.getNodePK());
        })
        .setLanguage(() -> null)
        .execute(() -> {
          assertFalse(processor.isSimulationProcessPerforming());
          return isSimulationProcessPerformed();
        });
    assertFalse(processor.isSimulationProcessPerforming());
    assertTrue(result);
    assertThat(test.getSourcesPKs(), hasSize(2));
    assertThat(test.getSources(), empty());
    final Captures captures = getSimulationProcessCaptures(2);
    assertThat(captures.elementCaptor.getAllValues(), hasSize(2));
    assertThat(captures.elementCaptor.getAllValues().get(0).actionType, is(ActionType.CREATE));
    assertThat(captures.elementCaptor.getAllValues().get(0).target, is(TARGET.getNodePK()));
    assertThat(captures.elementCaptor.getAllValues().get(1).actionType, is(ActionType.CREATE));
    assertThat(captures.elementCaptor.getAllValues().get(1).target, is(OTHER_TARGET.getNodePK()));
    assertThat(captures.contextCaptor.getAllValues(), hasSize(2));
    assertThat(captures.contextCaptor.getAllValues().get(0).getComponentInstanceId(), is(TARGET_INSTANCE_ID));
    assertThat(captures.contextCaptor.getAllValues().get(1).getComponentInstanceId(), is(OTHER_TARGET_INSTANCE_ID));
  }

  @DisplayName("Process is executed with simulation when several context specified and several targets")
  @Test
  void processedWitheSimulationWhenSeveralContextsAndSeveralTargets() {
    final SimulationElementLister4Test test = new SimulationElementLister4Test();
    final OtherSimulationElementLister4Test otherTest = new OtherSimulationElementLister4Test();
    final boolean result = processor
        .withContext(s -> s.getSourcePKs().add(new ResourceReference("4", "kmelia04")))
          .listElementsWith(() -> test)
          .byAction(() -> ActionType.COPY)
        .andWithContext(s -> s.getSourceObjects().add(new ResourceReference("5", "kmelia05")))
          .listElementsWith(() -> otherTest)
          .byAction(() -> ActionType.CREATE)
        .toTargets(t -> {
          t.getTargetPKs().add(TARGET.getNodePK());
          t.getTargetPKs().add(OTHER_TARGET.getNodePK());
        })
        .setLanguage(() -> null)
        .execute(() -> {
          assertFalse(processor.isSimulationProcessPerforming());
          return isSimulationProcessPerformed();
        });
    assertFalse(processor.isSimulationProcessPerforming());
    assertTrue(result);
    assertThat(test.getSourcesPKs(), hasSize(2));
    assertThat(test.getSources(), empty());
    assertThat(otherTest.getSourcesPKs(), empty());
    assertThat(otherTest.getSources(), hasSize(2));
    final Captures captures = getSimulationProcessCaptures(4);
    assertThat(captures.elementCaptor.getAllValues(), hasSize(4));
    assertThat(captures.elementCaptor.getAllValues().get(0).actionType, is(ActionType.COPY));
    assertThat(captures.elementCaptor.getAllValues().get(0).target, is(TARGET.getNodePK()));
    assertThat(captures.elementCaptor.getAllValues().get(1).actionType, is(ActionType.COPY));
    assertThat(captures.elementCaptor.getAllValues().get(1).target, is(OTHER_TARGET.getNodePK()));
    assertThat(captures.elementCaptor.getAllValues().get(2).actionType, is(ActionType.CREATE));
    assertThat(captures.elementCaptor.getAllValues().get(2).target, is(TARGET.getNodePK()));
    assertThat(captures.elementCaptor.getAllValues().get(3).actionType, is(ActionType.CREATE));
    assertThat(captures.elementCaptor.getAllValues().get(3).target, is(OTHER_TARGET.getNodePK()));
    assertThat(captures.contextCaptor.getAllValues(), hasSize(4));
    assertThat(captures.contextCaptor.getAllValues().get(0).getComponentInstanceId(), is(TARGET_INSTANCE_ID));
    assertThat(captures.contextCaptor.getAllValues().get(1).getComponentInstanceId(), is(OTHER_TARGET_INSTANCE_ID));
    assertThat(captures.contextCaptor.getAllValues().get(2).getComponentInstanceId(), is(TARGET_INSTANCE_ID));
    assertThat(captures.contextCaptor.getAllValues().get(3).getComponentInstanceId(), is(OTHER_TARGET_INSTANCE_ID));
  }

  private Captures getSimulationProcessCaptures(final int nbCall) {
    final ArgumentCaptor<SimulationElementConversionProcess> elementCaptor = forClass(SimulationElementConversionProcess.class);
    final ArgumentCaptor<ProcessExecutionContext> contextCaptor = forClass(ProcessExecutionContext.class);
    try {
      verify(processManagement, times(nbCall)).execute(elementCaptor.capture(), contextCaptor.capture());
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
    return new Captures(elementCaptor, contextCaptor);
  }

  private boolean isSimulationProcessPerformed() {
    return CacheServiceProvider.getRequestCacheService()
        .getCache()
        .get(SIMULATION_PROCESS_PERFORMED) != null;
  }

  static class Captures {
    private final ArgumentCaptor<SimulationElementConversionProcess> elementCaptor;
    private final ArgumentCaptor<ProcessExecutionContext> contextCaptor;

    Captures(final ArgumentCaptor<SimulationElementConversionProcess> elementCaptor,
        final ArgumentCaptor<ProcessExecutionContext> contextCaptor) {
      this.elementCaptor = elementCaptor;
      this.contextCaptor = contextCaptor;
    }
  }

  public static class SimulationElementLister4Test extends SimulationElementLister {
    private final List<ResourceReference> sourcesPKs = new ArrayList<>();
    private final List<Object> sources = new ArrayList<>();

    @Override
    public void listElements(final ResourceReference sourcePK, final String language) {
      this.sourcesPKs.add(sourcePK);
    }

    @Override
    public void listElements(final Object source, final String language, final ResourceReference targetPK) {
      this.sources.add(source);
    }

    List<ResourceReference> getSourcesPKs() {
      return sourcesPKs;
    }

    List<Object> getSources() {
      return sources;
    }
  }

  public static class OtherSimulationElementLister4Test extends SimulationElementLister4Test {
  }
}