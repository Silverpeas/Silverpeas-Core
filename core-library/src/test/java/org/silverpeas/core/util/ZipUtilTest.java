/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.TestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static java.io.File.separatorChar;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author ehugonnet
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ZipUtilTest {

  private Path tempDir;
  private final TestContext ctx = TestContext.getInstance();

  @BeforeEach
  public void setUpClass() throws IOException {
    tempDir = ctx.getPathOfBuildDirectory().resolve("zipdir");
    Files.createDirectories(tempDir);
  }

  @AfterEach
  public void tearDownClass() {
    FileUtils.deleteQuietly(tempDir.toFile());
  }

  @Test
  void testCompressPathToZip() throws Exception {
    File path = ctx.getPathOfTestResources().resolve("ZipSample").toFile();
    File outfile = tempDir.resolve("testCompressPathToZip.zip").toFile();
    ZipUtil.compressPathToZip(path, outfile);
    try (ZipFile zipFile = new ZipFile(outfile, Charsets.UTF_8.name())) {
      Enumeration<? extends ZipEntry> entries = zipFile.getEntries();
      assertThat(zipFile.getEncoding(), is(Charsets.UTF_8.name()));
      int nbEntries = 0;
      while (entries.hasMoreElements()) {
        entries.nextElement();
        nbEntries++;
      }
      assertThat(nbEntries, is(5));
      assertThat(zipFile.getEntry("ZipSample/simple.txt"), is(notNullValue()));
      assertThat(zipFile.getEntry("ZipSample/level1/simple.txt"), is(notNullValue()));
      assertThat(zipFile.getEntry("ZipSample/level1/level2b/simple.txt"), is(notNullValue()));
      assertThat(zipFile.getEntry("ZipSample/level1/level2a/simple.txt"), is(notNullValue()));

      ZipEntry accentuatedEntry = zipFile
          .getEntry("ZipSample/level1/level2a/sïmplifié.txt");
      if (accentuatedEntry == null) {
        accentuatedEntry = zipFile.getEntry("ZipSample/level1/level2a/" +
            new String("sïmplifié.txt".getBytes(StandardCharsets.UTF_8), Charset.defaultCharset()));
      }
      assertThat(accentuatedEntry, is(notNullValue()));
      assertThat(zipFile.getEntry("ZipSample/level1/level2c/"), is(nullValue()));
    }
  }

  @Test
  void testCompressStreamToZip() throws Exception {
    InputStream inputStream = this.getClass().getClassLoader()
        .getResourceAsStream("FrenchScrum.odp");
    String filePathNameToCreate =
        separatorChar + "dir1" + separatorChar + "dir2" + separatorChar + "FrenchScrum.odp";
    File outfile = tempDir.resolve("testCompressStreamToZip.zip").toFile();
    ZipUtil.compressStreamToZip(inputStream, filePathNameToCreate, outfile.getPath());
    Objects.requireNonNull(inputStream).close();
    assertThat(outfile, is(notNullValue()));
    assertThat(outfile.exists(), is(true));
    assertThat(outfile.isFile(), is(true));
    int result = ZipUtil.getNbFiles(outfile);
    assertThat(result, is(1));
    ZipFile zipFile = new ZipFile(outfile);
    assertThat(zipFile.getEntry("/dir1/dir2/FrenchScrum.odp"), is(notNullValue()));
    zipFile.close();
  }

  @Test
  void testExtractZipFromLinux() throws IOException {
    Path source = ctx.getPathOfTestResources().resolve("testExtractZipFromLinux.zip");
    Path dest = tempDir.resolve("extract");
    Files.createDirectories(dest);
    final Optional<String> encodingUsed = ZipUtil.extract(source.toFile(), dest.toFile());
    assertThat(encodingUsed.isPresent(), is(true));
    try(Stream<Path> children = Files.list(dest)) {
      assertThat(children.count(), is(not(0)));
    }
  }

  @Test
  void testExtractZipFrom7ZipWithoutAccent() throws IOException {
    Path source = ctx.getPathOfTestResources().resolve("testExtractZipFrom7ZipWithoutAccent.zip");
    Path dest = tempDir.resolve("extract");
    Files.createDirectories(dest);
    final Optional<String> encodingUsed = ZipUtil.extract(source.toFile(), dest.toFile());
    assertThat(encodingUsed.isPresent(), is(true));
    try(Stream<Path> children = Files.list(dest)) {
      assertThat(children.count(), is(not(0)));
    }
  }

  /**
   * Test of extract method, of class ZipManager.
   */
  @Test
  void testExtractZipFrom7Zip() throws IOException {
    Path source = ctx.getPathOfTestResources().resolve("testExtractZipFrom7Zip.zip");
    Path dest = tempDir.resolve("extract");
    Files.createDirectories(dest);
    final Optional<String> encodingUsed = ZipUtil.extract(source.toFile(), dest.toFile());
    assertThat(encodingUsed.isPresent(), is(true));
    try(Stream<Path> children = Files.list(dest)) {
      assertThat(children.count(), is(not(0)));
    }
  }

  @Test
  void testExtractZipFromWindows() throws IOException {
    Path source = ctx.getPathOfTestResources().resolve("testExtractZipFromWindows.zip");
    Path dest = tempDir.resolve("extract");
    Files.createDirectories(dest);
    final Optional<String> encodingUsed = ZipUtil.extract(source.toFile(), dest.toFile());
    assertThat(encodingUsed.isPresent(), is(true));
    try(Stream<Path> children = Files.list(dest)) {
      assertThat(children.count(), is(not(0)));
    }
  }

  @Test
  void testExtractZipFromMacos() throws IOException {
    Path source = ctx.getPathOfTestResources().resolve("testExtractZipFromMacos.zip");
    Path dest = tempDir.resolve("extract");
    Files.createDirectories(dest);
    final Optional<String> encodingUsed = ZipUtil.extract(source.toFile(), dest.toFile());
    assertThat(encodingUsed.isPresent(), is(true));
    try(Stream<Path> children = Files.list(dest)) {
      assertThat(children.count(), is(not(0)));
    }
  }

  @Test
  void testExtractTarGz() throws IOException {
    Path source = ctx.getPathOfTestResources().resolve("testExtract.tar.gz");
    Path dest = tempDir.resolve("extract-tar");
    Files.createDirectories(dest);
    ZipUtil.extract(source.toFile(), dest.toFile());
    Path uncompressedDir = dest.resolve("ZipSample");
    assertThat(Files.exists(uncompressedDir), is(true));
    assertThat(Files.isDirectory(uncompressedDir), is(true));
    try(Stream<Path> children = Files.list(uncompressedDir)) {
      assertThat(children.count(), is(not(2)));
    }
  }

  @Test
  void testExtractTarBz2() throws Exception {
    Path source = ctx.getPathOfTestResources().resolve("testExtract.tar.bz2");
    Path dest = tempDir.resolve("extract-bz2");
    Files.createDirectories(dest);
    ZipUtil.extract(source.toFile(), dest.toFile());
    Path uncompressedDir = dest.resolve("ZipSample");
    assertThat(Files.exists(uncompressedDir), is(true));
    assertThat(Files.isDirectory(uncompressedDir), is(true));
    try(Stream<Path> children = Files.list(uncompressedDir)) {
      assertThat(children.count(), is(not(2)));
    }
  }

  @Test
  void testGetNbFiles() throws Exception {
    Path path = ctx.getPathOfTestResources().resolve("ZipSample");
    Path outfile = tempDir.resolve("testGetNbFiles.zip");
    ZipUtil.compressPathToZip(path.toFile(), outfile.toFile());
    assertThat(Files.exists(outfile), is(true));
    assertThat(Files.isRegularFile(outfile), is(true));
    int result = ZipUtil.getNbFiles(outfile.toFile());
    assertThat(result, is(5));
  }
}
