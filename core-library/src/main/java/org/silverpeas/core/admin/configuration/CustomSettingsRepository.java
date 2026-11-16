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

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.kernel.SilverpeasResourcesLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/** Merges selected settings into customer installation scripts, independently of runtime writes. */
@Repository
public class CustomSettingsRepository {
  private static final String DEFAULT_NAME = "CustomerSettings.xml";
  private static final String ABSENT = "absent";
  private final Path propertiesRoot;
  private final Path home;
  private final Path directory;

  public CustomSettingsRepository() {
    this(SilverpeasResourcesLocation.getInstance().getConfigurationFilesRootPath());
  }

  CustomSettingsRepository(Path propertiesRoot) {
    this.propertiesRoot = propertiesRoot.toAbsolutePath().normalize();
    home = this.propertiesRoot.getParent();
    directory = home.resolve("configuration/silverpeas");
  }

  /**
   * Discovers custom scripts in setup execution order, without returning their values.
   * @return existing targets, or a proposed CustomerSettings.xml when none exists
   * @throws IOException if a candidate XML cannot be read or parsed
   */
  public List<CustomSettingsTarget> list() throws IOException {
    checkDirectory();
    var targets = new ArrayList<CustomSettingsTarget>();
    if (Files.exists(directory, NOFOLLOW_LINKS)) {
      try (var files = Files.list(directory)) {
        for (var path : files.sorted().toList()) {
          if (isCandidate(path.getFileName().toString()) && Files.isRegularFile(path, NOFOLLOW_LINKS)) {
            var content = Files.readAllBytes(path);
            if (isCustom(parse(content))) {
              targets.add(new CustomSettingsTarget(path.getFileName().toString(), revision(content), true));
            }
          }
        }
      }
    }
    return targets.isEmpty() ? List.of(new CustomSettingsTarget(DEFAULT_NAME, ABSENT, false))
        : List.copyOf(targets);
  }

