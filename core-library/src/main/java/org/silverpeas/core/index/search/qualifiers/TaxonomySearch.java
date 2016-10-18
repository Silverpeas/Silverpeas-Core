package org.silverpeas.core.index.search.qualifiers;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Nicolas Eysseric
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface TaxonomySearch {}
