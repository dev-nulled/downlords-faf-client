package com.faforever.client.update;


import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.CompletableFuture;

public interface ClientUpdateService extends InitializingBean, DisposableBean {

  /**
   * Returns information about an available newest update. Returns {@code null} if no update is available.
   */
  CompletableFuture<UpdateInfo> getNewestUpdate();

  void checkForUpdateInBackground();

  String getCurrentVersion();

  DownloadUpdateTask downloadAndInstallInBackground(UpdateInfo updateInfo);
}
