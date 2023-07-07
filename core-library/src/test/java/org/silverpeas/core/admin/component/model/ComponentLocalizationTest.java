/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.component.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.silverpeas.core.admin.component.WAComponentRegistry;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.test.extention.TestManagedBeans;

import java.util.MissingResourceException;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class of unit tests deals with the localized data of components described with XML
 * descriptors.
 * for each test, 3 locales are verified:
 * <ul>
 *   <li>FR locale is retrieved from XML component descriptor</li>
 *   <li>ES locale is retrieved from XML bundle property file</li>
 *   <li>IT locale does not exist, default language is used instead</li>
 * </ul>
 * @author silveryocha
 */
@EnableSilverTestEnv
@TestManagedBeans(PublicationTemplateManager.class)
class ComponentLocalizationTest {

  private static final String COMPONENT_NAME = "kmeliaForLocalizedTests";
  private static final String FR_LOCALE = "fr";
  private static final String ES_LOCALE = "es";
  private static final String IT_LOCALE = "it";
  private static final String ERR_TEMPLATE = "Can't find localization into xmlcomponents.%s" +
      "_%s.properties with key %s, or into %s XML descriptor";
  private WAComponent kmeliaWA;

  @TestManagedBean
  private WAComponentRegistry registry;

  @BeforeEach
  public void setup() throws Exception {
    registry.init();
    kmeliaWA = registry.getWAComponent(COMPONENT_NAME).orElse(null);
    assertThat(kmeliaWA, notNullValue());
  }

  @Test
  @DisplayName("Localized data from XML component descriptor if bundle property file does not exist")
  void noBundlePropertyFile() {
    final SilverpeasComponent classifiedsWA = registry.getWAComponent("classifieds").orElse(null);
    assertThat(classifiedsWA, notNullValue());
    assertThat(classifiedsWA.getLabel(FR_LOCALE), is("Petites annonces"));
    assertThat(classifiedsWA.getLabel(ES_LOCALE), is("Petites annonces"));
    assertThat(classifiedsWA.getLabel(IT_LOCALE), is("Petites annonces"));
  }

  @Test
  @DisplayName("Verifying that component label and description of SilverpeasComponent API " +
      "implementation use LocalizedComponent. For FR locale, localized data are retrieved from " +
      "the XML component descriptor whereas the ES one which are retrieved from bundle property " +
      "file")
  void labelAndDescriptionOfSilverpeasComponentApi() {
    assertThat(kmeliaWA.getLabel(FR_LOCALE), is("Gestion documentaire I"));
    assertThat(kmeliaWA.getLabel(ES_LOCALE), is("Gestión de documentos I"));
    assertThat(kmeliaWA.getLabel(IT_LOCALE), is("Gestion documentaire I"));
    assertThat(kmeliaWA.getDescription(FR_LOCALE),
        startsWith("Les publications offrent la possibilité"));
    assertThat(kmeliaWA.getDescription(ES_LOCALE),
        startsWith("Las publicaciones ofrecen la posibilidad"));
    assertThat(kmeliaWA.getDescription(IT_LOCALE),
        startsWith("Les publications offrent la possibilité"));
  }

  @Test
  @DisplayName("Verifying the LocalizedComponent implementation")
  void localizedWAComponent() {
    final LocalizedWAComponent frLocalized = getLocalizedWAComponent(FR_LOCALE);
    final LocalizedWAComponent esLocalized = getLocalizedWAComponent(ES_LOCALE);
    final LocalizedWAComponent itLocalized = getLocalizedWAComponent(IT_LOCALE);
    assertThat(frLocalized.getLabel(), is("Gestion documentaire I"));
    assertThat(esLocalized.getLabel(), is("Gestión de documentos I"));
    assertThat(itLocalized.getLabel(), is("Gestion documentaire I"));
    assertThat(frLocalized.getDescription(), startsWith("Les publications offrent la possibilité"));
    assertThat(esLocalized.getDescription(), startsWith("Las publicaciones ofrecen la posibilidad"));
    assertThat(itLocalized.getDescription(), startsWith("Les publications offrent la possibilité"));
    assertThat(frLocalized.getSuite(), is("01 Gestion Documentaire"));
    assertThat(esLocalized.getSuite(), is("01 Gestión de documentos"));
    assertThat(itLocalized.getSuite(), is("01 Gestion Documentaire"));
  }

