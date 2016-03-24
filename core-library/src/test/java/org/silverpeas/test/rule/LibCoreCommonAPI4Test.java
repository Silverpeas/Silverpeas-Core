package org.silverpeas.core.test.rule;

import org.silverpeas.cache.service.CacheServiceProvider;
import org.silverpeas.core.test.rule.CommonAPI4Test;

/**
 * @author Yohann Chastagnier
 */
public class LibCoreCommonAPI4Test extends CommonAPI4Test {

  @Override
  protected void beforeEvaluate(final TestContext context) {
    super.beforeEvaluate(context);
    clearCacheData();
  }

  private void clearCacheData() {
    CacheServiceProvider.getRequestCacheService().clear();
    CacheServiceProvider.getThreadCacheService().clear();
  }
}
