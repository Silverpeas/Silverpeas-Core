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

package org.silverpeas.core.pdc.thesaurus.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.silverpeas.core.pdc.thesaurus.model.ThesaurusException;
import org.silverpeas.core.pdc.thesaurus.model.Jargon;
import org.silverpeas.core.pdc.thesaurus.model.Synonym;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.service.GlobalPdcManager;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.exception.SilverpeasException;

import javax.inject.Inject;

/**
 * Class ThesaurusManager Classe servant d'interface aux autres composants ayant besoin du thesaurus
 */
public class ThesaurusManager {

  @Inject
  private ThesaurusService thesaurus;

  protected ThesaurusManager() {

  }

  /**
   * Retourne la liste des synonymes d'un mot donné (ce mot peut-etre un terme ou un synonyme) pour
   * un utilisateur donné retourne une Collection de String
   * @param mot
   * @param idUser
   * @return Collection
   * @throws ThesaurusException
   */
  public Collection<String> getSynonyms(String mot, String idUser)
      throws ThesaurusException {

    Collection<String> finalList = new ArrayList<String>();
    try {
      // recupere le jargon de l'utilisateur
      Jargon jargon = getJargon(idUser);

      if (jargon != null) {
        long idVoca = jargon.getIdVoca();

        // recupere la liste des mots synonymes du mot si c'est un terme
        Collection<String> synonyms = getSynonymsTerm(idVoca, mot);
        List<String> interList = new ArrayList<String>(synonyms);

        // recupere la liste des mots synonymes du mot si c'est un synonyme
        Collection<String> otherSynonyms = getSynonyms(idVoca, mot);

        interList.addAll(otherSynonyms);

        // parsing de la liste pour enlever les doublons
        for (String synonyme : interList) {
          if ((!isExist(synonyme, finalList))
              && (!mot.toLowerCase().equals(synonyme.toLowerCase()))) {
            finalList.add(synonyme);
          }
        }
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException(
          "ThesaurusManager.getSynonyms(String mot, String idUser)",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS_USER", "",
          e);
    }

    return finalList;
  }

  /**
   * Retourne la liste des mots synonymes d'un terme dans un vocabulaire donné retourne une
   * Collection de String
   * @param idVoca
   * @param term
   * @return Collection
   * @throws ThesaurusException
   */
  private Collection<String> getSynonymsTerm(long idVoca, String term)
      throws ThesaurusException {

    Collection<String> theList = new ArrayList<String>();
    try {
      // recupere les termes correspondant à un nom de terme
      PdcManager pdc = new GlobalPdcManager();
      List<Value> valueList = pdc.getAxisValuesByName(term);

      for (Value value : valueList) {
        long idTree = Long.parseLong(value.getTreeId());
        long idTerm = Long.parseLong(value.getPK().getId());

        // recupere la liste des mots synonymes du terme
        Collection<Synonym> synonymes = thesaurus.getSynonyms(idVoca, idTree, idTerm);

        for (Synonym synonyme : synonymes) {
          String nom = synonyme.getName();
          theList.add(nom);
        }
      }

    } catch (PdcException e) {
      throw new ThesaurusException("ThesaurusManager.getSynonymsTerm",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS_USER", "",
          e);
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusManager.getSynonymsTerm",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS_USER", "",
          e);
    }

    return theList;
  }

  /**
   * Retourne la liste des autres synonymes d'un mot étant un synonyme d'un terme dans un
   * vocabulaire donné retourne une Collection de String
   * @param idVoca
   * @param synonym
   * @return Collection
   * @throws ThesaurusException
   */
  private Collection<String> getSynonyms(long idVoca, String synonym)
      throws ThesaurusException {

    Collection<String> theList = new ArrayList<String>();
    try {
      // recupere les autres synonymes du synonyme dans le vocabulaire
      Collection<Synonym> synonymsList = thesaurus.getSynonyms(idVoca, synonym);

      for (Synonym aSynonym : synonymsList) {
        long idTree = aSynonym.getIdTree();
        long idTerm = aSynonym.getIdTerm();

        // recupere la liste des synonymes du terme dans le vocabulaire
        Collection<Synonym> synonyms = thesaurus.getSynonyms(idVoca, idTree, idTerm);

        for (Synonym synonymTerm : synonyms) {
          String name = synonymTerm.getName();
          theList.add(name);

          // recupere le nom du terme correspondant
          PdcManager pdc = new GlobalPdcManager();
          Value value = pdc.getAxisValue(Long.toString(idTerm), Long.toString(idTree));
          String nameTerm = value.getName();
          theList.add(nameTerm);
        }
      }

    } catch (PdcException e) {
      throw new ThesaurusException(
          "ThesaurusManager.getSynonyms(long idVoca, String synonym)",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS_USER", "",
          e);
    } catch (ThesaurusException e) {
      throw new ThesaurusException(
          "ThesaurusManager.getSynonyms(long idVoca, String synonym)",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS_USER", "",
          e);
    }

    return theList;

  }

