/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.i18n;

/**
 * A translation of a text, a property of a resource supporting the i18n features. Such a bean
 * can be a user's contribution or a contribution's content or a transverse business entity in
 * Silverpeas. All objects that represent a translation of a given i18n or l10n bean
 * must implement either this interface or one of its more-typed children interfaces/classes.
 * <p>
 *   For example, as the <code>Publication</code> is a i18n contribution, it must be able to
 *   return a <code>PublicationTranslation</code> instance for each translation asked in a given
 *   language. Another example, as the PdC's axis is a i18n entity, it must be able to return
 *   a <code>AxisTranslation</code> instance for each translation asked in a given language.
 * </p>
 * @author mmoquillon
 */
public interface Translation {

  String getLanguage();
}
