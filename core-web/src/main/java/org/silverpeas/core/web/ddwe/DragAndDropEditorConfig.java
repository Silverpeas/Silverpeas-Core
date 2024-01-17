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

package org.silverpeas.core.web.ddwe;

import org.silverpeas.core.wbe.WbeEdition;
import org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBarElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.silverpeas.core.web.ddwe.DragAndDropMode.MAIL;
import static org.silverpeas.core.web.ddwe.DragAndDropMode.WEB;

/**
 * Permits to set a specific configuration.
 * @author silveryocha
 */
public class DragAndDropEditorConfig {

  private static final String WBE_CONFIG_KEY = "WBE_CONFIG";

  private final String validateUrl;
  private final String cancelUrl;
  private final List<BrowseBarElement> manualBrowseBarElements = new ArrayList<>();
  private DragAndDropMode mode = WEB;

  private DragAndDropEditorConfig(final String validateUrl, final String cancelUrl) {
    this.validateUrl = validateUrl;
    this.cancelUrl = cancelUrl;
  }

  /**
   * Initialization of the editor of a Drag And Drop Edition.
   * @param validateUrl the URL to perform when validating the data.
   * @param cancelUrl the URL to perform when cancelling the modifications.
   * @return a {@link Builder} instance.
   */
  public static Builder withConnectors(final String validateUrl, final String cancelUrl) {
    return new Builder(validateUrl, cancelUrl);
  }

  /**
   * Gets from WBE edition configuration the Drag And Drop editor configuration.
   * @param wbeConfiguration {@link WbeEdition.Configuration} instance.
   * @return an optional {@link DragAndDropEditorConfig} instance.
   */
  public static Optional<DragAndDropEditorConfig> getFrom(
      final WbeEdition.Configuration wbeConfiguration) {
    return wbeConfiguration.get(WBE_CONFIG_KEY)
        .filter(DragAndDropEditorConfig.class::isInstance)
        .map(DragAndDropEditorConfig.class::cast);
  }

  public String getValidateUrl() {
    return validateUrl;
  }

  public String getCancelUrl() {
    return cancelUrl;
  }

  public List<BrowseBarElement> getManualBrowseBarElements() {
    return manualBrowseBarElements;
  }

  public DragAndDropMode getMode() {
    return mode;
  }

  /**
   * Registers into {@link WbeEdition.Configuration} the {@link DragAndDropEditorConfig}.
   * @param wbeConfiguration a {@link WbeEdition.Configuration} instance.
   */
  public void applyTo(final WbeEdition.Configuration wbeConfiguration) {
    wbeConfiguration.put(WBE_CONFIG_KEY, this);
  }

  /**
   * Builder dedicated to {@link DragAndDropEditorConfig} instantiation.
   */
  public static class Builder {
    private final DragAndDropEditorConfig config;

    private Builder(final String validateUrl, final String cancelUrl) {
      config = new DragAndDropEditorConfig(validateUrl, cancelUrl);
    }

    /**
     * Adds manually a browse bar element into browse bar.
     * <p>When the method used, default browse bar construction is ignored and given elements are
     * added after space and component instance into the browse bar.</p>
     * @param element a {@link BrowseBarElement} instance.
     * @return the builder instance itself.
     */
    public Builder addBrowseBarElement(final BrowseBarElement element) {
      config.manualBrowseBarElements.add(element);
      return this;
    }

    /**
     * Sets the editing into a mode which facilitates mail content creation.
     * @return the builder instance itself.
     */
    public Builder setMailMode() {
      config.mode = MAIL;
      return this;
    }

    /**
     * Builds the configuration.
     * @return a {@link DragAndDropEditorConfig} instance.
     */
    public DragAndDropEditorConfig build() {
      return config;
    }
  }
}
