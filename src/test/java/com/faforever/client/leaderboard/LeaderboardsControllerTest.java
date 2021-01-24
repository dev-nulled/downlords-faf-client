package com.faforever.client.leaderboard;

import com.faforever.client.i18n.I18n;
import com.faforever.client.notification.NotificationService;
import com.faforever.client.task.TaskService;
import com.faforever.client.test.AbstractPlainJavaFxTest;
import com.faforever.client.theme.UiService;
import com.google.common.eventbus.EventBus;
import javafx.scene.control.Tab;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class LeaderboardsControllerTest extends AbstractPlainJavaFxTest {

  private LeaderboardsController instance;

  @Mock
  private EventBus eventBus;
  @Mock
  private NotificationService notificationService;
  @Mock
  private UiService uiService;
  @Mock
  private I18n i18n;
  @Mock
  private LeaderboardController leaderboardController;
  @Mock
  private TaskService taskService;

  private final MockLeaderboardService leaderboardService = new MockLeaderboardService(taskService);

  @Before
  public void setUp() throws Exception {
    when(uiService.loadFxml("theme/leaderboard/leaderboard.fxml")).thenReturn(leaderboardController);
    when(leaderboardController.getRoot()).thenReturn(new Tab());

    instance = new LeaderboardsController(eventBus, i18n, leaderboardService, notificationService, uiService);

    loadFxml("theme/leaderboard/leaderboards.fxml", clazz -> instance);
  }

  @Test
  public void testInitialize() {
    assertEquals(3, instance.leaderboardRoot.getTabs().size());
    assertEquals(0, instance.leaderboardRoot.getSelectionModel().getSelectedIndex());
  }


  @Test
  public void testGetRoot() throws Exception {
    assertEquals(instance.getRoot(), instance.leaderboardRoot);
    assertNull(instance.getRoot().getParent());
  }
}
