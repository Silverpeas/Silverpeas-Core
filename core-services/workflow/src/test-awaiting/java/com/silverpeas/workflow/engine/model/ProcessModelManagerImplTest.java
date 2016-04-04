/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.workflow.engine.model;

import java.io.File;
import com.silverpeas.workflow.api.model.Role;
import com.silverpeas.workflow.api.model.ProcessModel;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class ProcessModelManagerImplTest {

  public ProcessModelManagerImplTest() {
  }

  @org.junit.BeforeClass
  public static void setUpClass() throws Exception {
  }

  @org.junit.AfterClass
  public static void tearDownClass() throws Exception {
  }

  @org.junit.Before
  public void setUp() throws Exception {
  }

  @org.junit.After
  public void tearDown() throws Exception {
  }

  /**
   * Test of loadProcessModel method, of class ProcessModelManagerImpl.
   */
  @org.junit.Test
  public void testLoadProcessModel() throws Exception {
    System.out.println("loadProcessModel");
    String processFileName = "DemandeCongesSimple.xml";
    boolean absolutePath = false;
    ProcessModelManagerImpl instance = new ProcessModelManagerImpl();
    ProcessModel result = instance.loadProcessModel(processFileName, absolutePath);
    assertNotNull(result);
    Role[] roles = result.getRoles();
    assertNotNull(roles);
    assertEquals(3, roles.length);
    assertNotNull(result.getRole("Employe"));
    assertEquals("Demandeur", result.getRole("Employe").getLabel("Employe", "fr"));
    assertEquals("Demandeur", result.getRole("Employe").getLabel("Employe", "en"));
  }

  /**
   * Test of saveProcessModel method, of class ProcessModelManagerImpl.
   */
  @org.junit.Test
  public void testSaveProcessModel() throws Exception {
    System.out.println("saveProcessModel");
    String processFileName = "DemandeCongesSimple.xml";
    boolean absolutePath = false;
    ProcessModelManagerImpl instance = new ProcessModelManagerImpl();
    ProcessModel process = instance.loadProcessModel(processFileName, absolutePath);
    String resultFileName = "DemandeCongesSimpleSerial.xml";
    instance.saveProcessModel(process, resultFileName);
    FileUtils.contentEquals(new File(instance.getProcessPath(processFileName)), new File(instance.getProcessPath(resultFileName)));
  }

  /**
   * Test of getProcessModelDir method, of class ProcessModelManagerImpl.
   */
  @org.junit.Test
  public void testGetProcessModelDir() {
    System.out.println("getProcessModelDir");
    ProcessModelManagerImpl instance = new ProcessModelManagerImpl();
    String expResult = System.getProperty("basedir") + File.separatorChar + "target"
            + File.separatorChar + "test-classes" + File.separatorChar;
    String result = instance.getProcessModelDir();
    assertEquals(expResult, result);
  }
}