  @Test
  @DisplayName("Verifying the LocalizedProfile implementation")
  void localizedProfile() {
    final LocalizedProfile frAdmin = getLocalizedWAComponent(FR_LOCALE).getProfile("admin");
    final LocalizedProfile esAdmin = getLocalizedWAComponent(ES_LOCALE).getProfile("admin");
    final LocalizedProfile itAdmin = getLocalizedWAComponent(IT_LOCALE).getProfile("admin");
    assertThat(frAdmin, notNullValue());
    assertThat(esAdmin, notNullValue());
    assertThat(itAdmin, notNullValue());
    assertThat(frAdmin.getLabel(), is("Gestionnaires"));
    assertThat(esAdmin.getLabel(), is("Gestores"));
    assertThat(itAdmin.getLabel(), is("Gestionnaires"));
    assertThat(frAdmin.getHelp(), startsWith("Les gestionnaires créent des dossiers"));
    assertThat(esAdmin.getHelp(), startsWith("Los gestores crean archivos"));
    assertThat(itAdmin.getHelp(), startsWith("Les gestionnaires créent des dossiers"));
  }

  @Test
  @DisplayName("Verifying the LocalizedProfile implementation when missing some translation into XML descriptor")
  void localizedProfileWhenMissingSomeTranslationsIntoXmlDescriptor() {
    final LocalizedProfile frAdmin = getLocalizedWAComponent(FR_LOCALE).getProfile("publisher");
    final LocalizedProfile esAdmin = getLocalizedWAComponent(ES_LOCALE).getProfile("publisher");
    final LocalizedProfile itAdmin = getLocalizedWAComponent(IT_LOCALE).getProfile("publisher");
    assertThat(frAdmin, notNullValue());
    assertThat(esAdmin, notNullValue());
    assertThat(itAdmin, notNullValue());
    assertThat(frAdmin.getLabel(), is("Herausgeber"));
    assertThat(esAdmin.getLabel(), is("Herausgeber"));
    assertThat(itAdmin.getLabel(), is("Herausgeber"));
    assertThat(frAdmin.getHelp(), startsWith("Publisher create publication and validate"));
    assertThat(esAdmin.getHelp(), startsWith("Publisher create publication and validate"));
    assertThat(itAdmin.getHelp(), startsWith("Publisher create publication and validate"));
  }

  private LocalizedWAComponent getLocalizedWAComponent(final String locale) {
    return new LocalizedWAComponent(kmeliaWA, locale);
  }

  @Test
  @DisplayName("Verifying the LocalizedGroupOfParameters implementation")
  void localizedGroupOfParameters() {
    final GroupOfParameters group = getFirstGroupOfParametersWithName();
    assertThat(group.getName(), is("folders"));
    final LocalizedGroupOfParameters frGroup = getLocalizedGroupOfParameters(group, FR_LOCALE);
    final LocalizedGroupOfParameters esGroup = getLocalizedGroupOfParameters(group, ES_LOCALE);
    final LocalizedGroupOfParameters itGroup = getLocalizedGroupOfParameters(group, IT_LOCALE);
    assertThat(frGroup.getLabel(), is("Dossiers"));
    assertThat(esGroup.getLabel(), is("Archivos"));
    assertThat(itGroup.getLabel(), is("Dossiers"));
    assertThat(frGroup.getDescription(), is("Ils vous permettent d'organiser facilement votre GED"));
    assertThat(esGroup.getDescription(), is("Le permiten organizar fácilmente su GED"));
    assertThat(itGroup.getDescription(), is("Ils vous permettent d'organiser facilement votre GED"));
  }

