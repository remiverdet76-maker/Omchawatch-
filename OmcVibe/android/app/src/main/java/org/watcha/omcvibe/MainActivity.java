package org.watcha.omcvibe;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    // Enregistre le plugin AVANT super.onCreate() (convention Capacitor) —
    // pont natif pour _enableBackgroundAudio/_disableBackgroundAudio
    // (cf. www/index.html, appelé au démarrage/arrêt du flux binaural).
    registerPlugin(BackgroundAudioPlugin.class);
    super.onCreate(savedInstanceState);
  }
}
