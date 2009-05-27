package com.silverpeas.workflow.tests;

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
		mapping.loadMapping( new InputSource(CastorTest.class.getClassLoader()
				.getSystemResourceAsStream(MAPPING_FILE_NAME)) );

		// instantiate a UnMarshaller
		Unmarshaller unmar = new Unmarshaller(mapping);
		unmar.setValidation(false);

		// Unmarshall the process model
		ProcessModelImpl process = (ProcessModelImpl) unmar
				.unmarshal(new InputSource(CastorTest.class.getClassLoader()
						.getSystemResourceAsStream(MODEL_FILE_NAME)));
		
		System.out.println("Done");
	}
}
