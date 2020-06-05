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
package org.monte.media.io;

import java.io.IOException;
import java.nio.ByteOrder;
import javax.imageio.stream.ImageInputStreamImpl;

/**
 * {@code ImageInputStreamImpl2} fixes bugs in ImageInputStreamImpl. <p> ImageInputStreamImpl uses
 * read(byte[]) instead of readFully(byte[]) inside of readShort. This results in corrupt data input
 * if the underlying stream can not fulfill the read operation in a single step.
 *
 * @author Werner Randelshofer
 * @version $Id: ImageInputStreamImpl2.java 134 2011-12-02 16:23:00Z werner $
 */
public abstract class ImageInputStreamImpl2 extends ImageInputStreamImpl {
  // Length of the buffer used for readFully(type[], int, int)

  private static final int BYTE_BUF_LENGTH = 8192;
  /**
   * Byte buffer used for readFully(type[], int, int). Note that this array is also used for bulk
   * reads in readShort(), readInt(), etc, so it should be large enough to hold a primitive value
   * (i.e. >= 8 bytes). Also note that this array is package protected, so that it can be used by
   * ImageOutputStreamImpl in a similar manner.
   */
  byte[] byteBuf = new byte[BYTE_BUF_LENGTH];

  @Override
  public short readShort() throws IOException {
    readFully(byteBuf, 0, 2);

    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      return (short) (((byteBuf[0] & 0xff) << 8) | ((byteBuf[1] & 0xff) << 0));
    } else {
      return (short) (((byteBuf[1] & 0xff) << 8) | ((byteBuf[0] & 0xff) << 0));
    }
  }

  public int readInt() throws IOException {
    readFully(byteBuf, 0, 4);

    if (byteOrder == ByteOrder.BIG_ENDIAN) {
      return (((byteBuf[0] & 0xff) << 24) | ((byteBuf[1] & 0xff) << 16)
        | ((byteBuf[2] & 0xff) << 8) | ((byteBuf[3] & 0xff) << 0));
    } else {
      return (((byteBuf[3] & 0xff) << 24) | ((byteBuf[2] & 0xff) << 16)
        | ((byteBuf[1] & 0xff) << 8) | ((byteBuf[0] & 0xff) << 0));
    }
  }
}
