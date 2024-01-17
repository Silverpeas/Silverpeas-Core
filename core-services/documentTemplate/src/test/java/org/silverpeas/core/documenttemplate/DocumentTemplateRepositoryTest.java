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

package org.silverpeas.core.documenttemplate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedMock;
import org.silverpeas.core.test.unit.extention.TestedBean;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.file.DeletingPathVisitor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.documenttemplate.DocumentTemplateTestUtil.*;
import static org.silverpeas.core.documenttemplate.JsonDocumentTemplate.decode;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class DocumentTemplateRepositoryTest {

  private static final  DocumentTemplate DEFAULT_TEMPLATE = new DocumentTemplate(decode(DEFAULT_JSON), "txt");
  private static final String SIMPLE_CONTENT = "Simple content";

  @TestedBean
  private DefaultDocumentTemplateRepository repository;

  @TestManagedMock
  private UserProvider userProvider;

  @BeforeAll
  static void setTimeZone() {
    // we set explicitly a time zone different of UTC to check the datetime are correctly
    // converted in UTC in our API
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
  }

  @BeforeEach
  void setup() {
    final User user = mock(User.class);
    when(user.getId()).thenReturn("26");
    when(userProvider.getCurrentRequester()).thenReturn(user);
    assertThat(Files.exists(getRepositoryPath()), is(false));
  }

  @AfterEach
  void cleanUp() {
    DeletingPathVisitor.deleteQuietly(getRepositoryPath());
  }

  @DisplayName("Creating a document template without id should throw an exception")
  @Test
  void createIntoRepoWithoutId() {
    final ByteArrayInputStream content = new ByteArrayInputStream(new byte[0]);
    final DocumentTemplate templateWithoutId = new DocumentTemplate(DEFAULT_TEMPLATE);
    templateWithoutId.setId(null);
    final DocumentTemplateRuntimeException exception = assertThrows(
        DocumentTemplateRuntimeException.class,
        () -> repository.create(templateWithoutId, content));
    assertThat(exception.getMessage(), is("Document template has no id set"));
  }

  @DisplayName("Creating a document template without content should throw an exception")
  @Test
  void createIntoRepoWithoutContent() {
    final DocumentTemplateRuntimeException exception = assertThrows(
        DocumentTemplateRuntimeException.class,
        () -> repository.create(DEFAULT_TEMPLATE, null));
    assertThat(exception.getMessage(), is("Content MUST exists for document template with id an identifier"));
  }

  @DisplayName("Creating a document template without file extension should throw an exception")
  @Test
  void createIntoRepoWithoutExtension() {
    final ByteArrayInputStream content = new ByteArrayInputStream(new byte[0]);
    final DocumentTemplate templateWithoutFileExtension = new DocumentTemplate(DEFAULT_TEMPLATE);
    templateWithoutFileExtension.setExtension(null);
    final DocumentTemplateRuntimeException exception = assertThrows(
        DocumentTemplateRuntimeException.class,
        () -> repository.create(templateWithoutFileExtension, content));
    assertThat(exception.getMessage(), is("File extension MUST exists for document template with id an identifier"));
  }

  @DisplayName("Creating a document template should work")
  @Test
  void create() {
    final ByteArrayInputStream content = new ByteArrayInputStream(
        SIMPLE_CONTENT.getBytes(Charsets.UTF_8));
    final DocumentTemplate template = repository.create(DEFAULT_TEMPLATE, content);
    assertThat(template, not(sameInstance(DEFAULT_TEMPLATE)));
    assertThat(template.getJson().getCreatorId(), is("26"));
    assertThat(template.getJson().getCreationInstant(), greaterThan(DEFAULT_CREATION_INSTANT));
    assertThat(template.getJson().getLastUpdaterId(), is(template.getJson().getCreatorId()));
    assertThat(template.getJson().getLastUpdateInstant(), is(template.getJson().getCreationInstant()));
    final File contentFile = template.getContentFilePath().toFile();
    final File jsonFile = template.getDescriptorFilePath().toFile();
    assertThat(jsonFile, is(new File(contentFile.getParentFile(), template.getId() + ".json")));
    assertThat(contentFile.isFile(), is(true));
    assertThat(jsonFile.isFile(), is(true));
    assertThat(contentFile.getName(), is(template.getId() + ".txt"));
  }

  @DisplayName("Creating a document template whereas content file already exists should throw an exception")
  @Test
  void createIntoRepoWhereasContentFileAlreadyExists() throws IOException {
    final Path filePath = DEFAULT_TEMPLATE.getContentFilePath();
    Files.createDirectories(filePath.getParent());
    Files.createFile(filePath);
    final DocumentTemplateRuntimeException exception = assertThrows(
        DocumentTemplateRuntimeException.class, this::create);
    assertThat(exception.getMessage(), is("JSON or content or both files already exist for document template with id an identifier"));
  }

  @DisplayName("Creating a document template whereas json file already exists should throw an exception")
  @Test
  void createIntoRepoWhereasJsonFileAlreadyExists() throws IOException {
    final Path filePath = getRepositoryTemplatePath(DEFAULT_TEMPLATE.getId() + ".json");
    Files.createDirectories(filePath.getParent());
    Files.createFile(filePath);
    final DocumentTemplateRuntimeException exception = assertThrows(
        DocumentTemplateRuntimeException.class, this::create);
    assertThat(exception.getMessage(), is("JSON or content or both files already exist for document template with id an identifier"));
  }

  @DisplayName("Updating a document template without id should throw an exception")
  @Test
  void updateIntoRepoWithoutId() {
    create();
    final DocumentTemplate templateWithoutId = new DocumentTemplate(DEFAULT_TEMPLATE);
    templateWithoutId.setId(null);
    final DocumentTemplateRuntimeException exception = assertThrows(
        DocumentTemplateRuntimeException.class, () -> repository.update(templateWithoutId, null));
    assertThat(exception.getMessage(), is("Document template has no id set"));
  }

  @DisplayName("Updating a document template while json file is missing throw an exception")
  @Test
  void updateIntoRepoWhileJsonFileIsMissing() {
    create();
    final DocumentTemplate template = new DocumentTemplate(DEFAULT_TEMPLATE);
    DeletingPathVisitor.deleteQuietly(template.getDescriptorFilePath());
    final DocumentTemplateRuntimeException exception = assertThrows(
        DocumentTemplateRuntimeException.class, () -> repository.update(template, null));
    assertThat(exception.getMessage(), is("JSON or content or both files are missing for document template with id an identifier"));
  }

  @DisplayName("Updating a document template while content file is missing throw an exception")
  @Test
  void updateIntoRepoWhileContentFileIsMissing() {
    create();
    final DocumentTemplate template = new DocumentTemplate(DEFAULT_TEMPLATE);
    DeletingPathVisitor.deleteQuietly(template.getContentFilePath());
    final DocumentTemplateRuntimeException exception = assertThrows(
        DocumentTemplateRuntimeException.class, () -> repository.update(template, null));
    assertThat(exception.getMessage(), is("JSON or content or both files are missing for document template with id an identifier"));
  }

  @DisplayName("Updating only the json file of a document template should work")
  @Test
  void updateOnlyDescriptor() throws IOException {
    create();
    final DocumentTemplate template = new DocumentTemplate(DEFAULT_TEMPLATE);
    template.setExtension("png");
    template.setPosition(38);
    template.setName(template.getName("fr") + " [MODI]", "fr");
    template.setDescription(template.getDescription("en") + " [MODI]", "en");
    final File notExistsFile = template.getContentFilePath().toFile();
    assertThat(notExistsFile.getName(), is(template.getId() + ".png"));
    assertThat(notExistsFile.exists(), is(false));
    final DocumentTemplate updatedTemplate = repository.update(template, null);
    assertThat(updatedTemplate, not(sameInstance(template)));
    final File contentFile = updatedTemplate.getContentFilePath().toFile();
    final File jsonFile = updatedTemplate.getDescriptorFilePath().toFile();
    assertThat(jsonFile, is(new File(contentFile.getParentFile(), updatedTemplate.getId() + ".json")));
    assertThat(contentFile.isFile(), is(true));
    assertThat(jsonFile.isFile(), is(true));
    assertThat(contentFile.getName(), is(updatedTemplate.getId() + ".txt"));
    final OffsetDateTime lastUpdateDate = OffsetDateTime.ofInstant(updatedTemplate.getJson().getLastUpdateInstant(), ZoneId.systemDefault());
    assertThat(lastUpdateDate, greaterThan(DEFAULT_CREATION_DATE));
    assertThat(Files.readString(jsonFile.toPath()), is("{" +
        "\"id\":\"an identifier\"," +
        "\"nameTranslations\":{" +
        "\"fr\":\"Ceci est un test [MODI]\"," +
        "\"en\":\"This is a test\"" +
        "}," +
        "\"descriptionTranslations\":{\"en\":\" [MODI]\"}," +
        "\"position\":38," +
        "\"creatorId\":\"1\"," +
        "\"creationInstant\":\"2022-06-15T14:35:06.700176+02:00\"," +
        "\"lastUpdaterId\":\"26\"," +
        "\"lastUpdateInstant\":\"" + lastUpdateDate + "\"}"));
    assertThat(Files.readString(contentFile.toPath()), is(SIMPLE_CONTENT));
  }

  @DisplayName("Updating json and content files of a document template should work")
  @Test
  void update() throws IOException {
    create();
    final String updatedContent = "Another Simple content";
    final ByteArrayInputStream updatedContentStream = new ByteArrayInputStream(
        updatedContent.getBytes(Charsets.UTF_8));
    final DocumentTemplate template = new DocumentTemplate(DEFAULT_TEMPLATE);
    template.setExtension("png");
    template.setPosition(38);
    template.setName(template.getName("fr") + " [MODI]", "fr");
    final File notExistsFile = template.getContentFilePath().toFile();
    assertThat(notExistsFile.getName(), is(template.getId() + ".png"));
    assertThat(notExistsFile.exists(), is(false));
    final DocumentTemplate updatedTemplate = repository.update(template, updatedContentStream);
    assertThat(updatedTemplate, not(sameInstance(template)));
    final File contentFile = updatedTemplate.getContentFilePath().toFile();
    final File jsonFile = updatedTemplate.getDescriptorFilePath().toFile();
    assertThat(jsonFile, is(new File(contentFile.getParentFile(), updatedTemplate.getId() + ".json")));
    assertThat(contentFile.isFile(), is(true));
    assertThat(jsonFile.isFile(), is(true));
    assertThat(contentFile.getName(), is(updatedTemplate.getId() + ".png"));
    final OffsetDateTime lastUpdateDate = OffsetDateTime.ofInstant(updatedTemplate.getJson().getLastUpdateInstant(), ZoneId.systemDefault());
    assertThat(lastUpdateDate, greaterThan(DEFAULT_CREATION_DATE));
    assertThat(Files.readString(jsonFile.toPath()), is("{" +
        "\"id\":\"an identifier\"," +
        "\"nameTranslations\":{" +
        "\"fr\":\"Ceci est un test [MODI]\"," +
        "\"en\":\"This is a test\"" +
        "}," +
        "\"descriptionTranslations\":{}," +
        "\"position\":38," +
        "\"creatorId\":\"1\"," +
        "\"creationInstant\":\"2022-06-15T14:35:06.700176+02:00\"," +
        "\"lastUpdaterId\":\"26\"," +
        "\"lastUpdateInstant\":\"" + lastUpdateDate + "\"}"));
    assertThat(Files.readString(contentFile.toPath()), is(updatedContent));
  }

  @DisplayName("Deleting a document template without id should throw an exception")
  @Test
  void deleteFromRepoWithoutId() {
    create();
    final DocumentTemplate templateWithoutId = new DocumentTemplate(DEFAULT_TEMPLATE);
    templateWithoutId.setId(null);
    final DocumentTemplateRuntimeException exception = assertThrows(
        DocumentTemplateRuntimeException.class, () -> repository.delete(templateWithoutId));
    assertThat(exception.getMessage(), is("Document template has no id set"));
  }

  @DisplayName("Deleting a document template while json file is missing throw an exception")
  @Test
  void deleteIntoRepoWhileJsonFileIsMissing() {
    create();
    final DocumentTemplate template = new DocumentTemplate(DEFAULT_TEMPLATE);
    DeletingPathVisitor.deleteQuietly(template.getDescriptorFilePath());
    final DocumentTemplateRuntimeException exception = assertThrows(
        DocumentTemplateRuntimeException.class, () -> repository.delete(template));
    assertThat(exception.getMessage(), is("JSON or content or both files are missing for document template with id an identifier"));
  }

  @DisplayName("Deleting a document template while content file is missing throw an exception")
  @Test
  void deleteIntoRepoWhileContentFileIsMissing() {
    create();
    final DocumentTemplate template = new DocumentTemplate(DEFAULT_TEMPLATE);
    DeletingPathVisitor.deleteQuietly(template.getContentFilePath());
    final DocumentTemplateRuntimeException exception = assertThrows(
        DocumentTemplateRuntimeException.class, () -> repository.delete(template));
    assertThat(exception.getMessage(), is("JSON or content or both files are missing for document template with id an identifier"));
  }

  @DisplayName("Deleting a document template should work")
  @Test
  void delete() {
    create();
    final DocumentTemplate template = new DocumentTemplate(DEFAULT_TEMPLATE);
    final File contentFile = template.getContentFilePath().toFile();
    final File jsonFile = template.getDescriptorFilePath().toFile();
    assertThat(contentFile.exists(), is(true));
    assertThat(jsonFile.exists(), is(true));
    repository.delete(template);
    assertThat(contentFile.exists(), is(false));
    assertThat(jsonFile.exists(), is(false));
  }

  @DisplayName("Stream all document templates should work")
  @Test
  void streamAll() {
    IntStream.rangeClosed(1, 5).forEach(i -> {
      try {
        DocumentTemplateTestUtil.createTemplateFile(i);
      } catch (IOException e) {
        throw new RuntimeException(e);
    }
    });
    final List<Integer> allPositions = IntStream.rangeClosed(1, 5)
        .mapToObj(Integer::valueOf)
        .collect(Collectors.toList());
    final List<DocumentTemplate> all = repository.streamAll().collect(Collectors.toList());
    assertThat(all, hasSize(5));
    all.forEach(t -> {
      final int position = t.getPosition();
      allPositions.remove(Integer.valueOf(position));
      assertThat(t.getId(), endsWith("[" + position+"]"));
      assertThat(t.getName("fr"), endsWith("[" + position+"]"));
      assertThat(t.getExtension(), is("txt"));
    });
    assertThat(allPositions, empty());
  }

  @DisplayName("Gets by its identifier a document templates should work")
  @Test
  void getById() {
    streamAll();
    final String id = repository.streamAll().map(DocumentTemplate::getId).findFirst().orElse(null);
    final Optional<DocumentTemplate> documentTemplate = repository.getById(id);
    assertThat(documentTemplate.isEmpty(), is(false));
    documentTemplate.ifPresent(t -> assertThat(t.getId(), is(id)));
  }
}