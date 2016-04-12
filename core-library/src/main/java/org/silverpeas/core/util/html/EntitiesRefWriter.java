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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.cyberneko.html.HTMLEntities;
import org.cyberneko.html.filters.Writer;

public class EntitiesRefWriter extends Writer {

  /** Constructs a writer filter that prints to standard out. */
  public EntitiesRefWriter() {
    super();
  }

  public void setWriter(java.io.Writer writer, String encoding) {
    this.fEncoding = encoding;
    if (writer instanceof PrintWriter) {
      fPrinter = (PrintWriter) writer;
    } else {
      fPrinter = new PrintWriter(writer);
    }
  }

  /**
   * Constructs a writer filter using the specified output stream and encoding.
   * @param outputStream The output stream to write to.
   * @param encoding The encoding to be used for the output. The encoding name should be an official
   * IANA encoding name.
   */
  public EntitiesRefWriter(OutputStream outputStream, String encoding)
      throws UnsupportedEncodingException {
    this(new OutputStreamWriter(outputStream, encoding), encoding);
  }

  /**
   * Constructs a writer filter using the specified Java writer and encoding.
   * @param writer The Java writer to write to.
   * @param encoding The encoding to be used for the output. The encoding name should be an official
   * IANA encoding name.
   */
  public EntitiesRefWriter(java.io.Writer writer, String encoding) {
    super(writer, encoding);
  }

  @Override
  protected void printEntity(String name) {
    char entity = (char) HTMLEntities.get(name);
    if (Character.isWhitespace(entity) || "nbsp".equalsIgnoreCase(name)) {
      entity = ' ';
    }
    super.fPrinter.print(entity);
    super.fPrinter.flush();
  }
}