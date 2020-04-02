package org.pj.net;

import java.net.URI;
import java.nio.ByteBuffer;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.LoggerFactory;

public class ExampleClient extends WebSocketClient {

  public ExampleClient(URI serverUri) {
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
    LoggerFactory.getLogger(this.getClass()).info(new String(bytes.array()));
  }

  @Override
  public void onClose(int i, String s, boolean b) {

  }

  @Override
  public void onError(Exception e) {

  }
}
