package com.faforever.client.teammatchmaking;

import com.faforever.client.chat.CountryFlagService;
import com.faforever.client.chat.avatar.AvatarService;
import com.faforever.client.fx.AbstractViewController;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.game.Faction;
import com.faforever.client.i18n.I18n;
import com.faforever.client.main.event.ShowLadderMapsEvent;
import com.faforever.client.player.Player;
import com.faforever.client.player.PlayerService;
import com.faforever.client.teammatchmaking.Party.PartyMember;
import com.faforever.client.theme.UiService;
import com.faforever.client.ui.dialog.Dialog;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static javafx.beans.binding.Bindings.createBooleanBinding;
import static javafx.beans.binding.Bindings.createObjectBinding;
import static javafx.beans.binding.Bindings.createStringBinding;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class TeamMatchmakingController extends AbstractViewController<Node> {

  private static final PseudoClass LEADER_PSEUDO_CLASS = PseudoClass.getPseudoClass("leader");

  private final CountryFlagService countryFlagService;
  private final AvatarService avatarService;
  private final PlayerService playerService;
  private final I18n i18n;
  private final UiService uiService;
  private final TeamMatchmakingService teamMatchmakingService;
  private final EventBus eventBus;
  @FXML
  public Button invitePlayerButton;

  @FXML
  public StackPane teamMatchmakingRoot;
  @FXML
  public Button leavePartyButton;
  @FXML
  public Label refreshingLabel;
  public ToggleButton uefButton;
  public ToggleButton cybranButton;
  public ToggleButton aeonButton;
  public ToggleButton seraphimButton;
  @FXML
  public ImageView avatarImageView;
  @FXML
  public ImageView countryImageView;
  @FXML
  public Label clanLabel;
  @FXML
  public Label usernameLabel;
  @FXML
  public Label gameCountLabel;
  public Label leagueLabel;
  public HBox queueBox;
  public GridPane partyMemberPane;
  public VBox preparationArea;
  public ImageView leagueImageView;
  public Label queueHeadingLabel;
  public ScrollPane scrollPane;
  public HBox playerCard;
  public Label crownLabel;
  private Player player;

  @Override
  public void initialize() {
    player = playerService.getCurrentPlayer().get();
    JavaFxUtil.fixScrollSpeed(scrollPane);
    initializeUppercaseText();

    countryImageView.imageProperty().bind(createObjectBinding(() -> countryFlagService.loadCountryFlag(
        StringUtils.isEmpty(player.getCountry()) ? "" : player.getCountry()).orElse(null), player.countryProperty()));
    avatarImageView.visibleProperty().bind(player.avatarUrlProperty().isNotNull().and(player.avatarUrlProperty().isNotEmpty()));
    avatarImageView.imageProperty().bind(createObjectBinding(() -> Strings.isNullOrEmpty(player.getAvatarUrl()) ? null : avatarService.loadAvatar(player.getAvatarUrl()), player.avatarUrlProperty()));
    leagueImageView.setImage(avatarService.loadAvatar("https://content.faforever.com/faf/avatars/ICE_Test.png"));
    clanLabel.managedProperty().bind(clanLabel.visibleProperty());
    clanLabel.visibleProperty().bind(player.clanProperty().isNotEmpty().and(player.clanProperty().isNotNull()));
    clanLabel.textProperty().bind(createStringBinding(() ->
        Strings.isNullOrEmpty(player.getClan()) ? "" : String.format("[%s]", player.getClan()), player.clanProperty()));
    usernameLabel.textProperty().bind(player.usernameProperty());
    crownLabel.visibleProperty().bind(createBooleanBinding(() ->
        teamMatchmakingService.getParty().getMembers().size() > 1 && teamMatchmakingService.getParty().getOwner().equals(player),
        teamMatchmakingService.getParty().ownerProperty(), teamMatchmakingService.getParty().getMembers()));

    teamMatchmakingService.getParty().getMembers().addListener((Observable o) -> {
      playerCard.pseudoClassStateChanged(LEADER_PSEUDO_CLASS,
          (teamMatchmakingService.getParty().getOwner().equals(player) && teamMatchmakingService.getParty().getMembers().size() > 1));
      List<PartyMember> members = new ArrayList<>(teamMatchmakingService.getParty().getMembers());
      partyMemberPane.getChildren().clear();
      members.removeIf(partyMember -> partyMember.getPlayer().equals(player));
      for(int i = 0; i < members.size(); i++) {
        PartyMemberItemController controller = uiService.loadFxml("theme/play/teammatchmaking/matchmaking_member_card.fxml");
        controller.setMember(members.get(i));
        if (members.size() == 1)
          partyMemberPane.add(controller.getRoot(), 0, 0, 2, 1);
        else
          partyMemberPane.add(controller.getRoot(), i % 2, i / 2);
      }
    });

    teamMatchmakingService.getMatchmakingQueues().addListener((Observable o) -> {
      List<MatchmakingQueue> queues = teamMatchmakingService.getMatchmakingQueues();
      queueBox.getChildren().clear();
      queues.iterator().forEachRemaining(queue -> {
        MatchmakingQueueItemController controller = uiService.loadFxml("theme/play/teammatchmaking/matchmaking_queue_card.fxml");
        controller.setQueue(queue);
        queueBox.getChildren().add(controller.getRoot());
      });
    });

    invitePlayerButton.disableProperty().bind(createBooleanBinding(
        () -> teamMatchmakingService.getParty().getOwner().getId() != playerService.getCurrentPlayer().map(Player::getId).orElse(-1),
        teamMatchmakingService.getParty().ownerProperty(),
        playerService.currentPlayerProperty()
    ));
    leavePartyButton.disableProperty().bind(createBooleanBinding(() -> teamMatchmakingService.getParty().getMembers().size() <= 1, teamMatchmakingService.getParty().getMembers()));

    teamMatchmakingService.getParty().getMembers().addListener((InvalidationListener) c -> {
      refreshingLabel.setVisible(false);
      selectFactionsBasedOnParty();
    });
  }

  private void initializeUppercaseText() {
    // TODO: it would be nice if we could start from the root node, however SplitPane uses items instead of children,
    // so lookupAll ignores the contents of the SplitPane
    for (Node node : preparationArea.lookupAll(".uppercase")) {
      if (node instanceof Label) {
          Label label = (Label) node;
          label.setText(label.getText().toUpperCase());
      }
      if (node instanceof Button) {
        Button button = (Button) node;
        button.setText(button.getText().toUpperCase());
      }
    }

    leagueLabel.textProperty().bind(createStringBinding(() -> {
      String text = i18n.get("leaderboard.divisionName");
      if (leagueLabel.getStyleClass().contains("uppercase"))
        text = text.toUpperCase();
      return text;
    }, player.globalRatingMeanProperty())); // This should actually be a divisionProperty once that is available

    gameCountLabel.textProperty().bind(createStringBinding(() -> {
      String text = i18n.get("teammatchmaking.gameCount", player.getNumberOfGames());
      if (gameCountLabel.getStyleClass().contains("uppercase"))
        text = text.toUpperCase();
      return text;
    }, player.numberOfGamesProperty()));

    queueHeadingLabel.textProperty().bind(createStringBinding(() -> {
      String text = i18n.get("teammatchmaking.queueTitle");
      if (!teamMatchmakingService.getParty().getOwner().equals(player))
        text = i18n.get("teammatchmaking.queueTitle.inParty");
      if (queueHeadingLabel.getStyleClass().contains("uppercase"))
        text = text.toUpperCase();
      return text;
    }, teamMatchmakingService.getParty().ownerProperty()));
  }

  @Override
  public Node getRoot() {
    return teamMatchmakingRoot;
  }

  // TODO: use
  public void showMatchmakingMaps(ActionEvent actionEvent) {
    eventBus.post(new ShowLadderMapsEvent());//TODO show team matchmaking maps and not ladder maps
  }

  public void onInvitePlayerButtonClicked(ActionEvent actionEvent) {
    InvitePlayerController invitePlayerController = uiService.loadFxml("theme/play/teammatchmaking/matchmaking_invite_player.fxml");
    Pane root = invitePlayerController.getRoot();
    Dialog dialog = uiService.showInDialog(teamMatchmakingRoot, root, i18n.get("teammatchmaking.invitePlayer"));
  }

  public void onEnterQueueButtonClicked(ActionEvent actionEvent) {
    //TODO
  }

  public void onLeavePartyButtonClicked(ActionEvent actionEvent) {
    teamMatchmakingService.leaveParty();
  }

  public void onLeaveQueueButtonClicked(ActionEvent actionEvent) {
    //TODO
  }

  public boolean isSelfReady() {
    return teamMatchmakingService.getParty().getMembers().stream()
        .anyMatch(p -> p.getPlayer().getId() == playerService.getCurrentPlayer().map(Player::getId).orElse(-1)
            && p.isReady());
  }

  public void onFactionButtonClicked(ActionEvent actionEvent) {

    if (!uefButton.isSelected() && !aeonButton.isSelected() && !cybranButton.isSelected() && !seraphimButton.isSelected()) {
      selectFactionsBasedOnParty();
      return;
    }

    List<Faction> factions = new ArrayList<>();
    if (uefButton.isSelected()) {
      factions.add(Faction.UEF);
    }
    if (aeonButton.isSelected()) {
      factions.add(Faction.AEON);
    }
    if (cybranButton.isSelected()) {
      factions.add(Faction.CYBRAN);
    }
    if (seraphimButton.isSelected()) {
      factions.add(Faction.SERAPHIM);
    }

    teamMatchmakingService.setPartyFactions(factions);

    refreshingLabel.setVisible(true);
  }

  private void selectFactionsBasedOnParty() {
    uefButton.setSelected(isFactionSelectedInParty(Faction.UEF));
    aeonButton.setSelected(isFactionSelectedInParty(Faction.AEON));
    cybranButton.setSelected(isFactionSelectedInParty(Faction.CYBRAN));
    seraphimButton.setSelected(isFactionSelectedInParty(Faction.SERAPHIM));
  }

  private boolean isFactionSelectedInParty(Faction faction) {
    return teamMatchmakingService.getParty().getMembers().stream()
        .anyMatch(m -> m.getPlayer().getId() == player.getId() && m.getFactions().contains(faction));
  }
}
