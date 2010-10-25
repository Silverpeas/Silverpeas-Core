/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.thumbnail.control;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import com.silverpeas.thumbnail.ThumbnailException;
import com.silverpeas.thumbnail.ThumbnailRuntimeException;

import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.thumbnail.service.ThumbnailService;
import com.silverpeas.thumbnail.service.ThumbnailServiceImpl;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class ThumbnailController {
  private static ThumbnailService thumbnailService = null;
  
  private static final ResourceLocator publicationSettings = new ResourceLocator(
		      "com.stratelia.webactiv.util.publication.publicationSettings", "fr");
  
  /**
   * the constructor.
   */
  public ThumbnailController() {
  }

  private static ThumbnailService getThumbnailService() {

    if (thumbnailService == null) {
    	thumbnailService = new ThumbnailServiceImpl();
    }

    return thumbnailService;
  }

  /**
   * to update thumbnails files informations
   * 
   * @param thumbDetail :ThumbnailDetail.
   * @author Sebastien ROCHET
   * @version 3.0
   */
  public static void updateThumbnail(ThumbnailDetail thumbDetail, int thumbnailWidth, int thumbnailHeight) {

    try {
    	ThumbnailDetail completeThumbnail =  getThumbnailService().getCompleteThumbnail(thumbDetail);
    	if(completeThumbnail.getCropFileName() != null){
    		// on garde toujours le meme nom de fichier par contre on le supprime
        	// puis le recreer avec les nouvelles coordonnees
    		deleteThumbnailFileOnServer(completeThumbnail.getInstanceId(), completeThumbnail.getCropFileName());
    	}else{
    		// case creation
    		String type = completeThumbnail.getOriginalFileName().substring( completeThumbnail.getOriginalFileName().lastIndexOf(".") + 1,
    				 completeThumbnail.getOriginalFileName().length());
    		String cropFileName = new Long(new Date().getTime()).toString() + "."
  	          + type;
    		completeThumbnail.setCropFileName(cropFileName);
    	}
    	
    	// recup new crop definition
    	completeThumbnail.setXStart(thumbDetail.getXStart());
    	completeThumbnail.setYStart(thumbDetail.getYStart());
    	completeThumbnail.setXLength(thumbDetail.getXLength());
    	completeThumbnail.setYLength(thumbDetail.getYLength());
    	
    	String pathOriginalFile = FileRepositoryManager.getAbsolutePath(completeThumbnail.getInstanceId()) + publicationSettings.getString("imagesSubDirectory") + File.separator + completeThumbnail.getOriginalFileName();
		String pathCropdir = FileRepositoryManager.getAbsolutePath(completeThumbnail.getInstanceId()) + publicationSettings.getString("imagesSubDirectory");
		String pathCropFile = pathCropdir + File.separator + completeThumbnail.getCropFileName();
		
    	createCropThumbnailFileOnServer(pathOriginalFile, pathCropdir, pathCropFile, completeThumbnail, thumbnailWidth, thumbnailHeight);
    	getThumbnailService().updateThumbnail(completeThumbnail);
      
    } catch (Exception e) {
      throw new ThumbnailRuntimeException(
          "ThumbnailController.updateThumbnail()",
          SilverpeasRuntimeException.ERROR, "thumbnail_MSG_UPDATE_THUMBNAIL_KO",
          e);
    }
  }

  public static void deleteThumbnail(ThumbnailDetail thumbDetail) {

    try {
    	// delete the file on server
    	ThumbnailDetail completeThumbnail =  getThumbnailService().getCompleteThumbnail(thumbDetail);
    	if(completeThumbnail != null){
	    	if(completeThumbnail.getOriginalFileName() != null){
	    		deleteThumbnailFileOnServer(completeThumbnail.getInstanceId(), completeThumbnail.getOriginalFileName());
	    	}
	    	if(completeThumbnail.getCropFileName() != null){
	    		deleteThumbnailFileOnServer(completeThumbnail.getInstanceId(), completeThumbnail.getCropFileName());
	    	}
	    	// delete db line
	    	getThumbnailService().deleteThumbnail(thumbDetail);
    	}
    } catch (Exception fe) {
      throw new ThumbnailRuntimeException(
          "ThumbnailController.deleteThumbnail(ThumbnailDetail thumbDetail)",
          SilverpeasRuntimeException.ERROR, "thumbnail_MSG_DELETE_THUMBNAIL_KO", fe);
    }
  }
  
  public static ThumbnailDetail createThumbnail(ThumbnailDetail thumbDetail, int thumbnailWidth, int thumbnailHeight) {
	    try {
	    	// create line in db
	    	ThumbnailDetail thumdAdded = getThumbnailService().createThumbnail(thumbDetail);
	    	
	    	// create crop thumbnail
	    	if(thumdAdded.getCropFileName() == null){
	    		createCropFile(thumbnailWidth, thumbnailHeight, thumdAdded);
	    	}
	    	return thumdAdded;
	    } catch (Exception e) {
	      throw new ThumbnailRuntimeException(
	          "ThumbnailController.createThumbnail()",
	          SilverpeasRuntimeException.ERROR, "thumbnail_MSG_CREATE_THUMBNAIL_KO",
	          e);
	    }
	  }
  
  public static ThumbnailDetail getCompleteThumbnail(ThumbnailDetail thumbDetail) {
	    try {
	    	// get thumbnail
	    	return getThumbnailService().getCompleteThumbnail(thumbDetail);
	    } catch (Exception e) {
	      throw new ThumbnailRuntimeException(
	          "ThumbnailController.getCompleteThumbnail()",
	          SilverpeasRuntimeException.ERROR, "thumbnail_MSG_GET_COMPLETE_THUMBNAIL_KO",
	          e);
	    }
	  }
  
  protected static void createCropThumbnailFileOnServer(String pathOriginalFile, String pathCropdir, String pathCropFile, ThumbnailDetail thumbnail, int thumbnailWidth, int thumbnailHeight) {

    try {
      // Creates folder if not exists
	  File dir = new File(pathCropdir);
	  if (!dir.exists()) {
		  FileFolderManager.createFolder(pathCropdir);
	  }

      // create empty file
      File cropFile = new File(pathCropFile);
      if (!cropFile.exists()) {
          cropFile.createNewFile();
      }

      File originalFile = new File(pathOriginalFile);
      BufferedImage bufferOriginal = ImageIO.read(originalFile);
      // crop image
      BufferedImage cropPicture = bufferOriginal.getSubimage(thumbnail.getXStart(), thumbnail.getYStart(), thumbnail.getXLength(), thumbnail.getYLength());
      BufferedImage cropPictureFinal = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
      
      // Redimensionnement de l'image
      Graphics2D g2 = cropPictureFinal.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2.drawImage(cropPicture, 0, 0, thumbnailWidth, thumbnailHeight, null);
      g2.dispose();
      
      // save crop image
      String type = thumbnail.getOriginalFileName().substring( thumbnail.getOriginalFileName().lastIndexOf(".") + 1, thumbnail.getOriginalFileName().length());
      ImageIO.write(cropPictureFinal, type, cropFile);
      
    } catch (Exception e) {
      SilverTrace
          .warn(
          "thumbnail",
          "ThumbnailController.createThumbnailFileOnServer()",
          "thumbnail_MSG_CREATE_CROP_FILE_KO", "originalFileName=" + thumbnail.getOriginalFileName() + " cropFileName = " + thumbnail.getCropFileName(), e);
    }
  }
  
  private static void deleteThumbnailFileOnServer(String componentId, String fileName) {
	 
		String path = FileRepositoryManager.getAbsolutePath(componentId) + publicationSettings.getString("imagesSubDirectory") + File.separator
			+ fileName;

	    try {
	      File d = new File(path);

	      if (d.exists()) {
	        FileFolderManager.deleteFile(path);
	      }
	      
	    } catch (Exception e) {
	      SilverTrace
	          .warn(
	          "thumbnail",
	          "ThumbnailController.deleteThumbnailFileOnServer(String componentId, String fileName)",
	          "thumbnail_MSG_NOT_DELETE_FILE", "filePath=" + path, e);
	    }
	  }
 
  public static String getImage(String instanceId, int objectId, int objectType) throws ThumbnailException {
	  ThumbnailDetail thumbDetail = new ThumbnailDetail(instanceId, objectId, objectType);
	  
	  // default size if creation
	  String[] imageProps = getImageAndMimeType(thumbDetail, 50, 50);
	  return imageProps[0];
  }

  public static String getImageMimeType(String instanceId, int objectId, int objectType) throws ThumbnailException {
	  ThumbnailDetail thumbDetail = new ThumbnailDetail(instanceId, objectId, objectType);
	  
	  // default size if creation
	  String[] imageProps = getImageAndMimeType(thumbDetail, 50, 50);
	  return imageProps[1];
  }
  
  public static String[] getImageAndMimeType(ThumbnailDetail thumbDetail, int thumbnailWidth, int thumbnailHeight){
    try {
	    	ThumbnailDetail thumbDetailComplete = getThumbnailService().getCompleteThumbnail(thumbDetail);
	    	if(thumbDetailComplete != null){
	    		if(thumbDetailComplete.getCropFileName() == null){
		    		createCropFile(thumbnailWidth, thumbnailHeight,
							thumbDetailComplete);
		    	}
	    		return new String[]{thumbDetailComplete.getCropFileName(),thumbDetailComplete.getMimeType()};
	    	}else{
	    		// case no thumbnail define
	    		return new String[]{null,null};
		    }
	    } catch (Exception e) {
	      throw new ThumbnailRuntimeException(
	          "ThumbnailController.getCompleteThumbnail()",
	          SilverpeasRuntimeException.ERROR, "thumbnail_MSG_GET_IMAGE_KO",
	          e);
	    }
  }

	private static void createCropFile(int thumbnailWidth, int thumbnailHeight,
			ThumbnailDetail thumbDetailComplete) throws IOException,
			ThumbnailException {
		// if crop image does not exist, create it as the original file
		String type = thumbDetailComplete.getOriginalFileName().substring( thumbDetailComplete.getOriginalFileName().lastIndexOf(".") + 1,
				thumbDetailComplete.getOriginalFileName().length());
		// add 2 to be sure cropfilename is different from original filename
		String cropFileName = new Long(new Date().getTime()+2).toString() + "."
		  + type;
		thumbDetailComplete.setCropFileName(cropFileName);
		
		// crop sur l image entiere
		String pathOriginalFile = FileRepositoryManager.getAbsolutePath(thumbDetailComplete.getInstanceId()) + publicationSettings.getString("imagesSubDirectory") + File.separator + thumbDetailComplete.getOriginalFileName();
		cropFromPath(pathOriginalFile, thumbDetailComplete, thumbnailHeight,
				thumbnailWidth);
	}

	protected static void cropFromPath(String pathOriginalFile,
			ThumbnailDetail thumbDetailComplete, int thumbnailHeight,
			int thumbnailWidth) throws IOException, ThumbnailException {
		File originalFile = new File(pathOriginalFile);
		BufferedImage bufferOriginal = ImageIO.read(originalFile);
		if (bufferOriginal==null) {
			SilverTrace
	          .error(
	          "thumbnail",
	          "ThumbnailController.createCropFile(int thumbnailWidth, int thumbnailHeight,ThumbnailDetail thumbDetailComplete)",
	          "thumbnail.EX_MSG_NOT_AN_IMAGE", "pathOriginalFile=" + pathOriginalFile);
			throw new ThumbnailException("ThumbnailBmImpl.createCropFile()",
			          SilverpeasException.ERROR,
			          "thumbnail.EX_MSG_NOT_AN_IMAGE");
		}
		else {
			thumbDetailComplete.setXStart(0);
			thumbDetailComplete.setYStart(0);
			thumbDetailComplete.setXLength(bufferOriginal.getWidth());
			thumbDetailComplete.setYLength(bufferOriginal.getHeight());
			
			String pathCropdir = FileRepositoryManager.getAbsolutePath(thumbDetailComplete.getInstanceId()) + publicationSettings.getString("imagesSubDirectory");
			String pathCropFile = pathCropdir + File.separator + thumbDetailComplete.getCropFileName();
			
			createCropThumbnailFileOnServer(pathOriginalFile, pathCropdir, pathCropFile, thumbDetailComplete, thumbnailWidth, thumbnailHeight);
			getThumbnailService().updateThumbnail(thumbDetailComplete);
		}
	}
}