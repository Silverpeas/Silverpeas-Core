/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.media.image;

/**
 * @author Yohann Chastagnier
 */
public enum ImageInfoType {
  /** Orientation Tag */
  ORIENTATION("%[orientation]"),
  /** file size of image read in */
  FILE_SIZE("%b"),
  /** comment meta-data property */
  METADATA_COMMENT("%c"),
  /** directory component of path */
  PATH_DIRECTORY("%d"),
  /** filename extension or suffix */
  FILE_EXTENSION("%e"),
  /** filename (including suffix) */
  FILENAME("%f"),
  /** layer canvas page geometry (equivalent to "%Wx%H%X%Y") */
  CANVAS_PAGE_GEOMETRY("%g"),
  /** current image height in pixels */
  HEIGHT_IN_PIXEL("%h"),
  /** CALCULATED: number of unique colors */
  NB_UNIQUE_COLORS("%k"),
  /** label meta-data property */
  METADATA_LABEL("%l"),
  /** image file format (file magic) */
  FILE_FORMAT("%m"),
  /** number of images in current image sequence */
  NB_IMAGES_IN_SEQUENCE("%n"),
  /** image class and colorspace */
  CLASS_AND_COLOR_SPACE("%r"),
  /** scene number (from input unless re-assigned) */
  SCENE_NUMBER("%s"),
  /** filename without directory or extension (suffix) */
  FILENAME_ONLY("%t"),
  /** current width in pixels */
  WIDTH_IN_PIXEL("%w"),
  /** x resolution (density) */
  DENSITY_WIDTH("%x"),
  /** y resolution (density) */
  DENSITY_HEIGHT("%y"),
  /** image depth (as read in unless modified, image save depth) */
  DEPTH("%z"),
  /** image transparency channel enabled (true/false) */
  IS_TRANSPARENCY("%A"),
  /** image compression type */
  COMPRESSION_TYPE("%C"),
  /** image GIF dispose method */
  GIF_DISPOSE_METHOD("%D"),
  /** page (canvas) height */
  CANVAS_PAGE_HEIGHT("%H"),
  /** page (canvas) offset ( = %X%Y ) */
  CANVAS_PAGE_OFFSET("%O"),
  /** page (canvas) size ( = %Wx%H ) */
  CANVAS_PAGE_SIZE("%P"),
  /** image compression quality ( 0 = default ) */
  COMPRESSION_QUALITY_LEVEL("%Q"),
  /** image time delay (in centi-seconds) */
  TIME_DELAY("%T"),
  /** image resolution units */
  DENSITY_UNIT("%U"),
  /** page (canvas) width */
  CANVAS_PAGE_WIDTH("%W"),
  /** page (canvas) x offset (including sign) */
  CANVAS_PAGE_WIDTH_OFFSET("%X"),
  /** page (canvas) y offset (including sign) */
  CANVAS_PAGE_HEIGHT_OFFSET("%Y"),
  /** CALCULATED: trim bounding box (without actually trimming) */
  TRIM_BOUNDING_MODE("%@"),
  /** CALCULATED: 'signature' hash of image values */
  HASH("%#");

  private final String imOption;

  ImageInfoType(final String imOption) {
    this.imOption = imOption;
  }

  public String getImOption() {
    return imOption;
  }
}
