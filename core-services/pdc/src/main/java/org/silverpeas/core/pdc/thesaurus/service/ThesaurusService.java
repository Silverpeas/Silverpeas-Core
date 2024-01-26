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
import org.silverpeas.core.pdc.thesaurus.model.Jargon;
import org.silverpeas.core.pdc.thesaurus.model.Synonym;
import org.silverpeas.core.pdc.thesaurus.model.ThesaurusException;
import org.silverpeas.core.pdc.thesaurus.model.Vocabulary;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileServerUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Service dedicated to work on the thesaurus.
 */
@Service
public class ThesaurusService {

  private static final String ID_VOCA_EQUALS = " idVoca=";
  private static final String VOCABULARY_SYNONYMS_DELETION_FAILED =
      "Vocabulary synonyms deletion failed";
  private static final String VOCABULARY_JARGON_DELETION_FAILED =
      "Vocabulary jargon deletion failed";
  private static final String AND_ID_TREE_EQUALS = " AND idTree=";
  private static final String AND_ID_TERM_EQUALS = " AND idTerm=";
  private static final String CANNOT_GET_VOCABULARY_JARGON = "Cannot get vocabulary jargon";
  private static final String ID_USER_EQUALS = " idUser='";
  private static final String VOCABULARY_JARGON_CREATION_FAILED =
      "Vocabulary jargon creation failed";
  private SilverpeasBeanDAO<Vocabulary> vocabularyDao = null;
  private SilverpeasBeanDAO<Synonym> synonymDao = null;
  private SilverpeasBeanDAO<Jargon> jargonDao = null;

  protected ThesaurusService() {
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
   *
   */
  public Collection<Vocabulary> getListVocabulary() throws ThesaurusException {
    try {
      IdPK pk = new IdPK();
      return getVocabularyDao().findByWhereClause(pk, null);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Cannot get vocabularies", e);
    }
  }

  /**
   * Retourne le détail du vocabulaire à partir d'un idVoca
   * @param idVoca
   * @return Vocabulary
   * @throws ThesaurusException
   *
   */
  public Vocabulary getVocabulary(long idVoca) throws ThesaurusException {
    Vocabulary vocab = null;
    try {
      IdPK pk = new IdPK();
      pk.setIdAsLong(idVoca);
      vocab = getVocabularyDao().findByPrimaryKey(pk);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Cannot get vocabulary", e);
    }
    return vocab;
  }

  /**
   * Crée un nouveau vocabulaire et retourne l'id de celui-ci
   * @param voca
   * @return long
   * @throws ThesaurusException
   *
   */
  public long createVocabulary(Vocabulary voca) throws ThesaurusException {
    try {
      IdPK pkV = (IdPK) getVocabularyDao().add(voca);
      return pkV.getIdAsLong();
    } catch (PersistenceException e) {
      throw new ThesaurusException("Cannot create vocabulary", e);
    }
  }

  /**
   * Retourne vrai si le vocabulaire passé en paramètre existe déjà
   * @param name
   * @return boolean
   * @throws ThesaurusException
   *
   */
  @SuppressWarnings("unchecked")
  public boolean existVocabulary(String name) throws ThesaurusException {
    try {
      IdPK pk = new IdPK();
      Collection<Vocabulary> vocabs = getVocabularyDao().findByWhereClause(pk,
          " name= '" + encode(name) + "'");
      return !vocabs.isEmpty();
    } catch (PersistenceException e) {
      throw new ThesaurusException("Cannot get vocabulary of name = " + name, e);
    }
  }

  /**
   * Retourne une chaine qui a encodé les ' par des ''
   * @param name
   * @return String
   * @throws
   *
   */
  private String encode(String name) {
    StringBuilder str = new StringBuilder();

    for (int i = 0; i < name.length(); i++) {
      if (name.charAt(i) == '\'') {
        str.append("''");
      } else {
        str.append(name.charAt(i));
      }
    }
    return str.toString();
  }

  /**
   * Met à jour le vocabulaire passé en paramètre
   * @param voca
   * @return
   * @throws ThesaurusException
   *
   */
  public void updateVocabulary(Vocabulary voca) throws ThesaurusException {
    try {
      getVocabularyDao().update(voca);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Vocabulary update failed", e);
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
        if (con != null) {
          con.rollback();
        }
      } catch (Exception e1) {
        throw new ThesaurusException("Vocabulary deletion failed", e1);
      }

      throw new ThesaurusException("Vocabulary deletion failed", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Supprime tous les synonymes associés au vocabulaire passé en paramètre
   * @param idVoca
   * @return
   * @throws ThesaurusException
   *
   */
  private void deleteVocaSynonyms(Connection con, long idVoca)
      throws ThesaurusException {
    try {
      IdPK pk = new IdPK();
      getSynonymDao().removeWhere(con, pk, ID_VOCA_EQUALS + idVoca);
    } catch (PersistenceException e) {
      throw new ThesaurusException(VOCABULARY_SYNONYMS_DELETION_FAILED, e);
    }
  }

  /**
   * Supprime tous les jargons associés au vocabulaire passé en paramètre
   * @param idVoca
   * @return
   * @throws ThesaurusException
   *
   */
  private void deleteVocaJargons(Connection con, long idVoca)
      throws ThesaurusException {
    try {
      IdPK pk = new IdPK();
      getJargonDao().removeWhere(con, pk, ID_VOCA_EQUALS + idVoca);
    } catch (PersistenceException e) {
      throw new ThesaurusException(VOCABULARY_JARGON_DELETION_FAILED, e);
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
      throw new ThesaurusException(VOCABULARY_JARGON_DELETION_FAILED, e);
    }
  }

  /**
   * Supprime le jargon passé en paramètre
   * @param idJargon
   * @return
   * @throws ThesaurusException
   *
   */
  private void deleteJargon(String idJargon) throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoJ = getJargonDao();
      IdPK pk = new IdPK();
      pk.setId(idJargon);
      daoJ.remove(pk);
    } catch (PersistenceException e) {
      throw new ThesaurusException(VOCABULARY_JARGON_DELETION_FAILED, e);
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
        if (StringUtil.isDefined(synonyme.getName())) {
          createSynonym(con, synonyme);
        }
      }
    } catch (ThesaurusException e) {
      throw new ThesaurusException("Vocabulary synonyms creation failed", e);
    }
  }

