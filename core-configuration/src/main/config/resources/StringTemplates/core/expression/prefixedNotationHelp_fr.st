<div>
  <p>Une expression par notation préfixée avec parenthèses permet d'utiliser des opérateurs avec
    pour chacun un nombre variable d'opérandes.</p>
  <p>Des parenthèses <u>doivent</u> encadrer une opérande.<br/>
    Un opérateur avec ses opérandes <u>peuvent</u> être encadrés par des parenthèses.</p>
  <p>Il est possible d'échapper les caractères '(' et ')', lorsque ces derniers ne doivent pas
    définir une opérande, en ajoutant juste avant le caractère '\'.</p>
</div>
<hr style="border-color: #ECECEC; color: #ECECEC"/>
<div style="font-style: italic">
  <p>Exemple : soit le contexte de l'évaluation d'un calcul simple.</p>
  Les opérateurs seraient les suivants :
  <ul>
    <li><b>+</b> : opérateur d'addition</li>
    <li><b>-</b> : opérateur de soustraction</li>
  </ul>
  Les expressions suivantes pourraient être évaluées :
  <ul>
    <li><b>+(1)(2)(5)</b> donnerait 8, c'est à dire la somme de 1, 2 et 5</li>
    <li><b>-(+(3)(4))(+(-2)(4))</b> donnerait 5, c'est à dire la soustraction entre la somme de 3 et
      4, et la somme de -2 et 4
    </li>
    <li><b>-(+(3)(4))(-(+(5)(6))(4))</b>...</li>
    <li>etc.</li>
  </ul>
</div>