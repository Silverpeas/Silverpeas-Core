package com.silverpeas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class MSdocumentPropertiesManager {
	//Constructeurs
    /**
     * Unique constructeur de la classe
     */
    public MSdocumentPropertiesManager()   {	
    }
    
    
    /**
     * Return SummaryInformation of an Office document
     * @param file
     * @return SummaryInformation
     * 
     */
    public SummaryInformation getSummaryInformation(String fileName)
    {
    	InputStream inputStream = null;
    	DocumentInputStream stream = null;
    	PropertySet ps = null;
		try {
	    	inputStream		=	new	FileInputStream(new File(fileName));
	    	POIFSFileSystem fs			=	new POIFSFileSystem(inputStream);
	    	
//			DirectoryEntry 	directory	=	fs.getRoot();
			DirectoryEntry 	directory	=	fs.getRoot();
			DocumentEntry document		=	(DocumentEntry)directory.getEntry("\005SummaryInformation");
			stream	=	new	DocumentInputStream(document);
			ps	=	PropertySetFactory.create(stream);
		}
		catch(Exception ex) {
			//on estime que l'exception est dû au fait que nous ne sommes pas en présence d'un fichier OLE2 (office)
			SilverTrace.warn("MSdocumentPropertiesManager.getSummaryInformation()","SilverpeasException.WARNING","util.EXE_CANT_GET_SUMMARY_INFORMATION");
		}
		finally {
			try {
				stream.close();
			} catch(Exception ex) {}
			try {
				inputStream.close();
			} catch(Exception ex) {}
		}
		return (SummaryInformation) ps;
    }
    public SummaryInformation getSummaryInformation(File fileName)
    {
    	return getSummaryInformation(fileName.getAbsolutePath());
    }
    
	/**
     * Return DocumentSummaryInformation of an Office document
     * @param file
     * @return DocumentSummaryInformation
     */
    public DocumentSummaryInformation getDocumentSummaryInformation(String fileName)
    {
    	InputStream inputStream = null;
    	DocumentInputStream stream = null;
    	PropertySet ps = null;
    	
		try {
	    	inputStream		=	new	FileInputStream(new File(fileName));
	    	POIFSFileSystem fs			=	new POIFSFileSystem(inputStream);
			DirectoryEntry 	directory	=	fs.getRoot();
			DocumentEntry document		=	(DocumentEntry)directory.getEntry("\005DocumentSummaryInformation");
			stream	=	new	DocumentInputStream(document);
			ps	=	(PropertySet) PropertySetFactory.create(stream);
		}
		catch(Exception ex) {
			//on estime que l'exception est dû au fait que nous ne sommes pas en présence d'un fichier OLE2 (office)
			SilverTrace.warn("MSdocumentPropertiesManager.getSummaryInformation()","SilverpeasException.WARNING","util.EXE_CANT_GET_SUMMARY_INFORMATION"+ex.getMessage());
			//System.out.println("pb getDocumentSummaryInformation:"+ex.getMessage());
		}
		finally {
			try {
				stream.close();
			} catch(Exception ex) {}
			try {
				inputStream.close();
			} catch(Exception ex) {}
		}
		return (DocumentSummaryInformation) ps;
    }

	/**
     * Return Title of an Office document
     * @param File
     * @return String
     */
    public String getTitle(String fileName)
    {
    	return getSummaryInformation(fileName).getTitle();	
    }

	/**
     * Return Subject of an Office document
     * @param File
     * @return String
     */
    public String getSubject(String fileName)
    {
    	return getSummaryInformation(fileName).getSubject();
    }

	/**
     * Return Author of an Office document
     * @param File
     * @return String
     */
    public String getAuthor(String fileName)
    {
    	return getSummaryInformation(fileName).getAuthor();
    }

	/**
     * Return Comments of an Office document
     * @param File
     * @return String
     */
    public String getComments(String fileName)
    {
    	return getSummaryInformation(fileName).getComments();
    }

	/**
     * Return Security of an Office document
     * @param File
     * @return String
     */
    public int getSecurity(String fileName)
    {
    	return getSummaryInformation(fileName).getSecurity();
    }

	/**
     * Return Keywords of an Office document
     * @param File
     * @return String
     */
    public String getKeywords(String fileName)
    {
    	return getSummaryInformation(fileName).getKeywords();
    }

	/**
     * Return SILVERID of an Office document
     * @param String
     * @return String
     */
    public String getSilverId(String fileName)
    {
    	return getPropertyValue(fileName, "SILVERID");
    }

	/**
     * Return SILVERNAME of an Office document
     * @param String
     * @return String
     */
    public String getSilverName(String fileName)
    {
    	return getPropertyValue(fileName, "SILVERNAME");
    }

	/**
     * Return LastSaveDateTime of an Office document
     * @param String
     * @return Date
     */
    public Date getLastSaveDateTime(String fileName)
    {
    	return getSummaryInformation(fileName).getLastSaveDateTime();
    }

	/**
     * Return CreateDateTime of an Office document
     * @param String
     * @return Date
     */
    public Date getLastCreateDateTime(String fileName)
    {
    	return getSummaryInformation(fileName).getCreateDateTime();
    }
    
	/**
     * Return if an Office document has a SummaryInformation
     * @param SummaryInformation
     * @return boolean
     */
    public boolean isSummaryInformation(String fileName)
    {
    	boolean isSummaryInformation = false;
    	if (getSummaryInformation(fileName) != null)
    		isSummaryInformation = getSummaryInformation(fileName).isSummaryInformation();
    	return isSummaryInformation;
    }

	/**
     * Return The value of a personalizable Property of an Office document (DocumentSummaryInformation)
     * @param File - The Office document
     * @param String - The name of the property
     * @return String - The Value of the property
     */
    public String getPropertyValue(String fileName, String propertyName)
    {
		return "";
    }
    
}