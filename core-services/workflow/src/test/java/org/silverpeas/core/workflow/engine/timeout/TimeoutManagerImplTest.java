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
package org.silverpeas.core.workflow.engine.timeout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.workflow.api.ProcessInstanceManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.TimeoutEvent;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.engine.instance.ActionAndState;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the periodic processing of the timeout actions of the workflow instances. The
 * processing of the timeout of one instance must never prevent the processing of the timeout of
 * the other instances, whatever the error.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class TimeoutManagerImplTest {

  private static final String REMOVED_WORKFLOW = "workflow3";

  @TestManagedMock
  private ProcessInstanceManager instanceManager;

  @TestManagedMock
  private Administration administration;

  private final List<TimeoutEvent> pushedEvents = new ArrayList<>();

  @TestedBean
  private final TimeoutManagerImpl timeoutManager = new TimeoutManagerImpl() {
    @Override
    protected void pushTimeoutRequest(final TimeoutEvent event) {
      pushedEvents.add(event);
    }
  };

  @BeforeEach
  void prepareWorkflowApplications() throws AdminException {
    ComponentInstLight workflowApp = mock(ComponentInstLight.class);
    when(workflowApp.isRemoved()).thenReturn(false);
    ComponentInstLight removedWorkflowApp = mock(ComponentInstLight.class);
    when(removedWorkflowApp.isRemoved()).thenReturn(true);
    when(administration.getComponentInstLight(any())).thenReturn(workflowApp);
    when(administration.getComponentInstLight(REMOVED_WORKFLOW)).thenReturn(removedWorkflowApp);
  }

  @Test
  void allTheTimeoutsOfTheInstancesAreProcessed() throws WorkflowException {
    ProcessInstance first = anInstanceWithTimeout("1", "workflow1");
    ProcessInstance second = anInstanceWithTimeout("2", "workflow1");
    ProcessInstance third = anInstanceWithTimeout("3", "workflow2");
    timeoutInstancesAre(first, second, third);

    timeoutManager.triggerFired(null);

    assertThat(pushedInstanceIds(), contains("1", "2", "3"));
    TimeoutEvent event = pushedEvents.get(0);
    assertThat(event.getProcessInstance(), is(first));
    assertThat(event.getResolvedState().getName(), is("state"));
    assertThat(event.getActionName(), is("action"));
  }

  @Test
  void aTimeoutActionUnresolvableInItsModelDoesNotBlockTheOthers() throws WorkflowException {
    ProcessInstance first = anInstanceWithTimeout("1", "workflow1");
    ProcessInstance broken = anInstance("2", "workflow1");
    when(broken.getTimeOutAction(any()))
        .thenThrow(new WorkflowException("test", "the action doesn't exist anymore"));
    ProcessInstance third = anInstanceWithTimeout("3", "workflow2");
    timeoutInstancesAre(first, broken, third);

    timeoutManager.triggerFired(null);

    assertThat(pushedInstanceIds(), contains("1", "3"));
  }

  @Test
  void anUnexpectedErrorWithAnInstanceDoesNotBlockTheOthers() throws WorkflowException {
    ProcessInstance first = anInstanceWithTimeout("1", "workflow1");
    ProcessInstance broken = anInstance("2", "workflow1");
    when(broken.getTimeOutAction(any())).thenThrow(new NullPointerException("boom"));
    ProcessInstance third = anInstanceWithTimeout("3", "workflow2");
    timeoutInstancesAre(first, broken, third);

    timeoutManager.triggerFired(null);

    assertThat(pushedInstanceIds(), contains("1", "3"));
  }

  @Test
  void anErrorWhileCheckingTheWorkflowApplicationDoesNotBlockTheOthers()
      throws WorkflowException, AdminException {
    ProcessInstance first = anInstanceWithTimeout("1", "workflow1");
    ProcessInstance broken = anInstanceWithTimeout("2", "unknownWorkflow");
    when(administration.getComponentInstLight("unknownWorkflow"))
        .thenThrow(new AdminException("no such application"));
    ProcessInstance third = anInstanceWithTimeout("3", "workflow2");
    timeoutInstancesAre(first, broken, third);

    timeoutManager.triggerFired(null);

    assertThat(pushedInstanceIds(), contains("1", "3"));
  }

  @Test
  void anInstanceWithoutTimeoutActionToProcessIsSkipped() throws WorkflowException {
    ProcessInstance first = anInstanceWithTimeout("1", "workflow1");
    ProcessInstance nothingToDo = anInstance("2", "workflow1");
    when(nothingToDo.getTimeOutAction(any())).thenReturn(Optional.empty());
    ProcessInstance third = anInstanceWithTimeout("3", "workflow2");
    timeoutInstancesAre(first, nothingToDo, third);

    timeoutManager.triggerFired(null);

    assertThat(pushedInstanceIds(), contains("1", "3"));
  }

  @Test
  void theInstancesOfARemovedWorkflowApplicationAreIgnored() throws WorkflowException {
    ProcessInstance first = anInstanceWithTimeout("1", "workflow1");
    ProcessInstance ofRemovedApp = anInstanceWithTimeout("2", REMOVED_WORKFLOW);
    ProcessInstance third = anInstanceWithTimeout("3", "workflow2");
    timeoutInstancesAre(first, ofRemovedApp, third);

    timeoutManager.triggerFired(null);

    assertThat(pushedInstanceIds(), contains("1", "3"));
  }

  @Test
  void nothingIsProcessedWhenTheInstancesInTimeoutCannotBeGot() throws WorkflowException {
    when(instanceManager.getTimeOutProcessInstances())
        .thenThrow(new WorkflowException("test", "database unavailable"));

    timeoutManager.triggerFired(null);

    assertThat(pushedEvents, is(empty()));
  }

  private void timeoutInstancesAre(ProcessInstance... instances) throws WorkflowException {
    when(instanceManager.getTimeOutProcessInstances())
        .thenReturn(SilverpeasList.wrap(List.of(instances)));
  }

  private List<String> pushedInstanceIds() {
    List<String> ids = new ArrayList<>();
    pushedEvents.forEach(e -> ids.add(e.getProcessInstance().getInstanceId()));
    return ids;
  }

  private static ProcessInstance anInstance(String id, String workflowAppId) {
    ProcessInstance instance = mock(ProcessInstance.class);
    when(instance.getInstanceId()).thenReturn(id);
    when(instance.getModelId()).thenReturn(workflowAppId);
    return instance;
  }

  private static ProcessInstance anInstanceWithTimeout(String id, String workflowAppId)
      throws WorkflowException {
    ProcessInstance instance = anInstance(id, workflowAppId);
    State state = mock(State.class);
    when(state.getName()).thenReturn("state");
    Action action = mock(Action.class);
    when(action.getName()).thenReturn("action");
    when(instance.getTimeOutAction(any(Date.class)))
        .thenReturn(Optional.of(new ActionAndState(action, state)));
    return instance;
  }
}
