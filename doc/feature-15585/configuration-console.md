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
- « Rechercher une clé dans tous les fichiers » recherche un fragment de clé sans tenir compte
  de la casse, y compris dans les sous-dossiers. Les résultats indiquent la clé et le chemin,
  sans exposer les valeurs. Cliquer ouvre le fichier avec toutes ses propriétés visibles,
  vide les filtres et place le focus sur la valeur de la clé recherchée.
  Les recherches sont exécutées à la demande, sans index conservé en mémoire.
- Le tableau affiche les clés et les valeurs brutes : les expressions `${...}` sont conservées.
  Les filtres portent sur les clés et sur les seules lignes modifiées.
- Les commentaires `#` et `!` qui précèdent immédiatement une clé deviennent son aide.
  L'icône `?` propose une infobulle au survol et une aide ouvrable au clic ou au clavier.
  Les commentaires d'en-tête séparés de la première propriété par une ligne vide figurent
  dans « Documentation du fichier ». Les échappements Unicode des commentaires sont décodés
  pour l'affichage. Les fichiers conservent leurs commentaires d'origine à la sauvegarde.
- Les propriétés sensibles détectées automatiquement par leur nom sont exclues de la console,
  de la recherche et des opérations de modification/pérennisation côté serveur.
  Cette détection par nom n'est pas un catalogue exhaustif des secrets.
- « Abandonner les saisies » restaure les valeurs du dernier chargement/enregistrement, uniquement
  pour les saisies non enregistrées. Ce bouton ne défait pas une sauvegarde antérieure. Après un
  essai enregistré, rétablir les anciennes valeurs puis enregistrer à nouveau met à jour le
  properties et sa personnalisation. Quitter un fichier modifié demande confirmation.
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

## Pérenniser les paramètres après un `clean install`

L'installation peut reconstruire les properties à partir des valeurs livrées.
**Enregistrer et appliquer** conserve désormais automatiquement toutes les modifications
effectives dans le mécanisme XML natif de Silverpeas Setup. Il n'y a plus de sélection de clés,
de choix du XML ni de bouton de pérennisation dans la console.

Le serveur découvre les XML `silverpeas-settings` de personnalisation dans
`$SILVERPEAS_HOME/configuration/silverpeas`, quel que soit leur nom. Le préfixe réservé `00-`
et les documents déclarés `product="Silverpeas"` restent exclus. Il utilise le dernier XML
personnalisé dans l'ordre alphabétique d'exécution, afin que les nouvelles valeurs ne soient
pas écrasées par une personnalisation antérieure. Sans XML personnalisé, il crée
`CustomerSettings.xml`. Les autres XML et leurs règles restent inchangés.

Seules les clés dont la valeur change sont fusionnées dans les `fileset` / `configfile`
existants, ou ajoutées si nécessaire. Les autres règles, les éléments `xmlfile`, les commentaires
XML et l'encodage déclaré sont conservés. La sérialisation peut normaliser la mise en forme.
Les propriétés sensibles ne sont ni exportées ni modifiées.

Le XML est enregistré avant le remplacement atomique du properties. Une erreur de lecture,
de syntaxe, de validation ou d'écriture du XML empêche l'application des valeurs courantes.
Si le properties ne peut pas être enregistré (dont un conflit de révision), le XML est restauré
avec ses octets d'origine, ou supprimé s'il vient d'être créé. Une modification externe intervenue
pendant ce rétablissement n'est jamais écrasée ; l'échec de rétablissement est associé à l'erreur
initiale. Il ne s'agit pas d'une transaction durable sur deux fichiers : un arrêt brutal du
processus entre les écritures peut nécessiter une vérification manuelle.
Une sauvegarde sans changement effectif ne crée ni XML ni entrée de journal.

Les révisions continuent de protéger les écritures concurrentes et les saisies sont conservées
en cas d'erreur. Un XML mal formé, contenant une DTD, ou inaccessible n'est pas écrasé.
Les liens symboliques et les chemins sortant des répertoires de configuration sont refusés.
Cette opération ne lance pas une installation.

La compatibilité a été vérifiée avec le véritable `XmlSettingsScript` de Silverpeas Setup :
un XML généré a été réappliqué sur un répertoire de test dont les properties avaient été remis
aux valeurs par défaut, avec contrôle des valeurs rétablies et des propriétés non sélectionnées.
Cela ne constitue pas l'exécution d'un `clean install` complet sur une installation utilisateur.

## Socle et API REST

Le point d'entrée métier est
`org.silverpeas.core.admin.configuration.ConfigurationService`. Chaque opération vérifie
l'identité et les droits de l'administrateur général. L'API protégée utilise les chemins suivants,
relatifs au contexte web :

| Méthode | Chemin | Objet |
|---|---|---|
| GET | `/services/admin/configuration/files` | Chemins relatifs triés des fichiers |
| GET | `/services/admin/configuration/file?path=...` | Révision, propriétés et documentation |
| PUT | `/services/admin/configuration/file` | Mise à jour et pérennisation automatique de clés existantes |
| GET | `/services/admin/configuration/persistence` | XML cibles (compatibilité avec les anciens clients) |
| POST | `/services/admin/configuration/persistence` | Pérennisation explicite (compatibilité avec les anciens clients) |

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

Le POST de pérennisation reçoit `path`, `revision`, `keys` et `target` ; `target` contient
`name`, `revision` et `exists`, issus de la découverte. Il retourne la nouvelle révision XML.
Il n'accepte aucune valeur de propriété fournie par le client. Ses erreurs utilisent les mêmes
statuts et protections que les autres opérations de configuration.

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