  /**
   * Persists only selected, already saved runtime values. Unrelated XML rules are retained.
   * @param target target discovered by {@link #list()}, including its revision
   * @param propertiesPath slash-separated path relative to the properties root
   * @param values raw values of existing selected properties
   * @return the updated XML revision
   * @throws IOException if parsing or atomic replacement fails
   * @throws ConcurrentModificationException if the file changed or a later script overrides it
   */
  public synchronized CustomSettingsTarget persist(CustomSettingsTarget target, String propertiesPath,
      Map<String, String> values) throws IOException {
    var file = validateTarget(target);
    var propertyFile = resolveProperty(propertiesPath);
    if (values == null || values.isEmpty() || values.entrySet().stream()
        .anyMatch(entry -> !isXmlText(entry.getKey()) || !isXmlText(entry.getValue()))) {
      throw new IllegalArgumentException("A non-empty selection of properties is required");
    }
    var targets = list();
    if (!targets.contains(target)) {
      throw new ConcurrentModificationException("Custom settings changed; refresh the XML list");
    }
    checkLaterOverrides(target, targets, propertyFile, values);
    var original = target.exists() ? Files.readAllBytes(file) : null;
    checkRevision(target, original);
    var document = original == null ? newDocument() : parse(original);
    merge(document, propertyFile, propertiesPath, values);
    var content = serialize(document, original == null);
    Files.createDirectories(directory);
    var temporary = Files.createTempFile(directory, ".custom-settings-", ".tmp");
    try {
      if (target.exists()) {
        var view = Files.getFileAttributeView(file, PosixFileAttributeView.class, NOFOLLOW_LINKS);
        if (view != null) {
          Files.setPosixFilePermissions(temporary, view.readAttributes().permissions());
        }
      }
      Files.write(temporary, content);
      validateTarget(target);
      checkRevision(target, Files.exists(file, NOFOLLOW_LINKS) ? Files.readAllBytes(file) : null);
      Files.move(temporary, file, ATOMIC_MOVE, REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(temporary);
    }
    return new CustomSettingsTarget(target.name(), revision(content), true);
  }

  private void checkDirectory() {
    if (Files.isSymbolicLink(home.resolve("configuration")) || Files.isSymbolicLink(directory)) {
      throw new IllegalArgumentException("Symbolic links are not allowed in configuration paths");
    }
  }

  private Path validateTarget(CustomSettingsTarget target) {
    checkDirectory();
    if (target == null || target.name() == null || target.name().contains("/") ||
        target.name().contains("\\") || !isCandidate(target.name())) {
      throw new IllegalArgumentException("Invalid custom settings filename");
    }
    var file = directory.resolve(target.name());
    if (Files.isSymbolicLink(file) ||
        (Files.exists(file, NOFOLLOW_LINKS) && !Files.isRegularFile(file, NOFOLLOW_LINKS))) {
      throw new IllegalArgumentException("A regular XML file is required");
    }
    return file;
  }

  private static boolean isCandidate(String name) {
    return name.toLowerCase(java.util.Locale.ROOT).endsWith(".xml") && !name.startsWith("00-");
  }

  private static boolean isCustom(Document document) {
    var root = document.getDocumentElement();
    return "silverpeas-settings".equals(root.getTagName()) &&
        !"Silverpeas".equalsIgnoreCase(root.getAttribute("product"));
  }

  private Path resolveProperty(String relativePath) {
    if (!isXmlText(relativePath) || relativePath.isBlank() || relativePath.contains("\\")) {
      throw new IllegalArgumentException("A relative properties path is required");
    }
    var path = Path.of(relativePath);
    var resolved = propertiesRoot.resolve(path).normalize();
    if (path.isAbsolute() || !path.normalize().equals(path) || !resolved.startsWith(propertiesRoot) ||
        !relativePath.endsWith(".properties")) {
      throw new IllegalArgumentException("Invalid properties path");
    }
    return resolved;
  }

  private void checkLaterOverrides(CustomSettingsTarget target, List<CustomSettingsTarget> targets,
      Path propertyFile, Map<String, String> values) throws IOException {
    for (var later : targets) {
      if (later.exists() && later.name().compareTo(target.name()) > 0) {
        var document = parse(Files.readAllBytes(directory.resolve(later.name())));
        var overridden = matchingFiles(document, propertyFile).stream()
            .flatMap(config -> children(config, "parameter").stream()).anyMatch(parameter ->
                values.containsKey(parameter.getAttribute("key")) &&
                    !values.get(parameter.getAttribute("key")).equals(parameter.getTextContent()));
        if (overridden) {
          throw new ConcurrentModificationException("A later custom XML overrides selected settings");
        }
      }
    }
  }

  private List<Element> matchingFiles(Document document, Path propertyFile) {
    var matches = new ArrayList<Element>();
    for (var fileset : children(document.getDocumentElement(), "fileset")) {
      var root = fileset.getAttribute("root").replace("${SILVERPEAS_HOME}", home.toString());
      if (root.contains("${") || root.isBlank()) {
        continue;
      }
      for (var config : children(fileset, "configfile")) {
        if (Path.of(root).isAbsolute() && Path.of(root).resolve(config.getAttribute("name"))
            .normalize().equals(propertyFile)) {
          matches.add(config);
        }
      }
    }
    return matches;
  }

  private void merge(Document document, Path propertyFile, String relativePath, Map<String, String> values) {
    var matches = matchingFiles(document, propertyFile);
    if (matches.isEmpty()) {
      var fileset = document.createElement("fileset");
      fileset.setAttribute("root", "${SILVERPEAS_HOME}/properties");
      document.getDocumentElement().appendChild(fileset);
      var config = document.createElement("configfile");
      config.setAttribute("name", relativePath);
      fileset.appendChild(config);
      matches = List.of(config);
    }
    for (var config : matches) {
      values.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
        var parameters = children(config, "parameter").stream()
            .filter(parameter -> entry.getKey().equals(parameter.getAttribute("key"))).toList();
        if (parameters.isEmpty()) {
          var parameter = document.createElement("parameter");
          parameter.setAttribute("key", entry.getKey());
          config.appendChild(parameter);
          parameters = List.of(parameter);
        }
        parameters.forEach(parameter -> parameter.setTextContent(entry.getValue()));
      });
    }
  }

  private static List<Element> children(Element parent, String tag) {
    var result = new ArrayList<Element>();
    var nodes = parent.getChildNodes();
    for (var i = 0; i < nodes.getLength(); i++) {
      if (nodes.item(i) instanceof Element element && tag.equals(element.getTagName())) {
        result.add(element);
      }
    }
    return result;
  }

  private static boolean isXmlText(String value) {
    return value != null && value.codePoints().allMatch(code -> code == 9 || code == 10 || code == 13 ||
        (code >= 0x20 && code <= 0xD7FF) || (code >= 0xE000 && code <= 0xFFFD) ||
        (code >= 0x10000 && code <= 0x10FFFF));
  }

  private static DocumentBuilder builder() throws ParserConfigurationException {
    var factory = DocumentBuilderFactory.newDefaultInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    factory.setXIncludeAware(false);
    factory.setExpandEntityReferences(false);
    var builder = factory.newDocumentBuilder();
    builder.setErrorHandler(new DefaultHandler() {
      @Override
      public void fatalError(org.xml.sax.SAXParseException e) throws SAXException {
        throw e;
      }
    });
    return builder;
  }

  private static Document newDocument() throws IOException {
    try {
      var document = builder().newDocument();
      var root = document.createElement("silverpeas-settings");
      root.setAttribute("product", "Custom");
      document.appendChild(root);
      return document;
    } catch (ParserConfigurationException e) {
      throw new IOException("Cannot create custom settings XML", e);
    }
  }

  private static Document parse(byte[] content) throws IOException {
    try (var input = new ByteArrayInputStream(content)) {
      return builder().parse(input);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IOException("Cannot parse custom settings XML", e);
    }
  }

  private static byte[] serialize(Document document, boolean newFile) throws IOException {
    try (var output = new ByteArrayOutputStream()) {
      var factory = TransformerFactory.newDefaultInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
      var transformer = factory.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING,
          document.getXmlEncoding() == null ? "UTF-8" : document.getXmlEncoding());
      transformer.setOutputProperty(OutputKeys.INDENT, newFile ? "yes" : "no");
      transformer.transform(new DOMSource(document), new StreamResult(output));
      return output.toByteArray();
    } catch (TransformerException e) {
      throw new IOException("Cannot serialize custom settings XML", e);
    }
  }

  private static void checkRevision(CustomSettingsTarget target, byte[] content) {
    var actual = content == null ? ABSENT : revision(content);
    if (!actual.equals(target.revision())) {
      throw new ConcurrentModificationException("Custom settings changed; refresh before saving");
    }
  }

  private static String revision(byte[] content) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is required by Java", e);
    }
  }
}
