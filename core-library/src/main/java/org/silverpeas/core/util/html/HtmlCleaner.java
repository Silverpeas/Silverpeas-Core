/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.util.html;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.apache.commons.lang3.CharEncoding;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.HTMLTagBalancer;
import org.cyberneko.html.filters.ElementRemover;

/**
 * Remove all tags from a HTML fragment - all entities are replaced by their value.
 * @author sfariello
 */
public class HtmlCleaner {

  private XMLParserConfiguration parser;
  private EntitiesRefWriter writer;

  public HtmlCleaner() {
    ElementRemover remover = new ElementRemover();
    remover.removeElement("script");
    remover.acceptElement("", null);
    remover.getRecognizedFeatures();
    remover.getRecognizedProperties();
    writer = new EntitiesRefWriter();
    XMLDocumentFilter[] filters = { new HTMLTagBalancer(), remover, writer };
    parser = new HTMLConfiguration();
    parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
    parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
  }

  /**
   * Remove all tags from a HTML fragment - all entities are replaced by their value.
   * @param html a HTML fragment.
   * @return a String where all tags from a HTML fragment are removed and all entities are replaced
   * by their value.
   * @throws IOException
   */
  public String cleanHtmlFragment(String html) throws IOException {
    StringWriter content = new StringWriter();
    writer.setWriter(content, CharEncoding.UTF_8);
    XMLInputSource source = new XMLInputSource("-//W3C//DTD HTML 4.01", null, null, new StringReader(
        html), CharEncoding.UTF_8);
    parser.parse(source);
    return content.toString();
  }
}
