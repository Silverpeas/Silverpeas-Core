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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.workflow.engine.model;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.workflow.api.model.*;
import org.silverpeas.kernel.test.TestContext;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author ehugonnet
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ProcessModelManagerImplTest {

  @TestedBean
  private ProcessModelManagerImpl instance;

  /**
   * Test of loadProcessModel method, of class ProcessModelManagerImpl.
   */
  @Test
  void loadProcessModel() throws Exception {
    System.out.println("loadProcessModel");
    String processFileName = "DemandeCongesSimple.xml";

    ProcessModel result = instance.loadProcessModel(processFileName);
    assertNotNull(result);
    Role[] roles = result.getRoles();
    assertNotNull(roles);
    assertEquals(3, roles.length);
    String currentRole = "Employe";
    Role role = result.getRole("Employe");
    assertNotNull(role);
    assertEquals("Demandeur", role.getLabel(currentRole, "fr"));
    assertEquals("Requester", role.getLabel(currentRole, "en"));
    role = result.getRole("Responsable");
    assertNotNull(role);
    assertEquals("Responsable", role.getLabel(currentRole, "fr"));
    assertEquals("Responsable", role.getLabel(currentRole, "en"));

    currentRole = "Responsable";
    Presentation presentation = result.getPresentation();
    String title = presentation.getTitle(currentRole, null);
    assertEquals("Demande de ${action.Creation.actor}", title);

    Column[] columns = presentation.getColumns(currentRole);
    assertEquals(2, columns.length);

    State state = result.getState("AttenteValidation");
    assertNotNull(state);
    Action[] actions = state.getAllowedActions();
    assertEquals(2, actions.length);
    Action allowedAction = actions[0];
    assertNotNull(allowedAction);
    assertEquals("Accepter", allowedAction.getName());
    allowedAction = actions[1];
    assertEquals("Refuser", allowedAction.getName());

    Action action = result.getAction("Accepter");
    List<Consequence> consequences = action.getConsequences().getConsequenceList();
    assertEquals(1, consequences.size());
    Consequence consequence = consequences.get(0);
    State[] targetStates = consequence.getTargetStates();
    assertEquals(1, targetStates.length);
    State targetState = targetStates[0];
    assertEquals("Acceptee", targetState.getName());
  }

  /**
   * Test of saveProcessModel method, of class ProcessModelManagerImpl.
   */
  @Test
  void saveProcessModel() throws Exception {
    System.out.println("saveProcessModel");
    String processFileName = "DemandeCongesSimple.xml";
    ProcessModel process = instance.loadProcessModel(processFileName);
    String resultFileName = "DemandeCongesSimpleSerial.xml";
    instance.saveProcessModel(process, resultFileName);
    ProcessModel actualProcess = instance.loadProcessModel(resultFileName);
    assertThat(actualProcess.getName(), is(process.getName()));
    assertThat(actualProcess.getModelId(), is(process.getModelId()));
  }

  /**
   * Test of getProcessModelDir method, of class ProcessModelManagerImpl.
   */
  @Test
  void getProcessModelDir() {
    System.out.println("getProcessModelDir");
    String expResult =
        TestContext.getInstance().getPathOfTestResources().toString() + File.separator;
    String result = instance.getProcessModelDir();
    assertEquals(expResult, result);
  }
}