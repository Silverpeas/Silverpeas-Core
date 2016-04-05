package org.silverpeas.web.templatedesigner.servlets;

import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.io.FileUtils;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.ResourceLocator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;

public class FormLayerServlet extends HttpServlet {

  private static final long serialVersionUID = 1013565640540446129L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    HttpSession session = request.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
    if (mainSessionCtrl == null) {
      response.sendRedirect(URLUtil.getApplicationURL() +
          ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout"));
    }

    String pathInfo = request.getPathInfo();

    String form = pathInfo.substring(1);
    if (UserDetail.getCurrentRequester().isAccessAdmin()) {
      String filename = request.getParameter("Layer");
      String dir = PublicationTemplateManager.makePath(PublicationTemplateManager.templateDir, form);
      File file = new File(dir, filename);
      response.setContentType(FileUtil.getMimeType(filename));
      response.setHeader("Content-Disposition", "inline; filename=\"" + filename + '"');
      response.setHeader("Content-Length", String.valueOf(file.length()));
      FileUtils.copyFile(file, response.getOutputStream());
      response.getOutputStream().flush();
    } else {
      response.sendRedirect(URLUtil.getApplicationURL() +
          ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout"));
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

}
