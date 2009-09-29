/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.thesaurus.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.thesaurus.model.Jargon;
import com.silverpeas.thesaurus.model.Synonym;
import com.silverpeas.thesaurus.model.Vocabulary;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * ThesaurusBm
 * 
 * Classe servant à gérer le métier du thesuaurus
 * 
 */
public class ThesaurusBm {

  private static ThesaurusBm instance;
  private SilverpeasBeanDAO Vdao = null;
  private SilverpeasBeanDAO Sdao = null;
  private SilverpeasBeanDAO Jdao = null;

  private ThesaurusBm() {
  }

  static public ThesaurusBm getInstance() {
    if (instance == null)
      instance = new ThesaurusBm();
    return instance;
  }

  private SilverpeasBeanDAO getVdao() throws PersistenceException {
    if (Vdao == null)
      Vdao = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.thesaurus.model.Vocabulary");
    return Vdao;
  }

  private SilverpeasBeanDAO getSdao() throws PersistenceException {
    if (Sdao == null)
      Sdao = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.thesaurus.model.Synonym");
    return Sdao;
  }

  private SilverpeasBeanDAO getJdao() throws PersistenceException {
    if (Jdao == null)
      Jdao = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.thesaurus.model.Jargon");
    return Jdao;
  }

  /**
   * Retourne la liste des vocabulaires (Collection de Vocabulary)
   * 
   * 
   * @param
   * 
   * @return Collection
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public Collection getListVocabulary() throws ThesaurusException {
    Collection vocabs = new ArrayList();
    try {
      SilverpeasBeanDAO daoV = getVdao();
      IdPK pk = new IdPK();
      vocabs = daoV.findByWhereClause(pk, null);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.getListVocabulary",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_LIST_VOCABULARIES",
          "", e);
    }
    return vocabs;
  }

  /**
   * Retourne le détail du vocabulaire à partir d'un idVoca
   * 
   * 
   * @param idVoca
   * 
   * @return Vocabulary
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public Vocabulary getVocabulary(long idVoca) throws ThesaurusException {
    Vocabulary vocab = null;
    try {
      SilverpeasBeanDAO daoV = getVdao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(idVoca);
      vocab = (Vocabulary) daoV.findByPrimaryKey(pk);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.getVocabulary",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_VOCABULARY", "", e);
    }
    return vocab;
  }

  /**
   * Crée un nouveau vocabulaire et retourne l'id de celui-ci
   * 
   * 
   * @param voca
   * 
   * @return long
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public long createVocabulary(Vocabulary voca) throws ThesaurusException {
    long idV = -1;
    try {
      SilverpeasBeanDAO daoV = getVdao();
      IdPK pkV = (IdPK) daoV.add(voca);
      idV = pkV.getIdAsLong();
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.createVocabulary",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_VOCABULARY_FAILED",
          "", e);
    }
    return idV;
  }

  /**
   * Retourne vrai si le vocabulaire passé en paramètre existe déjà
   * 
   * 
   * @param name
   * 
   * @return boolean
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public boolean existVocabulary(String name) throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoV = getVdao();
      IdPK pk = new IdPK();
      Collection vocabs = (Collection) daoV.findByWhereClause(pk, " name= '"
          + encode(name) + "'");

      if (vocabs.size() > 0) { // le vocabulaire existe déjà
        return true;
      } else
        return false;

    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.existVocabulary",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_VOCABULARY",
          "name = " + name, e);
    }
  }

  /**
   * Retourne une chaine qui a encodé les ' par des ''
   * 
   * 
   * @param name
   * 
   * @return String
   * 
   * @throws
   * 
   * @see
   */
  private String encode(String name) {
    String chaine = "";

    for (int i = 0; i < name.length(); i++) {
      switch (name.charAt(i)) {
        case '\'':
          chaine += "''";
          break;
        default:
          chaine += name.charAt(i);
      }
    }
    return chaine;
  }

