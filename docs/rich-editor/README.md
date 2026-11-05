# Éditeur léger partagé

Le plugin Core `richEditor` fournit un éditeur visuel Tiptap, un format de stockage
Markdown et un rendu sécurisé. Discussion et les commentaires natifs utilisent
les mêmes ressources, sans chargement CDN. Les licences MIT et les notices des
bibliothèques restent distribuées avec les bundles.

## Intégration JSP / JavaScript

Inclure `<view:includePlugin name="richEditor"/>` dans la page JSP. Le plugin
charge une seule implémentation : Markdown-it, Tiptap, le sérialiseur Markdown,
le rendu et les styles Core. Les libellés traduits sont disponibles dans
`org.silverpeas.richEditor.multilang.richEditorBundle`.

L'initialisation s'effectue dans un fichier JavaScript externe :

```javascript
SilverpeasRichEditor.enhance(textarea, {
  labels: localizedLabels,
  people: query => loadAuthorizedUsers(query)
});
const markdown = SilverpeasRichEditor.getContent(textarea);
SilverpeasRichEditor.reset(textarea, markdown);
SilverpeasRichEditor.focus(textarea);
SilverpeasRichEditor.destroy(textarea);
```

`people` accepte un tableau ou une fonction asynchrone retournant des objets
`{id, name}`. Le serveur doit filtrer les utilisateurs par droits sur la ressource.
Les erreurs de recherche ne bloquent pas la saisie. Les mentions sont des nœuds
visuels ; leur syntaxe de stockage `@[Nom](id)` ne montre jamais l'identifiant
à l'utilisateur.

Le champ original est synchronisé par des événements `input`, ce qui permet
l'utilisation avec Vue ou un formulaire JSP. Un champ caché conserve sa valeur
Markdown pour la soumission. `destroy` libère les listeners et rétablit le champ
initial ; le retrait du composant du DOM libère aussi l'éditeur.

Options supplémentaires : `maxLength`, `fieldName`, `emoji`, `tool`, `integrated`,
`classPrefix` et les hooks de citation/annulation. Discussion utilise ces hooks
pour conserver ses icônes, citations et commandes métier. Les pièces jointes,
images, votes, idées, événements et visios restent gérés par l'application hôte.
Leur stockage et leurs droits ne sont pas déplacés dans l'éditeur.

## Rendu

```javascript
SilverpeasRichContent.render(markdown, {profileBase: profileUrl}); // fragment DOM
SilverpeasRichContent.renderHtml(markdown, {profileBase: profileUrl});
SilverpeasMarkdown.fromPlainText(historicalText); // import sans interprétation
```

Le sous-ensemble commun comprend gras, italique, barré, listes, citations, code,
liens, emoji et mentions. Le HTML brut n'est pas exécuté. Les protocoles dangereux
ne deviennent pas des liens. Les mentions dans du code restent du texte. Un hook
`decorateLink` permet à Discussion de reconnaître ses liens métier, sans introduire
une dépendance de Core envers Discussion.

## Commentaires natifs

Le composant Vue central active l'éditeur en création et modification. Les anciens
commentaires gardent explicitement leur format texte simple, même si leur contenu
ressemble à du Markdown. Leur première édition visuelle importe les caractères
littéralement.

La colonne historique `commentComment` conserve une projection lisible de 2 000
caractères maximum, utilisée par les notifications, indexations et clients existants.
La nouvelle colonne nullable `commentRichText` conserve la source riche (8 000
caractères maximum). Une valeur NULL signifie texte simple. Le serveur calcule
la projection ; il ne fait pas confiance au texte dérivé envoyé par le navigateur.
Une écriture par l'ancienne API texte remet le commentaire en texte simple.

L'API REST conserve `text` comme texte simple. Les nouveaux clients demandent
`?rich=true` pour recevoir aussi `format` (`PLAIN`/`MARKDOWN`) et `richText`.
Ils soumettent `format: "MARKDOWN"` et `richText` pour enregistrer du contenu riche.
Les clients historiques ne reçoivent pas de source riche à recopier par inadvertance.

`GET /services/comments/{instance}/{type}/{id}/mention-candidates?q=...`
retourne uniquement les comptes actifs autorisés à accéder à la contribution.
À la création, les mentions autorisées complètent les destinataires natifs sans
doublons. À la modification, seuls les utilisateurs nouvellement mentionnés sont
notifiés. L'auteur, les comptes inactifs et les utilisateurs sans accès sont exclus.
Les notifications suivent les canaux et préférences Silverpeas habituels.

Les commentaires ne reçoivent pas encore de stockage de pièces jointes ou d'images :
cette capacité demande un adaptateur de stockage propre aux commentaires. Le collage
et l'envoi d'images de Discussion restent disponibles via son adaptateur existant.

## Migration et compilation

Le module Core `comment` passe de 003 à 004. L'installation Silverpeas applique
l'ajout de colonne via `core-configuration/.../migrations/db/<db>/comment/up003/alter_table.sql`.
Le schéma d'installation neuve se trouve dans `comment/004`. La version de migration
de Discussion reste 001.

Le script PostgreSQL est :

```sql
ALTER TABLE SB_Comment_Comment ADD COLUMN IF NOT EXISTS commentRichText TEXT;
```

Laisser de préférence l'installeur l'appliquer automatiquement. Le script PostgreSQL
peut aussi être relancé si la colonne a déjà été ajoutée manuellement : elle est
conservée avec ses données. L'installeur peut ainsi terminer la migration normalement.
Aucune réécriture des commentaires existants n'est nécessaire.

Construire Core avant Components, sur leurs branches `feature-15552` :

```sh
cd ~/git/Silverpeas-Core
mvn -pl core-configuration,core-war -am clean install -DskipTests
cd ~/git/Silverpeas-Components
mvn -pl discussion/discussion-configuration,discussion/discussion-library,discussion/discussion-war -am clean install -DskipTests
```

Le build npm reproductible du bundle est dans `core-war/src/main/frontend/richEditor`.
Maven utilise le bundle embarqué sans exécuter npm. Les tests Discussion extraient
les ressources partagées du WAR Core installé dans Maven, sans deuxième copie source.
