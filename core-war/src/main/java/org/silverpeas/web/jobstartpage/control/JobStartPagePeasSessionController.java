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
package org.silverpeas.web.jobstartpage.control;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.silverpeas.core.admin.component.model.*;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.quota.exception.QuotaRuntimeException;
import org.silverpeas.core.admin.service.*;
import org.silverpeas.core.admin.service.SpaceProfile;
import org.silverpeas.core.admin.space.*;
import org.silverpeas.core.admin.space.quota.ComponentSpaceQuotaKey;
import org.silverpeas.core.admin.space.quota.DataStorageSpaceQuotaKey;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplates;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.memory.MemoryUnit;
import org.silverpeas.core.web.look.SilverpeasLook;
import org.silverpeas.core.web.mvc.controller.AbstractAdminComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.web.jobstartpage.*;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import static org.silverpeas.core.admin.component.model.ComponentInst.getComponentLocalId;

public class JobStartPagePeasSessionController extends AbstractAdminComponentSessionController {

  public static final int SCOPE_BACKOFFICE = 0;
  public static final int SCOPE_FRONTOFFICE = 1;
  public static final int MAINTENANCE_OFF = 0;
  public static final int MAINTENANCE_PLATFORM = 1;
  public static final int MAINTENANCE_ONEPARENT = 2;
  public static final int MAINTENANCE_THISSPACE = 3;
  private static final Properties templateConfiguration = new Properties();

  private final AdminController adminController;
  private int scope = SCOPE_BACKOFFICE;
  NavBarManager m_NavBarMgr = new NavBarManager();
  String m_ManagedSpaceId = null;
  boolean m_isManagedSpaceRoot = true;
  Selection selection = null;
  String m_ManagedInstanceId = null;
  ProfileInst m_ManagedProfile = null;
  ProfileInst m_ManagedInheritedProfile = null;
  // Space sort buffers
  SpaceInst[] m_BrothersSpaces = new SpaceInst[0];
  ComponentInst[] m_BrothersComponents = new ComponentInst[0];
  private final CommunityFactory communityFactory;

