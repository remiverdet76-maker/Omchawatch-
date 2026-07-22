# OmcVibe 181 — 9 points d'ergonomie / design / optimisation CPU & batterie

Suite du premier passage (`AMELIORATIONS-MOTEUR-BINAURAL.md`, 18 points sur le
moteur binaural). Cette passe traite le claquement audio constaté sur un
Redmi 2021 (rendu visuel qui se bat avec l'audio pour le CPU au premier
plan — d'où l'app plus stable en arrière-plan, où le rendu se met en pause),
l'ergonomie du dock, et les modes samples/presets.

## Les 9 points — tous implémentés dans `OmcVibe/www/index.html`

1. **Fond d'écran par défaut (HD, optimisé)** — `img/cosmic-flux.jpg` était
   référencé dans le CSS/JS mais **absent de l'APK** (404 silencieux depuis
   le début). Ton visuel est maintenant recadré/compressé pour mobile
   (800×1200, JPEG progressif qualité 82, ~244 Ko — largement suffisant pour
   un fond flouté à 42% d'opacité derrière l'UI) et bien présent dans le
   build. Corrige un vrai bug en plus d'ajouter le nouveau visuel.
2. **Forme d'onde avec zoom/dézoom** — l'éditeur de découpe (`_drawSedWaveform`)
   a maintenant un état `zoom`/`viewStart` : molette (bureau), pincement à
   deux doigts (tactile) et boutons +/−/Ajuster (`sedZoom()`, `sedZoomFit()`).
   Un glissement à un doigt fait défiler la fenêtre visible quand zoomé.
3. **Marqueurs par appui long (couper/copier/coller/supprimer)** — appui
   long sur la forme d'onde pose un marqueur (`_sedPlaceMarkerAt`) ; appui
   court sur un marqueur existant ouvre un menu flottant à 4 actions
   (`sedMarkerCut/Copy/Paste/Delete`) qui manipulent directement l'AudioBuffer
   décodé (retrait/insertion réelle de segments, marqueurs et bornes in/out
   réajustés automatiquement).
4. **Glisser-déposer un sample vers un dossier (appui long)** — appui long
   sur une ligne de sample la décroche (fantôme qui suit le doigt), survoler
   un onglet de dossier le surligne, relâcher dessus déplace le sample
   (`_sampleMoveToFolder`, persisté via `_sdbUpdateFolder` — seule la
   métadonnée de rangement change, jamais les octets sur le disque).
5. **Superposition de 2 samples + pause du moteur binaural en édition** —
   un 2ᵉ sample peut être chargé en overlay dans l'éditeur (tracé en vert,
   volume réglable, fusionné dans le buffer à l'export). Ouvrir l'éditeur
   met le moteur binaural en **pause complète** (`stopFlow()`, pas juste en
   sourdine) pour libérer tout le CPU à l'édition ; il reprend automatiquement
   à la fermeture.
6. **Dock raccourci d'1/3 + 2ᵉ sphère raccourci par case** — hauteur du dock
   96px→64px sur smartphone (92px→61px ailleurs), cellules et bouton Flux
   réduits en proportion. Chaque case (sauf Flux, déjà pleine) a maintenant
   une 2ᵉ sphère "+" assignable indépendamment (`DOCK_MAP[cell].extra2`),
   par défaut branchée sur les nouveautés du premier passage (ancre
   respiratoire, sub-drone, anti-pompage, annuler le tirage).
7. **Suppression des émoticônes colorées des menus** — 🗂🎲📁➕🗑⚡ remplacés
   par des symboles sobres cohérents avec l'identité déjà en place (⚄ ▤ ✕ + ◐).
   Les glyphes déjà sobres de l'app (⚙ ✦ ⚄ ⊞ ⊙ ◎ ≡ ⊹ ◈ ✂ ✕ ★…) sont inchangés
   — ce sont des icônes, pas des émoticônes.
