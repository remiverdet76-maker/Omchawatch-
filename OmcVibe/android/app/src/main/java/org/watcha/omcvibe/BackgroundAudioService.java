package org.watcha.omcvibe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

/**
 * Service de premier plan minimal : sa SEULE raison d'être est d'empêcher
 * Android (Doze / App Standby / gel du process en arrière-plan) de couper
 * le thread audio du flux binaural quand l'écran s'éteint ou que l'app
 * passe en arrière-plan — jusqu'à ce que l'utilisateur quitte vraiment
 * l'app (stopFlow() côté JS appelle stop(), cf. BackgroundAudioPlugin).
 *
 * Deux mécanismes complémentaires :
 *  - startForeground() : signale au système que le process est en usage
 *    actif (lecture média), le protège du gel/kill agressif.
 *  - PARTIAL_WAKE_LOCK : garde le CPU éveillé écran éteint (n'allume PAS
 *    l'écran, contrairement à un wake lock complet — juste le CPU, pour
 *    que le thread audio Web Audio continue de calculer à l'heure).
 *
 * L'audio lui-même continue de vivre dans le contexte JS/WebView (index.html)
 * — ce service ne fait AUCUN rendu audio, il garde juste le process actif
 * pour que ce rendu-là ne soit pas interrompu par le système.
 */
public class BackgroundAudioService extends Service {

  public static final String ACTION_STOP = "org.watcha.omcvibe.action.STOP_BG_AUDIO";
  private static final String CHANNEL_ID = "omcvibe_bg_audio";
  private static final int NOTIF_ID = 4321;

  private PowerManager.WakeLock wakeLock;

  @Override
  public void onCreate() {
    super.onCreate();
    createChannelIfNeeded();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && ACTION_STOP.equals(intent.getAction())) {
      stopSelfCleanly();
      return START_NOT_STICKY;
    }

    startAsForeground();
    acquireWakeLock();

    // START_STICKY : si le système tue quand même le process en cas de
    // pression mémoire extrême, il tente de le relancer — mais SANS
    // pouvoir relancer le flux audio de lui-même (l'état vit en JS), donc
    // pas de comportement "fantôme" à craindre : au pire, la notification
    // réapparaît vide un instant avant que stop() ne la referme au
    // prochain _disableBackgroundAudio() de l'app.
    return START_STICKY;
  }

  private void startAsForeground() {
    Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("OmcVibe · Flux binaural actif")
      .setContentText("La lecture continue en arrière-plan — tap pour revenir à l'app")
      .setSmallIcon(R.mipmap.ic_launcher)
      .setOngoing(true)
      .setSilent(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .setContentIntent(buildOpenAppIntent())
      .build();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      ServiceCompat.startForeground(
        this, NOTIF_ID, notif,
        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
      );
    } else {
      startForeground(NOTIF_ID, notif);
    }
  }

  private PendingIntent buildOpenAppIntent() {
    Intent openIntent = new Intent(this, MainActivity.class);
    openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
    return PendingIntent.getActivity(this, 0, openIntent, flags);
  }

  private void acquireWakeLock() {
    if (wakeLock != null && wakeLock.isHeld()) return;
    try {
      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      if (pm == null) return;
      wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OmcVibe:BackgroundAudio");
      wakeLock.setReferenceCounted(false);
      // Pas de timeout : tenu tant que le flux tourne, relâché explicitement
      // au stop() (jamais indéfiniment "oublié" — cf. _disableBackgroundAudio
      // appelé systématiquement par stopFlow() côté JS).
      wakeLock.acquire();
    } catch (Exception e) {
      // Silencieux : au pire l'app perd juste l'avantage du wake lock CPU,
      // le service de premier plan seul aide déjà beaucoup.
    }
  }

  private void releaseWakeLock() {
    try {
      if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
    } catch (Exception e) {
      // no-op
    }
    wakeLock = null;
  }

  private void stopSelfCleanly() {
    releaseWakeLock();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      stopForeground(Service.STOP_FOREGROUND_REMOVE);
    } else {
      stopForeground(true);
    }
    stopSelf();
  }

  private void createChannelIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    if (nm == null || nm.getNotificationChannel(CHANNEL_ID) != null) return;
    NotificationChannel channel = new NotificationChannel(
      CHANNEL_ID, "Lecture en arrière-plan", NotificationManager.IMPORTANCE_LOW
    );
    channel.setDescription("Garde le flux binaural OmcVibe actif écran éteint / app en arrière-plan.");
    channel.setShowBadge(false);
    nm.createNotificationChannel(channel);
  }

  @Override
  public void onDestroy() {
    releaseWakeLock();
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null; // service "started", jamais bindé
  }
}
