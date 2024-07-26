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
import org.silverpeas.core.persistence.jdbc.bean.*;
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
@SuppressWarnings("deprecation")
public class ThesaurusService {

  private static final String VOCABULARY_SYNONYMS_DELETION_FAILED =
      "Vocabulary synonyms deletion failed";
  private static final String VOCABULARY_JARGON_DELETION_FAILED =
      "Vocabulary jargon deletion failed";
  private static final String CANNOT_GET_VOCABULARY_JARGON = "Cannot get vocabulary jargon";
  private static final String VOCABULARY_JARGON_CREATION_FAILED =
      "Vocabulary jargon creation failed";
  private static final String VOCA_ID = "idVoca";
  private static final String TREE_ID = "idTree";
  private static final String TERM_ID = "idTerm";
  private static final String ID_USER = "idUser";
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
      vocabularyDao = SilverpeasBeanDAOFactory.getDAO(Vocabulary.class);
    }
    return vocabularyDao;
  }

  private SilverpeasBeanDAO<Synonym> getSynonymDao() throws PersistenceException {
    if (synonymDao == null) {
      synonymDao = SilverpeasBeanDAOFactory.getDAO(Synonym.class);
    }
    return synonymDao;
  }

  private SilverpeasBeanDAO<Jargon> getJargonDao() throws PersistenceException {
    if (jargonDao == null) {
      jargonDao = SilverpeasBeanDAOFactory.getDAO(Jargon.class);
    }
    return jargonDao;
  }

  /**
   * Gets all the available vocabularies.
   * @return a collection of all of the vocabularies.
   * @throws ThesaurusException if an error occurs
   *
   */
  public Collection<Vocabulary> getListVocabulary() throws ThesaurusException {
    try {
      return getVocabularyDao().findBy(BeanCriteria.emptyCriteria());
    } catch (PersistenceException e) {
      throw new ThesaurusException("Cannot get vocabularies", e);
    }
  }

  /**
   * Gets the vocabulary with the specified identifier.
   * @param idVoca the identifier of the vocabulary.
   * @return Vocabulary
   * @throws ThesaurusException if the vocabulary cannot be got.
   *
   */
  public Vocabulary getVocabulary(long idVoca) throws ThesaurusException {
    Vocabulary vocab;
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
   * Saves the new specified vocabulary in the data source.
   * @param voca the vocabulary to save.
   * @return long the unique identifier of the persisted vocabulary.
   * @throws ThesaurusException if the persistence of the vocabulary fails.
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
   * Is the specified vocabularies exist in the data source.
   * @param name the name of the vocabularies.
   * @return true of there is at least one vocabulary with the given name already exists. False
   * otherwise.
   * @throws ThesaurusException if the vocabularies with the given name cannot be got.
   *
   */
  public boolean existVocabulary(String name) throws ThesaurusException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion("name", encode(name));
      Collection<Vocabulary> vocabs = getVocabularyDao().findBy(criteria);
      return !vocabs.isEmpty();
    } catch (PersistenceException e) {
      throw new ThesaurusException("Cannot get vocabulary of name = " + name, e);
    }
  }

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
   * Updates in the data source the vocabulary with the specified one. The vocabulary in the data
   * source is identified by the unique identifier of the specified one.
   * @param voca the vocabulary to update.
   * @throws ThesaurusException if the update fails.
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
   * Deletes in the datasource the specified vocabulary.
   * @param idVoca the unique identifier of the vocabulary to delete.
   * @throws ThesaurusException if the deletion fails.
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
   * Deletes all the synonyms of the specified vocabulary.
   * @param con a connection to the datasource.
   * @param idVoca the unique identifier of a vocabulary.
   * @throws ThesaurusException if the deletion fails.
   */
  private void deleteVocaSynonyms(Connection con, long idVoca)
      throws ThesaurusException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(VOCA_ID, idVoca);
      getSynonymDao().removeBy(con, criteria);
    } catch (PersistenceException e) {
      throw new ThesaurusException(VOCABULARY_SYNONYMS_DELETION_FAILED, e);
    }
  }

  /**
   * Deletes all the jargons of the specified vocabulary.
   * @param con a connection to the datasource.
   * @param idVoca the unique identifier of a vocabulary.
   * @throws ThesaurusException if the deletion fails.
   */
  private void deleteVocaJargons(Connection con, long idVoca)
      throws ThesaurusException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(VOCA_ID, idVoca);
      getJargonDao().removeBy(con, criteria);
    } catch (PersistenceException e) {
      throw new ThesaurusException(VOCABULARY_JARGON_DELETION_FAILED, e);
    }
  }

  /**
   * Deletes all the specified jargons in the datasource.
   * @param jargons a collection of jargons to delete.
   * @throws ThesaurusException if the deletion fails.
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
   * Delete the specified jargons in the datasource.
   * @param idJargon the unique identifier of a jargon.
   * @throws ThesaurusException if the deletion fails.
   *
   */
  private void deleteJargon(String idJargon) throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      IdPK pk = new IdPK();
      pk.setId(idJargon);
      daoJ.remove(pk);
    } catch (PersistenceException e) {
      throw new ThesaurusException(VOCABULARY_JARGON_DELETION_FAILED, e);
    }
  }

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

  private void createSynonym(Connection con, Synonym synonym)
      throws ThesaurusException {
    try {
      getSynonymDao().add(con, synonym);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Vocabulary synonyms creation failed", e);
    }
  }

  /**
   * Gets the synonyms of a vocabulary for a given term.
   * @param idVoca the identifier of the vocabulary.
   * @param idTree the unique identifier of the tree in which is defined the terms.
   * @param idTerm the unique identifier of a term.
   * @return a collection of the synonyms of the vocabulary.
   * @throws ThesaurusException if the synonyms cannot be got.
   *
   */
  public Collection<Synonym> getSynonyms(long idVoca, long idTree, long idTerm)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Synonym> daoS = getSynonymDao();
      BeanCriteria criteria = BeanCriteria.addCriterion(VOCA_ID, idVoca)
          .and(TREE_ID, idTree)
          .and(TERM_ID, idTerm);
      return daoS.findBy(criteria);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Cannot get vocabulary synonyms", e);
    }
  }

  /**
   * Updates the with specified synonyms the related ones in the datasource. The synonyms in the
   * datasource are identified by the unique identifier of the given synonyms.
   * @param synonyms the synonyms to update.
   * @throws ThesaurusException if the update fails.
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

  private void deleteSynonyms(Connection con, long idVoca, long idTree, long idTerm) throws
      ThesaurusException {
    try {
      SilverpeasBeanDAO<Synonym> daoS = getSynonymDao();
      BeanCriteria criteria = BeanCriteria.addCriterion(VOCA_ID, idVoca)
              .and(TREE_ID, idTree)
              .and(TERM_ID, idTerm);
      daoS.removeBy(con, criteria);
    } catch (PersistenceException e) {
      throw new ThesaurusException(VOCABULARY_SYNONYMS_DELETION_FAILED, e);
    }
  }

  /**
   * Deletes the synonyms of all of the terms defined in the specified tree.
   * @param idTree the unique identifier of a tree.
   * @throws ThesaurusException if the deletion fails.
   *
   */
  public void deleteSynonymsAxis(Connection con, long idTree)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Synonym> daoS = getSynonymDao();
      BeanCriteria criteria = BeanCriteria.addCriterion(TREE_ID, idTree);
      daoS.removeBy(con, criteria);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Vocabulary synonyms axis deletion failed", e);
    }
  }

  /**
   * Deletes all the synonyms of the specified terms defined in the given tree.
   * @param con the connection to the datasource.
   * @param idTree the unique identifier of a tree.
   * @param idTerms a list of identifiers of terms.
   * @throws ThesaurusException if the deletion fails.
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

  private void deleteSynonymsTerm(Connection con, long idTree, long idTerm)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Synonym> daoS = getSynonymDao();
      BeanCriteria criteria = BeanCriteria.addCriterion(TREE_ID, idTree)
          .and(TERM_ID, idTerm);
      daoS.removeBy(con, criteria);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Vocabulary synonyms term deletion failed", e);
    }
  }

  public Collection<Jargon> getJargons(long idVoca) throws ThesaurusException {
    try {
      return getJargonDao().findBy(BeanCriteria.addCriterion(VOCA_ID, idVoca));
    } catch (PersistenceException e) {
      throw new ThesaurusException(CANNOT_GET_VOCABULARY_JARGON, e);
    }
  }

  /**
   * Gets the list of jargons used by the specified users.
   * @param idUsers a collection of the unique identifier of either users or groups of users.
   * @param type the type of the resource referred by the identifiers: 0 for users, 1 for group
   * of users.
   * @return a collection of jargons.
   * @throws ThesaurusException if the jargons cannot be got from the datasource.
   */
  public Collection<Jargon> getJargons(Collection<String> idUsers, int type)
      throws ThesaurusException {
    Collection<Jargon> jargons = new ArrayList<>();
    try {
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      for (String idUser : idUsers) {
        BeanCriteria criteria = BeanCriteria.addCriterion(ID_USER, idUser)
            .and("type", type);
        Collection<Jargon> theJargons = daoJ.findBy(criteria);
        jargons.addAll(theJargons);
      }
    } catch (PersistenceException e) {
      throw new ThesaurusException(CANNOT_GET_VOCABULARY_JARGON, e);
    }
    return jargons;
  }

  /**
   * Gets the list of jargons used by the specified users.
   * @param con a connection to the datasource.
   * @param idUsers a collection of the unique identifier of either users or groups of users.
   * @param type the type of the resource referred by the identifiers: 0 for users, 1 for group
   * of users.
   * @return a collection of jargons.
   * @throws ThesaurusException if the jargons cannot be got from the datasource.
   */
  public Collection<Jargon> getJargons(Connection con, Collection<String> idUsers, int type)
      throws ThesaurusException {
    Collection<Jargon> jargons = new ArrayList<>();
    try {
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      for (String idUser : idUsers) {
        Collection<Jargon> theJargons = daoJ.findBy(con,
            BeanCriteria.addCriterion(ID_USER, idUser).and("type", type));
        jargons.addAll(theJargons);
      }
    } catch (PersistenceException e) {
      throw new ThesaurusException(CANNOT_GET_VOCABULARY_JARGON, e);
    }
    return jargons;
  }

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
   * Creates the specified jargons of the given vocabulary. The existing jargons of the
   * vocabulary are first deleted before to create the specified ones.
   * @param jargons the jargons to create.
   * @param idVoca the unique identifier of the vocabulary.
   * @throws ThesaurusException if the creation (or the previous deletion) fails.
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

  private void createJargon(Jargon jargon) throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      daoJ.add(jargon);
    } catch (PersistenceException e) {
      throw new ThesaurusException(VOCABULARY_JARGON_CREATION_FAILED, e);
    }
  }

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

  private void createJargon(Connection con, Jargon jargon)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      daoJ.add(con, jargon);
    } catch (PersistenceException e) {
      throw new ThesaurusException(VOCABULARY_JARGON_CREATION_FAILED, e);
    }
  }

  public Jargon getJargon(String idUser) throws ThesaurusException {
    Jargon jargon = null;
    try {
      SilverpeasBeanDAO<Jargon> daoJ = getJargonDao();
      BeanCriteria criteria = BeanCriteria.addCriterion(ID_USER, idUser)
          .and("type", 0);
      Collection<Jargon> jargons = daoJ.findBy(criteria);
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
   * Gets all the others wynonyms of a given synonym of the specified vocabulary.
   * @param idVoca the unique identifier of the vocabulary.
   * @param name the name of a synonym.
   * @return a collection of synonyms.
   * @throws ThesaurusException if the synonyms cannot be got.
   */
  public Collection<Synonym> getSynonyms(long idVoca, String name) throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Synonym> daoS = getSynonymDao();
      String nameEncode = encode(name);
      String nameNoAccent = FileServerUtils.replaceAccentChars(nameEncode);
      //search brut and case insensitive + search without accent and case insensitive
      BeanCriteria criteria = BeanCriteria.addCriterion(VOCA_ID, idVoca)
          .and(BeanCriteria.emptyCriteria().andWithFunction("LOWER(name)", nameEncode, "LOWER")
              .orWithFunction("LOWER(name)", nameNoAccent, "LOWER"));
      return daoS.findBy(criteria);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Cannot get vocabulary synonyms", e);
    }
  }

  public Collection<Synonym> getSynonymsByTree(long idVoca, long idTree)
      throws ThesaurusException {
    try {
      SilverpeasBeanDAO<Synonym> daoS = getSynonymDao();
      BeanCriteria criteria = BeanCriteria.addCriterion(VOCA_ID, idVoca)
          .and(TREE_ID, idTree);
      return daoS.findBy(criteria);
    } catch (PersistenceException e) {
      throw new ThesaurusException("Cannot get vocabulary synonyms by tree", e);
    }
  }
}