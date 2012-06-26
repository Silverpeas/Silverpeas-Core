/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.attachment.repository;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.util.ArrayUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static javax.jcr.Property.JCR_CONTENT;
import static javax.jcr.Property.JCR_DATA;

/**
 * Wrapper of JCR Binary Object for manipulating the content.
 *
 * @author ehugonnet
 */
public class BinaryInputStream extends InputStream {

  private final Binary binary;
  private final InputStream in;
  private final Session session;

  public BinaryInputStream(String path) throws IOException {
    try {
      session = BasicDaoFactory.getSystemSession();
      String relativePath = path;
      if(path.startsWith("/")) {
        relativePath = path.substring(1);
      }
      Node rootNode = session.getRootNode();
      if (session.itemExists(path) && session.itemExists(path + '/' + JCR_CONTENT)) {
        Node contentNode = rootNode.getNode(relativePath + '/' + JCR_CONTENT);
        binary = contentNode.getProperty(JCR_DATA).getBinary();
        in = binary.getStream();
      } else {
        binary = null;
        in = new ByteArrayInputStream(ArrayUtil.EMPTY_BYTE_ARRAY);
      }
    } catch (RepositoryException rex) {
      throw new IOException(rex);
    }
  }
  
  

  @Override
  public int read() throws IOException {
    return in.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return in.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return in.read(b, off, len);
  }

  @Override
  public long skip(long n) throws IOException {
    return in.skip(n);
  }

  @Override
  public int available() throws IOException {
    return in.available();
  }

  @Override
  public void close() throws IOException {
    try {
      in.close();
    } finally {
      if (binary != null) {
        binary.dispose();
      }
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public synchronized void mark(int readlimit) {
    in.mark(readlimit);
  }

  @Override
  public synchronized void reset() throws IOException {
    in.reset();
  }

  @Override
  public boolean markSupported() {
    return in.markSupported();
  }
}
