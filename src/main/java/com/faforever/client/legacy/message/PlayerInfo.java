package com.faforever.client.legacy.message;

import java.util.Map;

public class PlayerInfo extends ServerMessage {

  public String clan;
  public String login;
  public String avatar;
  public String country;
  public String ratingMean;
  public Integer numberOfGames;
  public Float ratingDeviation;
  public Double ladderRatingMean;
  public Map<String, String> league;
  public Double ladderRatingDeviation;
}
