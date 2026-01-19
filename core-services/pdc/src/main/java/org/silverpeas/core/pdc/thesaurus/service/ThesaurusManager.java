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
package org.silverpeas.core.pdc.thesaurus.service;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.thesaurus.model.Jargon;
import org.silverpeas.core.pdc.thesaurus.model.Synonym;
import org.silverpeas.core.pdc.thesaurus.model.ThesaurusException;

import jakarta.inject.Inject;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class ThesaurusManager Classe servant d'interface aux autres composants ayant besoin du thesaurus
 */
@Service
public class ThesaurusManager {

  @Inject
  private ThesaurusService thesaurus;

  protected ThesaurusManager() {

  }

  /**
   * Retourne la liste des synonymes d'un mot donné (ce mot peut être un terme ou un synonyme) pour
   * un utilisateur donné
   * @param mot le mot
   * @param idUser l'identifiant de l'utilisateur
   * @return une collection de synonymes
   * @throws ThesaurusException si une erreur survient
   */
  public Collection<String> getSynonyms(String mot, String idUser)
      throws ThesaurusException {

    Collection<String> finalList = new ArrayList<>();
    // recupere le jargon de l'utilisateur
    Jargon jargon = getJargon(idUser);

    if (jargon != null) {
      long idVoca = jargon.getIdVoca();

      // recupere la liste des mots synonymes du mot si c'est un terme
      Collection<String> synonyms = getSynonymsTerm(idVoca, mot);
      List<String> interList = new ArrayList<>(synonyms);

      // recupere la liste des mots synonymes du mot si c'est un synonyme
      Collection<String> otherSynonyms = getSynonyms(idVoca, mot);

      interList.addAll(otherSynonyms);

      // parsing de la liste pour enlever les doublons
      for (String synonyme : interList) {
        if ((!isExist(synonyme, finalList)) &&
            (!mot.equalsIgnoreCase(synonyme))) {
          finalList.add(synonyme);
        }
      }
    }

    return finalList;
  }

  /**
   * Retourne la liste des mots synonymes d'un terme dans un vocabulaire donné
   * @param idVoca l'identifiant du vocabulaire à utiliser
   * @param term le terme
   * @return une collection de synonymes
   * @throws ThesaurusException si une erreur survient
   */
  private Collection<String> getSynonymsTerm(long idVoca, String term)
      throws ThesaurusException {

    Collection<String> theList = new ArrayList<>();
    try {
      // recupere les termes correspondant à un nom de terme
      PdcManager pdc = PdcManager.get();
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
      throw new ThesaurusException(e);
    }

    return theList;
  }

  /**
   * Retourne la liste des autres synonymes d'un mot étant un synonyme d'un terme dans un
   * vocabulaire donné
   * @param idVoca l'identifiant du vocabulaire
   * @param synonym le synonyme
   * @return collection des autres synonymes
   * @throws ThesaurusException si une erreur survient
   */
  private Collection<String> getSynonyms(long idVoca, String synonym)
      throws ThesaurusException {

    Collection<String> theList = new ArrayList<>();
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
          PdcManager pdc = PdcManager.get();
          Value value = pdc.getAxisValue(Long.toString(idTerm), Long.toString(idTree));
          String nameTerm = value.getName();
          theList.add(nameTerm);
        }
      }

    } catch (PdcException e) {
      throw new ThesaurusException(e);
    }

    return theList;

  }

  private boolean isExist(String nom, Collection<String> tab) {
    for (String mot : tab) {
      if (nom.equalsIgnoreCase(mot)) {
        return true;
      }
    }
    return false;
  }

  public Collection<String> getSynonyms(long idTree, long idTerm, String idUser)
      throws ThesaurusException {

    Collection<String> theList = new ArrayList<>();
    // recupere le jargon de l'utilisateur
    Jargon jargon = getJargon(idUser);
    if (jargon != null) {
      long idVoca = jargon.getIdVoca();

      theList.addAll(getSynonyms(idTree, idTerm, idVoca));
    }
    return theList;
  }

  public Collection<String> getSynonyms(long idTree, long idTerm, long idVoca)
      throws ThesaurusException {

    List<String> theList = new ArrayList<>();

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
   * @param axisId identifiant d'un axe du PdC
   * @param idUser identifiant de l'utilisateur
   * @return collection des synonymes sur l'axe
   * @throws ThesaurusException si une erreur survient
   */
  public Collection<String> getSynonymsAxis(String axisId, String idUser)
      throws ThesaurusException {

    Collection<String> theList = new ArrayList<>();
    try {
      // recupere le jargon de l'utilisateur
      Jargon jargon = getJargon(idUser);
      if (jargon != null) {
        long idVoca = jargon.getIdVoca();

        // recupere le treeId et le idTerm de l'axe
        PdcManager pdc = PdcManager.get();
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
      throw new ThesaurusException(e);
    }

    return theList;
  }

  /**
   * Retourne le jargon utilisé par l'utilisateur retourne un Jargon ou null s'il n'a pas de jargon
   * @param idUser l'identifiant de l'utilisateur
   * @return Jargon
   * @throws ThesaurusException si une erreur survient
   */
  public Jargon getJargon(String idUser) throws ThesaurusException {
    return thesaurus.getJargon(idUser);
  }

  /**
   * Supprime les synonymes de tous les termes associés à l'axe passé en paramètre
   * @param idTree l'identifiant d'un axe du PdC
   * @throws ThesaurusException si une erreur survient
   */
  public void deleteSynonymsAxis(Connection con, long idTree)
      throws ThesaurusException {
      thesaurus.deleteSynonymsAxis(con, idTree);
  }

  /**
   * Supprime les synonymes de tous les termes passés en paramètre
   * @param idTree identifiant de l'axe
   * @param idTerms liste de termes
   * @throws ThesaurusException si une erreur survient
   */
  public void deleteSynonymsTerms(Connection con, long idTree, List<String> idTerms)
      throws ThesaurusException {
      thesaurus.deleteSynonymsTerms(con, idTree, idTerms);
  }

}
