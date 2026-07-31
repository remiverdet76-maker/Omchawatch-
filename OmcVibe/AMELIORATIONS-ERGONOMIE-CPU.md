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

## #83 : fuite audio — un oscillateur "mute" pouvait quand même s'entendre

Retour terrain : "des sons qui s'activent sans savoir d'où ils sortent".

Cause racine : la respiration par paire (`attachOscVolLFO`, LFO doux de
volume) connecte son gain DIRECTEMENT sur l'AudioParam `node.g.gain` — un
AudioParam SOMME toutes ses entrées (valeur de base + tout ce qui y est
connecté). Couper une paire (mute) ramène la valeur de BASE à 0, mais
n'empêchait pas un LFO déjà (ou nouvellement) attaché de continuer à
moduler ce même paramètre par-dessus — l'oscillateur "muet" se mettait
alors à pulser audiblement au rythme du LFO (quelques secondes à plus
d'une minute de cycle), sans bouton ni sphère associée à l'origine du son
puisque rien dans l'UI ne "montre" un LFO de respiration actif sur une
paire éteinte.

Deux déclencheurs identifiés :
1. Le tirage aléatoire (`triggerMagicAuto`) attache une respiration à
   TOUTES les paires quand l'option "Respiration par paire" est cochée —
   y compris les paires mute (bug depuis l'introduction de la fonctionnalité,
   resté longtemps invisible tant que peu de paires étaient mute par
   défaut ; devenu flagrant depuis #69, une seule paire active de base).
2. Couper une paire qui avait déjà une respiration active (bouton dédié,
   ou solo, ou ancre respiratoire) ne la détachait jamais.

Corrigé à la source : `attachOscVolLFO` refuse maintenant d'attacher un
LFO sur un oscillateur mute (protège tous les points d'entrée d'un coup —
tirage aléatoire, ancre respiratoire, bouton manuel), et les 3 fonctions
qui coupent une paire interactivement (mute d'une sphère, solo, solo
sample) détachent explicitement toute respiration déjà en cours au moment
de couper.

Vérifié par test Playwright : gain d'une paire mute strictement à 0 sur
10 échantillons après un tirage aléatoire avec respiration activée
(avant : aurait pulsé) ; respiration attachée puis paire mute juste après
→ gain retombe et reste exactement à 0 (avant : serait resté audible).
0 erreur JS, 0 régression sur les suites existantes.

## #84 : refonte du panneau Options Aléatoire

Retour terrain sur plusieurs points du panneau "Omchaléatoire".

1. **"Caractère du battement" (Apaisé/Équilibré/Immersif) sans effet
   perceptible** — double cause trouvée. D'abord, ce réglage n'agit QUE
   verrou binaural décoché (`RAND_OPTS.lockBinaural`) — activé par défaut,
   donc invisible pour qui ne l'a jamais désactivé manuellement. Choisir
   explicitement un caractère décoche maintenant ce verrou à sa place (avec
   un message expliquant pourquoi), sinon le bouton semblait ne servir à
   rien. Ensuite, même actif, les 3 plages de Δ se chevauchaient presque
   entièrement (Apaisé finissait à 1.00 Hz, Immersif commençait à 1.00 Hz)
   — redistribuées sur 3 tiers bien distincts de la bande Delta
   (0.8-1.2 Hz, plage volontairement resserrée, jamais élargie).
2. **Modes de tirage Ratio⌀/Identique/Harmonique cassés depuis toujours**
   — `RAND_OPTS.ratioMode` était réglé par les 3 boutons mais jamais LU
   nulle part : appuyer dessus ne changeait strictement rien au tirage
   suivant. Implémenté pour de vrai : Identique tire un seul ratio partagé
   par toutes les paires (seul le registre varie encore) ; Harmonique
   assigne les 6 ratios dans un ordre fixe et non mélangé (au lieu du
   tirage aléatoire habituel).
3. **Nouveau mode Personnalisé** — 4ᵉ bouton : le tirage aléatoire ne
   touche plus jamais aux ratios/registres des paires satellites, qui
   restent exactement ce que l'utilisateur a réglé à la main.
4. **Édition directe + lecture/pause dans la table d'oscillateurs** (déjà
   visible dans le panneau Options Aléatoire) — chaque ligne a maintenant
   un bouton lecture/pause (mute sans passer par la sphère), le ratio se
   touche pour tourner parmi les 6 valeurs, et ×n s'édite au clavier
   (tap → saisie → Entrée). Rend le mode Personnalisé utilisable
   directement depuis cette liste, comme demandé.
5. **Évolution harmonique douce** (nouveau) — case à cocher séparée du
   tirage aléatoire classique : toutes les ~45s, UNE paire satellite (jamais
   le maître, jamais mute/verrouillée) dérive très légèrement de registre
   (±12%, jamais un saut de créneau) avec une glisse de 6 à 10s — assez
   lent pour ne jamais s'entendre comme un "changement", juste une
   respiration harmonique du jeu dans la durée. Hors plage active : le
   cycle ne fait rien plutôt que de forcer un retour brusque en bord de
   plage (aucun risque de à-coup/saturation).

Vérifié par tests Playwright : plages Δ non chevauchantes + décochage
automatique du verrou, Identique/Harmonique/Personnalisé produisent
exactement le comportement attendu sur plusieurs tirages, lecture/pause +
cycle de ratio + édition ×n depuis la table fonctionnent, évolution douce
ne touche jamais le maître/une paire mute/verrouillée et reste dans les
bornes. 0 erreur JS, 0 régression sur l'ensemble des suites existantes.

## #85 : toutes les sphères démarrent mute + marge de sécurité 36 Hz

1. **Toutes les sphères démarrent coupées (blanc quartz)** — y compris le
   maître, qui était la seule active par défaut depuis #69. L'utilisateur
   active désormais tout manuellement (tap/solo) ou via un tirage
   aléatoire. Ne change rien pour qui a déjà une session sauvegardée
   (`loadState()` reste prioritaire) — n'affecte que le premier lancement
   et "Réinitialiser tout".
2. **Marge de sécurité 36 Hz entre fréquences (pas une option)** — sur 7
   oscillateurs indépendants répartis dans la même plage, deux paires
   DIFFÉRENTES pouvaient par hasard tomber à quelques Hz l'une de l'autre :
   un battement non voulu, non contrôlé, entre deux paires qui n'ont rien
   à voir (à ne pas confondre avec le Δ binaural intentionnel à
   l'intérieur d'une paire). Chaque tirage aléatoire vérifie maintenant la
   fréquence de chaque paire contre toutes celles déjà retenues ce
   tirage-ci ; en cas de conflit, recherche du point libre le plus proche
   de la position visée dans toute la plage active valide (pas seulement
   le créneau log d'origine, qui peut être "pollué" si le maître tombe
   justement dedans). Se dégrade proprement si la plage active est trop
   étroite pour le nombre de paires actives (garde le tirage habituel
   plutôt qu'une valeur forcée).

Vérifié par tests Playwright : toutes les paires mute au 1er lancement et
après Réinitialiser (y compris le maître), état sauvegardé toujours
respecté au rechargement, 0 violation de la marge de 36 Hz sur 25 tirages
aléatoires consécutifs (525 paires de fréquences comparées), aucun crash
sur une plage active volontairement trop étroite pour tenir la contrainte.
0 erreur JS, 0 régression sur l'ensemble des suites existantes.

## #86 : option « Battement rapproché » (1–7.2 Hz) — exception assumée à la marge de 36 Hz

Demande : pouvoir influencer le tirage aléatoire pour retrouver, VOLONTAIREMENT,
l'effet dynamique d'un battement rapproché entre deux paires (1 à 7.2 Hz
d'écart) — contradictoire avec la marge de sécurité #85, donc une exception
explicite, pas un retour en arrière sur la règle des 36 Hz.

1. **Nouvelle case à cocher "Battement rapproché (1–7.2 Hz)"** dans le
   panneau Options Aléatoire (`RAND_OPTS.closeBeatPair`, **OFF par
   défaut** — la marge de sécurité pleine reste le comportement par
   défaut, l'utilisateur choisit explicitement plus de dynamisme).
2. **Un seul duo par tirage** — quand l'option est active, `triggerMagicAuto`
   désigne au hasard DEUX paires satellites actives (non mute, non
   verrouillées, non conservées) : une "meneuse" tirée normalement (36 Hz
   respectés vis-à-vis de tout), puis une "suiveuse" dont la fréquence
   vise volontairement 1 à 7.2 Hz au-dessus OU en-dessous de la meneuse
   (`_drawCloseFollowerN`). C'est la SEULE exception à la marge de 36 Hz :
   la suiveuse reste malgré tout à ≥36 Hz de TOUTES les autres paires
   (uniquement la meneuse est frôlée) — aucune paire tierce ne se
   retrouve prise dans le rapprochement.
3. **Dégradation propre** — si aucune des deux positions (meneuse + gap,
   meneuse − gap) n'est libre par rapport à une tierce paire, la
   suiveuse retombe sur le tirage normal à 36 Hz pour ce tirage-là
   (jamais de valeur forcée qui romprait la marge avec une autre paire).

Vérifié par tests Playwright : option décochée par défaut et persistée
(sauvegarde/rechargement), option décochée → 0 changement de comportement
(marge de 36 Hz intacte sur 20 tirages), option cochée → un écart compris
entre 1 et 7.2 Hz apparaît sur 28 tirages sur 30, toujours entre EXACTEMENT
deux paires (jamais de violation de 36 Hz "en trop" ailleurs dans le même
tirage). 0 erreur JS, 0 régression sur l'ensemble des suites existantes.

## #87 : nouveau module "Géométrie" + binaural OmchaVibe 432 en fond dans Sphère/Flux

Demande : un nouveau module de modélisation 3D, à sa place aux côtés de
Sphère 432 / Torsion 432 dans OmchaSphere/Flux, en partant du moteur 3D déjà
existant — et si possible, garder le son d'OmchaVibe 432 audible en fond
pendant qu'on parcourt ces modules visuels (au lieu de le couper).

1. **Nouveau module `geometrie.html`** — export du moteur 3D existant
   (projection/rotation canvas, mandala/double-hélice/tore, modes
   Expansion/Contraction/Flux) déjà éprouvé dans Torsion 432, repris tel
   quel comme socle du nouveau projet "Géométrie" (identité propre :
   titre, en-tête). Base de départ pour la modélisation 3D plus complexe à
   venir, comme demandé — rien n'est réinventé qui marchait déjà.
2. **Nouvelle carte "Géométrie"** dans le sélecteur OmchaSphere/Flux
   (`sphere-flux.html`), aux côtés de Sphère 432 et Torsion 432.
3. **Binaural d'OmchaVibe 432 en fond sonore réel** — jusqu'ici, ouvrir
   Sphère 432/Torsion 432 depuis le lanceur faisait une navigation
   complète (`location.href`) : nouvelle page, nouveau contexte audio,
   donc le binaural en cours s'arrêtait net. Le sélecteur OmchaSphere/Flux
   s'ouvre désormais dans un **overlay iframe** par-dessus `index.html` au
   lieu de le remplacer : le moteur audio d'OmchaVibe 432 tourne
   toujours en dessous, sans coupure, tout le temps qu'on navigue entre
   Sphère 432 / Torsion 432 / Géométrie. Ces 3 modules coupent déjà
   automatiquement leur propre moteur binaural interne dès qu'ils
   détectent l'embarquement (`window.self !== window.top`, mécanisme déjà
   en place) — le son d'OmchaVibe 432 reste ainsi l'unique source, sans
   double lecture ni conflit. "Retour à l'accueil" depuis l'intérieur de
   l'overlay referme simplement l'overlay (jamais de rechargement imbriqué
   d'`index.html` dans lui-même).
4. **CPU** — le rendu visuel d'OmchaVibe 432 (`masterTick`, sphères/canvas)
   s'arrête pendant que l'overlay est ouvert (nouvelle vérification dédiée
   — `document.visibilityState` ne suffisait pas ici, l'onglet reste
   "visible" tout du long) : pas de rendu invisible gâché derrière le
   module affiché, seul le moteur audio continue de tourner.

Vérifié par tests Playwright : le binaural reste `flowing` et l'AudioContext
`running` tout au long de l'ouverture/navigation/fermeture de l'overlay,
0 AudioContext créé côté module tant que son bouton "Activer" n'est pas
pressé à la main, les 3 cartes (Sphère 432 / Torsion 432 / Géométrie)
s'ouvrent correctement dans l'overlay avec `window.self !== window.top`,
fermeture de l'overlay (`_closeModuleOverlay`) remet bien l'iframe à
`about:blank` et le binaural n'est jamais interrompu. 0 erreur JS, 0
régression sur l'ensemble des suites existantes.

**Correction (même jour) — mauvais moteur exporté.** Le retour terrain a
signalé que `geometrie.html` reprenait par erreur le moteur de Torsion 432
(double hélice/tore, projection canvas maison) — un doublon, pas "le moteur
3D de OmchaVibe". Le vrai moteur visé est celui qui tourne EN PERMANENCE
derrière la sphère maître d'OmchaVibe 432 elle-même (`meta-canvas`,
fonctions `FX3D`/`drawMetatron` d'`index.html`) : réseau de lignes de flux
lumineuses avec bloom, mandala sacré en fond, 72 particules orbitales sur 6
familles de couleur, centre fractal pulsant — sensiblement plus riche que
Torsion 432. `geometrie.html` a été entièrement réécrit autour de CE moteur :

1. **Export fidèle du moteur FX3D** — `proj`, `buildMC` (mandala), `FX3D`
   (construction des points + rendu bloom/flux/centre), particules
   orbitales, projection caméra (`V`) avec glisser/pincer/zoom : code repris
   à l'identique d'`index.html`, aucune réécriture des maths.
2. **Les 4 formes originales, toutes réactivées** — dans OmchaVibe 432
   lui-même, seul Torus reste sélectionnable (Sphère/Métatron/Merkaba ont
   leur code de rendu intact mais dormant, exclues de l'UI). Ce module leur
   redonne accès : un sélecteur "Formes" bascule librement entre les 4,
   plus une évolution automatique optionnelle qui les enchaîne toute
   seule.
3. **Panneau "Paramètres"** — densité, torsion, expansion, étalement,
   éclat, polarité de couleur, vitesse : tous les curseurs de FX3D exposés
   pour explorer librement le moteur (base pour les évolutions design à
   venir, comme demandé).
4. **Teinte reliée au binaural RÉEL en cours de lecture** — dans OmchaVibe
   432, la couleur du flux suit `masterFreq`. Embarqué dans l'overlay (cf.
   #87 ci-dessus), Géométrie lit maintenant la fréquence maître
   RÉELLEMENT jouée chez le parent via un pont dédié
   (`window.getMasterFreq` côté `index.html` — `masterFreq` est déclarée
   en `let`, donc invisible depuis un iframe enfant via `parent.masterFreq`
   directement, contrairement à une globale `var` ; bug découvert et
   corrigé pendant la vérification) : les couleurs réagissent vraiment à ce
   qu'on entend, pas à une simulation. Page autonome (hors overlay) :
   dérive douce et purement décorative autour de 432 Hz.

Vérifié par tests Playwright : les 4 formes rendent sans erreur, tous les
curseurs/interrupteurs du panneau "Paramètres" s'appliquent bien à `FX3D`/
`V`/`AE`, le pont `getMasterFreq` renvoie la valeur exacte et à jour de
`masterFreq` côté parent (testé avec changement en direct : 108 → 288 Hz,
bien répercuté), 0 erreur JS. Captures d'écran validées visuellement pour
Torus/Métatron (rendu identique en qualité à ce qui tourne derrière la
sphère maître). Suite de régression complète (16 fichiers) toujours à 0
erreur.

## #88 : Géométrie — 17 points design / fractal / 3D / organique

Suite de la demande précédente : 17 des 18 points proposés pour enrichir le
moteur FX3D exporté dans `geometrie.html` (le 18ᵉ, pulsation synchronisée
sur le battement binaural réel, explicitement écarté par le demandeur).
Tout reste **purement additif** — aucun réglage neuf ne change quoi que ce
soit tant qu'on ne le touche pas (valeurs par défaut strictement
équivalentes à l'état livré juste avant, p=q=1 pour le torus, bruit/
vibration/plan de coupe à 0%, etc.).

**Géométrie fractale**
1. **Fractal (IFS, chaos game)** — nouvelle forme : nuage de points
   auto-similaire par jeu du chaos (3 à 9 attracteurs selon la profondeur),
   mis en cache et stable (pas de scintillement d'une frame à l'autre).
2. **Torus généralisé en nœud (p,q)** — la formule d'origine du torus est
   étendue avec 2 multiplicateurs de phase (p, q) ; p=q=1 (défaut)
   reproduit EXACTEMENT le torus d'origine, au-delà on obtient un vrai
   nœud toroïdal tissé.
3. **Symétrie kaléidoscopique réglable** — le mandala de fond n'avait que
   3 bras de spirale dorée fixes ; curseur 3 à 16.
4. **Arbre de vie** — nouvelle forme : structure arborescente générée par
   L-system récursif (racines → branches), profondeur ET densité de
   nœuds réglables (voir correctif ci-dessous).
5. **Formes composites** — une 2ᵉ forme au choix se superpose à la forme
   principale en transparence réglable.

**Organique**
6. **Distorsion organique** — bruit spatial 3D (implémentation "bruit de
   valeur" compacte, auto-contenue) qui froisse doucement n'importe
   quelle forme, intensité réglable.
7. **Nuée de particules (boids)** — mode alternatif pour les 72
   particules : séparation/alignement/cohésion au lieu d'orbites fixes.
8. **Vibration haute fréquence** — jitter aléatoire ré-tiré à CHAQUE
   frame sur les sommets (distinct de la distorsion organique, qui varie
   lentement avec le temps).
9. **Morphing interpolé** — l'évolution automatique (déjà existante)
   glisse désormais d'une forme à l'autre sur ~2,6s (easing) au lieu d'un
   cut instantané ; la sélection manuelle d'une forme reste, elle,
   immédiate.

**Profondeur / 3D**
11. **Profondeur de champ** — passe floutée (canvas `filter: blur()`)
    sous la passe nette, approximation pragmatique d'un flou de
    profondeur.
12. **Lumière directionnelle rotative simulée** — chaque segment de ligne
    est éclairci/assombri selon son orientation par rapport à une source
    lumineuse qui tourne lentement (façon éclairage Lambertien 2D).
13. **Vue libre étendue + plan de coupe** — plage de zoom élargie
    (.15×–6× au lieu de .3×–3×) et un curseur "plan de coupe" qui masque
    les points les plus lointains (espace de vue, après rotation caméra)
    pour révéler l'intérieur des formes.
14. **Traînées de particules** — calque canvas séparé, effacé par
    `destination-out` (fondu vers le TRANSPARENT) plutôt qu'un simple
    remplissage noir à faible alpha qui, lui, finit par s'accumuler en un
    calque opaque et masquer tout le reste — bug détecté à la capture
    d'écran et corrigé avant livraison.

**Design / couleur**
15. **Palette dynamique étendue** — un curseur de teinte continue (0-360°)
    tourne désormais aussi les couleurs du réseau de lignes/formes (pas
    seulement les particules, qui avaient déjà leur propre suivi de
    fréquence maître).
16. **Mandala génératif à seed** — bouton "Nouveau mandala" : re-tire un
    nombre de pétales et un léger désalignement des bras via un PRNG à
    seed (mulberry32), pour un motif jamais identique.

**Créatif / UX**
17. **Export image** — capture le rendu courant (fond + flux + traînées
    éventuelles) en PNG et déclenche un téléchargement.
18. **Presets "constellations"** — sauvegarde/rappel de combinaisons
    complètes (forme, ratio, tous les curseurs, tous les effets actifs)
    dans un espace `localStorage` dédié (`omcha432_geo_presets`, aucune
    collision avec les clés d'OmchaVibe 432 ou des autres modules).

**2 bugs trouvés et corrigés pendant la vérification (avant livraison) :**
- L'Arbre de vie ne produisait que ~13 nœuds par défaut (profondeur
  mappée trop bas) — quasiment invisible à l'écran. Reparamétré pour
  viser un nombre de nœuds dérivé de la Densité déjà existante (comme le
  Fractal), donnant ~120 nœuds par défaut — vraie structure arborescente
  visible immédiatement.
- Les Traînées masquaient progressivement tout le reste du rendu (voir
  point 14 ci-dessus).

Vérifié par tests Playwright : les 6 formes rendent sans erreur, chaque
nouveau curseur/interrupteur modifie bien l'état attendu (`FX3D`, `V`,
`AE`, `COMPOSITE`, `DOF`, `LIGHT`, `TRAILS`, `PARTMODE`, `MANDALA_SYM`),
morphing déclenché par l'évolution auto, presets sauvegarde/rappel/
suppression fonctionnels et fidèles (forme + réglage retrouvés à
l'identique), aucune régression sur l'overlay audio (#87 — le binaural
d'OmchaVibe 432 reste `flowing` tout du long). Captures d'écran
inspectées visuellement pour chacun des 17 points — 2 défauts réels
repérés et corrigés avant cette livraison (voir ci-dessus). Suite de
régression complète (18 fichiers) à 0 erreur JS.

## #89/#90/#91 : OmcVibe 432 — mute maître, persistance random, fréquence libre + volume par oscillateur

Retour terrain, 4 points sur OmcVibe 432 lui-même (pas sur Géométrie) :

1. **Bug : sphère maître impossible à mute/unmute par tap (#89)** — le
   maître était le SEUL oscillateur dont le tap court ouvrait le menu
   rapide au lieu de couper/rétablir directement (les satellites, eux,
   faisaient déjà mute/unmute au tap court depuis #35/#41). Depuis #85,
   TOUTES les sphères démarrent mute par défaut, y compris le maître — cette
   incohérence de geste devenait un vrai bug d'usage quotidien. Corrigé :
   le maître suit désormais EXACTEMENT le même geste que les satellites
   (tap court = mute/unmute, appui long = menu rapide). Le random global
   auparavant lancé par l'appui long sur le maître reste accessible via le
   bouton dédié "⚄ Omchaléatoire" déjà existant — rien n'est perdu.
2. **Bug : les modes de tirage (Ratio⌀/Identique/Harmonique/Personnalisé)
   semblaient se décocher, combinés à un Caractère (Apaisé/Équilibré/
   Immersif) (#90)** — vérifié : en session, les deux réglages persistent
   déjà parfaitement à travers des tirages répétés (aucun code ne les
   réinitialise). Le vrai bug survient après un RECHARGEMENT de l'app
   (mise en arrière-plan Android, redémarrage) : `_syncRandOptsUI()`
   resynchronisait déjà le bouton Caractère actif après un `loadState()`,
   mais PAS le bouton de mode de tirage — le réglage réel restait bien
   chargé (le prochain tirage l'utilisait correctement) mais le bouton
   affichait "Ratio⌀" par défaut, donnant l'impression trompeuse d'un
   choix perdu. Corrigé : `_syncRandOptsUI()` resynchronise maintenant
   aussi le bouton de mode de tirage (et l'indice "Mode Personnalisé
   actif" associé) à partir de `RAND_OPTS.ratioMode`.
3. **Fréquence libre par oscillateur + raccourci menu (#91)** — dans la
   table Options Aléatoire, chaque ligne a maintenant : la fréquence
   elle-même touchable pour saisir une valeur EXACTE en Hz (calcule le ×n
   nécessaire pour l'atteindre, sans jamais appliquer la marge de sécurité
   36 Hz du tirage aléatoire — comme déjà pour les éditeurs ratio/×n
   existants, une saisie manuelle reste toujours volontaire et n'est
   jamais contrainte par cette marge), deux boutons −9 Hz / +9 Hz pour un
   ajustement rapide, et un raccourci "☰" qui ouvre directement le menu
   rapide de cet oscillateur (solo, FX, random ciblé, modal complet).
   Fonctionne aussi pour le maître (retune `masterFreq` directement, pas
   de modèle ratio×n pour la racine).
4. **Barre de volume large sous chaque ligne (#91)** — curseur pleine
   largeur, branché sur `setVolP()` (même mécanisme que le fader du
   panneau oscillateur complet), hérite du style fader agrandi commun à
   toute l'app plutôt que d'en redéfinir un plus petit ici (cohérence
   tactile).

Vérifié par tests Playwright : tap court sur le maître bascule
`mutedOscs` (true→false→true), appui long ouvre bien le menu rapide sans
toucher au mute ; mode de tirage + caractère survivent à un rechargement
complet AVEC bouton correctement resynchronisé (testé Harmonique+Apaisé et
Personnalisé+Immersif) ; saisie de fréquence directe sur une paire
satellite volontairement à moins de 36 Hz d'une autre confirmée non
bloquée ; ±9 Hz et saisie directe fonctionnent pour le maître et les
satellites, respectent le verrou ; curseur de volume branché et
resynchronisé sans interférer avec un glissé en cours. 0 erreur JS, 0
régression sur l'ensemble des suites existantes.

## #92 : Chaharmony — nouvelle boîte à rythme intégrée (108 pas)

Nouveau module dans `chaharmony.html`, accessible via un 8ᵉ onglet
"▦ RYTHME" à côté des 7 oscillateurs existants (même page, aucune
navigation, aucun état oscillateur touché).

**Important — presets Roland TR-8 :** je n'ai ni les samples ni le code
Roland (propriété de Roland, absents de ce dépôt) et je ne peux pas les
fabriquer à partir de rien sans reproduire du contenu protégé. Les 9 voies
utilisent par défaut des sons **synthétisés en Web Audio, "dans l'esprit"
TR-8/909** — techniques de synthèse génériques et bien connues
(oscillateur + enveloppe pour kick/conga, bruit filtré pour snare/clap/
rimshot, banc d'oscillateurs carrés à ratios inharmoniques passe-haut pour
hi-hats/cymbale) — aucun sample ni algorithme Roland copié. L'import de
samples perso (ci-dessous) permet de remplacer n'importe quelle voix par
ses propres sons.

1. **108 pas = 3 banques de 36** — grille 12×3 par banque, boutons A/B/C
   pour naviguer. Le playhead pendant la lecture change automatiquement de
   banque affichée pour suivre la position réelle sur les 108 pas.
2. **9 voies** : Kick, Snare, HH Fermé, HH Ouvert, Cymbale, Conga Grave,
   Conga Aigu, Clap, Rimshot. Tap sur une voix = mute/unmute direct (même
   geste que les sphères d'OmcVibe/le mute des oscillateurs) ; appui long
   = sélectionne cette voix pour l'éditer dans le séquenceur (la grille
   12×3 affiche alors SES 36 pas de la banque courante).
3. **Auto-assign** — intervalle réglable (1, 5, 9, 13… n'importe quel N) :
   place un coup tous les N pas sur les 108 pas de la voie active en un
   tap. Un bouton "Effacer" vide entièrement la voie active.
4. **Import sample + découpe précise** — bouton "⚙" sous chaque voix,
   ouvre un panneau avec import de fichier audio (`decodeAudioData`),
   waveform dessinée sur canvas, deux poignées glissables (souris/tactile)
   pour le point de début/fin, lecture d'aperçu, et retrait du sample
   (retour au son synthétisé). Aucune restriction de plage — la découpe
   va du sample entier à quelques millisecondes.
5. **FX par voix** — même trio HPF/BPF/LPF (cutoff/résonance) que les
   oscillateurs, + Delay et Reverb (bus de reverb PARTAGÉ entre les 9
   voies — un seul convolveur, pas 9, pour le coût CPU ; seul le niveau
   d'envoi change par voix) + Distortion et Overdrive comme deux étages
   de saturation indépendants (écrêtage dur vs. saturation tanh chaude).
6. **Tap tempo** — bouton dédié, calcule le BPM à partir de la moyenne
   des derniers intervalles entre taps (40–240 BPM), réinitialise si
   l'écart entre deux taps dépasse 2s.
7. **Scheduler à anticipation** (lookahead 120ms, vérifié toutes les 25ms)
   plutôt qu'un simple `setInterval` — évite la dérive de timing classique
   des scheduler JS naïfs. Route dans le MÊME bus master que les
   oscillateurs (donc protégé par la même chaîne anti-clip), fonctionne
   indépendamment de l'état Flux des oscillateurs.

Vérifié par tests Playwright : grille (36 cellules/banque, 108 pas
total), bascule de pas + correspondance banque→index global correcte,
auto-assign produit exactement les indices attendus (tous les N sur 108),
tap = mute/unmute, appui long = sélection de voix, lecture démarre/
avance/s'arrête correctement (AudioContext `running`), tap tempo converge
vers le bon BPM, réglages FX appliqués aux nœuds live, import d'un vrai
fichier WAV + glissé de poignée de découpe fonctionnel (testé avec un
fichier généré, valeurs de trim vérifiées), lecture d'un sample importé
via le scheduler sans erreur, aucune régression sur les 7 oscillateurs
existants (fréquence/binaural/Δ/filtre/reverb/delay/mute, bascule entre
tous les onglets). 1 bug visuel trouvé et corrigé avant livraison (bouton
"Fermer" du panneau FX hérite du style navigateur par défaut faute de
reset CSS sur un `<button>` — remplacé par un `<div>`, cohérent avec le
reste du panneau). 0 erreur JS.

## #93 : OmchaWatch — nouveau module d'horloge solaire fractale (v1)

Nouveau module `omchawatch.html`, 4ᵉ carte du lanceur (`index.html`),
navigation classique (comme Chaharmony) puisqu'il n'y a pas de flux audio
à préserver en arrière-plan.

Première implémentation du modèle discuté avec l'utilisateur : cadran
fixe à 432 repères (108 lever / 216 zénith / 324 coucher / 432 zénith
polaire), entouré d'anneaux concentriques représentant les échelles
fractales imbriquées (Solônde → Chavibe (cV) → Omc → OmcV « jour » →
Année (360 OmcV) → Cycle organique (432 OmcV) → Grand cycle (15552
OmcV)), chacun animé par le même micro-cycle respiratoire à 4 temps
9×[11/10, 6/5, 10/11, 5/6] — dont le produit vaut exactement 1, donc
chaque anneau revient pile à son rayon de départ en fin de cycle (vérifié
en test). Centre doré + mini mandala fleur-de-vie (compas/règle, 6+12
pétales), croisillon doré reliant les 4 repères solaires — reprend
l'esthétique de la référence fournie (anneaux imbriqués + sphère dorée
centrale) sans dépendre du moteur 3D de `geometrie.html` (rendu 2D canvas
autonome, léger).

1. **Calage solaire du jour** — 4 boutons tap (Lever/Zénith/Coucher/
   Zénith polaire) enregistrent l'heure réelle de l'observation
   (`Date.now()`, persisté en `localStorage`). Une aiguille sur l'anneau
   OmcV indique la position solaire actuelle interpolée entre les repères
   calés ; le zénith polaire est estimé (coucher + demi-écart lever/
   zénith) si non calé manuellement.
2. **Jauge de densité du jour** — ratio après-midi/matin (coucher−zénith
   sur zénith−lever) une fois les 3 taps posés : proche de 1 = centre
   stable (396), >1 = tendance expansion (6/5), <1 = tendance contraction
   (5/6). C'est la seule mesure exploitable sans référence externe
   (conforme à la contrainte du modèle : uniquement lever/zénith/coucher
   observés, aucun GPS ni horloge 24/60/60).
3. **Personnalisation J0** — champ date de naissance → J0 = naissance −
   288 jours (persisté), affiche les jours écoulés depuis J0 et la
   position dans le cycle année (360) et cycle organique (432).
4. **Référence zéro** — équinoxe du 20 mars 2026, affichée en rappel.

Explicitement une v1 : la vitesse/amplitude de respiration des anneaux
est un choix esthétique (les vrais rapports d'échelle, ex. 15552³, ne
sont pas transposables à un rendu visuel perceptible), et la latitude/
longitude par gnomon discutées en amont ne sont pas encore câblées — la
prochaine itération pourra les ajouter une fois le calage solaire de base
validé à l'usage.

Vérifié par tests Playwright : rendu canvas non vide, taps de calage
mettent bien à jour l'état + l'UI + `localStorage`, calcul de densité et
détection du quart solaire courant corrects, réinitialisation du calage
fonctionnelle, calcul J0 vérifié indépendamment (naissance 15/06/1990 →
J0 31/08/1989), 4ᵉ carte du lanceur présente et route bien vers
`omchawatch.html` sans régression sur les 3 modules existants. 0 erreur
JS.

**APK** : `omchawatch.html` ajouté à `NEW_FILES` de `repack.py` (même
mécanisme que Chaharmony/Sphère432/Torsion432/Géométrie), `index.html`
repris tel quel (contient déjà la 4ᵉ carte). `build/OmcVibe432-debug.apk`
régénéré et re-signé (V1 jarsigner + V2 apksig par-dessus, même pipeline
que d'habitude) ; vérifié `verified=true`/`v2=true` pour API 24+, identique
au comportement de l'APK précédemment livré (`v1=false` par construction
de ce pipeline — limite déjà documentée, pas une régression). Contenu du
fichier dans le zip vérifié (`assets/public/omchawatch.html` présent,
lien vers le module bien présent dans `index.html`).

## #94 : OmchaWatch v2 — refonte 3D complète (retour utilisateur)

Retour direct après livraison de la v1 : "aucun chiffre, aucun flux et
aucun repère" (trop superficiel), mauvaise structure d'anneaux (7 au lieu
de la vraie hiérarchie), et demande explicite de repartir du moteur 3D de
`sphere432.html` plutôt que du canvas 2D plat de la v1.

**Moteur 3D** : `proj(x,y,z)` (rotation Euler + perspective à FOV variable
selon la profondeur) copié à l'identique de `sphere432.html`, même
mécanique de glisser-orbiter/molette-pincement-zoom (`CAM.rx/ry/zoom` avec
lissage exponentiel vers une cible `trx/try_/tzoom`). Deux canvas
superposés (fond étoilé ou image importée + scène horaire), comme le
`cvUniverse`/`cvSphere` de la référence.

**Anneaux (6, pas 7)** — chacun avec une lecture chiffrée réelle, pas
décorative :
1. **Solônde (centre)** — sphère dorée agrandie (~3× la v1), affiche la
   lecture "Omc" en cours à 0.1 près (ex. `317.2`). Réutilise le moteur
   de calage solaire de la v1 (`currentSolarStep()`, 0-432 par
   interpolation lever→zénith→coucher→zénith polaire) ; sans calage,
   estimation depuis minuit local, étiquetée "estimation" pour rester
   honnête sur la précision.
2. **Chavibe (cV)** — anneau fin, indicateur qui boucle une fois par
   unité Omc entière, affiche le dixième en pourcentage (`+20%` pour
   `324.2`), exactement l'exemple donné.
3. **Omc** — anneau principal, le plus large/visible : 432 micro-sphères
   (12 secteurs de 36, séparateurs visuels), dégradé arc-en-ciel complet
   (teinte = position/432), s'allument progressivement et RESTENT
   allumées jusqu'à la fin du cycle du jour (flux cumulatif, pas un
   simple curseur). Repère organique : mini Merkaba 3D (deux tétraèdres
   entrelacés, tournant sur lui-même) posé sur l'anneau comme landmark.
4. **Année (360)** — 10 sphères (10×36 jours), palette inspirée des 4
   saisons (vert printemps → or été → rouge automne → bleu hiver),
   volontairement plus légère/discrète que l'anneau Omc.
5. **Cycle organique (432)** — en option, actif seulement si J0 est
   réglé. Rendu en spirale non refermée plutôt qu'un cercle : même
   vitesse angulaire que l'anneau Année, donc au bout de 432 jours elle a
   parcouru 432/360 tours — le tracé déborde visiblement de 20% au-delà
   d'un tour complet (léger décalage de rayon/hauteur), ce qui illustre
   concrètement "un cercle qui ne suit pas la linéarité du cercle" sans
   rien inventer d'arbitraire.
6. **Grand cycle (15552)** — en option (J0), 36 divisions (1 par cycle
   organique de 432 jours), regroupées par 3 → 12 couleurs arc-en-ciel
   (15552 = 432×36 = 12 groupes de 3).

**Réglages visuels** ("chaque paramètre observable modulable") : fond
d'écran personnalisable par import d'image (persistée en `localStorage`
si < ~3,8 Mo), interrupteur par anneau, intensité lumineuse globale,
rotation automatique de la caméra on/off, réinitialisation de la vue. Le
calage solaire et le panneau J0 de la v1 sont conservés à l'identique,
déplacés dans des feuilles coulissantes (☉ et ⚙) pour laisser le cadran
3D en plein écran.

Vérifié par tests Playwright : rendu non vide, calcul Omc calibré vs
estimation, calcul J0/cycles identique à la v1 (`Cycle 32 · 90/432` pour
la même date de test), position année depuis l'équinoxe correcte
(132/360 le 30/07/2026, vérifié par calcul manuel), glisser-orbiter et
molette-zoom modifient bien la caméra, bascule de visibilité d'anneau et
intensité fonctionnelles, réinitialisation de vue. 0 erreur JS. APK
régénéré avec le même pipeline `repack.py`/`jarsigner`/`apksig` que
d'habitude, vérifié `verified=true` (API 24+).

## #95 : OmchaWatch v3 — sphères imbriquées, respiration fractale, flux/particules

Retour direct après la v2 : les anneaux plats ne respiraient pas "comme
dans Sphère", pas de glow flux paramétrable, le centre ne "pulsait pas
comme un battement cardiaque". Demande explicite de repartir du moteur
de `sphere432.html` tel quel plutôt que de réinventer un rendu plus
léger, et d'ajouter un vrai mode minimaliste/mode 3D complet au choix.

**Respiration fractale par niveau** — la déformation à volume préservé
de `sphere432.html` (`BREATH2`/`breathScale()` : Y×s, X·Z×1/√s, avec
s×(1/√s)²=1 donc le volume de chaque sphère est rigoureusement conservé
pendant qu'elle respire) est reprise à l'identique (`sphereBreath()`,
même formule `s = ratio^sin(phase)`), mais appliquée **indépendamment à
chacun des 6 niveaux de densité** plutôt qu'à toute la scène d'un bloc —
chaque niveau a sa propre période (2,2s pour Solônde → 38s pour le grand
cycle), ce qui matérialise littéralement la respiration fractale
(mêmes proportions, vitesses différentes à chaque échelle).

**Sphères imbriquées** (au lieu des anneaux plats de la v2) : les 6
niveaux redeviennent des sphères concentriques centrées sur l'origine,
comme dans `sphere432.html`. Chaque "sphère de densité" (Omc, Année,
Grand cycle) est peuplée par une répartition en spirale dorée
(`fibPoint()`, même technique que les distributions de points uniformes
sur sphère) — 432/10/36 points selon le niveau, chacun = une unité de
densité, toujours avec l'allumage cumulatif arc-en-ciel déjà en place.
Le cycle organique (432) devient une vraie spirale 3D pôle-à-pôle sur la
sphère plutôt qu'un tracé plat — la cadence angulaire reste calée sur
l'anneau Année (360), donc elle déborde visiblement du pôle après un
tour complet : "un cercle qui ne suit pas la linéarité du cercle" rendu
en 3D plutôt qu'en 2D.

**Flux paramétrable sur l'anneau Omc** (le "focus" demandé) : portage
direct de `drawFlux()` de `sphere432.html` (mêmes formules de rayon/angle
oscillants, même halo `shadowBlur` en bout de traînée) — c'est le "glow
très flash mais pas agressif" demandé, avec un curseur d'intensité dédié
dans les réglages (indépendant de l'intensité globale).

**Particules ambiantes** : portage direct de `drawParticles()` de
`sphere432.html` (tri par profondeur pour l'ordre de dessin, pulsation de
taille, halo), une population dédiée par niveau de densité (plus dense
sur Omc), réglable/désactivable.

**Sphère solaire centrale — battement cardiaque** : `heartbeat()` simule
un vrai battement (deux impulsions gaussiennes rapprochées, "lub-dub",
rythme délibérément calme ~52/min) qui pilote la taille, le halo et la
teinte (plus chaude au pic) de la sphère dorée ; chaque battement émet un
anneau doré qui se propage et s'estompe vers l'extérieur — la "danse
cosmique" reliant visuellement le centre au reste du cadran.

**Détail géométrique** : bras en spirale logarithmique en arrière-plan
(même technique que `buildMC()` de `geometrie.html`), très faible
opacité, purement décoratif, activable/désactivable.

**Deux modes, un raccourci direct** dans la barre du haut (icône ◉,
bascule immédiate sans passer par les réglages) : "minimaliste" (le rendu
v2, anneaux plats, conservé intact comme option légère) et "3D complet"
(nouveau rendu ci-dessus, actif par défaut). Chaque sphère de densité
reste affichable/masquable indépendamment dans les deux modes (repris de
`V.vis[]` de `sphere432.html`).

Vérifié par tests Playwright : rendu non vide en mode 3D, formule de
respiration mathématiquement vérifiée (volume préservé, s×(1/√s)²=1),
répartition en spirale dorée vérifiée par calcul indépendant (premier
point), battement cardiaque vérifié à deux instants (pic vs creux),
78 particules et 14 flux initialisés comme attendu, bascule
minimaliste↔3D dans les deux sens, réglages flux/particules/détail
géométrique fonctionnels, aucune erreur JS après plusieurs secondes
d'exécution continue. Captures d'écran (face + orbite + zoom) vérifiées
visuellement. APK régénéré et re-signé avec le même pipeline.

## #96 : OmchaWatch v4 — pivot sur le moteur de geometrie.html

Nouveau retour, plus net que les précédents : "ça me plaît pas du tout".
Question de clarification posée avant de retenter à l'aveugle — réponse :
le problème n'était pas le style ni le fouillis, c'était de ne pas être
assez proche des HTML de référence (l'app avait reconstruit une
approximation du moteur de `sphere432.html` au lieu de littéralement
partir du fichier). Décision de l'utilisateur ensuite : repartir de
`geometrie.html` directement, désigné comme le plus abouti visuellement
des trois modules de référence.

**Choix technique** : `geometrie.html` est *purement visuel* (aucun
moteur audio, contrairement à `sphere432.html`) — c'est ce qui a permis
cette fois de littéralement forker le fichier (`cp`) et de greffer
l'horloge dessus par ajouts, sans aucune suppression risquée de code
audio profondément imbriqué (le problème rencontré en tentant d'abord de
partir de `sphere432.html`, abandonné à mi-parcours à la demande de
l'utilisateur). Tout le moteur FX3D existant (6 formes, mandala sacré
génératif, particules orbitales/boids/traînées, distorsion organique,
vibration, profondeur de champ, lumière rotative, plan de coupe, formes
composites, évolution automatique morphée, presets, export image) reste
strictement intact.

**Ajouts, tous additifs :**
- **7ᵉ forme "Densité"** (nouvelle, sélectionnée par défaut) —
  `genFormPoints()` étendu avec un cas `'density'` : même répartition en
  spirale dorée que la forme Sphère existante, mais chaque point porte SA
  PROPRE couleur arc-en-ciel (teinte = position/N) et s'allume
  progressivement et cumulativement selon la lecture Omc du jour (0-432,
  calage solaire repris tel quel des versions précédentes). `FX3D.render()`
  étendu pour dessiner ces couleurs par point quand elles existent (les 6
  formes d'origine, qui n'en fournissent pas, sont rendues exactement
  comme avant).
- **Nouvel onglet "☉ Horloge"** (premier onglet, ouvert par défaut) :
  lecture Omc/cV/Année en direct, 4 boutons de calage solaire
  (lever/zénith/coucher/zénith polaire), champ de naissance → J0 avec
  jours écoulés et position dans le cycle organique (432) — texte "Cycle
  N · X/432" comme demandé (quotient ET reste, pas juste le reste comme
  dans une version précédente). Entièrement construit avec les classes
  CSS déjà existantes du fichier (`.pcard`/`.pr`/`.chip`/`.hint`) —
  aucune nouvelle règle de style, fidélité visuelle totale à la
  référence.
- **Centre solaire → battement cardiaque réel** : `drawCenter()` reprend
  la double impulsion gaussienne "lub-dub" (rythme fixe ~52/min,
  indépendant du curseur "Vitesse" décoratif) à la place du simple sinus
  d'origine ; couleur qui vire au chaud sur le pic.
  - `AE.on` (évolution automatique des formes) passé à `false` par
    défaut : le foyer d'OmchaWatch est la sphère Densité, elle ne doit
    pas dériver seule vers une autre forme (réactivable manuellement).
- En-tête ("hdr-sub") affiche la lecture Omc en direct au lieu du
  sous-titre statique d'origine.

Vérifié par tests Playwright : rendu non vide, forme par défaut =
`density`, `AE.on=false` par défaut, calcul Omc/J0 identiques aux
versions précédentes (mêmes valeurs de référence), navigation complète
du tiroir (Horloge/Formes/Paramètres/Effets/Presets), bascule vers une
autre forme (Torus) puis retour à Densité sans erreur, tableau de
couleurs par point bien généré pour la forme Densité, glisser-caméra
fonctionnel, battement cardiaque vérifié à deux instants. 0 erreur JS.
Captures d'écran (vue par défaut, orbite, tiroir Horloge) vérifiées
visuellement — mandala + fond cosmique + points arc-en-ciel + centre
pulsant, cohérent avec l'identité visuelle de `geometrie.html`. APK
régénéré et re-signé avec le même pipeline `repack.py`/`jarsigner`/
`apksig`, vérifié `verified=true` (API 24+).

## #97 : OmchaWatch v5 — anneaux lisibles à la place du nuage de points

Retour sans détour : "on est censé lire quoi sur ton nuage de points
Smarties ?". Diagnostic correct — la forme "Densité" en v4 (répartition
en spirale dorée, 220+ points scattered en 3D) ne portait aucune
structure lisible : rien à suivre du regard, pas de tracé continu, pas
de "aiguille". Ça ne correspondait pas non plus à l'image de référence
(des ANNEAUX concentriques nets, pas un nuage), reposté une 3ᵉ fois par
l'utilisateur pour trancher le doute.

**Changement de nature, pas de degré** : abandon complet du nuage de
points comme représentation de l'horloge. À la place, `drawClockRings()`
dessine de vrais anneaux concentriques — bande arc-en-ciel CONTINUE (144
segments, dégradé de teinte complet sur 360°) dont seule la portion déjà
parcourue est allumée, le reste restant terne. Une tête lumineuse
("comète", cercle blanc au centre d'un halo coloré) marque la position
actuelle sur chaque anneau — c'est elle qu'on lit, exactement comme
l'aiguille d'une montre, pas une position à deviner dans un nuage.
L'anneau Omc (le focus) porte en plus un croisillon doré avec les 4
repères solaires nommés (Lever/Zénith/Coucher/Zén. polaire) aux positions
108/216/324/432, et 144 points dorés le long de son tracé qui s'allument
un à un avec la progression — texture "tissée" comme la référence
visuelle, mais ancrée sur le cercle plutôt que dispersée en 3D. Chaque
anneau affiche aussi sa lecture en toutes lettres à côté (ex. "OMC ·
343.3/432"). 4 anneaux : Omc (le plus intérieur, le plus épais), Année
(360), Organique (432) et Grand cycle (15552) — ces deux derniers
uniquement si J0 est réglé, comme avant.

Deux bugs trouvés et corrigés en testant sur device réel (capture
transmise par l'utilisateur) :
1. **Rayons mal calés à l'échelle de `proj()`** — les premiers rayons
   choisis (0,5 à 1,12) donnaient des anneaux bien plus larges que
   l'écran en largeur (l'app calibre son échelle sur la HAUTEUR de
   l'écran, `sc≈V.zoom×hauteur×0.38`, qui sur un portrait étroit produit
   un rayon en pixels très supérieur à la largeur disponible) — les
   anneaux n'apparaissaient plus que comme des bandes tronquées quasi
   droites. Corrigé : rayons recalés (0,12 à 0,26) pour que le grand
   anneau tienne confortablement dans la largeur du portrait.
2. **Forme décorative par défaut trop chargée** — la sphère par défaut
   (220 points + lignes de connexion) créait un maillage très dense qui
   noyait visuellement les anneaux. Changé pour Métatron (icosaèdre à 12
   sommets fixes, aspect "cube de Métatron" classique) — repère central
   calme qui ne rivalise plus avec le cadran, qui reste le seul élément
   qu'on est censé "lire" en un coup d'œil.

Le code de la forme "Densité" (v4, jugée illisible) est retiré
entièrement — pas de code mort spéculatif à conserver.

Vérifié par tests Playwright + captures d'écran à chaque étape (avant/
après correction des rayons, avant/après changement de forme par
défaut) : anneaux effectivement circulaires et lisibles à l'écran, textes
de lecture visibles pour les 4 niveaux, 0 erreur JS. APK régénéré et
re-signé avec le même pipeline, vérifié `verified=true` (API 24+).

## #98 : OmchaWatch — retrait de toute assomption 24/60/60 cachée

Question directe de l'utilisateur : "comment est calculé le cycle vu
qu'il n'y a aucun repère solaire/lunaire/équinoxe/solstice — une horloge
24/60/60 cachée ?". Audit honnête du code : oui, à trois endroits.
1. Le mode "non calé" de `computeOmc()` lisait `new Date().getHours()`
   (minuit civil local) et divisait par `86400000` — une hypothèse de
   jour fixe, pas une mesure.
2. `computeYearPos()`/`computeJ0Cycles()` divisaient `(maintenant −
   référence)` par `86400000` pour compter des "jours" — même défaut,
   posture calendaire déguisée en solaire.
3. L'interpolation entre deux taps réels (`currentSolarStep()`), elle,
   était déjà honnête — elle mesure la durée RÉELLE entre deux
   événements tapés par l'utilisateur, aucune constante de jour.

**Correction (pas un compromis, un changement de nature)** :
- Le mode "non calé" n'existe plus. Sans repère réel posé, `computeOmc()`
  renvoie `null` et l'anneau Omc l'affiche explicitement ("pas encore
  calé", anneau éteint) plutôt que d'inventer une lecture via l'horloge
  civile du téléphone.
- Nouveau `DAY_LOG` (persisté) : compte les **jours réellement observés**
  — un jour "mesuré" = un nouveau tap "Lever" détecté à plus de 20h du
  précédent (marge large anti-double-tap, jamais un vrai cycle raté).
  Zéro constante de durée.
- `daysSinceRef()` : pour toute période AVANT le premier tap de
  l'utilisateur, il n'existe tout simplement aucune donnée solaire — la
  seule option honnête est une estimation calendaire, clairement
  étiquetée comme telle ("estimation calendaire"). Pour tout ce qui a été
  observé DEPUIS le premier tap, c'est un comptage réel, étiqueté "(N
  mesurés)". Les deux ne sont jamais confondus dans l'UI.

Limite assumée et expliquée à l'utilisateur en amont de ce correctif :
aucun logiciel sur un appareil numérique ne peut s'affranchir d'un
oscillateur physique (le quartz du téléphone) pour mesurer une durée
écoulée — c'est le sablier lui-même, inévitable et sans rapport avec le
24/60/60. Ce qui EST évitable, et qui l'a été ici, c'est l'interprétation
en heure civile et l'hypothèse de durée fixe posées par-dessus.

Vérifié par tests Playwright : `computeOmc()` renvoie bien `{value:null}`
avant tout calage (aucun texte fabriqué), `DAY_LOG.daysTapped` s'incrémente
correctement sur un nouveau cycle simulé (>20h), J0/Année affichent
"(N mesurés)" après calage, retour à "pas encore calé" après
réinitialisation, code source de `computeOmc`/`currentSolarStep` vérifié
sans trace de `86400000` ni `getHours`/`setHours`. 0 erreur JS. APK
régénéré et re-signé, vérifié `verified=true` (API 24+).

## #99 : onde Solônde — compteur de cycles réel, plus une horloge du tout

Suite du #98 : l'utilisateur propose sa propre issue au problème du
sablier — ne plus mesurer "du temps" mais compter des cycles d'une onde
(la Solônde), dont la période vaut "par coïncidence" une demi-seconde.
Objection technique soulevée puis retirée en cours d'échange : fixer une
période à 500ms revient forcément à caler ce nombre sur la seconde SI
(500ms = une demi-seconde par définition), donc annoncer "ce n'est pas
une seconde" sans le dire serait aussi malhonnête que le point #98 —
MAIS rien n'empêche d'assumer ce choix explicitement (la période est une
constante délibérée, pas une redécouverte technique) tout en respectant
la vraie demande : ne plus JAMAIS lire ni afficher quoi que ce soit en
heure civile, ne compter que des cycles.

**Implémentation** : `SOLONDE_MS=500` (documenté comme choix assumé, pas
comme indépendance technique). `solondeCount()` = nombre de cycles
Solônde écoulés depuis J0 (ou, à défaut, le premier repère solaire
jamais posé) — un entier pur, jamais formaté en heure. `solondeMovement()`
répartit ce compteur sur 36 Solônde/tour en 4 blocs de 9, un par
mouvement (Expansion douce 11/10 → Expansion charmante 6/5 → Contraction
douce 10/11 → Contraction charmante 5/6 — l'ordre exact du modèle).
`solondeScale()` reprend le produit cumulatif déjà vérifié pour la
respiration fractale (retour exact à 1 après un tour complet, propriété
mathématique : 11/10 × 6/5 × 10/11 × 5/6 = 1). Le pouls du centre
(`drawCenter`) est désormais piloté par ce compteur réel plutôt que par
un temps d'animation arbitraire — la sphère respire littéralement au
rythme de l'onde Solônde. Nouvel encart "Onde Solônde" dans l'onglet
Horloge : nombre de cycles + mouvement en cours, avec le rappel explicite
que ce n'est pas une horloge.

Vérifié par tests Playwright : compteur `null` tant qu'aucune ancre
n'existe (ni J0 ni premier tap), résout correctement après un tap ou un
J0 réglé, mouvement/bloc identifiés avec la bonne progression locale
(9 Solônde par bloc), `solondeScale(0)=1`, `solondeScale(9)=1.1`
(=11/10 exact), `solondeScale(18)=1.32` (=11/10×6/5 exact),
`solondeScale(36)=1` (retour exact après un tour complet), code source
de toutes les fonctions Solônde vérifié sans aucune trace de
`getHours`/`setHours`/`toLocaleTimeString`. 0 erreur JS. APK régénéré
et re-signé, vérifié `verified=true` (API 24+).

## #100 : fond d'écran personnalisé avec retouche de centrage

Deux demandes en une : un widget Android 2×2 (écran d'accueil) et un
fond d'écran personnalisable avec recentrage. Le widget a été décliné
explicitement — vérification concrète (pas de mémoire) faite dans cette
session : aucun SDK Android installé (`ANDROID_HOME` vide, pas de
`aapt2`/`build-tools`), pas d'`apktool`, et le réseau de l'environnement
rejette explicitement les serveurs de distribution du SDK Android
(`dl.google.com`, `redirector.gvt1.com` → 403 côté passerelle). Un
widget natif nécessite un `AppWidgetProvider` compilé (Kotlin/Java) et
enregistré dans un `AndroidManifest.xml` recompilé — hors de portée du
pipeline de repackaging au niveau fichiers utilisé pour cet APK, quels
que soient les précédents dans d'autres sessions/environnements.

**Fond d'écran personnalisé** (réalisé) : nouvel onglet "🖼 Fond" dans le
tiroir. Import d'image (`FileReader`→dataURL, persistée si < ~3,8 Mo),
mode retouche activé automatiquement après import — les mêmes gestes
qui orbitent normalement la caméra 3D (glisser, pincer/molette)
repositionnent et zooment l'image importée à la place tant que le mode
est actif, sans toucher à la caméra. Le tiroir se ferme automatiquement
à l'import pour voir l'écran en entier pendant le cadrage. `background-
position` en pourcentage pour le centrage, `transform:scale()` sur
`#cosmic-bg` pour le zoom (60-300%, curseur ou pincement) — persistés
ensemble en `localStorage`, restaurés identiques au rechargement.
Réversible à tout moment ("Revenir au fond cosmique").

Vérifié par tests Playwright : import déclenche bien le mode retouche +
fermeture du tiroir, glisser modifie l'offset du fond SANS changer
`V.trx`/`V.try_` (caméra 3D confirmée inchangée pendant la retouche),
molette modifie le zoom, état intégralement restauré après rechargement
de page (image, offset, zoom identiques), réinitialisation revient bien
à `img/cosmic-flux.jpg`. 0 erreur JS. APK régénéré et re-signé, vérifié
`verified=true` (API 24+).

## #101 : unité Miaow — 1er générateur de fréquence en Miaow (Chaharmony + OmcVibe)

Suite directe du travail sur l'onde Solônde : puisque la Solônde est
désormais 1 seconde SI "revivifiée" (et non une durée indépendante), et
qu'il y a 15552 Solônde dans 1 cV, la fréquence associée à une Solônde
vaut 15552 Hz — définie comme **1 Miaow**. Vérifié par le calcul : 432 Hz
= 1/36 Miaow exact, 108 Hz = 1/144 Miaow exact (15552 = 432×36 = 108×144),
396 Hz = 11/432 Miaow, 360 Hz = 5/216 Miaow. Demande explicite : « on ne
parle plus en Hz [...] pour faire le 1er générateur de Fréquence Miaow »,
appliqué à Chaharmony ET OmcVibe.

**Chaharmony** (`chaharmony.html`) : `MIAOW_HZ=15552`, `UNIT_MODE`
persisté (`chaharmony-unit`), `hzToMiaow`/`miaowToHz`, `toggleUnitMode()`
déclenché en tapant sur le label d'unité (`.freq-hz`) sous chaque
oscillateur. Affichage principal (`.freq-num`) et curseur (min/max)
convertis selon le mode ; en mode Miaow, le libellé secondaire rappelle
la valeur Hz (`🐈 Miaow · 432.0Hz`). Saisie manuelle de fréquence
convertie dans les deux sens à l'entrée/sortie de l'édition.

**OmcVibe** (`index.html`) : même paire `MIAOW_HZ`/`UNIT_MODE` (persisté
`fbf432-unit`), portée volontairement limitée à l'affichage/saisie de la
**fréquence maître** (`#ms-freq`, `openFreqEdit`/`exitEditMaster`) — les
fréquences des paires satellites et les mirroirs `master-input`/
`dcell-purple-input` restent en Hz brut, le moteur audio (Web Audio)
n'ayant de toute façon jamais eu besoin de connaître le Miaow. Nouveau
sélecteur Hz/🐈Miaow dans le panneau « Personnaliser l'interface »
(réutilise le style `.cust-bg-grid`/`.cust-bg-btn` existant).

**Bug trouvé et corrigé en cours de route** : `updatePairUI(i)`, appelée
en boucle sur toutes les paires par `updateDisplay()`, réécrivait
`#ms-freq` juste après mon nouvel affichage — mais avec `fmtShort(pF)`
(toujours en Hz brut, `.toFixed(1)`), quel que soit `UNIT_MODE`. Résultat
observé : "432.0" au lieu de "432" en mode Hz, et aucune conversion en
mode Miaow. Corrigé en faisant utiliser à cette écriture la même fonction
centrale `fmtMasterFreqDisplay()` que partout ailleurs. Un second site
d'écriture (`onMasterInput`, aperçu live pendant le glissé du curseur
`#master-input`) a été aligné de la même façon pour rester cohérent
pendant le geste, avant la validation par `onMasterChange`.

Vérifié par tests Playwright (Chaharmony et OmcVibe séparément) :
`hzToMiaow(432)===1/36` exact, bascule d'unité + persistance
`localStorage`, édition manuelle en mode Miaow round-trip correcte
(0.02778 Miaow → 216 Hz sur OmcVibe, → ~432 Hz sur Chaharmony), onglets/
tabs toujours fonctionnels, `PAIRS.length` inchangé (7), panneau de
personnalisation toujours valide. 0 erreur JS sur les deux apps. APK
régénéré et re-signé, vérifié `verified=true` (API 24+).

## #102 : correction du ratio Miaow — 512/243 au lieu de 1/15552

Retour utilisateur immédiat après #101 : la définition voulue n'est pas
« 1 Miaow = 15552 Hz » (qui réduit 432 Hz à une fraction minuscule,
1/36 Miaow) mais l'inverse évoqué plus tôt dans la session — **1 Miaow =
243/512 Hz**, soit **512/243 ≈ 2,10699588477 Miaow par Hz** (fraction
exacte 32768/15552 réduite ; 32768 = fréquence du cristal RTC standard,
15552 = Miaow par Solônde). Avec ce ratio, 432 Hz devient **8192/9 ≈
910,22 Miaow** — la plage audible 20–20 000 Hz devient environ 42 à
42 140 Miaow, avec une résolution numérique environ doublée par rapport
au Hz (chaque Hz vaut ~2,107 Miaow).

**Implémentation** : `MIAOW_HZ` remplacé par `MIAOW_PER_HZ = 512/243`
dans les deux apps ; `hzToMiaow(hz) = hz * MIAOW_PER_HZ`,
`miaowToHz(m) = m / MIAOW_PER_HZ`. Précision d'affichage réduite de 5 à
2 décimales partout (`.toFixed(5)` → `.toFixed(2)`) — les valeurs
étant désormais de l'ordre de plusieurs centaines à dizaines de milliers,
5 décimales n'apportaient plus rien. Textes d'aide (encart Chaharmony,
panneau « Personnaliser l'interface » d'OmcVibe) mis à jour avec la
nouvelle formule et l'exemple 432 Hz = 8192/9 Miaow.

Vérifié par tests Playwright (Chaharmony et OmcVibe) : `hzToMiaow(432)
=== 910.2222...` (8192/9 exact), `miaowToHz(8192/9) ≈ 432`, édition
manuelle en mode Miaow round-trip correcte vers 216 Hz sur les deux apps,
bascule d'unité + persistance intactes, 0 erreur JS. APK régénéré et
re-signé, vérifié `verified=true` (API 24+).

## #103 : presets « rock star binaurale » 432/396/360 Miaow + plage adaptée

Deux demandes liées : (1) le champ de saisie de la fréquence maître
(OmcVibe) gardait des bornes `min`/`max` en Hz même en mode Miaow — un
utilisateur tapant en Miaow voyait un plafond HTML toujours annoncé
« 432 » alors que ce nombre-là signifie maintenant tout autre chose (le
nouveau nombre sacré 432 **Miaow** ≈ 205 Hz, à ne pas confondre avec
l'ancien plafond 432 **Hz** ≈ 910 Miaow) ; (2) demande explicite de faire
de 432 / 396 / 360 **Miaow** — pensés directement dans cette unité, pas
convertis depuis les anciens 432/396/360 Hz — les nouvelles fréquences de
référence de l'app.

**Plage adaptée** (`index.html`, `openFreqEdit()`) : `min`/`max`/`step`/
`placeholder` de `#freq-input-master` recalculés à l'ouverture de
l'édition selon `UNIT_MODE` — bornes Hz (`F_MIN`–432) en mode Hz, bornes
Miaow (`hzToMiaow(F_MIN)`–`hzToMiaow(432)` ≈ 113,78–910,22) en mode
Miaow.

**Presets rock star** : nouvelle fonction `setMasterFreqMiaow(miaowVal)`
(OmcVibe) / `setOscFreqMiaow(i, miaowVal)` (Chaharmony), toutes deux de
simples wrappers `setMasterFreq(Math.round(miaowToHz(v)))` /
`setOscFreq(i, miaowToHz(v))` — aucune nouvelle logique de tuning, juste
la conversion. Trois boutons 432/396/360 (→ 205,0/187,9/170,9 Hz, tous
confortablement dans la plage existante 54-432 Hz / 54-864 Hz, donc pas
besoin d'élargir le moteur audio) : sur OmcVibe dans le panneau
« Personnaliser l'interface », juste sous le sélecteur Hz/Miaow ; sur
Chaharmony directement sous le curseur de fréquence de chaque
oscillateur (s'applique à l'oscillateur affiché).

Vérifié par tests Playwright : `miaowToHz(432)===205.03125`,
`miaowToHz(396)===187.9453125`, `miaowToHz(360)===170.859375` (exacts),
`setMasterFreqMiaow(432)` → `masterFreq===205`, bornes de
`#freq-input-master` correctement basculées entre mode Hz (54/432) et
mode Miaow (113.78/910.22), boutons presets présents et fonctionnels sur
les deux apps, 0 erreur JS. APK régénéré et re-signé, vérifié
`verified=true` (API 24+).

## #104 : le Miaow devient l'unité définitive d'OmcVibe

Demande explicite et sans ambiguïté : « l'unité de mesure sur OmcVibe
devient définitivement le Miaow, c'est pas une blague. Tout doit être
converti en Miaow. » Jusqu'ici (#101-#103) la conversion ne touchait que
la fréquence maître, en Hz par défaut, Miaow en option. Ce soir : Miaow
devient le mode par défaut (`UNIT_MODE='miaow'` sauf choix explicite
contraire déjà enregistré), et la conversion s'étend à **toutes** les
lectures de fréquence visibles de l'app, pas seulement le nombre
central.

**Étendu à** : labels de fréquence sur chaque sphère satellite
(`vp-pf-i`, le plus visible de tous), formule masterFreq×ratio×n=résultat
de chaque oscillateur (`bmf-i`/`bfr-i`/`ibf-p-i`/`ibf-r-i`, via `fmtFreq`/
`fmtShort` désormais unit-aware), table du tirage aléatoire et vue
compacte (`patchRandomTable`/`patchFreqMini`), afficheur de la
Progression Harmonique, barre d'info (`ib-freq`/`hdr-freq`), noms par
défaut et badges des presets sauvegardés. Le mini-panneau `#master-input`
et la cellule violette du dock (`#dcell-purple-input`) basculent aussi
leurs bornes/valeurs affichées, avec la même logique de bornes que
`#freq-input-master` (#101).

**Choix de conception, assumé** : les *pas* de nudge (`masterStep`
±18/36/72/108, `_purpleFreqStep` ±9) restent des pas en Hz, inchangés —
ce sont des incréments harmoniques pensés comme fractions de 432 Hz, les
convertir aurait cassé leur logique musicale. Seul ce qui est *affiché*
change ; ce qui est *retuné* reste en Hz, comme le veut le moteur Web
Audio. Volontairement laissés en Hz (hors sujet du Miaow, qui ne
concerne que la hauteur/le "carrier") : le Δ binaural (battement,
`ibf-d-i`/`sv-delta-drift`/LFO), les fréquences de filtre (Cutoff/HPF),
et la fréquence fondamentale détectée d'un sample importé (feature #77,
déjà signalée instable côté #55 — non retouchée pour ne pas mélanger les
deux chantiers).

Vérifié par tests Playwright : `UNIT_MODE==='miaow'` sur une install
neuve (localStorage vidé), lecture d'une sphère satellite exacte dans les
deux sens (Hz→Miaow et retour, `hzToMiaow(calcPFreq(0))` ===
texte affiché, aux deux décimales près), saisie manuelle via
`#master-input`/`#dcell-purple-input` interprétée correctement selon
l'unité affichée, pas ±9 toujours en Hz réel (vérifié à la borne 432),
bascule Hz↔Miaow sans régression sur `PAIRS.length`/panneau
personnalisation, 0 erreur JS. APK régénéré et re-signé, vérifié
`verified=true` (API 24+).

## #105 : raccourci dock — retour au menu principal (OmcVibe)

Demande : un raccourci depuis OmcVibe pour revenir au lanceur 3 modules
sans quitter l'app. `_launcherGo('vibe')` masquait déjà le lanceur au
lancement (`classList.add('hide')` + `display:none` après transition)
mais rien ne faisait l'inverse — nouvelle fonction `_showLauncher()`
(remet `display:flex`, retire `.hide` au frame suivant pour laisser la
transition CSS jouer), sans jamais recharger `index.html` : le binaural
continue de tourner en dessous exactement comme au premier lancement.

Ajouté au registre existant `DOCK_ACTIONS` (`backToMenu`, "↩ Retour au
menu principal") plutôt qu'un bouton dédié codé en dur — cohérent avec
tous les autres raccourcis du dock (personnalisables tap/appui long/
bouton extra depuis Paramètres → Personnaliser l'interface). Mappé par
défaut sur le bouton extra de la case Flux (`flux.extra`), jusqu'ici
`'none'` — donc disponible immédiatement sur une installation neuve, sans
toucher aux 14 autres raccourcis déjà assignés par défaut. **Sur un
appareil qui a déjà de l'usage** (mapping déjà enregistré en
localStorage), le nouveau défaut ne s'applique pas automatiquement : il
faut l'assigner soi-même dans Personnaliser → Case → slot (ou
"Réinitialiser" la carte du dock).

Vérifié par tests Playwright : `DOCK_ACTIONS.backToMenu` enregistrée,
`DOCK_MAP_DEFAULT.flux.extra==='backToMenu'` sur install neuve, cycle
complet `_launcherGo('vibe')` → lanceur masqué → `_runDockAction
('backToMenu')` → lanceur réaffiché → re-`_launcherGo('vibe')` → masqué
à nouveau, sans erreur ; confirmation que le bouton extra réel du dock
route bien vers `_runDockAction(map.extra)` (même mécanisme que
tap/appui long, lu dans `_bindDock()`). 0 erreur JS. APK régénéré et
re-signé, vérifié `verified=true` (API 24+).

## #106 : OmchaWatch — SOLONDE_MS corrigé par le ratio fractal 12/11

Retour sur un point resté en suspens depuis la nuit précédente : la plus
petite unité du système (`SOLONDE_MS`, jusqu'ici une demi-seconde brute,
500) restait un nombre "mort", non texturé par le reste du modèle
fractal — incohérent avec le principe déjà posé pour la Solônde elle-même
("on ne remplace pas la seconde, on la revivifie"). Proposition de
l'utilisateur : appliquer à cette brique le même ratio 12/11 déjà utilisé
ailleurs dans la numérologie du modèle (correction 365,2524j civil → 360
OmcV), plutôt que de la laisser telle quelle.

Deux paramètres restaient à trancher (base 500 vs 1000ms, direction
12/11 vs 11/12) — posés à l'utilisateur plutôt que devinés, vu l'impact
sur tout le système de comptage. Choix retenu : **500 × 12/11 ≈ 545,45
ms**. `SOLONDE_MS` passe de la constante littérale `500` à l'expression
`500*12/11`, gardée non arrondie dans le code pour que le raisonnement
reste lisible à la source. Aucune autre fonction (`solondeCount`,
`solondeMovement`, `solondeScale`) n'a besoin d'être modifiée — leur
math ne dépend que du *nombre de cycles*, jamais de la durée d'un cycle
en ms, donc les identités déjà vérifiées (retour exact à 1 après 36
cycles, etc.) restent intactes par construction. Texte d'aide du panneau
Horloge et commentaire de code mis à jour pour rester transparents sur
cette convention (ni cachée, ni présentée comme une mesure indépendante
— même standard que l'audit 24/60/60).

Vérifié par tests Playwright : `SOLONDE_MS === 500*12/11` exact,
`solondeScale(0)=1`/`solondeScale(9)=1.1`/`solondeScale(18)=1.32`/
`solondeScale(36)=1` toujours vrais, compteur de cycles réel avancé de 2
ticks sur ~1200ms écoulées (cohérent avec 1200/545,45 ≈ 2,2), 0 erreur
JS. APK régénéré et re-signé, vérifié `verified=true` (API 24+).

## #107 : OmchaWatch — J0 avec heure précise + modèle saisonnier hémisphère

Demande : pouvoir donner un "temps biologique" via J0 + heure précise, et
avoir un repère 108/216/324/432 (lever/zénith/coucher/zénith polaire)
disponible toute l'année, pas seulement quand un calage réel existe. Point
bloquant identifié ensemble : une heure de lever de soleil *réelle*
dépend de la latitude — sans elle, impossible de la calculer honnêtement
(la déclinaison solaire ne dépend que du jour de l'année, mais la traduire
en durée de jour à un endroit précis exige la position). Décision : pas de
GPS, pas de latitude — un **modèle saisonnier assumé**, séparé et
clairement labellisé comme tel, qui ne remplace jamais le calage réel et
ne s'active que si l'utilisateur le demande explicitement.

**Heure de naissance** : nouveau champ `<input type="time">` à côté de la
date, persisté séparément (`omchawatch-naissance-heure`). `J0_DATE` se
recalcule désormais via `_recomputeJ0()` (date + heure, minuit par défaut
si l'heure n'est pas renseignée) — `setNaissance`/`setNaissanceHeure`
partagent ce même calcul.

**Modèle saisonnier** (nouveau toggle "🌊 Modèle saisonnier", off par
défaut) : `theoreticalSolarStep()` calcule une fraction "jour" continue —
`0.5 + signe×0.15×sin(2π×(jour_de_l'année − 79.5)/365.25)` — ancrée
exactement à 216/216 aux deux équinoxes (sin=0 par construction) et
respirant vers ~15,6h/8,4h au solstice le plus favorable selon
l'hémisphère (signe +1 Nord, −1 Sud). L'heure civile locale sert de proxy
approximatif à l'heure solaire — assumé et affiché comme tel, jamais
présenté comme une mesure. `computeOmc()` n'utilise ce modèle qu'en
dernier recours, seulement quand `currentSolarStep()` (le calage réel)
rend `null` — la garantie "0 GPS, 0 horloge civile, vraiment" reste
intacte tant que le modèle n'est pas activé, exactement comme avant
(`omcNullByDefault` vérifié). Nouveau sélecteur d'hémisphère (Nord/Sud,
persisté) détermine le sens de la respiration. `updateClockUI` distingue
maintenant trois états visuellement (calé / modèle saisonnier /
estimation partielle), jamais confondus.

Vérifié par tests Playwright : comportement par défaut inchangé
(`SEASON_MODEL_ON=false` ⇒ `computeOmc().value===null`, régression
zéro), modèle activé donne une valeur 0-432 qui recoupe exactement une
réimplémentation indépendante de la formule, `dayFrac===0.5` pile au jour
de l'équinoxe pour les deux hémisphères, bascule Nord/Sud donne des
lectures différentes (miroir), `J0_DATE` exact avec date+heure
(`1997-06-12T14:32` − 288j), persistance intégrale après rechargement
(saisonnier, hémisphère, J0), 0 erreur JS. APK régénéré et re-signé,
vérifié `verified=true` (API 24+).

**Non retenu pour l'instant, noté pour plus tard** : la proposition d'un
"planisphère rond" (un par hémisphère) comme sélecteur visuel de
latitude approximative — bonne piste si un jour on veut un vrai calcul
astronomique par latitude (option 2 déjà évoquée), et pourrait doubler
comme correcteur rapide en voyage. Pas nécessaire pour le modèle
saisonnier actuel, qui ne demande que l'hémisphère.

## #108 : OmchaWatch remplacé par le design UTC432 (ChaWatch432), amélioré

Rebondissement de la nuit : l'utilisateur a retrouvé un fichier autonome
qu'il avait fait faire il y a un mois ("UTC432 · Project") et réalisé
qu'il contenait déjà presque toute l'architecture conceptuelle
reconstruite ce soir — cycle 432 fractal (12 phases × 36 souffles × 6
instants), le même triangle 396/432/360 utilisé pour les presets Miaow,
les mêmes modes EXPANSIF/NEUTRE/CONTRACTIF que les mouvements Solônde, et
même un mode "brut" (UT18, heure civile sans correction) face à un mode
corrigé — exactement le geste Hz/Miaow. Décision : plutôt que de
continuer à développer le moteur FX3D/3D construit plus tôt cette nuit,
**ce fichier devient le nouveau `omchawatch.html`**, amélioré sur
demande. L'ancienne version (moteur `geometrie.html`, anneaux Omc/cV/
Année/Solônde, calage solaire par tap, J0 personnalisé, modèle
saisonnier hémisphère) reste entièrement récupérable dans l'historique
git — rien n'est perdu, juste remplacé au fichier de tête.

**Retrait de Paris** : l'ancien fichier calculait un vrai midi solaire
via l'équation du temps (formule astronomique réelle, ne dépend que du
jour de l'année — aucun GPS) + une longitude fixée en dur à 2,35°
(Paris) + les règles de l'heure d'été française câblées en dur
(`frenchOffset`/`frenchNow`, tout un mécanisme de reconstruction de faux
"temps français"). Remplacé par un calcul générique et plus simple :
heure UTC réelle (`getUTCHours()`, toujours correcte quel que soit le
fuseau du téléphone) + longitude choisie × 4 min/° + équation du temps —
fonctionne n'importe où sur Terre, sans base de données de fuseaux
horaires. Le mode UT18 ("neutre") utilise l'heure civile locale du
téléphone directement (`getHours()`), portable par construction.

**Planisphère** : nouvel onglet "LIEU" — grille équirectangulaire
(pas de tracé côtier réel, juste graticule + 21 villes repères
mondiales, pour rester honnête sur ce qui est vraiment dessiné),
zoomable (molette/pincement, ×1 à ×8), pannable (glisser), tape pour
poser un repère ou pour attraper une ville proche (rayon de capture
14px). Curseurs latitude/longitude manuels en complément pour la
précision. Persisté en local, jamais envoyé nulle part — sert
uniquement à la correction de longitude du midi solaire.

**Fond personnalisé** : import d'image + mode retouche (glisser pour
centrer, molette/pincement pour zoomer 60-300%), repris du même schéma
que la version précédente d'OmchaWatch, adapté à ce fichier (pas de
caméra 3D à ménager ici — le geste de glisser peut se brancher
directement).

**Design** : anneaux repoussés vers le bord (.875/.750/.630 → .94/.80/
.66 de R) pour mieux les séparer et remplir l'espace ; bandes
arc-en-ciel épaissies (5/3.5/2.5 → 7/5/3.5) ; comète (le point lumineux
marquant la position actuelle sur chaque anneau) largement agrandie —
halo ×6.5 (était ×4.5), noyau ×1.3 (était ×0.9), traînée-repère étendue.

Vérifié par tests Playwright : titre sans "Paris", `calcDensity()`
renvoie une valeur valide 0-432, planisphère s'ouvre/se ferme, tap sur
la grille pose bien un repère lat/lon cohérent, "Valider" persiste dans
`LOCATION` (confirmé après rechargement), curseurs manuels fonctionnels,
import/application de fond personnalisé opérationnel, bascule du mode
retouche correcte, 0 erreur JS, aucune requête vers l'ancien
`bg-rotator.css/js` (fichiers qui n'existaient déjà pas dans ce dépôt).
Vérifié aussi visuellement par capture d'écran (rendu horloge + rendu
planisphère). APK régénéré et re-signé, vérifié `verified=true` (API
24+).
