/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.servlets;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.contribution.content.LinkUrlDataSource;
import org.silverpeas.core.contribution.content.LinkUrlDataSourceScanner;
import org.silverpeas.core.contribution.content.wysiwyg.service.directive.ImageUrlAccordingToHtmlSizeDirective;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileDescriptor;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.AbstractFileSender;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.activation.FileDataSource;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.silverpeas.core.contribution.content.LinkUrlDataSourceScanner.extractUrlParameters;
import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexp;
import static org.silverpeas.core.util.StringDataExtractor.from;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.file.FileServerUtils.*;

public class FileServer extends AbstractFileSender {

  private static final long serialVersionUID = 6377810839728682983L;

  @Inject
  private OrganizationController organizationController;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) {
    try {
      final Map<String, String> params = HttpRequest.decorate(req).getParameterSimpleMap();
      final String componentId = params.get(COMPONENT_ID_PARAMETER);
      final HttpSession session = req.getSession(true);
      final MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(
          MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      if ((mainSessionCtrl == null) || (!isUserAllowed(mainSessionCtrl, componentId))) {
        SilverLogger.getLogger(this)
            .warn("Session timeout after {1}. New session id: {0}", session.getId(),
                ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout"));
        res.sendRedirect(URLUtil.getApplicationURL() +
            ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout"));
        return;
      }
      final SilverpeasFile file = getSilverpeasFile(params);
      sendFile(req, res, file);
      final String archiveIt = params.get(ARCHIVE_IT_PARAMETER);
      final String userId = params.get(USER_ID_PARAMETER);
      if (StringUtil.isDefined(archiveIt)) {
        final String nodeId = params.get(NODE_ID_PARAMETER);
        final String pubId = params.get(PUBLICATION_ID_PARAMETER);
        final ResourceReference pubPK = new ResourceReference(pubId, componentId);
        addStatistic(userId, nodeId, pubPK);
      }
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private static SilverpeasFile getSilverpeasFile(final Map<String, String> params) {
    final String componentId = params.get(COMPONENT_ID_PARAMETER);
    final String mimeType = params.get(MIME_TYPE_PARAMETER);
    final String dirType = params.get(DIR_TYPE_PARAMETER);
    final String typeUpload = params.get(TYPE_UPLOAD_PARAMETER);
    final String size = params.get(SIZE_PARAMETER);
    String sourceFile = params.get(SOURCE_FILE_PARAMETER);
    if (StringUtil.isDefined(size)) {
      sourceFile = size + File.separatorChar + sourceFile;
    }
    SilverpeasFileDescriptor descriptor =
        new SilverpeasFileDescriptor(componentId).fileName(sourceFile).mimeType(mimeType);
    if (typeUpload != null) {
      descriptor.absolutePath();
    } else {
      if (dirType != null) {
        if (dirType.equals(
            ResourceLocator.getGeneralSettingBundle().getString("RepositoryTypeTemp"))) {
          descriptor = descriptor.temporaryFile();
        }
      } else {
        String directory = params.get(DIRECTORY_PARAMETER);
        descriptor = descriptor.parentDirectory(directory);
      }
    }
    return SilverpeasFileProvider.getFile(descriptor);
  }

  private void addStatistic(final String userId, final String nodeId,
      final ResourceReference pubPK) {
    try {
      StatisticService statisticService = StatisticService.get();
      statisticService.addStat(userId, pubPK, 1, "Publication");
    } catch (Exception ex) {
      SilverLogger.getLogger(this)
          .error("Cannot write statistics about publication " + pubPK + " in node " + nodeId, ex);
    }
  }

  // check if the user is allowed to access the required component
  private boolean isUserAllowed(MainSessionController controller, String componentId) {
    boolean isAllowed;
    if (componentId == null) {
      // Personal space
      isAllowed = true;
    } else {
      if ("yes".equalsIgnoreCase(
          controller.getComponentParameterValue(componentId, "publicFiles"))) {
        // Case of file contained in a component used as a file storage
        isAllowed = true;
      } else {
        isAllowed =
            organizationController.isComponentAvailableToUser(componentId, controller.getUserId());
      }
    }
    return isAllowed;
  }

  @Override
  protected SettingBundle getSettingBunde() {
    return ResourceLocator.getSettingBundle(
        "org.silverpeas.util.peasUtil.multiLang.fileServerBundle");
  }

  @Singleton
  public static class ImageUrlToDataSourceScanner implements LinkUrlDataSourceScanner {

    private static final Pattern FILESERVER_CONTENT_LINK_PATTERN =
        Pattern.compile("(?i)=\"([^\"]*/FileServer/thumbnail[^\"]+)");

    @Override
    public List<LinkUrlDataSource> scanHtml(final String htmlContent) {
      final List<LinkUrlDataSource> result = new ArrayList<>();
      from(htmlContent).withDirectives(singletonList(regexp(FILESERVER_CONTENT_LINK_PATTERN, 1)))
          .extract()
          .forEach(l -> {
            final Map<String, String> params = extractUrlParameters(l);
            final SilverpeasFile imageFile = getSilverpeasFile(params);
            if (imageFile.exists()) {
              result.add(new LinkUrlDataSource(l, () -> new FileDataSource(imageFile)));
            }
          });
      return result;
    }
  }

  @Singleton
  public static class ImageUrlAccordingToHtmlSizeDirectiveTranslator extends
      ImageUrlAccordingToHtmlSizeDirective.SrcWithSizeParametersTranslator {

    @Override
    public boolean isCompliantUrl(final String url) {
      return defaultStringIfNotDefined(url).contains("/FileServer/thumbnail");
    }
  }
}
