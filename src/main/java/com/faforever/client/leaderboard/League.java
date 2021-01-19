package com.faforever.client.leaderboard;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Objects;

@Data
public class League {
  private final IntegerProperty id;
  private final StringProperty technicalName;
  private final StringProperty nameKey;
  private final StringProperty descriptionKey;
  private final ObjectProperty<OffsetDateTime> createTime;
  private final ObjectProperty<OffsetDateTime> updateTime;

  public League() {
    id = new SimpleIntegerProperty();
    technicalName = new SimpleStringProperty();
    nameKey = new SimpleStringProperty();
    descriptionKey = new SimpleStringProperty();
    createTime = new SimpleObjectProperty<>();
    updateTime = new SimpleObjectProperty<>();
  }

  public static League fromDto(com.faforever.client.api.dto.League dto) {
    League league = new League();
    league.setId(Integer.parseInt(dto.getId()));
    league.setCreateTime(dto.getCreateTime());
    league.setUpdateTime(dto.getUpdateTime());
    league.setDescriptionKey(dto.getDescriptionKey());
    league.setNameKey(dto.getNameKey());
    league.setTechnicalName(dto.getTechnicalName());
    return league;
  }

  public Integer getId() {
    return id.get();
  }

  public void setId(int id) {
    this.id.set(id);
  }

  public IntegerProperty idProperty() {
    return id;
  }

  public String getDescriptionKey() {
    return descriptionKey.get();
  }

  public void setDescriptionKey(String descriptionKey) {
    this.descriptionKey.set(descriptionKey);
  }

  public StringProperty descriptionKeyProperty() {
    return descriptionKey;
  }

  public String getNameKey() {
    return nameKey.get();
  }

  public void setNameKey(String nameKey) {
    this.nameKey.set(nameKey);
  }

  public StringProperty nameKeyProperty() {
    return nameKey;
  }

  public String getTechnicalName() {
    return technicalName.get();
  }

  public void setTechnicalName(String technicalName) {
    this.technicalName.set(technicalName);
  }

  public StringProperty technicalNameProperty() {
    return technicalName;
  }

  public OffsetDateTime getCreateTime() {
    return createTime.get();
  }

  public void setCreateTime(OffsetDateTime createTime) {
    this.createTime.set(createTime);
  }

  public ObjectProperty<OffsetDateTime> createTimeProperty() {
    return createTime;
  }

  public OffsetDateTime getUpdateTime() {
    return updateTime.get();
  }

  public void setUpdateTime(OffsetDateTime updateTime) {
    this.updateTime.set(updateTime);
  }

  public ObjectProperty<OffsetDateTime> updateTimeProperty() {
    return updateTime;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id.get());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    League that = (League) o;

    return id.get() == that.id.get();

  }

  @Override
  public String toString() {
    return "League{" +
        "technicalName=" + technicalName.get() +
        '}';
  }
}