  public JobStartPagePeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle",
        "org.silverpeas.jobStartPagePeas.settings.jobStartPagePeasIcons",
        "org.silverpeas.jobStartPagePeas.settings.jobStartPagePeasSettings");
    setComponentRootName(URLUtil.CMP_JOBSTARTPAGEPEAS);
    selection = getSelection();
    adminController = ServiceProvider.getService(AdminController.class);
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR,
        JobStartPagePeasSettings.TEMPLATE_PATH);
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR,
        JobStartPagePeasSettings.CUSTOMERS_TEMPLATE_PATH);
    communityFactory = getCommunityFactory();
  }

  /**
   * Dedicated for tests
   */
  public JobStartPagePeasSessionController(final MainSessionController controller,
      final ComponentContext context, final String localizedMessagesBundleName,
      final String iconFileName, final String settingsFileName) {
    super(controller, context, localizedMessagesBundleName, iconFileName, settingsFileName);
    adminController = null;
    communityFactory = getCommunityFactory();
  }

  // Init at first entry
  public void init(final boolean force) {
    if (force || !m_NavBarMgr.hasBeenInitialized()) {
      m_NavBarMgr.initWithUser(this, getUserDetail());
    }
  }

  @Override
  public boolean isAccessGranted() {
    return isAccessGranted(getManagedSpaceId(), getManagedInstanceId(), true);
  }

  // method du spaceInst
  public SpaceInst getSpaceInstById() {
    if (!StringUtil.isDefined(getManagedSpaceId())) {
      return null;
    }
    return adminController.getSpaceInstById("WA" + getManagedSpaceId());
  }

  public void setManagedSpaceId(String sId, boolean isManagedSpaceRoot) {
    String spaceId = getShortSpaceId(sId);
    checkAccessGranted(spaceId, null, true);
    m_ManagedSpaceId = spaceId;
    m_isManagedSpaceRoot = isManagedSpaceRoot;

  }

  public String getManagedSpaceId() {
    return m_ManagedSpaceId;
  }

  public DisplaySorted getManagedSpace() {
    return getManagedSpace(getManagedSpaceId());
  }

  public DisplaySorted getManagedSpace(String spaceId) {
    return m_NavBarMgr.getSpace(spaceId);
  }

  public boolean isManagedSpaceRoot() {
    return m_isManagedSpaceRoot;
  }

  // methods set
  public void setSubSpaceId(String subSpaceId) {
    if (m_NavBarMgr.setCurrentSubSpace(subSpaceId)) {
      setManagedSpaceId(subSpaceId, false);
    } else {
      setManagedSpaceId(getSpaceId(), true);
    }
    setManagedInstanceId(null, getScope());
  }

  public void setSpaceId(String spaceUserId) {
    String spaceId = spaceUserId;
    if (StringUtil.isDefined(spaceId)) {
      List<SpaceInstLight> path =
          ServiceProvider.getService(AdminController.class).getPathToSpace(spaceId, true);
      if (path.size() > 1) {
        spaceId = path.get(0).getId();
      }
    }
    if (m_NavBarMgr.setCurrentSpace(spaceId)) {
      setManagedSpaceId(spaceId, true);
    } else {
      setManagedSpaceId(null, true);
    }
    String subSpaceId = null;
    if (spaceId != null && !spaceId.equals(spaceUserId)) {
      subSpaceId = spaceUserId;
    }
    setSubSpaceId(subSpaceId);
  }

  private String getShortSpaceId(String spaceId) {

    if (spaceId != null && spaceId.startsWith(SpaceInst.SPACE_KEY_PREFIX)) {
      return spaceId.substring(SpaceInst.SPACE_KEY_PREFIX.length());
    }
    return (spaceId == null) ? "" : spaceId;
  }

  @Override
  public String getSpaceId() {
    return m_NavBarMgr.getCurrentSpaceId();
  }

  public Collection<DisplaySorted> getSpaces() {
    return m_NavBarMgr.getAvailableSpaces();
  }

  public Collection<DisplaySorted> getSpaceComponents() {
    return m_NavBarMgr.getAvailableSpaceComponents();
  }

  public String getSubSpaceId() {
    return m_NavBarMgr.getCurrentSubSpaceId();
  }

  public Collection<DisplaySorted> getSubSpaces() {
    return m_NavBarMgr.getAvailableSubSpaces();
  }

  public Collection<DisplaySorted> getSubSpaceComponents() {
    return m_NavBarMgr.getAvailableSubSpaceComponents();
  }

  public void setSpaceMaintenance(String spaceId, boolean mode) {
    setSpaceModeMaintenance(spaceId, mode);
  }

  public void refreshCurrentSpaceCache() {
    m_NavBarMgr.resetSpaceCache(getManagedSpaceId());
  }

  public void setManagedInstanceId(String sId) {
    checkAccessGranted(null, sId, true);
    m_ManagedInstanceId = sId;
    setScope(SCOPE_BACKOFFICE);
  }

  public void setManagedInstanceId(String sId, int scope) {
    setManagedInstanceId(sId);
    setScope(scope);
  }

  public String getManagedInstanceId() {
    return m_ManagedInstanceId;
  }

  public void setManagedProfile(ProfileInst sProfile) {
    m_ManagedProfile = sProfile;

    if (sProfile != null) {
      m_ManagedInheritedProfile = adminController.getComponentInst(
          getManagedInstanceId()).getInheritedProfileInst(sProfile.getName());
    } else {
      m_ManagedInheritedProfile = null;
    }
  }

  public ProfileInst getManagedProfile() {
    return m_ManagedProfile;
  }

  public String getManagedProfileHelp(String componentName) {
    return new LocalizedWAComponent(getComponentByName(componentName), getLanguage())
        .getProfile(getManagedProfile().getName())
        .getHelp();
  }

  public ProfileInst getManagedInheritedProfile() {
    return m_ManagedInheritedProfile;
  }

  public Boolean isProfileEditable() {
    return JobStartPagePeasSettings.IS_PROFILE_EDITABLE;
  }

  public Boolean isBackupEnable() {
    return JobStartPagePeasSettings.IS_BACKUP_ENABLED;
  }

  public String getConfigSpacePosition() {
    return JobStartPagePeasSettings.SPACE_DISPLAY_POSITION_CONFIG;
  }

  /*
   * ********************* Gestion des espaces ****************************************
   */

  public SpaceInst[] getBrotherSpaces(boolean isNew) {
    String[] sids;
    SpaceInst spaceint1 = getSpaceInstById();
    String fatherId;
    String currentSpaceId;
    int j;

    if (isNew) {
      if (spaceint1 == null) {
        fatherId = null;
      } else {
        fatherId = "WA" + getManagedSpaceId();
      }
      currentSpaceId = "";
    } else {
      fatherId = spaceint1.getDomainFatherId();
      currentSpaceId = "WA" + getManagedSpaceId();
    }

    if (fatherId != null && !fatherId.equals("0")) {
      sids = adminController.getAllSubSpaceIds(fatherId);
    } else {
      sids = adminController.getAllRootSpaceIds();
    }

    if (sids == null || sids.length == 0) {
      return new SpaceInst[0];
    }
    if (isNew) {
      m_BrothersSpaces = new SpaceInst[sids.length];
    } else {
      m_BrothersSpaces = new SpaceInst[sids.length - 1];
    }
    j = 0;
    for (String sid : sids) {
      if (isNew || !sid.equals(currentSpaceId)) {
        m_BrothersSpaces[j++] = adminController.getSpaceInstById(sid);
      }
    }
    Arrays.sort(m_BrothersSpaces, Comparator.comparing(SpaceInst::getOrderNum));
    return m_BrothersSpaces;
  }

  public void setSpacePlace(String idSpaceBefore) {
    int orderNum = 0;
    int i;
    SpaceInst theSpace = getSpaceInstById();

    for (i = 0; i < m_BrothersSpaces.length; i++) {
      if (idSpaceBefore.equals(m_BrothersSpaces[i].getId())) {
        theSpace.setOrderNum(orderNum);
        adminController.updateSpaceOrderNum(theSpace.getId(), orderNum);
        orderNum++;
      }
      if (m_BrothersSpaces[i].getOrderNum() != orderNum) {
        m_BrothersSpaces[i].setOrderNum(orderNum);
        adminController.updateSpaceOrderNum(m_BrothersSpaces[i].getId(), orderNum);
      }
      orderNum++;
    }
    if (orderNum == i) {
      theSpace.setOrderNum(orderNum);
      adminController.updateSpaceOrderNum(theSpace.getId(), orderNum);
    }
    if (!theSpace.isRoot()) {
      m_NavBarMgr.resetSpaceCache(theSpace.getDomainFatherId());
    } else {
      m_NavBarMgr.resetAllCache();
    }
  }

  public SpaceInst getSpaceInstById(String idSpace) {
    if (idSpace == null || idSpace.isEmpty()) {
      return null;
    }
    if (idSpace.length() > 2 && idSpace.startsWith("WA")) {
      idSpace = idSpace.substring(2);
    }
    return adminController.getSpaceInstById("WA" + idSpace);
  }

  public ComponentInst createCommunitySpace(SpaceInst newSpace) {
    newSpace.setCommunitySpace(true);
    newSpace.setInheritanceBlocked(true);
    newSpace.setFirstPageType(SpaceHomePageType.COMPONENT_INST.ordinal());
    boolean created = createSpace(newSpace);
    if (created) {
      // spawn the component instance dedicated to the community memberships management
      var componentInst = communityFactory.createCommunity(newSpace);
      try {
        addComponentInst(componentInst);
      } catch (QuotaException e) {
        // shouldn't occurs
        SilverLogger.getLogger(this).error(e.getMessage());
        return null;
      }
      return componentInst;
    }
    return null;
  }

  public boolean createSpace(SpaceInst newSpace) {
    SpaceInst parentSpace = getSpaceInstById();
    if (parentSpace != null) {
      // on est en creation de sous-espace
      newSpace.setDomainFatherId(parentSpace.getId());
    }
    newSpace.setCreatorUserId(getUserId());
    String sSpaceInstId = addSpaceInst(newSpace);
    if (StringUtil.isDefined(sSpaceInstId)) {
      if (parentSpace != null) {
        // on est en creation de sous-espace
        setSubSpaceId(sSpaceInstId);
      } else {
        // on est en creation d'espace
        setSpaceId(sSpaceInstId);
      }
    } else {
      return false;
    }
    // Only use global variable to set spacePosition
    boolean spaceFirst = !JobStartPagePeasSettings.SPACE_DISPLAY_POSITION_AFTER.equalsIgnoreCase(
        JobStartPagePeasSettings.SPACE_DISPLAY_POSITION_CONFIG);
    newSpace.setDisplaySpaceFirst(spaceFirst);
    return true;
  }

  public String addSpaceInst(SpaceInst spaceInst) {
    String res = adminController.addSpaceInst(spaceInst);
    if (StringUtil.isDefined(res)) {
      // Finally refresh the cache
      m_NavBarMgr.addSpaceInCache(res);
    }
    return res;
  }

  public boolean updateSpaceInst(SpaceInst spaceInst) {
    spaceInst.setUpdaterUserId(getUserId());
    return StringUtil.isDefined(adminController.updateSpaceInst(spaceInst));
  }

  public SpaceLookHelper getSpaceLookHelper() {
    List<File> files;
    try {
      files = (List<File>) FileFolderManager.getAllFile(getSpaceLookBasePath());
    } catch (org.silverpeas.core.util.UtilException e) {
      files = new ArrayList<>();
    }

    SpaceLookHelper slh = new SpaceLookHelper("Space" + getManagedSpaceId());
    slh.setFiles(files);

    return slh;
  }

  public void removeExternalElementOfSpaceAppearance(String fileName) {
    File file = new File(getSpaceLookBasePath(), fileName);
    FileUtils.deleteQuietly(file);
  }

  public void updateSpaceAppearance(List<FileItem> items) throws Exception {
    processExternalElementsOfSpaceAppearance(items);

    String selectedLook = FileUploadUtil.getParameter(items, "SelectedLook");
    if (!StringUtil.isDefined(selectedLook)) {
      selectedLook = null;
    }

    SpaceInst space = getSpaceInstById();
    space.setLook(selectedLook);

    // Retrieve global variable configuration
    String configSpacePosition = getConfigSpacePosition();
    boolean isDisplaySpaceFirst;
    // Use global variable if defined else use SpacePosition request parameter.
    if ("BEFORE".equalsIgnoreCase(configSpacePosition)) {
      isDisplaySpaceFirst = true;
    } else if ("AFTER".equalsIgnoreCase(configSpacePosition)) {
      isDisplaySpaceFirst = false;
    } else {
      String spacePosition = FileUploadUtil.getParameter(items, "SpacePosition");
      isDisplaySpaceFirst = !(StringUtil.isDefined(spacePosition)
          && "2".equalsIgnoreCase(spacePosition));
    }
    // Set new space position VO
    space.setDisplaySpaceFirst(isDisplaySpaceFirst);

    // Save these changes in database
    updateSpaceInst(space);
  }

  private void processExternalElementsOfSpaceAppearance(List<FileItem> items) throws Exception {
    String mainDir = "Space" + getManagedSpaceId();
    FileRepositoryManager.createAbsolutePath(mainDir, "look");

    String path = SilverpeasLook.getSilverpeasLook().getSpaceBasePath(getManagedSpaceId());

    processSpaceWallpaper(items, path);
    processSpaceCSS(items, path);
  }

  private String getSpaceLookBasePath() {
    return SilverpeasLook.getSilverpeasLook().getSpaceBasePath(getManagedSpaceId());
  }

  private void processSpaceWallpaper(List<FileItem> items, String path) throws Exception {
    FileItem file = FileUploadUtil.getFile(items, "wallPaper");
    if (file != null && StringUtil.isDefined(file.getName())) {
      String extension = FileRepositoryManager.getFileExtension(file.getName());
      if (extension != null && extension.equalsIgnoreCase("jpeg")) {
        extension = "jpg";
      }

      // Remove all wallpapers to ensure it is unique
      File dir = new File(path);
      Collection<File> wallpapers =
          FileUtils.listFiles(dir, FileFilterUtils.prefixFileFilter(
          SilverpeasLook.DEFAULT_WALLPAPER_PROPERTY, IOCase.INSENSITIVE), null);
      for (File wallpaper : wallpapers) {
        FileUtils.deleteQuietly(wallpaper);
      }

      String imgExtension = extension != null ? "." + extension.toLowerCase() : "";
      file.write(new File(path + File.separatorChar + "wallPaper" + imgExtension));
    }
  }

  private void processSpaceCSS(List<FileItem> items, String path) throws Exception {
    FileItem file = FileUploadUtil.getFile(items, "css");
    if (file != null && StringUtil.isDefined(file.getName())) {
      // Remove previous file
      File css = new File(path, SilverpeasLook.SPACE_CSS + ".css");
      if (css.exists()) {
        Files.delete(css.toPath());
      }

      file.write(css);
    }
  }

  public void saveSpaceQuota(final SpaceInst spaceInst, int componentSpaceQuotaMaxCount,
      long dataStorageQuotaMaxCount) {
    boolean isAdmin = isUserAdmin();
    // Component space quota
    if (isAdmin && JobStartPagePeasSettings.COMPONENTS_IN_SPACE_QUOTA_ENABLED) {
      try {
        if (componentSpaceQuotaMaxCount >= 0) {
          spaceInst.setComponentSpaceQuotaMaxCount(componentSpaceQuotaMaxCount);
        }
        SpaceServiceProvider.getComponentSpaceQuotaService().initialize(
            ComponentSpaceQuotaKey.from(spaceInst), spaceInst.getComponentSpaceQuota().getMaxCount());
        spaceInst.clearComponentSpaceQuotaCache();
      } catch (QuotaException qe) {
        throw new QuotaRuntimeException(qe.getMessage(), qe);
      }
    }

    // Data storage quota
    if (isAdmin && JobStartPagePeasSettings.DATA_STORAGE_IN_SPACE_QUOTA_ENABLED) {
      try {
        if (dataStorageQuotaMaxCount >= 0) {
          spaceInst.setDataStorageQuotaMaxCount(UnitUtil.convertTo(
              dataStorageQuotaMaxCount, MemoryUnit.MB, MemoryUnit.B));
        }
        SpaceServiceProvider.getDataStorageSpaceQuotaService().initialize(
            DataStorageSpaceQuotaKey.from(spaceInst), spaceInst.getDataStorageQuota().getMaxCount());
        spaceInst.clearDataStorageQuotaCache();
      } catch (QuotaException qe) {
        throw new QuotaRuntimeException(qe.getMessage(), qe);
      }
    }
  }

  private boolean isRemovingSpaceAllowed(String spaceId) {
    if (isUserAdmin()) {
      // admin can always remove space
      return true;
    } else {
      List<String> spaceIds = Arrays.asList(adminController.getUserManageableSpaceIds(getUserId()));
      if (spaceIds.isEmpty()) {
        // user is not a space manager
        return false;
      } else {
        // Check if user manages this space or one of its parent
        List<SpaceInstLight> spaces = getOrganisationController().getPathToSpace(spaceId);
        for (SpaceInstLight spaceInPath : spaces) {
          if (spaceIds.contains(getShortSpaceId(spaceInPath.getId()))) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public void deleteSpace(String spaceId) {

    if (!isRemovingSpaceAllowed(spaceId)) {
      SilverLogger.getLogger(this)
          .error("User " + getUserId() + " isn't allowed to delete space " + spaceId);
    } else {
      SpaceInst spaceint1 = adminController.getSpaceInstById(spaceId);
      boolean definitiveDelete = !JobStartPagePeasSettings.IS_BASKET_ENABLED;
      if (JobStartPagePeasSettings.IS_BASKET_ENABLED && isUserAdmin()) {
        definitiveDelete = !JobStartPagePeasSettings.USE_BASKET_WHEN_ADMIN;
      }

      String res = adminController.deleteSpaceInstById(getUserDetail(), spaceint1.getId(),
          definitiveDelete);

      m_NavBarMgr.removeSpaceInCache(res);
      if (isManagedSpaceRoot()) {
        setManagedSpaceId(null, true);
      } else {
        setManagedSpaceId(getSpaceId(), true);
      }
    }
  }

  public void recoverSpaceRights(String spaceId) throws AdminException {
    RightRecover rightRecover = AdministrationServiceProvider.getRightRecoveringService();
    if (spaceId == null) {
      rightRecover.recoverRights();
    } else if (StringUtil.isDefined(spaceId)) {
      rightRecover.recoverSpaceRights(spaceId);
    }
  }

  /*
   * ********************* Gestion des managers d'espaces ****************************************
   */

  public SpaceProfile getCurrentSpaceProfile(String role) {
    return getOrganisationController()
        .getSpaceProfile(getManagedSpaceId(), SilverpeasRole.fromString(role));
  }

  // user panel de selection de n groupes et n users
  public void initUserPanelSpaceForGroupsUsers(List<String> userIds,
      List<String> groupIds) {
    selection.resetAll();
    selection.setFilterOnDeactivatedState(false);

    String hostSpaceName = getMultilang().getString("JSPP.manageHomePage");
    selection.setHostSpaceName(hostSpaceName);

    String idFather = getSpaceInstById().getDomainFatherId();
    setHostComponentName(idFather);

    LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(getLanguage());
    //noinspection rawtypes
    Pair[] hostPath = {new Pair<>(generalMessage.getString("GML.selection"), null)};
    //noinspection unchecked
    selection.setHostPath(hostPath);
    selection.setPopupMode(true);
    selection.setHtmlFormElementId("roleItems");
    selection.setHtmlFormName("dummy");
    selection.setSelectedElements(userIds);
    selection.setSelectedSets(groupIds);
  }

  private void setHostComponentName(String idFather) {
    Pair<String, String> hostComponentName;
    if (idFather != null && !idFather.equals("0")) {// je suis sur un ss-espace
      SpaceInst spaceFather = getSpaceInstById(idFather);
      hostComponentName = new Pair<>(spaceFather.getName() + " > " + getSpaceInstById().
          getName(), null);
    } else {
      hostComponentName = new Pair<>(getSpaceInstById().getName(), null);
    }
    selection.setHostComponentName(hostComponentName);
  }

  public void updateSpaceRole(String role, List<String> userIds, List<String> groupIds) {
    // Update the profile
    SpaceInst spaceint1 = getSpaceInstById();
    SpaceProfileInst spaceProfileInst = spaceint1.getDirectSpaceProfileInst(role);
    if (spaceProfileInst == null) {
      spaceProfileInst = new SpaceProfileInst();
      spaceProfileInst.setName(role);
      if (role.equals("Manager")) {
        spaceProfileInst.setLabel("Manager d'espace");
      }
      spaceProfileInst.setSpaceFatherId(spaceint1.getId());
      spaceProfileInst.setUsers(userIds);
      spaceProfileInst.setGroups(groupIds);
      // Add the profile
      adminController.addSpaceProfileInst(spaceProfileInst, getUserId());
    } else {
      spaceProfileInst.setUsers(userIds);
      spaceProfileInst.setGroups(groupIds);

      // Update the profile
      adminController.updateSpaceProfileInst(spaceProfileInst, getUserId());
    }
  }

  /**
   * ********************* Gestion de la corbeille ****************************************
   */
  public List<SpaceInstLight> getRemovedSpaces() {
    List<SpaceInstLight> removedSpaces = adminController.getRemovedSpaces();
    String name;
    for (SpaceInstLight space: removedSpaces) {
      space.setRemoverName(getOrganisationController().getUserDetail(String.valueOf(space.
          getRemovedBy())).getDisplayedName());

      // Remove suffix
      name = space.getName();
      name = name.substring(0, name.indexOf(Administration.Constants.BASKET_SUFFIX));
      space.setName(name);
    }
    return removedSpaces;
  }

  public List<ComponentInstLight> getRemovedComponents() {
    List<ComponentInstLight> removedComponents = adminController.getRemovedComponents();
    String name;
    for (ComponentInstLight component: removedComponents) {
      component.setRemoverName(getOrganisationController().getUserDetail(String.valueOf(component.
          getRemovedBy())).getDisplayedName());

      // Remove suffix
      name = component.getLabel();
      name = name.substring(0, name.indexOf(Administration.Constants.BASKET_SUFFIX));
      component.setLabel(name);
    }
    return removedComponents;
  }

  public void restoreSpaceFromBin(String spaceId) {
    adminController.restoreSpaceFromBasket(spaceId);

    // Display restored space in navBar
    m_NavBarMgr.resetAllCache();
  }

  public void deleteSpaceInBin(String spaceId) {
    adminController.deleteSpaceInstById(getUserDetail(), spaceId, true);
  }

  public void restoreComponentFromBin(String componentId) {
    adminController.restoreComponentFromBasket(componentId);
  }

  public void deleteComponentInBin(String componentId) {
    adminController.deleteComponentInst(getUserDetail(), componentId, true);
  }

  /**
   * ********************* Gestion des composants ****************************************
   */
  public ComponentInst[] getBrotherComponents(boolean isNew) {
    List<ComponentInst> arc = getSpaceInstById().getAllComponentsInst();
    if (arc == null || arc.isEmpty()) {
      return new ComponentInst[0];
    }
    if (isNew) {
      m_BrothersComponents = new ComponentInst[arc.size()];
    } else {
      m_BrothersComponents = new ComponentInst[arc.size() - 1];
    }
    int j = 0;
    for (ComponentInst theComponent : arc) {
      if (isNew || !theComponent.getId().equals(getManagedInstanceId())) {
        m_BrothersComponents[j++] = theComponent;
      }
    }
    Arrays.sort(m_BrothersComponents, Comparator.comparing(ComponentInst::getOrderNum));
    return m_BrothersComponents;
  }

  public void setComponentPlace(String idComponentBefore) {
    adminController
        .setComponentPlace(getManagedInstanceId(), idComponentBefore, m_BrothersComponents);
    m_NavBarMgr.resetSpaceCache(getManagedSpaceId());
  }

  public List<LocalizedWAComponent> getAllLocalizedComponents() {
    // liste des composants triés ordre alphabétique
    Map<String, WAComponent> resTable = adminController.getAllComponents();
    List<LocalizedWAComponent> result = new ArrayList<>(resTable.size());
    for (WAComponent component : resTable.values()) {
      result.add(new LocalizedWAComponent(component, getLanguage()));
    }
    result.sort((o1, o2) -> {
      String valComp1 = o1.getSuite() + o1.getLabel();
      String valComp2 = o2.getSuite() + o2.getLabel();
      return valComp1.toUpperCase().compareTo(valComp2.toUpperCase());
    });
    return result;
  }

  public WAComponent getComponentByName(String name) {
    return adminController.getAllComponents().get(name);
  }

  private void setParameterOptions(ParameterList parameterList, String appName) {
    for (Parameter parameter : parameterList) {
      if (parameter.isXmlTemplate()) {
        // display only templates allowed according to context
        parameter.setOptions(getVisibleTemplateOptions(appName, parameter));
      }
    }
  }

  private void setParameterValues(ParameterList parameters) {
    if (StringUtil.isDefined(getManagedInstanceId())) {
      ComponentInst componentInst = getComponentInst(getManagedInstanceId());
      if (componentInst != null) {
        parameters.setValues(componentInst.getParameters());
      }
    }
  }

  public AllComponentParameters getParameters(WAComponent component, boolean creation) {
    LocalizedParameterList parameters = getUngroupedParameters(component, creation);
    return new AllComponentParameters(component, parameters, getGroupsOfParameters(component));
  }

  private LocalizedParameterList getUngroupedParameters(WAComponent component, boolean creation) {
    ParameterList parameterList = ParameterList.copy(component.getParameters());
    parameterList.sort();
    setParameterOptions(parameterList, component.getName());
    setParameterValues(parameterList);

    LocalizedParameterList localized = new LocalizedParameterList(component, parameterList, getLanguage());

    ComponentInst existingComponent = null;
    if (!creation) {
      existingComponent = getComponentInst(getManagedInstanceId());
    }
    localized.add(0, createIsHiddenParam(component, existingComponent));
    if (JobStartPagePeasSettings.IS_PUBLIC_PARAMETER_ENABLED) {
      localized.add(0, createIsPublicParam(component, existingComponent));
    }

    return localized;
  }

  private List<LocalizedGroupOfParameters> getGroupsOfParameters(WAComponent component) {
    List<GroupOfParameters> groups = component.getSortedGroupsOfParameters();
    List<LocalizedGroupOfParameters> localizedGroups = new ArrayList<>();
    for (GroupOfParameters group : groups) {
      GroupOfParameters clonedGroup = new GroupOfParameters(group);
      ParameterList parameters = clonedGroup.getParameterList();
      parameters.sort();
      setParameterOptions(parameters, component.getName());
      setParameterValues(parameters);
      localizedGroups.add(new LocalizedGroupOfParameters(component, clonedGroup, getLanguage()));
    }
    return localizedGroups;
  }

  private List<Option> getVisibleTemplateOptions(String appName, Parameter parameter) {
    GlobalContext aContext = new GlobalContext(getManagedSpaceId(), getManagedInstanceId());
    aContext.setComponentName(appName);
    PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
    List<Option> options = parameter.getOptions();
    List<Option> visibleOptions = new ArrayList<>();
    for (Option option : options) {
      String templateName = option.getValue();
      try {
        if (templateManager.isPublicationTemplateVisible(templateName, aContext)) {
          visibleOptions.add(option);
        }
      } catch (PublicationTemplateException e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return visibleOptions;
  }

  public String addComponentInst(ComponentInst componentInst) throws QuotaException {
    componentInst.setCreatorUserId(getUserId());
    return adminController.addComponentInst(componentInst);
  }

  public ComponentInst getComponentInst(String sInstanceId) {
    return adminController.getComponentInst(sInstanceId);
  }

  public String updateComponentInst(ComponentInst componentInst) {
    componentInst.setUpdaterUserId(getUserId());
    return adminController.updateComponentInst(componentInst);
  }

  public void deleteComponentInst(String sInstanceId) {
    boolean definitiveDelete = !JobStartPagePeasSettings.IS_BASKET_ENABLED;
    if (JobStartPagePeasSettings.IS_BASKET_ENABLED && isUserAdmin()) {
      definitiveDelete = !JobStartPagePeasSettings.USE_BASKET_WHEN_ADMIN;
    }

    adminController.deleteComponentInst(getUserDetail(), sInstanceId, definitiveDelete);
  }

  // ArrayList de ProfileInst dont l'id est vide ou pas
  // role non cree : id vide - name - label (identique à name)
  // role cree : id non vide - name - label
  public List<ProfileInst> getAllProfiles(ComponentInst m_FatherComponentInst) {
    ArrayList<ProfileInst> alShowProfile = new ArrayList<>();
    String sComponentName = m_FatherComponentInst.getName();
    // profils dispo
    String[] asAvailProfileNames = adminController.getAllProfilesNames(sComponentName);
    for (String profileName : asAvailProfileNames) {
      boolean bFound = false;

      ProfileInst profile = m_FatherComponentInst.getProfileInst(profileName);
      if (profile != null) {
        bFound = true;
        setProfileLabel(sComponentName, profileName, profile);
        alShowProfile.add(profile);
      }

      if (!bFound) {
        profile = new ProfileInst();
        profile.setName(profileName);
        setProfileLabel(sComponentName, profileName, profile);
        alShowProfile.add(profile);
      }
    }
    return alShowProfile;

  }

  private void setProfileLabel(String sComponentName, String profileName, ProfileInst profile) {
    String label = adminController.getProfileLabelByName(sComponentName, profileName,
        getLanguage());
    if (!StringUtil.isDefined(label)) {
      label = adminController.getProfileLabelByName(sComponentName, profileName,
          DisplayI18NHelper.getDefaultLanguage());
    }
    if (StringUtil.isDefined(label)) {
      profile.setLabel(label);
    }
  }

  public ProfileInst getProfile(String sProfileId, String sProfileName,
      String sProfileLabel) {
    if (StringUtil.isDefined(sProfileId)) {
      return adminController.getProfileInst(sProfileId);
    }
    ProfileInst res = new ProfileInst();
    res.setName(sProfileName);
    res.setLabel(sProfileLabel);
    return res;
  }

  public List<UserDetail> getAllCurrentUserInstance() {
    return userIds2users(getManagedProfile().getAllUsers());
  }

  public List<Group> getAllCurrentGroupInstance() {
    List<String> alGroupIds = getManagedProfile().getAllGroups();

    return groupIds2groups(alGroupIds);
  }

  public List<Group> groupIds2groups(List<String> groupIds) {
    List<Group> res = new ArrayList<>();
    Group theGroup;

    for (int nI = 0; groupIds != null && nI < groupIds.size(); nI++) {
      theGroup = adminController.getGroupById(groupIds.get(nI));
      if (theGroup != null) {
        res.add(theGroup);
      }
    }

    return res;
  }

  public List<UserDetail> userIds2users(List<String> userIds) {
    List<UserDetail> res = new ArrayList<>();
    UserDetail user;

    for (int nI = 0; userIds != null && nI < userIds.size(); nI++) {
      user = getUserDetail(userIds.get(nI));
      if (user != null) {
        res.add(user);
      }
    }

    return res;
  }

  // user panel de selection de n groupes et n users
  public void initUserPanelInstanceForGroupsUsers(String compoURL, List<String> userIds,
      List<String> groupIds) {
    String profileId = getManagedProfile().getId();
    String profile = getManagedProfile().getLabel();

    selection.resetAll();
    selection.setFilterOnDeactivatedState(false);

    String hostSpaceName = getMultilang().getString("JSPP.manageHomePage");
    selection.setHostSpaceName(hostSpaceName);

    SpaceInst space = getSpaceInstById();
    if (space != null) {
      String idFather = space.getDomainFatherId();
      setHostComponentName(idFather);
    }

    LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(getLanguage());
    String compoName = getComponentInst(getManagedInstanceId()).getLabel();
    @SuppressWarnings("unchecked")
    Pair<String, String>[] hostPath =
        new Pair[]{new Pair<>(compoName + " > " + profile + " > " + generalMessage.
            getString("GML.selection"), null)};
    selection.setHostPath(hostPath);

    String hostUrl = compoURL + "EffectiveUpdateInstanceProfile";
    if (!StringUtil.isDefined(profileId)) { // creation
      hostUrl = compoURL + "EffectiveCreateInstanceProfile";
    }
    selection.setGoBackURL(hostUrl);
    selection.setPopupMode(true);
    selection.setHtmlFormElementId("roleItems");
    selection.setHtmlFormName("dummy");
    selection.setSelectedElements(userIds);
    selection.setSelectedSets(groupIds);
  }

  public void updateInstanceProfile(String[] userIds, String[] groupIds) {

    ProfileInst profile = getManagedProfile();

    // set groupIds and userIds
    profile.setUsers(Arrays.asList(userIds));
    profile.setGroups(Arrays.asList(groupIds));

    if (!StringUtil.isDefined(profile.getId())) {
      profile.setComponentFatherId(getComponentLocalId(getManagedInstanceId()));
      // Add the profile
      adminController.addProfileInst(profile, getUserId());

    } else {
      // Update the profile
      adminController.updateProfileInst(profile, getUserId());
    }

    // mise à jour
    setManagedProfile(profile);
  }

  public void copyComponent(String id) throws ClipboardException {
    copyOrCutComponent(id, false);
  }

  public void cutComponent(String id) throws ClipboardException {
    copyOrCutComponent(id, true);
  }

  private void copyOrCutComponent(String id, boolean cut) throws ClipboardException {
    checkAccessGranted(null, id, false);
    ComponentInst componentInst = getComponentInst(id);
    ComponentSelection compoSelect = new ComponentSelection(componentInst);
    compoSelect.setCut(cut);
    addClipboardSelection(compoSelect);
  }

  public void copySpace(String id) throws ClipboardException {
    copyOrCutSpace(id, false);
  }

  public void cutSpace(String id) throws ClipboardException {
    copyOrCutSpace(id, true);
  }

  private void copyOrCutSpace(String id, boolean cut) throws ClipboardException {
    checkAccessGranted(id, null, false);
    SpaceInst space = getSpaceInstById(id);
    SpaceSelection spaceSelect = new SpaceSelection(space);
    spaceSelect.setCut(cut);
    addClipboardSelection(spaceSelect);
  }

  public void paste(Map<String, String> options) throws ClipboardException, JobStartPagePeasException {
    checkAccessGranted(getManagedSpaceId(), getManagedInstanceId(), false);
    try {
      Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
      boolean refreshCache = false;
      for (ClipboardSelection clipObject : clipObjects) {
        if (clipObject != null) {
          if (clipObject.isDataFlavorSupported(ComponentSelection.ComponentDetailFlavor)) {
            ComponentInst compo = (ComponentInst) clipObject.getTransferData(
                ComponentSelection.ComponentDetailFlavor);
            if (clipObject.isCut()) {
              moveComponent(compo.getId());
            } else {
              PasteDetail pasteDetail = new PasteDetail(compo.getId(), getUserId());
              pasteDetail.setOptions(options);
              pasteComponent(pasteDetail);
            }
            refreshCache = true;
          } else if (clipObject.isDataFlavorSupported(SpaceSelection.SpaceFlavor)) {
            SpaceInst space = (SpaceInst) clipObject.getTransferData(SpaceSelection.SpaceFlavor);
            if (clipObject.isCut()) {
              moveSpace(space.getId());
            } else {
              PasteDetail pasteDetail = new PasteDetail(getUserId());
              pasteDetail.setFromSpaceId(space.getId());
              pasteDetail.setOptions(options);
              pasteSpace(pasteDetail);
            }
            refreshCache = true;
          }
        }
      }
      if (refreshCache) {
        m_NavBarMgr.resetAllCache();
      }
    } catch (Exception e) {
      m_NavBarMgr.resetAllCache();
      throw new JobStartPagePeasException(e);
    }
    clipboardPasteDone();
  }

  /**
   * Get names of all copied components directly or indirectly (case of a space)
   * @return a Set of component names
   * @throws JobStartPagePeasException if an error occurs while getting copied components
   */
  public Set<String> getCopiedComponents() throws JobStartPagePeasException {
    Set<String> copiedComponents = new HashSet<>();
    try {
      Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
      for (ClipboardSelection clipObject : clipObjects) {
        if (clipObject != null) {
          if (clipObject.isDataFlavorSupported(ComponentSelection.ComponentDetailFlavor)) {
            ComponentInst compo = (ComponentInst) clipObject.getTransferData(
                ComponentSelection.ComponentDetailFlavor);
            if (!clipObject.isCut()) {
              copiedComponents.add(compo.getName());
            }
          } else if (clipObject.isDataFlavorSupported(SpaceSelection.SpaceFlavor)) {
            SpaceInst space = (SpaceInst) clipObject.getTransferData(SpaceSelection.SpaceFlavor);
            if (!clipObject.isCut()) {
              String[] componentIds = getOrganisationController().getAllComponentIdsRecur(space.getId());
              for (String componentId : componentIds) {
                String componentName = ComponentInst.getComponentName(componentId);
                copiedComponents.add(componentName);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      throw new JobStartPagePeasException(e);
    }
    return copiedComponents;
  }

  /**
   * Paste component with profiles
   *
   * @param pasteDetail the detail about the component to paste.
   * @throws JobStartPagePeasException if an error occurs while pasting the component.
   */
  private void pasteComponent(PasteDetail pasteDetail) throws JobStartPagePeasException {
    try {
      pasteDetail.setToSpaceId(getManagedSpaceId());
      String sComponentId = adminController.copyAndPasteComponent(pasteDetail);
      // Adding ok
      if (StringUtil.isDefined(sComponentId)) {
        setManagedInstanceId(sComponentId);
        refreshCurrentSpaceCache();
      }
    } catch (Exception e) {
      throw new JobStartPagePeasException(
          "componentId = " + pasteDetail.getFromComponentId() + " in space " + getManagedSpaceId(),
          e);
    }
  }

  private void moveComponent(String componentId) throws AdminException {
    adminController.moveComponentInst(getManagedSpaceId(), componentId, null, null);
  }

  private void pasteSpace(PasteDetail pasteDetail) throws JobStartPagePeasException {
    try {
      pasteDetail.setToSpaceId(getManagedSpaceId());
      String newSpaceId = adminController.copyAndPasteSpace(pasteDetail);
      if (StringUtil.isDefined(newSpaceId)) {
        if (StringUtil.isDefined(getManagedSpaceId())) {
          refreshCurrentSpaceCache();
        } else {
          m_NavBarMgr.addSpaceInCache(newSpaceId);
        }
      }
    } catch (Exception e) {
      throw new JobStartPagePeasException(
          "spaceId = " + pasteDetail.getFromSpaceId() + " in space " + getManagedSpaceId(), e);
    }
  }

  private void moveSpace(String spaceId) throws AdminException {
    moveSpace(spaceId, getManagedSpaceId());
  }

  public void moveSpace(String spaceId, String targetSpaceId) throws AdminException {
    adminController.moveSpace(spaceId, targetSpaceId);
  }

  public int getCurrentSpaceMaintenanceState() {
    if (isAppInMaintenance()) {
      return JobStartPagePeasSessionController.MAINTENANCE_PLATFORM;
    }
    if (isSpaceInMaintenance(getManagedSpaceId())) {
      return JobStartPagePeasSessionController.MAINTENANCE_THISSPACE;
    }
    // check if a parent is is maintenance
    List<SpaceInstLight> spaces = getOrganisationController().getPathToSpace(getManagedSpaceId());
    for (SpaceInstLight space : spaces) {
      if (isSpaceInMaintenance(space.getId())) {
        return JobStartPagePeasSessionController.MAINTENANCE_ONEPARENT;
      }
    }
    return JobStartPagePeasSessionController.MAINTENANCE_OFF;
  }

  public void setScope(int scope) {
    this.scope = scope;
  }

  public int getScope() {
    return scope;
  }

  /**
   * Return the silverpeas template linked to JobStartPage module
   *
   * @return a SilverpeasTemplate
   */
  public SilverpeasTemplate getSilverpeasTemplate() {
    Properties configuration = new Properties(templateConfiguration);
    SilverpeasTemplate template = SilverpeasTemplates.createSilverpeasTemplate(configuration);
    template.setAttribute("communityEnabled", isCommunityEnabled());
    return template;
  }

  private LocalizedParameter createIsHiddenParam(WAComponent descriptor, ComponentInst component) {
    String isHidden = "no";
    if (component != null && component.isHidden()) {
      isHidden = "yes";
    }
    Parameter hiddenParam = new Parameter();
    hiddenParam.setName("HiddenComponent");
    hiddenParam.setOrder(-5);
    hiddenParam.setMandatory(false);
    hiddenParam.setUpdatable("always");
    hiddenParam.setType(ParameterInputType.checkbox.toString());
    hiddenParam.setValue(isHidden);
    hiddenParam.putLabel(getLanguage(), getString("JSPP.hiddenComponent"));
    hiddenParam.putHelp(getLanguage(), null);
    Warning warning = new Warning();
    warning.putMessage(getLanguage(), getString("Warning.hiddenComponent"));
    hiddenParam.setWarning(warning);
    return new LocalizedParameter(descriptor, hiddenParam, getLanguage());
  }

  private LocalizedParameter createIsPublicParam(final WAComponent descriptor, ComponentInst component) {
    String isPublic = "no";
    if ((component == null && descriptor.isPublicByDefault()) ||
        (component != null && component.isPublic())) {
      isPublic = "yes";
    }
    Parameter publicParam = new Parameter();
    publicParam.setName("PublicComponent");
    publicParam.setOrder(-6);
    publicParam.setMandatory(false);
    publicParam.setUpdatable("always");
    publicParam.setType(ParameterInputType.checkbox.toString());
    publicParam.setValue(isPublic);
    publicParam.putLabel(getLanguage(), getString("JSPP.publicComponent"));
    publicParam.putHelp(getLanguage(), null);
    Warning warning = new Warning();
    warning.putMessage(getLanguage(), getString("Warning.publicComponent"));
    publicParam.setWarning(warning);
    return new LocalizedParameter(descriptor, publicParam, getLanguage());
  }

  public boolean isCommunityEnabled() {
    return communityFactory != null && getSettings().getBoolean("UseCommunitySpace", false);
  }

  private CommunityFactory getCommunityFactory() {
    try {
      return ServiceProvider.getService(CommunityFactory.class);
    } catch (Exception e) {
      return null;
    }
  }
}