8. **Appui long sur une sphère "+" → assignation rapide** — un appui long qui
   NE bouge PAS la sphère (le déplacement réel reste inchangé) ouvre un
   panneau avec deux sections : bascules directes des nouveautés du premier
   passage (respiration, ancre, dérive Δ, casque strict, anti-pompage,
   sub-drone — on/off immédiat) et réassignation de cette sphère précise à
   n'importe quelle action du registre existant.
9. **Auto-adaptation CPU du rendu au premier plan** — la boucle de rendu
   (déjà bridée à ~22 fps, déjà coupée pendant les menus/l'arrière-plan)
   mesure maintenant l'écart réel entre frames dessinées ; si l'appareil ne
   tient pas le rythme (>35% des frames à plus du double du cap sur les 40
   premières observées), le mode "Visuel léger" existant s'active **tout
   seul**, sans que l'utilisateur ait à le découvrir — jamais s'il a déjà
   fait un choix manuel. Répond directement au constat premier-plan/arrière-plan.

## Build APK

`OmcVibe/build/OmcVibe181-modifie-debug.apk` régénéré avec ces 9 points en
plus des 18 précédents, via le même pipeline (assets remplacés/ajoutés,
`AndroidManifest.xml`/`resources.arsc`/`classes*.dex` strictement identiques
à l'APK d'origine, signature APK v2 vérifiée pour API 24-34). Même limite
que la passe précédente : pas de signature v1, donc incompatible avec
Android 5.1/6.0 (API 22-23) — sans impact sur un usage réel en 2026.

## Où regarder dans le code

- Fond d'écran : `OmcVibe/www/img/cosmic-flux.jpg`, `setBackground()`/`_loadBackground()`.
- Éditeur de découpe (zoom/marqueurs/overlay) : section `_sed*` — `sampleEditorOpen()`,
  `_drawSedWaveform()`, `_sedBindWaveInteractions()`, `sedMarkerCut/Copy/Paste/Delete()`,
  `sedOverlayPick()`, `_sedMixOverlayInto()`, `_sedPauseBinauralForEdit()`.
- Glisser-déposer samples : `_sampleListBindDrag()`, `_sampleMoveToFolder()`, `_sdbUpdateFolder()`.
- Dock (2ᵉ sphère + assignation rapide) : `DOCK_CELLS`, `DOCK_MAP_DEFAULT`, `_bindExtraDraggable()`,
  `_openDockQuickAssign()`, `DQA_TOGGLES`.
- Auto-adaptation CPU : `_perfAutoAdapt()`, `_autoEnableVisLite()`, appelé depuis `masterTick()`.

## Passe de correction (retours du test sur appareil réel)

Le premier rendu du dock raccourci (point 6 ci-dessus) s'est révélé cassé sur
un appareil réel : le dock avait grossi de 50% au lieu de rétrécir, et les
sphères raccourcis chevauchaient les cellules. Cause : un système de
positionnement `position:absolute` calculé en pixels par JS
(`_defaultExtraPositions()`), fiable en test mais pas assez robuste face aux
variations réelles d'écran/densité. Corrections apportées :

1. **Dock reconstruit en flexbox pur** — chaque cellule (`.dcell-wrap`)
   contient maintenant son bouton principal (`flex:2`, largeur réduite d'1/3)
   et un conteneur `.dcell-extras` (`flex:1`) avec ses 2 sphères, tous en
   flux normal. Plus aucun calcul de pixels JS par défaut : impossible que ça
   chevauche ou déborde, quel que soit l'écran. Le glisser-déposer (appui
   long) reste possible — SEULE une sphère activement décrochée passe en
   `position:fixed`, gérée dans `_bindExtraDraggable()`/`_loadExtraPos()`/
   `resetExtraPos()` (clé de sauvegarde renommée en `-v3` pour ignorer les
   anciennes positions issues du build cassé).
2. **Émoticônes colorées retirées des 3 cellules signalées** — ⚄ (Option Jeu
   Aléatoire), ♪ (Samples), ✦ (Effet Audio) : ces glyphes, censés être en
   "présentation texte" par défaut selon Unicode, s'affichaient en couleur
   sur l'appareil réel (police emoji du fabricant plus agressive que prévu).
   Retirés purement et simplement des libellés de ces 3 cellules.
3. **Sphères "+" pleines et cohérentes** — fond plein assorti à la couleur de
   la cellule (`#FF4646`/`#FF9628`/`#5AFFA0`/`#3CAAFF`), bordure et symbole
   "+" en doré (`#FFD700`).
4. **Plus aucune police blanche** — chaque `color:#fff`/`rgb(255,255,255)`
   du fichier a été remplacé par une teinte cuivrée/argentée/dorée selon le
   contexte (doré `#FFD700` pour les états actifs/survol/icônes de menu,
   argenté `#D7DAE0` pour le texte neutre secondaire). Le corps de l'app
   utilisait déjà un blanc cassé chaleureux (`#f0ead8`), inchangé.
5. **Sélection de texte tactile désactivée** — `user-select:none` +
   `-webkit-touch-callout:none` posés globalement (`html,body,*`), avec
   exception explicite pour les vrais champs `input`/`textarea` (renommage
   de dossier/sample) qui restent sélectionnables/collables normalement.
   Corrige le menu contextuel natif Android ("Copier/Partager/Tout
   sélectionner") qui apparaissait sur un appui long dans les panneaux.
6. **Plein écran par défaut** — tentative immédiate au chargement
   (`_autoRequestFullscreen()`), plus un déclenchement de secours sur le
   tout premier appui tactile de la session (la Fullscreen API exige un
   geste utilisateur, non garanti au chargement pur).

APK régénéré avec cette passe de correction en plus des 18+9 points
précédents, même pipeline de signature (v2 uniquement, vérifiée API 24-34).

## 2e passe de correction — dock agrandi, sphères encadrantes

Retour utilisateur : le dock devait être agrandi (comme à l'origine +25%),
avec les sphères raccourcis placées à gauche ET à droite de chaque cellule,
sans espace vide (cellule et sphère affleurent), et sans jamais que le
panneau général ne chevauche le dock/les boutons plein écran/retour.

1. **Dock, cellules et sphères agrandis** — dimensions recalculées depuis la
   toute première version de l'app (avant toute modification), ×1.25 :
   hauteur du dock 92px→115px (96px→120px sur smartphone), cellule
   34px→43px (36px→45px), sphère 42px→53px, bouton Flux 76px→95px
   (74px→93px sur smartphone). Toutes les références à l'ancienne hauteur du
   dock (positions du chevron, du bouton retour, hauteur réservée par les
   panneaux) recalculées en conséquence — c'est ce mécanisme existant qui
   garantit qu'aucun panneau ne chevauche jamais le dock, quelle que soit sa
   hauteur.
2. **Sphères à gauche ET à droite, affleurantes** — testé et mesuré : deux
   sphères de 53px de chaque côté d'une cellule ne laissent quasiment plus
   de place pour le texte sur un écran de téléphone réel (~15px de large,
   illisible). Après clarification, retour à 1 seule sphère par cellule
   (comme à l'origine, mais agrandie +25%) : colonne gauche (rouge/orange) →
   sphère côté extérieur gauche, colonne droite (vert/bleu) → sphère côté
   extérieur droit — les sphères encadrent bien le dock des deux côtés.
   Layout flexbox `gap:0` : sphère et cellule se touchent exactement, sans
   vide ni chevauchement. Perd la 2ᵉ sphère raccourci par case ajoutée dans
   la passe précédente (accepté).
3. **Aucun conflit avec le menu général** — vérifié avec Playwright sur
   plusieurs largeurs d'écran (360/393/412px) : le bas du panneau
   Paramètres/Samples/etc. s'arrête exactement au sommet du dock, le bouton
   Retour et le bloc chevron/plein écran restent toujours au-dessus, jamais
   de superposition.

APK régénéré avec cette 2e passe de correction.
