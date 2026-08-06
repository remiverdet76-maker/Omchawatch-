package org.watcha.omcvibe;

import android.content.Intent;
import android.os.Build;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PermissionState;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

/**
 * Pont JS ↔ BackgroundAudioService — appelé depuis www/index.html
 * (_enableBackgroundAudio/_disableBackgroundAudio, cf. startFlow/stopFlow)
 * pour que le flux binaural continue écran éteint / app en arrière-plan,
 * jusqu'à ce que l'utilisateur quitte vraiment l'app.
 */
@CapacitorPlugin(
  name = "BackgroundAudio",
  permissions = {
    @Permission(strings = { android.Manifest.permission.POST_NOTIFICATIONS }, alias = "notifications")
  }
)
public class BackgroundAudioPlugin extends Plugin {

  @PluginMethod
  public void start(PluginCall call) {
    // La permission de notification (Android 13+) ne conditionne QUE
    // l'affichage de la notification — le service de premier plan démarre
    // dans tous les cas (cf. onNotifPermResult), donc pas de blocage
    // possible du flux audio même si l'utilisateur refuse.
    if (Build.VERSION.SDK_INT >= 33 && getPermissionState("notifications") != PermissionState.GRANTED) {
      requestPermissionForAlias("notifications", call, "onNotifPermResult");
      return;
    }
    startService();
    call.resolve();
  }

  @PermissionCallback
  private void onNotifPermResult(PluginCall call) {
    startService();
    call.resolve();
  }

  @PluginMethod
  public void stop(PluginCall call) {
    try {
      Intent stopIntent = new Intent(getContext(), BackgroundAudioService.class);
      stopIntent.setAction(BackgroundAudioService.ACTION_STOP);
      getContext().startService(stopIntent);
    } catch (Exception e) {
      // service déjà arrêté / process en cours de destruction — sans effet
    }
    call.resolve();
  }

  private void startService() {
    Intent intent = new Intent(getContext(), BackgroundAudioService.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      getContext().startForegroundService(intent);
    } else {
      getContext().startService(intent);
    }
  }
}
