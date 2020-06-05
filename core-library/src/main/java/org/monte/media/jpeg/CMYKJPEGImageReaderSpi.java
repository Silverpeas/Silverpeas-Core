/* @(#)CMYKJPEGImageReaderSpi.java  1.2  2011-02-17
 *
 * Copyright (c) 2010-2011 Werner Randelshofer, Switzerland.
 * You may only use this file in compliance with the accompanying license terms.
 */
package org.monte.media.jpeg;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;


/**
 * A reader for JPEG images in the CMYK color space.
 *
 * @author Werner Randelshofer
 * @version 1.2 2011-02-17 Removes support for JMF.
 * <br>1.0 2010-07-23 Created.
 */
public class CMYKJPEGImageReaderSpi extends ImageReaderSpi {

  public CMYKJPEGImageReaderSpi() {
    super("Werner Randelshofer",//vendor name
        "1.0",//version
        new String[]{"JPEG","JPG"},//names
        new String[]{"jpg"},//suffixes,
        new String[]{"image/jpg"},// MIMETypes,
        "org.monte.media.jpeg.CMYKJPEGImageReader",// readerClassName,
        new Class<?>[]{ImageInputStream.class,InputStream.class,byte[].class},// inputTypes,
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
