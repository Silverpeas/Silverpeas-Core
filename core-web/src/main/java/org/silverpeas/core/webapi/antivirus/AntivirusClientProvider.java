package org.silverpeas.core.webapi.antivirus;

import org.silverpeas.core.annotation.Provider;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Optional;

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
