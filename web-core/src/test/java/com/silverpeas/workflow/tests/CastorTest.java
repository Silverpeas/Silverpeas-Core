package com.silverpeas.workflow.tests;

import com.silverpeas.workflow.api.model.ContextualDesignation;
import com.silverpeas.workflow.api.model.ContextualDesignations;
import com.silverpeas.workflow.api.model.Role;
import java.util.Iterator;
import junit.framework.TestCase;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

import com.silverpeas.workflow.engine.model.ProcessModelImpl;
import com.silverpeas.workflow.engine.model.RoleImpl;

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
