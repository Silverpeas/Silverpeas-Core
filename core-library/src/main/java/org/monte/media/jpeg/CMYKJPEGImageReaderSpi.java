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
package org.monte.media.jpeg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * A reader for JPEG images in the CMYK color space.
 *
 * @author Werner Randelshofer
 * @version 1.2 2011-02-17 Removes support for JMF. <br>1.0 2010-07-23 Created.
 */
public class CMYKJPEGImageReaderSpi extends ImageReaderSpi {

  public CMYKJPEGImageReaderSpi() {
    super("Werner Randelshofer",//vendor name
      "1.0",//version
      new String[]{"JPEG", "JPG"},//names
      new String[]{"jpg"},//suffixes,
      new String[]{"image/jpg"},// MIMETypes,
      "org.monte.media.jpeg.CMYKJPEGImageReader",// readerClassName,
      new Class[]{ImageInputStream.class, InputStream.class, byte[].class},// inputTypes,
      null,// writerSpiNames,
      false,// supportsStandardStreamMetadataFormat,
      null,// nativeStreamMetadataFormatName,
      null,// nativeStreamMetadataFormatClassName,
      null,// extraStreamMetadataFormatNames,
      null,// extraStreamMetadataFormatClassNames,
      false,// supportsStandardImageMetadataFormat,
      null,// nativeImageMetadataFormatName,
      null,// nativeImageMetadataFormatClassName,
      null,// extraImageMetadataFormatNames,
      null// extraImageMetadataFormatClassNames
      );
  }

  @Override
  public boolean canDecodeInput(Object source) throws IOException {
    if (source instanceof ImageInputStream) {
      ImageInputStream in = (ImageInputStream) source;
      in.mark();
      // Check if file starts with a JFIF SOI magic (0xffd8=-40)
      if (in.readShort() != -40) {
        in.reset();
        return false;
      }
      in.reset();
      return true;
    }
    return false;
  }

  @Override
  public ImageReader createReaderInstance(Object extension) throws IOException {
    return new CMYKJPEGImageReader(this);
  }

  @Override
  public String getDescription(Locale locale) {
    return "CMYK JPEG Image Reader";
  }
}
