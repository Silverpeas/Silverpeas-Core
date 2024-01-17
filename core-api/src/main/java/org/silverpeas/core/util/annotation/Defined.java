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
 * "https://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.util.annotation;

import org.silverpeas.core.util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifier;
import javax.annotation.meta.TypeQualifierValidator;
import javax.annotation.meta.When;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotated {@link String} must be defined.
 * <p>
 * Annotated fields must be defined after construction has completed.
 * <p>
 * When this annotation is applied to a method it applies to the method return value.
 * @author mmoquillon
 * @see StringUtil#isDefined(String)
 */
@Documented
@TypeQualifier(applicableTo = String.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Defined {

  When when() default When.ALWAYS;

  class Checker implements TypeQualifierValidator<Defined> {

    @Nonnull
    public When forConstantValue(@Nonnull Defined qualifierArgument, Object value) {
      if (! (value instanceof String)) {
        return When.NEVER;
      }
      return StringUtil.isDefined((String) value) ? When.ALWAYS : When.NEVER;
    }
  }
}
