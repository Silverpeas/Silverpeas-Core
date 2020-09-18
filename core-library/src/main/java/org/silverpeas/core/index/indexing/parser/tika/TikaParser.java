/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.index.indexing.parser.tika;

import org.apache.tika.Tika;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.index.indexing.parser.DefaultParser;
import org.silverpeas.core.index.indexing.parser.Parser;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

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
  public Reader getReader(String path, String encoding) {
    try {
      return tika.parse(new File(path));
    } catch (IOException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return new StringReader("");
  }
}
