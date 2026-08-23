package org.watcha.morsecosmos;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;

// L'app web (index.html) accède à la caméra/flash via getUserMedia() +
// track.applyConstraints({torch}) — des API web standard, pas un plugin
// Capacitor natif. Le WebView de Capacitor n'accorde une requête
// getUserMedia (BridgeWebChromeClient.onPermissionRequest) que si la
// permission Android CAMERA est DÉJÀ accordée côté OS — sans ce
// onCreate, le tout premier appel à getUserMedia serait silencieusement
// refusé tant que l'utilisateur n'a jamais vu de popup de permission.
// On la demande donc dès le lancement, une seule fois (dialogue standard
// Android), avant même que l'utilisateur touche à l'écran caméra.
public class MainActivity extends BridgeActivity {
  private static final int CAMERA_PERMISSION_REQUEST = 1001;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
    }
  }
}
