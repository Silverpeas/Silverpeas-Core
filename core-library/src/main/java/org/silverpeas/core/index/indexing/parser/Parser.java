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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.indexing.parser;

import java.io.Reader;
import java.util.Optional;

/**
 * A parser is used to retrieve the text content of a file.
 */
public interface Parser {

  interface Metadata {
    Optional<String> getValue(final String key);
  }

  /**
   * The paser context ensures to provide a {@link Reader} instance with also {@link Metadata}.
   */
  class Context {
    private final Reader reader;
    private final Metadata metadata;

    public Context(final Reader reader, final Metadata metadata) {
      this.reader = reader;
      this.metadata = metadata;
    }

    public Reader getReader() {
      return reader;
    }

    public Metadata getMetadata() {
      return metadata;
    }
  }

  /**
   * @return a {@link Context} instance providing a {@link Reader} and {@link Metadata} about the
   * file.
   */
  Context getContext(String path, String encoding);
}
