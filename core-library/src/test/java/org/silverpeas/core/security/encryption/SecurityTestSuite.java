package org.silverpeas.core.security.encryption;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to sequence the unit tests on security tools.
 * As each unit tests works on the same static data, it is required to sequence them so
 * that they work on the memory each of their turn.
 * @author Yohann Chastagnier
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestConcurrentExecution.class,
    TestContentEncryption.class,
    TestKeyManagement.class
})
public class SecurityTestSuite {}
