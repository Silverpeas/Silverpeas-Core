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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import static java.text.MessageFormat.format;
import static org.silverpeas.kernel.bundle.ResourceLocator.getGeneralSettingBundle;

/**
 * @author silveryocha
 */
public class DocumentTemplateTestUtil {

  final static OffsetDateTime DEFAULT_CREATION_DATE = OffsetDateTime.parse("2022-06-15T14:35:06.700176+02:00");
  final static Instant DEFAULT_CREATION_INSTANT = DEFAULT_CREATION_DATE.toInstant();
  final static String DEFAULT_JSON = "{" +
      "\"id\":\"an identifier\"," +
      "\"nameTranslations\":{" +
        "\"en\":\"This is a test\"," +
        "\"fr\":\"Ceci est un test\"" +
      "}," +
      "\"descriptionTranslations\":{}," +
      "\"position\":3," +
      "\"creatorId\":\"1\"," +
      "\"creationInstant\":\"" + DEFAULT_CREATION_DATE + "\"," +
      "\"lastUpdaterId\":\"1\"," +
      "\"lastUpdateInstant\":\"" + DEFAULT_CREATION_DATE + "\"}";

  static DocumentTemplate createTemplateInstance(final int position) {
    final String fileNamePrefix = UUID.randomUUID() + "[" + position + "]";
    final DocumentTemplate template = new DocumentTemplate();
    template.setId(fileNamePrefix);
    template.setPosition(position);
    return template;
  }

  static void createTemplateFile(final int position) throws IOException {
    final String fileNamePrefix = UUID.randomUUID() + "[" + position + "]";
    final String contentPattern = "This is the content of [{0}]";
    final String jsonPattern = "'{'" +
        "\"id\":\"{0}\"," +
        "\"nameTranslations\":'{'" +
          "\"en\":\"This is a test [{1}]\"," +
          "\"fr\":\"Ceci est un test [{1}]\"'}'," +
        "\"position\":{1}'}'";
    final Path contentPath = getRepositoryTemplatePath(fileNamePrefix + ".txt");
    Files.createDirectories(contentPath.getParent());
    Files.write(contentPath, format(contentPattern, position).getBytes());
    Files.write(getRepositoryTemplatePath(fileNamePrefix + ".json"),
        format(jsonPattern, fileNamePrefix, String.valueOf(position)).getBytes());
  }

  static Path getRepositoryPath() {
    return Paths.get(getGeneralSettingBundle().getString("dataHomePath"),
        "documentTemplateRepository");
  }

  static Path getRepositoryTemplatePath(String fileName) {
    return Paths.get(getGeneralSettingBundle().getString("dataHomePath"),
        "documentTemplateRepository", fileName);
  }
}
