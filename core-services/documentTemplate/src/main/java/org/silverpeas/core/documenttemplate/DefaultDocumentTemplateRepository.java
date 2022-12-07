/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.DeletingPathVisitor;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.silverpeas.core.util.ResourceLocator.getGeneralSettingBundle;
import static org.silverpeas.core.util.StringUtil.*;

/**
 * This {@link DocumentTemplateRepository} implementation registers the document templates into
 * $SILVERPEAS_HOME_DATA/documentTemplateRepository folder.
 * <p>
 * Data about a document template are registered using JSON structure.
 * </p>
 * <p>
 * A document template is registered with two files :
 *   <ul>
 *     <li>One file is representing the template content. The base name part of file is the
 *     identifier (an UUID) and the extension part is corresponding to the extension of the file
 *     to create</li>
 *     <li>An other file (a descriptor), a JSON one, contains additional data such as the name
 *     into different user languages. The base name part of file is the identifier (an UUID) and
 *     the extension is '.json'</li>
 *   </ul>
 * </p>
 * @author silveryocha
 */
@Repository
public class DefaultDocumentTemplateRepository implements DocumentTemplateRepository {

  @Override
  public Optional<DocumentTemplate> getById(final String id) {
    final DocumentTemplateVisitor visitor = new DocumentTemplateVisitor()
        .fileNameFilter(n -> getBaseName(n).equalsIgnoreCase(id))
        .walk();
    return visitor.contentPaths.stream().map(this::fetchJsonData).findFirst();
  }

  @Override
  public Stream<DocumentTemplate> streamAll() {
    final DocumentTemplateVisitor visitor = new DocumentTemplateVisitor().walk();
    return visitor.contentPaths.stream().map(this::fetchJsonData);
  }

  @Override
  public synchronized DocumentTemplate create(final DocumentTemplate documentTemplate,
      final InputStream content) {
    requireIdIsSet(documentTemplate);
    requireFileExtensionIsSet(documentTemplate);
    requireContent(documentTemplate, content);
    final DocumentTemplateCheckIntegrityVisitor integrityVisitor =
        new DocumentTemplateCheckIntegrityVisitor(documentTemplate).walk();
    integrityVisitor.requireNoFileExists();
    final DocumentTemplate toCreate = new DocumentTemplate(documentTemplate);
    toCreate.getJson().setCreatorId(User.getCurrentUser().getId());
    toCreate.getJson().setCreationInstant(Instant.now());
    final Path jsonPath = toCreate.getDescriptorFilePath();
    final Path contentPath = toCreate.getContentFilePath();
    try {
      Files.createDirectories(jsonPath.getParent());
      Files.write(jsonPath, toCreate.getJson().toString().getBytes());
      Files.copy(content, contentPath);
    } catch (IOException e) {
      throw new DocumentTemplateRuntimeException(e);
    }
    return toCreate;
  }

  @Override
  public synchronized DocumentTemplate update(final DocumentTemplate documentTemplate,
      final InputStream content) {
    requireIdIsSet(documentTemplate);
    requireFileExtensionIsSet(documentTemplate);
    final DocumentTemplateCheckIntegrityVisitor integrityVisitor =
        new DocumentTemplateCheckIntegrityVisitor(documentTemplate).walk();
    integrityVisitor.requireFileExists();
    final DocumentTemplate toUpdate = new DocumentTemplate(documentTemplate);
    toUpdate.getJson().setLastUpdaterId(User.getCurrentUser().getId());
    toUpdate.getJson().setLastUpdateInstant(Instant.now());
    if (content != null) {
      try {
        final Path contentPath = toUpdate.getContentFilePath();
        if (getExtension(contentPath.toString()).equalsIgnoreCase(integrityVisitor.getContentFileExtension())) {
          DeletingPathVisitor.deleteQuietly(contentPath);
        }
        Files.copy(content, contentPath);
      } catch (IOException e) {
        throw new DocumentTemplateRuntimeException(e);
      }
    } else {
      toUpdate.setExtension(integrityVisitor.getContentFileExtension());
    }
    final Path jsonPath = toUpdate.getDescriptorFilePath();
    try {
      Files.createDirectories(jsonPath.getParent());
      Files.write(jsonPath, toUpdate.getJson().toString().getBytes());
    } catch (IOException e) {
      throw new DocumentTemplateRuntimeException(e);
    }
    return toUpdate;
  }

  @Override
  public synchronized void delete(final DocumentTemplate documentTemplate) {
    requireIdIsSet(documentTemplate);
    requireFileExtensionIsSet(documentTemplate);
    final DocumentTemplateCheckIntegrityVisitor integrityVisitor =
        new DocumentTemplateCheckIntegrityVisitor(documentTemplate).walk();
    integrityVisitor.requireFullIntegrity();
    Stream.of(documentTemplate.getDescriptorFilePath(), documentTemplate.getContentFilePath())
        .forEach(DeletingPathVisitor::deleteQuietly);
  }

