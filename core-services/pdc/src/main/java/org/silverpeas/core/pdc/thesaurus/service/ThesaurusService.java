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

import org.silverpeas.core.pdc.thesaurus.model.ThesaurusException;
import org.silverpeas.core.pdc.thesaurus.model.Jargon;
import org.silverpeas.core.pdc.thesaurus.model.Synonym;
import org.silverpeas.core.pdc.thesaurus.model.Vocabulary;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.exception.SilverpeasException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Service dedicated to work on the thesaurus.
 */
public class ThesaurusService {

  private SilverpeasBeanDAO<Vocabulary> vocabularyDao = null;
  private SilverpeasBeanDAO<Synonym> synonymDao = null;
  private SilverpeasBeanDAO<Jargon> jargonDao = null;

  private ThesaurusService() {
  }

  public static ThesaurusService getInstance() {
    return ServiceProvider.getService(ThesaurusService.class);
  }

  private SilverpeasBeanDAO<Vocabulary> getVocabularyDao() throws PersistenceException {
    if (vocabularyDao == null) {
      vocabularyDao = SilverpeasBeanDAOFactory.<Vocabulary> getDAO(
          "org.silverpeas.core.pdc.thesaurus.model.Vocabulary");
    }
    return vocabularyDao;
  }

  private SilverpeasBeanDAO<Synonym> getSynonymDao() throws PersistenceException {
    if (synonymDao == null) {
      synonymDao = SilverpeasBeanDAOFactory.<Synonym> getDAO(
          "org.silverpeas.core.pdc.thesaurus.model.Synonym");
    }
    return synonymDao;
  }

  private SilverpeasBeanDAO<Jargon> getJargonDao() throws PersistenceException {
    if (jargonDao == null) {
      jargonDao = SilverpeasBeanDAOFactory.<Jargon> getDAO("org.silverpeas.core.pdc.thesaurus.model.Jargon");
    }
    return jargonDao;
  }

