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

package org.silverpeas.core.io.media.image.thumbnail.control;

import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import junit.framework.TestCase;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class ThumbnailControllerTest extends TestCase {

	@Test
	/**
	 * test a small crop zone to an image
	 * crop 50*50 -> image 100*100
	 */
	public void testASmallerCropOfTheDestinationImage() throws Exception {
		goTesting(50, 50);
	}

	@Test
	/**
	 * test a big crop zone to a image
	 * crop 101*101 -> image 100*100
	 */
	public void testABiggerCropOfTheDestinationImage() throws Exception {
		// warning base image height = 102
		goTesting(101, 101);
	}

	private void goTesting(int x_length, int y_length){

		String instanceId = "kmelia57";
	    int objectId = 999999;
	    int objectType = ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE;
	    String mimeType = "image/gif";
	    String originalFileName = "silverpeas.gif";
	    String cropFileDir = "croptest";
	    String cropFileName = "croptest//silverpeasCropTest.gif";
	    int x_start = 0;
	    int y_start = 0;

	    ThumbnailDetail detail = new ThumbnailDetail(instanceId,objectId,objectType);
	    detail.setOriginalFileName(originalFileName);
	    detail.setMimeType(mimeType);
	    detail.setCropFileName(cropFileName);
	    detail.setXStart(x_start);
	    detail.setYStart(y_start);
	    detail.setXLength(x_length);
	    detail.setYLength(y_length);

		File originalFile = null;
		File cropFile = null;
		try{
			originalFile = new File(originalFileName);
			try {
				FileOutputStream outputStream = new FileOutputStream(originalFile);
				InputStream inputStream = ThumbnailControllerTest.class.getResourceAsStream("silverpeas.gif");
				DataOutputStream out = new DataOutputStream(new BufferedOutputStream(outputStream));
				int c;
				while((c = inputStream.read()) != -1) {
					out.writeByte(c);
				}
				inputStream.close();
				out.close();
			}catch(IOException e) {
				assertFalse("Exception has be thrown", true);
			}

			try{
			ThumbnailController.createCropThumbnailFileOnServer(originalFileName, cropFileDir, cropFileName, detail, 100, 100);
			cropFile = new File(cropFileName);
			assertFalse("Crop file doesn't exist", !cropFile.exists());
			Dimension dim = getImageDim(cropFileName);
			assertFalse("Width is wrong", dim.getWidth() != 100.0);
			assertFalse("Height is wrong", dim.getHeight() != 100.0);
		    }catch(Exception e){
			assertFalse("Exception has be thrown", true);
		    }finally{
			if(cropFile != null){
					boolean result = cropFile.delete();
					assertFalse("error suppression", !result);
				}
		    }

		}finally{
			if(originalFile != null){
				originalFile.delete();
			}
			File cropDir = new File(cropFileDir);
			if(cropDir.exists()){
				cropDir.delete();
			}
		}
	}

	private Dimension getImageDim(String path) throws IOException {
	    Dimension result = null;
	    Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix("gif");
	    if (iter.hasNext()) {
	        ImageReader reader = iter.next();
	        ImageInputStream stream = null;
	        try {
	            stream = new FileImageInputStream(new File(path));
	            reader.setInput(stream);
	            int width = reader.getWidth(reader.getMinIndex());
	            int height = reader.getHeight(reader.getMinIndex());
	            result = new Dimension(width, height);
	        } finally {
			if(stream != null){
				stream.close();
			}
	            reader.dispose();
	        }
	    } else {
	        assertFalse("No reader for gif", true);
	    }
	    return result;
	}


	@Test
	/**
	 * test crop a non image file
	 */
	  public void testExceptionWhenNotAnImage() throws Exception {

		String instanceId = "kmelia57";
	    int objectId = 999999;
	    int objectType = ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE;
	    String mimeType = "txt";
	    String originalFileName = "55555555.txt";
	    String cropFileName = "";
	    int x_start = 0;
	    int y_start = 0;
	    int x_length = 0;
	    int y_length = 0;

	    ThumbnailDetail detail = new ThumbnailDetail(instanceId,objectId,objectType);
	    detail.setOriginalFileName(originalFileName);
	    detail.setMimeType(mimeType);
	    detail.setCropFileName(cropFileName);
	    detail.setXStart(x_start);
	    detail.setYStart(y_start);
	    detail.setXLength(x_length);
	    detail.setYLength(y_length);

		File originalFile = null;
		try{
			originalFile = new File(originalFileName);
			if(!originalFile.exists()){
				originalFile.createNewFile();
				FileOutputStream outputStream = new FileOutputStream(originalFile);
				outputStream.write("fichier de test non image".getBytes());
				outputStream.close();
			}
		    try{
			ThumbnailController.cropFromPath(originalFileName, detail, 100, 100);
			assertFalse("Exception has not be thrown", true);
		    }catch(Exception e){
			// mode normal
		    }
		}finally{
			if(originalFile != null){
				originalFile.delete();
			}
		}
	}


}
