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
package org.silverpeas.core.contribution.attachment.process.huge;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.util.annotation.SourceObject;
import org.silverpeas.core.util.annotation.SourcePK;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to specify a service method which the treatment MUST abort if a huge processing
 * over attachments is running.
 * <p>
 *   This annotation MUST be used with one of the following annotations on method parameters:
 *   <ul>
 *     <li>@{@link SourcePK}: allows to identify a {@link ResourceReference} into parameters.
 *     From this reference will be extracted the identifier of the component instance involved by
 *     the processing</li>
 *     <li>@{@link Target}: same as {@link SourcePK}</li>
 *     <li>{@link SourceObject}: allows to identify from any object the identifier of the
 *     component instance involved by the processing. If the object is not a
 *     {@link SimpleDocument} one, then the method {@link Object#toString()} of the annotated
 *     object instance MUST return a component instance identifier to be functional</li> </ul>
 *   </ul>
 * </p>
 * @author silveryocha
 */
@Inherited
@Documented
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@InterceptorBinding
@InterceptorBindingOfPreventAttachmentHugeProcess
public @interface PreventAttachmentHugeProcess {
}
