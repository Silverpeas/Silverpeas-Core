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
package org.silverpeas.core.contribution.content.form.displayers;

import org.silverpeas.core.contribution.content.form.FormException;
import java.util.Arrays;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.fieldType.FileField;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 *
 * @author mmoquillon
 */
public class VideoFieldDisplayerTest {

  public VideoFieldDisplayerTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getManagedTypes method, of class VideoFieldDisplayer.
   */
  @Test
  public void testGetManagedTypes() {
    VideoFieldDisplayer instance = new VideoFieldDisplayer();
    String[] expResult = {FileField.TYPE};
    String[] result = instance.getManagedTypes();
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of update method, of class VideoFieldDisplayer.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testUpdateAttachment() throws Exception {
    String attachmentId = "toto.flv";
    FileField field = new FileField();
    FieldTemplate template = new GenericFieldTemplate("video", FileField.class);
    PagesContext PagesContext = mock(PagesContext.class);
    VideoFieldDisplayer instance = new VideoFieldDisplayer();
    List<String> expResult = Arrays.asList("toto.flv");
    List<String> result = instance.update(attachmentId, field, template, PagesContext);
    assertEquals(expResult, result);
  }

  /**
   * Test of update method, of class VideoFieldDisplayer.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testUpdateEmptyAttachment() throws Exception {
    String attachmentId = "";
    FileField field = new FileField();
    FieldTemplate template = new GenericFieldTemplate("video", FileField.class);
    PagesContext PagesContext = mock(PagesContext.class);
    VideoFieldDisplayer instance = new VideoFieldDisplayer();
    List<String> result = instance.update(attachmentId, field, template, PagesContext);
    assertTrue(result.isEmpty());
    assertTrue((field.isNull()));
  }

  /**
   * Test of isDisplayedMandatory method, of class VideoFieldDisplayer.
   */
  @Test
  public void testIsDisplayedMandatory() {
    VideoFieldDisplayer instance = new VideoFieldDisplayer();
    boolean result = instance.isDisplayedMandatory();
    assertTrue(result);
  }

  /**
   * Test of getNbHtmlObjectsDisplayed method, of class VideoFieldDisplayer.
   *
   * @throws org.silverpeas.core.contribution.content.form.FormException
   */
  @Test
  public void testGetNbHtmlObjectsDisplayed() throws FormException {
    FieldTemplate template = new GenericFieldTemplate("video", FileField.class);
    PagesContext pagesContext = mock(PagesContext.class);
    VideoFieldDisplayer instance = new VideoFieldDisplayer();
    int expResult = 2;
    int result = instance.getNbHtmlObjectsDisplayed(template, pagesContext);
    assertEquals(expResult, result);
  }
}
