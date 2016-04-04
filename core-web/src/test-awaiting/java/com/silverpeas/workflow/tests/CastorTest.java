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

package com.silverpeas.workflow.tests;

import com.silverpeas.workflow.api.model.Role;
import junit.framework.TestCase;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

import com.silverpeas.workflow.engine.model.ProcessModelImpl;

public class CastorTest extends TestCase {

  static public final String MAPPING_FILE_NAME = "com/silverpeas/workflow/tests/mapping.xml";
  static public final String MODEL_FILE_NAME = "com/silverpeas/workflow/tests/DemandeConges.xml";

  public CastorTest(String name) {
    super(name);
  }

  /**
   * Test de charger le model de demande de conges
   * @throws MappingException
   * @throws Exception
   */
  public void testMarshall() throws Exception {

    Mapping mapping = new Mapping();
    mapping.loadMapping(new InputSource(CastorTest.class.getClassLoader().getResourceAsStream(MAPPING_FILE_NAME)));

    // instantiate a UnMarshaller
    Unmarshaller unmar = new Unmarshaller(mapping);
    unmar.setValidation(false);

    // Unmarshall the process model
    ProcessModelImpl process = (ProcessModelImpl) unmar.unmarshal(new InputSource(CastorTest.class.getClassLoader().
        getResourceAsStream(MODEL_FILE_NAME)));
    assertNotNull(process);
    assertNotNull("We should have roles", process.getRoles());
    assertEquals("We should have 4 roles", 4, process.getRoles().length);
    Role employe = process.getRole("Employe");
    assertNotNull("We should have an employe role", employe);
    assertEquals("We should have a label", "Demandeur", employe.getLabel(null, null));
    Role responsable = process.getRole("Responsable");
    assertNotNull("We should have an responsable role", responsable);
    assertEquals("We should have a label", "Responsable", responsable.getLabel(null, null));
    Role secretaire = process.getRole("Secretaire");
    assertNotNull("We should have an secretaire role", secretaire);
    assertEquals("We should have a label", "Secrétaire", secretaire.getLabel(null, null));
    Role supervisor = process.getRole("supervisor");
    assertNotNull("We should have an supervisor role", supervisor);
    assertEquals("We should have a label", "Superviseur", supervisor.getLabel(null, null));
    System.out.println("Done");
  }
}
