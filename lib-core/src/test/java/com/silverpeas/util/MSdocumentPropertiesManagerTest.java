/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of
 * the text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.silverpeas.util;

import java.io.File;
import java.util.Date;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
public class MSdocumentPropertiesManagerTest {

  private static final String docFile = PathTestUtil.TARGET_DIR + "test-classes" + PathTestUtil.SEPARATOR + "Liste_ECCA.doc";
  private static final String docxFile = PathTestUtil.TARGET_DIR + "test-classes" + PathTestUtil.SEPARATOR + "Test.docx";

  public MSdocumentPropertiesManagerTest() {
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
   * Test of getSummaryInformation method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetSummaryInformationByFilename() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    SummaryInformation result = instance.getSummaryInformation(docFile);
    assertThat(result, is(notNullValue()));
    assertThat(result.getPageCount(), is(1));
    /*
     * result = instance.getSummaryInformation(docxFile); assertThat(result, is(notNullValue()));
     * assertThat(result.getPageCount(), is(100));
     */
  }

  /**
   * Test of getSummaryInformation method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetSummaryInformationFromFile() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    File file = new File(docFile);
    SummaryInformation result = instance.getSummaryInformation(file);
    assertThat(result, is(notNullValue()));
    /*
     * file = new File(docxFile); result = instance.getSummaryInformation(file); assertThat(result,
     * is(notNullValue()));
     */
  }

  /**
   * Test of getDocumentSummaryInformation method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetDocumentSummaryInformation() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    DocumentSummaryInformation result = instance.getDocumentSummaryInformation(docFile);
    assertThat(result, is(notNullValue()));
  }

  /**
   * Test of getTitle method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetTitle() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    String title = instance.getTitle(docFile);
    assertThat(title, is(notNullValue()));
    assertThat(title, is("Liste de taches"));
    /*
     * title = instance.getTitle(docxFile); assertThat(title, is(notNullValue())); assertThat(title,
     * is("Les donuts"));
     */
  }

  /**
   * Test of getSubject method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetSubject() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    String subject = instance.getSubject(docFile);
    assertThat(subject, is(notNullValue()));
    assertThat(subject, is("Modif sur WF ECCA"));
    /*
     * subject = instance.getSubject(docxFile); assertThat(subject, is(notNullValue()));
     * assertThat(subject, is("Skateboard"));
     */
  }

  /**
   * Test of getAuthor method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetAuthor() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    String author = instance.getAuthor(docFile);
    assertThat(author, is(notNullValue()));
    assertThat(author, is("Administrateur"));
    /*
     * author = instance.getAuthor(docxFile); assertThat(author, is(notNullValue()));
     * assertThat(author, is("Bart Simpson"));
     */
  }

  /**
   * Test of getComments method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetComments() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    String comments = instance.getComments(docFile);
    assertThat(comments, is(notNullValue()));
    assertThat(comments, is(""));
    /*
     * comments = instance.getComments(docxFile); assertThat(comments, is(notNullValue()));
     * assertThat(comments, is("Commentaires accentués"));
     */
  }

  /**
   * Test of getSecurity method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetSecurity() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    int security = instance.getSecurity(docFile);
    assertThat(security, is(notNullValue()));
    assertThat(security, is(0));
    /*
     * security = instance.getSecurity(docxFile); assertThat(security, is(notNullValue()));
     * assertThat(security, is(0));
     */
  }

  /**
   * Test of getKeywords method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetKeywords() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    String keywords = instance.getKeywords(docFile);
    assertThat(keywords, is(notNullValue()));
    assertThat(keywords, is("test formation SA"));
    /*
     * keywords = instance.getKeywords(docxFile); assertThat(keywords, is(notNullValue()));
     * assertThat(keywords, is("mots clés du documents"));
     */
  }

  /**
   * Test of getSilverId method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetSilverId() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    String silverId = instance.getSilverId(docFile);
    assertThat(silverId, is(notNullValue()));
    assertThat(silverId, is(""));
    /*
     * silverId = instance.getSilverId(docxFile); assertThat(silverId, is(notNullValue()));
     * assertThat(silverId, is(""));
     */
  }

  /**
   * Test of getSilverName method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetSilverName() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    String silverName = instance.getSilverName(docFile);
    assertThat(silverName, is(notNullValue()));
    assertThat(silverName, is(""));
    /*
     * silverName = instance.getSilverName(docxFile); assertThat(silverName, is(notNullValue()));
     * assertThat(silverName, is(""));
     */
  }

  /**
   * Test of getLastSaveDateTime method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetLastSaveDateTime() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    Date lastSavedDate = instance.getLastSaveDateTime(docFile);
    Date expectedDate = new Date(1220877840000L);

    assertThat(lastSavedDate, is(notNullValue()));
    assertThat(lastSavedDate.getTime(), is(1220877840000L));
    assertThat(lastSavedDate, is(expectedDate));
    /*
     * lastSavedDate = instance.getLastSaveDateTime(docxFile); expectedDate = new
     * Date(1220877840000L); assertThat(lastSavedDate, is(notNullValue()));
     * assertThat(lastSavedDate.getTime(), is(1220877840000L)); assertThat(lastSavedDate,
     * is(expectedDate));
     */

  }

  /**
   * Test of getLastCreateDateTime method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testGetLastCreateDateTime() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    Date lastCreatedDate = instance.getLastCreateDateTime(docFile);
    Date expectedDate = new Date(1122998040000L);

    assertThat(lastCreatedDate, is(notNullValue()));
    assertThat(lastCreatedDate.getTime(), is(1122998040000L));
    assertThat(lastCreatedDate, is(expectedDate));
    /*
     * lastCreatedDate = instance.getLastCreateDateTime(docxFile); expectedDate = new
     * Date(1220877840000L); assertThat(lastCreatedDate, is(notNullValue()));
     * assertThat(lastCreatedDate.getTime(), is(1220877840000L)); assertThat(lastCreatedDate,
     * is(expectedDate));
     */
  }

  /**
   * Test of isSummaryInformation method, of class MSdocumentPropertiesManager.
   */
  @Test
  public void testIsSummaryInformation() {
    MSdocumentPropertiesManager instance = new MSdocumentPropertiesManager();
    boolean result = instance.isSummaryInformation(docFile);
    assertThat(result, is(true));
    /*
     * result = instance.isSummaryInformation(docxFile); assertThat(result,is(true));
     */
  }
}
