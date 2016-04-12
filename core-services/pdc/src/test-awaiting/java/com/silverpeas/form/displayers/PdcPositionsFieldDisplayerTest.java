/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.contribution.content.form.displayers;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.silverpeas.util.Charsets;

import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.fieldType.TextField;

import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import com.stratelia.silverpeas.pdc.control.PdcManager;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.Value;

import org.apache.commons.fileupload.FileItem;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Ludovic Bertin
 */
public class PdcPositionsFieldDisplayerTest {

  private static final String AXIS_ID = "2";
  private static final String LANGUAGE = "fr";
  private static final String OBJECT_ID = "23";
  private static final String COMPONENT_ID = "whitePages23";

  public PdcPositionsFieldDisplayerTest() {
  }

  /**
   * Test of getManagedTypes method, of class PdcPositionsFieldDisplayer.
   */
  @Test
  public void testGetManagedTypes() {
    PdcPositionsFieldDisplayer instance = new PdcPositionsFieldDisplayer();
    String[] result = instance.getManagedTypes();
    assertNotNull(result);
    assertThat(result, org.hamcrest.collection.IsArrayWithSize.arrayWithSize(1));
    assertThat(result, org.hamcrest.collection.IsArrayContaining.hasItemInArray(TextField.TYPE));
  }

  /**
   * Test of displayScripts method, of class PdcPositionsFieldDisplayer.
   */
  @Test
  public void testDisplayScripts() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    PrintWriter printer = new PrintWriter(new OutputStreamWriter(out, Charsets.UTF_8), true);
    PdcPositionsFieldDisplayer instance = new PdcPositionsFieldDisplayer();
    instance.displayScripts(printer, null, null);
    assertEquals(0, out.toByteArray().length);
  }

  /**
   * Test of display method, of class PdcPositionsFieldDisplayer.
   */
  @Test
  public void testDisplay() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    PrintWriter printer = new PrintWriter(new OutputStreamWriter(out, Charsets.UTF_8), true);

    HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put("axisId", AXIS_ID);

    FieldTemplate template = mock(FieldTemplate.class);
    when(template.getParameters(LANGUAGE)).thenReturn(parameters);

    PagesContext pagesContext = mockPageContext();
    PdcManager pdcManager = mockPdcBm();

    ContentManager contentManager = mock(ContentManager.class);
    when(contentManager.getSilverContentId(OBJECT_ID, COMPONENT_ID)).thenReturn(55);

    PdcPositionsFieldDisplayer instance = new PdcPositionsFieldDisplayer(pdcManager, contentManager);
    instance.display(printer, null, template, pagesContext);
    String display = new String(out.toByteArray(), Charsets.UTF_8).trim();
    assertNotNull(display);
    assertEquals(59, display.length());
  }

  /**
   * Test of display method, of class PdcPositionsFieldDisplayer.
   */
  @Test
  public void testDisplayNotParameterized() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    PrintWriter printer = new PrintWriter(new OutputStreamWriter(out, Charsets.UTF_8), true);

    HashMap<String, String> emptyParameters = new HashMap<String, String>();
    FieldTemplate template = mock(FieldTemplate.class);
    when(template.getParameters(LANGUAGE)).thenReturn(emptyParameters);

    PagesContext pagesContext = mockPageContext();
    PdcManager pdcManager = mockPdcBm();

    ContentManager contentManager = mock(ContentManager.class);
    when(contentManager.getSilverContentId(OBJECT_ID, COMPONENT_ID)).thenReturn(55);

    PdcPositionsFieldDisplayer instance = new PdcPositionsFieldDisplayer(pdcManager, contentManager);
    instance.display(printer, null, template, pagesContext);
    String display = new String(out.toByteArray(), Charsets.UTF_8).trim();
    assertNotNull(display);
    assertEquals("??axisId??", display);
  }

  /**
   * Test of update method, of class PdcPositionsFieldDisplayer.
   */
  @Test
  public void testUpdateWithList() throws Exception {
    PdcPositionsFieldDisplayer instance = new PdcPositionsFieldDisplayer();
    List<String> updates = instance.update(new ArrayList<FileItem>(), null, null, null);
    assertNotNull(updates);
    assertEquals(0, updates.size());
  }

  /**
   * Test of update method, of class PdcPositionsFieldDisplayer.
   */
  @Test
  public void testUpdateWithString() throws Exception {
    PdcPositionsFieldDisplayer instance = new PdcPositionsFieldDisplayer();
    List<String> updates = instance.update("", null, null, null);
    assertNotNull(updates);
    assertEquals(0, updates.size());
  }

  private PagesContext mockPageContext() {
    PagesContext pagesContext = mock(PagesContext.class);
    when(pagesContext.getObjectId()).thenReturn(OBJECT_ID);
    when(pagesContext.getComponentId()).thenReturn(COMPONENT_ID);
    when(pagesContext.getLanguage()).thenReturn(LANGUAGE);
    return pagesContext;
  }

  private PdcManager mockPdcBm() throws PdcException {
    ArrayList<ClassifyPosition> positions = buildPositions();

    Value value1 = mock(Value.class);
    when(value1.getName()).thenReturn("chimie");
    Value value2 = mock(Value.class);
    when(value1.getName()).thenReturn("géographie");

    PdcManager pdcManager = mock(PdcManager.class);
    when(pdcManager.getPositions(55, COMPONENT_ID)).thenReturn(positions);
    when(pdcManager.getValue(AXIS_ID, "2")).thenReturn(value1);
    when(pdcManager.getValue(AXIS_ID, "5")).thenReturn(value2);

    return pdcManager;
  }

  private ArrayList<ClassifyPosition> buildPositions() {
    ClassifyPosition position1 = mock(ClassifyPosition.class);
    when(position1.getValueOnAxis(2)).thenReturn("/0/2/");

    ClassifyPosition position2 = mock(ClassifyPosition.class);
    when(position1.getValueOnAxis(2)).thenReturn("/0/3/5/");

    ArrayList<ClassifyPosition> results = new ArrayList<ClassifyPosition>();
    results.add(position1);
    results.add(position2);
    return results;
  }

  /**
   * Test of isDisplayedMandatory method, of class PdcPositionsFieldDisplayer.
   */
  @Test
  public void testIsDisplayedMandatory() {
    PdcPositionsFieldDisplayer instance = new PdcPositionsFieldDisplayer();
    assertFalse(instance.isDisplayedMandatory());
  }

  /**
   * Test of getNbHtmlObjectsDisplayed method, of class JdbcFieldDisplayer.
   */
  @Test
  public void testGetNbHtmlObjectsDisplayed() {
    PdcPositionsFieldDisplayer instance = new PdcPositionsFieldDisplayer();
    int expResult = 1;
    int result = instance.getNbHtmlObjectsDisplayed(null, null);
    assertEquals(expResult, result);
  }
}
