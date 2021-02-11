package com.faforever.client.reporting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Lazy
@Service
@RequiredArgsConstructor
public class ReportingService {

  public void reportError(Throwable e) {
    log.warn("Reporting has not yet been implemented");
  }
}
