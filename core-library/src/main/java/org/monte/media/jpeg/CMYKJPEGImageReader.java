/* @(#)CMYJKJPEGImageReader.java
 *
 * Copyright (c) 2010-2011 Werner Randelshofer, Switzerland.
 * You may only use this file in compliance with the accompanying license terms.
 */
package org.monte.media.jpeg;

import org.monte.media.color.ICCPackedColorModel;
import org.monte.media.io.ByteArrayImageInputStream;
import org.monte.media.io.ImageInputStreamAdapter;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Reads a JPEG image with colors in the CMYK color space.
 *
 * @author Werner Randelshofer
 * @version $Id: CMYKJPEGImageReader.java 351 2016-10-23 15:15:55Z werner $
 */
public class CMYKJPEGImageReader extends ImageReader {

  private boolean isIgnoreICCProfile = false;
  /**
   * In JPEG files, YCCK and CMYK values are typically stored as inverted
   * values.
   */
  private boolean isInvertColors = true;
  private static final DirectColorModel RGB = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
  /**
   * When we read the header, we read the whole image.
   */
  private BufferedImage image;

  /**
   * This value is set to true, when we returned the image.
   */
  private boolean didReturnImage;

  public CMYKJPEGImageReader() {
    this(new CMYKJPEGImageReaderSpi());
  }

  public CMYKJPEGImageReader(ImageReaderSpi originatingProvider) {
    super(originatingProvider);
  }

  @Override
  public int getNumImages(boolean allowSearch) throws IOException {
    return 1;
  }

  @Override
  public int getWidth(int imageIndex) throws IOException {
    readHeader();
    return image.getWidth();
  }

  @Override
  public int getHeight(int imageIndex) throws IOException {
    readHeader();
    return image.getHeight();
  }

