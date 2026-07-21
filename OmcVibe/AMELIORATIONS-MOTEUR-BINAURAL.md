# OmcVibe 181 — 18 points d'amélioration du moteur binaural

Analyse de l'APK `OmcVibe181-debug-apk (3).zip` (app Capacitor, `org.watcha.omcvibe`,
WebView sur `assets/public/index.html`). Le code source web extrait vit dans ce
dossier (`OmcVibe/www/index.html`) — c'est la vraie source du comportement de
l'app, l'APK n'étant qu'un artefact de build autour de ce WebView.

## Diagnostic du "tremolo" au-delà de Δ > 2 Hz

Le battement binaural *réel* (perçu par le cerveau à partir de deux tons purs,
un par oreille) ne devrait jamais sonner comme un tremolo — c'est un phénomène
neurologique, pas une modulation d'amplitude physique. Le tremolo désagréable
que vous entendiez vient d'ailleurs : **Pingala et Ida (les deux voix d'une
paire) n'étaient panoramisées qu'à ±0,10 autour d'un même centre** (`PAN_SPREAD`),
quel que soit Δ. Avec un `StereoPannerNode` à puissance constante, un écart aussi
faible laisse une part significative des DEUX porteuses dans CHAQUE oreille —
elles s'additionnent alors physiquement et créent une vraie modulation
d'amplitude à la fréquence Δ. En dessous de 2 Hz cette AM est trop lente pour
être perçue comme un défaut ; au-delà, elle devient un tremolo audible qui se
superpose (et masque) le vrai battement binaural. C'est un artefact de mixage,
pas une limite du binaural — le point **#1** ci-dessous le corrige à la racine.

## Les 18 points — tous implémentés dans `OmcVibe/www/index.html`

1. **Correctif racine anti-tremolo (Δ > 2 Hz)** — l'écart stéréo Pingala/Ida
   grandit avec Δ (`binauralPanGap()`, `BINAURAL_GAP_PER_HZ = 0.055`, plafond
   `BINAURAL_GAP_MAX = 0.86`), recalculé et rejoué en douceur
   (`refreshBinauralPan()`) à **chaque** changement de Δ — manuel (`setDelta`,
   `setGlobalDelta`) ou tirage aléatoire, pas seulement au random comme avant.
   Le centre de paire (`spread`) n'est pas touché : seule la distance
   Pingala↔Ida à l'intérieur de la paire s'élargit. Indicateur "Séparation
   anti-tremolo" affiché en direct (vire à l'ambre au-delà de 2 Hz).
2. **LFO respiration par paire, aléatoire à chaque nouveau jeu** — étend le
   patch existant `attachOscVolLFO`/`toggleOscVolLFO` (§2.10) :
   `triggerMagicAuto()` tire, pour chaque paire non verrouillée, un rythme
   (≈4–17 respirations/min), une profondeur et un **déphasage aléatoires
   indépendants** — les paires respirent hors phase, texture organique plutôt
   qu'un pompage synchrone. Option "Respiration par paire (aléatoire)" +
   slider "Profondeur" (`PAIR_BREATH_OPTS.depthMax`).
