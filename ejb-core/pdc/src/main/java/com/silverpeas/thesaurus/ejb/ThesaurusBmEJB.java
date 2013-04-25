/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.thesaurus.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.thesaurus.control.ThesaurusManager;
import com.silverpeas.thesaurus.model.Synonym;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

@Stateless(name = "Thesaurus", description = "Stateless EJB to access the thesaurus.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class ThesaurusBmEJB implements ThesaurusBm {

  private static final long serialVersionUID = 1L;
  private ThesaurusManager thesaurus = null;

  public ThesaurusManager getThesaurus() {
    if (thesaurus == null) {
      thesaurus = new ThesaurusManager();
    }
    return thesaurus;
  }

  public ThesaurusBmEJB() {
  }

  @Override
  public List<String> getSynonyms(long idTree, long idTerm, long idVoca) {
    try {
      return (List<String>) getThesaurus().getSynonyms(idTree, idTerm, idVoca);
    } catch (ThesaurusException e) {
      throw new ThesaurusBmRuntimeException("ThesaurusBmEJB.getSynonyms",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", "idTree = " + idTree
          + ", idTerm = " + idTerm + ", idVoca = " + idVoca, e);
    }
  }

  @Override
  public List<Synonym> getSynonymsByTree(long idTree, long idVoca) {
    try {
      return (List<Synonym>) getThesaurus().getSynonymsByTree(idTree, idVoca);
    } catch (ThesaurusException e) {
      throw new ThesaurusBmRuntimeException("ThesaurusBmEJB.getSynonymsByTree",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", "idTree = " + idTree
          + ", idVoca = " + idVoca, e);
    }
  }
}