  /**
   * Retourne vrai si le nom appartient au tableau
   * @param nom
   * @param tab
   * @return boolean
   * @throws
   * @see
   */
  private boolean isExist(String nom, Collection<String> tab) {
    for (String mot : tab) {
      if (nom.toLowerCase().equals(mot.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Retourne la liste des synonymes d'un terme pour un utilisateur donné retourne une Collection de
   * String
   * @param idTree
   * @param idTerm
   * @param idUser
   * @return Collection
   * @throws ThesaurusException
   */
  public Collection<String> getSynonyms(long idTree, long idTerm, String idUser)
      throws ThesaurusException {

    Collection<String> theList = new ArrayList<String>();
    try {
      // recupere le jargon de l'utilisateur
      Jargon jargon = getJargon(idUser);
      if (jargon != null) {
        long idVoca = jargon.getIdVoca();

        theList.addAll(getSynonyms(idTree, idTerm, idVoca));
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException(
          "ThesaurusManager.getSynonyms(long idTree, long idTerm, String idUser)",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS", "", e);
    }
    return theList;
  }

  public Collection<String> getSynonyms(long idTree, long idTerm, long idVoca)
      throws ThesaurusException {

    List<String> theList = new ArrayList<String>();

    // recupere la liste des synonymes du terme dans le vocabulaire
    Collection<Synonym> synonyms = thesaurus.getSynonyms(idVoca, idTree, idTerm);

    for (Synonym synonym : synonyms) {
      theList.add(synonym.getName());
    }
    return theList;
  }

  public Collection<Synonym> getSynonymsByTree(long idTree, long idVoca)
      throws ThesaurusException {
    return thesaurus.getSynonymsByTree(idVoca, idTree);
  }

  /**
   * Retourne la liste des synonymes d'un terme pour un utilisateur donné, à partir de son axe
   * retourne une Collection de String
   * @param axisId
   * @param idUser
   * @return Collection
   * @throws ThesaurusException
   */
  public Collection<String> getSynonymsAxis(String axisId, String idUser)
      throws ThesaurusException {

    Collection<String> theList = new ArrayList<String>();
    try {
      // recupere le jargon de l'utilisateur
      Jargon jargon = getJargon(idUser);
      if (jargon != null) {
        long idVoca = jargon.getIdVoca();

        // recupere le treeId et le idTerm de l'axe
        PdcManager pdc = new GlobalPdcManager();
        Value value = pdc.getRoot(axisId);

        long idTree = Long.parseLong(value.getTreeId());
        long idTerm = Long.parseLong(value.getPK().getId());

        // recupere la liste des synonymes du terme dans le vocabulaire
        Collection<Synonym> synonyms = thesaurus.getSynonyms(idVoca, idTree, idTerm);

        for (Synonym synonym : synonyms) {
          theList.add(synonym.getName());
        }
      }

    } catch (PdcException e) {
      throw new ThesaurusException(
          "ThesaurusManager.getSynonymsAxis(String axisId, String idUser)",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS", "", e);
    } catch (ThesaurusException e) {
      throw new ThesaurusException(
          "ThesaurusManager.getSynonymsAxis(String axisId, String idUser)",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS", "", e);
    }

    return theList;
  }

  /**
   * Retourne le jargon utilisé par l'utilisateur retourne un Jargon ou null s'il n'a pas de jargon
   * @param idUser
   * @return Jargon
   * @throws ThesaurusException
   */
  public Jargon getJargon(String idUser) throws ThesaurusException {

    Jargon jargon = null;
    try {
      jargon = thesaurus.getJargon(idUser);
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusManager.getJargon",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_JARGON", "", e);
    }

    return jargon;
  }

  /**
   * Supprime les synonymes de tous les termes associés à l'axe passé en paramètre
   * @param idTree
   * @return
   * @throws ThesaurusException
   */
  public void deleteSynonymsAxis(Connection con, long idTree)
      throws ThesaurusException {

    try {
      thesaurus.deleteSynonymsAxis(con, idTree);
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusManager.deleteSynonymsAxis",
          SilverpeasException.ERROR,
          "Thesaurus.EX_DELETE_SYNONYMS_AXIS_FAILED", "", e);
    }
  }

  /**
   * Supprime les synonymes de tous les termes passés en paramètre
   * @param idTree
   * @param idTerms : List de String
   * @return
   * @throws ThesaurusException
   */
  public void deleteSynonymsTerms(Connection con, long idTree, List<String> idTerms)
      throws ThesaurusException {

    try {
      thesaurus.deleteSynonymsTerms(con, idTree, idTerms);
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusManager.deleteSynonymsTerms",
          SilverpeasException.ERROR,
          "Thesaurus.EX_DELETE_SYNONYMS_TERMS_FAILED", "", e);
    }
  }

}
