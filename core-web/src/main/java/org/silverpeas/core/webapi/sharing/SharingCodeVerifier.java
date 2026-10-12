package org.silverpeas.core.webapi.sharing;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.silverpeas.core.sharing.model.Ticket;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicInteger;
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
  private static final AtomicInteger REQUEST_COUNT = new AtomicInteger();
  private static final int CLEANUP_INTERVAL = 256;

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

    return verify(ticket, code, now, maximum, window, duration);
  }

  static boolean verify(Ticket ticket, String code, long now, int maximum, long window,
      long duration) {
    if (ticket == null || !ticket.isValid()) {
      return false;
    }
    if (ticket.getSecurityCode() == null || ticket.getSecurityCode().isEmpty()) {
      return true;
    }
    if (REQUEST_COUNT.incrementAndGet() % CLEANUP_INTERVAL == 0) {
      cleanup(now, window);
    }
    final boolean[] accepted = {false};
    final boolean[] locked = {false};
    ATTEMPTS.compute(ticket.getToken(), (token, attempts) -> {
      if (attempts == null) {
        attempts = new Attempts();
      }
      if (attempts.lockedUntil > now) {
        locked[0] = true;
        return attempts;
      }
      if (attempts.lockedUntil != 0) {
        attempts.failures.clear();
        attempts.lockedUntil = 0;
      }
      attempts.failures.removeIf(time -> time <= now - window);
      if (ticket.checkSecurityCode(code)) {
        accepted[0] = true;
        return null;
      }
      attempts.failures.addLast(now);
      if (attempts.failures.size() >= maximum) {
        attempts.lockedUntil = now + duration;
        locked[0] = true;
      }
      return attempts;
    });
    if (locked[0]) {
      throw new WebApplicationException(Response.Status.TOO_MANY_REQUESTS);
    }
    return accepted[0];
  }

  static void cleanup(long now, long window) {
    // computeIfPresent serializes cleanup with verification for each token.
    ATTEMPTS.forEach((token, ignored) -> ATTEMPTS.computeIfPresent(token, (key, attempts) -> {
      if (attempts.lockedUntil > now) {
        return attempts;
      }
      if (attempts.lockedUntil != 0) {
        return null;
      }
      attempts.failures.removeIf(time -> time <= now - window);
      return attempts.failures.isEmpty() ? null : attempts;
    }));
  }

  static int trackedTickets() {
    return ATTEMPTS.size();
  }

  private static final class Attempts {
    private final Deque<Long> failures = new ArrayDeque<>();
    private long lockedUntil;
  }
}
