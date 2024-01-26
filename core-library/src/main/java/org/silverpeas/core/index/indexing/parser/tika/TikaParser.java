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
package org.silverpeas.core.index.indexing.parser.tika;

import org.apache.tika.Tika;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.core.index.indexing.parser.DefaultParser;
import org.silverpeas.core.index.indexing.parser.Parser;
import org.silverpeas.kernel.util.StringUtil;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.silverpeas.core.index.indexing.IndexingLogger.indexingLogger;

@Technical
@Bean
@Named("tikaParser")
@DefaultParser
public class TikaParser implements Parser {

  private Tika tika;

  @PostConstruct
  private void initTika() {
    tika = new Tika();
  }

  @Override
  public Context getContext(String path, String encoding) {
    final org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();
    try {
      // open a reader that can be passed into the parsing context for further treatment(s)
      Reader reader = tika.parse(new File(path), metadata);
      return new Context(reader, new TikaMetadata(metadata));
    } catch (IOException ex) {
      indexingLogger().error(ex.getMessage(), ex);
    }
    return new Context(new StringReader(""), new TikaMetadata(null));
  }

  private static class TikaMetadata implements Metadata {
    private final org.apache.tika.metadata.Metadata metadata;

    private TikaMetadata(final org.apache.tika.metadata.Metadata metadata) {
      this.metadata = metadata;
    }

    @Override
    public Optional<String> getValue(final String key) {
      return metadata == null
          ? empty()
          : ofNullable(metadata.get(key)).filter(StringUtil::isDefined);
    }
  }
}