  @Test
  @DisplayName("Verifying the LocalizedParameter implementation from a LocalizedGroupOfParameters")
  void localizedParameterOfLocalizedGroupOfParameters() {
    final GroupOfParameters group = getFirstGroupOfParametersWithName();
    assertThat(group.getName(), is("folders"));
    final LocalizedParameter frParam = getFirstLocalizedParameterWithoutOptionAndWarning(
        getLocalizedGroupOfParameters(group, FR_LOCALE));
    final LocalizedParameter esParam = getFirstLocalizedParameterWithoutOptionAndWarning(
        getLocalizedGroupOfParameters(group, ES_LOCALE));
    final LocalizedParameter itParam = getFirstLocalizedParameterWithoutOptionAndWarning(
        getLocalizedGroupOfParameters(group, IT_LOCALE));
    assertThat(frParam.getLabel(), is("Gestion déléguée"));
    assertThat(esParam.getLabel(), is("Gestión delegada"));
    assertThat(itParam.getLabel(), is("Gestion déléguée"));
    assertThat(frParam.getHelp(), startsWith("La gestion des dossiers est déléguée aux publieurs et rédacteurs."));
    assertThat(esParam.getHelp(), startsWith("La gestión de los archivos se delega en los editores y redactores."));
    assertThat(itParam.getHelp(), startsWith("La gestion des dossiers est déléguée aux publieurs et rédacteurs."));
  }

  @Test
  @DisplayName("Verifying the LocalizedOption implementation of LocalizedParameter from a " +
      "LocalizedGroupOfParameters")
  void localizedOptionOfLocalizedParameterOfLocalizedGroupOfParameters() {
    final GroupOfParameters group = getFirstGroupOfParametersWithName();
    assertThat(group.getName(), is("folders"));
    final LocalizedOption frOption = getOptionOfFirstLocalizedParameterWithoutWarning(
        getLocalizedGroupOfParameters(group, FR_LOCALE));
    final LocalizedOption esOption = getOptionOfFirstLocalizedParameterWithoutWarning(
        getLocalizedGroupOfParameters(group, ES_LOCALE));
    final LocalizedOption itOption = getOptionOfFirstLocalizedParameterWithoutWarning(
        getLocalizedGroupOfParameters(group, IT_LOCALE));
    assertThat(frOption.getName(), is("Oui avec explorateur de dossiers"));
    assertThat(esOption.getName(), is("Sí con el explorador de carpetas"));
    assertThat(itOption.getName(), is("Oui avec explorateur de dossiers"));
  }

  @Test
  @DisplayName("Verifying the LocalizedWarning implementation of LocalizedParameter from a " +
      "LocalizedGroupOfParameters")
  void localizedWarningOfLocalizedParameterOfLocalizedGroupOfParameters() {
    final GroupOfParameters group = kmeliaWA.getSortedGroupsOfParameters().get(3);
    assertThat(group.getName(), is("sharing"));
    final LocalizedWarning frWarning = getWarningOfFirstLocalizedParameterHasAny(
        getLocalizedGroupOfParameters(group, FR_LOCALE));
    final LocalizedWarning esWarning = getWarningOfFirstLocalizedParameterHasAny(
        getLocalizedGroupOfParameters(group, ES_LOCALE));
    final LocalizedWarning itWarning = getWarningOfFirstLocalizedParameterHasAny(
        getLocalizedGroupOfParameters(group, IT_LOCALE));
    assertThat(frWarning.getValue(), startsWith("Attention! En sélectionnant cette option"));
    assertThat(esWarning.getValue(), startsWith("Atención Al seleccionar esta opción"));
    assertThat(itWarning.getValue(), startsWith("Attention! En sélectionnant cette option"));
  }

  private GroupOfParameters getFirstGroupOfParametersWithName() {
    return kmeliaWA.getSortedGroupsOfParameters()
        .stream()
        .filter(g -> !"noname".equals(g.getName()))
        .findFirst()
        .orElseGet(() -> fail(
            "A group of parameters having the attribute name filled should have been found"));
  }

  private LocalizedGroupOfParameters getLocalizedGroupOfParameters(final GroupOfParameters group,
      final String locale) {
    return new LocalizedGroupOfParameters(kmeliaWA, group, locale);
  }

