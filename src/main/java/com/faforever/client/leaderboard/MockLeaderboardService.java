package com.faforever.client.leaderboard;

import com.faforever.client.FafClientApplication;
import com.faforever.client.i18n.I18n;
import com.faforever.client.task.CompletableTask;
import com.faforever.client.task.TaskService;
import com.faforever.client.util.Tuple;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.faforever.client.leaderboard.DivisionName.BRONZE;
import static com.faforever.client.leaderboard.DivisionName.COMMANDER;
import static com.faforever.client.leaderboard.DivisionName.DIAMOND;
import static com.faforever.client.leaderboard.DivisionName.GOLD;
import static com.faforever.client.leaderboard.DivisionName.I;
import static com.faforever.client.leaderboard.DivisionName.II;
import static com.faforever.client.leaderboard.DivisionName.III;
import static com.faforever.client.leaderboard.DivisionName.IV;
import static com.faforever.client.leaderboard.DivisionName.MASTER;
import static com.faforever.client.leaderboard.DivisionName.NONE;
import static com.faforever.client.leaderboard.DivisionName.SILVER;
import static com.faforever.client.leaderboard.DivisionName.V;
import static com.faforever.client.task.CompletableTask.Priority.HIGH;

@Lazy
@Service
@Profile(FafClientApplication.PROFILE_OFFLINE)
@RequiredArgsConstructor
public class MockLeaderboardService implements LeaderboardService {

  private final TaskService taskService;
  private final I18n i18n;

  public CompletableFuture<List<RatingStat>> getLeaderboardStats(String leaderboardTechnicalName) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<Leaderboard>> getLeaderboards() {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<League>> getLeagues() {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<DivisionStat>> getDivisionStats() {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<Division>> getDivisions(String leagueTechnicalName) {
    DivisionName[] subnames = {V, IV, III, II, I};
    DivisionName[] majornames = {BRONZE, SILVER, GOLD, DIAMOND, MASTER};
    List<Division> divisions = new LinkedList<>();
    for (int k=1; k<6; k++) {
      for (int i=1; i<6; i++) {
        Division div = new Division(1, k, i, majornames[k-1], subnames[i-1], 10);
        //if (k!=5 || i!=5)
        divisions.add(div);
      }
    }
    Division div2 = new Division(1, 6, 1, COMMANDER, NONE, 10);
    divisions.add(div2);
    return CompletableFuture.completedFuture(divisions);
  }

  @Override
  public CompletableFuture<List<LeagueEntry>> getDivisionEntries(Division division) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<LeaderboardEntry>> getEntriesForPlayer(int playerId) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<List<LeaderboardEntry>> getEntries(Leaderboard leaderboard) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<Tuple<List<LeaderboardEntry>, Integer>> getPagedEntries(Leaderboard leaderboard, int count, int page) {
    return CompletableFuture.completedFuture(new Tuple<>(Collections.emptyList(), 1));
  }

  @Override
  public CompletableFuture<LeagueEntry> getLeagueEntryForPlayer(int playerId, String leagueTechnicalName) {
    LeagueEntry entry = new LeagueEntry();
    entry.setSubDivisionIndex(4);
    entry.setMajorDivisionIndex(2);
    entry.setScore(8);
    entry.setGamesPlayed(3);

    Throwable noEntry = new Throwable();
    boolean testNoEntry = false;
    if (testNoEntry)
      return CompletableFuture.failedFuture(noEntry);
    else
      return CompletableFuture.completedFuture(entry);
  }

  @Override
  public CompletableFuture<List<LeagueEntry>> getEntries(Division division) {
    return taskService.submitTask(new CompletableTask<List<LeagueEntry>>(HIGH) {
      @Override
      protected List<LeagueEntry> call() throws Exception {
        updateTitle("Reading ladder");

        List<LeagueEntry> list = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
          String name = RandomStringUtils.random(10);
          int score = (int) (Math.random() * 25);
          int gamecount = (int) (Math.random() * 1000);
          float winloss = (float) (Math.random() * 100);

          list.add(createLeagueEntryBean(name, score, gamecount, winloss));

        }
        return list;
      }
    }).getFuture();
  }

  private LeagueEntry createLeagueEntryBean(String name, int score, int gamesPlayed, float winLossRatio) {
    LeagueEntry leagueEntry = new LeagueEntry();
    leagueEntry.setUsername(name);
    leagueEntry.setScore(score);
    leagueEntry.setGamesPlayed(gamesPlayed);
    leagueEntry.setWinLossRatio(winLossRatio);

    return leagueEntry;
  }
}
