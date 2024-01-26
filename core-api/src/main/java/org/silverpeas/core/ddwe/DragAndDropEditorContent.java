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

package org.silverpeas.core.ddwe;

import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.kernel.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.silverpeas.kernel.util.StringUtil.EMPTY;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

/**
 * Permits to centralization some content manipulation with Drag and Drop editor.
 * @author silveryocha
 */
public class DragAndDropEditorContent {
  final String content;
  final Map<String, String> json;

  @SuppressWarnings("unchecked")
  public DragAndDropEditorContent(final String content) {
    this.content = defaultStringIfNotDefined(content);
    json = getSimpleContent().isPresent() ?
        new HashMap<>() :
        ofNullable(content)
            .filter(StringUtil::isDefined)
            .map(f -> JSONCodec.decode(f, Map.class))
            .orElseGet(HashMap::new);
  }

  /**
   * Gets optional simple content.
   * <p>
   * Will be filled if the editor is initialized with a non JSON structure.
   * </p>
   * @return an option simple HTML content.
   */
  public Optional<String> getSimpleContent() {
    return of(content).filter(StringUtil::isDefined).filter(not(c -> c.startsWith("{")));
  }

  /**
   * Sets a temporary inlined HTML content.
   * <p>
   * A temporary inlined HTML content is taken into account by the functionality that detect a
   * potential loss od modified data.
   * </p>
   * @param html an inlined HTML as string.
   */
  public void setTemporaryInlinedHtml(final String html) {
    json.put("gjs-tmp-inlinedHtml", defaultStringIfNotDefined(html));
  }

  /**
   * Gets the inlined HTML of the current content.
   * <p>
   *   The inlined HTML content is the HTML which HTML and CSS have been performed in order to
   *   have hte style computed on each HTML element.
   * </p>
   * @return an inlined HTML content.
   */
  public String getInlinedHtml() {
    return getSimpleContent().orElseGet(() -> json.getOrDefault("gjs-inlinedHtml", EMPTY));
  }

  /**
   * Gets the JSON content handled by Drag and Drop Editor.
   * @return a string representing a JSON structure.
   */
  public String getEncodedJson() {
    return JSONCodec.encode(json);
  }

  /**
   * Gets the content from which this instance has been initialized.
   * @return string representing the initialized content.
   */
  public String getInitialRawContent() {
    return content;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DragAndDropEditorContent that = (DragAndDropEditorContent) o;
    return content.equals(that.content) && json.equals(that.json);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, json);
  }
}