  private LocalizedParameter getFirstLocalizedParameterWithoutOptionAndWarning(final LocalizedGroupOfParameters group) {
    final LocalizedParameterList parameters = group.getParameters();
    parameters.sort(comparing(LocalizedParameter::getOrder));
    return parameters.stream()
        .filter(p -> p.getOptions().isEmpty())
        .filter(p -> p.getWarning().isEmpty())
        .findFirst()
        .orElse(null);
  }

  private LocalizedOption getOptionOfFirstLocalizedParameterWithoutWarning(
      final LocalizedGroupOfParameters group) {
    final LocalizedParameterList parameters = group.getParameters();
    parameters.sort(comparing(LocalizedParameter::getOrder));
    return parameters.stream()
        .filter(p -> !p.getOptions().isEmpty())
        .filter(p -> p.getWarning().isEmpty())
        .flatMap(p -> p.getOptions().stream())
        .findFirst()
        .orElse(null);
  }

  private LocalizedWarning getWarningOfFirstLocalizedParameterHasAny(
      final LocalizedGroupOfParameters group) {
    final LocalizedParameterList parameters = group.getParameters();
    parameters.sort(comparing(LocalizedParameter::getOrder));
    return parameters.stream()
        .filter(p -> p.getWarning().isPresent())
        .flatMap(p -> p.getWarning().stream())
        .findFirst()
        .orElse(null);
  }

  @Test
  @DisplayName("Verifying the LocalizedGroupOfParameters implementation when no group name is set")
  void localizedGroupOfParametersHavingNoGroupName() {
    final GroupOfParameters group = getFirstGroupOfParametersWithoutName();
    assertThat(group.getName(), is("noname"));
    final LocalizedGroupOfParameters frGroup = getLocalizedGroupOfParameters(group, FR_LOCALE);
    final LocalizedGroupOfParameters esGroup = getLocalizedGroupOfParameters(group, ES_LOCALE);
    final LocalizedGroupOfParameters itGroup = getLocalizedGroupOfParameters(group, IT_LOCALE);
    assertThat(frGroup.getLabel(), is("Divers"));
    assertThat(esGroup.getLabel(), is("Miscelánea"));
    assertThat(itGroup.getLabel(), is("Divers"));
    assertThat(frGroup.getDescription(), is("Autres paramètres pouvant être tout aussi utiles..."));
    assertThat(esGroup.getDescription(), is("Otros parámetros que pueden ser igual de útiles..."));
    assertThat(itGroup.getDescription(), is("Autres paramètres pouvant être tout aussi utiles..."));
  }

  @Test
  @DisplayName("Verifying the LocalizedParameter implementation from a LocalizedGroupOfParameters having no group name")
  void localizedParameterOfLocalizedGroupOfParametersHavingNoGroupName() {
    final GroupOfParameters group = getFirstGroupOfParametersWithoutName();
    assertThat(group.getName(), is("noname"));
    final LocalizedParameter frParam = getFirstLocalizedParameterWithoutOptionAndWarning(
        getLocalizedGroupOfParameters(group, FR_LOCALE));
    final LocalizedParameter esParam = getFirstLocalizedParameterWithoutOptionAndWarning(
        getLocalizedGroupOfParameters(group, ES_LOCALE));
    final LocalizedParameter itParam = getFirstLocalizedParameterWithoutOptionAndWarning(
        getLocalizedGroupOfParameters(group, IT_LOCALE));
    assertThat(frParam.getLabel(), is("Orienté CMS"));
    assertThat(esParam.getLabel(), is("Orientación CMS"));
    assertThat(itParam.getLabel(), is("Orienté CMS"));
    assertThat(frParam.getHelp(), startsWith("Permet d'activer les fonctions liées à la gestion de contenu web"));
    assertThat(esParam.getHelp(), startsWith("Permite activar funciones relacionadas con la gestión de contenidos web"));
    assertThat(itParam.getHelp(), startsWith("Permet d'activer les fonctions liées à la gestion de contenu web"));
  }

  private GroupOfParameters getFirstGroupOfParametersWithoutName() {
    return kmeliaWA.getSortedGroupsOfParameters()
        .stream()
        .filter(g -> "noname".equals(g.getName()))
        .findFirst()
        .orElseGet(() -> fail(
            "A group of parameters having empty attribute name should have been found"));
  }

