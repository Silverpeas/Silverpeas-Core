/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.MissingResourceException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.indexEngine.parser.excelParser.ExcelParser;
import com.stratelia.webactiv.util.indexEngine.parser.pptParser.PptParser;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class FileServer extends HttpServlet
{

    HttpSession session;
    PrintWriter out;
    
    public static String getUrl(String spaceId, String componentId, String logicalName, String physicalName, String mimeType, String subDirectory)
    {
        return FileServerUtils.getUrl(spaceId, componentId, logicalName, physicalName, mimeType, subDirectory);
    }

    public static String getUrl(String logicalName, String physicalName, String mimeType)
    {
        return FileServerUtils.getUrl(logicalName, physicalName, mimeType);
    }

    public static String getUrl(String spaceId, String componentId, String name, String mimeType, String subDirectory)
    {
        return FileServerUtils.getUrl(spaceId, componentId, name, name, mimeType, subDirectory);
    }

    public static String getUrl(String spaceId, String componentId, String userId, String logicalName, String physicalName, String mimeType, boolean archiveIt, int pubId, int nodeId, String subDirectory)
    {
        return FileServerUtils.getUrl(spaceId, componentId, userId, logicalName, physicalName, mimeType, archiveIt, pubId, nodeId, subDirectory);
    }

    public static String getUrlToTempDir(String logicalName, String physicalName, String mimeType)
    {
        return FileServerUtils.getUrlToTempDir(logicalName, physicalName, mimeType);
    }

    public void init(ServletConfig config)
    {
        try
        {
            super.init(config);
        }
        catch (ServletException se)
        {
            SilverTrace.fatal("peasUtil", "FileServer.init", "peasUtil.CANNOT_ACCESS_SUPERCLASS");
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        doPost(req, res);
    }

    /**
     * Method declaration
     * 
     * 
     * @param req
     * @param res
     * 
     * @throws IOException
     * @throws ServletException
     * 
     * @see
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        SilverTrace.info("peasUtil", "FileServer.doPost", "root.MSG_GEN_ENTER_METHOD");
        String mimeType = req.getParameter("MimeType");
        String sourceFile = req.getParameter("SourceFile");
        String directory = req.getParameter("Directory");
        String archiveIt = req.getParameter("ArchiveIt");
        String dirType = req.getParameter("DirType");
        String userId = req.getParameter("UserId");
        String spaceId = req.getParameter("SpaceId");
        String componentId = req.getParameter("ComponentId");
        String typeUpload = req.getParameter("TypeUpload");
        // Add By Mohammed Hguig
        String htmlisation = req.getParameter("htmlisation");
        String zip = req.getParameter("zip");
        String fileName = req.getParameter("fileName");
        String tempDirectory = FileRepositoryManager.getTemporaryPath(spaceId, componentId);
        String command = null;
        File   tempFile = null;
        // End Add By Mohammed Hguig
        
        String attachmentId = req.getParameter("attachmentId");
        String language		= req.getParameter("lang");
        AttachmentDetail attachment = null;
        if (StringUtil.isDefined(attachmentId))
        {
        	//Check first if attachment exists
        	attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(attachmentId));
        	if (attachment != null)
        	{
        		mimeType = attachment.getType(language);
        		sourceFile = attachment.getPhysicalName(language);
        		directory = FileRepositoryManager.getRelativePath(FileRepositoryManager.getAttachmentContext(attachment.getContext()));
        	}
        }
        
        String documentId = req.getParameter("DocumentId");
        if (StringUtil.isDefined(documentId))
        {
        	String versionId = req.getParameter("VersionId");
        	VersioningUtil versioning = new VersioningUtil();
        	DocumentVersionPK versionPK = new DocumentVersionPK(Integer.parseInt(versionId), "useless", componentId);
        	DocumentVersion version = versioning.getDocumentVersion(versionPK);
        	
        	if (version != null)
        	{
        		mimeType = version.getMimeType();
        		sourceFile = version.getPhysicalName();

        		String[] path = new String[1];
        		path[0] = "Versioning";
        		directory = FileRepositoryManager.getRelativePath(path);
        	}
        }

        String filePath = null;

        HttpSession session = req.getSession(true);
        MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
        if ((mainSessionCtrl == null) || (!isUserAllowed(mainSessionCtrl,spaceId,componentId)))
        {
            SilverTrace.warn("peasUtil", "FileServer.doPost", "root.MSG_GEN_SESSION_TIMEOUT", "NewSessionId=" + session.getId() + GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")+GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout"));
            res.sendRedirect(GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")+GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout"));
        }
        if (typeUpload != null)
        {
            filePath = sourceFile;
        }
        else
        {
            if (dirType != null)
            {
                if (dirType.equals(GeneralPropertiesManager.getGeneralResourceLocator().getString("RepositoryTypeTemp")))
                {
                    filePath = FileRepositoryManager.getTemporaryPath(spaceId, componentId) + sourceFile;
                }
            }
            else
            {
                // the file to download is not in a temporary directory
                filePath = FileRepositoryManager.getAbsolutePath(componentId) + directory + File.separator + sourceFile;
            }

        }

        res.setContentType(mimeType);
        //res.setHeader("Cache-Control", "no-store");

        // Add By Mohammed Hguig
        if (htmlisation != null)
        {
            res.setContentType("text/html");

			// 2 ways to call the parser, the first one: CMD with a single file as source and redirect result to the stdout
			//                            second one: CMD with inpout output in 2 separate files.
			if (mimeType.equals("application/msword") )
			{
					// OLD cmd: (please keep this commented line, maybe usefull.
					//command = getParser(mimeType) + " -o " + tempFile.getCanonicalPath() + " " + filePath;
				Process process;
				command = getParser(mimeType) + " " + filePath;
				try
				{
				  SilverTrace.info("peasUtil", "FileServer", "root.MSG_GEN_PARAM_VALUE",
								  "Will start to htmlize file, cmd= " + command);
				  process = Runtime.getRuntime().exec(command);
				  if (process == null)
				  {
					SilverTrace.fatal("peasUtil", "FileServer",
									  "indexEngine.MSG_PARSER_EXEC_FAILED", command);
					return;
				  }
				}
				catch (IOException e)
				{
					SilverTrace.fatal("peasUtil", "FileServer",
									  "indexEngine.MSG_PARSER_EXEC_FAILED", command, e);
				  return;
				}

				try
				{
				  FileWriter out = new FileWriter(tempDirectory+"resultParse.html");	// create the TEMP file
				  Reader outProcess = new BufferedReader(
										new InputStreamReader(
										  process.getInputStream()));
				  char[] buffer = new char[1024];
				  int countRead, totalRead=0;
				  int limitSize = 50000;		// limit to 50 KO
				  while ((countRead = outProcess.read(buffer)) != -1)
				  {
					totalRead+=countRead;
					if ( totalRead <= limitSize)
					{
					out.write(buffer, 0, countRead);
					}
				  }
				  out.close();
				  tempFile = new File(tempDirectory+"resultParse.html");
				}
				catch (IOException e)
				{
					process.destroy();
					SilverTrace.error("peasUtil", "FileServer",
									  "indexEngine.MSG_IO_ERROR_WHILE_PARSING", command, e);
				}

			// new way to wait for the parser end by itself (not killed after timeout).
			// PHiL 03/02/2004
				while (true)
				{
				  try
				  {
					Integer returnCode = new Integer(process.waitFor());

					if (returnCode.intValue() != 0)
					{
							SilverTrace.warn("indexEngine", "ExecViaTempParser", "root.EX_IGNORED", "error_level= "+returnCode.toString()+" cmd= "+command);
					}
					break ;
				  }
				  catch (InterruptedException e) 
				  {
					  //wait
				  }
				}

				SilverTrace.info("indexEngine", "ExecViaTempParser", "root.MSG_GEN_PARAM_VALUE",
								  "Finish to parse file, cmd= " + command);
			}
			else
			{
				if (mimeType.equals("application/x-msexcel") || mimeType.equals("application/vnd.ms-excel"))
				{
					// command = getParser(mimeType)+" "+filePath+" > "+tempFile.getCanonicalPath();
					Writer outpars = res.getWriter();

					new ExcelParser().outPutContent(outpars, filePath, "");
					return;
				}

				if (mimeType.equals("application/x-mspowerpoint") || mimeType.equals("application/vnd.ms-powerpoint"))
				{
					// command = getParser(mimeType)+" -o "+tempFile.getCanonicalPath()+" "+filePath;
					Writer outpars = res.getWriter();

					new PptParser(getParser(mimeType), "").outPutContent(outpars, filePath, null);
					return;
				}


				if (mimeType.equals("application/pdf"))
				{

					/*
					 * Writer outpars = res.getWriter();
					 * new PdfParser().outPutContent(outpars, filePath, "");
					 * return;
					 */
					// command = getParser(mimeType)+" -i -noframes -l 5 "+filePath+" "+tempFile.getCanonicalPath();

					// with -i option, we don't want an image
					// with -l 1 we want only one html page
					command = getParser(mimeType) + " -i -c -l 1 " + filePath + " " + tempDirectory+"resultParse.html";

					// here, we rebuild the new tempFile File.
					// with the -c option, this tool (pdf2html) transforms the pdf into 3 html files
					// resultPage1234.html (a frameset html page)
					// resultPage1234-1.html (the file we must show)
					// resultPage1234_ind.html (
					//String filename = tempFile.getCanonicalPath();
					//int    extension_idx = filename.indexOf(".html");
					//String filename_withoutExtension = filename.substring(0, extension_idx);
					//tempFile = new File(filename_withoutExtension + "-1.html");
					tempFile = new File(tempDirectory+"resultParse-1.html");
					// expurge
					//filename = null;
					//filename_withoutExtension = null;
				}

				if (mimeType.equals("text/richtext"))
				{
					command = getParser(mimeType) + " -o " + tempFile.getCanonicalPath() + " " + filePath;
				}
				SilverTrace.info("PeasUtil", "FilServer.doPost()", "root.MSG_GEN_ENTER_METHOD", " SEA htmlisation command : " + command);

				if (command != null)
				{
					Process process;
					try
					{
						process = Runtime.getRuntime().exec(command);
						if (process == null)
						{
							SilverTrace.fatal("peasUtil", "FileServer.doPost()", "peasUtil.MSG_PARSER_EXEC_FAILED", command);
							return;
						}
					}
					catch (IOException e)
					{
						SilverTrace.fatal("peasUtil", "FileServer.doPost()", "peasUtil.MSG_PARSER_EXEC_FAILED", command);
						return;
					}

					try
					{
						// Ou putain!!! Virez moi ça!!!
						// TODO :)
						synchronized (process)
						{
							process.wait(3000);
						}
						process.destroy();
					}
					catch (InterruptedException e)
					{
						SilverTrace.warn("peasUtil", "FileServer.doPost()", "peasUtil.MSG_PROCESS_INTERRUPTED_BEFORE_PARSING_FILE", filePath, e);
					}
					catch (IllegalMonitorStateException e)
					{
						SilverTrace.warn("peasUtil", "FileServer.doPost()", "peasUtil.MSG_CURRENT_THREAD_IS_NOT_THE_OWNER_OBJECT_MONITOR", filePath, e);
					}
					catch (IllegalArgumentException e)
					{
						SilverTrace.warn("peasUtil", "FileServer.doPost()", "peasUtil.MSG_ILLEGAL_ARGUMENT", "argument = 3000", e);
					}
					filePath = tempFile.getCanonicalPath();
				}
			}
        }

        SilverTrace.debug("peasUtil", "FileServer.doPost()", "root.MSG_GEN_PARAM_VALUE", " zip="+zip);
        if (zip != null)
        {
            res.setContentType("application/x-zip-compressed");
            tempFile = File.createTempFile("zipfile", ".zip", new File(tempDirectory));
            SilverTrace.debug("peasUtil", "FileServer.doPost()", "root.MSG_GEN_PARAM_VALUE", " filePath ="+filePath+" tempFile.getCanonicalPath()="+tempFile.getCanonicalPath()+" fileName="+fileName);
            zipFile(filePath, tempFile.getCanonicalPath(), fileName);
            filePath = tempFile.getCanonicalPath();
        }

        // End Add By Mohammed Hguig
        // display the preview code generated by the production tools
        if (zip == null)
        {
			if (tempFile !=  null)
			{
				displayHtmlCode(res, tempFile.getCanonicalPath());
			}
			else
			{
				if (attachment != null)
					res.setContentLength(new Long(attachment.getSize()).intValue());
				displayHtmlCode(res, filePath);
			}
        }
        
        if (tempFile != null)
        {
            SilverTrace.info("peasUtil", "FileServer.doPost()", "root.MSG_GEN_ENTER_METHOD", " tempFile != null " + tempFile);
            tempFile.delete();
            // the tool which transforms pdf file to html, creates 3 files with the -c option
            // resultPage1234.html, resultPage1234-1.html and resultPage1234_ind.html
            // in the solution with pdf files, the files resultPage1234.html and resultPage1234_ind.html are not deleted
            // we test the mime-type and we delete the files resultPage1234.html and resultPage1234_ind.html too.
            /*if (mimeType.equals("application/pdf"))
            {
                String canonicalPath = tempFile.getCanonicalPath();
                String name = canonicalPath.substring(0, canonicalPath.indexOf("-1.html"));
                File   indexFileName = new File(name + "_ind.html");
                File   pureFileName = new File(name + ".html");

                // delete
                indexFileName.delete();
                pureFileName.delete();
                // expurge
                name = null;
                canonicalPath = null;
            }*/
        }


        if ((archiveIt != null) && (archiveIt.length() > 0) && (!archiveIt.equals("null")))
        {
            String        nodeId = req.getParameter("NodeId");
            String        pubId = req.getParameter("PubId");
            NodePK        nodePK = null;
            PublicationPK pubPK = null;

            try
            {
                StatisticBmHome statisticBmHome = (StatisticBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBmHome.class);
                StatisticBm     statisticBm = statisticBmHome.create();

                nodePK = new NodePK(nodeId, spaceId, componentId);
                pubPK = new PublicationPK(pubId, spaceId, componentId);
                //statisticBm.addReading(userId, nodePK, pubPK);
                statisticBm.addStat(userId, new ForeignPK(pubPK), 1, "Publication");
            }
            catch (Exception e)
            {
                SilverTrace.warn("peasUtil", "FileServer.doPost", "peasUtil.CANNOT_WRITE_STATISTICS", "pubPK = " + pubPK.toString() + " and nodePK = " + nodePK.toString(), e);
            }
        }
    }

    // Add By Mohammed Hguig

    // check if the user is allowed to access the required component
    private boolean isUserAllowed(MainSessionController controller, String spaceId, String componentId) {
        boolean isAllowed = false;

        if(spaceId  == null && componentId == null)
        {   // Personal space
            isAllowed = true;
        }
        else
        {
        	isAllowed = controller.getOrganizationController().isComponentAvailable(componentId, controller.getUserId());
        }

        return isAllowed;
    }

    /**
     * Methode declaration
     * 
     * @param mimeType
     * 
     * @Get the corrasponding parser
     * @for given mime Type
     */
    private String getParser(String mimeType)
    {
        ResourceLocator resourceLocator = null;
        String          parser = null;

        try
        {
            resourceLocator = new ResourceLocator("com.stratelia.webactiv.util.peasUtil.htmlisation", "");


            if (resourceLocator != null)
            {
                parser = resourceLocator.getString(mimeType);
            }
        }
        catch (MissingResourceException e)
        {
            SilverTrace.warn("peasUtil", "FileServer", "peasUtil.MSG_MISSING_PEASUTIL_PROPERTIES", null, e);
        }
        return parser;
    }

    /**
     * Methode declaration
     * @param filePath
     * @param zipFilePath
     * 
     * @zip a given file
     */
    private void zipFile(String filePath, String zipFilePath, String fileName)
    {
        int compressionLevel = Deflater.DEFAULT_COMPRESSION;
        SilverTrace.debug("peasUtil", "FileServer.zipFile()", "root.MSG_GEN_PARAM_VALUE", " fileName ="+fileName);

        try
        {
            // create and initialize a stream to write it
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFilePath));

            // zip.setComment("created by Mohammed Hguig - http://www.silverpeas.com");
            zip.setMethod(ZipOutputStream.DEFLATED);
            zip.setLevel(compressionLevel);

            // read a file into memory
            File            file = new File(filePath);
            FileInputStream in = new FileInputStream(file);
            byte[]          bytes = new byte[in.available()];

            in.read(bytes);
            in.close();

            // create and initialize a zipentry for it
            // ZipEntry entry = new ZipEntry(file.getName());
            ZipEntry entry = new ZipEntry(fileName);

            entry.setTime(file.lastModified());

            // write the entry header, and the data, to the zip
            zip.putNextEntry(entry);
            zip.write(bytes);

            // write the end-of-entry marker to the zip
            zip.closeEntry();

            // no more files, close the zip. This writes the zip
            // directory, so don't forget it.
            zip.close();
        }
        catch (Exception e)
        {
            SilverTrace.warn("peasUtil", "FileServer", "peasUtil.MSG_ZIP_FILE_FAIL", "file to zip = " + filePath, e);
        }
    }

    // End Add By Mohammed Hguig

    /**
     * This method writes the result of the preview action.
	 * @param res - The HttpServletResponse where the html code is write
	 * @param htmlFilePath - the canonical path of the html document generated by the parser tools. if this String is null that an exception had been catched
	 * the html document generated is empty !! also, we display a warning html page
     */
    private void displayHtmlCode(HttpServletResponse res, String htmlFilePath) throws IOException
    {
        OutputStream        out2 = res.getOutputStream();
        int                 read;
        BufferedInputStream input = null; // for the html document generated
        SilverTrace.info("peasUtil", "FileServer.displayHtmlCode()", "root.MSG_GEN_ENTER_METHOD", " htmlFilePath "+htmlFilePath);
        try
        {
                input = new BufferedInputStream(new FileInputStream(htmlFilePath));
                read = input.read();
				SilverTrace.info("peasUtil", "FileServer.displayHtmlCode()", "root.MSG_GEN_ENTER_METHOD", " BufferedInputStream read "+read);
                if (read == -1){
                	displayWarningHtmlCode(res);
                } else {
					while (read != -1)
					{
						out2.write(read); // writes bytes into the response
						read = input.read();
					}
				}
        }
        catch (Exception e)
        {
            SilverTrace.warn("peasUtil", "FileServer.doPost", "root.EX_CANT_READ_FILE", "file name=" + htmlFilePath);
           displayWarningHtmlCode(res);
        }
        finally
        {
            SilverTrace.info("peasUtil", "FileServer.displayHtmlCode()", "", " finally ");
            // we must close the in and out streams
            try
            {
                if (input != null)
                {
                    input.close();
                }
                out2.close();
            }
            catch (Exception e)
            {
                SilverTrace.warn("peasUtil", "FileServer.displayHtmlCode", "root.EX_CANT_READ_FILE", "close failed");
            }
        }
    }

	
    private void displayWarningHtmlCode(HttpServletResponse res) throws IOException{
        StringReader        sr = null; 
        OutputStream        out2 = res.getOutputStream();
        int                 read;
		ResourceLocator resourceLocator = new ResourceLocator("com.stratelia.webactiv.util.peasUtil.multiLang.fileServerBundle", "");

		sr = new StringReader(resourceLocator.getString("warning"));
		try{
			read = sr.read();
			while (read != -1){
				SilverTrace.info("peasUtil", "FilServer.displayHtmlCode()", "root.MSG_GEN_ENTER_METHOD", " StringReader read "+read);
				out2.write(read); // writes bytes into the response
				read = sr.read();
			}
		} catch (Exception e){
            SilverTrace.warn("peasUtil", "FileServer.displayWarningHtmlCode", "root.EX_CANT_READ_FILE", "warning properties");
		} finally {
			try{
                if (sr != null)
                    sr.close();
                out2.close();
			} catch (Exception e){
                SilverTrace.warn("peasUtil", "FileServer.displayHtmlCode", "root.EX_CANT_READ_FILE", "close failed");
			}
		}
	}
}
