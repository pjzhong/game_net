package org.pj.core.context;

import com.google.protobuf.InvalidProtocolBufferException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pj.boot.ServerConfig;
import org.pj.core.framework.SpringGameContext;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.net.ExampleWebSocketClient;
import org.pj.protocols.hello.HelloWorldProto.HelloWorld;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GameContextTest {

  private static SpringGameContext ctx;

  @BeforeClass
  public static void init() throws Exception {
    long start = System.currentTimeMillis();
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServerConfig.class);
    SpringGameContext gameContext = context.getBean(SpringGameContext.class);
    gameContext.start();

    ctx = gameContext;
    System.out.println("cost:" + (System.currentTimeMillis() - start));
  }

  @AfterClass
  public static void end() throws Exception {
    ctx.close();
  }


  @Test
  public void echoHelloWorld() throws Exception {
    HelloWorld world = HelloWorld.newBuilder().setStr("Hello World").build();
    Message message = Message.newBuilder()
        .setVersion(1)
        .setModule(3)
        .setStat(200)
        .setSerial(0)
        .setBody(HelloWorld.newBuilder().setStr("Hello World").build().toByteString()).build();

    int loop = 5;
    CountDownLatch latch = new CountDownLatch(loop);
    WebSocketClient client = new ExampleWebSocketClient(new URI("ws://127.0.0.1:8080"), latch) {
      @Override
      public void onMessage(ByteBuffer bytes) {
        try {
          Message echoMessage = Message.parseFrom(bytes);
          HelloWorld echoWorld = HelloWorld.parseFrom(echoMessage.getBody());
          Assert.assertEquals(message, echoMessage);
          Assert.assertEquals(echoWorld, world);
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
        latch.countDown();
      }
    };
    client.connectBlocking();

    for (int i = 0; i < loop; i++) {
      client.send(message.toByteArray());
    }

    boolean suc = latch.await(1, TimeUnit.SECONDS);
    Assert.assertTrue("Echo Failed", suc);
  }

}
