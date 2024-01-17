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

package org.silverpeas.core.admin.domain.driver.googledriver;

import com.google.api.client.json.GenericJson;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author silveryocha
 */
class GoogleEntitySimpleAttributePathResolver {

  private static final Pattern PATH_PART_DECODER = Pattern.compile("(?i)^\\s*(\\S+)\\s*");

  static <T extends GenericJson> Object resolve(final T entity, final String attributePath) {
    return resolve(entity, decodePath(attributePath));
  }

  static <T extends GenericJson> Object resolve(final T entity,
      final AttributePathDecoder decoder) {
    return resolvePath(entity, decoder, 0, entity);
  }

  private GoogleEntitySimpleAttributePathResolver() {
    throw new IllegalAccessError("Utility class");
  }

  private static <T extends GenericJson> Object resolvePath(final T entity,
      final AttributePathDecoder attributePathDecoder, final int pathLevel, final Object data) {
    if (data instanceof GenericJson) {
      final GenericJson json = (GenericJson) data;
      final String[] path = attributePathDecoder.getExplodedPath();
      final String pathPart = path[pathLevel];
      Object subData = json.get(pathPart);
      if (path.length == pathLevel + 1) {
        return decodeResult(subData);
      }
      if (subData instanceof List) {
        subData = ((List<?>) subData)
            .stream()
            .map(i -> decodeValue(entity, attributePathDecoder, i))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
      }
      return resolvePath(entity, attributePathDecoder, pathLevel + 1, subData);
    } else if (data instanceof List) {
      final Object result = ((List<?>) data)
          .stream()
          .flatMap(o -> Stream.of(resolvePath(entity, attributePathDecoder, pathLevel, o)))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
      return decodeResult(result);
    } else {
      throw new SilverpeasRuntimeException("unknown ");
    }
  }

  private static Object decodeResult(Object subData) {
    if (subData instanceof List) {
      final List<?> list = (List<?>) subData;
      if (list.size() == 1) {
        subData = list.get(0);
      } else if (list.isEmpty()) {
        subData = null;
      }
    }
    return subData;
  }

  @SuppressWarnings("unchecked")
  private static <T extends GenericJson> GenericJson decodeValue(final T entity,
      final AttributePathDecoder attributePathDecoder, final Object data) {
    final GenericJson result;
    if (!(data instanceof GenericJson) && data instanceof Map) {
      final GenericJson temp = new GenericJson();
      ((Map<String, Object>) data).forEach(temp::set);
      temp.set("id", entity.get("id"));
      result = temp;
    } else if ("customSchemas".equals(attributePathDecoder.getExplodedPath()[0]) && data == null) {
      result = new GenericJson();
    } else {
      result = null;
    }
    return result;
  }

  /**
   * Decodes the attribute path.
   * @param attributePath attribute path.
   * @return an {@link AttributePathDecoder} instance.
   */
  static AttributePathDecoder decodePath(final String attributePath) {
    AttributePathDecoder attrPathDecoder = new SimpleAttributePathDecoder(attributePath);
    if (!attrPathDecoder.isMatching()) {
      final String message = "attribute path '" + attributePath + "' is not correct !";
      SilverLogger.getLogger(GoogleEntitySimpleAttributePathResolver.class).error(message);
      throw new SilverpeasRuntimeException(message);
    }
    return attrPathDecoder;
  }

  abstract static class AttributePathDecoder {
    String path;
    String[] explodedPath;
    boolean match = false;

    AttributePathDecoder(final String attributePath) {
      this.path = attributePath;
      decode();
      this.explodedPath = path.split("[.]");
    }

    protected abstract void decode();

    public boolean isMatching() {
      return match;
    }

    public String getPath() {
      return path;
    }

    String[] getExplodedPath() {
      return explodedPath;
    }
  }

  static class SimpleAttributePathDecoder extends AttributePathDecoder {

    SimpleAttributePathDecoder(final String attributePath) {
      super(attributePath);
    }

    @Override
    protected void decode() {
      final Matcher matcher = PATH_PART_DECODER.matcher(getPath());
      if (matcher.find()) {
        this.path = matcher.group(1);
        explodedPath = path.split("[.]");
        match = true;
      }
    }
  }
}
