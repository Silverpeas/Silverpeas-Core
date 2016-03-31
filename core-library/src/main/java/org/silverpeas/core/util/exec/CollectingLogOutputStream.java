/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.util.exec;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.exec.LogOutputStream;

import static org.silverpeas.core.util.StringUtil.newline;

/**
 * Helper class to collectect the output of a command execution.
 */
public class CollectingLogOutputStream extends LogOutputStream {

  private final List<String> lines;

  public CollectingLogOutputStream() {
    this(new LinkedList<>());
  }

  public CollectingLogOutputStream(List<String> lines) {
    this.lines = lines;
  }

  @Override
  protected void processLine(String line, int level) {
    lines.add(line);
  }

  public List<String> getLines() {
    return Collections.unmodifiableList(lines);
  }

  public String getMessage() {
    StringBuilder builder = new StringBuilder(512 * lines.size());
    for (String line : lines) {
      builder.append(line);
      builder.append(newline);
    }
    return builder.toString();
  }
}
