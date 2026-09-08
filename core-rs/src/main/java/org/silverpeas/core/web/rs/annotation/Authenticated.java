/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.web.rs.annotation;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to indicate the access a web resource must be done within an authenticated session.
 * With this annotation, for each incoming request, the authentication of the caller is validated.
 * If the user isn't yet authenticated, an authentication challenge is triggered with the
 * credentials from the Authenticate HTTP header of the request.
 */
@SecurityScheme(name = "silverpeasCredentials", type = SecuritySchemeType.HTTP, scheme = "basic",
    description = "The credentials of the user. The preferred way for a REST client as it offers " +
        "a better scalability.")
@SecurityScheme(name = "silverpeasSession", type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER, paramName = UserPrivilegeValidation.HTTP_SESSIONKEY,
    description = "The key of a session opened by the user, or a user token to perform the " +
        "request without opening any session.")
@SecurityScheme(name = "silverpeasAccessToken", type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.QUERY, paramName = UserPrivilegeValidation.HTTP_ACCESS_TOKEN,
    description = "An access token, for a token based authentication mechanism like OAuth2.")
@SecurityRequirement(name = "silverpeasCredentials")
@SecurityRequirement(name = "silverpeasSession")
@SecurityRequirement(name = "silverpeasAccessToken")
@ApiResponse(responseCode = "401",
    description = "The user isn't authenticated: the credentials are either missing or invalid.")
@InterceptorBinding
@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
public @interface Authenticated {

}
