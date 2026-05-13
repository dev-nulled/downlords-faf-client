package com.faforever.client.patch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("local")
public class NoOpGameUpdater implements GameUpdater {

  @Override
  public CompletableFuture<Void> update(String featuredModName, @Nullable Map<String, Integer> featuredModFileVersions,
                                        @Nullable Integer baseVersion, boolean forReplays) {
    return CompletableFuture.completedFuture(null);
  }
}
