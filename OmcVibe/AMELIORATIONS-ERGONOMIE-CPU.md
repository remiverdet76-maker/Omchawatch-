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

## 3e passe — correction du claquement audio ("saturation façon LFO")

Retour terrain (Redmi 2021) : claquements rythmés ressemblant à un LFO réglé
beaucoup trop haut, empirant avec l'usage. Diagnostic posé puis corrigé :

1. **Fuite de nœuds LFO à chaque reconstruction d'oscillateur (cause
   principale)** — `releaseOsc()` (§2.5) nettoyait le patchbay modulaire
   (`clearOscMods`) mais jamais le LFO de respiration par paire
   (`_oscVolLFOs`, §2.10), actif par défaut sur quasiment tous les
   oscillateurs. À chaque reconstruction (`rebuildAllOscs()` — déclenché par
   "Annuler le dernier tirage", le chargement d'un preset à moteurs
   différents, le toggle anti-pompage — ou un simple changement de moteur
   d'onde par paire), l'ancien LFO restait connecté à un `AudioParam`
   orphelin et continuait de tourner indéfiniment, jamais stoppé. Sur une
   session avec beaucoup de tirages/annulations, ces nœuds fantômes
   s'accumulaient jusqu'à ce que le thread audio ne tienne plus le temps
   réel → dépassements de tampon → claquements rythmés. Corrigé :
   `releaseOsc()` appelle maintenant `clearOscVolLFO()` (et, par sécurité,
   `clearDeltaDrift()` pour la paire concernée) de façon synchrone à la
   destruction de l'oscillateur. Vérifié : 8 tirages aléatoires consécutifs
   maintiennent exactement 14 LFO de respiration + 7 dérives Δ (un jeu de
   7 paires), sans aucune croissance — avant le correctif, ce test n'était
   pas borné.
2. **Animation CSS continue du bouton Flux insensible au mode "Visuel
   léger"** — `fluxIdleGold`/`fluxPulseGold` (`box-shadow` en boucle
   infinie) tournaient même quand "Visuel léger" était actif, ce mode ne
   coupant que le rendu JS (`masterTick`), pas les animations CSS pures.
   Sur le bouton Flux agrandi (#32, 95px), cette recomposition continue
   pesait davantage. Corrigé : `body.vis-lite .dcell-flux{ animation:none; }`
   — "Visuel léger" libère maintenant vraiment ce budget CPU/GPU.

## Où regarder dans le code (audio)

- Fuite corrigée : `releaseOsc()`, `attachOscVolLFO()`/`clearOscVolLFO()`,
  `attachDeltaDriftToNode()`/`clearDeltaDrift()`, `rebuildAllOscs()`.
- Mode Visuel léger : `toggleVisLite()`, `_autoEnableVisLite()`,
  classe `body.vis-lite`, règle CSS sur `.dcell-flux`.

## 4e passe — "Qualité audio" adaptative + rebrand OmcVibe432

Retour terrain : claquements toujours présents malgré le correctif de fuite.
Demande : passe d'analyse en 18 points (sources + options) pour que tout
appareil, bas ou haut de gamme, profite d'un son propre, avec l'idée d'un
"détecteur CPU + limiteur". Implémenté dans la foulée, à la suite de
l'analyse validée par l'utilisateur :

1. **Le convolveur de reverb tournait à plein régime en permanence, même à
   0%** — `masterReverb` (IR jusqu'à 14s sur l'espace "Cosmos") recevait un
   signal réel des 7 paires en continu (`PAIR_FX` = `true` par défaut). Un
   gating existait déjà (`_gateReverb`/`_gateDelay`/`_gatePP`) mais ne
   coupait que la SORTIE (après le calcul) — le calcul de convolution
   lui-même tournait quand même. Corrigé : le gating coupe maintenant aussi
   l'ENTRÉE (`reverbSendBus↔masterReverb`, idem delay/ping-pong), au même
   instant sûr (retour déjà à ~0). En creusant, le contrôle qui déclenche ce
   gating avait lui-même un délai trop court (350ms/300ms) pour un wet parti
   du maximum (1.0) avec le time-constant utilisé — le seuil de coupure
   n'était souvent jamais atteint, donc le gate ne se déclenchait quasiment
   jamais, même pour la sortie. Délais recalculés (550/450ms) pour couvrir
   le pire cas ; vérifié par mesure directe du gain toutes les 150ms.
2. **Un DelayNode de dérive analogique par PARTIEL** (`_connectDecorrelatedDrift`)
   — un moteur à 4 partiels (voixquat) créait jusqu'à 4 DelayNode pour ce
   seul effet cosmétique par oscillateur. Mutualisé : un seul DelayNode par
   oscillateur (Pingala/Ida), partagé entre tous ses partiels — même rythme
   de dérive propre à cet oscillateur, juste appliqué à tous ses partiels au
   lieu d'être dupliqué. Jusqu'à 56 nœuds → 14 dans le pire cas.
3. **"Qualité audio" (Auto / Complète / Légère)** — nouveau réglage dans
   Personnaliser l'interface. En "Légère" : reverb totalement coupée (quel
   que soit le curseur) et le tirage aléatoire ne pioche plus que le moteur
   d'onde le plus simple (1 partiel) pour la paire vedette. En "Auto"
   (défaut) : un watchdog (`_armAudioWatchdog`, toutes les 3s pendant la
   lecture) surveille `AudioContext.outputLatency` — signal standard du Web
   Audio API qui grandit spécifiquement quand le THREAD AUDIO peine à tenir
   le temps réel (contrairement au détecteur visuel existant, qui ne voit
   que le thread de rendu, séparé et non prioritaire sur Android). Après ~9s
   de tension soutenue, bascule seule en "Légère" — jamais si l'utilisateur
   a lui-même choisi un réglage fixe, jamais ne remonte automatiquement
   (redémarrer le Flux repart propre), même logique que "Visuel léger".

APK renommé **OmcVibe432** à la demande (l'ancien "OmcVibe 181" est
retiré). Limites techniques honnêtes sur ce rebrand :
- Le **nom affiché sous l'icône** dans le tiroir d'applications Android
  (`android:label` d'AndroidManifest.xml) ne peut pas être changé avec ce
  pipeline (fichier XML compilé, hors de portée sans les outils Android SDK
  complets) — seuls le titre de la page web, le titre d'onglet dynamique
  (`OmcVibe432 · <fréquence>`) et le splash à l'ouverture sont renommés.
- L'**icône réelle de l'écran d'accueil sur Android 8+** utilise une icône
  adaptative dont le calque de premier plan est un vecteur XML compilé
  (`res/drawable-anydpi-v24/ic_launcher_foreground.xml`) — non modifiable
  avec ce pipeline pour la même raison. Les PNG de secours (legacy/round,
  utilisés par les lanceurs plus anciens, les notifications, certains
  contextes) sont bien remplacés par le nouveau visuel.
- L'**écran de démarrage natif** (`res/drawable*/splash.png`, image simple,
  pas de vecteur ni de manifest impliqué) est lui entièrement remplacé et
  fonctionnera partout — c'était de toute façon le placeholder Capacitor
  générique par défaut, jamais personnalisé dans l'APK d'origine.

## 5e passe — audit ciblé (pas de quota, ce qui était réellement à trouver)

Demande explicite : creuser encore, sans viser un nombre de points fixe —
remonter uniquement ce qui est réellement à réparer. Vérifiés et jugés
sains (aucune modification nécessaire) : moteur "Bowl" (AudioWorklet des
bols chantants — polyphonie déjà plafonnée à 24 voix avec éviction de la
plus ancienne, voix auto-nettoyées ; **inutilisé par l'UI actuelle**, code
mort mais inoffensif, laissé en l'état), fuites de blob URL (`sampleStop`/
`sampleDelete` révoquent correctement), tous les `window.addEventListener`
de gestes tactiles (chacun protégé par un flag anti-double-binding ou une
référence de fonction nommée idempotente — molette EQ2D, glisser-déposer
samples, poignées de découpe, rotation 3D…), historique des tirages
(`_gameHistory`, déjà borné par `GAME_HISTORY_MAX`).

Un vrai bug trouvé, directement lié au correctif de la 4e passe :

1. **Les bus d'entrée reverb/delay/ping-pong étaient connectés sans
   condition à `initFXChain()`** — appelée une seule fois par lancement du
   Flux — alors que `_gRev`/`_gDel`/`_gPP` démarrent à `false` ("attendu
   déconnecté"). Le correctif précédent (gating de l'entrée en plus de la
   sortie) ne s'appliquait donc qu'à partir du premier geste de
   l'utilisateur sur un curseur wet : entre le lancement du Flux et ce
   premier geste, le convolveur de reverb (et les tanks delay/ping-pong)
   tournaient déjà à plein régime sur le vrai signal envoyé par les paires
   (FX actif par défaut sur la plupart d'entre elles). Corrigé : ces trois
   connexions d'entrée ne sont plus établies qu'à la demande, par
   `_gateReverb`/`_gateDelay`/`_gatePP` — jamais par défaut. Vérifié : état
   des trois portes bien à `false` juste après le lancement du Flux, bascule
   propre à `true`/`false` au geste utilisateur.

## Où regarder dans le code (qualité audio adaptative + branding)

- Qualité audio : `AUDIO_QUALITY_MODE`/`AUDIO_QUALITY_TIER`, `setAudioQuality()`,
  `_armAudioWatchdog()`/`_audioWatchdogCheck()`/`_degradeAudioQuality()`,
  `_gateReverb()`/`_gateDelay()`/`_gatePP()`, `_wavePoolForDraw()`.
- Dérive mutualisée : `_connectDecorrelatedDrift()`, boucle `engine.forEach`
  dans `buildOsc()`.
- Branding : `OmcVibe/www/img/omcvibe432-logo.jpg` (splash web),
  `OmcVibe/branding/icons/` (icônes + splash natifs Android, mêmes chemins
  que l'APK d'origine), `<title>`/`document.title` dans `index.html`,
  `manifest.json`.

## 6e passe — poids de l'APK (3,6 → 10,4 Mo) : compression des images de branding

Constat (question directe de l'utilisateur) : l'APK était passé de 3,6 Mo à
10,4 Mo depuis le rebrand OmcVibe432. Diagnostic : pas le code (`index.html`
ne pèse que 561 Ko), mais les images natives Android ajoutées pour l'icône
et l'écran de démarrage — `res/drawable*/splash.png` (10 densités,
~5,9 Mo à elles seules, dont 2×1,3 Mo pour les xxxhdpi) et
`res/mipmap*/ic_launcher*.png` (15 fichiers, ~1,2 Mo) — stockées **non
compressées** dans le zip de l'APK (`ZIP_STORED`, comme c'est l'usage
Android pour les ressources raster), donc leur poids dans l'APK = leur
poids PNG brut à l'octet près.

Ces PNG étaient en couleur vraie (RGB/RGBA, jusqu'à ~270 000 couleurs
uniques par image — un mandala avec dégradés/lueurs/paillettes), donc peu
compressibles par le DEFLATE interne au PNG malgré de larges zones de fond
uni. Correctif : régénération de toutes ces images depuis la même source
(`gen_icons.py`/nouveau `gen_icons_optimized.py` + `gen_splash_optimized.py`
dans le scratchpad, même recadrage/redimensionnement qu'à l'origine) avec :

