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
package com.silverpeas.thesaurusPeas.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.thesaurus.control.ThesaurusBm;
import com.silverpeas.thesaurus.model.Jargon;
import com.silverpeas.thesaurus.model.Synonym;
import com.silverpeas.thesaurus.model.Vocabulary;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ThesaurusSessionController extends
    AbstractComponentSessionController {

  private Vocabulary currentVoca = null;
  private Axis currentAxis = null;
  private Collection usersSelected = new ArrayList(); // Collection de userId
  private Collection groupsSelected = new ArrayList(); // Collection de groupId
  private Collection jargonsSelected = new ArrayList(); // Collection de Jargon
  // correspondant aux
  // usersSelected et
  // groupsSelected
  private Collection newJargonsSelected = new ArrayList(); // Collection de
  // Jargon
  // correspondant aux
  // usersSelected et
  // groupsSelected
  // n'ayant pas de
  // jargon
  private Vocabulary currentUpdVoca = null; // vocabulaire en cours de création
  // ou de modification
  private PdcBm pdcBm;
  private ThesaurusBm thBm;

  // gestions des objets en session
  private void setCurrentVoca(Vocabulary voca) {
    currentVoca = voca;
  }

  public void setCurrentUpdVoca(String nom, String desc) {
    Vocabulary voca = new Vocabulary();
    voca.setName(nom);
    voca.setDescription(desc);
    currentUpdVoca = voca;
  }

  private void setCurrentAxis(Axis axis) {
    currentAxis = axis;
  }

  private void setUsersSelected(Collection userIds) {
    usersSelected = userIds;
  }

  // private void setGroupsSelected(Collection groupIds) {groupsSelected =
  // groupIds;}
  private void setPdcBm() {
    pdcBm = new PdcBmImpl();
  }

  private void setThBm() {
    thBm = ThesaurusBm.getInstance();
  }

  public Vocabulary getCurrentVoca() {
    return currentVoca;
  }

  public Vocabulary getCurrentUpdVoca() {
    return currentUpdVoca;
  }

  public Axis getCurrentAxis() {
    return currentAxis;
  }

  private Collection getUsersSelected() {
    return usersSelected;
  }

  private Collection getGroupsSelected() {
    return groupsSelected;
  }

  public Collection getUserSelectedJargons() {
    return jargonsSelected;
  }

  public Collection getUserSelectedNewJargons() {
    return newJargonsSelected;
  }

  private PdcBm getPdcBm() {
    return pdcBm;
  }

  private ThesaurusBm getThBm() {
    return thBm;
  }

  public void resetCriterias() {
    setCurrentVoca(null);
    setCurrentAxis(null);
  }

  // constructeur
  public ThesaurusSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle,
      String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle);
    setPdcBm();
    setThBm();
  }

  // *** méthodes métier *** //

  // *** Gestion des axes et termes *** /
  /**
   * Retourne la liste des axes (Collection Axis)
   * 
   * @param
   * @return Collection
   * @throws ThesaurusException
   * @see
   */
  public Collection getListAxis() throws ThesaurusException {
    Collection axis = new ArrayList();
    try {
      axis = getPdcBm().getAxis();
    } catch (PdcException e) {
      throw new ThesaurusException("ThesaurusSessionController.getListAxis",
          SilverpeasException.ERROR, "thesaurusPeas.EX_CANT_GET_AXIS_LIST", e);
    }
    return axis;

  }

  /**
   * Retourne le détail d'un axe et le met en session (currentAxis)
   * 
   * @param idAxis
   * @return Axis
   * @throws ThesaurusException
   * @see
   */
  public Axis getAxis(String idAxis) throws ThesaurusException {
    try {
      if (idAxis.equals("0"))
        setCurrentAxis(null);
      else {
        Axis axis = getPdcBm().getAxisDetail(idAxis);
        setCurrentAxis(axis);
      }
      return getCurrentAxis();
    } catch (PdcException e) {
      throw new ThesaurusException("ThesaurusSessionController.getAxis",
          SilverpeasException.ERROR, "thesaurusPeas.EX_CANT_GET_AXIS", e);
    }
  }

  /**
   * Retourne la liste des termes de l'axe courant
   * (getCurrentAxis().getValues())
   * 
   * @param
   * @return Collection
   * @throws ThesaurusException
   * @see
   */
  public Collection getAxisTerms() {
    Collection terms = new ArrayList();
    if (getCurrentAxis() != null)
      terms = getCurrentAxis().getValues();
    return terms;
  }

  /**
   * Retourne le détail d'un terme à partir d'un idTerm et du rootId de l'axe
   * courant, met le terme en session (currentTerm)
   * 
   * @param idTerm
   * @return Value
   * @see
   */
  public Value getTerm(String idTerm) throws ThesaurusException {
    try {
      Value term = null;
      if ((getCurrentAxis() != null) && (!idTerm.equals("-1"))) {
        term = getPdcBm().getAxisValue(
            idTerm,
            new Integer(getCurrentAxis().getAxisHeader().getRootId())
                .toString());
        if (term == null)
          setCurrentAxis(null);
      } else if ((getCurrentAxis() != null) && (idTerm.equals("-1"))) {
        String id = getCurrentAxis().getAxisHeader().getPK().getId();
        getAxis(id);
      }
      return term;
    } catch (PdcException e) {
      throw new ThesaurusException("ThesaurusSessionController.getAxis",
          SilverpeasException.ERROR, "thesaurusPeas.EX_CANT_GET_TERM", e);
    }
  }

  // *** Gestion des vocabulaires *** //
  /**
   * Retourne la liste des vocabulaires
   * 
   * @param
   * @return Collection
   * @throws ThesaurusException
   * @see
   */
  public Collection getListVoca() throws ThesaurusException {
    Collection vocas = new ArrayList();
    try {
      vocas = getThBm().getListVocabulary();
      Collections.sort((List) vocas);
    } catch (ThesaurusException e) {
      throw new ThesaurusException("ThesaurusSessionController.getListVoca",
          SilverpeasException.ERROR,
          "thesaurusPeas.EX_CANT_GET_VOCABULARY_LIST", e);
    }
    return vocas;
  }

  /**
   * Retourne le détail d'un vocabulaire et le met en session (currentVoca)
   * 
   * @param idVoca
   * @return Vocabulary
   * @throws ThesaurusException
   * @see
   */
  public Vocabulary getVocabulary(long idVoca) throws ThesaurusException {
    if (idVoca == 0) {
      setCurrentVoca(null);
      setCurrentAxis(null);
    } else {
      Vocabulary voca = getThBm().getVocabulary(idVoca);
      setCurrentVoca(voca);
    }
    return getCurrentVoca();
  }

  /**
   * Crée en base un vocabulaire à partir du nom et de la description du
   * vocabulaire Retourne l'id du vocabulaire créé ou -1 si le vocabulaire ne
   * peut pas être créé car un vocabulaire de même nom existe
   * 
   * @param name
   * @param desc
   * @return long
   * @throws ThesaurusException
   * @see
   */
  public long createVocabulary(String name, String desc)
      throws ThesaurusException {
    long ret = -1;
    if (!existVocabulary(name)) {
      Vocabulary voca = new Vocabulary();
      voca.setName(name);
      voca.setDescription(desc);
      ret = getThBm().createVocabulary(voca);
    }
    return ret;
  }

  /**
   * Indique si un vocabulaire de nom "name" existe
   * 
   * @param name
   * @return boolean
   * @throws ThesaurusException
   * @see
   */
  private boolean existVocabulary(String name) throws ThesaurusException {
    return getThBm().existVocabulary(name);
  }

  /**
   * Affecte au vocabualire courant le nom et la description en paramètre et
   * enregistre en base Retourne -1 si le vocabulaire ne peut pas être modifié
   * car un vocabulaire de même nom existe, 0 sinon
   * 
   * @param name
   * @param desc
   * @return
   * @throws ThesaurusException
   * @see
   */
  public long updateVocabulary(String name, String desc)
      throws ThesaurusException {
    if ((!getCurrentVoca().getName().toLowerCase().equals(name.toLowerCase()))
        && (existVocabulary(name)))
      return -1;
    getCurrentVoca().setName(name);
    getCurrentVoca().setDescription(desc);
    getThBm().updateVocabulary(getCurrentVoca());
    return 0;
  }

  /**
   * Supprime un vocabulaire à partir de son id
   * 
   * @param idVoca
   * @return
   * @throws ThesaurusException
   * @see
   */
  public void deleteVocabulary(long idVoca) throws ThesaurusException {
    getThBm().deleteVocabulary(idVoca);
  }

  /**
   * Supprime le vocabulaire courant
   * 
   * @param
   * @return
   * @throws ThesaurusException
   * @see
   */
  public void deleteVocabulary() throws ThesaurusException {
    long idVoca = ((IdPK) getCurrentVoca().getPK()).getIdAsLong();
    setCurrentVoca(null);
    setCurrentAxis(null);
    getThBm().deleteVocabulary(idVoca);
  }

  // *** Gestion des synonymes *** //
  /**
   * Retourne un nouveau Synonyme pour le terme et le vocabulaire courant (new
   * Synonym(), set ...)
   * 
   * @param name
   * @return Synonym
   * @throws ThesaurusException
   * @see
   */
  private Synonym getNewSynonym(String name, String termId) {
    Synonym syno = new Synonym();
    syno.setName(name);
    if (getCurrentVoca() != null && getCurrentAxis() != null) {
      long idVoca = ((IdPK) getCurrentVoca().getPK()).getIdAsLong();
      long idTree = new Integer(getCurrentAxis().getAxisHeader().getRootId())
          .longValue();
      long idTerm = new Long(termId).longValue();
      syno.setIdVoca(idVoca);
      syno.setIdTree(idTree);
      syno.setIdTerm(idTerm);
    }
    return syno;
  }

  /**
   * Retourne la liste des synonymes du vocabulaire et du terme courant
   * 
   * @param
   * @return Collection
   * @throws ThesaurusException
   * @see
   */
  public Collection getSynonyms(String termId) throws ThesaurusException {
    long idVoca = ((IdPK) getCurrentVoca().getPK()).getIdAsLong();
    long idTree = new Integer(getCurrentAxis().getAxisHeader().getRootId())
        .longValue();
    long idTerm = new Long(termId).longValue();
    Collection synonyms = getThBm().getSynonyms(idVoca, idTree, idTerm);
    // Collections.sort((List)synonyms);
    return synonyms;
  }

  /**
   * Met à jour la liste des synonymes du vocabulaire et du terme courant
   * 
   * @param names
   * @return
   * @throws ThesaurusException
   * @see
   */
  public void updateSynonyms(Collection names, String termId)
      throws ThesaurusException {
    Collection synonyms = new ArrayList();
    Iterator it = names.iterator();
    if (it.hasNext()) {
      while (it.hasNext()) {
        String name = (String) it.next();
        Synonym syno = getNewSynonym(name, termId);
        synonyms.add(syno);
      }
      getThBm().updateSynonyms(synonyms);
    } else {
      getThBm().deleteSynonyms(currentVoca.getPK().getId(),
          currentAxis.getAxisHeader().getPK().getId(), termId);
    }
  }

  /**
   * Supprime un ensemble de synonymes à partir de leur id
   * 
   * @param idSynonyms
   * @return
   * @throws ThesaurusException
   * @see getNewSynonym
   */
  public void deleteSynonyms(Collection idSynonyms) throws ThesaurusException {
    getThBm().deleteSynonyms(idSynonyms);
  }

  // *** Gestion des users et groupes affectés aux vocabulaire *** //
  /**
   * Retourne la liste des users ou groupes utilisant le vocabulaire courant
   * (Collection Jargon)
   * 
   * @param
   * @return Collection
   * @throws ThesaurusException
   * @see
   */
  public Collection getJargons() throws ThesaurusException {
    long idVoca = ((IdPK) getCurrentVoca().getPK()).getIdAsLong();
    Collection jargons = getThBm().getJargons(idVoca);
    Collection userJargons = new ArrayList();
    Collection jargonsToDelete = new ArrayList();
    Iterator it = jargons.iterator();
    while (it.hasNext()) {
      Jargon jargon = (Jargon) it.next();
      if (jargon.readUserName() != null) // le user existe
        userJargons.add(jargon);
      else
        jargonsToDelete.add(jargon);
    }
    if (jargonsToDelete.size() > 0)
      getThBm().deleteJargons(jargons);
    return userJargons;
  }

  /**
   * Paramètre le userPannel => tous les users et groupes, users et groupes
   * affectés au voca courant sont sélectionnés
   * 
   * @param
   * @return
   * @throws ThesaurusException
   * @see getJargons
   */
  public String initSelectedUserPanel() throws ThesaurusException {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    String hostSpaceName = getSpaceLabel();
    PairObject hostComponentName = new PairObject(
        getString("thesaurus.componentName"), m_context
            + "/Rthesaurus/jsp/Main");
    PairObject[] hostPath = new PairObject[2];
    hostPath[0] = new PairObject(getString("thesaurus.thesaurus") + " > ",
        "/Rthesaurus/jsp/Back");
    String nomVoca = EncodeHelper.javaStringToHtmlString(getCurrentVoca()
        .getName());
    hostPath[1] = new PairObject(getString("thesaurus.BBlistAffectations")
        + nomVoca, "/Rthesaurus/jsp/EditAssignments");
    String hostUrl = m_context + "/Rthesaurus/jsp/SaveAssignUser";
    String cancelUrl = m_context + "/Rthesaurus/jsp/Main";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(hostPath);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    sel.setMultiSelect(true);
    sel.setPopupMode(false);

    Collection jargons = getJargons();
    Iterator it = jargons.iterator();
    if (it.hasNext()) {
      Collection users = new ArrayList();
      Collection groups = new ArrayList();
      while (it.hasNext()) {
        Jargon jargon = (Jargon) it.next();
        if (jargon.getType() == 1) // groupe
        {
          groups.add(jargon.getIdUser());
        }
        if (jargon.getType() == 0) // user
        {
          users.add(jargon.getIdUser());
        }
      }
      sel.setSelectedElements((String[]) users.toArray(new String[0]));
      sel.setSelectedSets((String[]) groups.toArray(new String[0]));
      sel.setFirstPage(Selection.FIRST_PAGE_CART);
    } else {
      sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    }
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /**
   * Paramètre le userPannel => tous les users et groupes, aucun sélectionné
   * 
   * @param
   * @return
   * @throws ThesaurusException
   * @see
   */
  public String initUserPanel() throws ThesaurusException {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    String hostSpaceName = "";
    PairObject hostComponentName = new PairObject(
        getString("thesaurus.componentName"), m_context
            + "/Rthesaurus/jsp/Main");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("thesaurus.thesaurus"),
        "/Rthesaurus/jsp/Back");
    String hostUrl = m_context + "/Rthesaurus/jsp/UserAssignments";
    String cancelUrl = m_context + "/Rthesaurus/jsp/Main";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(hostPath);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    sel.setMultiSelect(true);
    sel.setPopupMode(false);
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /**
   * met en session les jargons des users et groupes sélectionnés au travers
   * du userPanel met en session la liste des users et la liste des groupes
   * sélectionnés au travers du userPanel
   * 
   * @param
   * @return
   * @throws ThesaurusException
   * @see
   */
  public void setUserPanelJargons() throws ThesaurusException {
    Selection sel = getSelection();
    String[] userIds = SelectionUsersGroups.getDistinctUserIds(sel
        .getSelectedElements(), sel.getSelectedSets());
    Collection users = new ArrayList();
    for (int i = 0; (userIds != null) && (i < userIds.length); i++) {
      users.add(userIds[i]);
    }
    setUsersSelected(users);
    setJargons();
  }

  /**
   * met en session les jargons des users et groupes sélectionnés au travers
   * du userPanel
   * 
   * @param
   * @return
   * @throws ThesaurusException
   * @see
   */
  public void setJargons() throws ThesaurusException {
    setUserSelectedJargons();
    setUserSelectedNewJargons();
  }

  /**
   * Set la liste des jargons pour la liste des users et groupes sélectionnés
   * au travers du userPanel ayant un jargon
   * 
   * @param
   * @return Collection
   * @throws ThesaurusException
   * @see
   */
  private void setUserSelectedJargons() throws ThesaurusException {
    Collection userJargons = getThBm().getJargons(getUsersSelected(), 0);
    Collection jargons = getThBm().getJargons(getGroupsSelected(), 1);
    jargons.addAll(userJargons);
    Collection effectiveJargons = new ArrayList();
    Collection jargonsToDelete = new ArrayList();
    Iterator it = jargons.iterator();
    while (it.hasNext()) {
      Jargon jargon = (Jargon) it.next();
      if (jargon.readUserName() != null) // le user existe
        effectiveJargons.add(jargon);
      else
        jargonsToDelete.add(jargon);
    }
    if (jargonsToDelete.size() > 0)
      getThBm().deleteJargons(jargons);

    jargonsSelected = effectiveJargons;

  }

  /**
   * Set la liste des jargons pour la liste des users et groupes sélectionnés
   * au travers du userPanel n'ayant pas de jargons
   * 
   * @param
   * @return
   * @see
   */
  private void setUserSelectedNewJargons() {
    Collection jargons = new ArrayList();
    Iterator itU = getUsersSelected().iterator();
    Iterator itG = getGroupsSelected().iterator();
    ArrayList jSelected = (ArrayList) getUserSelectedJargons();
    while (itU.hasNext()) {
      String userId = (String) itU.next();
      Jargon jargon = new Jargon();
      jargon.setIdVoca(0);
      jargon.setIdUser(userId);
      jargon.setType(0);
      int index = jSelected.indexOf(jargon);
      if (index == -1) {
        jargons.add(jargon);
      }
    }
    while (itG.hasNext()) {
      String groupId = (String) itG.next();
      Jargon jargon = new Jargon();
      jargon.setIdVoca(0);
      jargon.setIdUser(groupId);
      jargon.setType(1);
      int index = jSelected.indexOf(jargon);
      if (index == -1) {
        jargons.add(jargon);
      }
    }
    newJargonsSelected = jargons;
  }

  /**
   * Enregistre l'affectation au vocabulaire courant de tous les users et
   * groupes sélectionnés au travers du userPanel s'il n'existe pas de
   * conflits Retourne le nombre de conflits
   * 
   * @param
   * @return int
   * @throws ThesaurusException
   * @see existJargonsConflict
   * @see createNewJargons
   */
  public int assignVocabulary() throws ThesaurusException {
    int nbConflict = existJargonsConflict(((IdPK) getCurrentVoca().getPK())
        .getIdAsLong());
    if (nbConflict == 0) {
      createJargons();
    }
    return nbConflict;
  }

  /**
   * Enregistre l'affectation au vocabulaire passé en paramètre de tous les
   * users et groupes sélectionnés au travers du userPanel s'il n'existe pas
   * de conflits Retourne le nombre de conflits
   * 
   * @param idVoca
   * @return int
   * @throws ThesaurusException
   * @see existJargonsConflict
   * @see createNewJargons
   */
  public int assignVocabulary(long idVoca) throws ThesaurusException {
    int nbConflict = 0;
    if (idVoca == 0) {// on retire le vocabulaire
      deleteJargons();
    } else {
      nbConflict = existJargonsConflict(idVoca);
      if (nbConflict == 0) {
        createJargons(idVoca);
      }
    }
    return nbConflict;
  }

  /**
   * Retourne le nombre de conflits : nbre de user ou groupe sélectionnés au
   * travers du userPanel... ... qui possède déjà une affectation autre que
   * celle au vocabulaire passé en paramètre
   * 
   * @param idVoca
   * @return
   * @throws ThesaurusException
   * @see
   */
  private int existJargonsConflict(long idVoca) {
    int nbConflict = 0;
    Collection jargons = getUserSelectedJargons();
    Iterator it = jargons.iterator();
    while (it.hasNext()) {
      Jargon jargon = (Jargon) it.next();
      if (jargon.getIdVoca() != idVoca) {
        nbConflict++;
      }
    }
    return nbConflict;
  }

  /**
   * Enregistre en base les affectations au vocabulaire courant de tous les
   * users et groupes sélectionnés au travers du userPanel
   * 
   * @param
   * @return
   * @throws ThesaurusException
   * @see
   */
  public void createJargons() throws ThesaurusException {
    long idVoca = ((IdPK) getCurrentVoca().getPK()).getIdAsLong();
    getThBm().createVocaJargons(getJargons(idVoca), idVoca);
  }

  /**
   * Enregistre en base les affectations au vocabulaire courant de tous les
   * users et groupes sélectionnés au travers du userPanel et ... ... n'ayant
   * pas déjà une affectation autre que celle au vocabulaire courant
   * 
   * @param
   * @return
   * @throws ThesaurusException
   * @see getNewJargons
   */
  public void createNewJargons() throws ThesaurusException {
    long idVoca = ((IdPK) getCurrentVoca().getPK()).getIdAsLong();
    Collection jargons = new ArrayList();
    jargons.addAll(getNewJargons(idVoca));
    jargons.addAll(getSameJargons(idVoca));
    getThBm().createVocaJargons(jargons, idVoca);
  }

  /**
   * Enregistre en base les affectations au vocabulaire passé en paramètre de
   * tous les users et groupes sélectionnés au travers du userPanel
   * 
   * @param idVoca
   * @return
   * @throws ThesaurusException
   * @see
   */
  public void createJargons(long idVoca) throws ThesaurusException {
    getThBm().createJargons(getJargons(idVoca));
  }

  /**
   * Enregistre en base les affectations au vocabulaire passé en paramètre de
   * tous les users et groupes sélectionnés au travers du userPanel et ... ...
   * n'ayant pas déjà une affectation autre que celle au vocabulaire
   * 
   * @param idVoca
   * @return
   * @throws ThesaurusException
   * @see getNewJargons
   */
  public void createNewJargons(long idVoca) throws ThesaurusException {
    getThBm().createJargons(getNewJargons(idVoca));
  }

  /**
   * Retourne la liste des nouvelles affectations (à enregistrer) pour la liste
   * des users et groupes sélectionnés au travers du userPanel i.e. vérifie
   * pour chaque user et groupe s'ils n'ont pas un jargon différent de idVoca
   * dans la liste des jargons ... ... des users et groupes sélectionnés
   * (jargonsSelected)
   * 
   * @param idVoca
   * @return Collection
   * @see
   */
  private Collection getNewJargons(long idVoca) {
    Collection jargons = getUserSelectedNewJargons();
    Collection newJargons = new ArrayList();
    Iterator it = jargons.iterator();
    while (it.hasNext()) {
      Jargon jargon = (Jargon) it.next();
      Jargon newJargon = new Jargon();
      newJargon.setIdVoca(idVoca);
      newJargon.setIdUser(jargon.getIdUser());
      newJargon.setType(jargon.getType());
      newJargons.add(newJargon);
    }
    return newJargons;
  }

  /**
   * Retourne la liste des affectations (à enregistrer) portant sur le
   * vocabulaire pour la liste des users et groupes sélectionnés au travers du
   * userPanel
   * 
   * @param idVoca
   * @return Collection
   * @see
   */
  private Collection getSameJargons(long idVoca) {
    Collection jargons = getUserSelectedJargons();
    Collection sameJargons = new ArrayList();
    Iterator it = jargons.iterator();
    while (it.hasNext()) {
      Jargon jargon = (Jargon) it.next();
      if (jargon.getIdVoca() == idVoca)
        sameJargons.add(jargon);
    }
    return sameJargons;
  }

  /**
   * Retourne la liste de toutes les affectations (à enregistrer) pour la liste
   * des users et groupes sélectionnés au travers du userPanel
   * 
   * @param idVoca
   * @return Collection
   * @see
   */
  private Collection getJargons(long idVoca) {
    Collection jargons = getUserSelectedJargons();
    Collection newJargons = new ArrayList();
    Iterator it = jargons.iterator();
    while (it.hasNext()) {
      Jargon jargon = (Jargon) it.next();
      Jargon newJargon = new Jargon();
      newJargon.setIdVoca(idVoca);
      newJargon.setIdUser(jargon.getIdUser());
      newJargon.setType(jargon.getType());
      newJargons.add(newJargon);
    }
    newJargons.addAll(getNewJargons(idVoca));
    return newJargons;
  }

  /**
   * Supprime l'affectation de vocabulaire actuel de la liste des users et
   * groupes sélectionnés au travers du userPanel
   * 
   * @param
   * @return
   * @see
   */
  public void deleteJargons() throws ThesaurusException {
    // jargons actuelles a supprimer
    Collection jargons = getUserSelectedJargons();
    if (jargons.size() > 0) {
      // supprime tous les jargons
      getThBm().deleteJargons(jargons);
    }
  }

}
