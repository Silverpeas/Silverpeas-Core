/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.contribution.content.ddwe.model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.content.ddwe.model.DragAndDropWebEditorStore.Container;
import org.silverpeas.core.contribution.content.ddwe.model.DragAndDropWebEditorStore.Content;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.Charsets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class DragAndDropWebEditorStoreStructureTest {

  private File tempFile;

  @BeforeEach
  void setup() throws IOException {
    tempFile = File.createTempFile("ddwe", "content.xml");
  }

  @AfterEach
  void clear() {
    FileUtils.deleteQuietly(tempFile);
  }

  @Test
  void loadXmlContent() throws IOException {
    final Container container = loadContainer();
    assertThat(container, notNullValue());
    final Content tmpContent = container.getOrCreateTmpContent();
    assertThat(tmpContent, notNullValue());
    assertThat(tmpContent.getValue(), is("TEST TMP"));
    final Content.Metadata tmpMetadata = tmpContent.getMetadata();
    assertThat(tmpMetadata, notNullValue());
    assertThat(tmpMetadata.getCreatedBy(), is("26"));
    assertThat(tmpMetadata.getCreated(), is(ZonedDateTime.parse("2021-12-03T10:15:30+01:00[Europe/Paris]")));
    assertThat(tmpMetadata.getLastUpdatedBy(), is("38"));
    assertThat(tmpMetadata.getLastUpdated(), is(ZonedDateTime.parse("2021-12-04T16:38:59+01:00[Europe/Paris]")));
    final Content content = container.getOrCreateContent();
    assertThat(content, notNullValue());
    assertThat(content.getValue(), is("TEST"));
    final Content.Metadata metadata = content.getMetadata();
    assertThat(metadata, notNullValue());
    assertThat(metadata.getCreatedBy(), is("2"));
    assertThat(metadata.getCreated(), is(ZonedDateTime.parse("2021-11-03T10:15:30+01:00[Europe/Paris]")));
    assertThat(metadata.getLastUpdatedBy(), is("1"));
    assertThat(metadata.getLastUpdated(), is(ZonedDateTime.parse("2021-11-04T16:38:59+01:00[Europe/Paris]")));
  }

  @Test
  void saveXmlContent() throws IOException {
    final Container container = loadContainer();
    try(OutputStream out = FileUtils.openOutputStream(tempFile)) {
      Container.writeIn(container, out);
    }
    try (InputStream in = getContentStructureXmlStream()){
      String expected = IOUtils.toString(in, Charsets.UTF_8);
      String actual = FileUtils.readFileToString(tempFile, Charsets.UTF_8);
      assertThat(actual, is(expected));
    }
  }

  @Test
  void newXmlContent() {
    final Container container = new Container();
    assertThat(container.getContent().isEmpty(), is(true));
    final Content content = container.getOrCreateContent();
    assertThat(content, notNullValue());
    assertThat(container.getContent().isEmpty(), is(false));
    assertThat(container.getTmpContent().isEmpty(), is(true));
    final Content tmpContent = container.getOrCreateTmpContent();
    assertThat(tmpContent, notNullValue());
    assertThat(container.getTmpContent().isEmpty(), is(false));
  }

  private Container loadContainer() throws IOException {
    final Container container;
    try (InputStream in = getContentStructureXmlStream()) {
      container = Container.loadFrom(in);
    }
    return container;
  }

  private InputStream getContentStructureXmlStream() {
    return DragAndDropWebEditorStoreStructureTest.class.getClassLoader()
        .getResourceAsStream("org/silverpeas/core/contribution/content/ddwe/ddwecontent_structure.xml");
  }
}