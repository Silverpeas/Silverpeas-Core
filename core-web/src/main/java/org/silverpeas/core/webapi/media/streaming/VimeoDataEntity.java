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
package org.silverpeas.core.webapi.media.streaming;

import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.io.media.Definition;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.media.streaming.StreamingProvider;
import org.silverpeas.core.webapi.media.MediaDefinitionEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author silveryocha
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class VimeoDataEntity extends StreamingProviderDataEntity {
  private static final long serialVersionUID = 724049725696379973L;

  public static final String NAME = "vimeo";

  /**
   * Default hidden constructor.
   */
  VimeoDataEntity(final StreamingProvider streamingProvider,
      final OembedDataEntity oembedVimeoData) {
    super(streamingProvider, oembedVimeoData);

    // As a specific way, vimeo is supplying additional information about the video streaming
    // duration.
    if (StringUtil.isInteger(oembedVimeoData.getDuration())) {
      setFormattedDurationHMS(
          UnitUtil.getDuration(Long.valueOf(oembedVimeoData.getDuration()), TimeUnit.SECOND)
              .getFormattedDurationAsHMS());
    }

    // As the duration, the width and height supplied are those of the video and not
    // those of the streaming player.
    String width = oembedVimeoData.getWidth();
    String height = oembedVimeoData.getHeight();
    if (StringUtil.isInteger(width) && StringUtil.isInteger(height)) {
      setDefinition(MediaDefinitionEntity
          .createFrom(Definition.of(Integer.valueOf(width), Integer.valueOf(height))));
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  protected VimeoDataEntity() {
    super();
  }
}
