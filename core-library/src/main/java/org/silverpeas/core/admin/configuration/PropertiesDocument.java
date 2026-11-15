/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

package org.silverpeas.core.admin.configuration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Edits existing logical property lines while retaining comments and untouched physical lines.
 * Uses the same UTF-8 / ISO-8859-1 fallback as {@link java.util.PropertyResourceBundle}.
 */
final class PropertiesDocument {

  private final Charset charset;
  private final List<Entry> entries;
  private final Map<String, String> values;

  PropertiesDocument(byte[] content) throws IOException {
    charset = detectCharset(content);
    entries = parse(new String(content, charset));
    var properties = load(new String(content, charset));
    values = new TreeMap<>();
    properties.stringPropertyNames().forEach(key -> values.put(key, properties.getProperty(key)));
  }

  Map<String, String> values() {
    return values;
  }

  byte[] update(Map<String, String> changes) throws IOException {
    var content = new StringBuilder();
    for (var entry : entries) {
      if (entry.key() != null && changes.containsKey(entry.key())) {
        content.append(serialize(entry.key(), changes.get(entry.key())));
      } else {
        content.append(entry.content());
      }
    }
    return content.toString().getBytes(charset);
  }

  private static Charset detectCharset(byte[] content) {
    try {
      UTF_8.newDecoder().decode(ByteBuffer.wrap(content));
      return UTF_8;
    } catch (CharacterCodingException e) {
      return ISO_8859_1;
    }
  }

  private static List<Entry> parse(String content) throws IOException {
    var entries = new ArrayList<Entry>();
    var block = new StringBuilder();
    for (var line : content.split("(?<=\n)|(?<=\r)(?!\n)")) {
      block.append(line);
      var stripped = line.stripLeading();
      var comment = block.length() == line.length() &&
          (stripped.startsWith("#") || stripped.startsWith("!") || stripped.isBlank());
      if (comment || !continues(line)) {
        entries.add(entry(block.toString()));
        block.setLength(0);
      }
    }
    if (!block.isEmpty()) {
      entries.add(entry(block.toString()));
    }
    return entries;
  }

  private static boolean continues(String line) {
    var end = line.length();
    while (end > 0 && (line.charAt(end - 1) == '\n' || line.charAt(end - 1) == '\r')) {
      end--;
    }
    var backslashes = 0;
    while (end > 0 && line.charAt(end - 1) == '\\') {
      backslashes++;
      end--;
    }
    return backslashes % 2 != 0;
  }

  private static Entry entry(String content) throws IOException {
    var properties = load(content);
    return new Entry(content, properties.stringPropertyNames().stream().findFirst().orElse(null));
  }

  private static Properties load(String content) throws IOException {
    var properties = new Properties();
    try (var reader = new StringReader(content)) {
      properties.load(reader);
    }
    return properties;
  }

  private static String serialize(String key, String value) throws IOException {
    var properties = new Properties();
    properties.setProperty(key, value);
    try (var output = new ByteArrayOutputStream()) {
      // The stream overload escapes non-ASCII characters, independently of the document encoding.
      properties.store(output, null);
      var serialized = output.toString(ISO_8859_1);
      return serialized.substring(serialized.indexOf('\n') + 1);
    }
  }

  private record Entry(String content, String key) {
  }
}
