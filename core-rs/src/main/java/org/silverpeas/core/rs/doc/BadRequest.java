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
package org.silverpeas.core.rs.doc;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Documents that the annotated endpoint can answer a 400 (Bad Request) to the requester.
 * <p>
 * The annotation carries nothing but documentation: it has no effect at runtime and it is read
 * only when generating the documentation of the REST API. Its purpose is to declare in a single
 * place the wording of a response that is common to a lot of endpoints.
 * </p>
 * <p>
 * Beware the description brought by this annotation takes precedence over any
 * {@link ApiResponse} the endpoint would declare for the same status code. An endpoint requiring
 * a more specific wording must therefore declare its own {@link ApiResponse} and <em>not</em> be
 * annotated with this annotation.
 * </p>
 * @author Miguel Moquillon
 */
@ApiResponse(responseCode = "400", description = "The request is malformed.")
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface BadRequest {
}