Restent également à traiter les descriptions typées et leurs validations métier,
l'historique et un véritable retour aux valeurs livrées.
Le module graphique Aurora/skins (#14174) reste pour une étape ultérieure ; il pourra réutiliser
le service métier. Les médias et variables CSS demandent un traitement distinct des properties.

## Validation

Les tests Java couvrent le dépôt, les commentaires, les conflits, le cache, l'accès REST,
les erreurs et le contrat JSON, ainsi que la visibilité du menu selon les droits.

```sh
mvn -pl core-library,core-web -PskipMinify \
  -Dtest=CustomSettingsRepositoryTest,ConfigurationFileRepositoryTest,ConfigurationServiceTest,ConfigurationResourceTest \
  -Dsurefire.failIfNoSpecifiedTests=false install
mvn -pl core-configuration,core-war -PskipMinify \
  -Dtest=JobManagerConfigurationMenuTest -Dsurefire.failIfNoSpecifiedTests=false package
```

Les tests navigateur utilisent le balisage de la vraie JSP, les vrais JS/CSS, le constructeur de requêtes AJAX Silverpeas et une API isolée
en mémoire. Ils couvrent la sauvegarde sans sélection technique, les aides au clavier, l'affichage texte, les filtres, la sauvegarde,
l'annulation, les conflits, les erreurs et la navigation avec une saisie non enregistrée.
Ils nécessitent Node.js et Playwright (avec Chromium installé, ou `CHROME_PATH` pointant
sur un exécutable local).

```sh
node --test core-war/src/test/javascript/configuration-console.spec.cjs
```

Ces validations ne remplacent pas une recette de l'application déployée dans WildFly.

## Journal des changements

Les opérations réussies de la console sont tracées dans
`$SILVERPEAS_LOG/silverpeas-configuration.log` (habituellement `$SILVERPEAS_HOME/log`).
Chaque entrée contient un horodatage UTC, l'identifiant, le login et le domaine de
l'administrateur, l'opération, le fichier et les clés.
Pour un XML, le chemin du fichier properties source est également enregistré.
Pour les properties, `changes` contient pour chaque clé `key`, `before` et `after` en clair.
Pour les XML, `values` contient les valeurs modifiées écrites dans le XML ; les anciennes
valeurs du XML ne sont pas exposées par cette opération. Les empreintes des fichiers ne sont
pas écrites dans le journal ; les révisions restent utilisées en interne pour détecter les
modifications concurrentes.
Les valeurs des clés contenant `password`, `passwd`, `pwd`, `passphrase`, `secret`,
`credential`, `token`, `privateKey`, `apiKey` ou `accessKey` (avec les séparateurs usuels)
sont remplacées par `[MASKED]`, sans distinction de casse. La détection par nom ne constitue
pas un catalogue exhaustif des secrets.
Le JSON échappe les caractères de contrôle pour conserver une entrée sur une seule ligne.

Les opérations distinguées sont `UPDATE_PROPERTIES`, `CREATE_CUSTOM_SETTINGS` et
`UPDATE_CUSTOM_SETTINGS`. Les mises à jour sans changement de contenu ne produisent pas
d'entrée ; une tentative échouée n'est pas enregistrée comme modification réussie.
Les clés properties correspondent aux valeurs effectivement changées ; pour un XML,
il s'agit des clés sélectionnées écrites dans le XML. Les éditions manuelles et les
modifications effectuées par l'installateur ne passent pas par ce journal de la console.

Le namespace `silverpeas.core.admin.configuration.audit` est configuré en INFO,
indépendamment du niveau général. Le profil WildFly dans **Silverpeas-Distribution**
associe ce namespace à un handler synchrone dédié, en ajout et avec autoflush.
Il utilise les mêmes `SILVERPEAS_LOG_BACKUP_SIZE` et `SILVERPEAS_LOG_BACKUP_MAX` que
`silverpeas-trace.log`, sans recopier ces entrées dans le journal général.
La rotation implique une durée de conservation limitée par ces paramètres.

Le déploiement nécessite **les deux branches `feature-15585` de Core et Distribution**.
Il faut mettre à jour le profil `configuration/jboss/silverpeas-logging-profile.cli`
et le réappliquer via l'installation Silverpeas ; remplacer uniquement le WAR ne crée
pas le handler dédié. Les installations existantes doivent conserver leurs réglages locaux
lors de cette mise à jour. Aucun fichier d'installation local n'est modifié par ces tests.

Vérification du profil avec une instance WildFly embarquée temporaire :
`JBOSS_HOME=/chemin/vers/wildfly python3 src/test/python/configuration_audit_logging_test.py`
depuis Silverpeas-Distribution.

## Exclusion automatique des propriétés sensibles

La console exclut les clés dont le nom évoque un mot de passe, un token, un secret,
une clé privée/API ou un code de sécurité. La détection ignore la casse, les accents
et les séparateurs (par exemple `clientSecret`, `api_key`, `security.code`).
Ces propriétés et leur aide associée sont absentes des réponses de lecture et des
résultats de recherche. Le serveur refuse leur modification et leur pérennisation XML,
y compris par appel direct à l'API. Modifier une autre propriété préserve leur valeur.
La même détection sert au masquage dans le journal.

Cette heuristique porte sur les noms des clés : un secret placé dans une clé au nom
anodin ne peut pas être identifié de manière fiable. Elle ne nécessite aucun fichier
de configuration supplémentaire.
