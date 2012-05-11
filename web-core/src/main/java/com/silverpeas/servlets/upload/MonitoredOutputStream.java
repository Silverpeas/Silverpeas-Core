/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.servlets.upload;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An outpuutstream that registers an OutputStreamListener.
 * @author ehugonnet
 */
public class MonitoredOutputStream extends OutputStream {

  private OutputStream out;
  private OutputStreamListener listener;

  public MonitoredOutputStream(OutputStream out, OutputStreamListener listener) {
    this.out = out;
    this.listener = listener;
    this.listener.begin();
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
    listener.readBytes(1);
  }

  @Override
  public void close() throws IOException {
    out.close();
    listener.end();
  }

  @Override
  public void flush() throws IOException {
    out.flush();
  }

  @Override
  public void write(byte[] b) throws IOException {
    out.write(b);
    listener.readBytes(b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    listener.readBytes(len - off);
  }
}
