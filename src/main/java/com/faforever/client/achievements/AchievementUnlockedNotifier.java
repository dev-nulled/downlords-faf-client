package com.faforever.client.achievements;

import com.faforever.client.achievements.AchievementService.AchievementState;
import com.faforever.client.api.dto.AchievementDefinition;
import com.faforever.client.audio.AudioService;
import com.faforever.client.i18n.I18n;
import com.faforever.client.notification.events.TransientNotificationEvent;
import com.faforever.client.remote.FafService;
import com.faforever.client.remote.UpdatedAchievement;
import com.faforever.client.remote.UpdatedAchievementsMessage;
import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Lazy
@Component
@RequiredArgsConstructor
public class AchievementUnlockedNotifier implements InitializingBean {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final EventBus eventBus;
  private final I18n i18n;
  private final AchievementService achievementService;
  private final FafService fafService;
  private final AudioService audioService;

  private long lastSoundPlayed;

  @Override
  public void afterPropertiesSet() {
    fafService.addOnMessageListener(UpdatedAchievementsMessage.class, this::onUpdatedAchievementsMessage);
  }

  private void onUpdatedAchievementsMessage(UpdatedAchievementsMessage message) {
    message.getUpdatedAchievements().stream()
        .filter(UpdatedAchievement::getNewlyUnlocked)
        .forEachOrdered(updatedAchievement -> achievementService.getAchievementDefinition(updatedAchievement.getAchievementId())
            .thenAccept(this::notifyAboutUnlockedAchievement)
            .exceptionally(throwable -> {
              logger.warn("Could not valueOf achievement definition for achievement: {}", updatedAchievement.getAchievementId(), throwable);
              return null;
            })
        );
  }

  private void notifyAboutUnlockedAchievement(AchievementDefinition achievementDefinition) {
    if (lastSoundPlayed < System.currentTimeMillis() - 1000) {
      audioService.playAchievementUnlockedSound();
      lastSoundPlayed = System.currentTimeMillis();
    }
    eventBus.post(new TransientNotificationEvent(
            i18n.get("achievement.unlockedTitle"),
            achievementDefinition.getName(),
            achievementService.getImage(achievementDefinition, AchievementState.UNLOCKED)
        )
    );
  }
}
