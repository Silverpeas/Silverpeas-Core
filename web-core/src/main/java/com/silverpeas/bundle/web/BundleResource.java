/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.bundle.web;

import com.silverpeas.annotation.Authenticated;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.MissingResourceException;
import java.util.Properties;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The bundle resource represents in the WEB a localized bundle of messages. This WEB service is an
 * entry point to access the different bundles in use in Silverpeas. It can be accessed only by
 * authenticated users. Ony the localized texts can be actually accessed, no settings.
 *
 * The localized bundled is refered in the URI by its absolute classpath location and only the one
 * that matches the prefered language of the current user in the session is taken into account. If
 * no bunble exists in the language of the current user, then the default one is sent back.
 */
@Service
@Scope("request")
@Path("bundles")
@Authenticated
public class BundleResource extends RESTWebService {

  @Override
  public String getComponentId() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @GET
  @Path("{bundle: com/[a-zA-Z0-9/]+}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getBundle(@PathParam("bundle") final String bundle) throws IOException {
    String language = getUserPreferences().getLanguage();
    ResourceLocator resource = new ResourceLocator(bundle, language);
    try {
      Properties translations = resource.getProperties();
      if (!bundle.trim().isEmpty() && bundle.contains("multilang")) {
        StringWriter messages = new StringWriter();
        translations.store(messages, bundle + " - " + language);
        return Response.ok(messages.toString()).build();
      } else {
        return Response.status(Response.Status.FORBIDDEN).entity(
                "It is not a localized bundle with translations").build();
      }
    } catch (MissingResourceException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }
}