  @Override
  public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
    readHeader();
    LinkedList<ImageTypeSpecifier> l = new LinkedList<ImageTypeSpecifier>();
    l.add(new ImageTypeSpecifier(RGB, RGB.createCompatibleSampleModel(image.getWidth(), image.getHeight())));
    return l.iterator();
  }

  @Override
  public IIOMetadata getStreamMetadata() throws IOException {
    return null;
  }

  @Override
  public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    return null;
  }

  @Override
  public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
    if (imageIndex > 0) {
      throw new IndexOutOfBoundsException();
    }
    readHeader();
    didReturnImage = true;
    return image;
  }

  /**
   * Reads the PGM header. Does nothing if the header has already been loaded.
   */
  private void readHeader() throws IOException {
    if (image == null) {

      ImageInputStream iis = null;
      Object in = getInput();
      /* No need for JMF support in CMYKJPEGImageReader.
             if (in instanceof Buffer) {
             in = ((Buffer) in).getData();
             }*/

      if (in instanceof byte[]) {
        iis = new ByteArrayImageInputStream((byte[]) in);
      } else if (in instanceof ImageInputStream) {
        iis = (ImageInputStream) in;
      } else if (in instanceof InputStream) {
        iis = new MemoryCacheImageInputStream((InputStream) in);
      } else {
        throw new IOException("Can't handle input of type " + in);
      }
      didReturnImage = false;
      image = read(iis, isInvertColors, isIgnoreICCProfile);
    }
  }

  /**
   * @return the YCCKInversed property.
   */
  public boolean isInvertColors() {
    return isInvertColors;
  }

  /**
   * @param newValue the new value
   */
  public void setInvertColors(boolean newValue) {
    this.isInvertColors = newValue;
  }

  public boolean isIgnoreICCProfile() {
    return isIgnoreICCProfile;
  }

  public void setIgnoreICCProfile(boolean newValue) {
    this.isIgnoreICCProfile = newValue;
  }

  public static BufferedImage read(ImageInputStream in, boolean inverseYCCKColors, boolean isIgnoreColorProfile) throws IOException {
    // Seek to start of input stream
    in.seek(0);

    // Extract metadata from the JFIF stream.
    // --------------------------------------
    // In particular, we are interested into the following fields:
    int samplePrecision = 0;
    int numberOfLines = 0;
    int numberOfSamplesPerLine = 0;
    int numberOfComponentsInFrame = 0;
    int app14AdobeColorTransform = 0;
    ByteArrayOutputStream app2ICCProfile = new ByteArrayOutputStream();
    // Browse for marker segments, and extract data from those
    // which are of interest.
    JFIFInputStream fifi = new JFIFInputStream(new ImageInputStreamAdapter(in));
    for (JFIFInputStream.Segment seg = fifi.getNextSegment(); seg != null; seg = fifi.getNextSegment()) {
      if (0xffc0 <= seg.marker && seg.marker <= 0xffc3
          || 0xffc5 <= seg.marker && seg.marker <= 0xffc7
          || 0xffc9 <= seg.marker && seg.marker <= 0xffcb
          || 0xffcd <= seg.marker && seg.marker <= 0xffcf) {
        // SOF0 - SOF15: Start of Frame Header marker segment
        DataInputStream dis = new DataInputStream(fifi);
        samplePrecision = dis.readUnsignedByte();
        numberOfLines = dis.readUnsignedShort();
        numberOfSamplesPerLine = dis.readUnsignedShort();
        numberOfComponentsInFrame = dis.readUnsignedByte();
        // ...the rest of SOF header is not important to us.
        // In fact, by encountering a SOF header, we have reached
        // the end of the metadata section we are interested in.
        // Thus we can abort here.
        break;

      } else if (seg.marker == 0xffe2) {
        // APP2: Application-specific marker segment
        if (seg.length >= 26) {
          DataInputStream dis = new DataInputStream(fifi);
          // Check for 12-bytes containing the null-terminated string: "ICC_PROFILE".
          if (dis.readLong() == 0x4943435f50524f46L && dis.readInt() == 0x494c4500) {
            // Skip 2 bytes
            dis.skipBytes(2);

            // Read Adobe ICC_PROFILE int buffer. The profile is split up over
            // multiple APP2 marker segments.
            byte[] b = new byte[1024];
            for (int count = dis.read(b); count != -1; count = dis.read(b)) {
              app2ICCProfile.write(b, 0, count);
            }
          }
        }
      } else if (seg.marker == 0xffee) {
        // APP14: Application-specific marker segment
        if (seg.length == 12) {
          DataInputStream dis = new DataInputStream(fifi);
          // Check for 6-bytes containing the null-terminated string: "Adobe".
          if (dis.readInt() == 0x41646f62L && dis.readUnsignedShort() == 0x6500) {
            int version = dis.readUnsignedByte();
            int app14Flags0 = dis.readUnsignedShort();
            int app14Flags1 = dis.readUnsignedShort();
            app14AdobeColorTransform = dis.readUnsignedByte();
          }
        }
      }
    }
    //fifi.close();

    // Read the image data
    BufferedImage img = null;
    if (numberOfComponentsInFrame != 4) {
      // Read image with YCC color encoding.
      in.seek(0);
//            img = readImageFromYCCorGray(in);
      img = readRGBImageFromYCC(new ImageInputStreamAdapter(in), null);
    } else if (numberOfComponentsInFrame == 4) {

      // Try to instantiate an ICC_Profile from the app2ICCProfile
      ICC_Profile profile = null;
      if (!isIgnoreColorProfile && app2ICCProfile.size() > 0) {
        try {
          profile = ICC_Profile.getInstance(new ByteArrayInputStream(app2ICCProfile.toByteArray()));
        } catch (Throwable ex) {
          // icc profile is corrupt
          ex.printStackTrace();
        }
      }

      switch (app14AdobeColorTransform) {
        case 0:
        default:
          // Read image with RGBW color encoding.
          in.seek(0);

          if (inverseYCCKColors) {
            img = readImageFromInvertedCMYK(new ImageInputStreamAdapter(in), profile);
          } else {
            img = readImageFromCMYK(new ImageInputStreamAdapter(in), profile);
          }
          break;
        case 1:
          throw new IOException("YCbCr not supported");
        case 2:
          // Read image with inverted YCCK color encoding.
          // FIXME - How do we determine from the JFIF file whether
          // YCCK colors are inverted?

          // We must have a color profile in order to perform a
          // conersion from CMYK to RGB.
          // I case none has been supplied, we create a default one here.
          if (profile == null) {
            profile = ICC_Profile.getInstance(CMYKJPEGImageReader.class.getResourceAsStream("Generic CMYK Profile.icc"));
          }
          in.seek(0);
          if (inverseYCCKColors) {
            img = readImageFromInvertedYCCK(new ImageInputStreamAdapter(in), profile);
          } else {
            img = readImageFromYCCK(new ImageInputStreamAdapter(in), profile);
          }
          break;
      }
    }

    return img;
  }

  private static ImageReader createNativeJPEGReader() {
    for (ImageReader r : (Iterable<ImageReader>) () -> ImageIO.getImageReadersByFormatName("jpeg")) {
      if ("com.sun.imageio.plugins.jpeg.JPEGImageReader".equals(r.getClass().getName())) {
        return r;
      }
    }
    throw new InternalError("could not find native JPEG Reader");
  }

  /**
   * Reads a CMYK JPEG image from the provided InputStream, converting the
   * colors to RGB using the provided CMYK ICC_Profile. The image data must be
   * in the CMYK color space.
   * <p>
   * Use this method, if you have already determined that the input stream
   * contains a CMYK JPEG image.
   *
   * @param in An InputStream, preferably an ImageInputStream, in the JPEG File
   * Interchange Format (JFIF).
   * @param cmykProfile An ICC_Profile for conversion from the CMYK color space
   * to the RGB color space. If this parameter is null, a default profile is
   * used.
   * @return a BufferedImage containing the decoded image converted into the RGB
   * color space.
   * @throws java.io.IOException
   */
  public static BufferedImage readImageFromCMYK(InputStream in, ICC_Profile cmykProfile) throws IOException {
    ImageInputStream inputStream = null;
    ImageReader reader = createNativeJPEGReader();
    try {
      inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
      reader.setInput(inputStream);
      Raster raster = reader.readRaster(0, null);
      BufferedImage image = createImageFromCMYK(raster, cmykProfile);
      return image;
    } finally {
      reader.dispose();
    }
  }

  /**
   * Reads a RGBA JPEG image from the provided InputStream, converting the
   * colors to RGBA using the provided RGBA ICC_Profile. The image data must be
   * in the RGBA color space.
   * <p>
   * Use this method, if you have already determined that the input stream
   * contains a RGBA JPEG image.
   *
   * @param in An InputStream, preferably an ImageInputStream, in the JPEG File
   * Interchange Format (JFIF).
   * @param rgbaProfile An ICC_Profile for conversion from the RGBA color space
   * to the RGBA color space. If this parameter is null, a default profile is
   * used.
   * @return a BufferedImage containing the decoded image converted into the RGB
   * color space.
   * @throws java.io.IOException
   */
  public static BufferedImage readImageFromInvertedCMYK(InputStream in, ICC_Profile rgbaProfile) throws IOException {
    ImageInputStream inputStream = null;
    ImageReader reader = createNativeJPEGReader();
    try {
      inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
      reader.setInput(inputStream);
      Raster raster = reader.readRaster(0, null);
      BufferedImage image = createImageFromInvertedCMYK(raster, rgbaProfile);
      return image;
    } finally {
      reader.dispose();
    }
  }

  public static BufferedImage readImageFromRGB(InputStream in, ICC_Profile rgbaProfile) throws IOException {
    ImageInputStream inputStream = null;
    ImageReader reader = createNativeJPEGReader();
    try {
      inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
      reader.setInput(inputStream);
      Raster raster = reader.readRaster(0, null);
      BufferedImage image = createImageFromRGB(raster, rgbaProfile);
      return image;
    } finally {
      reader.dispose();
    }
  }

  public static BufferedImage readRGBImageFromYCC(InputStream in, ICC_Profile rgbaProfile) throws IOException {
    ImageInputStream inputStream = null;
    ImageReader reader = createNativeJPEGReader();
    try {
      inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
      reader.setInput(inputStream);
      Raster raster = reader.readRaster(0, null);
      BufferedImage image = createImageFromYCC(raster, rgbaProfile);
      return image;
    } finally {
      reader.dispose();
    }
  }

  /**
   * Reads a YCCK JPEG image from the provided InputStream, converting the
   * colors to RGB using the provided CMYK ICC_Profile. The image data must be
   * in the YCCK color space.
   * <p>
   * Use this method, if you have already determined that the input stream
   * contains a YCCK JPEG image.
   *
   * @param in An InputStream, preferably an ImageInputStream, in the JPEG File
   * Interchange Format (JFIF).
   * @param cmykProfile An ICC_Profile for conversion from the CMYK color space
   * to the RGB color space. If this parameter is null, a default profile is
   * used.
   * @return a BufferedImage containing the decoded image converted into the RGB
   * color space.
   * @throws java.io.IOException
   */
  public static BufferedImage readImageFromYCCK(InputStream in, ICC_Profile cmykProfile) throws IOException {
    ImageInputStream inputStream = null;
    ImageReader reader = createNativeJPEGReader();
    try {
      inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
      reader.setInput(inputStream);
      Raster raster = reader.readRaster(0, null);
      BufferedImage image = createImageFromYCCK(raster, cmykProfile);
      return image;
    } finally {
      reader.dispose();
    }
  }

  /**
   * Reads an inverted-YCCK JPEG image from the provided InputStream, converting
   * the colors to RGB using the provided CMYK ICC_Profile. The image data must
   * be in the inverted-YCCK color space.
   * <p>
   * Use this method, if you have already determined that the input stream
   * contains an inverted-YCCK JPEG image.
   *
   * @param in An InputStream, preferably an ImageInputStream, in the JPEG File
   * Interchange Format (JFIF).
   * @param cmykProfile An ICC_Profile for conversion from the CMYK color space
   * to the RGB color space. If this parameter is null, a default profile is
   * used.
   * @return a BufferedImage containing the decoded image converted into the RGB
   * color space.
   * @throws java.io.IOException
   */
  public static BufferedImage readImageFromInvertedYCCK(InputStream in, ICC_Profile cmykProfile) throws IOException {
    ImageInputStream inputStream = null;
    ImageReader reader = createNativeJPEGReader();
    try {
      inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
      reader.setInput(inputStream);
      Raster raster = reader.readRaster(0, null);
      BufferedImage image = createImageFromInvertedYCCK(raster, cmykProfile);
      return image;
    } finally {
      reader.dispose();
    }
  }

  /**
   * Creates a buffered image from a raster in the YCCK color space, converting
   * the colors to RGB using the provided CMYK ICC_Profile.
   *
   * @param ycckRaster A raster with (at least) 4 bands of samples.
   * @param cmykProfile An ICC_Profile for conversion from the CMYK color space
   * to the RGB color space. If this parameter is null, a default profile is
   * used.
   * @return a BufferedImage in the RGB color space.
   * @throws NullPointerException
   */
  public static BufferedImage createImageFromYCCK(Raster ycckRaster, ICC_Profile cmykProfile) {
    return createImageFromCMYK(convertYCCKtoCMYK(ycckRaster), cmykProfile);
  }

  /**
   * Creates a buffered image from a raster in the inverted YCCK color space,
   * converting the colors to RGB using the provided CMYK ICC_Profile.
   *
   * @param ycckRaster A raster with (at least) 4 bands of samples.
   * @param cmykProfile An ICC_Profile for conversion from the CMYK color space
   * to the RGB color space. If this parameter is null, a default profile is
   * used.
   * @return a BufferedImage in the RGB color space.
   */
  public static BufferedImage createImageFromInvertedYCCK(Raster ycckRaster, ICC_Profile cmykProfile) {
    return createImageFromCMYK(convertInvertedYCCKToCMYK(ycckRaster), cmykProfile);
  }

  /**
   * Creates a buffered image from a raster in the color space specified by the
   * given ICC_Profile.
   *
   * @param raster A raster.
   * @param profile An ICC_Profile specifying the color space of the raster.
   * @return a BufferedImage in the color space specified by the profile.
   */
  public static BufferedImage createImageFromICCProfile(Raster raster, ICC_Profile profile) {
    ICC_ColorSpace cs = new ICC_ColorSpace(profile);
    WritableRaster r = (WritableRaster) raster;

    ColorModel cm;
    if (raster.getSampleModel() instanceof PixelInterleavedSampleModel) {
      cm = new ComponentColorModel(cs, false, false, ColorModel.OPAQUE, raster.getTransferType());

    } else {
      cm = new ICCPackedColorModel(cs, raster);
    }
    return new BufferedImage(cm, (WritableRaster) raster, cm.isAlphaPremultiplied(), null);
  }

  public static BufferedImage createImageFromCMYK(Raster cmykRaster, ICC_Profile cmykProfile) {
    if (cmykProfile == null) {
      try {
        cmykProfile = ICC_Profile.getInstance(CMYKJPEGImageReader.class.getResourceAsStream("Generic CMYK Profile.icc"));
      } catch (IOException ex) {
        System.err.println("" + CMYKJPEGImageReader.class + " resource missing: Generic CMYK Profile.icc");
      }
    }

    if (cmykProfile != null) {
      return createImageFromICCProfile(cmykRaster, cmykProfile);
    } else {
      // => There is no color profile.
      // Convert image to RGB using a simple conversion algorithm.

      int w = cmykRaster.getWidth();
      int h = cmykRaster.getHeight();

      int[] rgb = new int[w * h];

      int[] C = cmykRaster.getSamples(0, 0, w, h, 0, (int[]) null);
      int[] M = cmykRaster.getSamples(0, 0, w, h, 1, (int[]) null);
      int[] Y = cmykRaster.getSamples(0, 0, w, h, 2, (int[]) null);
      int[] K = cmykRaster.getSamples(0, 0, w, h, 3, (int[]) null);

      // Split the rgb array into bands and process each band in parallel.
      // for (int i=0;i<rgb.length;i++) {
      final int BSIZE = 4096;
      IntStream.range(0, (rgb.length + BSIZE - 1) / BSIZE).parallel().parallel().forEach(band -> {
        for (int i = band * BSIZE, m = Math.min(band * BSIZE + BSIZE, rgb.length); i < m; i++) {
          int k = K[i];
          rgb[i] = (255 - min(255, C[i] + k)) << 16
              | (255 - min(255, M[i] + k)) << 8
              | (255 - min(255, Y[i] + k));
        }
      });
      Hashtable<Object, Object> properties = new Hashtable<Object, Object>();
      Raster rgbRaster = Raster.createPackedRaster(
          new DataBufferInt(rgb, rgb.length),
          w, h, w, new int[]{0xff0000, 0xff00, 0xff}, null);
      ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
      ColorModel cm = RGB;//new DirectColorModel(cs, 24, 0xff0000, 0xff00, 0xff, 0x0, false, DataBuffer.TYPE_INT);
      return new BufferedImage(cm, (WritableRaster) rgbRaster, cm.isAlphaPremultiplied(), properties);
    }
  }

  /**
   * Creates a buffered image from a raster in the RGBW color space.
   *
   * As seen from a comment made by 'phelps' at
   * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4799903
   *
   * @param rgbwRaster A raster with inverted CMYK values (=RGBW).
   * @param cmykProfile An ICC_Profile. If this parameter is null, a default
   * profile is used.
   * @return a BufferedImage in the RGB color space.
   */
  public static BufferedImage createImageFromInvertedCMYK(Raster rgbwRaster, ICC_Profile cmykProfile) {
    int w = rgbwRaster.getWidth();
    int h = rgbwRaster.getHeight();

    try {
      CompletableFuture<int[]> cfR = CompletableFuture.supplyAsync(() -> rgbwRaster.getSamples(0, 0, w, h, 0, (int[]) null));
      CompletableFuture<int[]> cfG = CompletableFuture.supplyAsync(() -> rgbwRaster.getSamples(0, 0, w, h, 1, (int[]) null));
      CompletableFuture<int[]> cfB = CompletableFuture.supplyAsync(() -> rgbwRaster.getSamples(0, 0, w, h, 2, (int[]) null));
      CompletableFuture<int[]> cfW = CompletableFuture.supplyAsync(() -> rgbwRaster.getSamples(0, 0, w, h, 3, (int[]) null));
      int[] rgb = new int[w * h];
      int[] R = cfR.get();
      int[] G = cfG.get();
      int[] B = cfB.get();
      int[] W = cfW.get();

      // Split the rgb array into bands and process each band in parallel.
      // for (int i=0;i<rgb.length;i++) {
      final int BSIZE = 4096;
      IntStream.range(0, (rgb.length + BSIZE - 1) / BSIZE).parallel().parallel().forEach(band -> {
        for (int i = band * BSIZE, m = Math.min(band * BSIZE + BSIZE, rgb.length); i < m; i++) {
          rgb[i] = (255 - W[i]) << 24 | (255 - R[i]) << 16 | (255 - G[i]) << 8 | (255 - B[i]) << 0;
        }
      });

      Raster packedRaster = Raster.createPackedRaster(
          new DataBufferInt(rgb, rgb.length),
          w, h, w, new int[]{0xff0000, 0xff00, 0xff, 0xff000000}, null);
      return createImageFromCMYK(packedRaster, cmykProfile);
    } catch (ExecutionException | InterruptedException e) {
      throw new InternalError(e);
    }
  }

  public static BufferedImage createImageFromRGB(Raster rgbRaster, ICC_Profile rgbProfile) {
    if (rgbProfile != null) {
      return createImageFromICCProfile(rgbRaster, rgbProfile);
    } else {
      BufferedImage image;
      int w = rgbRaster.getWidth();
      int h = rgbRaster.getHeight();

      try {
        CompletableFuture<int[]> cfR = CompletableFuture.supplyAsync(() -> rgbRaster.getSamples(0, 0, w, h, 0, (int[]) null));
        CompletableFuture<int[]> cfG = CompletableFuture.supplyAsync(() -> rgbRaster.getSamples(0, 0, w, h, 1, (int[]) null));
        CompletableFuture<int[]> cfB = CompletableFuture.supplyAsync(() -> rgbRaster.getSamples(0, 0, w, h, 2, (int[]) null));
        int[] rgb = new int[w * h];
        int[] R = cfR.get();
        int[] G = cfG.get();
        int[] B = cfB.get();

        // Split the rgb array into bands and process each band in parallel.
        // for (int i=0;i<rgb.length;i++) {
        final int BSIZE = 4096;
        IntStream.range(0, (rgb.length + BSIZE - 1) / BSIZE).parallel().parallel().forEach(band -> {
          for (int i = band * BSIZE, m = Math.min(band * BSIZE + BSIZE, rgb.length); i < m; i++) {
            rgb[i] = 0xff << 24 | R[i] << 16 | G[i] << 8 | B[i];
          }
        });

        WritableRaster packedRaster = Raster.createPackedRaster(
            new DataBufferInt(rgb, rgb.length),
            w, h, w, new int[]{0xff0000, 0xff00, 0xff, 0xff000000}, null);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = ColorModel.getRGBdefault();//new DirectColorModel(cs, 32, 0xff0000, 0xff00, 0xff, 0x0ff000000, false, DataBuffer.TYPE_INT);
        Hashtable<Object, Object> properties = new Hashtable<Object, Object>();
        return new BufferedImage(cm, packedRaster, cm.isAlphaPremultiplied(), properties);
      } catch (ExecutionException | InterruptedException e) {
        throw new InternalError(e);
      }
    }
  }

  public static BufferedImage createImageFromYCC(Raster yccRaster, ICC_Profile yccProfile) {
    if (yccProfile != null) {
      return createImageFromICCProfile(yccRaster, yccProfile);
    } else {
      BufferedImage image;
      int w = yccRaster.getWidth();
      int h = yccRaster.getHeight();

      try {
        CompletableFuture<int[]> cfY = CompletableFuture.supplyAsync(() -> yccRaster.getSamples(0, 0, w, h, 0, (int[]) null));
        CompletableFuture<int[]> cfCb = CompletableFuture.supplyAsync(() -> yccRaster.getSamples(0, 0, w, h, 1, (int[]) null));
        CompletableFuture<int[]> cfCr = CompletableFuture.supplyAsync(() -> yccRaster.getSamples(0, 0, w, h, 2, (int[]) null));
        int[] rgb = new int[w * h];
        int[] Y = cfY.get();
        int[] Cb = cfCb.get();
        int[] Cr = cfCr.get();

        // Split the rgb array into bands and process each band in parallel.
        // for (int i=0;i<rgb.length;i++) {
        final int BSIZE = 4096;
        IntStream.range(0, (rgb.length + BSIZE - 1) / BSIZE).parallel().parallel().forEach(band -> {
          for (int i = band * BSIZE, m = Math.min(band * BSIZE + BSIZE, rgb.length); i < m; i++) {
            int Yi, Cbi, Cri;
            int R, G, B;

            //RGB can be computed directly from YCbCr (256 levels) as follows:
            //R = Y + 1.402 (Cr-128)
            //G = Y - 0.34414 (Cb-128) - 0.71414 (Cr-128)
            //B = Y + 1.772 (Cb-128)
            Yi = Y[i];
            Cbi = Cb[i];
            Cri = Cr[i];
            R = (1000 * Yi + 1402 * (Cri - 128)) / 1000;
            G = (100000 * Yi - 34414 * (Cbi - 128) - 71414 * (Cri - 128)) / 100000;
            B = (1000 * Yi + 1772 * (Cbi - 128)) / 1000;

            R = min(255, max(0, R));
            G = min(255, max(0, G));
            B = min(255, max(0, B));

            rgb[i] = 0xff << 24 | R << 16 | G << 8 | B;
          }
        });
        Hashtable<Object, Object> properties = new Hashtable<>();
        Raster rgbRaster = Raster.createPackedRaster(
            new DataBufferInt(rgb, rgb.length),
            w, h, w, new int[]{0xff0000, 0xff00, 0xff, 0xff000000}, null);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = ColorModel.getRGBdefault();//new DirectColorModel(cs, 32, 0xff0000, 0xff00, 0xff, 0x0ff000000, false, DataBuffer.TYPE_INT);
        return new BufferedImage(cm, (WritableRaster) rgbRaster, cm.isAlphaPremultiplied(), properties);
      } catch (ExecutionException | InterruptedException e) {
        throw new InternalError(e);
      }
    }
  }
  /**
   * Define tables for YCC->RGB color space conversion.
   */
  private final static int SCALEBITS = 16;
  private final static int MAXJSAMPLE = 255;
  private final static int CENTERJSAMPLE = 128;
  private final static int ONE_HALF = 1 << (SCALEBITS - 1);
  private final static int[] Cr_r_tab = new int[MAXJSAMPLE + 1];
  private final static int[] Cb_b_tab = new int[MAXJSAMPLE + 1];
  private final static int[] Cr_g_tab = new int[MAXJSAMPLE + 1];
  private final static int[] Cb_g_tab = new int[MAXJSAMPLE + 1];

  /*
   * Initialize tables for YCC->RGB colorspace conversion.
   */
  private static synchronized void buildYCCtoRGBtable() {
    if (Cr_r_tab[0] == 0) {
      for (int i = 0, x = -CENTERJSAMPLE; i <= MAXJSAMPLE; i++, x++) {
        // i is the actual input pixel value, in the range 0..MAXJSAMPLE/
        // The Cb or Cr value we are thinking of is x = i - CENTERJSAMPLE
        // Cr=>R value is nearest int to 1.40200 * x
        Cr_r_tab[i] = (int) ((1.40200 * (1 << SCALEBITS) + 0.5) * x + ONE_HALF) >> SCALEBITS;
        // Cb=>B value is nearest int to 1.77200 * x
        Cb_b_tab[i] = (int) ((1.77200 * (1 << SCALEBITS) + 0.5) * x + ONE_HALF) >> SCALEBITS;
        // Cr=>G value is scaled-up -0.71414 * x
        Cr_g_tab[i] = -(int) (0.71414 * (1 << SCALEBITS) + 0.5) * x;
        // Cb=>G value is scaled-up -0.34414 * x
        // We also add in ONE_HALF so that need not do it in inner loop
        Cb_g_tab[i] = -(int) ((0.34414) * (1 << SCALEBITS) + 0.5) * x + ONE_HALF;
      }
    }
  }

  /*
   * Adobe-style YCCK->CMYK conversion.
   * We convert YCbCr to C, M, Y, while passing K (black) unchanged.
   * We assume build_ycc_rgb_table has been called.
   */
  private static Raster convertInvertedYCCKToCMYK(Raster ycckRaster) {
    return convertInvertedYCCKToCMYK_byBytes(ycckRaster);
  }

  /**
   * Fastest method but may not always work.
   *
   * @param ycckRaster a YCCK raster
   * @return a CMYK raster
   */
  private static Raster convertInvertedYCCKToCMYK_byBytes(Raster ycckRaster) {
    buildYCCtoRGBtable();
    int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();

    if (!(ycckRaster.getDataBuffer() instanceof DataBufferByte)) {
      return convertInvertedYCCKToCMYK_byPixels(ycckRaster);
    }

    // XXX foolishly assume that raster width = w, raster height=h, and scanline stride = 4*w
    byte[] ycck = ((DataBufferByte) ycckRaster.getDataBuffer()).getData();
    int[] cmyk = new int[w * h];

    // Split the cmyk array into bands and process each band in parallel.
    // for (int i=0;i<cmyk.length;i++) {
    final int BSIZE = 4096;
    IntStream.range(0, (cmyk.length + BSIZE - 1) / BSIZE).parallel().parallel().forEach(band -> {
          for (int i = band * BSIZE, m = Math.min(band * BSIZE + BSIZE, cmyk.length); i < m; i++) {
            int j = i * 4;
            int y = 255 - (ycck[j] & 0xff);
            int cb = 255 - (ycck[j + 1] & 0xff);
            int cr = 255 - (ycck[j + 2] & 0xff);
            int k = 255 - (ycck[j + 3] & 0xff);
            // Range-limiting is essential due to noise introduced by DCT losses.
            int cmykC = MAXJSAMPLE - (y + Cr_r_tab[cr]);
            int cmykM = MAXJSAMPLE - (y + (Cb_g_tab[cb] + Cr_g_tab[cr] >> SCALEBITS));
            int cmykY = MAXJSAMPLE - (y + Cb_b_tab[cb]);
            // k passes through unchanged
            cmyk[i] = (cmykC < 0 ? 0 : (cmykC > 255) ? 255 : cmykC) << 24
                | (cmykM < 0 ? 0 : (cmykM > 255) ? 255 : cmykM) << 16
                | (cmykY < 0 ? 0 : (cmykY > 255) ? 255 : cmykY) << 8
                | k;
          }
        }
    );

    Raster cmykRaster = Raster.createPackedRaster(
        new DataBufferInt(cmyk, cmyk.length),
        w, h, w, new int[]{0xff000000, 0xff0000, 0xff00, 0xff}, null);
    return cmykRaster;

  }

  /**
   * This is slightly faster than _bySamples and does not use any internal APIs.
   */
  private static Raster convertInvertedYCCKToCMYK_byPixels(Raster ycckRaster) {
    buildYCCtoRGBtable();
    int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();

    int[] ycck = ycckRaster.getPixels(0, 0, w, h, (int[]) null);

    int[] cmyk = new int[w * h];

    // Split the cmyk array into bands and process each band in parallel.
    // for (int i=0;i<cmyk.length;i++) {
    final int BSIZE = 4096;
    IntStream.range(0, (cmyk.length + BSIZE - 1) / BSIZE).parallel().parallel().forEach(band -> {
      for (int i = band * BSIZE, m = Math.min(band * BSIZE + BSIZE, cmyk.length); i < m; i++) {
        int j = i * 4;
        int y = 255 - ycck[j];
        int cb = 255 - ycck[j + 1];
        int cr = 255 - ycck[j + 2];
        int cmykC, cmykM, cmykY;
        // Range-limiting is essential due to noise introduced by DCT losses.
        cmykC = MAXJSAMPLE - (y + Cr_r_tab[cr]);
        cmykM = MAXJSAMPLE - (y + (Cb_g_tab[cb] + Cr_g_tab[cr] >> SCALEBITS));
        cmykY = MAXJSAMPLE - (y + Cb_b_tab[cb]);
        // K passes through unchanged
        cmyk[i] = (cmykC < 0 ? 0 : (cmykC > 255) ? 255 : cmykC) << 24
            | (cmykM < 0 ? 0 : (cmykM > 255) ? 255 : cmykM) << 16
            | (cmykY < 0 ? 0 : (cmykY > 255) ? 255 : cmykY) << 8
            | 255 - ycck[j + 3];
      }
    });
//      }
    Raster cmykRaster = Raster.createPackedRaster(
        new DataBufferInt(cmyk, cmyk.length),
        w, h, w, new int[]{0xff000000, 0xff0000, 0xff00, 0xff}, null);
    return cmykRaster;
  }

  /**
   * This is slower but does not use any internal APIs.
   */
  private static Raster convertInvertedYCCKToCMYK_bySamples(Raster ycckRaster) {
    buildYCCtoRGBtable();
    int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();

    try {
      CompletableFuture<int[]> cfY = CompletableFuture.supplyAsync(() -> ycckRaster.getSamples(0, 0, w, h, 0, (int[]) null));
      CompletableFuture<int[]> cfCb = CompletableFuture.supplyAsync(() -> ycckRaster.getSamples(0, 0, w, h, 1, (int[]) null));
      CompletableFuture<int[]> cfCr = CompletableFuture.supplyAsync(() -> ycckRaster.getSamples(0, 0, w, h, 2, (int[]) null));
      CompletableFuture<int[]> cfK = CompletableFuture.supplyAsync(() -> ycckRaster.getSamples(0, 0, w, h, 3, (int[]) null));
      int[] cmyk = new int[w * h];
      int[] ycckY = cfY.get();
      int[] ycckCb = cfCb.get();
      int[] ycckCr = cfCr.get();
      int[] ycckK = cfK.get();

      // Split the cmyk array into bands and process each band in parallel.
      // for (int i=0;i<cmyk.length;i++) {
      final int BSIZE = 4096;
      IntStream.range(0, (cmyk.length + BSIZE - 1) / BSIZE).parallel().parallel().forEach(band -> {
        for (int i = band * BSIZE, m = Math.min(band * BSIZE + BSIZE, cmyk.length); i < m; i++) {
          int y = 255 - ycckY[i];
          int cb = 255 - ycckCb[i];
          int cr = 255 - ycckCr[i];
          int cmykC, cmykM, cmykY;
          // Range-limiting is essential due to noise introduced by DCT losses.
          cmykC = MAXJSAMPLE - (y + Cr_r_tab[cr]);	// red
          cmykM = MAXJSAMPLE - (y
              + // green
              (Cb_g_tab[cb] + Cr_g_tab[cr]
                  >> SCALEBITS));
          cmykY = MAXJSAMPLE - (y + Cb_b_tab[cb]);	// blue
          // K passes through unchanged
          cmyk[i] = (cmykC < 0 ? 0 : (cmykC > 255) ? 255 : cmykC) << 24
              | (cmykM < 0 ? 0 : (cmykM > 255) ? 255 : cmykM) << 16
              | (cmykY < 0 ? 0 : (cmykY > 255) ? 255 : cmykY) << 8
              | 255 - ycckK[i];
        }
      });

      Raster cmykRaster = Raster.createPackedRaster(
          new DataBufferInt(cmyk, cmyk.length),
          w, h, w, new int[]{0xff000000, 0xff0000, 0xff00, 0xff}, null);
      return cmykRaster;
    } catch (InterruptedException | ExecutionException ex) {
      throw new InternalError(ex);
    }
  }

  private static Raster convertYCCKtoCMYK(Raster ycckRaster) {
    buildYCCtoRGBtable();

    int w = ycckRaster.getWidth(), h = ycckRaster.getHeight();
    try {
      CompletableFuture<int[]> cfY = CompletableFuture.supplyAsync(() -> ycckRaster.getSamples(0, 0, w, h, 0, (int[]) null));
      CompletableFuture<int[]> cfCb = CompletableFuture.supplyAsync(() -> ycckRaster.getSamples(0, 0, w, h, 1, (int[]) null));
      CompletableFuture<int[]> cfCr = CompletableFuture.supplyAsync(() -> ycckRaster.getSamples(0, 0, w, h, 2, (int[]) null));
      CompletableFuture<int[]> cfK = CompletableFuture.supplyAsync(() -> ycckRaster.getSamples(0, 0, w, h, 3, (int[]) null));
      int[] cmyk = new int[w * h];
      int[] ycckY = cfY.get();
      int[] ycckCb = cfCb.get();
      int[] ycckCr = cfCr.get();
      int[] ycckK = cfK.get();

      // Split the cmyk array into bands and process each band in parallel.
      // for (int i=0;i<cmyk.length;i++) {
      final int BSIZE = 4096;
      IntStream.range(0, (cmyk.length + BSIZE - 1) / BSIZE).parallel().parallel().forEach(band -> {
        for (int i = band * BSIZE, m = Math.min(band * BSIZE + BSIZE, cmyk.length); i < m; i++) {
          int y = ycckY[i];
          int cb = ycckCb[i];
          int cr = ycckCr[i];
          int cmykC, cmykM, cmykY;
          // Range-limiting is essential due to noise introduced by DCT losses.
          cmykC = MAXJSAMPLE - (y + Cr_r_tab[cr]);	// red
          cmykM = MAXJSAMPLE - (y
              + // green
              (Cb_g_tab[cb] + Cr_g_tab[cr]
                  >> SCALEBITS));
          cmykY = MAXJSAMPLE - (y + Cb_b_tab[cb]);	// blue
          // K passes through unchanged
          cmyk[i] = (cmykC < 0 ? 0 : (cmykC > 255) ? 255 : cmykC) << 24
              | (cmykM < 0 ? 0 : (cmykM > 255) ? 255 : cmykM) << 16
              | (cmykY < 0 ? 0 : (cmykY > 255) ? 255 : cmykY) << 8
              | ycckK[i];
        }
      });

      return Raster.createPackedRaster(
          new DataBufferInt(cmyk, cmyk.length),
          w, h, w, new int[]{0xff000000, 0xff0000, 0xff00, 0xff}, null);
    } catch (InterruptedException | ExecutionException e) {
      throw new InternalError(e);
    }
  }

  /**
   * Reads a JPEG image from the provided InputStream. The image data must be in
   * the YUV or the Gray color space.
   * <p>
   * Use this method, if you have already determined that the input stream
   * contains a YCC or Gray JPEG image.
   *
   * @param in An InputStream, preferably an ImageInputStream, in the JPEG File
   * Interchange Format (JFIF).
   * @return a BufferedImage containing the decoded image converted into the RGB
   * color space.
   * @throws java.io.IOException
   */
  public static BufferedImage readImageFromYCCorGray(ImageInputStream in) throws IOException {
    ImageReader r = createNativeJPEGReader();
    try {
      r.setInput(in);
      BufferedImage img = r.read(0);
      return img;
    } finally {
      r.dispose();
    }
  }

  /**
   * Disposes of resources held internally by the reader.
   */
  @Override
  public void dispose() {
    try {
      if (image != null && !didReturnImage) {
        image.flush();
      }
    } catch (Throwable ex) {
      // consume the exception
      ex.printStackTrace();
    } finally {
      image = null;
    }
  }

}