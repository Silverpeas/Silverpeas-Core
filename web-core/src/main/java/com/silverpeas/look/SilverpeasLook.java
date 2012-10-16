/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.look;

import java.util.Collection;
import java.util.List;
import com.stratelia.webactiv.util.FileServerUtils;
import com.silverpeas.util.FileUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import java.io.File;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import static com.stratelia.webactiv.beans.admin.Admin.*;
import static com.silverpeas.util.StringUtil.*;

/**
 * It is a singleton that represents the current look of the running Silverpeas. Its single object
 * provides an access to the different look of the widgets that compound Silverpeas.
 */
public class SilverpeasLook {

  /**
   * The property in which is defined the URL of the default wallpaper for the Silverpeas
   * application. If this property is not set, then the
   * SILVERPEAS_CONTEXT/admin/jsp/icons/silverpeasV5/bandeauTop.jpg is taken as the URL of the
   * default wallpaper, with SILVERPEAS_CONTEXT the web context at which Silverpeas is deployed.
   */
  public static final String DEFAULT_WALLPAPER_PROPERTY = "wallPaper";
  private static SilverpeasLook look = new SilverpeasLook();
  @Inject
  private OrganizationController organizationController;

  /**
   * Gets the look of Silverpeas.
   * @return an instance representing the current look of the Silverpeas application.
   */
  public static SilverpeasLook getSilverpeasLook() {
    return look;
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
    String wallpaperURL = null;
    List<SpaceInst> path = organizationController.getSpacePath(spaceId);
    for (int i = path.size() - 1; i >= 0; i--) {
      SpaceInst space = path.get(i);
      wallpaperURL = getWallPaperURL(space.getId());
      if (isDefined(wallpaperURL)) {
        break;
      }
    }
    return wallpaperURL;
  }

  /**
   * Gets the wallpaper of the specified space. If the space or one of its parent have no specific
   * wallpaper set, then returns the default one.
   * @see SilverpeasLook#getWallpaperOfSpace(java.lang.String)
   * @param spaceId the identifier of the space.
   * @return the URL of the wallpaper image or the default one if the space or its parents have no
   * wallpaper.
   */
  public String getWallpaperOfSpaceOrDefaultOne(String spaceId) {
    String wallpaperURL = getWallpaperOfSpace(spaceId);
    if (!isDefined(wallpaperURL)) {
      GraphicElementFactory elementFactory = new GraphicElementFactory(
          GraphicElementFactory.defaultLookName);
      wallpaperURL = elementFactory.getIcon(DEFAULT_WALLPAPER_PROPERTY);
      if (!isDefined(wallpaperURL)) {
        wallpaperURL = FileServerUtils.getApplicationContext()
            + "/admin/jsp/icons/silverpeasV5/bandeauTop.jpg";
      }
    }
    return wallpaperURL;
  }

  private String getWallPaperURL(String spaceId) {
    String wallpaperURL = null;
    String id = spaceId;
    if (id.startsWith(SPACE_KEY_PREFIX)) {
      id = id.substring(2);
    }
    String basePath =
        FileRepositoryManager.getAbsolutePath("Space" + id, new String[] { "look" });
    File dir = new File(basePath);
    if (dir.exists() && dir.isDirectory()) {
      Collection<File> wallpapers = FileUtils.listFiles(dir, FileFilterUtils.prefixFileFilter(
          "wallPaper", IOCase.INSENSITIVE), null);
      for (File wallpaper : wallpapers) {
        if (wallpaper.isFile() && FileUtil.isImage(wallpaper.getName())) {
          wallpaperURL = FileServerUtils.getOnlineURL(
              "Space" + id, wallpaper.getName(),
              wallpaper.getName(),
              FileUtil.getMimeType(wallpaper.getName()), "look");
          break;
        }
      }
    }
    return wallpaperURL;
  }

  private SilverpeasLook() {
  }
}
