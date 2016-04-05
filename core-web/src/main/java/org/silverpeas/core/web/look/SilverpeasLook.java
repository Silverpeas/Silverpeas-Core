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

package org.silverpeas.core.web.look;

import org.silverpeas.core.admin.space.SpaceInst;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * It is a singleton that represents the current look of the running Silverpeas. Its single object
 * provides an access to the different look of the widgets that compound Silverpeas.
 */
@Singleton
public class SilverpeasLook {

  /**
   * The property in which is defined the URL of the default wallpaper for the Silverpeas
   * application. If this property is not set, then the
   * SILVERPEAS_CONTEXT/admin/jsp/icons/silverpeasV5/bandeauTop.jpg is taken as the URL of the
   * default wallpaper, with SILVERPEAS_CONTEXT the web context at which Silverpeas is deployed.
   */
  public static final String DEFAULT_WALLPAPER_PROPERTY = "wallPaper";
  public static final String SPACE_CSS = "styles";
  @Inject
  private OrganizationController organizationController;

  /**
   * Gets the look of Silverpeas.
   * @return an instance representing the current look of the Silverpeas application.
   */
  public static SilverpeasLook getSilverpeasLook() {
    return ServiceProvider.getService(SilverpeasLook.class);
  }

  /**
   * Is the specified space has a specific wallpaper?
   * @param spaceId the unique identifier of the space. It shouldn't be null.
   * @return true if a wallpaper is set specifically for the space, false otherwise.
   */
  public boolean hasSpaceWallpaper(String spaceId) {
    return isDefined(getWallPaperURL(spaceId));
  }

  /**
   * Gets the wallpaper of the specified space. The wallpaper of a space is either the path of an
   * image specifically set for itself or, in the case it has no specific wallpaper set, the one of
   * its closest parent space.
   * @param spaceId the unique identifier of the space. The spaceId shouldn't be null.
   * @return the URL of the wallpaper image or null if both no wallpaper is set for the specified
   * space and for any of its parent spaces.
   */
  public String getWallpaperOfSpace(String spaceId) {
    List<SpaceInst> path = organizationController.getSpacePath(spaceId);
    for (int i = path.size() - 1; i >= 0; i--) {
      SpaceInst space = path.get(i);
      String wallpaperURL = getWallPaperURL(space.getId());
      if (isDefined(wallpaperURL)) {
        return wallpaperURL;
      }
    }
    return null;
  }

  /**
   * Gets the wallpaper of the specified space. If the space or one of its parent have no specific
   * wallpaper set, then returns the default one.
   * @param spaceId the identifier of the space.
   * @return the URL of the wallpaper image or the default one if the space or its parents have no
   * wallpaper.
   * @see SilverpeasLook#getWallpaperOfSpace(java.lang.String)
   */
  public String getWallpaperOfSpaceOrDefaultOne(String spaceId) {
    String wallpaperURL = getWallpaperOfSpace(spaceId);
    if (!isDefined(wallpaperURL)) {
      GraphicElementFactory elementFactory =
          new GraphicElementFactory(GraphicElementFactory.defaultLookName);
      wallpaperURL = elementFactory.getIcon(DEFAULT_WALLPAPER_PROPERTY);
      if (!isDefined(wallpaperURL)) {
        wallpaperURL = FileServerUtils.getApplicationContext() +
            "/admin/jsp/icons/silverpeasV5/bandeauTop.jpg";
      }
    }
    return wallpaperURL;
  }

  private String getWallPaperURL(String spaceId) {
    String id = getShortSpaceId(spaceId);
    String basePath = getSpaceBasePath(id);
    File dir = new File(basePath);
    if (dir.exists() && dir.isDirectory()) {
      Collection<File> wallpapers = FileUtils
          .listFiles(dir, FileFilterUtils.prefixFileFilter("wallPaper", IOCase.INSENSITIVE), null);
      for (File wallpaper : wallpapers) {
        if (wallpaper.isFile() && FileUtil.isImage(wallpaper.getName())) {
          return getURLOfElement(id, wallpaper.getName());
        }
      }
    }
    return null;
  }

  /**
   * return the first space id with a specific CSS in path of given space
   * This space can be the given space itself or one of its parents
   * @param spaceId
   * @return the first space id (from given space to root) with a specific CSS. If no space
   * in path have got specific CSS, returns null.
   */
  public String getSpaceWithCSS(String spaceId) {
    List<SpaceInst> path = organizationController.getSpacePath(spaceId);
    Collections.reverse(path);
    for (SpaceInst space : path) {
      String url = getSpaceCSSURL(space.getId());
      if (isDefined(url)) {
        return getShortSpaceId(space.getId());
      }
    }
    return null;
  }

  /**
   * return the CSS URL of space with a specific CSS. This space can be the given space itself or
   * one
   * of its parents. It is this URL which must be applied to given space.
   * @param spaceId
   * @return the CSS URL of first space (from given space to root) with a specific CSS. If no space
   * in path have got specific CSS, returns null.
   */
  public String getCSSOfSpace(String spaceId) {
    List<SpaceInst> path = organizationController.getSpacePath(spaceId);
    Collections.reverse(path);
    for (SpaceInst space : path) {
      String url = getSpaceCSSURL(space.getId());
      if (isDefined(url)) {
        return url;
      }
    }
    return null;
  }

  public String getCSSOfSpaceLook(String spaceId) {
    List<SpaceInst> path = organizationController.getSpacePath(spaceId);
    Collections.reverse(path);
    String cssURL = null;
    for (SpaceInst space : path) {
      if (StringUtil.isDefined(space.getLook())) {
        cssURL = GraphicElementFactory.getCSSOfLook(space.getLook());
      }
      if (StringUtil.isDefined(cssURL)) {
        break;
      }
    }
    return cssURL;
  }

  public String getSpaceLook(String spaceId) {
    List<SpaceInst> path = organizationController.getSpacePath(spaceId);
    Collections.reverse(path);
    String spaceLook = null;
    for (SpaceInst space : path) {
      spaceLook = space.getLook();
      if (StringUtil.isDefined(space.getLook())) {
        break;
      }
    }
    return spaceLook;
  }

  private String getSpaceCSSURL(String spaceId) {
    String id = getShortSpaceId(spaceId);
    File dir = new File(getSpaceBasePath(id));
    if (dir.exists() && dir.isDirectory()) {
      String filename = SPACE_CSS + ".css";
      File css = new File(dir, filename);
      if (css != null && css.exists()) {
        return getURLOfElement(id, filename);
      }
    }
    return null;
  }

  public String getSpaceBasePath(String spaceId) {
    return FileRepositoryManager.getAbsolutePath("Space" + spaceId, new String[]{"look"});
  }

  private String getURLOfElement(String spaceId, String filename) {
    return FileServerUtils
        .getOnlineURL("Space" + spaceId, filename, filename, FileUtil.getMimeType(filename),
            "look");
  }

  private String getShortSpaceId(String spaceId) {
    if (spaceId.startsWith(SpaceInst.SPACE_KEY_PREFIX)) {
      return spaceId.substring(2);
    }
    return spaceId;
  }

  private SilverpeasLook() {
  }
}
