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
package org.silverpeas.core.workflow.engine.instance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.workflow.api.ErrorManager;
import org.silverpeas.core.workflow.api.ProcessInstanceManager;
import org.silverpeas.core.workflow.api.ProcessModelManager;
import org.silverpeas.core.workflow.api.TaskManager;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.WorkflowEngine;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.model.TimeOutAction;
import org.silverpeas.core.workflow.engine.WorkflowHub;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestManagedMocks;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.util.Reflections;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the resolution of the timeout action to perform on a process instance. The
 * resolution must tell explicitly why no action can be performed when the process model is
 * inconsistent with the active states of the instance, instead of failing silently.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
@TestManagedMocks({ProcessInstanceManager.class, UserManager.class, WorkflowEngine.class,
    TaskManager.class, ErrorManager.class})
class ProcessInstanceTimeOutActionTest {

  private static final String WORKFLOW_ID = "workflow1";
  private static final String STATE_NAME = "waitingForValidation";
  private static final Date NOW = new Date();
  private static final Date ONE_HOUR_AGO = new Date(NOW.getTime() - TimeUnit.HOURS.toMillis(1));
  private static final Date IN_ONE_HOUR = new Date(NOW.getTime() + TimeUnit.HOURS.toMillis(1));

  @TestManagedMock
  private ProcessModelManager modelManager;

  private ProcessModel model;
  private ProcessInstanceImpl instance;

  @BeforeEach
  void prepareProcessModelAndInstance() throws WorkflowException {
    model = mock(ProcessModel.class);
    when(modelManager.getProcessModel(WORKFLOW_ID)).thenReturn(model);
    instance = new ProcessInstanceImpl();
    instance.setInstanceId("42");
    instance.setModelId(WORKFLOW_ID);
  }

  /**
   * The WorkflowHub keeps the first resolved managers in a static singleton. It is reset so that
   * the next test gets its own mocks.
   */
  @AfterEach
  void clearWorkflowHub() {
    Reflections.setStaticField(WorkflowHub.class, "instance", null);
  }

  @Test
  void noActionWhenNoActiveState() throws WorkflowException {
    assertThat(instance.getTimeOutAction(NOW), is(Optional.empty()));
  }

  @Test
  void noActionWhenNoActiveStateInTimeout() throws WorkflowException {
    instance.addActiveState(activeState(STATE_NAME, IN_ONE_HOUR, 0));

    assertThat(instance.getTimeOutAction(NOW), is(Optional.empty()));
  }

  @Test
  void noActionWhenTheActiveStateHasNoTimeoutDate() throws WorkflowException {
    instance.addActiveState(activeState(STATE_NAME, null, 0));

    assertThat(instance.getTimeOutAction(NOW), is(Optional.empty()));
  }

  @Test
  void theActionOfTheNextTimeoutOfTheActiveStateInTimeout() throws WorkflowException {
    instance.addActiveState(activeState(STATE_NAME, ONE_HOUR_AGO, 1));
    State state = aStateInModel(STATE_NAME);
    Action firstAction = anAction("remind");
    Action secondAction = anAction("escalate");
    timeOutActionsOf(state, aTimeOutAction(1, firstAction), aTimeOutAction(2, secondAction));

    Optional<ActionAndState> result = instance.getTimeOutAction(NOW);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getState(), is(state));
    assertThat(result.get().getAction(), is(secondAction));
  }

  @Test
  void failureWhenTheActiveStateIsNoMoreInTheModel() {
    instance.addActiveState(activeState(STATE_NAME, ONE_HOUR_AGO, 0));
    when(model.getState(STATE_NAME)).thenReturn(null);

    WorkflowException e =
        assertThrows(WorkflowException.class, () -> instance.getTimeOutAction(NOW));
    assertThat(e.getMessage(), containsString(STATE_NAME));
  }

  @Test
  void failureWhenNoTimeoutIsDefinedForTheExpectedOrder() {
    instance.addActiveState(activeState(STATE_NAME, ONE_HOUR_AGO, 1));
    State state = aStateInModel(STATE_NAME);
    timeOutActionsOf(state, aTimeOutAction(1, anAction("remind")));

    WorkflowException e =
        assertThrows(WorkflowException.class, () -> instance.getTimeOutAction(NOW));
    assertThat(e.getMessage(), containsString(STATE_NAME));
    assertThat(e.getMessage(), containsString("2"));
  }

  @Test
  void failureWhenTheActionOfTheTimeoutIsNoMoreInTheModel() {
    instance.addActiveState(activeState(STATE_NAME, ONE_HOUR_AGO, 0));
    State state = aStateInModel(STATE_NAME);
    timeOutActionsOf(state, aTimeOutAction(1, null));

    WorkflowException e =
        assertThrows(WorkflowException.class, () -> instance.getTimeOutAction(NOW));
    assertThat(e.getMessage(), containsString(STATE_NAME));
  }

  @Test
  void failureWhenTheModelCannotBeLoaded() throws WorkflowException {
    instance.addActiveState(activeState(STATE_NAME, ONE_HOUR_AGO, 0));
    when(modelManager.getProcessModel(WORKFLOW_ID))
        .thenThrow(new WorkflowException("test", "model file is corrupted"));

    WorkflowException e =
        assertThrows(WorkflowException.class, () -> instance.getTimeOutAction(NOW));
    assertThat(e.getMessage(), containsString("model file is corrupted"));
  }

  @Test
  void theActionOfAnotherActiveStateWhenOneIsUnresolvable() throws WorkflowException {
    instance.addActiveState(activeState("removedState", ONE_HOUR_AGO, 0));
    when(model.getState("removedState")).thenReturn(null);
    instance.addActiveState(activeState(STATE_NAME, ONE_HOUR_AGO, 0));
    State state = aStateInModel(STATE_NAME);
    Action action = anAction("remind");
    timeOutActionsOf(state, aTimeOutAction(1, action));

    Optional<ActionAndState> result = instance.getTimeOutAction(NOW);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getState(), is(state));
    assertThat(result.get().getAction(), is(action));
  }

  private static ActiveState activeState(String name, Date timeoutDate, int timeoutStatus) {
    ActiveState activeState = new ActiveState(name);
    activeState.setTimeoutDate(timeoutDate);
    activeState.setTimeoutStatus(timeoutStatus);
    return activeState;
  }

  private State aStateInModel(String name) {
    State state = mock(State.class);
    when(state.getName()).thenReturn(name);
    when(model.getState(name)).thenReturn(state);
    return state;
  }

  /**
   * The timeout actions must be fully built before stubbing the state, otherwise Mockito
   * complains about an unfinished stubbing.
   */
  private static void timeOutActionsOf(State state, TimeOutAction... timeOutActions) {
    when(state.getTimeOutActions()).thenReturn(timeOutActions);
  }

  private static Action anAction(String name) {
    Action action = mock(Action.class);
    when(action.getName()).thenReturn(name);
    return action;
  }

  private static TimeOutAction aTimeOutAction(int order, Action action) {
    TimeOutAction timeOutAction = mock(TimeOutAction.class);
    when(timeOutAction.getOrder()).thenReturn(order);
    when(timeOutAction.getAction()).thenReturn(action);
    return timeOutAction;
  }
}