  /**
   * Met à jour le vocabulaire passé en paramètre
   * 
   * 
   * @param voca
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public void updateVocabulary(Vocabulary voca) throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoV = getVdao();
      daoV.update(voca);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.updateVocabulary",
          SilverpeasException.ERROR, "Thesaurus.EX_UPDATE_VOCABULARY_FAILED",
          "", e);
    }
  }

  /**
   * Supprime le vocabulaire passé en paramètre
   * 
   * 
   * @param idVoca
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see deleteVocaSynonyms, deleteVocaJargons
   */
  public void deleteVocabulary(long idVoca) throws ThesaurusException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.THESAURUS_DATASOURCE);
      con.setAutoCommit(false);

      // supprime les synonymes du vocabulaire
      deleteVocaSynonyms(con, idVoca);

      // supprime les jargons du vocabulaire
      deleteVocaJargons(con, idVoca);

      // supprime le vocabulaire
      SilverpeasBeanDAO daoV = getVdao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(idVoca);
      daoV.remove(con, pk);

      con.commit();

    } catch (Exception e) {
      try {
        con.rollback();
      } catch (Exception e1) {
        throw new ThesaurusException("ThesaurusBm.deleteVocabulary",
            SilverpeasException.ERROR, "Thesaurus.EX_DELETE_VOCABULARY_FAILED",
            "Error in rollback", e1);
      }

      throw new ThesaurusException("ThesaurusBm.deleteVocabulary",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_VOCABULARY_FAILED",
          "", e);
    } finally {
      if (con != null)
        try {
          con.close();
        } catch (SQLException e) {
          throw new ThesaurusException("ThesaurusBm.deleteVocabulary",
              SilverpeasException.ERROR,
              "Thesaurus.EX_DELETE_VOCABULARY_FAILED", "", e);
        }
    }
  }

  /**
   * Supprime tous les synonymes associés au vocabulaire passé en paramètre
   * 
   * 
   * @param idVoca
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  private void deleteVocaSynonyms(Connection con, long idVoca)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSdao();
      IdPK pk = new IdPK();
      daoS.removeWhere(con, pk, " idVoca=" + idVoca);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.deleteVocaSynonyms",
          SilverpeasException.ERROR,
          "Thesaurus.EX_DELETE_SYNONYMS_VOCA_FAILED", "", e);
    }
  }

  /**
   * Supprime tous les jargons associés au vocabulaire passé en paramètre
   * 
   * 
   * @param idVoca
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  private void deleteVocaJargons(Connection con, long idVoca)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoJ = getJdao();
      IdPK pk = new IdPK();
      daoJ.removeWhere(con, pk, " idVoca=" + idVoca);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.deleteVocaJargons",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_JARGONS_FAILED", "",
          e);
    }
  }

  /**
   * Supprime tous les jargons passés en paramètre
   * 
   * 
   * @param jargons
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see deleteJargon
   */
  public void deleteJargons(Collection jargons) throws ThesaurusException {

    try {
      Iterator i = jargons.iterator();
      while (i.hasNext()) {
        Jargon jargon = (Jargon) i.next();
        String idJargon = jargon.getPK().getId();
        // supprime jargon
        deleteJargon(idJargon);
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusBm.deleteJargons",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_JARGONS_FAILED", "",
          e);
    }
  }

  /**
   * Supprime le jargon passé en paramètre
   * 
   * 
   * @param idJargon
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  private void deleteJargon(String idJargon) throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoJ = getJdao();
      IdPK pk = new IdPK();
      pk.setId(idJargon);
      daoJ.remove(pk);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.deleteJargon",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_JARGON_FAILED", "", e);
    }
  }

  /**
   * Crée de nouveaux synonymes à un terme
   * 
   * 
   * @param synonyms
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see createSynonym
   */
  private void createSynonyms(Connection con, Collection synonyms)
      throws ThesaurusException {
    try {
      Iterator i = synonyms.iterator();
      while (i.hasNext()) {
        Synonym synonyme = (Synonym) i.next();
        createSynonym(con, synonyme);
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusBm.createSynonyms",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_SYNONYMS_FAILED", "",
          e);
    }
  }

  /**
   * Crée un nouveau synonyme à un terme
   * 
   * 
   * @param synonym
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  private void createSynonym(Connection con, Synonym synonym)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSdao();
      daoS.add(con, synonym);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.createSynonym",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_SYNONYMS_FAILED", "",
          e);
    }
  }

  /**
   * Retourne la liste des synonymes d'un vocabulaire, pour un terme
   * 
   * 
   * @param idVoca
   * @param idTree
   * @param idTerm
   * 
   * @return Collection
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public Collection getSynonyms(long idVoca, long idTree, long idTerm)
      throws ThesaurusException {
    Collection synonyms = new ArrayList();
    try {
      SilverpeasBeanDAO daoS = getSdao();
      IdPK pk = new IdPK();
      synonyms = daoS.findByWhereClause(pk, " idVoca=" + idVoca
          + " AND idTree=" + idTree + " AND idTerm=" + idTerm);
    } catch (PersistenceException e) {
      throw new ThesaurusException(
          "ThesaurusBm.getSynonyms(long idVoca, long idTree, long idTerm)",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS", "", e);
    }
    return synonyms;
  }

  /**
   * Met à jour une liste de synonymes
   * 
   * 
   * @param synonyms
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see deleteSynonyms, createSynonyms
   */
  public void updateSynonyms(Collection synonyms) throws ThesaurusException {
    Connection con = null;

    try {
      con = DBUtil.makeConnection(JNDINames.THESAURUS_DATASOURCE);
      con.setAutoCommit(false);

      Iterator i = synonyms.iterator();
      // premier élément de la liste
      if (i.hasNext()) {
        Synonym synonyme = (Synonym) i.next();
        long idVoca = synonyme.getIdVoca();
        long idTree = synonyme.getIdTree();
        long idTerm = synonyme.getIdTerm();
        // supprime les synonymes du terme
        deleteSynonyms(con, idVoca, idTree, idTerm);
      }

      // crée les synonymes du terme
      createSynonyms(con, synonyms);

      con.commit();
    } catch (Exception e) {
      try {
        con.rollback();
      } catch (Exception e1) {
        throw new ThesaurusException("ThesaurusBm.updateSynonyms",
            SilverpeasException.ERROR, "Thesaurus.EX_UPDATE_SYNONYMS_FAILED",
            "Error in rollback", e1);
      }

      throw new ThesaurusException("ThesaurusBm.updateSynonyms",
          SilverpeasException.ERROR, "Thesaurus.EX_UPDATE_SYNONYMS_FAILED", "",
          e);
    } finally {
      if (con != null)
        try {
          con.close();
        } catch (SQLException e) {
          throw new ThesaurusException("ThesaurusBm.updateSynonyms",
              SilverpeasException.ERROR, "Thesaurus.EX_UPDATE_SYNONYMS_FAILED",
              "", e);
        }
    }
  }

  /**
   * Supprime les synonymes d'un terme dans un vocabulaire
   * 
   * 
   * @param idVoca
   * @param idTree
   * @param idTerm
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public void deleteSynonyms(String idVoca, String idTree, String idTerm)
      throws ThesaurusException {
    Connection con = null;

    try {
      con = DBUtil.makeConnection(JNDINames.THESAURUS_DATASOURCE);
      con.setAutoCommit(true);
      deleteSynonyms(con, Long.parseLong(idVoca), Long.parseLong(idTree), Long
          .parseLong(idTerm));
    } catch (Exception e) {
      throw new ThesaurusException("ThesaurusBm.deleteSynonyms",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_SYNONYMS_FAILED", "",
          e);
    } finally {
      if (con != null)
        try {
          con.close();
        } catch (SQLException e) {
          throw new ThesaurusException("ThesaurusBm.updateSynonyms",
              SilverpeasException.ERROR, "Thesaurus.EX_UPDATE_SYNONYMS_FAILED",
              "", e);
        }
    }
  }

  private void deleteSynonyms(Connection con, long idVoca, long idTree,
      long idTerm) throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSdao();
      IdPK pk = new IdPK();
      daoS.removeWhere(con, pk, " idVoca=" + idVoca + " AND idTree=" + idTree
          + " AND idTerm=" + idTerm);
    } catch (PersistenceException e) {
      throw new ThesaurusException(
          "ThesaurusBm.deleteSynonyms(Connection con, long idVoca, long idTree, long idTerm)",
          SilverpeasException.ERROR,
          "Thesaurus.EX_DELETE_SYNONYMS_TERM_FAILED", "", e);
    }
  }

  /**
   * Supprime les synonymes passés en paramètre
   * 
   * 
   * @param idSynonyms
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see deleteSynonym
   */
  public void deleteSynonyms(Collection idSynonyms) throws ThesaurusException {
    try {
      Iterator i = idSynonyms.iterator();
      while (i.hasNext()) {
        Long idSynonym = (Long) i.next();

        // supprime le synonyme
        deleteSynonym(idSynonym.longValue());
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException(
          "ThesaurusBm.deleteSynonyms(Collection idSynonyms)",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_SYNONYMS_FAILED", "",
          e);
    }
  }

  /**
   * Supprime le synonyme passés en paramètre
   * 
   * 
   * @param idSynonym
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  private void deleteSynonym(long idSynonym) throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSdao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(idSynonym);
      daoS.remove(pk);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.deleteSynonym",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_SYNONYM_FAILED", "",
          e);
    }
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
   * @see
   */
  public void deleteSynonymsAxis(Connection con, long idTree)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSdao();
      IdPK pk = new IdPK();
      daoS.removeWhere(con, pk, " idTree=" + idTree);
    } catch (PersistenceException e) {
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
   * @see deleteSynonymsTerm
   */
  public void deleteSynonymsTerms(Connection con, long idTree, List idTerms)
      throws ThesaurusException {
    try {
      Iterator i = idTerms.iterator();
      while (i.hasNext()) {
        long idTerm = new Long(((String) i.next())).longValue();
        deleteSynonymsTerm(con, idTree, idTerm);
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusManager.deleteSynonymsTerms",
          SilverpeasException.ERROR,
          "Thesaurus.EX_DELETE_SYNONYMS_TERMS_FAILED", "", e);
    }
  }

  /**
   * Supprime les synonymes du terme passé en paramètre
   * 
   * 
   * @param idTree
   * @param idTerm
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  private void deleteSynonymsTerm(Connection con, long idTree, long idTerm)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSdao();
      IdPK pk = new IdPK();
      daoS.removeWhere(con, pk, " idTree=" + idTree + " AND idTerm=" + idTerm);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusManager.deleteSynonymsTerm",
          SilverpeasException.ERROR,
          "Thesaurus.EX_DELETE_SYNONYMS_TERM_FAILED", "", e);
    }
  }

  /**
   * Retourne la liste des utilisateurs utilisant le vocabulaire passé en
   * paramètre retourne une Collection de Jargon
   * 
   * 
   * @param idVoca
   * 
   * @return Collection
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public Collection getJargons(long idVoca) throws ThesaurusException {
    Collection jargons = new ArrayList();
    try {
      SilverpeasBeanDAO daoJ = getJdao();
      IdPK pk = new IdPK();
      jargons = daoJ.findByWhereClause(pk, " idVoca=" + idVoca);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.getJargons",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_JARGONS", "", e);
    }
    return jargons;
  }

  /**
   * Retourne la liste des jargons untilisés pas la liste des utilisateurs
   * passés en paramètre Retoune une Collection de Jargon
   * 
   * 
   * @param idUsers
   * @param type
   *          (0=UserDetail ou 1=Group)
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public Collection getJargons(Collection idUsers, int type)
      throws ThesaurusException {
    Collection jargons = new ArrayList();
    try {
      SilverpeasBeanDAO daoJ = getJdao();
      IdPK pk = new IdPK();

      Iterator i = idUsers.iterator();
      while (i.hasNext()) {
        String idUser = (String) i.next();
        Collection theJargons = daoJ.findByWhereClause(pk, " idUser='" + idUser
            + "'" + " AND type=" + type);
        Iterator j = theJargons.iterator();
        while (j.hasNext()) {
          Jargon jargon = (Jargon) j.next();
          jargons.add(jargon);
        }
      }
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.getJargons",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_JARGONS_USERS", "",
          e);
    }
    return jargons;
  }

  /**
   * Retourne la liste des jargons untilisés pas la liste des utilisateurs
   * passés en paramètre Retoune une Collection de Jargon
   * 
   * 
   * @param idUsers
   * @param type
   *          (0=UserDetail ou 1=Group)
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public Collection getJargons(Connection con, Collection idUsers, int type)
      throws ThesaurusException {
    Collection jargons = new ArrayList();
    try {
      SilverpeasBeanDAO daoJ = getJdao();
      IdPK pk = new IdPK();

      Iterator i = idUsers.iterator();
      while (i.hasNext()) {
        String idUser = (String) i.next();
        Collection theJargons = daoJ.findByWhereClause(con, pk, " idUser='"
            + idUser + "'" + " AND type=" + type);
        Iterator j = theJargons.iterator();
        while (j.hasNext()) {
          Jargon jargon = (Jargon) j.next();
          jargons.add(jargon);
        }
      }
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.getJargons",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_JARGONS_USERS", "",
          e);
    }
    return jargons;
  }

  /**
   * Crée une liste de jargons
   * 
   * 
   * @param jargons
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see getJargons, createJargon, updateJargon
   */
  public void createJargons(Collection jargons) throws ThesaurusException {
    try {
      Iterator i = jargons.iterator();
      while (i.hasNext()) {
        Jargon jargon = (Jargon) i.next();
        long idVoca = jargon.getIdVoca();
        String idUser = jargon.getIdUser();
        int type = jargon.getType();

        // recupere le jargon actuel utilisé par l'utilisateur
        ArrayList user = new ArrayList();
        user.add(idUser);
        ArrayList actualJargon = new ArrayList(getJargons(user, type));

        if (actualJargon.size() > 0) {// l'utilisateur avait deja un jargon
          Jargon theActualJargon = (Jargon) actualJargon.get(0);
          if (theActualJargon.getIdVoca() != idVoca) {// update de l'idVoca
            IdPK pk = new IdPK();
            pk.setIdAsLong(((IdPK) theActualJargon.getPK()).getIdAsLong());
            jargon.setPK(pk);
            updateJargon(jargon);
          }
        }

        else {// l'utilisateur n'avait pas de jargon
          createJargon(jargon);
        }
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusBm.createJargons",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_JARGONS_FAILED", "",
          e);
    }
  }

  /**
   * Crée une liste de jargons
   * 
   * 
   * @param jargons
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see getJargons, createJargon, updateJargon
   */
  public void createJargons(Connection con, Collection jargons)
      throws ThesaurusException {
    try {
      Iterator i = jargons.iterator();
      while (i.hasNext()) {
        Jargon jargon = (Jargon) i.next();
        long idVoca = jargon.getIdVoca();
        String idUser = jargon.getIdUser();
        int type = jargon.getType();

        // recupere le jargon actuel utilisé par l'utilisateur
        ArrayList user = new ArrayList();
        user.add(idUser);
        ArrayList actualJargon = new ArrayList(getJargons(con, user, type));

        if (actualJargon.size() > 0) {// l'utilisateur avait deja un jargon
          Jargon theActualJargon = (Jargon) actualJargon.get(0);
          if (theActualJargon.getIdVoca() != idVoca) {// update de l'idVoca
            IdPK pk = new IdPK();
            pk.setIdAsLong(((IdPK) theActualJargon.getPK()).getIdAsLong());
            jargon.setPK(pk);
            updateJargon(jargon);
          }
        }

        else {// l'utilisateur n'avait pas de jargon
          createJargon(con, jargon);
        }
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusBm.createJargons",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_JARGONS_FAILED", "",
          e);
    }
  }

  /**
   * Crée la liste des jargons d'un vocabulaire (suppression tous les jargons du
   * voca puis création)
   * 
   * 
   * @param jargons
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see deleteVocaJargons, createJargon
   */
  public void createVocaJargons(Collection jargons, long idVoca)
      throws ThesaurusException {
    Connection con = null;

    try {
      con = DBUtil.makeConnection(JNDINames.THESAURUS_DATASOURCE);
      con.setAutoCommit(false);

      deleteVocaJargons(con, idVoca);
      createJargons(con, jargons);

      con.commit();
    } catch (Exception e) {
      try {
        con.rollback();
      } catch (Exception e1) {
        throw new ThesaurusException("ThesaurusBm.createJargons",
            SilverpeasException.ERROR, "Thesaurus.EX_CREATE_JARGONS_FAILED",
            "Error in rollback", e1);
      }

      throw new ThesaurusException("ThesaurusBm.createJargons",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_JARGONS_FAILED", "",
          e);
    } finally {
      if (con != null)
        try {
          con.close();
        } catch (SQLException e) {
          throw new ThesaurusException("ThesaurusBm.createJargons",
              SilverpeasException.ERROR, "Thesaurus.EX_CREATE_JARGONS_FAILED",
              "", e);
        }
    }
  }

  /**
   * Crée un nouveau jargon
   * 
   * 
   * @param jargon
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  private void createJargon(Jargon jargon) throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoJ = getJdao();
      daoJ.add(jargon);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.createJargon",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_JARGON_FAILED", "", e);
    }
  }

  /**
   * Met à jour le jargon du user
   * 
   * 
   * @param jargon
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  private void updateJargon(Jargon jargon) throws ThesaurusException {
    try {
      long newIdVoca = jargon.getIdVoca();

      SilverpeasBeanDAO daoJ = getJdao();
      IdPK pk = new IdPK();
      pk.setId(jargon.getPK().getId());
      Jargon jargonToModify = (Jargon) daoJ.findByPrimaryKey(pk);
      jargonToModify.setIdVoca(newIdVoca);
      daoJ.update(jargonToModify);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.updateJargon",
          SilverpeasException.ERROR, "Thesaurus.EX_UPDATE_JARGON_FAILED", "", e);
    }
  }

  /**
   * Crée un nouveau jargon
   * 
   * 
   * @param jargon
   * 
   * @return
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  private void createJargon(Connection con, Jargon jargon)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoJ = getJdao();
      daoJ.add(con, jargon);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.createJargon",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_JARGON_FAILED", "", e);
    }
  }

  /**
   * Retourne le jargon de l'utilisateur (type 0) passé en paramètre
   * 
   * 
   * @param idUser
   * 
   * @return Jargon
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public Jargon getJargon(String idUser) throws ThesaurusException {
    Jargon jargon = null;
    try {
      SilverpeasBeanDAO daoJ = getJdao();
      IdPK pk = new IdPK();
      Collection jargons = daoJ.findByWhereClause(pk, " idUser='" + idUser
          + "'" + " AND type=0");
      Iterator i = jargons.iterator();
      if (i.hasNext()) {
        jargon = (Jargon) i.next();
      }

    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusBm.getJargon",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_JARGON", "", e);
    }
    return jargon;
  }

  /**
   * Retourne la liste des autres synonymes d'un synonyme dans un vocabulaire
   * retourne une Collection de Synonym
   * 
   * @param idVoca
   * @param name
   * 
   * @return Collection
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public Collection getSynonyms(long idVoca, String name)
      throws ThesaurusException {
    Collection synonyms = new ArrayList();
    try {
      SilverpeasBeanDAO daoS = getSdao();
      IdPK pk = new IdPK();
      synonyms = daoS.findByWhereClause(pk, " idVoca=" + idVoca + " AND name='"
          + encode(name) + "'");
    } catch (PersistenceException e) {
      throw new ThesaurusException(
          "ThesaurusBm.getSynonyms(long idVoca, String name)",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS_NAME", "",
          e);
    }
    return synonyms;
  }

  /**
   * Retourne la liste des autres synonymes d'un synonyme dans un vocabulaire
   * retourne une Collection de Synonym
   * 
   * @param idVoca
   * @param name
   * 
   * @return Collection
   * 
   * @throws ThesaurusException
   * 
   * @see
   */
  public Collection getSynonymsByTree(long idVoca, long idTree)
      throws ThesaurusException {
    Collection synonyms = new ArrayList();
    try {
      SilverpeasBeanDAO daoS = getSdao();
      IdPK pk = new IdPK();
      synonyms = daoS.findByWhereClause(pk, " idVoca=" + idVoca
          + " AND idTree=" + idTree);
    } catch (PersistenceException e) {
      throw new ThesaurusException(
          "ThesaurusBm.getSynonymsByTree(long idVoca, long idTree)",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS_NAME",
          "idVoca = " + idVoca + ", idTree = " + idTree, e);
    }
    return synonyms;
  }

}