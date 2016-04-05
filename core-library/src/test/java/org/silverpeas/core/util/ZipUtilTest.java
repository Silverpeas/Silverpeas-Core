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
package org.silverpeas.core.util;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.ZipUtil;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static java.io.File.separatorChar;

/**
* @author ehugonnet
*/
public class ZipUtilTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  private File tempDir;

  @Before
  public void setUpClass() throws Exception {
    tempDir = new File(mavenTargetDirectoryRule.getBuildDirFile(), "zipdir");
    tempDir.mkdirs();
  }

  @After
  public void tearDownClass() throws Exception {
    FileUtils.deleteQuietly(tempDir);
  }

  /**
* Test of compressPathToZip method, of class ZipManager.
*
* @throws Exception
*/
  @Test
  public void testCompressPathToZip() throws Exception {
    File path = new File(mavenTargetDirectoryRule.getResourceTestDirFile(), "ZipSample");
    File outfile = new File(tempDir, "testCompressPathToZip.zip");
    ZipUtil.compressPathToZip(path, outfile);
    ZipFile zipFile = new ZipFile(outfile, CharEncoding.UTF_8);
    try {
      Enumeration<? extends ZipEntry> entries = zipFile.getEntries();
      assertThat(zipFile.getEncoding(), is(CharEncoding.UTF_8));
      int nbEntries = 0;
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        nbEntries++;
      }
      assertThat(nbEntries, is(5));
      assertThat(zipFile.getEntry("ZipSample/simple.txt"), is(notNullValue()));
      assertThat(zipFile.getEntry("ZipSample/level1/simple.txt"), is(notNullValue()));
      assertThat(zipFile.getEntry("ZipSample/level1/level2b/simple.txt"), is(notNullValue()));
      assertThat(zipFile.getEntry("ZipSample/level1/level2a/simple.txt"), is(notNullValue()));

      ZipEntry accentuatedEntry = zipFile.getEntry("ZipSample/level1/level2a/s\u00efmplifi\u00e9.txt") ;
      if(accentuatedEntry == null) {
       accentuatedEntry = zipFile.getEntry("ZipSample/level1/level2a/" + new String(
           "sïmplifié.txt".getBytes("UTF-8"), Charset.defaultCharset()));
      }
      assertThat(accentuatedEntry, is(notNullValue()));
      assertThat(zipFile.getEntry("ZipSample/level1/level2c/"), is(nullValue()));
    } finally {
      zipFile.close();
    }
  }

  /**
* Test of compressStreamToZip method, of class ZipManager.
*
* @throws Exception
*/
  @Test
  public void testCompressStreamToZip() throws Exception {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("FrenchScrum.odp");
    String filePathNameToCreate = separatorChar + "dir1" + separatorChar + "dir2" + separatorChar
        + "FrenchScrum.odp";
    File outfile = new File(tempDir, "testCompressStreamToZip.zip");
    ZipUtil.compressStreamToZip(inputStream, filePathNameToCreate, outfile.getPath());
    inputStream.close();
    assertThat(outfile, is(notNullValue()));
    assertThat(outfile.exists(), is(true));
    assertThat(outfile.isFile(), is(true));
    int result = ZipUtil.getNbFiles(outfile);
    assertThat(result, is(1));
    ZipFile zipFile = new ZipFile(outfile);
    assertThat(zipFile.getEntry("/dir1/dir2/FrenchScrum.odp"), is(notNullValue()));
    zipFile.close();
  }

  /**
* Test of extract method, of class ZipManager.
*
* @throws Exception
*/
  @Test
  public void testExtract() throws Exception {
    File source = new File(mavenTargetDirectoryRule.getResourceTestDirFile(), "testExtract.zip");
    File dest = new File(tempDir, "extract");
    dest.mkdirs();
    ZipUtil.extract(source, dest);
    assertThat(dest, is(notNullValue()));
  }

  /**
* Test of extract method, of class ZipManager.
*
* @throws Exception
*/
  @Test
  public void testExtractTargz() throws Exception {
    File source = new File(mavenTargetDirectoryRule.getResourceTestDirFile(), "testExtract.tar.gz");
    File dest = new File(tempDir, "extract-tar");
    dest.mkdirs();
    ZipUtil.extract(source, dest);
    assertThat(dest, is(notNullValue()));
    File uncompressedDir = new File(dest, "ZipSample");
    assertThat(uncompressedDir.exists(), is(true));
    assertThat(uncompressedDir.isDirectory(), is(true));
    assertThat(uncompressedDir.list().length, is(2));
  }

  /**
* Test of extract method, of class ZipManager.
*
* @throws Exception
*/
  @Test
  public void testExtractTarBz2() throws Exception {
    File source = new File(mavenTargetDirectoryRule.getResourceTestDirFile(), "testExtract.tar.bz2");
    File dest = new File(tempDir, "extract-bz2");
    dest.mkdirs();
    ZipUtil.extract(source, dest);
    assertThat(dest, is(notNullValue()));
    File uncompressedDir = new File(dest, "ZipSample");
    assertThat(uncompressedDir.exists(), is(true));
    assertThat(uncompressedDir.isDirectory(), is(true));
    assertThat(uncompressedDir.list().length, is(2));
  }

  /**
* Test of getNbFiles method, of class ZipManager.
*
* @throws Exception
*/
  @Test
  public void testGetNbFiles() throws Exception {
    File path = new File(mavenTargetDirectoryRule.getResourceTestDirFile(), "ZipSample");
    File outfile = new File(tempDir, "testGetNbFiles.zip");
    ZipUtil.compressPathToZip(path, outfile);
    assertThat(outfile, is(notNullValue()));
    assertThat(outfile.exists(), is(true));
    assertThat(outfile.isFile(), is(true));
    int result = ZipUtil.getNbFiles(outfile);
    assertThat(result, is(5));
  }
}