  /**
   * Retourne la liste des vocabulaires (Collection de Vocabulary)
   * @return Collection
   * @throws ThesaurusException
   * @see
   */
  public Collection<Vocabulary> getListVocabulary() throws ThesaurusException {
    try {
      IdPK pk = new IdPK();
      return getVocabularyDao().findByWhereClause(pk, null);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.getListVocabulary",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_LIST_VOCABULARIES", "", e);
    }
  }

  /**
   * Retourne le détail du vocabulaire à partir d'un idVoca
   * @param idVoca
   * @return Vocabulary
   * @throws ThesaurusException
   * @see
   */
  public Vocabulary getVocabulary(long idVoca) throws ThesaurusException {
    Vocabulary vocab = null;
    try {
      IdPK pk = new IdPK();
      pk.setIdAsLong(idVoca);
      vocab = getVocabularyDao().findByPrimaryKey(pk);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.getVocabulary",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_VOCABULARY", "", e);
    }
    return vocab;
  }

  /**
   * Crée un nouveau vocabulaire et retourne l'id de celui-ci
   * @param voca
   * @return long
   * @throws ThesaurusException
   * @see
   */
  public long createVocabulary(Vocabulary voca) throws ThesaurusException {
    try {
      IdPK pkV = (IdPK) getVocabularyDao().add(voca);
      return pkV.getIdAsLong();
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.createVocabulary", SilverpeasException.ERROR,
          "Thesaurus.EX_CREATE_VOCABULARY_FAILED", "", e);
    }
  }

  /**
   * Retourne vrai si le vocabulaire passé en paramètre existe déjà
   * @param name
   * @return boolean
   * @throws ThesaurusException
   * @see
   */
  @SuppressWarnings("unchecked")
  public boolean existVocabulary(String name) throws ThesaurusException {
    try {
      IdPK pk = new IdPK();
      Collection<Vocabulary> vocabs = getVocabularyDao().findByWhereClause(pk,
          " name= '" + encode(name) + "'");
      return !vocabs.isEmpty();
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.existVocabulary",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_VOCABULARY",
          "name = " + name, e);
    }
  }

  /**
   * Retourne une chaine qui a encodé les ' par des ''
   * @param name
   * @return String
   * @throws
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
   * @param voca
   * @return
   * @throws ThesaurusException
   * @see
   */
  public void updateVocabulary(Vocabulary voca) throws ThesaurusException {
    try {
      getVocabularyDao().update(voca);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.updateVocabulary",
          SilverpeasException.ERROR, "Thesaurus.EX_UPDATE_VOCABULARY_FAILED",
          "", e);
    }
  }

  /**
   * Supprime le vocabulaire passé en paramètre
   * @param idVoca
   * @return
   * @throws ThesaurusException
   */
  public void deleteVocabulary(long idVoca) throws ThesaurusException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      con.setAutoCommit(false);

      // supprime les synonymes du vocabulaire
      deleteVocaSynonyms(con, idVoca);

      // supprime les jargons du vocabulaire
      deleteVocaJargons(con, idVoca);

      // supprime le vocabulaire
      SilverpeasBeanDAO<Vocabulary> daoV = getVocabularyDao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(idVoca);
      daoV.remove(con, pk);

      con.commit();

    } catch (Exception e) {
      try {
        con.rollback();
      } catch (Exception e1) {
        throw new ThesaurusException("ThesaurusService.deleteVocabulary",
            SilverpeasException.ERROR, "Thesaurus.EX_DELETE_VOCABULARY_FAILED",
            "Error in rollback", e1);
      }

      throw new ThesaurusException("ThesaurusService.deleteVocabulary",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_VOCABULARY_FAILED", "", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Supprime tous les synonymes associés au vocabulaire passé en paramètre
   * @param idVoca
   * @return
   * @throws ThesaurusException
   * @see
   */
  private void deleteVocaSynonyms(Connection con, long idVoca)
      throws ThesaurusException {
    try {
      IdPK pk = new IdPK();
      getSynonymDao().removeWhere(con, pk, " idVoca=" + idVoca);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.deleteVocaSynonyms",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_SYNONYMS_VOCA_FAILED", "", e);
    }
  }

  /**
   * Supprime tous les jargons associés au vocabulaire passé en paramètre
   * @param idVoca
   * @return
   * @throws ThesaurusException
   * @see
   */
  private void deleteVocaJargons(Connection con, long idVoca)
      throws ThesaurusException {
    try {
      IdPK pk = new IdPK();
      getJargonDao().removeWhere(con, pk, " idVoca=" + idVoca);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.deleteVocaJargons",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_JARGONS_FAILED", "",
          e);
    }
  }

  /**
   * Supprime tous les jargons passés en paramètre
   * @param jargons
   * @return
   * @throws ThesaurusException
   */
  public void deleteJargons(Collection<Jargon> jargons) throws ThesaurusException {
    try {
      for (Jargon jargon : jargons) {
        // supprime jargon
        deleteJargon(jargon.getPK().getId());
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusService.deleteJargons",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_JARGONS_FAILED", "", e);
    }
  }

  /**
   * Supprime le jargon passé en paramètre
   * @param idJargon
   * @return
   * @throws ThesaurusException
   * @see
   */
  private void deleteJargon(String idJargon) throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoJ = getJargonDao();
      IdPK pk = new IdPK();
      pk.setId(idJargon);
      daoJ.remove(pk);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.deleteJargon",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_JARGON_FAILED", "", e);
    }
  }

  /**
   * Crée de nouveaux synonymes à un terme
   * @param synonyms
   * @return
   * @throws ThesaurusException
   */
  private void createSynonyms(Connection con, Collection<Synonym> synonyms)
      throws ThesaurusException {
    try {
      for (Synonym synonyme : synonyms) {
        createSynonym(con, synonyme);
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusService.createSynonyms",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_SYNONYMS_FAILED", "",
          e);
    }
  }

  /**
   * Crée un nouveau synonyme à un terme
   * @param synonym
   * @return
   * @throws ThesaurusException
   * @see
   */
  private void createSynonym(Connection con, Synonym synonym)
      throws ThesaurusException {
    try {
      getSynonymDao().add(con, synonym);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.createSynonym",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_SYNONYMS_FAILED", "",
          e);
    }
  }

  /**
   * Retourne la liste des synonymes d'un vocabulaire, pour un terme
   * @param idVoca
   * @param idTree
   * @param idTerm
   * @return Collection
   * @throws ThesaurusException
   * @see
   */
  @SuppressWarnings("unchecked")
  public Collection<Synonym> getSynonyms(long idVoca, long idTree, long idTerm)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSynonymDao();
      IdPK pk = new IdPK();
      return daoS.findByWhereClause(pk, " idVoca=" + idVoca +
          " AND idTree=" + idTree + " AND idTerm=" + idTerm);
    } catch (PersistenceException e) {
      throw new ThesaurusException(
          "ThesaurusService.getSynonyms(long idVoca, long idTree, long idTerm)",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS", "", e);
    }
  }

  /**
   * Met à jour une liste de synonymes
   * @param synonyms
   * @return
   * @throws ThesaurusException
   */
  public void updateSynonyms(Collection<Synonym> synonyms) throws ThesaurusException {
    Connection con = null;

    try {
      con = DBUtil.openConnection();
      con.setAutoCommit(false);

      Iterator<Synonym> i = synonyms.iterator();
      // premier élément de la liste
      if (i.hasNext()) {
        Synonym synonyme = i.next();
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
        throw new ThesaurusException("ThesaurusService.updateSynonyms", SilverpeasException.ERROR,
            "Thesaurus.EX_UPDATE_SYNONYMS_FAILED", "Error in rollback", e1);
      }
      throw new ThesaurusException("ThesaurusService.updateSynonyms",
          SilverpeasException.ERROR, "Thesaurus.EX_UPDATE_SYNONYMS_FAILED", "", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Supprime les synonymes d'un terme dans un vocabulaire
   * @param idVoca
   * @param idTree
   * @param idTerm
   * @return
   * @throws ThesaurusException
   * @see
   */
  public void deleteSynonyms(String idVoca, String idTree, String idTerm) throws ThesaurusException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      con.setAutoCommit(true);
      deleteSynonyms(con, Long.parseLong(idVoca), Long.parseLong(idTree), Long.parseLong(idTerm));
    } catch (Exception e) {
      throw new ThesaurusException("ThesaurusService.deleteSynonyms",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_SYNONYMS_FAILED", "", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private void deleteSynonyms(Connection con, long idVoca, long idTree, long idTerm) throws
      ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSynonymDao();
      IdPK pk = new IdPK();
      daoS.removeWhere(con, pk,
          " idVoca=" + idVoca + " AND idTree=" + idTree + " AND idTerm=" + idTerm);
    } catch (PersistenceException e) {
      throw new ThesaurusException(
          "ThesaurusService.deleteSynonyms(Connection con, long idVoca, long idTree, long idTerm)",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_SYNONYMS_TERM_FAILED", "", e);
    }
  }

  /**
   * Supprime les synonymes passés en paramètre
   * @param idSynonyms
   * @return
   * @throws ThesaurusException
   */
  public void deleteSynonyms(Collection<Long> idSynonyms) throws ThesaurusException {
    try {
      for (Long idSynonym : idSynonyms) {
        deleteSynonym(idSynonym);
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusService.deleteSynonyms(Collection idSynonyms)",
          SilverpeasException.ERROR, "Thesaurus.EX_DELETE_SYNONYMS_FAILED", "", e);
    }
  }

  /**
   * Supprime le synonyme passés en paramètre
   * @param idSynonym
   * @return
   * @throws ThesaurusException
   * @see
   */
  private void deleteSynonym(long idSynonym) throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSynonymDao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(idSynonym);
      daoS.remove(pk);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.deleteSynonym", SilverpeasException.ERROR,
          "Thesaurus.EX_DELETE_SYNONYM_FAILED", "", e);
    }
  }

  /**
   * Supprime les synonymes de tous les termes associés à l'axe passé en paramètre
   * @param idTree
   * @return
   * @throws ThesaurusException
   * @see
   */
  public void deleteSynonymsAxis(Connection con, long idTree)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSynonymDao();
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
   * @param idTree
   * @param idTerms : List de String
   * @return
   * @throws ThesaurusException
   */
  public void deleteSynonymsTerms(Connection con, long idTree, List<String> idTerms)
      throws ThesaurusException {
    try {
      for (String id : idTerms) {
        long idTerm = Long.parseLong(id);
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
   * @param idTree
   * @param idTerm
   * @return
   * @throws ThesaurusException
   * @see
   */
  private void deleteSynonymsTerm(Connection con, long idTree, long idTerm)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSynonymDao();
      IdPK pk = new IdPK();
      daoS.removeWhere(con, pk, " idTree=" + idTree + " AND idTerm=" + idTerm);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusManager.deleteSynonymsTerm",
          SilverpeasException.ERROR,
          "Thesaurus.EX_DELETE_SYNONYMS_TERM_FAILED", "", e);
    }
  }

  /**
   * Retourne la liste des utilisateurs utilisant le vocabulaire passé en paramètre retourne une
   * Collection de Jargon
   * @param idVoca
   * @return Collection
   * @throws ThesaurusException
   * @see
   */
  public Collection<Jargon> getJargons(long idVoca) throws ThesaurusException {
    try {
      IdPK pk = new IdPK();
      return getJargonDao().findByWhereClause(pk, " idVoca=" + idVoca);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.getJargons",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_JARGONS", "", e);
    }
  }

  /**
   * Retourne la liste des jargons untilisés pas la liste des utilisateurs passés en paramètre
   * Retoune une Collection de Jargon
   * @param idUsers
   * @param type (0=UserDetail ou 1=Group)
   * @return
   * @throws ThesaurusException
   * @see
   */
  public Collection<Jargon> getJargons(Collection<String> idUsers, int type)
      throws ThesaurusException {
    Collection<Jargon> jargons = new ArrayList<Jargon>();
    try {
      SilverpeasBeanDAO daoJ = getJargonDao();
      IdPK pk = new IdPK();

      for (String idUser : idUsers) {
        Collection<Jargon> theJargons = daoJ.findByWhereClause(pk, " idUser='" + idUser +
            "'" + " AND type=" + type);
        for (Jargon jargon : theJargons) {
          jargons.add(jargon);
        }
      }
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.getJargons", SilverpeasException.ERROR,
          "Thesaurus.EX_CANT_GET_JARGONS_USERS", "", e);
    }
    return jargons;
  }

  /**
   * Retourne la liste des jargons untilisés pas la liste des utilisateurs passés en paramètre
   * Retoune une Collection de Jargon
   * @param idUsers
   * @param type (0=UserDetail ou 1=Group)
   * @return
   * @throws ThesaurusException
   * @see
   */
  public Collection<Jargon> getJargons(Connection con, Collection<String> idUsers, int type)
      throws ThesaurusException {
    Collection<Jargon> jargons = new ArrayList<Jargon>();
    try {
      SilverpeasBeanDAO daoJ = getJargonDao();
      IdPK pk = new IdPK();

      for (String idUser : idUsers) {
        Collection<Jargon> theJargons = daoJ.findByWhereClause(con, pk, " idUser='" +
            idUser + "'" + " AND type=" + type);
        for (Jargon jargon : theJargons) {
          jargons.add(jargon);
        }
      }
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.getJargons", SilverpeasException.ERROR,
          "Thesaurus.EX_CANT_GET_JARGONS_USERS", "", e);
    }
    return jargons;
  }

  /**
   * Crée une liste de jargons
   * @param jargons
   * @return
   * @throws ThesaurusException
   */
  public void createJargons(Collection<Jargon> jargons) throws ThesaurusException {
    try {
      for (Jargon jargon : jargons) {
        long idVoca = jargon.getIdVoca();
        String idUser = jargon.getIdUser();
        int type = jargon.getType();

        // recupere le jargon actuel utilisé par l'utilisateur
        List<String> user = new ArrayList<String>();
        user.add(idUser);
        List<Jargon> actualJargon = new ArrayList<Jargon>(getJargons(user, type));

        if (actualJargon.size() > 0) {// l'utilisateur avait deja un jargon
          Jargon theActualJargon = actualJargon.get(0);
          if (theActualJargon.getIdVoca() != idVoca) {// update de l'idVoca
            IdPK pk = new IdPK();
            pk.setIdAsLong(((IdPK) theActualJargon.getPK()).getIdAsLong());
            jargon.setPK(pk);
            updateJargon(jargon);
          }
        } else {// l'utilisateur n'avait pas de jargon
          createJargon(jargon);
        }
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusService.createJargons",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_JARGONS_FAILED", "", e);
    }
  }

  /**
   * Crée une liste de jargons
   * @param jargons
   * @return
   * @throws ThesaurusException
   */
  public void createJargons(Connection con, Collection<Jargon> jargons) throws ThesaurusException {
    try {
      for (Jargon jargon : jargons) {
        long idVoca = jargon.getIdVoca();
        String idUser = jargon.getIdUser();
        int type = jargon.getType();

        // recupere le jargon actuel utilisé par l'utilisateur
        List<String> user = new ArrayList<String>();
        user.add(idUser);
        List<Jargon> actualJargon = new ArrayList<Jargon>(getJargons(con, user, type));

        if (actualJargon.size() > 0) {// l'utilisateur avait deja un jargon
          Jargon theActualJargon = actualJargon.get(0);
          if (theActualJargon.getIdVoca() != idVoca) {// update de l'idVoca
            IdPK pk = new IdPK();
            pk.setIdAsLong(((IdPK) theActualJargon.getPK()).getIdAsLong());
            jargon.setPK(pk);
            updateJargon(jargon);
          }
        } else {// l'utilisateur n'avait pas de jargon
          createJargon(con, jargon);
        }
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusService.createJargons", SilverpeasException.ERROR,
          "Thesaurus.EX_CREATE_JARGONS_FAILED", "", e);
    }
  }

  /**
   * Crée la liste des jargons d'un vocabulaire (suppression tous les jargons du voca puis création)
   * @param jargons
   * @return
   * @throws ThesaurusException
   */
  public void createVocaJargons(Collection<Jargon> jargons, long idVoca) throws ThesaurusException {
    Connection con = null;

    try {
      con = DBUtil.openConnection();
      con.setAutoCommit(false);

      deleteVocaJargons(con, idVoca);
      createJargons(con, jargons);

      con.commit();
    } catch (Exception e) {
      try {
        con.rollback();
      } catch (Exception e1) {
        throw new ThesaurusException("ThesaurusService.createJargons", SilverpeasException.ERROR,
            "Thesaurus.EX_CREATE_JARGONS_FAILED", "Error in rollback", e1);
      }
      throw new ThesaurusException("ThesaurusService.createJargons", SilverpeasException.ERROR,
          "Thesaurus.EX_CREATE_JARGONS_FAILED", "", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Crée un nouveau jargon
   * @param jargon
   * @return
   * @throws ThesaurusException
   * @see
   */
  private void createJargon(Jargon jargon) throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      daoJ.add(jargon);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.createJargon",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_JARGON_FAILED", "", e);
    }
  }

  /**
   * Met à jour le jargon du user
   * @param jargon
   * @return
   * @throws ThesaurusException
   * @see
   */
  private void updateJargon(Jargon jargon) throws ThesaurusException {
    try {
      long newIdVoca = jargon.getIdVoca();
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      IdPK pk = new IdPK();
      pk.setId(jargon.getPK().getId());
      Jargon jargonToModify = daoJ.findByPrimaryKey(pk);
      jargonToModify.setIdVoca(newIdVoca);
      daoJ.update(jargonToModify);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.updateJargon",
          SilverpeasException.ERROR, "Thesaurus.EX_UPDATE_JARGON_FAILED", "", e);
    }
  }

  /**
   * Crée un nouveau jargon
   * @param jargon
   * @return
   * @throws ThesaurusException
   * @see
   */
  private void createJargon(Connection con, Jargon jargon)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      daoJ.add(con, jargon);
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.createJargon",
          SilverpeasException.ERROR, "Thesaurus.EX_CREATE_JARGON_FAILED", "", e);
    }
  }

  /**
   * Retourne le jargon de l'utilisateur (type 0) passé en paramètre
   * @param idUser
   * @return Jargon
   * @throws ThesaurusException
   * @see
   */
  public Jargon getJargon(String idUser) throws ThesaurusException {
    Jargon jargon = null;
    try {
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      IdPK pk = new IdPK();
      Collection<Jargon> jargons = daoJ.findByWhereClause(pk, " idUser='" + idUser +
          "'" + " AND type=0");
      Iterator<Jargon> i = jargons.iterator();
      if (i.hasNext()) {
        jargon = i.next();
      }
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.getJargon", SilverpeasException.ERROR,
          "Thesaurus.EX_CANT_GET_JARGON", "", e);
    }
    return jargon;
  }

  /**
   * Retourne la liste des autres synonymes d'un synonyme dans un vocabulaire retourne une
   * Collection de Synonym
   * @param idVoca
   * @param name
   * @return Collection
   * @throws ThesaurusException
   * @see
   */
  public Collection<Synonym> getSynonyms(long idVoca, String name) throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Synonym> daoS = getSynonymDao();
      IdPK pk = new IdPK();
      String nameEncode = encode(name);
      String nameNoAccent = FileServerUtils.replaceAccentChars(nameEncode);
      //search brut and case insensitive + search without accent and case insensitive
      return daoS.findByWhereClause(pk, " idVoca=" + idVoca +" AND "+
                                        "(LOWER(name) = LOWER('" + nameEncode + "') OR "+
                                        "LOWER(name) = LOWER('" + nameNoAccent + "'))");
    } catch (PersistenceException e) {
      throw new ThesaurusException("ThesaurusService.getSynonyms(long idVoca, String name)",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS_NAME", "", e);
    }
  }

  /**
   * Retourne la liste des autres synonymes d'un synonyme dans un vocabulaire retourne une
   * Collection de Synonym
   * @param idVoca
   * @param idTree
   * @return Collection
   * @throws ThesaurusException
   * @see
   */
  public Collection<Synonym> getSynonymsByTree(long idVoca, long idTree)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSynonymDao();
      IdPK pk = new IdPK();
      return daoS.findByWhereClause(pk, " idVoca=" + idVoca + " AND idTree=" + idTree);
    } catch (PersistenceException e) {
      throw new ThesaurusException(
          "ThesaurusService.getSynonymsByTree(long idVoca, long idTree)",
          SilverpeasException.ERROR, "Thesaurus.EX_CANT_GET_SYNONYMS_NAME",
          "idVoca = " + idVoca + ", idTree = " + idTree, e);
    }
  }
}