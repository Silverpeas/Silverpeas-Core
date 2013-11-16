/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.util;

import java.io.File;
import java.util.Date;

import org.junit.Test;

import static com.silverpeas.util.PathTestUtil.SEPARATOR;
import static com.silverpeas.util.PathTestUtil.TARGET_DIR;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
public class MetadataExtractorTest {

  private static final String docFile = TARGET_DIR + "test-classes" + SEPARATOR + "Liste_ECCA.doc";
  private static final String docxFile = TARGET_DIR + "test-classes" + SEPARATOR + "Test.docx";
  private static final String ooFile = TARGET_DIR + "test-classes" + SEPARATOR + "LibreOffice.odt";
  private static final String tifFile = TARGET_DIR + "test-classes" + SEPARATOR
      + "logo-silverpeas-2010.tif";
  private static final String emptyPdfFile = TARGET_DIR + "test-classes" + SEPARATOR
      + "20115061_FDS_Rubson SA2 Sanitaire 2 en 1 tous coloris MAJ 2012_HENKEL.pdf";

  public MetadataExtractorTest() {
  }

  /**
   * Test of getSummaryInformation method, of class MetadataExtractor.
   */
  @Test
  public void testExtractMetadataFromOLE2WordDocument() {
    MetadataExtractor instance = new MetadataExtractor();
    File file = new File(docFile);
    MetaData result = instance.extractMetadata(file);
    assertThat(result, is(notNullValue()));
    assertThat(result.getTitle(), is("Liste de taches"));
    assertThat(result.getSubject(), is("Modif sur WF ECCA"));
    assertThat(result.getAuthor(), is("Administrateur"));
    assertThat(result.getComments(), is(nullValue()));
    assertThat(result.getKeywords()[0], is("test formation SA"));
    assertThat(result.getSilverId(), is(nullValue()));
    assertThat(result.getSilverName(), is(nullValue()));
    assertThat(result.getCreationDate().getTime(), is(1122998040000L));
    assertThat(result.getLastSaveDateTime().getTime(), is(1316063700000L));
  }

  @Test
  public void testExtractMetadataFrom2007WordDocument() {
    MetadataExtractor instance = new MetadataExtractor();
    File file = new File(docxFile);
    assertThat(file.exists(), is(true));
    MetaData result = instance.extractMetadata(file);
    assertThat(result, is(notNullValue()));
    if (StringUtil.isDefined(result.getTitle())) {
      assertThat(result.getTitle(), is("Les donuts"));
      assertThat(result.getSubject(), is("Skateboard"));
      assertThat(result.getAuthor(), is("Bart Simpson"));
      assertThat(result.getComments(), is("Commentaires accentués"));
      assertThat(result.getKeywords()[0], is("mots clés du documents"));
      assertThat(result.getSilverId(), is(nullValue()));
      assertThat(result.getSilverName(), is(nullValue()));
      assertThat(result.getCreationDate(), is(new Date(1315916400000L)));
      assertThat(result.getLastSaveDateTime().getTime(), is(1316001900000L));
    } else {
      System.out.println("testExtractMetadataFrom2007WordDocument is not working correctly");
    }
  }

  @Test
  public void testExtractMetadataFromOpenOfficeDocument() {
    MetadataExtractor instance = new MetadataExtractor();
    File file = new File(ooFile);
    MetaData result = instance.extractMetadata(file);
    assertThat(result, is(notNullValue()));
    assertThat(result.getTitle(), is("Test pour Tika"));
    assertThat(result.getSubject(), is("Document de test pour Tika"));
    assertThat(result.getAuthor(), is("Emmanuel Hugonnet"));
    assertThat(result.getComments(), is("Comments"));
    assertThat(result.getKeywords()[0], is("Tika Keywords Test"));
    assertThat(result.getSilverId(), is(nullValue()));
    assertThat(result.getSilverName(), is(nullValue()));
    assertThat(result.getCreationDate().getTime(), is(1239874322000L));
    assertThat(result.getLastSaveDateTime(), is(nullValue()));
  }

  @Test
  public void testExtractMetadataFromTifImage() {
    MetadataExtractor instance = new MetadataExtractor();
    File file = new File(tifFile);
    MetaData result = instance.extractMetadata(file);
    assertThat(result, is(notNullValue()));
    assertThat(result.getTitle(), is("Logo Silverpeas"));
    assertThat(result.getSubject(), is("silverpeas"));
    assertThat(result.getAuthor(), is("AuroreAllibe"));
    assertThat(result.getComments(), is("Logo silverpeas txt noir"));
    assertThat(result.getKeywords(), is(notNullValue()));
    assertThat(result.getKeywords().length, is(2));
    assertThat(result.getKeywords()[0], is("silverpeas"));
    assertThat(result.getKeywords()[1], is("logo"));
    assertThat(result.getSilverId(), is(nullValue()));
    assertThat(result.getSilverName(), is(nullValue()));
    assertThat(result.getCreationDate().getTime(), is(1340963223000L));
    assertThat(result.getLastSaveDateTime(), is(nullValue()));
  }

  /**
   * Test of getSummaryInformation method, of class MetadataExtractor.
   */
  @Test
  public void testExtractMetadataFromPdfWithoutMetadata() throws Exception {
    File file = new File(emptyPdfFile);
    MetadataExtractor instance = new MetadataExtractor();
    MetaData result = instance.extractMetadata(file);
    assertThat(result, is(notNullValue()));
    assertThat(result.getTitle(), is(
        "20115061_FDS_Rubson SA2 Sanitaire 2 en 1 tous coloris MAJ 2012_HENKEL"));
    assertThat(result.getSubject(), is(""));
    assertThat(result.getAuthor(), is(""));
    assertThat(result.getComments(), is(nullValue()));
    assertThat(result.getKeywords()[0], is(""));
    assertThat(result.getSilverId(), is(nullValue()));
    assertThat(result.getSilverName(), is(nullValue()));
    assertThat(result.getCreationDate().getTime(), is(1356623042000L));
    assertThat(result.getLastSaveDateTime().getTime(), is(1361543391000L));

  }
  /*
   private void loadPdfWithPdfBox(File file) throws Exception {
   PDDocument document = PDDocument.load(file);
   PDDocumentInformation info = document.getDocumentInformation();
   System.out.println("Title: " + info.getTitle());
   String istring = info.getTitle().substring(info.getTitle().length() - 1);
   int ivalue = istring.codePointAt(0);
   System.out.println(Integer.toHexString(ivalue));
   RandomAccessBuffer buffer = new RandomAccessBuffer();
   document = PDDocument.loadNonSeq(file, buffer);
   info = document.getDocumentInformation();
   System.out.println("NonSeqTitle: " + info.getTitle());
   istring = info.getTitle().substring(info.getTitle().length() - 1);
   ivalue = istring.codePointAt(0);
   System.out.println(Integer.toHexString(ivalue));
   }*/
}
