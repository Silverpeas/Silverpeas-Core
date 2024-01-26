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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.ddwe;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.wbe.WbeEdition;
import org.silverpeas.core.web.mvc.webcomponent.WebComponentRequestContext;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.singleton;
import static java.util.Optional.ofNullable;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.READER;
import static org.silverpeas.core.admin.user.model.User.getCurrentRequester;
import static org.silverpeas.kernel.util.StringUtil.getBooleanValue;
import static org.silverpeas.core.web.rs.UserPrivilegeValidation.HTTP_ACCESS_TOKEN;

/**
 * @author silveryocha
 */
public class DragAndDropWebEditorRequestContext extends WebComponentRequestContext<DragAndDropWebEditorController> {

  @Override
  public Collection<SilverpeasRole> getUserRoles() {
    return singleton(getCurrentRequester().isAccessAdmin() ? ADMIN : READER);
  }

  public Optional<WbeEdition> getWbeEdition() {
    return ofNullable(getRequest().getAttribute("wbe_edition")).map(WbeEdition.class::cast);
  }

  public String getAccessToken() {
    return getRequestData(HTTP_ACCESS_TOKEN);
  }

  public String getFileId() {
    return getRequestData("file_id");
  }

  public boolean isLoadFromEditorInitialization() {
    return getBooleanValue(getRequest().getHeader("initialization"));
  }

  private String getRequestData(final String key) {
    return ofNullable(getRequest().getAttribute(key))
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .filter(StringUtil::isDefined)
        .orElseGet(() ->
            ofNullable(getRequest().getParameter(key))
                .filter(StringUtil::isDefined)
                .orElseGet(() ->
                    getRequest().getHeader(key)));
  }
}
