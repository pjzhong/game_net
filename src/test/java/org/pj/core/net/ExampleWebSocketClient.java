package org.pj.core.net;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleWebSocketClient extends WebSocketClient {

  public ExampleWebSocketClient(URI serverUri) {
    super(serverUri);
  }


  @Override
  public void onOpen(ServerHandshake serverHandshake) {
  }

  @Override
  public void onMessage(String s) {
  }

  @Override
  public void onMessage(ByteBuffer bytes) {
  }

  @Override
  public void onClose(int i, String s, boolean b) {

  }

  @Override
  public void onError(Exception e) {

  }
}
