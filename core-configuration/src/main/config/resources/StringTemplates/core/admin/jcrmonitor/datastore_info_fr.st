<p>
Silverpeas utilise un <strong>DataStore</strong> de la JCR pour gérer les fichiers ajoutés dans la plate-forme. <br/>
En combinant l'utilisation d'une base de données et d'un espace disque, il est en charge d'augmenter
les performances des traitements de données volumineuses.<br/>
Une des particularités dans sa gestion est de ne jamais supprimer de données, mêmes lorsque ces dernières ne sont plus utiles.<br/>
De ce fait, l'espace disque occupé par le DataStore augmente sans cesse au fil de l'utilisation de la JCR, notamment avec
$if(wbeEnabled)$
les fonctionnalités d'édition en ligne et d'édition dans le navigateur.
$else$
la fonctionnalité d'édition en ligne.
$endif$
<br/>Pour palier à cela, un traitement de purge des données devenues inutiles a été mis en place.
Il se lance automatiquement lorsque la plate-forme n'est plus sollicitée.<br/>
Mais dans certains contextes où la plate-forme est utilisée sans interruption significative, ce dernier ne se lance jamais.<br/>
Il est alors possible de lancer manuellement une purge depuis le menu proposé par cet écran.
</p>
<p>
Le chemin d'accès du <strong>DataStore</strong> sur le disque est :
<span class="path">$datastore_path$</span>.<br/>
</p>