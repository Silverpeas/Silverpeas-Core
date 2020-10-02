 /* @(#)ICCPackedColorModel.java  1.0  2011-03-13
 * 
 * Copyright (c) 2011 Werner Randelshofer, Switzerland. 
 * You may only use this file in compliance with the accompanying license terms.
 */
package org.monte.media.color;

import java.awt.color.ICC_ColorSpace;
import java.awt.image.DataBuffer;
import java.awt.image.PackedColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

/**
 * {@code ICCPackedColorModel}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class ICCPackedColorModel extends PackedColorModel {

    private final ICC_ColorSpace colorSpace;
    private final int[] maskArray;
    private final int[] maskOffsets;

    /** Returns the number of bits per pixel. */
    private static int getBits(Raster raster) {
        int bits = 0;
        SinglePixelPackedSampleModel sm = (SinglePixelPackedSampleModel) raster.getSampleModel();
        int[] bitOffsets = sm.getBitOffsets();
        int[] sampleSizes = sm.getSampleSize();
        for (int i = 0; i < bitOffsets.length; i++) {
            bits = Math.max(bits, bitOffsets[i] + sampleSizes[i]);
        }
        return bits;
    }

    public ICCPackedColorModel(ICC_ColorSpace colorSpace, Raster raster) {
        // FIXME this super call silently only handles rasters without alpha channel!
        super(colorSpace,//
                getBits(raster),//
                ((SinglePixelPackedSampleModel) raster.getSampleModel()).getBitMasks(), 0, true, OPAQUE, raster.getTransferType());
        this.colorSpace = colorSpace;
        this.maskArray = ((SinglePixelPackedSampleModel) raster.getSampleModel()).getBitMasks();
        this.maskOffsets = ((SinglePixelPackedSampleModel) raster.getSampleModel()).getBitOffsets();
    }

    @Override
    public int getRGB(int pixel) {

        float[] rgbF = colorSpace.toRGB(new float[]{//
            (pixel & maskArray[0]) / (float) (maskArray[0] & 0xffffffffL),//
            (pixel & maskArray[1]) / (float) (maskArray[1] & 0xffffffffL),//
            (pixel & maskArray[2]) / (float) (maskArray[2] & 0xffffffffL),//
            (pixel & maskArray[3]) / (float) (maskArray[3] & 0xffffffffL)//
        });/*
         float[] rgbF = colorSpace.toRGB(new float[]{//
         (pixel & maskArray[0]) >>> offsetArray[0],//
         (pixel & maskArray[1]) >>> offsetArray[1],//
         (pixel & maskArray[2]) >>> offsetArray[2],//
         (pixel & maskArray[3])>>> offsetArray[3]//
         });*/

        return ((int) (rgbF[0] * 255) << 16) | ((int) (rgbF[1] * 255) << 8) | ((int) (rgbF[2] * 255) << 0);
    }

    @Override
    public int getRed(int pixel) {
        int rgb = getRGB(pixel);
        return (rgb & 0xff0000) >> 16;
    }

    @Override
    public int getGreen(int pixel) {
        int rgb = getRGB(pixel);
        return (rgb & 0xff00) >> 8;
    }

    @Override
    public int getBlue(int pixel) {
        int rgb = getRGB(pixel);
        return rgb & 0xff;
    }

    @Override
    public int getAlpha(int pixel) {
        return 0xff;
    }

    @Override
    public boolean isCompatibleRaster(Raster raster) {
        SampleModel sm = raster.getSampleModel();
        SinglePixelPackedSampleModel spsm;
        if (sm instanceof SinglePixelPackedSampleModel) {
            spsm = (SinglePixelPackedSampleModel) sm;
        } else {
            return false;
        }
        if (spsm.getNumBands() != getNumComponents()) {
            return false;
        }

        int[] bitMasks = spsm.getBitMasks();
        for (int i = 0; i < 4; i++) {
            if (bitMasks[i] != maskArray[i]) {
                return false;
            }
        }

        return (raster.getTransferType() == transferType);
    }
    @Override
    final public int[] getComponents(Object pixel, int[] components,
                                     int offset) {
        int intpixel=0;
        switch (transferType) {
            case DataBuffer.TYPE_BYTE:
               byte bdata[] = (byte[])pixel;
               intpixel = bdata[0] & 0xff;
            break;
            case DataBuffer.TYPE_USHORT:
               short sdata[] = (short[])pixel;
               intpixel = sdata[0] & 0xffff;
            break;
            case DataBuffer.TYPE_INT:
               int idata[] = (int[])pixel;
               intpixel = idata[0];
            break;
            default:
               throw new UnsupportedOperationException("This method has not been "+
                   "implemented for transferType " + transferType);
        }
        return getComponents(intpixel, components, offset);
    }

    @Override
    public int[] getComponents(int pixel, int[] components, int offset) {
        final int numComponents = getNumComponents();
       if (components == null) {
            components = new int[offset+numComponents];
        }

        for (int i=0; i < numComponents; i++) {
            components[offset+i] = (pixel & maskArray[i]) >>> maskOffsets[i];
        }

        return components;
     }
    
    
}
