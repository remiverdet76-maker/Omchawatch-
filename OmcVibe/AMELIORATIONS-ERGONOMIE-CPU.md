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
