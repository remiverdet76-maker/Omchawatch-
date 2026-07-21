# OmcVibe 181 — 18 points d'amélioration du moteur binaural

Analyse de l'APK `OmcVibe181-debug-apk (3).zip` (app Capacitor, `org.watcha.omcvibe`,
WebView sur `assets/public/index.html`). Le code source web extrait vit maintenant
dans ce dossier (`OmcVibe/www/index.html`) — c'est la vraie source du comportement
de l'app, l'APK n'étant qu'un artefact de build autour de ce WebView.

**Important — portée de cette passe :** cette session a modifié le **code source
web** (le moteur audio réel). Recompiler un `.apk` signé nécessite le projet
Android/Capacitor complet (Gradle, keystore) qui n'est pas présent dans ce dépôt
(seul le zip de l'APK compilé l'était) — cette recompilation n'a donc pas été
faite ici. Le fichier `OmcVibe/www/index.html` est prêt à être réinjecté dans le
projet Capacitor existant (`npx cap sync && ./gradlew assembleDebug`) pour
regénérer l'APK.

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

## Les 18 points

### ✅ Implémentés dans cette passe

1. **Correctif racine anti-tremolo (Δ > 2 Hz)** — l'écart stéréo Pingala/Ida
   grandit maintenant avec Δ (`binauralPanGap()`, `BINAURAL_GAP_PER_HZ = 0.055`,
   plafond `BINAURAL_GAP_MAX = 0.86`), recalculé et rejoué en douceur
   (`refreshBinauralPan()`) à **chaque** changement de Δ — manuel (`setDelta`,
   `setGlobalDelta`) ou tirage aléatoire — pas seulement au moment du random
   comme avant. Le centre de paire (`spread`) n'est pas touché : seule la
   distance Pingala↔Ida à l'intérieur de la paire s'élargit.
2. **LFO respiration par paire, aléatoire à chaque nouveau jeu** — réutilise et
   étend le patch existant `attachOscVolLFO`/`toggleOscVolLFO` (§2.10) :
   `triggerMagicAuto()` tire maintenant, pour chaque paire non verrouillée, un
   rythme (≈4–17 respirations/min), une profondeur et un **déphasage aléatoires
   indépendants** — les paires respirent hors phase, texture organique plutôt
   qu'un pompage synchrone. Option "Respiration par paire (aléatoire)" +
   slider "Profondeur" (plafond réglable, `PAIR_BREATH_OPTS.depthMax`) dans le
   panneau Options aléatoire.
3. **Détune Sinus Duo paramétrable -72 / +72 ¢** — le moteur `sine2` (deux voix
   sinus) était figé à ±7 ¢ dans `OSC_ENGINES`. Il est maintenant piloté par
   `DUALSINE_DETUNE` (slider "Sinus Duo" dans le panneau FX, -72..+72 ¢),
   appliqué à la construction (`sine2Engine()`) et mis à jour **en direct** sur
   les oscillateurs déjà en cours via `setDualSineDetune()` (pas de reconstruction).
4. **Indicateur "Séparation anti-tremolo"** — pourcentage affiché en direct à
   côté du réglage Binaural (`sv-binaural-sep`), qui vire à l'ambre au-delà de
   2 Hz : rend visible/compréhensible la correction du point #1.
5. **Persistance étendue** — les options du jeu aléatoire (verrou binaural, LFO
   filtre, timbre/profondeur/respiration auto), le détune Sinus Duo et la
   profondeur de respiration max sont maintenant sauvegardés/restaurés
   (`_doSave`/`loadState`/`_syncRandOptsUI`) ; auparavant ces réglages étaient
   silencieusement réinitialisés à chaque relance de l'app.

### 💡 Proposés (feuille de route, non codés cette passe)

