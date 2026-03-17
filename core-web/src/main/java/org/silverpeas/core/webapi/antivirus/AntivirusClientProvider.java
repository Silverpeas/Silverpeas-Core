/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.webapi.antivirus;

import org.silverpeas.core.annotation.Provider;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Optional;

/**
 * Provides an instance of {@link AntivirusClient} based on the application settings.
 * <p>
 * The provider checks if antivirus functionality is enabled and returns the client
 * matching the configured name. If antivirus is disabled or no matching client is found,
 * an empty {@link Optional} is returned.
 */
@Provider
public class AntivirusClientProvider {
    @Inject
    private Instance<AntivirusClient> antivirus;

    public Optional<AntivirusClient> getAntivirusClient() {
        SettingBundle settings =
                ResourceLocator.getSettingBundle("org.silverpeas.util.attachment.Antivirus");
        if (!settings.getBoolean("antivirus.enable", false)) return Optional.empty();
        String antivirusName = settings.getString("antivirus.client", "clamav");

        return antivirus.stream()
                .filter(a -> a.getName().equalsIgnoreCase(antivirusName))
                .findFirst();
    }
}