3. **Détune Sinus Duo paramétrable -72 / +72 ¢** — le moteur `sine2` (deux
   voix sinus) était figé à ±7 ¢. Piloté par `DUALSINE_DETUNE` (slider "Sinus
   Duo"), appliqué à la construction (`sine2Engine()`) et mis à jour **en
   direct** via `setDualSineDetune()` (pas de reconstruction).
4. **Δ indépendant par paire en mode random déverrouillé** — option "Δ varié
   par paire" (`RAND_OPTS.deltaVariety`) : chaque paire pioche son propre
   battement dans le même ensemble musical, au lieu d'un `baseDelta` unique
   imposé à toutes — jeu binaural plus riche.
5. **Dérive lente du Δ ("respiration du battement")** — `DELTA_DRIFT_STATE` :
   un LFO natif Web Audio (cycle de 3 à 7 minutes, phase de départ aléatoire
   par paire) fait onduler très légèrement (± amount Hz réglable) la
   fréquence réelle de l'oscillateur Ida, sans jamais changer de bande
   cérébrale — évite l'habituation, rend la session vivante dans la durée.
   Attaché automatiquement à chaque (re)création d'un node Ida
   (`attachDeltaDriftToNode`, appelé depuis `buildOsc()`).
6. **Décorrélation du drift analogique partagé** — le `_driftLFO` (0,07 Hz)
   reste unique et partagé (simple à gérer), mais chaque oscillateur reçoit
   maintenant le signal via son **propre `DelayNode` à retard aléatoire**
   (`_connectDecorrelatedDrift()`) : même onde lente, déphasée différemment
   par voix — mouvement organique réellement indépendant au lieu d'un
   vibrato d'ensemble parfaitement synchrone.
7. **Polarité variée, même Δ verrouillé** — `RAND_OPTS.polarityVariety` :
   le signe (Ida au-dessus/en-dessous de Pingala) est retiré au hasard à
   chaque tirage **indépendamment du verrou binaural** — ne touche jamais à
   l'amplitude du battement, seulement à l'image spatiale.
8. **Mode "casque strict" (routage 100% isolé)** — `STRICT_HEADPHONE_MODE` :
   Pingala 100% gauche / Ida 100% droite pour chaque paire (`buildOscPan()`
   retourne `[-1,1]` partout). Un `StereoPannerNode` à puissance constante
   n'atteint le silence total sur le canal opposé qu'exactement à pan=±1 :
   ce mode sacrifie l'éventail spatial inter-paires pour une pureté
   binaurale absolue, zéro résidu de crosstalk.
9. **Compression douce anti-pompage par paire** — `PAIR_COMP_STATE` (option) :
   chaque paire converge vers un bus + `DynamicsCompressorNode` dédié
   (seuil -16dB, ratio 2, attaque 80ms, release 500ms) avant le master —
   absorbe les micro-pics d'enveloppe (Δ élevé + respiration + FX cumulés)
   sans jamais s'entendre comme une compression.
10. **Rampe dédiée au changement de Δ pendant le tirage aléatoire** — les
    paires dont Δ change réellement à un tirage sont suivies
    (`_deltaChangedThisDraw`) et leur oscillateur Ida glisse vers la
    nouvelle valeur sur ~2,2s (`tuneOsc(id, freq, rampDur)`, désormais avec
    durée de rampe paramétrable) une fois le son revenu du fondu — sensation
    d'arrivée en douceur plutôt qu'un saut figé.
11. **"Caractère du battement" à la place d'une liste plate de Δ** —
    `DELTA_CHARACTER` : 3 familles nommées explicites (Apaisé <2 Hz /
    Équilibré 3–6 Hz / Immersif 6–10 Hz), sélectionnables via 3 boutons dans
    le panneau Options aléatoire, avec séparation stéréo naturellement
    croissante puisqu'elle suit Δ (point #1).
12. **"Ancre respiratoire" périodique** — `BREATH_ANCHOR_STATE` (option) :
    toutes les 4 minutes (réglable), les paires respirent un instant à la
    même phase avant de redériver naturellement (rythmes différents par
    paire) — bref moment d'unisson au sein d'une texture normalement
    décorrélée.
13. **Détune Sinus Duo par paire** — `PAIR_DUALSINE` : le réglage -72/+72 ¢
    (point #3) est aussi disponible individuellement par paire, dans l'onglet
    Oscillo de chaque paire (comme n/ratio/Δ le sont déjà), pour des timbres
    différents pair par pair au sein d'un même jeu.
14. **Sub-drone d'ancrage optionnel** — `SUBDRONE_STATE` : un pad grave fixe
    (racine ÷ 2), volume très bas réglable, jamais concerné par le tirage
    aléatoire — stabilise perceptuellement le mix quand plusieurs Δ élevés
    jouent ensemble. Suit la fréquence maître (`_retuneSubDrone()`).
15. **Historique des tirages avec retour arrière** — `_gameHistory` (pile de
    5 tirages max) : chaque "Nouveau jeu aléatoire" pousse un snapshot
    pré-tirage ; bouton "↩ Annuler le tirage" (`undoLastRandom()`) restaure
    fréquence maître, ratios/registres, Δ, polarités, volumes, timbres et
    filtres du tirage précédent.
16. **Export/partage d'un "jeu" complet étendu** — `exportState()` couvre
    désormais respiration (profondeur max, ancre), détune Sinus Duo (global
    + par paire), dérive du Δ, caractère du battement, mode casque strict,
    anti-pompage, sub-drone et toutes les options du jeu aléatoire — plus
    seulement fréquence/Δ/ratios.
17. **Persistance étendue** — tous les réglages ci-dessus (+ les options du
    jeu aléatoire elles-mêmes : verrou binaural, LFO filtre, timbre/
    profondeur/respiration/Δ variés) sont désormais sauvegardés et restaurés
    (`_doSave`/`loadState`/`_syncRandOptsUI`) ; auparavant plusieurs de ces
    réglages étaient silencieusement réinitialisés à chaque relance de l'app.
18. **Nettoyage des oscillateurs de fond (fix de fiabilité)** — les LFO de
    respiration par paire et de dérive du Δ sont des oscillateurs Web Audio
    indépendants qui ne sont jamais reconstruits automatiquement ; sans
    nettoyage explicite au "Dissoudre", ils continueraient à tourner en
    silence indéfiniment. Ajout du nettoyage complet dans `stopFlow()`
    (et des bus de compression par paire) — important pour les sessions
    longues avec plusieurs tirages aléatoires dans la même écoute.

## Build APK

Le fichier `OmcVibe/build/OmcVibe181-modifie-debug.apk` est un APK signé et
vérifié contenant ces 18 points, généré sans passer par le SDK Android
officiel (indisponible dans cet environnement, `dl.google.com` étant bloqué
par la politique réseau) :

- `AndroidManifest.xml`, `resources.arsc`, `classes*.dex` : **strictement
  identiques** à l'APK d'origine (aucune recompilation Java/Kotlin — seuls
  les assets web sont remplacés par ceux de `OmcVibe/www/`).
- Signature **APK Signature Scheme v2** (RSA 2048, SHA384withRSA) via la
  bibliothèque `com.android.tools.build:apksig` (Maven Central), avec un
  nouveau debug keystore (alias `androiddebugkey`, mot de passe `android` —
  les conventions standard du debug Android).
- Vérifié avec `ApkVerifier` (même bibliothèque) : `verified=true` pour
  API 24 à 34 (Android 7.0 et plus récent — l'immense majorité des appareils
  réels aujourd'hui).
- **Limite connue** : la signature JAR historique (v1) n'a pas pu être
  regénérée (la bibliothèque `apksig` 2.3.0 disponible sur Maven Central
  s'appuie sur des classes internes du JDK 8 qui ont changé de signature
  dans les JDK récents). Résultat : cet APK ne s'installera **pas** sur
  Android 5.1/6.0 (API 22-23, appareils 2014-2015). minSdkVersion déclaré
  reste 22 dans le manifeste (inchangé), mais dans les faits seul API 24+
  est couvert par la signature présente.
- Intégrité vérifiée : `unzip -t` propre, contenu de `index.html` dans
  l'APK identique octet pour octet à `OmcVibe/www/index.html`.

Pour une signature v1+v2 complète (compatible API 22+) ou pour repartir
d'une chaîne d'outils standard, régénérer via le pipeline Capacitor/Gradle
habituel : `npx cap sync android && cd android && ./gradlew assembleDebug`
(nécessite le SDK Android, non disponible dans cet environnement).

## Où regarder dans le code

- Correctif anti-tremolo : `binauralPanGap()`, `refreshBinauralPan()`,
  `buildOscPan()`, `STRICT_HEADPHONE_MODE` — section config, après `PAN_CENTERS`.
- Respiration par paire + ancre : `attachOscVolLFO()`, `PAIR_BREATH_OPTS`,
  `BREATH_ANCHOR_STATE`, `_breathAnchorPulse()` (§2.10/§2.11).
- Dérive du Δ : `DELTA_DRIFT_STATE`, `attachDeltaDriftToNode()` — branché
  dans `buildOsc()`.
- Décorrélation du drift analogique : `_connectDecorrelatedDrift()`.
- Détune Sinus Duo (global + par paire) : `DUALSINE_DETUNE`, `PAIR_DUALSINE`,
  `sine2Engine()`, `setDualSineDetune()`, `setPairDualSineDetune()`.
- Caractère du battement : `DELTA_CHARACTER`, `setDeltaCharacter()`.
- Compression par paire : `PAIR_COMP_STATE`, `_ensurePairComp()`.
- Sub-drone : `SUBDRONE_STATE`, `_startSubDrone()`.
- Historique/undo : `_gameHistory`, `undoLastRandom()`.
- Persistance : `_doSave()` / `loadState()` / `_syncRandOptsUI()` /
  `getFXState()` / `applyFXState()`.
- UI : panneau "OPTIONS ALÉATOIRE" (verrou, variétés, caractère, respiration,
  ancre) et bloc FX compact (Sinus Duo, dérive Δ, casque strict, anti-pompage,
  sub-drone, indicateur anti-tremolo) ; onglet Oscillo par paire (Sinus Duo
  par paire).
