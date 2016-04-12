/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of
 * the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with this
 * program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.media;

import org.apache.tika.Tika;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
public class MetadataExtractorTest {

  private final static Tika tika = new Tika();

  private static final File docFile = getDocumentNamed("/Liste_ECCA.doc");
  private static final File docxFile = getDocumentNamed("/Test.docx");
  private static final File ooFile = getDocumentNamed("/LibreOffice.odt");
  private static final File tifFile = getDocumentNamed("/logo-silverpeas-2010.tif");
  private static final File emptyPdfFile = getDocumentNamed("/empty.pdf");
  private static final File mp4File = getDocumentNamed("/video.mp4");
  private static final File movFile = getDocumentNamed("/video.mov");
  private static final File flvFile = getDocumentNamed("/video.flv");
  private static final File mp3File = getDocumentNamed("/sound.mp3");

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  private MetadataExtractor instance;

  @Before
  public void setup() {
    instance = new MetadataExtractor();
    reflectionRule.setField(instance, tika, "tika");
  }

  /**
   * Test of getSummaryInformation method, of class MetadataExtractor.
   */
  @Test
  public void testExtractMetadataFromOLE2WordDocument() {
    File file = docFile;
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
    assertThat(result.getMemoryData().getSizeAsLong(), is(file.length()));
    assertThat(result.getDefinition(), is(Definition.NULL));
    assertThat(result.getFramerate(), nullValue());
    assertThat(result.getDuration(), nullValue());
  }

  @Test
  public void testExtractMetadataFrom2007WordDocument() {
    File file = docxFile;
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
      assertThat(result.getMemoryData().getSizeAsLong(), is(file.length()));
      assertThat(result.getDefinition(), is(Definition.NULL));
      assertThat(result.getFramerate(), nullValue());
      assertThat(result.getDuration(), nullValue());
    } else {
      System.out.println("testExtractMetadataFrom2007WordDocument is not working correctly");
    }
  }

  @Test
  public void testExtractMetadataFromOpenOfficeDocument() {
    File file = ooFile;
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
    assertThat(result.getMemoryData().getSizeAsLong(), is(file.length()));
    assertThat(result.getDefinition(), is(Definition.NULL));
    assertThat(result.getFramerate(), nullValue());
    assertThat(result.getDuration(), nullValue());
  }

  @Test
  public void testExtractMetadataFromTifImage() {
    File file = tifFile;
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
    assertThat(result.getMemoryData().getSizeAsLong(), is(file.length()));
    assertThat(result.getDefinition(), is(Definition.of(1942, 1309)));
    assertThat(result.getFramerate(), nullValue());
    assertThat(result.getDuration(), nullValue());
  }

  /**
   * Test of getSummaryInformation method, of class MetadataExtractor.
   */
  @Test
  public void testExtractMetadataFromPdfWithoutMetadata() throws Exception {
    File file = emptyPdfFile;
    MetaData result = instance.extractMetadata(file);
    assertThat(result, is(notNullValue()));
    assertThat(result.getTitle(), is("Blank PDF Document"));
    assertThat(result.getSubject(), nullValue());
    assertThat(result.getAuthor(),
        is("Department of Justice (Executive Office of Immigration Review)"));
    assertThat(result.getComments(), is(nullValue()));
    assertThat(result.getKeywords(), emptyArray());
    assertThat(result.getSilverId(), is(nullValue()));
    assertThat(result.getSilverName(), is(nullValue()));
    assertThat(result.getCreationDate().getTime(), is(1141675593000L));
    assertThat(result.getLastSaveDateTime().getTime(), is(1141672353000L));
    assertThat(result.getMemoryData().getSizeAsLong(), is(file.length()));
    assertThat(result.getDefinition(), is(Definition.NULL));
    assertThat(result.getFramerate(), nullValue());
    assertThat(result.getDuration(), nullValue());
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

  @Test
  public void testExtractMetadataFromMp4Video() {
    File file = mp4File;
    MetaData result = instance.extractMetadata(file);
    assertThat(result, is(notNullValue()));
    assertThat(result.getSilverId(), is(nullValue()));
    assertThat(result.getSilverName(), is(nullValue()));
    assertThat(result.getMemoryData().getSizeAsLong(), is(file.length()));
    assertThat(result.getDefinition(), is(Definition.of(1280, 720)));
    assertThat(result.getFramerate(), nullValue());
    assertThat(result.getDuration().getTimeAsLong(), is(6040l));
    assertThat(result.getDuration().getFormattedDurationAsHMSM(), is("00:00:06.040"));
    assertThat(result.getDuration().getFormattedDurationAsHMS(), is("00:00:06"));
  }

  @Test
  public void testExtractMetadataFromMovVideo() {
    File file = movFile;
    MetaData result = instance.extractMetadata(file);
    assertThat(result, is(notNullValue()));
    assertThat(result.getSilverId(), is(nullValue()));
    assertThat(result.getSilverName(), is(nullValue()));
    assertThat(result.getMemoryData().getSizeAsLong(), is(file.length()));
    assertThat(result.getDefinition(), is(Definition.of(1280, 720)));
    assertThat(result.getFramerate(), nullValue());
    assertThat(result.getDuration().getTimeAsLong(), is(6040l));
    assertThat(result.getDuration().getFormattedDurationAsHMSM(), is("00:00:06.040"));
    assertThat(result.getDuration().getFormattedDurationAsHMS(), is("00:00:06"));
  }

  @Test
  public void testExtractMetadataFromFlvVideo() {
    File file = flvFile;
    MetaData result = instance.extractMetadata(file);
    assertThat(result, is(notNullValue()));
    assertThat(result.getSilverId(), nullValue());
    assertThat(result.getSilverName(), nullValue());
    assertThat(result.getMemoryData().getSizeAsLong(), is(file.length()));
    assertThat(result.getDefinition(), is(Definition.of(1280, 720)));
    assertThat(result.getFramerate().intValue(), is(25));
    assertThat(result.getDuration().getTimeAsLong(), is(6120l));
    assertThat(result.getDuration().getFormattedDurationAsHMSM(), is("00:00:06.120"));
    assertThat(result.getDuration().getFormattedDurationAsHMS(), is("00:00:06"));
  }

  @Test
  public void testExtractMetadataFromMp3Audio() {
    File file = mp3File;
    MetaData result = instance.extractMetadata(file);
    assertThat(result, is(notNullValue()));
    assertThat(result.getTitle(), isEmptyString());
    assertThat(result.getSubject(), nullValue());
    assertThat(result.getAuthor(), is("sound fishing bruitages"));
    assertThat(result.getComments(), nullValue());
    assertThat(result.getKeywords(), notNullValue());
    assertThat(result.getKeywords().length, is(0));
    assertThat(result.getSilverId(), nullValue());
    assertThat(result.getSilverName(), nullValue());
    assertThat(result.getMemoryData().getSizeAsLong(), is(file.length()));
    assertThat(result.getDefinition(), is(Definition.NULL));
    assertThat(result.getFramerate(), nullValue());
    assertThat(result.getDuration().getTimeAsLong(), is(4257l));
    assertThat(result.getDuration().getFormattedDurationAsHMSM(), is("00:00:04.257"));
    assertThat(result.getDuration().getFormattedDurationAsHMS(), is("00:00:04"));
  }

  private static File getDocumentNamed(final String name) {
    final URL documentLocation = MetadataExtractorTest.class.getResource(name);
    try {
      return new File(documentLocation.toURI());
    } catch (URISyntaxException e) {
      return null;
    }
  }
}
