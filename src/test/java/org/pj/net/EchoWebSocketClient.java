package org.pj.net;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoWebSocketClient extends WebSocketClient {


  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private CountDownLatch latch;

  public EchoWebSocketClient(URI serverUri,  CountDownLatch latch) {
    super(serverUri);
    this.latch = latch;
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {

  }

  @Override
  public void onMessage(String s) {
    logger.info(s);
    latch.countDown();
  }

  @Override
  public void onMessage(ByteBuffer bytes) {
    logger.info(new String(bytes.array()));
    latch.countDown();
  }

  @Override
  public void onClose(int i, String s, boolean b) {

  }

  @Override
  public void onError(Exception e) {

  }
}
