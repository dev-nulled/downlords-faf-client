package com.faforever.client.leaderboard;

import com.faforever.client.util.Tuple;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LeaderboardService {
  int MINIMUM_GAMES_PLAYED_TO_BE_SHOWN = 10;

  CompletableFuture<List<Leaderboard>> getLeaderboards();

  CompletableFuture<List<League>> getLeagues();

  CompletableFuture<List<LeaderboardEntry>> getEntriesForPlayer(int playerId);

  CompletableFuture<List<LeaderboardEntry>> getEntries(Leaderboard leaderboard);

  CompletableFuture<Tuple<List<LeaderboardEntry>, Integer>> getPagedEntries(Leaderboard leaderboard, int count, int page);

  CompletableFuture<List<RatingStat>> getLeaderboardStats(String leaderboardTechnicalName);

  CompletableFuture<LeagueEntry> getLeagueEntryForPlayer(int playerId, String leagueTechnicalName);

  CompletableFuture<List<LeagueEntry>> getEntries(Division division);

  CompletableFuture<List<DivisionStat>> getDivisionStats(String leagueTechnicalName);

  CompletableFuture<List<Division>> getDivisions(String leagueTechnicalName);

  CompletableFuture<List<LeagueEntry>> getDivisionEntries(Division division);
}
