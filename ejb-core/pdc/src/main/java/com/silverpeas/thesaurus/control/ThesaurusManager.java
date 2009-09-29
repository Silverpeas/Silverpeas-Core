package com.silverpeas.thesaurus.control;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.thesaurus.model.Jargon;
import com.silverpeas.thesaurus.model.Synonym;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Class ThesaurusManager
 * 
 * Classe servant d'interface aux autres composants ayant besoin du thesaurus
 * (module pdc et searchEngine)
 * 
 */
public class ThesaurusManager {

  private ThesaurusBm thesaurus = ThesaurusBm.getInstance();

  public ThesaurusManager() {

  }

  /**
   * Retourne la liste des synonymes d'un mot donné (ce mot peut-etre un terme
   * ou un synonyme) pour un utilisateur donné retourne une Collection de String
   * 
   * 
   * @param mot
   * @param idUser
   * 
   * @return Collection
   * 
   * @throws ThesaurusException
   * 
   * @see getJargon, getSynonymsTerm, getSynonyms, isExist
   */
  public Collection getSynonyms(String mot, String idUser)
      throws ThesaurusException {

    Collection finalList = new ArrayList();
    try {
      // recupere le jargon de l'utilisateur
      Jargon jargon = getJargon(idUser);

      if (jargon != null) {
        long idVoca = jargon.getIdVoca();

        // recupere la liste des mots synonymes du mot si c'est un terme
        Collection synonyms = getSynonymsTerm(idVoca, mot);
        ArrayList interList = new ArrayList(synonyms);

        // recupere la liste des mots synonymes du mot si c'est un synonyme
        Collection otherSynonyms = getSynonyms(idVoca, mot);

        Iterator i = otherSynonyms.iterator();
        while (i.hasNext()) {
          String synonyme = (String) i.next();
          interList.add(synonyme);
        }

        // parsing de la liste pour enlever les doublons
        i = interList.iterator();
        while (i.hasNext()) {
          String synonyme = (String) i.next();
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
   * Retourne la liste des mots synonymes d'un terme dans un vocabulaire donné
   * retourne une Collection de String
   * 
   * 
   * @param idVoca
   * @param term
   * 
   * @return Collection
   * 
   * @throws ThesaurusException
   * 
   * @see com.stratelia.silverpeas.pdc.control.PdcBm.getValues,
   *      com.silverpeas.thesaurus.control.ThesaurusBm.getSynonyms
   */
  private Collection getSynonymsTerm(long idVoca, String term)
      throws ThesaurusException {

    Collection theList = new ArrayList();
    try {
      // recupere les termes correspondant à un nom de terme
      PdcBm pdc = new PdcBmImpl();
      List valueList = pdc.getAxisValuesByName(term);

      Iterator i = valueList.iterator();
      while (i.hasNext()) {
        Value value = (Value) i.next();
        long idTree = new Long(value.getTreeId()).longValue();
        long idTerm = new Long(value.getPK().getId()).longValue();

        // recupere la liste des mots synonymes du terme
        Collection synonymes = thesaurus.getSynonyms(idVoca, idTree, idTerm);

        Iterator j = synonymes.iterator();
        while (j.hasNext()) {
          Synonym synonyme = (Synonym) j.next();
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
   * Retourne la liste des autres synonymes d'un mot étant un synonyme d'un
   * terme dans un vocabulaire donné retourne une Collection de String
   * 
   * 
   * @param idVoca
   * @param synonym
   * 
   * @return Collection
   * 
   * @throws ThesaurusException
   * 
   * @see com.silverpeas.thesaurus.control.ThesaurusBm.getSynonyms,
   *      com.stratelia.silverpeas.pdc.control.PdcBm.getValue
   */
  private Collection getSynonyms(long idVoca, String synonym)
      throws ThesaurusException {

    Collection theList = new ArrayList();
    try {
      // recupere les autres synonymes du synonyme dans le vocabulaire
      Collection synonymsList = thesaurus.getSynonyms(idVoca, synonym);

      Iterator i = synonymsList.iterator();
      while (i.hasNext()) {
        Synonym aSynonym = (Synonym) i.next();
        long idTree = aSynonym.getIdTree();
        long idTerm = aSynonym.getIdTerm();

        // recupere la liste des synonymes du terme dans le vocabulaire
        Collection synonyms = thesaurus.getSynonyms(idVoca, idTree, idTerm);

        Iterator j = synonyms.iterator();
        while (j.hasNext()) {
          Synonym synonymTerm = (Synonym) j.next();
          String name = synonymTerm.getName();
          theList.add(name);

          // recupere le nom du terme correspondant
          PdcBm pdc = new PdcBmImpl();
          Value value = pdc.getAxisValue(new Long(idTerm).toString(), new Long(
              idTree).toString());
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
   * 
   * 
   * @param nom
   * @param tab
   * 
   * @return boolean
   * 
   * @throws
   * 
   * @see
   */
  private boolean isExist(String nom, Collection tab) {
    Iterator i = tab.iterator();
    while (i.hasNext()) {
      String mot = (String) i.next();
      if (nom.toLowerCase().equals(mot.toLowerCase()))
        return true;
    }
    return false;
  }

  /**
   * Retourne la liste des synonymes d'un terme pour un utilisateur donné
   * retourne une Collection de String
   * 
   * 
   * @param idTree
   * @param idTerm
   * @param idUser
   * 
   * @return Collection
   * 
   * @throws ThesaurusException
   * 
   * @see getJargon, com.silverpeas.thesaurus.control.ThesaurusBm.getSynonyms
   */
  public Collection getSynonyms(long idTree, long idTerm, String idUser)
      throws ThesaurusException {

    Collection theList = new ArrayList();
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

  public Collection getSynonyms(long idTree, long idTerm, long idVoca)
      throws ThesaurusException {

    List theList = new ArrayList();

    // recupere la liste des synonymes du terme dans le vocabulaire
    Collection synonyms = thesaurus.getSynonyms(idVoca, idTree, idTerm);

    Iterator i = synonyms.iterator();
    while (i.hasNext()) {
      Synonym synonym = (Synonym) i.next();
      String name = synonym.getName();
      theList.add(name);
    }
    return theList;
  }

  public Collection getSynonymsByTree(long idTree, long idVoca)
      throws ThesaurusException {
    return thesaurus.getSynonymsByTree(idVoca, idTree);
  }

  /**
   * Retourne la liste des synonymes d'un terme pour un utilisateur donné, à
   * partir de son axe retourne une Collection de String
   * 
   * 
   * @param axisId
   * @param idUser
   * 
   * @return Collection
   * 
   * @throws ThesaurusException
   * 
   * @see getJargon, com.stratelia.silverpeas.pdc.control.PdcBm.getRoot,
   *      com.silverpeas.thesaurus.control.ThesaurusBm.getSynonyms
   */
  public Collection getSynonymsAxis(String axisId, String idUser)
      throws ThesaurusException {

    Collection theList = new ArrayList();
    try {
      // recupere le jargon de l'utilisateur
      Jargon jargon = getJargon(idUser);
      if (jargon != null) {
        long idVoca = jargon.getIdVoca();

        // recupere le treeId et le idTerm de l'axe
        PdcBm pdc = new PdcBmImpl();
        Value value = pdc.getRoot(axisId);

        long idTree = new Long(value.getTreeId()).longValue();
        long idTerm = new Long(value.getPK().getId()).longValue();

        // recupere la liste des synonymes du terme dans le vocabulaire
        Collection synonyms = thesaurus.getSynonyms(idVoca, idTree, idTerm);

        Iterator i = synonyms.iterator();
        while (i.hasNext()) {
          Synonym synonym = (Synonym) i.next();
          String name = synonym.getName();
          theList.add(name);

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
   * Retourne le jargon utilisé par l'utilisateur retourne un Jargon ou null
   * s'il n'a pas de jargon
   * 
   * 
   * @param idUser
   * 
   * @return Jargon
   * 
   * @throws ThesaurusException
   * 
   * @see com.silverpeas.thesaurus.control.ThesaurusBm.getJargon
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
   * Supprime les synonymes de tous les termes associés à l'axe passé en
   * paramètre
   * 
   * 
   * @param idTree
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see com.silverpeas.thesaurus.control.ThesaurusBm.deleteSynonymsAxis
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
   * 
   * 
   * @param idTree
   * @param idTerms
   *          : List de String
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see com.silverpeas.thesaurus.control.ThesaurusBm.deleteSynonymsTerms
   */
  public void deleteSynonymsTerms(Connection con, long idTree, List idTerms)
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
