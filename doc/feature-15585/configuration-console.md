# Feature #15585 — Socle de configuration et proposition d'IHM

## Périmètre de ce premier incrément

Le service `org.silverpeas.core.admin.configuration.ConfigurationService` constitue le point
d'entrée métier de la future console. Il vérifie les droits d'administrateur général pour
chaque lecture, énumération et modification. Il est indépendant d'une présentation web.

Le dépôt parcourt récursivement la racine fournie par `SilverpeasResourcesLocation`, soit
`$SILVERPEAS_HOME/properties` dans Silverpeas Core. Les chemins retournés sont relatifs,
triés et conservent tous les niveaux de dossiers. L'énumération ne retourne aucune valeur.
Le service lit les valeurs brutes : une expression `${...}` n'est pas remplacée par sa valeur
calculée lors de l'édition.

Une sauvegarde porte sur un fichier existant et sur un ensemble de clés existantes. Elle reçoit
la révision obtenue lors de la lecture, contrôle les changements concurrents et remplace le
fichier atomiquement. Elle conserve les commentaires, l'ordre et les lignes non modifiées,
ainsi que les permissions POSIX lorsque le système de fichiers les supporte. Les clés et
valeurs modifiées sont sérialisées avec l'échappement Java Properties. UTF-8 et les fichiers
historiques ISO-8859-1 sont pris en charge, selon le comportement par défaut de
`PropertyResourceBundle`. Les chemins sortant de la racine et les liens symboliques sont refusés.
Les valeurs ne figurent pas dans la représentation textuelle du résultat.

Ce premier incrément ne fournit pas encore d'endpoint REST, d'écran, de création de clés,
de suppression de clés, de description typée des paramètres, d'historique ou de sauvegarde
permettant un retour à une ancienne version. Les valeurs restent des chaînes : les règles
métier (bornes numériques, URL, dépendances entre paramètres) devront être ajoutées avant
l'exposition des paramètres concernés dans des formulaires spécialisés.

## Application immédiate : comportement vérifié et limites

Après une modification effective, le service invalide le cache du Kernel avec
`ResourceLocator.resetCache()`. Les lectures suivantes de `SettingBundle`, y compris sur
une instance conservée avant la modification, voient les nouvelles valeurs. Un test utilise
le vrai Kernel et un fichier créé uniquement dans le répertoire de sortie des tests pour
vérifier ce comportement. Aucune configuration d'une installation existante n'est modifiée
par les tests.

Cette invalidation s'applique au nœud JVM courant. Elle ne réinitialise pas les valeurs copiées
dans des champs, les propriétés système chargées au démarrage, les services externes ou les
caches des autres nœuds. Le ticket #15585 ne sera terminé qu'après identification et traitement
de ces cas : prévoir des adaptateurs de reconfiguration et, pour un déploiement multi-nœuds,
la propagation des changements. L'écran devra distinguer une sauvegarde d'une application
réellement terminée ; il ne devra pas annoncer une application immédiate qui n'a pas eu lieu.

Les révisions empêchent l'écrasement d'une version déjà modifiée et les mises à jour sont
sérialisées dans le dépôt singleton de cette JVM. Elles ne constituent pas un verrou distribué
avec les éditeurs externes ou d'autres JVM : une course reste possible entre la dernière
vérification et le remplacement atomique du fichier.

## Proposition d'IHM

Entrée proposée : **Administration → Configuration → Paramètres Silverpeas**,
visible uniquement aux administrateurs généraux.

```text
Paramètres Silverpeas                         [Rechercher un fichier…]

Dossiers / fichiers         │ org / silverpeas / general.properties
                            │
▾ org                       │ [Filtrer les clés…]  [Modifiés uniquement]
  ▾ silverpeas              │
      general.properties    │ Clé                 Valeur                 État
    ▸ lookAndFeel           │ …                   …                      …
    ▸ …                     │ …                   …                      Modifié
▸ …                         │
                            │ 2 modifications non enregistrées
                            │ [Annuler les modifications] [Enregistrer et appliquer]
```

- **À gauche :** arbre repliable reproduisant les dossiers physiques, fichiers triés par nom.
  Charger les valeurs uniquement lors de l'ouverture d'un fichier ; l'arbre utilise la liste
  des chemins relatifs. Conserver le dossier ouvert et la sélection pendant la navigation.
- **À droite :** fil d'Ariane, nom du fichier, filtre local et tableau des clés triées.
  Pour cette première interface générique, proposer des champs texte et des zones multiligne.
  Les interrupteurs, listes et contrôles numériques viendront des descriptions de paramètres,
  sans déduire arbitrairement leur type d'une valeur existante.
- **Édition :** signaler les lignes modifiées, proposer l'annulation locale d'une ligne et
  de l'ensemble des modifications. Une valeur vide est une valeur valide ; ce n'est pas une
  suppression. Prévenir avant de quitter un fichier contenant des modifications non enregistrées.
- **Sauvegarde :** envoyer uniquement les clés modifiées avec la révision du fichier lu.
  En cas de conflit, conserver les modifications locales et proposer de recharger/comparer
  la version courante. Ne jamais réessayer automatiquement en écrasant celle-ci.
- **Valeurs sensibles :** les champs concernés devront être explicitement identifiés et masqués
  par défaut. Aucun contenu sensible dans les URL, notifications ou journaux. La réponse web
  devra empêcher la mise en cache des configurations.
- **Recherche transversale, ensuite :** rechercher un nom de clé et retrouver son chemin dans
  l'arbre. Cela demandera un contrat de recherche supplémentaire ; le socle actuel expose la
  liste des fichiers et la lecture à la demande.

Le bouton « Annuler les modifications » ne restaure que l'état lu lors de l'ouverture. Un vrai
retour aux valeurs livrées par Silverpeas suppose une source de valeurs par défaut identifiable ;
elle ne se déduit pas du fichier actuellement installé.

## Suite de l'implémentation

1. Définir les paramètres éligibles à l'application immédiate et les reconfigurations nécessaires.
2. Exposer le service dans une ressource REST protégée, avec les erreurs adaptées : accès refusé,
   chemin invalide, fichier absent, conflit de révision et erreur de sauvegarde.
3. Intégrer l'écran dans la console existante avec ses composants de navigation et de formulaire,
   en conservant les protections de session et CSRF du framework.
4. Ajouter descriptions, validations, masquage des valeurs sensibles et suivi des changements.
5. Pour #14174, réutiliser le même service depuis des formulaires Aurora/skins adaptés aux usages.
   Les variables CSS et les médias demandent un traitement distinct de celui des `.properties`.

## Validation de ce socle

```sh
mvn -o -pl core-library -PskipMinify \
  -Dtest=ConfigurationFileRepositoryTest,ConfigurationServiceTest test
```

Les tests couvrent l'accès, l'arborescence, les formats Properties, les encodages, les
commentaires, les valeurs brutes, les conflits, les chemins et liens interdits, les permissions,
les échecs de sauvegarde et la prise en compte immédiate par une instance de `SettingBundle`
déjà chargée.
