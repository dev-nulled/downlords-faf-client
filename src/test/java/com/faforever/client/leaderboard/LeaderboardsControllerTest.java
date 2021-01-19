package com.faforever.client.leaderboard;

import com.faforever.client.i18n.I18n;
import com.faforever.client.notification.NotificationService;
import com.faforever.client.test.AbstractPlainJavaFxTest;
import com.faforever.client.theme.UiService;
import com.google.common.eventbus.EventBus;
import javafx.scene.control.Tab;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class LeaderboardsControllerTest extends AbstractPlainJavaFxTest {

  private LeaderboardsController instance;

  @Mock
  private EventBus eventBus;
  @Mock
  private LeaderboardService leaderboardService;
  @Mock
  private NotificationService notificationService;
  @Mock
  private UiService uiService;
  @Mock
  private I18n i18n;
  @Mock
  private LeaderboardController leaderboardController;

  private League league;

  @Before
  public void setUp() throws Exception {
    league = League.fromDto(new com.faforever.client.api.dto.League("1", OffsetDateTime.now(), OffsetDateTime.now(), "mock", "mock", "mock", "1"));

    when(leaderboardService.getLeagues()).thenReturn(CompletableFuture.completedFuture(List.of(league, league)));
    when(uiService.loadFxml("theme/leaderboard/leaderboard.fxml")).thenReturn(leaderboardController);
    when(i18n.get("leaderboard.mock")).thenReturn("mock");
    when(leaderboardController.getRoot()).thenReturn(new Tab());

    instance = new LeaderboardsController(eventBus, i18n, leaderboardService, notificationService, uiService);

    loadFxml("theme/leaderboard/leaderboards.fxml", clazz -> instance);
  }

  @Test
  public void testInitialize() {
    assertEquals(2, instance.leaderboardRoot.getTabs().size());
    assertEquals(0, instance.leaderboardRoot.getSelectionModel().getSelectedIndex());
  }


  @Test
  public void testGetRoot() throws Exception {
    assertEquals(instance.getRoot(), instance.leaderboardRoot);
    assertNull(instance.getRoot().getParent());
  }
}