6. **Δ indépendant par paire en mode random déverrouillé** — aujourd'hui,
   quand "Verrou binaural" est décoché, `baseDelta` est **un seul** tirage
   commun appliqué à toutes les paires. Tirer un Δ propre par paire (dans un
   ensemble musicalement cohérent, ex. dérivé d'un même sous-multiple) donnerait
   un jeu binaural bien plus riche et moins uniforme.
7. **Dérive lente du Δ ("respiration du battement")** — un LFO très lent (cycle
   de plusieurs minutes) ferait légèrement onduler chaque Δ (±0,3 Hz) sans
   jamais changer de bande cérébrale : évite l'habituation, rend la session
   réellement évolutive dans la durée plutôt que figée sur un seul tirage.
8. **Décorrélation du drift analogique partagé** — un seul `_driftLFO` (0,07 Hz)
   alimente aujourd'hui TOUS les oscillateurs en phase (`_driftDepth` partagé) :
   ça bouge tout le mix ensemble, ça ne "décorrèle" rien entre voix. Donner à
   chaque paire son propre drift (rythme + phase indépendants) créerait un
   mouvement organique réellement indépendant.
9. **Polarité variée même Δ verrouillé** — permettre à chaque paire de garder un
   signe (+/−) de battement différent tout en gardant le Δ verrouillé identique,
   pour une image plus variée sans toucher au confort binaural garanti par le
   verrou.
10. **Mode "casque strict" (routage 100% isolé)** — `StereoPannerNode` à
    puissance constante laisse toujours fuir un peu de signal d'un canal vers
    l'autre. Un mode optionnel routant Pingala/Ida sur des canaux vraiment
    isolés (`ChannelSplitterNode`/`ChannelMergerNode`) éliminerait tout résidu
    de crosstalk, même à Δ élevé, pour une écoute au casque irréprochable.
11. **Compression douce anti-pompage par paire** — un limiteur très lent
    (quelques dizaines de ms) sur le bus de chaque paire absorberait les
    micro-pics d'enveloppe créés par un Δ élevé combiné à la respiration + FX,
    sans jamais "écraser" le son.
12. **Rampe dédiée au changement de Δ pendant le tirage aléatoire** — le retune
    de fréquence est déjà lissé, mais Δ lui-même saute d'une valeur à l'autre ;
    une glissade de quelques secondes rendrait les tirages plus fluides.
13. **"Caractère du battement" à la place d'une liste plate de Δ** — regrouper
    les deltas tirés par familles (Apaisé <2 Hz / Équilibré 2–4 Hz / Immersif
    4–8 Hz, avec séparation stéréo croissante en conséquence) et exposer ça
    comme un réglage explicite plutôt qu'un tableau `DELTAS` interne opaque.
14. **"Ancre respiratoire" périodique** — resynchroniser volontairement la
    respiration de toutes les paires sur une même phase de temps en temps (ex.
    toutes les 3–5 min) pour un bref moment d'unisson au sein d'une texture
    normalement décorrélée : alterne "vivant/désynchronisé" et "souffle commun".
15. **Détune Sinus Duo par paire** — exposer le réglage -72/+72 ¢ (point #3)
    aussi individuellement par paire, comme le sont déjà Δ/n/ratio, pour des
    timbres différents pair par pair au sein d'un même jeu.
16. **Sub-drone d'ancrage optionnel** — un pad grave fixe à très bas volume, non
    concerné par le tirage aléatoire, pour stabiliser perceptuellement le mix
    quand plusieurs Δ élevés jouent ensemble.
17. **Historique des tirages avec retour arrière** — puisque respiration/detune/Δ
    deviennent partiellement aléatoires à chaque "Nouveau jeu", pouvoir revenir
    en un tap au tirage précédent devient précieux (undo léger, pile de 3–5
    tirages en mémoire).
18. **Export/partage d'un "jeu" complet** — `exportState()` existe déjà mais ne
    couvre pas les nouveaux paramètres (respiration, détune Sinus Duo, options
    random). L'étendre permettrait de rejouer exactement la même session plus
    tard ou de la partager en une chaîne compacte.

## Où regarder dans le code

- Correctif anti-tremolo : `binauralPanGap()`, `refreshBinauralPan()`,
  `buildOscPan()` — section config, juste après `PAN_CENTERS`.
- Respiration par paire : `attachOscVolLFO()`, `PAIR_BREATH_OPTS`,
  `setPairBreathDepthMax()` (§2.10), branchement dans `triggerMagicAuto()`.
- Détune Sinus Duo : `DUALSINE_DETUNE`, `sine2Engine()`, `setDualSineDetune()`,
  branchement dans `buildOsc()`.
- Persistance : `_doSave()` / `loadState()` / `_syncRandOptsUI()`.
- UI : panneau "OPTIONS ALÉATOIRE" (case + slider respiration) et bloc FX
  compact (slider "Sinus Duo", indicateur "Séparation anti-tremolo").
