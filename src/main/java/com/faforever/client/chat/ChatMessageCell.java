package com.faforever.client.chat;

import com.faforever.client.fx.FxApplicationThreadExecutor;
import com.faforever.client.theme.UiService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.Cell;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChatMessageCell implements Cell<ChatMessage, Node> {

  private final ChatMessageController chatMessageController;
  private final FxApplicationThreadExecutor fxApplicationThreadExecutor;

  private final ObjectProperty<Consumer<ChatMessage>> onReplyButtonClicked = new SimpleObjectProperty<>();
  private final ObjectProperty<Consumer<ChatMessage>> onReplyClicked = new SimpleObjectProperty<>();
  private final ReadOnlyIntegerWrapper index = new ReadOnlyIntegerWrapper();
  private final BooleanProperty showDetails = new SimpleBooleanProperty();
  private final ReadOnlyObjectWrapper<ChatMessage> item = new ReadOnlyObjectWrapper<>();

  public ChatMessageCell(UiService uiService, FxApplicationThreadExecutor fxApplicationThreadExecutor) {
    this.fxApplicationThreadExecutor = fxApplicationThreadExecutor;
    chatMessageController = uiService.loadFxml("theme/chat/chat_message.fxml");
    chatMessageController.chatMessageProperty().bind(item);
    chatMessageController.showDetailsProperty().bind(showDetails);
    chatMessageController.onReplyButtonClickedProperty().bind(onReplyButtonClicked);
    chatMessageController.onReplyClickedProperty().bind(onReplyClicked);
  }

  @Override
  public VBox getNode() {
    return chatMessageController.getRoot();
  }

  @Override
  public boolean isReusable() {
    return true;
  }

  @Override
  public void updateItem(ChatMessage item) {
    fxApplicationThreadExecutor.execute(() -> this.item.set(item));
  }

  @Override
  public void updateIndex(int index) {
    fxApplicationThreadExecutor.execute(() -> this.index.set(index));
  }

  @Override
  public void reset() {
    fxApplicationThreadExecutor.execute(() -> this.item.set(null));
  }

  public Consumer<ChatMessage> getOnReplyButtonClicked() {
    return onReplyButtonClicked.get();
  }

  public ObjectProperty<Consumer<ChatMessage>> onReplyButtonClickedProperty() {
    return onReplyButtonClicked;
  }

  public void setOnReplyButtonClicked(Consumer<ChatMessage> onReplyButtonClicked) {
    this.onReplyButtonClicked.set(onReplyButtonClicked);
  }

  public Consumer<ChatMessage> getOnReplyClicked() {
    return onReplyClicked.get();
  }

  public ObjectProperty<Consumer<ChatMessage>> onReplyClickedProperty() {
    return onReplyClicked;
  }

  public void setOnReplyClicked(Consumer<ChatMessage> onReplyClicked) {
    this.onReplyClicked.set(onReplyClicked);
  }

  public boolean isShowDetails() {
    return showDetails.get();
  }

  public BooleanProperty showDetailsProperty() {
    return showDetails;
  }

  public void setShowDetails(boolean showDetails) {
    this.showDetails.set(showDetails);
  }

  public int getIndex() {
    return index.get();
  }

  public ReadOnlyIntegerProperty indexProperty() {
    return index.getReadOnlyProperty();
  }

  public ReadOnlyObjectProperty<ChatMessage> itemProperty() {
    return item.getReadOnlyProperty();
  }

  public ChatMessage getItem() {
    return item.get();
  }
}
