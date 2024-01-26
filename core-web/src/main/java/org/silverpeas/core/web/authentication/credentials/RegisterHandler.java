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
package org.silverpeas.core.web.authentication.credentials;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.UserRegistrationService;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Navigation case : user has not an account yet and submits registration form.
 */
public class RegisterHandler extends FunctionHandler {

  /**
   * The request attribute key for the registration token.
   */
  public static final String REGISTRATION_TOKEN = "registrationToken";
  private RegistrationSettings settings = RegistrationSettings.getSettings();

  @Override
  public String doAction(HttpServletRequest request) {
    HttpRequest req = HttpRequest.decorate(request);
    String firstName = req.getParameter("firstName");
    String lastName = req.getParameter("lastName");
    String email = req.getParameter("email");
    String domainId = settings.userSelfRegistrationDomainId();

    if (settings.isUserSelfRegistrationEnabled()) {
      try {
        UserRegistrationService service = UserRegistrationService.get();
        String userId = service.registerUser(firstName, lastName, email, domainId);

        processDataOfExtraTemplate(userId, req);

        saveAvatar(req, UserFull.getById(userId).getAvatarFileName());
      } catch (AdminException e) {
        return "/admin/jsp/registrationFailed.jsp";
      }
      return "/admin/jsp/registrationSuccess.jsp";
    } else {
      SilverLogger.getLogger(this).warn(
          "A user is trying to register himself although this capability is deactived! Registration information: [firstname: {0}, lastname: {1}, email: {2}]",
          firstName, lastName, email);
      return "";
    }
  }

  private void processDataOfExtraTemplate(String userId, HttpRequest request) {
    PagesContext context = getTemplateContext(userId);
    context.setDomainId(settings.userSelfRegistrationDomainId());
    PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
    PublicationTemplate template = templateManager.getDirectoryTemplate();
    if (template != null) {
      try {
        templateManager.saveData(template.getFileName(), context, request.getFileItems());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  private PagesContext getTemplateContext(String userId) {
    return PagesContext.getDirectoryContext(userId, userId, I18NHelper.DEFAULT_LANGUAGE);
  }

  private String saveAvatar(HttpRequest request, String nameAvatar) {
    List<FileItem> parameters = request.getFileItems();
    FileItem file = FileUploadUtil.getFile(parameters, "avatar");
    if (file != null && StringUtil.isDefined(file.getName())) {
      String extension = FileRepositoryManager.getFileExtension(file.getName());
      if (extension != null && extension.equalsIgnoreCase("jpeg")) {
        extension = "jpg";
      }

      if ("gif".equalsIgnoreCase(extension) || "jpg".equalsIgnoreCase(extension) ||
          "png".equalsIgnoreCase(extension)) {
        try (InputStream fis = file.getInputStream()) {
          SilverpeasFile image = SilverpeasFileProvider.newFile(getImagePath(nameAvatar));
          image.writeFrom(fis);
        } catch (IOException e) {
          SilverLogger.getLogger(this).error(e);
        }
      }
    }
    return nameAvatar;
  }

  private String getImagePath(String fileName) {
    return FileRepositoryManager.getAvatarPath() + File.separatorChar + fileName;
  }
}
