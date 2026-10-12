package org.silverpeas.core.webapi.sharing;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Limits failed verification code attempts across all shared REST resources.
 * State is local to the application JVM; clustered installations require shared storage.
 */
public final class SharingCodeVerifier {

  private static final String SETTINGS =
      "org.silverpeas.sharing.settings.sharingSecurity";
  private static final ConcurrentMap<String, Attempts> ATTEMPTS = new ConcurrentHashMap<>();

  private SharingCodeVerifier() {
  }

  public static boolean verify(Ticket ticket, String code) {
    return verify(ticket, code, Clock.systemUTC().millis());
  }

  static boolean verify(Ticket ticket, String code, long now) {
    if (ticket == null || !ticket.isValid()) {
      return false;
    }
    if (ticket.getSecurityCode() == null || ticket.getSecurityCode().isEmpty()) {
      return true;
    }
    SettingBundle settings = ResourceLocator.getSettingBundle(SETTINGS);
    int maximum = Math.max(1, settings.getInteger("sharing.securityCode.maxAttempts", 5));
    long window = TimeUnit.MINUTES.toMillis(
        Math.max(1, settings.getInteger("sharing.securityCode.attemptWindow", 15)));
    long duration = TimeUnit.MINUTES.toMillis(
        Math.max(1, settings.getInteger("sharing.securityCode.lockDuration", 15)));

    Attempts attempts = ATTEMPTS.computeIfAbsent(ticket.getToken(), ignored -> new Attempts());
    synchronized (attempts) {
      if (attempts.lockedUntil > now) {
        throw new WebApplicationException(Response.Status.TOO_MANY_REQUESTS);
      }
      attempts.failures.removeIf(time -> time <= now - window);
      if (attempts.lockedUntil != 0 && attempts.lockedUntil <= now) {
        attempts.failures.clear();
        attempts.lockedUntil = 0;
      }
      if (ticket.checkSecurityCode(code)) {
        attempts.failures.clear();
        ATTEMPTS.remove(ticket.getToken(), attempts);
        return true;
      }
      attempts.failures.addLast(now);
      if (attempts.failures.size() >= maximum) {
        attempts.lockedUntil = now + duration;
        throw new WebApplicationException(Response.Status.TOO_MANY_REQUESTS);
      }
      return false;
    }
  }

  private static final class Attempts {
    private final Deque<Long> failures = new ArrayDeque<>();
    private long lockedUntil;
  }
}
