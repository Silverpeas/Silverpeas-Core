# Feature #15585 — Console de configuration

## Accès à l'écran

Après reconstruction et déploiement de la branche `feature-15585`, ouvrir
**Administration → Configuration → Paramètres Silverpeas** avec un administrateur général.
La rubrique Configuration est indépendante de l'activation des outils optionnels.
Une nouvelle session d'administration peut être nécessaire : les menus sont conservés dans
le contrôleur de session.

L'écran se trouve à `/silverpeas/jobManagerPeas/jsp/configuration.jsp` lorsque le contexte
web est `/silverpeas`. Cet accès direct vérifie lui aussi les droits d'administrateur général.
Les utilisateurs ordinaires et les gestionnaires d'espaces ou de domaines ne peuvent pas
consulter ou modifier les fichiers.

## Fonctionnement

- L'arbre reproduit les dossiers contenant des `.properties` sous
  `$SILVERPEAS_HOME/properties`. Une recherche filtre les chemins et ouvre les dossiers
  correspondants. Les valeurs ne sont chargées qu'à l'ouverture du fichier.
- Le tableau affiche les clés et les valeurs brutes : les expressions `${...}` sont conservées.
  Les filtres portent sur les clés et sur les seules lignes modifiées.
- Les commentaires `#` et `!` qui précèdent immédiatement une clé deviennent son aide.
  L'icône `?` propose une infobulle au survol et une aide ouvrable au clic ou au clavier.
  Les commentaires d'en-tête séparés de la première propriété par une ligne vide figurent
  dans « Documentation du fichier ». Les échappements Unicode des commentaires sont décodés
  pour l'affichage. Les fichiers conservent leurs commentaires d'origine à la sauvegarde.
- Les clés contenant `password`, `passwd`, `secret`, `credential`, `token` ou `privateKey`
  (avec les séparateurs usuels pour ce dernier) sont masquées par défaut, avec un bouton
  Afficher/Masquer. Cette détection par nom n'est pas un catalogue exhaustif des secrets.
- Annuler une ligne ou toutes les modifications restaure l'état ouvert, pas une valeur par
  défaut fournie par Silverpeas. Quitter un fichier modifié demande confirmation.
- Enregistrer et appliquer transmet les seules clés modifiées avec la révision du fichier.
  Un fichier modifié entre-temps produit un conflit et conserve la saisie. La comparaison
  affiche les valeurs à l'ouverture, les valeurs courantes et les valeurs saisies. Les valeurs
  sensibles restent masquées dans cette comparaison.
- « Conserver ma saisie sur cette version » reprend explicitement les modifications sur la
  version comparée, sans les enregistrer. L'administrateur vérifie puis enregistre.
  Les clés supprimées par un éditeur externe doivent être traitées avant cette reprise.

Les aides et valeurs sont affichées en texte brut, sans interprétation HTML. La console utilise
les requêtes AJAX Silverpeas pour bénéficier de la protection de session et des jetons CSRF
existants. Les réponses de configuration, y compris les erreurs, portent `Cache-Control: no-store`.
Aucune valeur n'est placée dans les URL, dans les messages d'erreur ou dans la représentation
textuelle des objets de configuration.

## Socle et API REST

Le point d'entrée métier est
`org.silverpeas.core.admin.configuration.ConfigurationService`. Chaque opération vérifie
l'identité et les droits de l'administrateur général. L'API protégée utilise les chemins suivants,
relatifs au contexte web :

| Méthode | Chemin | Objet |
|---|---|---|
| GET | `/services/admin/configuration/files` | Chemins relatifs triés des fichiers |
| GET | `/services/admin/configuration/file?path=...` | Révision, propriétés et documentation |
| PUT | `/services/admin/configuration/file` | Mise à jour de clés existantes |

Le corps du PUT contient `path` (chemin relatif), `revision` et `properties` (objet clé/valeur).
Le chemin en paramètre de requête reste accepté pour les clients existants ; si les deux sont
présents, ils doivent être identiques. La console transmet le chemin dans le JSON : le
constructeur de requêtes AJAX Silverpeas remplace les paramètres de corps lors de l’envoi du JSON. Les chemins invalides et
les corps incomplets donnent 400, les accès interdits 403, les fichiers absents 404, les conflits
409 et les erreurs d'entrée/sortie 500. L'authentification préalable relève du mécanisme
`@Authorized` du framework.

Le dépôt remplace atomiquement les fichiers existants et conserve les commentaires, l'ordre
et les lignes inchangées, ainsi que les modes POSIX lorsque le système les supporte. UTF-8
et les fichiers historiques ISO-8859-1 sont pris en charge suivant le comportement par défaut
de `PropertyResourceBundle`. Les chemins sortant de la racine et les liens symboliques sont
refusés. Aucune création ou suppression de clé n'est proposée ; une chaîne vide reste une valeur.

## Application immédiate et limites restantes

Après une modification effective, `ResourceLocator.resetCache()` invalide le cache du Kernel.
Une instance de `SettingBundle` déjà conservée voit la nouvelle valeur lors de sa lecture
suivante ; ce comportement est testé avec le vrai Kernel.

Cela concerne la JVM courante. Les valeurs copiées dans les champs des services, les propriétés
système chargées au démarrage et les autres nœuds d'un déploiement ne sont pas reconfigurés
par cette seule invalidation. L'écran indique cette limite. Le ticket #15585 nécessite encore
l'inventaire de ces usages et leurs adaptateurs de reconfiguration, ainsi que la propagation
multi-nœuds si elle est nécessaire au déploiement.

Les mises à jour du dépôt singleton sont sérialisées et les révisions détectent les modifications
externes depuis l'ouverture et pendant la préparation de la sauvegarde. Elles ne constituent
pas un verrou distribué : un éditeur externe peut encore intervenir entre la dernière vérification
et le remplacement atomique.

Restent également à traiter les descriptions typées et leurs validations métier, l'identification
explicite des paramètres sensibles, l'historique et un véritable retour aux valeurs livrées.
Le module graphique Aurora/skins (#14174) reste pour une étape ultérieure ; il pourra réutiliser
le service métier. Les médias et variables CSS demandent un traitement distinct des properties.

## Validation

Les tests Java couvrent le dépôt, les commentaires, les conflits, le cache, l'accès REST,
les erreurs et le contrat JSON, ainsi que la visibilité du menu selon les droits.

```sh
mvn -pl core-library,core-web -PskipMinify \
  -Dtest=ConfigurationFileRepositoryTest,ConfigurationServiceTest,ConfigurationResourceTest \
  -Dsurefire.failIfNoSpecifiedTests=false install
mvn -pl core-configuration,core-war -PskipMinify \
  -Dtest=JobManagerConfigurationMenuTest -Dsurefire.failIfNoSpecifiedTests=false package
```

Les tests navigateur utilisent le balisage de la vraie JSP, les vrais JS/CSS, le constructeur de requêtes AJAX Silverpeas et une API isolée
en mémoire. Ils couvrent les aides au clavier, l'affichage texte, les filtres, la sauvegarde,
l'annulation, les conflits, les erreurs et la navigation avec une saisie non enregistrée.
Ils nécessitent Node.js et Playwright (avec Chromium installé, ou `CHROME_PATH` pointant
sur un exécutable local).

```sh
node --test core-war/src/test/javascript/configuration-console.spec.cjs
```

Ces validations ne remplacent pas une recette de l'application déployée dans WildFly.