  @Test
  @DisplayName("Verifying the LocalizedParameter implementation which is not part of a group")
  void localizedParameterNotPartOfAGroup() {
    final LocalizedParameter frParam = getFirstLocalizedParameterNotPartOfAGroup(FR_LOCALE);
    final LocalizedParameter esParam = getFirstLocalizedParameterNotPartOfAGroup(ES_LOCALE);
    final LocalizedParameter itParam = getFirstLocalizedParameterNotPartOfAGroup(IT_LOCALE);
    assertThat(frParam.getLabel(), is("Nb dernières publi"));
    assertThat(esParam.getLabel(), is("Nº de últimas publicaciones"));
    assertThat(itParam.getLabel(), is("Nb dernières publi"));
    assertThat(frParam.getHelp(), startsWith("Permet de définir le nombre de dernières publications"));
    assertThat(esParam.getHelp(), startsWith("Permite definir el número de últimas publicaciones"));
    assertThat(itParam.getHelp(), startsWith("Permet de définir le nombre de dernières publications"));
  }

  @Test
  @DisplayName("Verifying the LocalizedOption implementation of LocalizedParameter which is not part of a group")
  void localizedOptionOfLocalizedParameterNotPartOfAGroup() {
    final LocalizedOption frOption = getOptionOfFirstLocalizedParameterNotPartOfAGroup(FR_LOCALE);
    final LocalizedOption esOption = getOptionOfFirstLocalizedParameterNotPartOfAGroup(ES_LOCALE);
    final LocalizedOption itOption = getOptionOfFirstLocalizedParameterNotPartOfAGroup(IT_LOCALE);
    assertThat(frOption.getName(), is("Une première option"));
    assertThat(esOption.getName(), is("Una primera opción"));
    assertThat(itOption.getName(), is("Une première option"));
  }

  @Test
  @DisplayName("Verifying the LocalizedWarning implementation of LocalizedParameter which is not part")
  void localizedWarningOfLocalizedParameterNotPartOfAGroup() {
    final LocalizedWarning frWarning = getWarningOfFirstLocalizedParameterNotPartOfAGroup(FR_LOCALE);
    final LocalizedWarning esWarning = getWarningOfFirstLocalizedParameterNotPartOfAGroup(ES_LOCALE);
    final LocalizedWarning itWarning = getWarningOfFirstLocalizedParameterNotPartOfAGroup(IT_LOCALE);
    assertThat(frWarning.getValue(), startsWith("Un avertissement bien nécessaire"));
    assertThat(esWarning.getValue(), startsWith("Una advertencia muy necesaria"));
    assertThat(itWarning.getValue(), startsWith("Un avertissement bien nécessaire"));
  }

  private LocalizedParameter getFirstLocalizedParameterNotPartOfAGroup(final String locale) {
    return kmeliaWA.getSortedParameters().stream()
        .filter(p -> p.getOptions().isEmpty())
        .filter(p -> p.getWarning().isEmpty())
        .map(p -> new LocalizedParameter(kmeliaWA, p, locale))
        .findFirst()
        .orElse(null);
  }

  private LocalizedOption getOptionOfFirstLocalizedParameterNotPartOfAGroup(final String locale) {
    return kmeliaWA.getSortedParameters().stream()
        .filter(p -> !p.getOptions().isEmpty())
        .map(p -> new LocalizedParameter(kmeliaWA, p, locale))
        .flatMap(l -> l.getOptions().stream())
        .findFirst()
        .orElse(null);
  }

  private LocalizedWarning getWarningOfFirstLocalizedParameterNotPartOfAGroup(final String locale) {
    return kmeliaWA.getSortedParameters().stream()
        .filter(p -> p.getWarning().isPresent())
        .map(p -> new LocalizedParameter(kmeliaWA, p, locale))
        .flatMap(l -> l.getWarning().stream())
        .findFirst()
        .orElse(null);
  }

