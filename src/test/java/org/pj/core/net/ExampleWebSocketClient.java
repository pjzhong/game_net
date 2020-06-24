package org.pj.core.net;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleWebSocketClient extends WebSocketClient {


  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private CountDownLatch latch;

  public ExampleWebSocketClient(URI serverUri) {
    super(serverUri);
  }


  public ExampleWebSocketClient(URI serverUri, CountDownLatch latch) {
    super(serverUri);
    this.latch = latch;
  }


  @Override
  public void onOpen(ServerHandshake serverHandshake) {

  }

  @Override
  public void onMessage(String s) {
    logger.info(s);

  }

  @Override
  public void onMessage(ByteBuffer bytes) {
    logger.info(new String(bytes.array()));
    if (latch != null) {
      latch.countDown();
    }
  }

  @Override
  public void onClose(int i, String s, boolean b) {

  }

  @Override
  public void onError(Exception e) {

  }
}