  /**
   * Crée un nouveau synonyme à un terme
   * @param synonym
   * @return
   * @throws ThesaurusException
   *
   */
  private void createSynonym(Connection con, Synonym synonym)
      throws ThesaurusException {
    try {
      getSynonymDao().add(con, synonym);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Vocabulary synonyms creation failed", e);
    }
  }

  /**
   * Retourne la liste des synonymes d'un vocabulaire, pour un terme
   * @param idVoca
   * @param idTree
   * @param idTerm
   * @return Collection
   * @throws ThesaurusException
   *
   */
  @SuppressWarnings("unchecked")
  public Collection<Synonym> getSynonyms(long idVoca, long idTree, long idTerm)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSynonymDao();
      IdPK pk = new IdPK();
      return daoS.findByWhereClause(pk, ID_VOCA_EQUALS + idVoca + AND_ID_TREE_EQUALS + idTree +
          AND_ID_TERM_EQUALS + idTerm);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Cannot get vocabulary synonyms", e);
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
        if (con != null) {
          con.rollback();
        }
      } catch (Exception e1) {
        throw new ThesaurusException("Vocabulary synonyms update failed", e1);
      }
      throw new ThesaurusException("Vocabulary synonyms update failed", e);
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
   *
   */
  public void deleteSynonyms(String idVoca, String idTree, String idTerm) throws ThesaurusException {
    Connection con = null;
    try {
      con = DBUtil.openConnection();
      con.setAutoCommit(true);
      deleteSynonyms(con, Long.parseLong(idVoca), Long.parseLong(idTree), Long.parseLong(idTerm));
    } catch (Exception e) {
      throw new ThesaurusException(VOCABULARY_SYNONYMS_DELETION_FAILED, e);
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
          ID_VOCA_EQUALS + idVoca + AND_ID_TREE_EQUALS + idTree + AND_ID_TERM_EQUALS + idTerm);
    } catch (PersistenceException e) {
      throw new ThesaurusException(VOCABULARY_SYNONYMS_DELETION_FAILED, e);
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
      throw new ThesaurusException(VOCABULARY_SYNONYMS_DELETION_FAILED, e);
    }
  }

  /**
   * Supprime le synonyme passés en paramètre
   * @param idSynonym
   * @return
   * @throws ThesaurusException
   *
   */
  private void deleteSynonym(long idSynonym) throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSynonymDao();
      IdPK pk = new IdPK();
      pk.setIdAsLong(idSynonym);
      daoS.remove(pk);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Vocabulary synonym deletion failed", e);
    }
  }

  /**
   * Supprime les synonymes de tous les termes associés à l'axe passé en paramètre
   * @param idTree
   * @return
   * @throws ThesaurusException
   *
   */
  public void deleteSynonymsAxis(Connection con, long idTree)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSynonymDao();
      IdPK pk = new IdPK();
      daoS.removeWhere(con, pk, " idTree=" + idTree);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Vocabulary synonyms axis deletion failed", e);
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
      throw new ThesaurusException("Vocabulary synonyms terms deletion failed", e);
    }
  }

  /**
   * Supprime les synonymes du terme passé en paramètre
   * @param idTree
   * @param idTerm
   * @return
   * @throws ThesaurusException
   *
   */
  private void deleteSynonymsTerm(Connection con, long idTree, long idTerm)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSynonymDao();
      IdPK pk = new IdPK();
      daoS.removeWhere(con, pk, " idTree=" + idTree + AND_ID_TERM_EQUALS + idTerm);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Vocabulary synonyms term deletion failed", e);
    }
  }

  /**
   * Retourne la liste des utilisateurs utilisant le vocabulaire passé en paramètre retourne une
   * Collection de Jargon
   * @param idVoca
   * @return Collection
   * @throws ThesaurusException
   *
   */
  public Collection<Jargon> getJargons(long idVoca) throws ThesaurusException {
    try {
      IdPK pk = new IdPK();
      return getJargonDao().findByWhereClause(pk, ID_VOCA_EQUALS + idVoca);
    } catch (PersistenceException e) {
      throw new ThesaurusException(CANNOT_GET_VOCABULARY_JARGON, e);
    }
  }

  /**
   * Retourne la liste des jargons untilisés pas la liste des utilisateurs passés en paramètre
   * Retoune une Collection de Jargon
   * @param idUsers
   * @param type (0=UserDetail ou 1=Group)
   * @return
   * @throws ThesaurusException
   *
   */
  public Collection<Jargon> getJargons(Collection<String> idUsers, int type)
      throws ThesaurusException {
    Collection<Jargon> jargons = new ArrayList<>();
    try {
      SilverpeasBeanDAO daoJ = getJargonDao();
      IdPK pk = new IdPK();

      for (String idUser : idUsers) {
        Collection<Jargon> theJargons = daoJ.findByWhereClause(pk, ID_USER_EQUALS + idUser +
            "'" + " AND type=" + type);
        for (Jargon jargon : theJargons) {
          jargons.add(jargon);
        }
      }
    } catch (PersistenceException e) {
      throw new ThesaurusException(CANNOT_GET_VOCABULARY_JARGON, e);
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
   *
   */
  public Collection<Jargon> getJargons(Connection con, Collection<String> idUsers, int type)
      throws ThesaurusException {
    Collection<Jargon> jargons = new ArrayList<>();
    try {
      SilverpeasBeanDAO daoJ = getJargonDao();
      IdPK pk = new IdPK();

      for (String idUser : idUsers) {
        Collection<Jargon> theJargons = daoJ.findByWhereClause(con, pk, ID_USER_EQUALS +
            idUser + "'" + " AND type=" + type);
        for (Jargon jargon : theJargons) {
          jargons.add(jargon);
        }
      }
    } catch (PersistenceException e) {
      throw new ThesaurusException(CANNOT_GET_VOCABULARY_JARGON, e);
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
        List<String> user = new ArrayList<>();
        user.add(idUser);
        List<Jargon> actualJargon = new ArrayList<>(getJargons(user, type));

        if (!actualJargon.isEmpty()) {// l'utilisateur avait deja un jargon
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
      throw new ThesaurusException(VOCABULARY_JARGON_CREATION_FAILED, e);
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
        List<String> user = new ArrayList<>();
        user.add(idUser);
        List<Jargon> actualJargon = new ArrayList<>(getJargons(con, user, type));

        if (!actualJargon.isEmpty()) {// l'utilisateur avait deja un jargon
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
      throw new ThesaurusException(VOCABULARY_JARGON_CREATION_FAILED, e);
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
        if (con != null) {
          con.rollback();
        }
      } catch (Exception e1) {
        throw new ThesaurusException(VOCABULARY_JARGON_CREATION_FAILED, e1);
      }
      throw new ThesaurusException(VOCABULARY_JARGON_CREATION_FAILED, e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Crée un nouveau jargon
   * @param jargon
   * @return
   * @throws ThesaurusException
   *
   */
  private void createJargon(Jargon jargon) throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      daoJ.add(jargon);
    } catch (PersistenceException e) {
      throw new ThesaurusException(VOCABULARY_JARGON_CREATION_FAILED, e);
    }
  }

  /**
   * Met à jour le jargon du user
   * @param jargon
   * @return
   * @throws ThesaurusException
   *
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
      throw new ThesaurusException(VOCABULARY_JARGON_CREATION_FAILED, e);
    }
  }

  /**
   * Crée un nouveau jargon
   * @param jargon
   * @return
   * @throws ThesaurusException
   *
   */
  private void createJargon(Connection con, Jargon jargon)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      daoJ.add(con, jargon);
    } catch (PersistenceException e) {
      throw new ThesaurusException(VOCABULARY_JARGON_CREATION_FAILED, e);
    }
  }

  /**
   * Retourne le jargon de l'utilisateur (type 0) passé en paramètre
   * @param idUser
   * @return Jargon
   * @throws ThesaurusException
   *
   */
  public Jargon getJargon(String idUser) throws ThesaurusException {
    Jargon jargon = null;
    try {
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      IdPK pk = new IdPK();
      Collection<Jargon> jargons = daoJ.findByWhereClause(pk, ID_USER_EQUALS + idUser +
          "'" + " AND type=0");
      Iterator<Jargon> i = jargons.iterator();
      if (i.hasNext()) {
        jargon = i.next();
      }
    } catch (PersistenceException e) {
      throw new ThesaurusException(CANNOT_GET_VOCABULARY_JARGON, e);
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
   *
   */
  public Collection<Synonym> getSynonyms(long idVoca, String name) throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Synonym> daoS = getSynonymDao();
      IdPK pk = new IdPK();
      String nameEncode = encode(name);
      String nameNoAccent = FileServerUtils.replaceAccentChars(nameEncode);
      //search brut and case insensitive + search without accent and case insensitive
      return daoS.findByWhereClause(pk, ID_VOCA_EQUALS + idVoca +" AND "+
                                        "(LOWER(name) = LOWER('" + nameEncode + "') OR "+
                                        "LOWER(name) = LOWER('" + nameNoAccent + "'))");
    } catch (PersistenceException e) {
      throw new ThesaurusException("Cannot get vocabulary synonyms", e);
    }
  }

  /**
   * Retourne la liste des autres synonymes d'un synonyme dans un vocabulaire retourne une
   * Collection de Synonym
   * @param idVoca
   * @param idTree
   * @return Collection
   * @throws ThesaurusException
   *
   */
  public Collection<Synonym> getSynonymsByTree(long idVoca, long idTree)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO daoS = getSynonymDao();
      IdPK pk = new IdPK();
      return daoS.findByWhereClause(pk, ID_VOCA_EQUALS + idVoca + AND_ID_TREE_EQUALS + idTree);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Cannot get vocabulary synonyms by tree", e);
    }
  }
}