  @Test
  @DisplayName("Verifying the LocalizedParameter implementation when it does not exists labels into XML descriptor")
  void localizedParameterWithoutXmlDescriptorLocalization() {
    final LocalizedParameter frParam = getLocalizedParameterWithoutXmlDescriptorLocalization(FR_LOCALE);
    final LocalizedParameter esParam = getLocalizedParameterWithoutXmlDescriptorLocalization(ES_LOCALE);
    final LocalizedParameter itParam = getLocalizedParameterWithoutXmlDescriptorLocalization(IT_LOCALE);
    assertThat(frParam.getLabel(), is("Libellés uniquement dans le fichier bundle"));
    assertThat(esParam.getLabel(), is("Etiquetas sólo en el archivo bundle"));
    assertThat(itParam.getLabel(), is("Libellés uniquement dans le fichier bundle"));
    assertThat(frParam.getHelp(), startsWith("Il est possible que les libellés soient"));
    assertThat(esParam.getHelp(), startsWith("Las etiquetas pueden ser"));
    assertThat(itParam.getHelp(), startsWith("Il est possible que les libellés soient"));
  }

  @Test
  @DisplayName("Verifying the LocalizedOption implementation of a LocalizedParameter when it does" +
      " not exists labels into XML descriptor")
  void localizedOptionOfLocalizedParameterWithoutXmlDescriptorLocalization() {
    final LocalizedOption frOption = getOptionOfLocalizedParameterWithoutXmlDescriptorLocalization(FR_LOCALE);
    final LocalizedOption esOption = getOptionOfLocalizedParameterWithoutXmlDescriptorLocalization(ES_LOCALE);
    final LocalizedOption itOption = getOptionOfLocalizedParameterWithoutXmlDescriptorLocalization(IT_LOCALE);
    assertThat(frOption.getName(), is("Une première option"));
    assertThat(esOption.getName(), is("Una primera opción"));
    assertThat(itOption.getName(), is("Une première option"));
  }

  @Test
  @DisplayName("Verifying the LocalizedWarning implementation of a LocalizedParameter when it does" +
      " not exists labels into XML descriptor")
  void localizedWarningOfLocalizedParameterWithoutXmlDescriptorLocalization() {
    final LocalizedWarning frWarning = getWarningOfLocalizedParameterWithoutXmlDescriptorLocalization(FR_LOCALE);
    final LocalizedWarning esWarning = getWarningOfLocalizedParameterWithoutXmlDescriptorLocalization(ES_LOCALE);
    final LocalizedWarning itWarning = getWarningOfLocalizedParameterWithoutXmlDescriptorLocalization(IT_LOCALE);
    assertThat(frWarning.getValue(), startsWith("Un avertissement bien nécessaire"));
    assertThat(esWarning.getValue(), startsWith("Una advertencia muy necesaria"));
    assertThat(itWarning.getValue(), startsWith("Un avertissement bien nécessaire"));
  }

  private LocalizedParameter getLocalizedParameterWithoutXmlDescriptorLocalization(final String locale) {
    final LocalizedParameter localizedParameter =
        new LocalizedParameterList(kmeliaWA, ParameterList.copy(kmeliaWA.getParameters()), locale)
            .stream()
            .filter(p -> "noXmlDescriptorLocalization".equals(p.getName()))
            .findFirst()
            .orElse(null);
    assertThat(localizedParameter, notNullValue());
    return localizedParameter;
  }

  private LocalizedOption getOptionOfLocalizedParameterWithoutXmlDescriptorLocalization(final String locale) {
    return getLocalizedParameterWithoutXmlDescriptorLocalization(locale).getOptions().get(0);
  }

  private LocalizedWarning getWarningOfLocalizedParameterWithoutXmlDescriptorLocalization(final String locale) {
    final LocalizedWarning warning = getLocalizedParameterWithoutXmlDescriptorLocalization(locale)
        .getWarning()
        .orElse(null);
    assertThat(warning, notNullValue());
    return warning;
  }

