/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.webapi.pdc;

import org.silverpeas.core.pdc.thesaurus.model.ThesaurusException;
import org.silverpeas.core.pdc.thesaurus.service.ThesaurusManager;
import org.silverpeas.core.pdc.thesaurus.model.Jargon;
import org.silverpeas.core.admin.user.model.UserDetail;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A holder of the thesaurus for a given user. It provides convenient methods to access thesaurus
 * data for a given user.
 */
public class UserThesaurusHolder {

  private ThesaurusManager thesaurus = org.silverpeas.core.pdc.PdcServiceProvider.getThesaurusManager();
  private UserDetail user;

  public static UserThesaurusHolder holdThesaurus(final UserDetail user) {
    return new UserThesaurusHolder(user);
  }

  /**
   * A convinient method to enhance the readability of the methods.
   * @param user details about a user.
   * @return the user details.
   */
  public static UserDetail forUser(final UserDetail user) {
    return user;
  }

  /**
   * Gets the synonyms of the specified position value by using the hold user thesaurus. The
   * synonyms are thoses of the term backed by the value.
   * @param value the value as a PdcPositionValue instance.
   * @return a collection of synonyms.
   * @throws ThesaurusException if an error occurs while accessing the thesaurus for getting the
   * synonyms of the specified term.
   */
  public Collection<String> getSynonymsOf(final PdcValueEntity value) throws ThesaurusException {
    Collection<String> synonyms = null;
    Jargon jargon = getThesaurus().getJargon(getUser().getId());
    if (jargon != null && value.belongToATree()) {
      String idUser = jargon.getIdUser();
      synonyms = thesaurus.getSynonyms(Long.valueOf(value.getTreeId()),
          Long.valueOf(value.getTermId()), idUser);
    }
    if (synonyms == null) {
      synonyms = new ArrayList<>();
    }
    return synonyms;
  }

  protected ThesaurusManager getThesaurus() {
    return thesaurus;
  }

  protected UserDetail getUser() {
    return user;
  }

  private UserThesaurusHolder(final UserDetail user) {
    this.user = user;
  }
}
