package com.faforever.client.leaderboard;

import com.faforever.client.chat.avatar.AvatarService;
import com.faforever.client.fx.Controller;
import com.faforever.client.fx.JavaFxUtil;
import com.faforever.client.i18n.I18n;
import com.faforever.client.player.Player;
import com.faforever.client.player.PlayerService;
import com.faforever.client.theme.UiService;
import com.faforever.client.util.Assert;
import com.faforever.client.util.Validator;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class LeaderboardController implements Controller<Tab> {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final PseudoClass NOTIFICATION_HIGHLIGHTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("highlighted-bar");

  private final LeaderboardService leaderboardService;
  private final I18n i18n;
  private final PlayerService playerService;
  private final UiService uiService;
  private final AvatarService avatarService;
  public Tab leaderboardRoot;
  public TextField searchTextField;
  public Pane connectionProgressPane;
  public Pane contentPane;
  public BarChart<String, Integer> ratingDistributionChart;
  public Label playerDivisionNameLabel;
  public Label playerScoreLabel;
  public Label scoreLabel;
  public Label seasonLabel;
  public ComboBox<Division> majorDivisionPicker;
  public Arc scoreArc;
  public TabPane subDivisionTabPane;
  public ImageView playerDivisionImageView;
  public CategoryAxis rankNumber;
  private String leagueTechnicalName;
  private InvalidationListener playerLeagueScoreListener;

  @Override
  public void initialize() {
    seasonLabel.setText(i18n.get("leaderboard.season").toUpperCase());
    scoreLabel.setText(i18n.get("leaderboard.score").toUpperCase());
    searchTextField.setPromptText(i18n.get("leaderboard.searchPrompt").toUpperCase());

    contentPane.managedProperty().bind(contentPane.visibleProperty());
    connectionProgressPane.managedProperty().bind(connectionProgressPane.visibleProperty());
    connectionProgressPane.visibleProperty().bind(contentPane.visibleProperty().not());

    majorDivisionPicker.setConverter(divisionStringConverter());

    JavaFxUtil.addListener(playerService.currentPlayerProperty(), (observable, oldValue, newValue) -> Platform.runLater(() -> setCurrentPlayer(newValue)));

    searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
      TableView<LeagueEntry> ratingTable = (TableView<LeagueEntry>) subDivisionTabPane.getSelectionModel().getSelectedItem().getContent();
      if (Validator.isInt(newValue)) {
        ratingTable.scrollTo(Integer.parseInt(newValue) - 1);
      } else {
        LeagueEntry foundPlayer = null;
        for (LeagueEntry leagueEntry : ratingTable.getItems()) {
          if (leagueEntry.getUsername().toLowerCase().startsWith(newValue.toLowerCase())) {
            foundPlayer = leagueEntry;
            break;
          }
        }
        if (foundPlayer == null) {
          for (LeagueEntry leagueEntry : ratingTable.getItems()) {
            if (leagueEntry.getUsername().toLowerCase().contains(newValue.toLowerCase())) {
              foundPlayer = leagueEntry;
              break;
            }
          }
        }
        if (foundPlayer != null) {
          ratingTable.scrollTo(foundPlayer);
          ratingTable.getSelectionModel().select(foundPlayer);
        } else {
          ratingTable.getSelectionModel().select(null);
          searchInAllDivisions(newValue);
        }
      }
    });
  }

  private void searchInAllDivisions(String searchText) {
    playerService.getPlayerForUsername(searchText).ifPresent(player -> {
      leaderboardService.getLeagueEntryForPlayer(player.getId(), leagueTechnicalName).thenAccept(leagueEntry -> {
        majorDivisionPicker.getItems().stream()
            .filter(item -> item.getMajorDivisionIndex() == leagueEntry.getMajorDivisionIndex())
            .findFirst().ifPresent(item -> majorDivisionPicker.getSelectionModel().select(item));
        subDivisionTabPane.getTabs().stream()
            .filter(tab -> tab.getUserData().equals(leagueEntry.getSubDivisionIndex()))
            .findFirst().ifPresent(tab -> {
          subDivisionTabPane.getSelectionModel().select(tab);
          TableView<LeagueEntry> newTable = (TableView<LeagueEntry>) tab.getContent();
          newTable.scrollTo(leagueEntry);
          newTable.getSelectionModel().select(leagueEntry);
        });
      });
    });
  }

  protected void display() {
    Assert.checkNullIllegalState(leagueTechnicalName, "leagueName must not be null");

    contentPane.setVisible(false);
    leaderboardService.getDivisions(leagueTechnicalName).thenAccept(divisions -> Platform.runLater(() -> {
      majorDivisionPicker.getItems().clear();

      majorDivisionPicker.getItems().addAll(
          divisions.stream().filter(division -> division.getSubDivisionIndex() == 1).collect(Collectors.toList()));
      contentPane.setVisible(true);
    })).exceptionally(throwable -> {
      contentPane.setVisible(false);
      logger.warn("Could not read divisions", throwable);
      return null;
    });
    playerService.getCurrentPlayer().ifPresent(this::setCurrentPlayer);
  }

  public Tab getRoot() {
    return leaderboardRoot;
  }

  public void setLeagueTechnicalName(String leagueTechnicalName) {
    this.leagueTechnicalName = leagueTechnicalName;
  }

  private void setCurrentPlayer(Player player) {
    playerLeagueScoreListener = leagueObservable -> Platform.runLater(() -> updateStats(player));

    JavaFxUtil.addListener(player.subDivisionIndexProperty(), new WeakInvalidationListener(playerLeagueScoreListener));
    JavaFxUtil.addListener(player.scoreProperty(), new WeakInvalidationListener(playerLeagueScoreListener));
    updateStats(player);
  }

  private void updateStats(Player player) {
    leaderboardService.getLeagueEntryForPlayer(player.getId(), leagueTechnicalName).thenAccept(leagueEntry -> Platform.runLater(() -> {
      leaderboardService.getDivisions(leagueTechnicalName).thenAccept(divisions -> {
        divisions.forEach(division -> {
          if (division.getMajorDivisionIndex() == leagueEntry.getMajorDivisionIndex()
              && division.getSubDivisionIndex() == leagueEntry.getSubDivisionIndex()) {
            playerDivisionImageView.setImage(avatarService.loadAvatar(
                String.format("https://content.faforever.com/divisions/icons/%s-%s.png",
                    division.getMajorDivisionName().getImageKey(),
                    division.getSubDivisionName().getImageKey())));
            playerDivisionNameLabel.setText(i18n.get("leaderboard.divisionName",
                i18n.get(division.getMajorDivisionName().getI18nKey()),
                i18n.get(division.getSubDivisionName().getI18nKey())).toUpperCase());
            scoreArc.setLength(-360.0 * leagueEntry.getScore() / division.getHighestScore());
            playerScoreLabel.setText(i18n.number(leagueEntry.getScore()));
            majorDivisionPicker.getItems().stream()
                .filter(item -> item.getMajorDivisionIndex() == division.getMajorDivisionIndex())
                .findFirst().ifPresent(item -> majorDivisionPicker.getSelectionModel().select(item));
            onMajorDivisionChanged();
            subDivisionTabPane.getTabs().stream()
                .filter(tab -> tab.getUserData().equals(division.getSubDivisionIndex()))
                .findFirst().ifPresent(tab -> {
                  subDivisionTabPane.getSelectionModel().select(tab);
                  // Need to test this once the api is up
                  TableView<LeagueEntry> newTable = (TableView<LeagueEntry>) tab.getContent();
                  newTable.scrollTo(leagueEntry);
                  newTable.getSelectionModel().select(leagueEntry);
            });
            plotDivisionDistributions(divisions, leagueEntry);
          }
        });
      }).exceptionally(throwable -> {
        logger.warn("Could not get list of divisions", throwable);
        return null;
      });
    })).exceptionally(throwable -> {
      // Debug instead of warn, since it's fairly common that players don't have a leaderboard entry which causes a 404
      logger.debug("Leaderboard entry could not be read for current player: " + player.getUsername(), throwable);
      playerDivisionNameLabel.setText(i18n.get("leaderboard.placement",
          "X", // Would rather do this:
          // leagueEntry.getGamesPlayed(),
          10));
      majorDivisionPicker.getItems().stream()
          .findFirst().ifPresent(item -> majorDivisionPicker.getSelectionModel().select(item));
      subDivisionTabPane.getTabs().stream()
          .findFirst().ifPresent(tab -> subDivisionTabPane.getSelectionModel().select(tab));
      return null;
    });
  }

  private void plotDivisionDistributions(List<Division> divisions, LeagueEntry leagueEntry) {
    divisions.stream().filter(division -> division.getMajorDivisionIndex() == 1).forEach(firstTierSubDivision -> {
      XYChart.Series<String, Integer> series = new XYChart.Series<>();
      series.setName(i18n.get(firstTierSubDivision.getSubDivisionName().getI18nKey()));
      series.getData().addAll(
          divisions.stream().filter(division -> division.getSubDivisionIndex() == firstTierSubDivision.getSubDivisionIndex()).map(division -> {
            XYChart.Data<String, Integer> data = new XYChart.Data<>(i18n.get(division.getMajorDivisionName().getI18nKey()),(int) (Math.random() * 100 + 200 - Math.pow(division.getMajorDivisionIndex() - 3.3, 2) * 30));
            Text label = new Text();
            label.setText(i18n.get(division.getSubDivisionName().getI18nKey()));
            label.setFill(Color.WHITE);
            if (division.getMajorDivisionIndex() == leagueEntry.getMajorDivisionIndex()
                && division.getSubDivisionIndex() == leagueEntry.getSubDivisionIndex()) {
              data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                newValue.pseudoClassStateChanged(NOTIFICATION_HIGHLIGHTED_PSEUDO_CLASS, true);
                addNodeOnTopOfBar(data, label);
              });
            } else {
              data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                addNodeOnTopOfBar(data, label);
              });
            }
            return data;
          }).collect(Collectors.toList()));
      Platform.runLater(() -> ratingDistributionChart.getData().add(series));
    });
    leaderboardService.getDivisionStats(leagueTechnicalName)
        .thenAccept(divisionStats -> {
          int totalPlayers = 0;
          for (DivisionStat entry : divisionStats) {
            totalPlayers += entry.getTotalCount();
          }
          // TODO: fix access to rank
          //rankNumber.labelProperty().setValue(i18n.get("leaderboard.rank", leagueEntry.getRank(), totalPlayers));
        })
        .exceptionally(throwable -> {
          logger.warn("Could not get player rank", throwable);
          return null;
        });
  }

  private void addNodeOnTopOfBar(XYChart.Data<String, Integer> data, Node nodeToAdd) {
    final Node node = data.getNode();
    node.parentProperty().addListener((ov, oldParent, parent) -> {
      if (parent == null) {
        return;
      }
      Group parentGroup = (Group) parent;
      ObservableList<Node> children = parentGroup.getChildren();
      if (!children.contains(nodeToAdd)) {
        children.add(nodeToAdd);
        nodeToAdd.setViewOrder(-0.5);
      }
    });

    JavaFxUtil.addListener(node.boundsInParentProperty(), (ov, oldBounds, bounds) -> {
      nodeToAdd.setLayoutX(Math.round(bounds.getMinX() + bounds.getWidth() / 2 - nodeToAdd.prefWidth(-1) / 2));
      nodeToAdd.setLayoutY(Math.round(bounds.getMaxY() - nodeToAdd.prefHeight(-1) * 0.5));
    });
  }

  @NotNull
  private StringConverter<Division> divisionStringConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(Division division) {
        return i18n.get(division.getMajorDivisionName().getI18nKey()).toUpperCase();
      }

      @Override
      public Division fromString(String string) {
        return null;
      }
    };
  }

  public void onMajorDivisionChanged() {
    leaderboardService.getDivisions(leagueTechnicalName).thenAccept(divisions -> {
      subDivisionTabPane.getTabs().clear();
      divisions.stream()
          .filter(division -> division.getMajorDivisionIndex() == majorDivisionPicker.getValue().getMajorDivisionIndex())
          .forEach(division -> {
            SubDivisionTabController controller = uiService.loadFxml("theme/leaderboard/subDivisionTab.fxml");
            controller.getRoot().setUserData(division.getSubDivisionIndex());
            controller.populate(division);
            subDivisionTabPane.getTabs().add(controller.getRoot());
            subDivisionTabPane.getSelectionModel().selectLast();
          });
    });
    // The tabs always appear 40px wider than they should for whatever reason. We add a little more to prevent horizontal scrolling
    Platform.runLater(() -> subDivisionTabPane.setTabMinWidth(subDivisionTabPane.getWidth() / subDivisionTabPane.getTabs().size() - 45.0));
    Platform.runLater(() -> subDivisionTabPane.setTabMaxWidth(subDivisionTabPane.getWidth() / subDivisionTabPane.getTabs().size() - 45.0));
    // Todo: sometimes when starting the client the tabs have no text and still have default width
  }
}
