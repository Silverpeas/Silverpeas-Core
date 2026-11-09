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
package org.silverpeas.core.security;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The common cryptographically secure generator for passwords and sharing codes.
 * Policy adapters describe constraints; all random selection and shuffling happen here.
 */
public final class SecretGenerator {
  public static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
  public static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  public static final String DIGITS = "0123456789";
  private static final String SHARING_SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{}|;:'\",.<>?/\\~`";
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int MAX_ATTEMPTS = 256;
  private static final int MAX_SECRET_LENGTH = 1024;
  private static final int MAX_ALPHABET_LENGTH = 4096;

  private SecretGenerator() {
    // Utility class.
  }

  /** Allowed length range, including both limits. */
  public record Lengths(int minimum, int maximum) {
  }

  /** An alphabet and its required or combined minimum number of occurrences. */
  public record CharacterGroup(String alphabet, int count, boolean required, boolean combined) {
    public CharacterGroup {
      Objects.requireNonNull(alphabet);
      if (count < 0 || count > MAX_SECRET_LENGTH || alphabet.length() > MAX_ALPHABET_LENGTH ||
          (count > 0 && alphabet.isEmpty() && (required || combined))) {
        throw new IllegalStateException("Invalid secret character group");
      }
    }
  }

  /** Immutable generation policy, including the application's final validator. */
  public record Policy(Lengths lengths, List<CharacterGroup> groups, int combinedCount,
                       Predicate<String> validator) {
    public Policy {
      Objects.requireNonNull(lengths);
      groups = List.copyOf(groups);
      if (groups.size() > 64 || combinedCount < 0 || combinedCount > groups.size()) {
        throw new IllegalStateException("Invalid secret character group combination");
      }
      Objects.requireNonNull(validator);
      if (lengths.minimum() < 1 || lengths.maximum() < lengths.minimum() ||
          lengths.minimum() > MAX_SECRET_LENGTH) {
        throw new IllegalStateException("Inconsistent secret length constraints");
      }
    }
  }

  /**
   * Generates a secret accepted by the policy, or fails after a bounded number of attempts.
   * @param policy the character, length and validation constraints.
   * @return a new secret satisfying the policy.
   * @throws IllegalStateException if no satisfying secret can be generated.
   */
  public static String generate(Policy policy) {
    final String alphabet = policy.groups().stream().map(CharacterGroup::alphabet)
        .collect(Collectors.joining());
    if (alphabet.isEmpty() || alphabet.length() > MAX_ALPHABET_LENGTH) {
      throw new IllegalStateException("No characters available for secret generation");
    }
    long requiredSize = policy.groups().stream().filter(CharacterGroup::required)
        .mapToLong(CharacterGroup::count).sum();
    if (requiredSize > policy.lengths().maximum()) {
      throw new IllegalStateException("Required characters exceed maximum secret length");
    }
    for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
      String candidate = candidate(policy, alphabet);
      if (candidate != null && policy.validator().test(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Unable to generate a secret satisfying the configured policy");
  }

  /**
   * Generates a 32-character sharing code with all four character families.
   * @return a new sharing code, independently of the platform password policy.
   */
  public static String sharingCode() {
    return generate(new Policy(new Lengths(32, 32), List.of(
        new CharacterGroup(LOWERCASE, 1, true, false),
        new CharacterGroup(UPPERCASE, 1, true, false),
        new CharacterGroup(DIGITS, 1, true, false),
        new CharacterGroup(SHARING_SPECIAL_CHARACTERS, 1, true, false)), 0, value -> true));
  }

  private static String candidate(Policy policy, String alphabet) {
    final var selected = policy.groups().stream().filter(CharacterGroup::required)
        .collect(Collectors.toCollection(LinkedHashSet::new));
    final var combined = new ArrayList<>(policy.groups().stream()
        .filter(CharacterGroup::combined).toList());
    Collections.shuffle(combined, RANDOM);
    long alreadySelected = combined.stream().filter(selected::contains).count();
    combined.stream().filter(group -> !selected.contains(group))
        .limit(Math.max(0L, policy.combinedCount() - alreadySelected)).forEach(selected::add);
    long minimumSize = selected.stream().mapToLong(CharacterGroup::count).sum();
    if (minimumSize > policy.lengths().maximum()) {
      return null;
    }
    final StringBuilder result = new StringBuilder();
    selected.forEach(group -> appendRandom(result, group.alphabet(), group.count()));
    int length = Math.max(policy.lengths().minimum(), result.length());
    if (length > MAX_SECRET_LENGTH) {
      throw new IllegalStateException("Generated secret exceeds safe length limit");
    }
    appendRandom(result, alphabet, length - result.length());
    for (int i = result.length() - 1; i > 0; i--) {
      int j = RANDOM.nextInt(i + 1);
      char value = result.charAt(i);
      result.setCharAt(i, result.charAt(j));
      result.setCharAt(j, value);
    }
    return result.toString();
  }

  private static void appendRandom(StringBuilder target, String alphabet, int count) {
    for (int i = 0; i < count; i++) {
      target.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
    }
  }
}
