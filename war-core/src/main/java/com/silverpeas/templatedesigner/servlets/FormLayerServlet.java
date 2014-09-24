package com.silverpeas.templatedesigner.servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;

import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import org.silverpeas.util.FileUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public class FormLayerServlet extends HttpServlet {

  private static final long serialVersionUID = 1013565640540446129L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    HttpSession session = request.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
    if (mainSessionCtrl == null) {
      response.sendRedirect(URLManager.getApplicationURL() + GeneralPropertiesManager.getString("sessionTimeout"));
    }
    
    String pathInfo = request.getPathInfo();
    
    String form = pathInfo.substring(1);
    if (mainSessionCtrl.getCurrentUserDetail().isAccessAdmin()) {
      String filename = request.getParameter("Layer");
      String dir = PublicationTemplateManager.makePath(PublicationTemplateManager.templateDir, form);
      File file = new File(dir, filename);
      response.setContentType(FileUtil.getMimeType(filename));
      response.setHeader("Content-Disposition", "inline; filename=\"" + filename + '"');
      response.setHeader("Content-Length", String.valueOf(file.length()));
      FileUtils.copyFile(file, response.getOutputStream());
      response.getOutputStream().flush();
    } else {
      response.sendRedirect(URLManager.getApplicationURL() + GeneralPropertiesManager.getString("sessionTimeout"));
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

}