  private void requireIdIsSet(final DocumentTemplate documentTemplate) {
    if (isNotDefined(documentTemplate.getId())) {
      throw new DocumentTemplateRuntimeException("Document template has no id set");
    }
  }

  private void requireFileExtensionIsSet(final DocumentTemplate documentTemplate) {
    if (StringUtil.isNotDefined(documentTemplate.getExtension())) {
      throw new DocumentTemplateRuntimeException(
          format("File extension MUST exists for document template with id {0}",
              documentTemplate.getId()));
    }
  }

  private void requireContent(final DocumentTemplate documentTemplate, InputStream content) {
    if (content == null) {
      throw new DocumentTemplateRuntimeException(
          format("Content MUST exists for document template with id {0}",
              documentTemplate.getId()));
    }
  }

  private DocumentTemplate fetchJsonData(final Path content) {
    final String contentPathAsString = content.toString();
    final String fileBaseName = getBaseName(contentPathAsString);
    final String fileExtension = getExtension(contentPathAsString);
    final Path jsonDescriptor = Paths.get(content.getParent().toString(), fileBaseName + ".json");
    return new DocumentTemplate(JsonDocumentTemplate.decode(jsonDescriptor), fileExtension);
  }

  private static class DocumentTemplateVisitor extends AbstractDocumentTemplateVisitor {

    private final List<Path> contentPaths = new ArrayList<>();
    private Predicate<String> contentFileNameFilter;

    DocumentTemplateVisitor fileNameFilter(final Predicate<String> filter) {
      this.contentFileNameFilter = filter;
      return this;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
        throws IOException {
      super.visitFile(file, attrs);
      final String fileName = file.toString();
      final String fileExtension = getExtension(fileName);
      if (!fileExtension.equalsIgnoreCase("json") &&
          (contentFileNameFilter == null || contentFileNameFilter.test(fileName))) {
        contentPaths.add(file);
        if (contentFileNameFilter != null) {
          return FileVisitResult.TERMINATE;
        }
      }
      return FileVisitResult.CONTINUE;
    }
  }

  private static class DocumentTemplateCheckIntegrityVisitor
      extends AbstractDocumentTemplateVisitor {

    private final DocumentTemplate template;
    private boolean jsonFileExists = false;
    private boolean contentFileExists = false;
    private String contentFileExtension = EMPTY;

    private DocumentTemplateCheckIntegrityVisitor(final DocumentTemplate template) {
      this.template = template;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
        throws IOException {
      super.visitFile(file, attrs);
      final String fileBaseName = getBaseName(file.toString());
      final String fileExtension = getExtension(file.toString());
      if (fileBaseName.equalsIgnoreCase(template.getId())) {
        if (!fileExtension.equalsIgnoreCase("json")) {
          contentFileExists = true;
          contentFileExtension = fileExtension;
        } else {
          jsonFileExists = true;
        }
      }
      return jsonFileExists && contentFileExists && isDefined(contentFileExtension) ?
          FileVisitResult.TERMINATE :
          FileVisitResult.CONTINUE;
    }

    String getContentFileExtension() {
      return contentFileExtension;
    }

    void requireNoFileExists() {
      if (jsonFileExists || contentFileExists) {
        throw new DocumentTemplateRuntimeException(
            format("JSON or content or both files already exist for document template with id {0}",
                template.getId()));
      }
    }

    void requireFileExists() {
      if (!jsonFileExists || !contentFileExists) {
        throw new DocumentTemplateRuntimeException(
            format("JSON or content or both files are missing for document template with id {0}",
                template.getId()));
      }
    }

    void requireFullIntegrity() {
      requireFileExists();
      if (!contentFileExtension.equalsIgnoreCase(template.getExtension())) {
        throw new DocumentTemplateRuntimeException(format(
            "Extension of content file ''{0}'' is not the one expected ''{1}'' for document " +
                "template with id {2}",
            contentFileExtension, template.getExtension(), template.getId()));
      }
    }
  }

  private abstract static class AbstractDocumentTemplateVisitor extends SimpleFileVisitor<Path> {

    @SuppressWarnings("unchecked")
    <T extends AbstractDocumentTemplateVisitor> T walk() {
      try {
        final Path rootPath = getDocumentTemplateRepositoryPath();
        if (Files.exists(rootPath)) {
          Files.walkFileTree(rootPath, this);
        }
      } catch (IOException e) {
        SilverLogger.getLogger(this).error(e);
      }
      return (T) this;
    }
  }

  static Path getDocumentTemplateRepositoryPath() {
    return Paths.get(getGeneralSettingBundle().getString("dataHomePath"),
        "documentTemplateRepository");
  }
}