1. **Splash (10 fichiers)** : quantification à 256 couleurs avec tramage
   Floyd-Steinberg (`Image.quantize`) + recompression PNG max
   (`optimize=True, compress_level=9`). 6,02 Mo → 2,37 Mo (-3,65 Mo).
   Vérifié visuellement (comparaison des deux PNG côte à côte) : aucune
   différence perceptible, le tramage absorbe bien les dégradés du mandala.
2. **Icônes (15 fichiers)** : même quantification, mais en repartant du
   canal alpha réel de chaque fichier au lieu de le quantifier à l'aveugle.
   `ic_launcher`/`ic_launcher_foreground` ont un alpha **constant à 255**
   (entièrement opaques malgré le mode RGBA) → sauvegardés en PNG indexé
   (mode `P`, sans canal alpha du tout), ce qui élimine un canal entier de
   contenu que le PNG doit encoder — gain de loin supérieur à la seule
   quantification des couleurs (ex. le foreground xxxhdpi : 379 Ko → 130 Ko
   en `P`, contre seulement 363 Ko si on avait gardé un canal alpha
   RGBA constant). `ic_launcher_round` a un alpha **binaire** (0 ou 255,
   masque circulaire sans anti-aliasing) → PNG indexé avec un index de
   palette dédié à la transparence (`transparency=`), donc losslessly
   fidèle au masque d'origine tout en gagnant l'indexation. 1,23 Mo →
   0,42 Mo (-0,81 Mo). Vérifié : tous les PNG rouverts et validés
   (`Image.verify()`), rendu visuel du round et du foreground inchangé.
