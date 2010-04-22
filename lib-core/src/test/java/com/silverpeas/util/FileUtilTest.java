/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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
package com.silverpeas.util;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class FileUtilTest {

  public FileUtilTest() {
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
   * Test of getMimeType method, of class FileUtil.
   */
  @Test
  public void testGetMimeType() {
    String fileName = "";
    String expResult = FileUtil.DEFAULT_MIME_TYPE;
    String result = FileUtil.getMimeType(fileName);
    assertEquals(expResult, result);
    fileName = "toto.DOc";
    expResult = FileUtil.WORD_MIME_TYPE;
    result = FileUtil.getMimeType(fileName);
    assertEquals(expResult, result);
  }

  /**
   * Test of getAttachmentContext method, of class FileUtil.
   */
  @Test
  public void testGetAttachmentContext() {
    String context = "";
    String[] expResult = new String[]{FileUtil.BASE_CONTEXT};
    String[] result = FileUtil.getAttachmentContext(context);
    assertNotNull(result);
    assertArrayEquals(expResult, result);
    context = "test,context,complex";
    expResult = new String[]{FileUtil.BASE_CONTEXT, "test", "context", "complex"};
    result = FileUtil.getAttachmentContext(context);
    assertNotNull(result);
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of loadBundle method, of class FileUtil.
   */
  @Test
  public void testLoadBundle() {
    System.out.println("loadBundle");
    String name = "com/stratelia/webactiv/multilang/generalMultilang";
    ResourceBundle result = FileUtil.loadBundle(name, Locale.FRENCH);
    assertNotNull(result);
  }
}
