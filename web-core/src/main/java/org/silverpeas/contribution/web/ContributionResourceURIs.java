/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.contribution.web;


import org.silverpeas.web.util.URIUtil;

import java.net.URI;

/**
 * Base URIs from which the REST-based ressources representing password entities are defined.
 * @author Yohann Chastagnier
 */
public final class ContributionResourceURIs {

  public static final String CONTRIBUTION_BASE_URI = "contribution";
  public static final String CONTRIBUTION_CONTENT_URI_PART = "content";
  public static final String CONTRIBUTION_CONTENT_FORM_URI_PART = "form";

  /**
   * Builds a contribution form content URI
   * @param contributionResource
   * @return
   */
  public static URI buildURIOfContributionFormContent(
      AbstractContributionResource contributionResource, String formId) {
    return URIUtil.buildURI(contributionResource.getUriInfo(), CONTRIBUTION_BASE_URI,
        contributionResource.getComponentId(), contributionResource.getContributionId(),
        CONTRIBUTION_CONTENT_URI_PART, CONTRIBUTION_CONTENT_FORM_URI_PART, formId);
  }
}