3. **Non touché** : `cosmic-flux.jpg` (fond d'écran, 250 Ko) et
   `omcvibe432-logo.jpg` (93 Ko) sont déjà des JPEG compressés — gain
   négligeable, laissés tels quels. `classes.dex` (6,8 Mo décompressé mais
   compresse très bien dans le zip) et `index.html` (561 Ko) ne sont pas du
   "poids mort", donc non concernés par cette passe.

Résultat : APK **10,4 Mo → 6,5 Mo** (-4,46 Mo, -41 %). L'écart restant avec
les 3,6 Mo d'avant le rebrand correspond au contenu réellement nouveau
(icône + splash + fond d'écran + logo du rebrand OmcVibe432), pas à du
gaspillage.

Où regarder : `OmcVibe/branding/icons/` (fichiers régénérés en place, même
chemins, `repack.py` n'a rien à changer). Les scripts de génération
(`gen_icons_optimized.py`, `gen_splash_optimized.py`) restent dans le
scratchpad de session (pas commités — dépendent d'un chemin d'upload
temporaire) ; à reproduire à l'identique si l'image source du mandala
change un jour (quantize 256 couleurs + dither Floyd-Steinberg + P/indexé
avec transparency= pour l'alpha binaire, RGB/P opaque sans canal alpha
quand l'alpha est constant à 255).

## 7e passe — #38 : le volume max était bridé sans raison (marge inutilisée)

Retour terrain : "le gain max est trop faible". Avant de toucher au code,
mesure de la marge RÉELLE en sortie (Playwright + `AnalyserNode` branché
en parallèle à plusieurs points de la chaîne : `busTrim`, `preMaster`,
`masterGlue`, `limiter`, `safetyClip`), dans un scénario délibérément
extrême — 7 paires en moteur 4 partiels ("Voix Quat"), detune Sinus Duo à
±72 ¢ partout, Δ en haut de bande (1,2 Hz), FX (reverb/delay/ping-pong) à
80% wet, volume maître à fond. Même dans ce pire cas, le signal final ne
dépassait jamais environ **-12 dBFS** — c'est-à-dire que l'app tournait à
volume "max" avec la moitié du budget de gain totalement inutilisée.

Cause : `MASTER_GAIN_SCALE = 0.5`, un plafond fixe (-6 dB, appliqué
au-dessus du curseur 0-100%) posé par prudence à une époque antérieure à
la chaîne de sécurité actuelle. Cette chaîne (`masterGlue`, compresseur
doux ratio 1.5 ; `limiter`, compresseur dur seuil -3.6dB/ratio 20:1,
attaque 6ms ; `safetyClip`, waveshaper tanh sans latence qui garantit un
plafond dur à 0.95 pleine échelle) garantit déjà "0 clipping" quel que
soit le gain injecté en amont — le plafond `MASTER_GAIN_SCALE=0.5` ne
servait donc plus qu'à brider le volume sans plus aucun bénéfice réel de
sécurité.

Corrigé : `MASTER_GAIN_SCALE` remonté à **1.0** (+6 dB). Revérifié avec le
même protocole (fichier modifié rechargé, pas juste un override en
mémoire) sur une fenêtre de mesure plus longue (9s, pour limiter le risque
de rater un pic rare parmi ~100 partiels sinusoïdaux dérivant lentement) :
le pire cas plafonne autour de **-5,5 à -7,6 dBFS** selon la fenêtre — la
variance vient de la nature statistique de la sommation de ~100 partiels
en dérive lente (micro-detune aléatoire, LFO de drift), pas d'un problème
de réglage. Marge restante confirmée : encore plusieurs dB avant tout
risque de saturation, et le filet `safetyClip` (jamais dépassé, par
construction) absorbe en douceur les rares dépassements sans "clac".
En usage normal (config par défaut, rien de forcé), le gain de volume
perçu (RMS) est de **+7,4 dB** — un doublement de perception de volume,
sans distorsion additionnelle mesurée.

Où regarder : `MASTER_GAIN_SCALE` (const, section config, près de
`masterVol`), chaîne `preMaster→masterGlue→masterFader→masterHPF→
masterLowCut→limiter→safetyClip→destination` dans `initFXChain()`.

## 8e passe — 4 nouvelles consignes (dock, immersion, fréquence maître, sphères)

### #39 — Bug : la cellule "+" du dock grossissait au lieu d'ouvrir l'assignation rapide

Retour terrain : un appui long sur une sphère raccourci du dock (le "+")
faisait grossir/déformer sa cellule au lieu d'ouvrir le panneau
d'assignation rapide (#22). Cause : `_bindExtraDraggable()` détachait la
sphère de son flux flexbox (`position:fixed`, sortie du `.dcell-wrap`)
dès que le seuil d'appui long (480ms) était atteint — AVANT même de
savoir si l'utilisateur comptait vraiment glisser ou juste relâcher sur
place. Ce détachement fait instantanément grossir/reflow la cellule
vidée de son contenu (comportement normal du flexbox), et le panneau
d'assignation ne s'ouvrait qu'au relâchement — la sphère restait
détachée, flottante, la cellule vide. Corrigé : le détachement n'a
maintenant lieu qu'au premier vrai mouvement de glisser APRÈS le seuil
(nouvel état interne `holdReady`) ; un appui long immobile ouvre
l'assignation directement, sans jamais toucher au flux ni faire bouger
la cellule. Vérifié (Playwright) : `position`/`parentElement` de la
sphère restent inchangés (`static`, dans son `.dcell-wrap`) pendant tout
un appui long immobile jusqu'à l'ouverture du panneau ; le glisser réel
(appui long + déplacement) détache toujours correctement comme avant.

### #33 — Immersion plein écran : limite technique (non réalisable)

Demande : masquer aussi la barre de navigation Android en plein écran
(actuellement seule la Fullscreen API web est utilisée, qui ne cache que
le navigateur/contenu, pas les boutons système). Investigué :
`classes.dex` contient `androidx.activity.EdgeToEdge` (dessine sous les
barres système, ne les masque pas) mais aucun appel actif à
`WindowInsetsControllerCompat.hide(...)` (le vrai mécanisme d'immersive
mode). Le repo ne contient aucune source Android (Java/Kotlin,
AndroidManifest.xml) — seulement l'APK pré-construit, repackagé au
niveau fichiers (substitution de ressources) via un pipeline sans SDK
Android/aapt2. Masquer la barre de navigation nécessiterait de
recompiler `resources.arsc` et/ou `classes.dex`, hors de portée de ce
pipeline sans risquer de casser l'app. **Non implémenté** — signalé
explicitement plutôt que de simuler un correctif inopérant.

### #40 — Nouvelle cellule VIOLETTE : saisie directe de la fréquence maître

Nouvelle cellule sous la sphère Flux (on/off), même thème visuel que les
5 cellules existantes (fond translucide + bordure teintée,
`backdrop-filter`). Contenu, en une seule ligne compacte pour tenir dans
l'espace très contraint du dock : `[−9] [saisie] [+9] [✓ Valider]`. `✓`
applique la valeur tapée (`setMasterFreq`, retune en direct si le flux
joue) ; `−9`/`+9` appliquent immédiatement un pas de 9 Hz. Bornée à
[F_MIN, 432], entrée vide/invalide sans effet (pas de crash).

Contrainte découverte en implémentant : `#bottom-dock` a une hauteur
**fixe** (115px desktop / 120px mobile, pas auto), donc empiler une
sphère Flux (95px) + une nouvelle cellule dessous dépassait largement ce
budget — la cellule débordait sous l'écran ET chevauchait les colonnes
voisines (une largeur fixe de 150px ne rentrait pas dans les ~119px
réellement alloués à la colonne centrale par flexbox). Corrigé : cellule
en une seule ligne (pas deux) avec `width:100%` (épouse sa colonne, ne la
dépasse jamais) au lieu d'une largeur fixe ; marge `margin-bottom` de
Flux réduite (33→8px desktop, 38→8px mobile, la nouvelle cellule reprend
le rôle d'"élévation" visuelle) ; hauteur du dock relevée (115→148px
desktop, 120→158px mobile, et toutes les références `calc()`/`bottom`/
`translateY` associées mises à jour en conséquence). Vérifié
(Playwright, viewport 393×851) : cellule entièrement dans l'écran,
aucun chevauchement avec les 4 autres cellules, tous les cas
fonctionnels (pas, validation, bornage, entrée vide, retune en direct)
corrects.

### #41 — Refonte des raccourcis tactiles des sphères oscillateurs

Sphère **MAÎTRE** : inchangée (tap court = menu rapide, appui long =
random global).

Sphères **satellites** — permutées :
- **Tap court** = mute/unmute Pingala+Ida **ensemble**, d'un coup
  (`toggleMutePair`, nouvelle fonction). Remplace l'ouverture du menu
  rapide sur tap court. En conséquence, les 2 boutons séparés
  `pmute-`/`imute-` du panneau détaillé de l'oscillateur (mute Pingala
  seul / mute Ida seul) sont retirés — redondants avec ce nouveau
  raccourci unifié (les fonctions `toggleMuteP`/`toggleMuteI` associées
  sont supprimées).
- **Appui long** = ouvre le menu rapide (remplace l'ancien random ciblé
  sur cette seule paire, `_pairRandomFreq`, qui n'a pas disparu — il est
  déplacé, cf. ci-dessous).

Menu rapide — simplifié :
- Arcs Volume et Detune **retirés** (ils restaient un gros widget
  circulaire coûteux en place ; ils restent réglables "en interne", dans
  le panneau complet de l'oscillateur — volume `pvol-`/`ivol-` et
  detune Sinus Duo y sont toujours présents et fonctionnels).
- Ne reste que 2 cellules (Random ratio, Verrou) + le centre (ouvre le
  panneau complet) — un widget bien plus petit qu'avant.
- Le bouton **Verrou** gagne un appui long (`_bindTapHold`, 420ms) :
  déclenche un random ciblé sur CETTE sphère (`_pairRandomFreq` pour une
  satellite, `triggerMagicAuto()` pour le maître) — reprend exactement
  l'ancien comportement de l'appui long sur la sphère elle-même, qui a dû
  être libéré pour le nouveau mute/unmute. Le verrou continue de
  bloquer ce random comme avant (`isLocked` vérifié dans
  `_pairRandomFreq`).
- Géométrie resserrée au maximum : `.vp-mastermenu` passe de `inset:-1%`
  à `inset:0` (ne dépasse plus JAMAIS le cadre de sa propre sphère,
  quelle que soit la densité de la couronne de satellites) ; les 2
  cellules sont tirées vers l'intérieur du cadre (`top`/`bottom`
  positifs au lieu de négatifs) et légèrement réduites (64×56→56×48px).

Vérifié (Playwright, viewport 393×851, mode avancé/6 satellites) :
mute/unmute ensemble sur tap court (les 2 oscillateurs, dans les 2 sens) ;
appui long ouvre bien le menu rapide sans toucher à la fréquence ; le
menu rapide ne contient plus ni arc ni référence aux anciens éléments
(grep sur tout le fichier) ; le verrou bloque bien le random tant qu'il
est actif et le débloque une fois désactivé ; comportement de la sphère
maître intégralement inchangé ; les boutons mute internes ont disparu du
panneau détaillé, volume/detune toujours présents et fonctionnels ;
**zéro chevauchement** entre le menu rapide et les sphères voisines,
testé sur les 6 positions satellites une par une.

Où regarder : `toggleMutePair()`, `_vpStart()`/`_vpEnd()` (section
"04-vesica-ui.js"), `_openQuickMenu()`/`_mmLockLongPress()`,
`buildPairHTML()` (panneau détaillé, boutons mute retirés).

## 9e passe — 10 réglages fins (trémolo, EQ, volumes, menu rapide, matrice, couleurs)

### #42 — Trémolo retiré, respiration organique exposée

`LFO_STATE` (toggle "LFO — Trémolo organique", désactivé par défaut,
modulation sèche/mécanique) supprimé entièrement — code (`lfoToggle`,
`lfoSet`, `_lfoNode`, `_lfoDepthGain`) et UI. `BREATH_STATE` (respiration
organique, actif par défaut, `on:true, rate:0.11, depth:0.18`) existait
déjà côté moteur audio (chaîne `masterGain→_lfoGain→_breathGain→_fxInput`)
mais n'avait jamais eu de contrôle visible — il prend maintenant la même
place dans le panneau FX (`breathToggle`/`breathSet`, ids `gbreath-*` pour
éviter la collision avec `sv-breath-depth`, déjà pris par la respiration
PAR PAIRE dans le panneau Options Aléatoire, un réglage différent).
`_lfoGain` conservé tel quel : il sert uniquement de cible pour
l'anti-crack (`_applyAntiCrack`), indépendant du trémolo retiré.
Vérifié : `LFO_STATE`/`lfoToggle` bien supprimés, `BREATH_STATE.on` actif
par défaut, `breathToggle`/`breathSet` pilotent bien le gain réel du node.

### #43 — EQ Master : 3 nœuds libres → 6 bandes fixes (36-432 Hz)

L'"EQ Paramétrique 2D" (3 filtres `eqLow/eqMid/eqHigh`, fréquence ET gain
librement déplaçables à la souris) remplacé par un EQ graphique classique
à **6 bandes fixes** : 54 / 144 / 216 / 288 / 360 / 432 Hz (lowshelf,
peaking×4, highshelf), couleurs reprises de la palette des 6 paires. Seul
le gain se règle désormais (glisser verticalement, la fréquence de chaque
bande ne bouge plus) — `findBand()` sélectionne la bande la plus proche en
X avec une tolérance large (comme un vrai fader), la coordonnée Y n'entre
plus dans le choix de la bande. Nettoyage au passage : `mEqLow/mEqMid/
mEqHigh` et `setMasterEQ()`, du code mort jamais connecté au graphe audio
depuis leur création, supprimés. Format de sauvegarde (`getFXState`)
simplifié en tableau de 6 gains ; un ancien preset 3-bandes est ignoré
proprement sans faire planter la restauration du reste. Vérifié : 6 nœuds
`BiquadFilterNode` créés aux bonnes fréquences, application/reset/
sauvegarde/restauration du gain par bande, rendu visuel du fader.

### #44 — Volumes oscillateurs : affichage 0-1 + défauts par bande de fréquence

Sliders `pvol-`/`ivol-` : plage interne réelle (0-0.45, le gain appliqué
au node) inchangée, mais **affichée/saisie en 0-1** ("contraction max /
expansion max" demandé) — remap pur à la frontière UI
(`setVolP`/`setVolI` multiplient par `VOL_UI_SCALE=0.45`), aucune
amplification (vérifié : slider à 1.0 ⇒ gain réel 0.45, identique à
l'ancien maximum). Valeurs par défaut recalculées par bande de fréquence
de la paire (grave = priorité, demande explicite) : 36-108 Hz → 72%,
108-216 Hz → 66%, 216-324 Hz → 54%, 324-432 Hz → 36% du volume UI max —
appliqué à la déclaration initiale de `PAIRS` et dans `resetAll()` (calcul
live selon la fréquence réellement atteinte après réassignation
ratio/n, pas une valeur figée). Vérifié : les 7 défauts correspondent
exactement à leur bande attendue, `resetAll()` recalcule correctement
même avec des fréquences dépassant 432 Hz (plafonnées avant le calcul de
tier).

### #45/#46 — Menu rapide : boutons Solo (gauche) et FX on/off (droite)

Le menu rapide (2 cellules haut/bas déjà en place) gagne 2 cellules
gauche/droite : **Solo** (`soloPair(i)`, généralisation de `soloMaster()`
à n'importe quelle sphère — coupe tout sauf celle-ci, retap = restaure
tout le monde, état interrogé plutôt que mémorisé séparément) et **FX
on/off** (réutilise `setPairFX`/`isPairFX`, déjà existants). Avec 4
cellules au lieu de 2, l'ancien resserrement (#41, `top:6%`/`bottom:6%`,
cellules 56×48) chevauchait les sphères voisines sur les côtés gauche/
droite (la couronne hexagonale de satellites est plus dense
latéralement que radialement) — resserré à nouveau (cellules 42×36,
offsets 14%/18%) jusqu'à zéro chevauchement mesuré sur les 6 positions.

### #47/#48 — Contrôle n enrichi (+/-0.1, fader, plafond 432 Hz) + Matrice 🌀

Le contrôle `n` (panneau détaillé, onglet Binaural) gagne des boutons
+/-0.1, un fader précis (pas 0.1, max dynamique affiché), et un bouton
🌀 ouvrant directement le nouvel onglet **⑤ Matrice**. `setN()` plafonne
désormais `n` pour que `masterFreq × ratio × n` ne dépasse jamais 432 Hz
(`_nMaxFor(i)`, recalculé à chaque changement de ratio/fréquence maître
via `updatePairUI`). La Matrice affiche 36 cellules : les 6 ratios de
`RATIO_OPTS` (il n'y en a que 6 au total, donc "6 ratios" = tous, pas de
sélection à faire) × 6 valeurs de `n` aléatoires chacun (plafonnées à
432 Hz, mises en cache par paire pour ne pas sauter à chaque re-rendu),
+ un bouton "Nouveau tirage" qui régénère les 36 valeurs. Tap une cellule
= assigne ratio+n à cette sphère instantanément (retune en direct si le
flux joue). Vérifié : 36 cellules (6×6), toutes sous le plafond 432 Hz,
assignation + retune en direct corrects, plafonnage du fader/stepper.

### #49 — Fond du panneau oscillateur : dégradé harmonieux par paire

`pair.grad` (dégradé 2 couleurs déjà défini dans `PAIRS` pour chacune des
7 sphères, jusqu'ici jamais utilisé) appliqué en fond du panneau détaillé
(`#osc-modal-inner`, style inline posé par `openOscModal()`) — dégradé
doux et à faible opacité sur une base sombre neutre (jamais noir pur),
propre à la couleur de CHAQUE oscillateur. Vérifié visuellement sur 3
paires (rouge, cyan, rose du maître) : teinte bien visible et distincte,
texte toujours lisible.

### #50 — Correctif #49 : il restait du noir (retour direct de l'utilisateur)

Retour terrain sur le point #49 : *"ok sauf que le fond noir doit
disparaitre complètement"*. Diagnostic : le point d'ancrage sombre du
dégradé était une couleur fixe `rgba(14,6,30,.95)` (violet très foncé,
visuellement quasi-noir), et plusieurs boîtes imbriquées dans le panneau
(Pingala, Ida, ligne "LFO doux", `.block-formula`) utilisaient encore un
noir semi-transparent (`rgba(0,0,0,X)`) posé par-dessus — donc du noir
réapparaissait localement même là où le fond du panneau était coloré.

Correctif :
- Nouvelle fonction `_darkenHex(hex, factor)` : assombrit une couleur hex
  tout en préservant sa teinte (jamais vers le neutre — planchers non nuls
  sur chaque canal RGB), utilisée pour calculer les 2 points d'ancrage
  sombres du dégradé à partir des 2 couleurs propres de `pair.grad`, au
  lieu d'une teinte violette générique fixe.
- Boîtes Pingala / Ida / "LFO doux" : fond recalculé à partir de la couleur
  de la paire (`${c}26` / `${c}1a` / `${c}1c`) au lieu de noir semi-transparent.
- `.block-formula` (classe CSS partagée) : fond passé à
  `color-mix(in srgb, var(--ec,#888) 20%, transparent)`, teinté par la
  couleur de la paire courante via la variable CSS déjà posée sur
  `.pair-panel`.
- Vérifié par script Playwright : le fond calculé (`getComputedStyle`) sur
  3 paires distinctes (rouge, vert, violet) donne des couleurs sombres mais
  clairement teintées (ex. `rgb(66, 28, 28)`, `rgb(22, 66, 46)`,
  `rgb(66, 46, 66)`) — plus aucune valeur proche du noir neutre.

Où regarder (9e passe) : `breathToggle`/`breathSet` (section 2.6),
`EQ_BANDS`/`eqBands`/`_eqApply`/`initEQ2D` (section "EQ Graphique"),
`VOL_UI_SCALE`/`_defaultVolUIForFreq`/`setVolP`/`setVolI`, `soloPair`/
`_mmToggleSolo`/`_mmToggleFX` (section quick menu), `_nMaxFor`/`setN`/
`stepN`, `_matrixHTML`/`_matrixAssign`/`_matrixReshuffle`/`openMatrixTab`,
`openOscModal()`/`_darkenHex()` (fond dégradé, jamais noir).

## 10e passe — retours terrain (Solo/All, dock, filtre, onde par défaut, LFO Δ)

### #51 — Bug : boutons Solo/FX du menu rapide injoignables au toucher

Retour utilisateur (capture d'écran) : le bouton Solo/All du menu rapide
"ne fonctionne pas". Cause réelle (pas l'hypothèse de l'utilisateur sur le
tap long) : la sphère MAÎTRE (`#vp+MASTER_IDX`) est toujours ajoutée en
DERNIER dans le DOM par `buildVesicaPairs()`, donc sans z-index explicite
elle peint PAR-DESSUS toutes les satellites — y compris le menu rapide
ouvert sur une satellite, dont les cellules gauche/droite (Solo, FX,
ajoutées en #45/#46) se retrouvaient sous la zone cliquable du maître.
Confirmé par un test Playwright utilisant un VRAI clic DOM (pas un appel
direct de fonction) : Playwright rapportait explicitement
`<div id="vp6" class="vp-center-wrap"> intercepts pointer events`.

Correctif : `_openQuickMenu(i)` pose un `z-index:60` inline sur la sphère
qui possède le menu ouvert (au-dessus de tout le reste) ; `_closeQuickMenu()`
le retire de toutes les sphères à la fermeture. Revérifié par le même test
avec 2 clics DOM réels consécutifs : les deux bascules Solo↔All
fonctionnent, le menu reste ouvert.

### #52 — Dock : sphères -36%, cellules +36%, retrait du bouton "Valider" violet

Retour utilisateur : "les cellules du menu bas sont trop petites, on ne
voit même pas le texte entier" (captures : "Option ...", "Effet A...",
"Param..." tronqués).

- `.dcell-extra` (sphères raccourci rouge/orange/vert/bleu) et `.dcell-flux`
  (sphère Flux on/off) réduites de 36% (53px→34px, 95px→61px, desktop et
  mobile).
- `.dcell` (les 4 cellules) agrandie de 36% (43px→58px / 45px→61px selon
  breakpoint), polices `.dcell-name`/`.dcell-sub` +36%.
- Simplement agrandir la police ne suffisait pas (le mot restait tronqué
  en un seul, ex. "Paramètres") : `.dcell-name` passe de
  `white-space:nowrap` + ellipsis à un retour à la ligne normal
  (`white-space:normal`) avec `overflow-wrap:break-word` pour couper un
  mot long si besoin — la cellule est maintenant assez haute pour 2 lignes.
- Cellule violette agrandie de 36% également (`.dcell-purple`/`-step`/
  `-input`, max-width 150px→204px).
- Bouton "✓ Valider" retiré (HTML + CSS `.dcell-purple-validate` +
  fonction `_purpleFlashValidate`) : Entrée (clavier) validait déjà la
  saisie via `_purpleFreqValidate`, les pas −9/+9 s'appliquent
  immédiatement — ce bouton ne servait plus. `onblur` ajouté sur le champ
  pour appliquer aussi la saisie tapée si l'utilisateur tape ailleurs sans
  passer par Entrée (clavier mobile).
- Vérifié géométriquement (Playwright) : dock 158px de haut, toutes les
  cellules/sphères tiennent dans les bornes sans chevauchement ; vérifié
  visuellement : "Option Jeu / Aléatoire", "Effet Audio", "Param/ètres"
  s'affichent en entier.

### #53 — Filtre oscillateur : Cutoff/Résonance figé → HPF/BPF/LPF commutable

Demande : réutiliser le filtre HPF/LPF/BPF déjà existant pour les samples
(`FILTER_TYPES`/`sampleSetFilterMode`) et le brancher sur le filtre de
chaque oscillateur — un remplacement, pas un changement audio.

Le nœud `flt` (BiquadFilterNode) existait déjà dans la chaîne FX de chaque
oscillateur mais avait `.type` figé à `'lowpass'`. Il prend maintenant
`FILTER_TYPES[mode]` avec `mode` par défaut absent → `'lowpass'` : audio
strictement identique tant que l'utilisateur ne touche pas au nouveau
sélecteur. Nouveau bloc de 4 boutons OFF/HPF/BPF/LPF (`.osc-filt-mode-btn`,
même style que `.filt-mode-btn` des samples mais classe séparée pour ne
pas interférer avec les boutons globaux du panneau samples) au-dessus des
curseurs Cutoff/Résonance existants (masqués sur OFF, comme pour le
filtre samples). `setPairFilterMode(i, mode)` met à jour le `.type` des
nœuds vivants ET la persistance (`OSC_FILTER[id].mode`, inclus
automatiquement dans les snapshots JSON existants) ; les 2 points de
restauration (chargement d'état, annulation d'un tirage) ont été corrigés
pour réappliquer aussi `.type` au nœud vivant (ils ne réappliquaient avant
que cutoff/résonance, jamais le type — un oubli existant, pas introduit
ici, mais qui n'avait aucune conséquence tant que le type ne pouvait pas
changer).

### #54 — Onde par défaut = double sinusoïdale, détune à 0

`OSC_WAVES` est maintenant pré-rempli à `'sine2'` (≈≈ Sin×2) pour les 14
oscillateurs au chargement, et `DUALSINE_DETUNE` par défaut passe de 7 ¢
à 0 ¢ (quasi mono/pur, l'utilisateur ouvre l'écart lui-même).

Effet de bord détecté et corrigé pendant le test : le signe de chaque
voix du Sinus Duo (`_dualSign`, ±1) était jusqu'ici calculé via
`Math.sign(baseDetune)` — qui dégénère à `0`/`-0` (donc **faux**, la
fonction `_applyDualSineDetune` ignore alors silencieusement la voix)
quand le détune de la paire vaut `0` à la construction de l'oscillateur.
Comme le nouveau défaut est justement `0`, cela aurait rendu le curseur
Détune inopérant sur toute paire fraîchement créée : le déplacer n'aurait
plus eu aucun effet audible. Corrigé en dérivant `_dualSign` de la
POSITION structurelle du partiel dans `sine2Engine()` (qui renvoie
toujours `[voix "−", voix "+"]` dans cet ordre, quelle que soit la valeur
du détune) plutôt que du signe de sa valeur courante — `_dualSign` vaut
maintenant toujours ±1, jamais 0, pour les voix du moteur Sin×2.

### #55 — Nouveau LFO d'intensité du détune Sinus Duo (par paire, OFF par défaut)

Nouveau bouton "LFO Δ · intensité du détune" dans l'onglet ② Oscillo,
sous le curseur Détune. Même principe que le LFO doux existant
(`attachOscVolLFO`, §2.10) mais appliqué au `.detune` de chaque voix du
moteur Sin×2 au lieu du gain : un `OscillatorNode` lent (0,05–0,23 Hz,
tirage aléatoire à chaque activation) connecté à 2 `GainNode` (un par
voix, signe ±1 via `_dualSign`) eux-mêmes connectés en ADDITIF sur
`osc.detune` — la base réglée au curseur n'est jamais touchée, seule une
respiration douce (±10 ¢) s'ajoute par-dessus. Nettoyage ajouté dans
`releaseOsc()` (même fuite déjà corrigée une fois pour le LFO de volume :
sans arrêt explicite, le LFO continue de tourner indéfiniment après la
destruction de l'oscillateur qu'il modulait) et dans le teardown de
`stopFlow()`. OFF par défaut, par paire.

Où regarder (10e passe) : `_openQuickMenu`/`_closeQuickMenu` (z-index),
`.dcell`/`.dcell-extra`/`.dcell-flux`/`.dcell-purple*` (dock), `flt.type`/
`setPairFilterMode`/`_setOscFilterMode` (filtre oscillateur), `OSC_WAVES`
pré-remplissage + `DUALSINE_DETUNE` (onde par défaut), `_dualSign`
(buildOsc), `PAIR_DUALSINE_LFO`/`_attachPairDualSineLFO`/
`togglePairDualSineLFO` (LFO Δ).

## 11e passe — module tactile ADSR/Cutoff-Rés + BD-2 + automations (point 4)

Avant de coder : avis objectif donné à l'utilisateur sur la faisabilité
(canevas tactile déjà présent dans l'app — EQ graphique, Matrice —, moteur
de saturation déjà présent — Grain analogique —, mais 2 contraintes réelles
signalées : un vrai ADSR 4 étages n'a rien à piloter sur un drone continu
(l'app n'a que Attaque/Relâche, `OSC_ENV`), et un overdrive appliqué à des
sinus pures va à l'encontre d'un principe déjà écrit dans le code ("une
sinus pure doit rester PURE"). L'utilisateur a validé : pad 2 paramètres
(pas de vrai ADSR), BD-2 expérimental accepté, off par défaut dans tous les
cas.

### #56 — Pad tactile partagé Cutoff/Résonance ⇄ Attaque/Relâche

Nouveau canvas (`tpad-${i}`, onglet ③ FX) avec 2 onglets commutables. Un
seul point XY glissé (`initTPad`/`drawTPad`/`_tpadApply`, même schéma
d'interaction que `initEQ2D` : mousedown/touchstart → drag → mouseup/
touchend) :
- Mode **Cutoff/Rés.** : X → cutoff (échelle log 60–6000 Hz, mêmes bornes
  que l'ancien curseur), Y → résonance (0–6). Appelle `setPairFilter`
  (fonction déjà existante, aucune nouvelle automation audio).
- Mode **Attaque/Relâche** : X → attaque (0–4s), Y → relâche (0–6s).
  Appelle `setPairEnv` (déjà existante).

Le sélecteur de TYPE de filtre (OFF/HPF/BPF/LPF, #53) reste dans l'onglet
② Oscillo — c'est un choix de topologie, pas une valeur continue, il ne
fait pas partie du pad.

### #57 — Color FX BD-2 (Blues Driver), expérimental, par paire, OFF par défaut

Étage optionnel `WaveShaperNode` (courbe soft-clip asymétrique
`_bd2Curve`, façon overdrive à lampe) + filtre de tonalité (`peaking`
1500 Hz) + gain de sortie, inséré entre le panner et le coupe-bas/filtre
UNIQUEMENT si activé pour la paire (même stratégie que Grain analogique :
nœuds construits seulement si nécessaire, `rebuildAllOscs()` au
basculement on/off pour insérer/retirer proprement). **Drive et Niveau
plafonnés en dur à 0.5 dans `setPairBD2`** (pas juste visuellement dans
l'UI) — un dépassement de la borne UI ne peut pas dépasser le plafond
réel. OFF par défaut, par paire. Persisté dans les presets FX
(`PAIR_BD2`, `getFXState`/`applyFXState`).

### #58 — Onglet automations rapides (9/18/36/74/144s)

Toggle + sélecteur de période par paire (`PAIR_TPAD_AUTO`). Cible le mode
actif du pad :
- Mode Cutoff/Rés. : balayage doux vers une cible aléatoire proche à
  chaque tick (`setPairFilter`).
- Mode Attaque/Relâche : swell périodique (mute doux → remontée), utilise
  les temps Attaque/Relâche RÉELLEMENT réglés au pad pour la forme du
  swell.

Simple `setInterval` (pas de nœud audio-rate) : aux échelles de temps
demandées (9 à 144s), la précision audio-thread n'apporte rien. Arrêté
proprement dans le teardown de `stopFlow()` (même précédent que les autres
LFO/timers de l'app). OFF par défaut.

### #59 — Nettoyage : anciens curseurs Cutoff/Résonance/Amp EG retirés

Une fois le pad tactile vérifié (tests Playwright : drag réel vers les 4
coins du canvas, valeurs `OSC_FILTER`/`OSC_ENV` correctement appliquées),
les curseurs linéaires devenus redondants sont retirés de l'onglet
② Oscillo, remplacés par un simple renvoi vers l'onglet ③ FX. Fait
volontairement EN DERNIER, jamais avant que le remplaçant tactile soit
vérifié fonctionnel.

Où regarder (11e passe) : `initTPad`/`drawTPad`/`_tpadApply`/
`setPairTPadMode` (pad tactile), `PAIR_BD2`/`_bd2Curve`/`togglePairBD2`/
`setPairBD2` (BD-2), `PAIR_TPAD_AUTO`/`_tpadAutoTick`/`toggleTPadAuto`/
`setTPadAutoPeriod` (automations).

## 12e passe — #60 : effet "echo tunnel" permanent (retour terrain enceintes)

Retour utilisateur après écoute sur enceintes réelles : "il y a un effet
echo tunnel activé en permanence qui dénature complètement le son".

Cause trouvée dans `triggerMagicAuto()` : un "plancher de reverbe
ambiante" forçait le curseur Reverbe à 12% dès le premier tirage aléatoire
si sa valeur était sous 5% — et ne redescendait plus jamais tout seul.
Combiné à l'envoi par paire vers la reverbe selon sa "profondeur 3D"
(jusqu'à 30% du signal par paire, `drawDepth[i] * FX_SEND_LEVEL`), ce
plancher rendait une reverbe permanente et cumulative dès qu'un jeu
aléatoire avait été lancé une seule fois — perçue comme un echo/tunnel
qui ne s'arrête plus. Ce comportement était volontaire à l'origine (rendre
la profondeur 3D audible), mais le retour direct de l'utilisateur montre
qu'il dénature le son plus qu'il ne l'enrichit.

Correctif : le bloc qui forçait `reverbWet` à 0.12 est retiré. Le curseur
Reverbe reste fidèle à ce qu'il affiche (0% par défaut) tant que
l'utilisateur ne le monte pas lui-même. Le mécanisme d'envoi par
profondeur 3D reste en place (inoffensif tant que la reverbe est à 0) —
seul le forçage automatique disparaît. Vérifié par script Playwright : 5
tirages aléatoires consécutifs, `reverbWetGain.gain.value` reste à 0 tout
du long ; remonter le curseur manuellement fonctionne toujours normalement.

Où regarder : `triggerMagicAuto()` (le bloc "plancher de reverbe" a été
retiré, juste avant la boucle qui applique volumes/pan/filtres/envoi FX
par paire).

## 13e passe — #61 : audit complet des incohérences "en fond" (base propre)

Demande explicite : auditer TOUT l'APK pour tout ce qui tourne en fond
sans interaction directe avec le réglage concerné — pour chaque trouvaille,
quoi/source/conséquence/action.

**Corrigé :**
- **Détune Sinus Duo re-tiré au hasard à chaque tirage, sans option.**
  Source : `triggerMagicAuto()`, `PAIR_DUALSINE[idx] = Math.round((-72 +
  Math.random()*144)*10)/10;`, inconditionnel. Conséquence : écrasait le
  "détune à 0 par défaut" (#54) dès le premier tirage aléatoire — l'action
  la plus utilisée de l'app — pour toutes les paires. Fix : nouveau
  `RAND_OPTS.randomDualSine` (OFF par défaut, même précédent que
  `filterLFO`), le tirage ne touche plus au détune sauf activation
  explicite.
- **Le filtre par oscillateur (type + résonance) était écrasé en entier à
  chaque tirage.** Source : `triggerMagicAuto()`,
  `OSC_FILTER[pair.pingala.id] = { cutoff, res: 0.707, hp: 20 };`.
  Conséquence : le type (OFF/HPF/BPF/LPF, #53) et la résonance réglés au
  pad tactile (#56) étaient perdus silencieusement, sans que l'utilisateur
  n'ait touché ni l'un ni l'autre — invisible jusqu'à la prochaine
  reconstruction d'oscillateur, qui revenait alors en passe-bas standard.
  Fix : ne modifie plus que `cutoff` (seul paramètre réellement lié à la
  profondeur 3D), préserve `.mode`/`.res` existants.
  **Effet de bord découvert en testant ce fix** : `_setOscFilterMode`
  (ligne définissant le fallback `{cutoff:6000,res:0.707}`) ne posait
  jamais `.hp` — sans le forçage total qu'avait l'ancien code, `.hp`
  pouvait rester `undefined`, et `setOscHPF` plantait
  (`setTargetAtTime` sur une valeur non-finie). Réparé aux deux endroits
  (repli défensif `res`/`hp` dans le merge du tirage, `.hp` ajouté au
  fallback de `_setOscFilterMode`).
- **Code mort : `_applySeuilProtect()` / `isAboveSeuil()`.** Fonction
  "Protection seuil 360Hz" complète, jamais appelée nulle part (confirmé
  par recherche exhaustive) — le commentaire d'origine le disait déjà :
  "protection auto désactivée v2.x". Supprimée entièrement (fonction +
  helper `isAboveSeuil` devenu orphelin).

**Recensés, actifs par défaut, documentés et intentionnels (non touchés,
laissés au choix de l'utilisateur)** : `BREATH_STATE` (respiration
organique master, on/0.11Hz/18%), `DELTA_DRIFT_STATE` (dérive lente du Δ,
on/±0.06Hz sur 3-7min), `RAND_OPTS.breathRandom` (respiration aléatoire
par paire à chaque tirage, togglable dans Options Aléatoire),
`masterLowCut` (-5dB shelf @120Hz, anti-pompage) + micro-désaccord
aléatoire ±1.2¢ par voix (anti-addition-constructive, buildOsc),
`_applyAntiCrack` (réduction de gain automatique jusqu'à -72% selon le
nombre d'oscillateurs actifs, anti-saturation).

Vérifié par script Playwright : réglage manuel (mode HPF, résonance 4.2)
posé sur une paire, 6 tirages aléatoires consécutifs, mode et résonance
restent identiques (`hpf`/`4.2`) tout du long, aucune erreur JS ; le
détune Sinus Duo reste à sa valeur initiale par défaut à travers les 6
tirages, puis se randomise correctement une fois `randomDualSine` activé
manuellement.

Où regarder : `RAND_OPTS.randomDualSine` (déclaration + garde dans
`triggerMagicAuto()`), le bloc `OSC_FILTER` fusionné (au lieu de remplacé)
dans `triggerMagicAuto()`, `_setOscFilterMode` (fallback `hp` ajouté).

## 14e passe — #62/#63 : gains ramenés, diagnostic "tirage sans interaction"

Retour terrain : un nouveau jeu aléatoire apparaîtrait spontanément après
~2min en solo maître, sans aucun tap (vérifié plusieurs fois par
l'utilisateur). Plus, gains jugés trop forts, mauvaise qualité perçue.

### #62 — Gains ramenés (volume global 70%, oscillateurs plats à 30%)

- `masterVol` par défaut : 0.85 → 0.70 (variable JS, `resetAll()`,
  curseur HTML `#mvol-slider`/`#mvol-val`, cohérents partout).
- `_defaultVolUIForFreq()` : ne dépend plus de la bande de fréquence
  (72/66/54/36%, #44) — retourne un plateau unique de 30% pour comparaison.
  `PAIRS[].pingala.vol`/`.ida.vol` mis à jour en conséquence
  (0.30 × VOL_UI_SCALE = 0.135) pour les 7 paires. `resetAll()` en hérite
  automatiquement. La logique par bande reste dans le code (fonction
  simplifiée) si l'utilisateur veut y revenir après comparaison.
- Onde par défaut Sin×2 (#54) reconfirmée intacte — aucune régression
  depuis, `OSC_WAVES` toujours pré-rempli pour les 14 oscillateurs.

### #63 — Recherche du "tirage aléatoire sans interaction" : audit exhaustif + instrumentation

Audit statique complet, sans trouver de cause JS/minuteur :
- **Les 6 `setInterval()` de toute l'app** passés en revue un par un :
  heartbeat `AudioContext.resume()` (12s), watchdog CPU (3s, ne fait que
  dégrader la qualité si tension soutenue), ancre respiratoire (240s,
  OFF par défaut ET ne fait que moduler du volume, jamais un nouveau
  tirage), automation du pad tactile (#58, par paire, OFF par défaut,
  ne touche que cutoff/enveloppe), barre de progression, infobar (1s,
  affichage seul). Aucun n'appelle `triggerMagicAuto()`.
- **Les 10 points d'appel de `triggerMagicAuto()`** passés en revue un par
  un : bouton "⚄ Omchaléatoire", appui long sur une sphère, bouton
  "Random ratio" du menu rapide, verrou maître, jeu aléatoire depuis un
  sample, etc. — tous derrière un `onclick`/gestionnaire d'appui
  long/tap direct, aucun déclenché par un minuteur.
- **MediaSession** (contrôles écran verrouillé/casque Bluetooth) :
  seuls `play`/`pause`/`stop` sont branchés, rien qui appelle
  `triggerMagicAuto()`.
- Rien d'anormal trouvé qui explique un déclenchement après ~2min sans
  interaction — le bug ne se reproduit pas dans l'environnement de test
  (headless, sans attente réelle de 2min sur device réel).

**Instrumentation ajoutée** (`_diagLogAutoTrigger`) : un minuteur global
`_lastUserGestureAt`, mis à jour par un écouteur en phase de capture sur
`pointerdown`/`touchstart`/`mousedown` posé une seule fois sur
`document`. À CHAQUE appel de `triggerMagicAuto()`, si plus de 1,5s se
sont écoulées depuis le dernier geste tactile/souris réel, l'appel est
journalisé : horodatage, écart en ms, pile d'appel JS complète — dans
`localStorage.omc_diag_autotrigger` (8 dernières entrées) ET affiché
immédiatement à l'écran via le bandeau d'état existant (`ui('live', ...)`)
pour une confirmation visuelle instantanée si ça se reproduit. Vérifié par
script Playwright : un vrai geste juste avant l'appel → rien loggé ; un
appel simulé après 5s d'inactivité → loggé avec pile d'appel complète.

Où regarder : `_defaultVolUIForFreq()`/`PAIRS[]`/`masterVol` (#62),
`_diagLogAutoTrigger`/`_lastUserGestureAt` juste avant
`triggerMagicAuto()` (#63).

## 15e passe — #64 : univers à 3 modules (lanceur, icône, Chaharmony)

Demande : transformer l'APK en 3 modules choisis au lancement — OmchaVibe
432 (le jeu actuel), Chaharmony (générateur de fréquences), OmchaSphere /
Flux (visualisation géométrique). Icône remplacée par le mandala fourni.

### Icône

`branding/icons/` régénéré depuis la nouvelle image (mandala doré) via
un script adapté de `gen_icons.py` — toutes densités (mipmap
mdpi→xxxhdpi, legacy + round + foreground adaptatif) + les 11 écrans de
démarrage natifs (`splash.png` port/land toutes densités) + le logo
in-webview (`www/img/omcvibe432-logo.jpg`, utilisé par `#splash-36`).
Recadrage carré centré sur la sphère dorée du mandala. Le NOM de l'app
(label natif Android) n'a lui pas pu être changé : il vit dans
`AndroidManifest.xml`, compilé en binaire (AXML) dans l'APK d'origine —
ce pipeline ne repackage que les fichiers web (`assets/public/*`), sans
outil pour décoder/recompiler le manifeste natif.

### Lanceur 3 modules

Nouvel overlay plein écran `#omc-launcher` (z-index 10500, au-dessus de
tout, y compris le splash) affiché par défaut à l'ouverture. 3 cartes :
- **OmchaVibe 432** → masque simplement le lanceur (`_launcherGo('vibe')`),
  aucun rechargement — l'app est déjà initialisée en dessous.
- **Chaharmony** → navigue vers `chaharmony.html` (nouvelle page).
- **OmchaSphere / Flux** → navigue vers `sphere-flux.html` (page d'attente
  pour l'instant, cf. #59 à venir).

Les deux nouvelles pages sont enregistrées dans `NEW_FILES` de
`repack.py` (même mécanisme déjà utilisé pour les images de branding) —
ajoutées à l'APK en tant que nouvelles entrées, aucun fichier existant
touché.

**Effet de bord découvert en testant** : le lanceur intercepte désormais
TOUT geste tactile tant qu'il n'a pas été fermé (z-index au-dessus de
tout, comme voulu) — logique et volontaire, mais ça a cassé plusieurs
scripts de test Playwright existants qui interagissaient directement avec
l'app sans d'abord fermer le lanceur (un des tests a même déclenché une
vraie navigation vers Chaharmony en cliquant "à travers" une carte du
lanceur). Corrigé côté tests uniquement (ils retirent le lanceur avant
d'interagir) — comportement normal et volontaire pour un utilisateur réel.

### Module Chaharmony (nouvelle page `chaharmony.html`)

Générateur de fréquences autonome, 7 oscillateurs, calqué sur la
référence fournie mais dans l'identité visuelle d'OmcVibe (doré/cuivré,
Cinzel Decorative) plutôt que le thème néon cyan d'origine :
- Onglets OSC 1-7, affichage fréquence + curseur 54-864Hz, stepper n
  (−/■/+, le bouton central mute/solo cet oscillateur), curseur volume,
  curseur pan (mode mono).
- **Bouton L·R** : bascule mono (centré, pannable) ↔ binaural (2
  sous-oscillateurs L/R via `ChannelMergerNode`, écart Δ réglable) —
  demande explicite du point 4.
- **Filtre · Type / Cutoff / Résonance** (demande du point 2) : sélecteur
  OFF/HPF/BPF/LPF ajouté au-dessus des curseurs Cutoff/Résonance —
  absent de la référence, ajouté ici en reprenant le même `FILTER_TYPES`
  qu'OmcVibe.
- **Moteur FX propre à chaque oscillateur** (demande du point 3) :
  chaque oscillateur a son propre `ConvolverNode` (reverb) et
  `DelayNode` (delay) indépendants — pas de bus partagé entre
  oscillateurs, contrairement à la référence qui envoyait vers "le jeu
  en cours".
- **Bouton Silence** (demande du point 4) : coupe le master instantanément.
- **Bouton "+"** ouvre un menu déroulant (réinitialiser cet oscillateur,
  fréquence aléatoire, copier ces réglages sur les 7).
- Dock : Retour (→ lanceur), Silence, Flux on/off.

Testé par script Playwright : 7 onglets/oscillateurs présents, bascule
mono→binaural crée bien 2 oscillateurs (gauche/droite décalés de Δ),
changement de type de filtre appliqué en direct au nœud vivant, reverb
par oscillateur connectée indépendamment. Aucune erreur JS.

### OmchaSphere / Flux — en attente (#59)

Page d'attente pour l'instant (`sphere-flux.html`, thème cohérent,
bouton retour). La fusion des 2 visualisateurs fournis (Sphère 432 —
canvas 3D expansion fractale + hiérarchie de sphères harmoniques —, et
Torsion 432 — double hélice ↔ tore avec modes Expansion/Contraction/Flux)
dans l'identité visuelle d'OmcVibe reste à faire : ce sont deux pages
déjà abouties (~1000-2000 lignes chacune, moteur audio binaural intégré,
nombreux panneaux de paramètres) — portage complet dans un futur passage
dédié plutôt que précipité ici.

Où regarder : `#omc-launcher`/`_launcherGo()` (index.html), `NEW_FILES`
dans `repack.py`, `chaharmony.html` (nouveau fichier autonome),
`sphere-flux.html` (placeholder), `branding/icons/` (icône régénérée).

## 16e passe — #66 : entrée manuelle des fréquences (Chaharmony)

Chaharmony n'avait que le curseur 54-864Hz pour régler la fréquence de
chaque oscillateur, contrairement à OmcVibe qui a déjà une saisie
manuelle pour la fréquence maître. Ajouté le même principe : tap sur
la valeur (`.freq-num`) → un champ numérique apparaît, focus + valeur
pré-sélectionnée, Entrée applique (`setOscFreq`, live + état), Échap
annule sans rien changer. `openOscFreqEdit`/`handleOscFreqKey`/
`exitOscFreqEdit`, calqués sur `openFreqEdit`/`handleFreqKey`/
`exitEditMaster` d'index.html. Testé par script Playwright (ouverture,
application, annulation, clamp) : tout passe, 0 erreur JS.

## #65 : nom d'app peu sérieux ("OmcVibe432 · 181")

Signalé : "OmchaVibe 181 c'est nul comme numéro sérieux". Cause trouvée
dans `updateDisplay()` (index.html) : `document.title` était réécrit à
chaque mise à jour d'affichage avec `'OmcVibe432 · '+masterFreq` — donc
le titre de la page changeait en permanence selon la fréquence maître
courante (ex: "OmcVibe432 · 181" après un tirage aléatoire tombé sur
181Hz). Si ce WebView-shell utilise `document.title` comme libellé
affiché (switcher Android, notification, etc.), l'app apparaissait avec
un numéro aléatoire greffé au nom. Corrigé : titre figé, plus jamais
réécrit dynamiquement. Vérifié par ailleurs que le nom du fichier APK
livré est bien `OmcVibe432-debug.apk` (pas de "181" dans le nom du
fichier non plus — l'éventuel ancien fichier "OmcVibe181-…" qui traînait
dans le dossier de build local n'a jamais été livré).

## #67 : craquements sur oscillateur sinusoïdal — watchdog audio inerte

Signalé : craquements sur une sinusoïdale, même en solo. Un `OscillatorNode`
natif `type:'sine'` ne peut pas "mal sonner" — c'est un signal mathématique
pur, sans harmoniques ni distorsion possible côté génération. Un craquement
sur du sinus est donc quasi toujours un problème de plomberie temps réel
(le thread audio rate son rendez-vous), pas de qualité d'onde. C'est
exactement le sujet déjà traité en tête de ce document (Redmi 2021, rendu
visuel qui se bat avec l'audio pour le CPU).

Le point #33 (plus haut) avait déjà mis en place un filet de sécurité :
un watchdog qui surveille `AudioContext.outputLatency` et bascule l'app en
qualité "Légère" (coupe la reverb, l'onde la plus coûteuse du tirage
aléatoire) après ~9s de tension audio soutenue. Problème trouvé en
l'auditant : `outputLatency`/`baseLatency` ne sont pas fiablement
implémentés sur beaucoup de WebView Android (souvent bloqués à 0) — sur un
appareil où c'est le cas, la condition `baseLatency > 0.001` n'est jamais
vraie, donc `strained` ne passe jamais à `true`, et le watchdog reste
inerte en silence, précisément sur les appareils qui en auraient le plus
besoin. C'est la cause la plus probable des craquements signalés.

Corrigé (`_audioWatchdogCheck`, index.html) : ajout d'un second signal
totalement indépendant de cette API — la dérive du `setInterval` du
watchdog lui-même (nominal 3000ms). Si le thread JS principal a été
bloqué assez longtemps pour retarder son propre timer de façon
significative (>600ms), c'est un signe fiable et universel de saturation,
quelle que soit la cause exacte (rendu visuel, garbage collection, charge
CPU générale) — indépendant du support ou non de l'API Latency. Les deux
signaux sont combinés en OU. Réaction aussi accélérée : 2 échantillons
consécutifs de tension déclenchent la dégradation au lieu de 3 (~6s au
lieu de ~9s).

Testé par script Playwright (simulation d'une dérive de timer + vérif que
`AUDIO_QUALITY_TIER` bascule bien sur 'light' et que le watchdog s'arrête
proprement une fois au plancher) : passe, 0 erreur JS.

**Si les craquements persistent malgré ça** (l'auto-adaptation réagit
après coup, elle ne les empêche pas from-the-start) : le vrai fond du
problème reste le nombre de nœuds actifs simultanés vs. la puissance du
téléphone — chaque oscillateur "Double sinus" (le moteur par défaut)
utilise en réalité 2 `OscillatorNode`, soit jusqu'à 28 oscillateurs réels
avec les 7 paires + filtres/gains/panners/FX par voix + bus master (EQ 6
bandes, reverb, 2 delays avec feedback). Pistes si besoin d'aller plus
loin : passer manuellement en qualité "Légère" (bouton dédié) avant même
que les craquements apparaissent plutôt que d'attendre l'auto-détection,
ou couper "Visuel léger" ; réduire le nombre de paires actives (mute) en
solo prolongé.

## #68 : reprise du Color FX BD-2 → "Overdrive Halogène"

Signalé : le BD-2 Blues Driver (#57) "n'est pas de bonne qualité audio, à
reprendre". Bug de fond trouvé dans le routage (`buildOsc`, index.html) :
quand activé, le signal traité (waveshaper → filtre tonalité résonant →
gain plafonné à 50%) **remplaçait entièrement** le signal sec au lieu de
s'y mélanger — `p.connect(bd2Shaper)...bd2Gain.connect(hpf)` OU
`p.connect(hpf)`, jamais les deux. Concrètement : activer l'effet coupait
le volume de la paire de moitié en échange d'un timbre plus dur (filtre
en cloche résonant à 1500Hz, source du côté nasillard) — l'inverse de "un
son plus rond et plus intense" demandé.

Repris avec un modèle additif :
- **Le signal sec passe désormais toujours** (`p.connect(hpf)`
  inconditionnel) — la couche chaude s'ajoute par-dessus en parallèle
  (`hpf` est un nœud sommateur, aucun nœud de mix supplémentaire requis).
  Activer l'effet n'atténue plus jamais le son de base.
- **Courbe de saturation adoucie** : moins agressive à drive max (k
  18→13) et biais asymétrique renforcé (0.08→0.14) pour plus
  d'harmoniques paires — signature chaude/lampe plutôt que numérique dure.
- **"Tonalité" repensée** : l'ancien filtre en cloche résonant (Q 0.8 @
  1500Hz, source du nasillard) est remplacé par un passe-bas doux (Q
  0.707, 700-6000Hz) appliqué UNIQUEMENT à la couche ajoutée — grave =
  plus rond/chaud, aigu = plus présent, jamais sur le sec.
- Renommé dans l'UI ("BOSS Blues Driver" → "Overdrive Halogène"), même
  emplacement (onglet FX de chaque paire), toujours OFF par défaut,
  Drive/Intensité toujours bridés à 50% (expérimental, reste par paire).

Testé par script Playwright : le gain sec de la paire ne bouge plus du
tout à l'activation (avant : divisé par 2), passe-bas de tonalité
appliqué à la fréquence attendue, clamps Drive/Intensité toujours
respectés, nœuds bien retirés à la désactivation. 0 erreur JS.

## #59a : ajout brut de Sphère 432 et Torsion 432 (avant fusion complète)

Demande : "ajoute déjà les html torsion et sphère" — première étape avant
la fusion visuelle complète dans l'identité d'OmcVibe (#59, toujours à
faire). Les deux fichiers fournis sont ajoutés tels quels comme nouvelles
pages autonomes (`sphere432.html`, `torsion432.html`, mêmes mécanismes
que `chaharmony.html` — entrées `NEW_FILES` de `repack.py`), sans
modification de leur design/fonctionnalités.

Nettoyage effectué (pas de changement visuel) : les deux fichiers
provenaient d'un export Canva et traînaient des références mortes —
`sphere432.html` en particulier chargeait en fin de page un script de
tracking Cloudflare (`/cdn-cgi/challenge-platform/...` via iframe caché)
et un SDK d'édition Canva (`/_sdk/editing_sdk.js`), tous deux inertes
hors de l'environnement Canva mais sans rien faire d'utile ici — retirés.
Les deux fichiers référençaient aussi `bg-rotator.css`/`bg-rotator.js`,
absents du projet (404 silencieux) — retirés également.

La page `sphere-flux.html` (accessible depuis le lanceur "OmchaSphere /
Flux") devient un vrai chooser dans le thème doré d'OmcVibe (Cinzel
Decorative), avec une carte pour chacune des deux visualisations. Un
petit bouton "← Retour" a été ajouté dans la barre d'outils de chaque
page (elles n'en avaient aucun — sans lui, impossible de revenir à
OmcVibe autrement que le bouton système Android, non garanti dans un
WebView Capacitor).

Testé par script Playwright : les 3 pages chargent sans erreur JS ni
requête réseau qui échoue (hors les refs mortes déjà retirées), la
navigation chooser → sphère/torsion → retour fonctionne dans les deux
sens.

**Reste à faire (#59)** : fusion visuelle complète dans l'identité
d'OmcVibe (remplacer le thème néon cyan/Orbitron par le doré/cuivré
Cinzel Decorative directement dans ces deux pages, pas seulement dans le
chooser) — pas fait ici, demande explicitement de "d'abord ajouter" avant
la fusion.

## #69 : un seul oscillateur actif par défaut (OmcVibe + Chaharmony)

Demande : "Par défaut on mettra OmcVibe et Chaharmony sur un oscillateur
actif, les autres s'activent manuellement". Auparavant, `startFlow()` /
`startFlux()` démarraient les 7 paires (OmcVibe) ou les 7 oscillateurs
(Chaharmony) simultanément — l'utilisateur devait couper manuellement ce
qu'il ne voulait pas, plutôt que d'ajouter ce qu'il voulait.

- **OmcVibe** (`index.html`) : le mécanisme de coupure existait déjà en
  entier (`mutedOscs`, `toggleMutePair`, `soloMaster` — mute/solo par
  paire depuis longtemps). Seul le point de départ changeait : la
  déclaration initiale de `mutedOscs` mettait tout à `false` (tout actif)
  — désormais seule la paire maître (`MASTER_IDX`) démarre active, les 6
  autres démarrent coupées (nœuds audio quand même construits, juste à
  gain 0 — s'activent instantanément via le mute existant, pas de
  reconstruction nécessaire). `resetAll()` (bouton Réinitialiser) suit la
  même règle — avant il réactivait tout, maintenant il revient au même
  état "1 seul actif" que le premier lancement.
- **Chaharmony** (`chaharmony.html`) : même principe, seul `OSC[0]`
  démarre actif (`muted:false`), les 6 autres démarrent à `muted:true` —
  s'activent via le bouton mute (■/✕) déjà existant sur chaque
  oscillateur.
- **Une session déjà sauvegardée n'est jamais écrasée** : `loadState()`
  (OmcVibe) relit `mutedOscs` depuis le `localStorage` de l'utilisateur
  si une session existe et prend le dessus sur ce nouveau défaut — donc
  un utilisateur qui avait déjà personnalisé quels oscillateurs sont
  actifs retrouve exactement son réglage, seul le tout premier lancement
  (ou un Réinitialiser explicite) applique la règle "1 seul actif".

Testé par script Playwright : à froid (sans session sauvegardée), seule
la paire maître / OSC 1 est active (gain non nul), les autres à gain 0 ;
activation manuelle d'une paire/oscillateur coupé fonctionne
normalement ; `resetAll()` revient au même état ; une session sauvegardée
avec tout actif est bien respectée au rechargement (pas écrasée par le
nouveau défaut). 0 erreur JS sur l'ensemble de la suite de régression.

## #70 : refonte Sphère 432 / Torsion 432 + mode Respiration

Demande : boutons retour, fusion visuelle dans le design d'OmcVibe (menu
unique avec tous les onglets à l'intérieur, dock rapide en bas, écran
maximisé pour l'animation), contraste des flux amélioré, nouveau mode
"Respiration" (transform à volume préservé, 3 paliers 6/5↔5/6 →
11/10↔10/11 → 12/11↔11/12, cf. schéma fourni).

**Menu unique + dock rapide** (`sphere432.html` : 6 panneaux — Modes,
Paramètres, Binaural, BPM, Ping-Pong, Master ; `torsion432.html` : 3 —
Modes, Paramètres, Binaural) : l'ancienne toolbar à 5-7 boutons + panneaux
flottants dispersés aux 4 coins de l'écran est remplacée par un seul
tiroir (`#drawerMenu`) qui glisse depuis le bas, avec un bandeau d'onglets
horizontal (`#drawerTabs`) — même principe que les onglets OSC 1-7 de
Chaharmony. Chaque panneau garde son markup et ses ID EXACTEMENT
inchangés (aucun risque de casser le JS existant, `getElementById`
continue de trouver les mêmes éléments) — seule leur position CSS change
(`position:absolute` dispersé → `position:static` dans le tiroir,
affichage basculé par onglet plutôt que par bouton indépendant). Un dock
bas fixe (`#quickDock`, 5 boutons : Retour / Menu / Binaural / Respiration
/ Écran) reste visible en permanence, y compris tiroir ouvert.

**Écran maximisé** : en-tête réduit à un simple titre (l'ancien sous-titre
et les infos secondaires sont supprimés ou déplacés dans le tiroir),
`#kbdhint`/`#draghint` masqués (redondants avec la nouvelle UI), barre
d'info repositionnée juste au-dessus du dock plutôt qu'au ras du bord bas.

**Palette recolorée** : variables racine (`--c432`, `--cexp`, etc.)
basculées du cyan/vert/orange néon vers le doré/cuivré d'OmcVibe, police
des titres/boutons passée en Cinzel Decorative. Les couleurs propres à
l'animation (dégradés cyan→vert→or des brins/sphères) sont conservées —
c'est la signature visuelle du module, seul l'habillage UI change.

**Contraste des flux amélioré** : planchers d'alpha et largeurs de trait
relevés dans les fonctions de dessin (`drawHelix`/`drawMx` de
torsion432.html, `drawSphere` de sphere432.html), glow élargi — les
lignes du flux se détachent mieux du fond sombre.

**Mode Respiration** (nouveau, les deux fichiers) : `BREATH2` — le centre
oscille en 3 paliers successifs (6/5↔5/6, puis 11/10↔10/11, puis
12/11↔11/12, ~13s chacun) via `s = r^sin(phase)`, où `r` est le ratio du
palier. Appliqué comme `Y ×s, X·Z ×1/√s` : volume géométrique
mathématiquement préservé (s × (1/√s)² = 1), exactement la formule du
schéma fourni. Un léger vrillage corrélé à `s` est ajouté ("la corde tire
en Z, la sphère se tord"). Sur torsion432.html appliqué dans `hPt()` (le
générateur de points partagé par toute la géométrie) ; sur sphere432.html
appliqué directement dans `proj()` (la fonction de projection unique par
laquelle passe tout l'écran) — dans les deux cas un seul point
d'application suffit à faire respirer toute la scène de façon cohérente.
Activable via le bouton dédié du dock (indépendant des autres modes) ou
via l'entrée "Respiration Omcha" du panneau Modes.

Testé par script Playwright sur les deux fichiers : ouverture/fermeture
du tiroir, bascule entre tous les onglets, respiration on/off (change
bien la géométrie/projection), bouton retour → chooser, aucune régression
sur les fonctions existantes (modes, expansion maximale, BPM, ping-pong,
binaural). 0 erreur JS, syntaxe vérifiée (`node --check`).

## #71 : sphère coupée qui "disparaît" + solo maître cassé

Deux signalements liés au défaut #69 (1 seul oscillateur actif au
démarrage) :

**1/ Visuel — sphère coupée illisible.** `.vp-p.vp-muted` était à
`opacity:.12` — quasi invisible sur le fond sombre. Comme #69 fait
démarrer 6 des 7 sphères dans cet état, l'écran de départ semblait
presque vide. Corrigé : les sphères coupées restent bien visibles,
rendues en quartz blanc plat (dégradé radial pâle, bordure claire) et
**sans halo** (`box-shadow:none`), au lieu de disparaître.

**2/ Bug réel — solo maître (et le solo générique du menu rapide,
utilisé aussi par la sphère maître) cassés.** `soloMaster()`/`soloPair()`
déduisaient "est-on déjà en solo ?" en vérifiant si toutes les autres
paires sont actuellement coupées — un raccourci qui marchait tant que
l'état de départ normal était "tout actif". Depuis #69, l'état de départ
EST déjà "tout coupé sauf le maître", donc cette déduction est fausse dès
le premier appui : `currentlySoloed` valait déjà `true` par défaut, donc
`solo = !currentlySoloed` valait `false` — le premier appui sur "Solo
maître" RÉACTIVAIT tout au lieu d'isoler le maître (l'inverse de l'effet
voulu). Le commentaire du code documentait même explicitement cette
hypothèse ("soloPair interroge l'état courant, pas besoin de mémoriser")
— exactement l'hypothèse invalidée par #69.

Corrigé en remplaçant la déduction par un état explicite (`_soloedIdx` +
`_soloSnapshot`) : fiable quel que soit l'état de départ, et qui en
prime restaure maintenant l'état RÉEL d'avant le solo au lieu de tout
réactiver aveuglément (si seules 2 paires jouaient avant le solo, seules
ces 2 paires reviennent — pas les 6). `soloMaster()` devient un simple
raccourci vers `soloPair(MASTER_IDX)` (les deux étaient déjà le même
code dupliqué).

Testé par script Playwright : scénario "jam réel" (2 satellites actifs,
solo isole bien le maître puis restaure exactement les 2 satellites
d'origine) et scénario "démarrage vierge" (le tout premier appui isole
correctement, ne réactive plus tout par erreur) — les deux passent.
Sphère coupée : `opacity:.62`, `box-shadow:none`, dégradé quartz blanc
confirmés par style calculé + capture d'écran. 0 erreur JS.

## #72 : esthétique du dock OmcVibe + Tutoriel 3 niveaux

**1/ Fond transparent** — `#bottom-dock` était à `background:rgba(2,1,14,.92)`
(quasi opaque) ; ramené à `.58` pour laisser voir le fond cosmique même
sur les WebView Android où `backdrop-filter` ne s'applique pas de façon
fiable (cf. #67 déjà rencontré sur ce sujet).

**2/ "Petite sphère pas harmonieuse"** — les sphères "+" du dock
(`.dcell-extra`, raccourcis appui long) étaient en aplat de couleur plein
et mat (`background:#FF4646` etc.) — tranchaient avec l'esthétique en
dégradé/lueur du reste de l'app. Remplacées par un rendu "verre" (dégradé
radial + reflet + lueur douce), même principe que les sphères
d'oscillateur.

**3/ Texte qui déborde** — confirmé par capture d'écran à 360px de large :
"Aléatoire" coupé en "Aléatoir", "Paramètres" cassé en "Paramè"/"tres" au
milieu du mot. Cause : `.dcell-sub` était en `white-space:nowrap` (aucun
retour à la ligne, contrairement à `.dcell-name` qui l'avait déjà) et la
règle mobile (`@media max-width:900px`) réimposait des tailles de police
`!important` qui écrasaient silencieusement l'ajustement spécifique du
bouton bleu (`.dcell-blue .dcell-name`, sans `!important` lui). Corrigé :
`.dcell-sub` accepte maintenant le retour à la ligne comme `.dcell-name`,
tailles réduites (dock mobile : .9rem→.82rem / 1.14rem→.88rem), et
`.dcell-blue .dcell-name` passé en `!important` pour ne plus être écrasé.
Vérifié : 0 élément en dépassement (`scrollWidth`/`scrollHeight`) après
correction, contre 1 avant.

## #72b : Tutoriel utilisateur (Paramètres → Tutoriel)

Nouveau panneau accessible depuis Paramètres, guide complet à 3 niveaux
sélectionnables (le choix est mémorisé) :
- **Chaton** (débutant) — vocabulaire simple, l'essentiel pour démarrer :
  c'est quoi l'app, démarrer/couper le son, le binaural en une phrase,
  toucher une sphère, le dock.
- **Chat perché** (avancé) — les concepts en détail (Δ, ratios
  harmoniques), tous les gestes (tap/appui long/menu rapide à 4 boutons),
  solo maître, personnalisation du dock, FX par paire vs global.
- **Chat cosmique** (expert) — profondeur technique complète : moteur
  double-sinus, mode casque strict, filtre par oscillateur, Overdrive
  Halogène (modèle additif), compression/mastering, watchdog qualité
  audio, presets.

Chaque niveau est une liste de sections en accordéon (réutilise
`toggleAccord`, déjà existant ailleurs dans l'app). Testé par script
Playwright : contenu présent aux 3 niveaux, changement de niveau
fonctionne, accordéon s'ouvre/se ferme, préférence de niveau restaurée
après rechargement de page. 0 erreur JS.

## #73 : dock encore plus transparent

Retour terrain : `.58` (première passe de #72) pas assez transparent sur
l'appareil réel. Ramené à `.24` — le motif de géométrie sacrée et le fond
cosmique restent bien visibles à travers la barre.

## #74 : logo de l'icône APK recentré

Le crop carré de l'icône (mandala/sphère dorée) était calculé à l'oeil
(`center_y = H×0.455`), ce qui décentrait le logo — la sphère dorée
tombait visiblement trop bas dans le cadre, plus de rognage en bas qu'en
haut. Recalculé par détection de couleur (centroïde des tons dorés de la
sphère/mandala sur l'image source) : les mesures convergent entre 0.487
et 0.53 selon le seuil de détection ; confirmé visuellement à **0.50**
(centre vertical exact de l'image source) comme le mieux équilibré
haut/bas. Régénéré : toutes les densités d'icône (mdpi→xxxhdpi, legacy +
round + adaptive foreground), tous les splashscreens, et le logo web
(`img/omcvibe432-logo.jpg`, utilisé par l'écran de démarrage `#splash-36`
dans l'app elle-même).

## #75 : audit 18 points anti-craquement — vague de correctifs

Suite à l'audit approfondi des causes de craquement/déchirement/claquement
du moteur audio, passe de correctifs sur `OmcVibe/www/index.html` et
`OmcVibe/www/chaharmony.html` :

- **Module Bowl (bols tibétains) supprimé entièrement** — AudioWorklet
  orphelin, plus utilisé depuis plusieurs versions, seul point d'entrée
  jamais appelé nulle part (`window.Bowl`), risquait de créer un second
  `AudioContext` dupliqué si jamais déclenché.
- **Module Reverb maître supprimé entièrement** (Sec/Grotte/Cathédrale/
  Cosmos) — convolveur jusqu'à 14s d'IR, calcul le plus lourd de toute la
  chaîne FX ; retiré avec tout ce qui en dépendait exclusivement (envoi
  FX par paire/`PAIR_FX`, bouton FX du menu rapide, presets, FX aléatoire,
  fondu solo/master, envoi par profondeur 3D). Delay et Ping-Pong globaux
  restent inchangés. **La reverb propre à chaque échantillon dans
  l'éditeur de samples n'est PAS concernée** (moteur indépendant, conservé
  tel quel), pas plus que la reverb par oscillateur de Chaharmony.
- **Timer/modules "méditation" inutilisés** — le raccourci "Minuteur 20'
  rapide" n'existait déjà plus dans le registre d'actions du dock (mort
  depuis un nettoyage précédent) ; les dernières traces (options de menu
  déroulant dupliquées 15 fois dans un bloc HTML statique jamais affiché,
  remplacé au chargement) ont été retirées.
- **Chaîne de sécurité audio portée à Chaharmony** — ce module n'avait
  strictement aucune protection anti-clip (`master.connect(destination)`
  direct, jusqu'à 7 oscillateurs pouvant s'additionner). Ajout du même
  étage qu'OmcVibe : compresseur "glue" doux → limiteur brickwall → soft-
  clip tanh sans latence, plus un watchdog qualité audio (dérive de timer)
  qui coupe reverb/delay de tous les oscillateurs en cas de tension
  soutenue. Au passage, le convolveur/tank delay de chaque oscillateur
  n'est plus connecté en continu quel que soit son wet (même correctif
  que le #33 d'OmcVibe) — 7 convolveurs qui tournaient pour rien à 0%.
- **Watchdog qualité audio (OmcVibe)** retargeté : dégradait la reverb
  (maintenant supprimée) → dégrade désormais le Ping-Pong (nouveau poste
  le plus lourd de la chaîne FX restante).
- **Résonance de filtre plafonnée à 18 (était 30)** — un Q aussi extrême
  frôle l'auto-oscillation du filtre (pic de gain énorme à la coupure),
  contributeur plausible aux dépassements que le limiteur devait rattraper
  en urgence. Le pad tactile ne dépassait déjà jamais 6 en usage normal ;
  ce plafond est une protection défensive (presets, valeurs importées…).
  18 reste une résonance très marquée à l'oreille.
- **Bascule Overdrive Halogène par paire** (`togglePairBD2`) reconstruisait
  les **14 oscillateurs** (`rebuildAllOscs`) pour un changement qui ne
  concerne qu'**une seule paire** (2 oscillateurs) — les 6 autres paires en
  train de jouer normalement retombaient et rejouaient pour rien à chaque
  bascule. Nouvelle fonction `_rebuildPair(i)` : ne touche plus que la
  paire concernée, les autres continuent sans interruption.

Vérifié par une suite de tests Playwright (suppression Bowl/Reverb/Timer,
chaîne de sécurité Chaharmony, gating reverb/delay, non-régression du
rebuild ciblé par paire, plafond de résonance) : 0 erreur JS sur l'ensemble.

## #76 : 4 correctifs samples / jeu aléatoire

Retour terrain suite à la maj précédente.

1. **Glisser-déposer un sample vers un dossier trop lent** — le seuil
   d'appui long ramené de 420ms à 280ms (reste assez long pour ne pas
   confondre un scroll de liste avec un appui long).
2. **Impossible de corriger la fréquence fondamentale détectée** — elle
   était en lecture seule (aucun moyen de la corriger si l'autocorrélation
   se trompe sur un sample complexe). Ajout d'une édition tactile (toucher
   la valeur → clavier, Entrée valide/Échap annule), correction persistée
   sur le fichier comme la détection automatique.
3. **Conflit au lancement du jeu avec un sample : fréquence maître
   "perdue", jeu recalé sur rien de cohérent** — vraie course entre
   `stopFlow()` et un `startFlow()` rapproché (redémarrage rapide du Flux,
   scénario courant avec le jeu depuis un sample). Le nettoyage DIFFÉRÉ de
   `stopFlow()` (600ms) mettait `masterGain`/`_lfoGain`/etc. — des
   variables globales partagées, pas des références locales — à `null` en
   pleine NOUVELLE session si celle-ci avait démarré entre-temps : les
   paires pas encore reconstruites au moment du nettoyage restaient alors
   silencieusement absentes (garde `if (!flowing || !masterGain) return`
   dans `swapPingala`/`swapIda`), watchdog et sub-drone de la nouvelle
   session coupés au passage. Corrigé par une garde `if (flowing) return;`
   en tête du nettoyage différé : s'il y a une nouvelle session en cours,
   le nettoyage de l'ancienne ne fait plus rien.
4. **Le jeu aléatoire (Omchaléatoire / Nouveau jeu) ignorait les
   oscillateurs mute** — la répartition fréquentielle (`aeratedN`)
   utilisait toujours 6 créneaux fixes même quand une partie des paires
   est coupée, les tassant dans une plage pensée pour 6 au lieu de profiter
   de tout l'espace libéré. Le tirage ne construit désormais ses créneaux
   QUE sur les paires satellites actives (non mute, `slotCount` dynamique)
   ; une paire mute n'est plus retirée au hasard (elle garde sa fréquence
   jusqu'à être démutée) et la "vedette" (traitement timbre/profondeur) ne
   peut plus tomber sur une paire inaudible.

Vérifié par tests Playwright (édition fondamentale + persistance,
glisser-déposer bout en bout jusqu'au déplacement réel, redémarrage rapide
du Flux avec les 14 oscillateurs bien reconstruits, tirage aléatoire avec
paires mute qui ne redessinent plus leur fréquence) : 0 erreur JS.

## #82 : passe d'équilibrage modules + nettoyage interface

Audit de chaque module (OmcVibe, Chaharmony, Sphère432, Torsion432) pour
CPU/batterie et conflits, + nettoyage visuel.

**CPU/batterie :**
- **Sphère432** avait le même défaut que l'ancienne reverb d'OmcVibe
  (#33) : son convolveur (moteur audio indépendant, propre à ce module)
  restait connecté en continu même à 0% de reverbe. Corrigé avec le même
  gating (entrée coupée/reconnectée selon le wet). Ajout au passage d'un
  plafond dur (soft-clip tanh) avant la sortie matérielle — absent
  jusqu'ici, alors que ce module cumule EQ 3 bandes + compresseur + reverb.
- **Torsion432** : chaîne audio (2 sinus + gain) vérifiée intrinsèquement
  sûre (gain conservateur, aucune addition de sources capable de
  dépasser 0dBFS) — pas de plafond nécessaire.
- **`updateInfobar()`** (OmcVibe) tournait toutes les secondes en continu,
  y compris app en arrière-plan — travail trivial mais désormais sauté
  entièrement quand la page n'est pas visible.
- Vérification croisée : aucun timer/interval orphelin trouvé ailleurs
  (watchdog, ancre respiratoire, heartbeat, automations tactiles) — tous
  correctement armés/désarmés avec le Flux.
- Modules indépendants (Sphère432/Torsion432 = pages séparées, chacune son
  propre AudioContext) : pas de conflit constaté, la navigation entre
  modules décharge proprement le contexte précédent (navigation de page
  complète, pas de superposition de moteurs audio).

**Interface :**
- Texte "FBF · PRESS & DESTRESS" supprimé (bandeau au-dessus de la
  fréquence maître, jugé superflu/brouillon).
- Cellule "Option Jeu Aléatoire" du dock : elle seule mélangeait deux
  polices différentes (nom en Cinzel Decorative 12px + sous-titre en IM
  Fell English 16px, plus gros que le "titre") — unifiée sur la même
  police/taille que les 3 autres cellules (Samples/Effet Audio/
  Paramètres), retour à la ligne naturel comme "Paramètres" le fait déjà.
- Flou du fond de la barre du bas réduit (20px→9px) : à alpha égal (.24),
  un flou aussi fort aplatissait le fond étoilé en une masse sombre
  uniforme, recréant la "barrière visuelle" malgré 2 passes précédentes
  sur la seule transparence.
- Chiffre de fréquence maître (mode solo, actif par défaut depuis #69)
  adouci : 4.2rem→3.3rem, halo lumineux réduit — jugé trop imposant/
  agressif à l'écran d'accueil.

Vérifié par tests Playwright (gating reverbe Sphère432 par défaut et sur
changement de curseur, absence de régression sur les suites existantes
OmcVibe/Sphère432/Torsion432, 0 élément de texte en débordement dans le
dock) : 0 erreur JS.
