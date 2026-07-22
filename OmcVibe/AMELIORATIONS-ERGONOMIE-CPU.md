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