  @Test
  @DisplayName("Verifying the LocalizedParameter implementation when it does not exists labels into XML descriptor")
  void localizedParameterWithoutAnyLocalization() {
    final LocalizedParameter frParam = getLocalizedParameterWithoutAnyLocalization(FR_LOCALE);
    final LocalizedParameter esParam = getLocalizedParameterWithoutAnyLocalization(ES_LOCALE);
    final LocalizedParameter itParam = getLocalizedParameterWithoutAnyLocalization(IT_LOCALE);
    String bundleKey = "parameter.noLocalization.label";
    assertMissingResourceException(frParam::getLabel, bundleKey, FR_LOCALE);
    assertMissingResourceException(esParam::getLabel, bundleKey, ES_LOCALE);
    assertMissingResourceException(itParam::getLabel, bundleKey, IT_LOCALE);
    bundleKey = "parameter.noLocalization.help";
    assertMissingResourceException(frParam::getHelp, bundleKey, FR_LOCALE);
    assertMissingResourceException(esParam::getHelp, bundleKey, ES_LOCALE);
    assertMissingResourceException(itParam::getHelp, bundleKey, IT_LOCALE);
  }

  @Test
  @DisplayName("Verifying the LocalizedOption implementation of a LocalizedParameter when it does" +
      " not exists labels into XML descriptor")
  void localizedOptionOfLocalizedParameterWithoutAnyLocalization() {
    final LocalizedOption frOption = getOptionOfLocalizedParameterWithoutAnyLocalization(FR_LOCALE);
    final LocalizedOption esOption = getOptionOfLocalizedParameterWithoutAnyLocalization(ES_LOCALE);
    final LocalizedOption itOption = getOptionOfLocalizedParameterWithoutAnyLocalization(IT_LOCALE);
    final String bundleKey = "parameter.noLocalization.option.Op 1.name";
    assertMissingResourceException(frOption::getName, bundleKey, FR_LOCALE);
    assertMissingResourceException(esOption::getName, bundleKey, ES_LOCALE);
    assertMissingResourceException(itOption::getName, bundleKey, IT_LOCALE);
  }

  @Test
  @DisplayName("Verifying the LocalizedWarning implementation of a LocalizedParameter when it does" +
      " not exists labels into XML descriptor")
  void localizedWarningOfLocalizedParameterWithoutAnyLocalization() {
    final LocalizedWarning frWarning = getWarningOfLocalizedParameterWithoutAnyLocalization(FR_LOCALE);
    final LocalizedWarning esWarning = getWarningOfLocalizedParameterWithoutAnyLocalization(ES_LOCALE);
    final LocalizedWarning itWarning = getWarningOfLocalizedParameterWithoutAnyLocalization(IT_LOCALE);
    final String bundleKey = "parameter.noLocalization.warning";
    assertMissingResourceException(frWarning::getValue, bundleKey, FR_LOCALE);
    assertMissingResourceException(esWarning::getValue, bundleKey, ES_LOCALE);
    assertMissingResourceException(itWarning::getValue, bundleKey, IT_LOCALE);
  }

  private void assertMissingResourceException(final Executable executable, final String bundleKey,
      final String locale) {
    assertThat(assertThrows(MissingResourceException.class, executable).getMessage(),
        is(format(ERR_TEMPLATE, COMPONENT_NAME, locale, bundleKey, COMPONENT_NAME)));
  }

  private LocalizedParameter getLocalizedParameterWithoutAnyLocalization(final String locale) {
    final LocalizedParameter localizedParameter =
        new LocalizedParameterList(kmeliaWA, ParameterList.copy(kmeliaWA.getParameters()), locale)
            .stream()
            .filter(p -> "noLocalization".equals(p.getName()))
            .findFirst()
            .orElse(null);
    assertThat(localizedParameter, notNullValue());
    return localizedParameter;
  }

  private LocalizedOption getOptionOfLocalizedParameterWithoutAnyLocalization(final String locale) {
    return getLocalizedParameterWithoutAnyLocalization(locale).getOptions().get(0);
  }

  private LocalizedWarning getWarningOfLocalizedParameterWithoutAnyLocalization(final String locale) {
    final LocalizedWarning warning = getLocalizedParameterWithoutAnyLocalization(locale)
        .getWarning()
        .orElse(null);
    assertThat(warning, notNullValue());
    return warning;
  }
}