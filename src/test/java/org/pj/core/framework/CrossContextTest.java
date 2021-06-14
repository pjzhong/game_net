package org.pj.core.framework;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.pj.common.hello.HelloWorldProto.HelloWorld;
import org.pj.config.CrossServerConfig;
import org.pj.config.GameServerConfig;
import org.pj.core.anno.Facade;
import org.pj.core.framework.cross.CrossGameClient;
import org.pj.core.framework.cross.ResultCallBackAdapter;
import org.pj.core.framework.cross.SocketCallback;
import org.pj.core.msg.Message;
import org.pj.core.msg.Packet;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;


@SpringBootTest(classes = {GameServerConfig.class, CrossServerConfig.class})
@TestExecutionListeners(CrossContextTest.class)
public class CrossContextTest implements TestExecutionListener {

  private static SpringGameContext local;
  private static SpringGameContext cross;


  @Override
  public void beforeTestClass(TestContext testContext) throws Exception {
    local = testContext.getApplicationContext().getBean("gameContext", SpringGameContext.class);
    cross = testContext.getApplicationContext().getBean("crossContext", SpringGameContext.class);
    local.start();
    cross.start();
  }

  @Override
  public void afterTestClass(TestContext testContext) throws Exception {
    local.close();
    cross.close();
  }


  @Test
  public void echoTest() throws Exception {
    CrossGameClient client = new CrossGameClient(local);
    client.connect(URI.create("ws://localhost:8081"));

    Message request = Message.valueOf()
        .setModule(client.genMsgId())
        .setModule(2)
        .setBody("Hello World".getBytes(StandardCharsets.UTF_8));

    CountDownLatch latch = new CountDownLatch(1);
    client.addSocketCallback(request.getOpt(), (SocketCallback<Message>) message -> {
      assertArrayEquals(request.getBody(), message.getBody());
      assertEquals(request.getOpt(), message.getOpt());
      latch.countDown();
    });
    client.sendMessage(request);

    assertTrue(latch.await(1, TimeUnit.SECONDS), "echo failed");
  }

  @Test
  public void asyncProxyTest() throws Exception {
    CrossGameClient client = new CrossGameClient(local);
    client.connect(URI.create("//localhost:8081"));

    HelloWorld helloWorld = HelloWorld.newBuilder().setStr("HAHAHAHAHAHAHA").build();

    CountDownLatch latch = new CountDownLatch(1);
    CrossHelloFacade facade = client
        .asyncProxy(CrossHelloFacade.class, new ResultCallBackAdapter<HelloWorld>() {
          @Override
          public void accept(HelloWorld message) {
            assertEquals(helloWorld, message);
            latch.countDown();
          }

        });

    facade.echoHelloWorld(helloWorld);
    assertTrue(latch.await(100, TimeUnit.MILLISECONDS), "echo failed");
  }

  public void syncProxyTest() {
    //TODO 尝试提供同步功能
/*    CrossGameClient client = new CrossGameClient(local);
    client.connect(URI.create("//localhost:8081"));

    HelloWorld helloWorld = HelloWorld.newBuilder().setStr("HAHAHAHAHAHAHA").build();
    CrossHelloFacade facade = client.asyncProxy(CrossHelloFacade.class,
        (SocketCallback<HelloWorld>) System.out::println);

    HelloWorld world = facade.echoHelloWorld(helloWorld);
    assertEquals(helloWorld, world);*/
  }

  @Test
  public void syncProxyTestFailed() throws Exception {
    CrossGameClient client = new CrossGameClient(local);
    client.connect(URI.create("//localhost:8081"));

    CrossHelloFacade facade = client.syncProxy(CrossHelloFacade.class);
    assertNull(facade.notExists());
  }


  @Facade
  public interface CrossHelloFacade {

    @Packet(2)
    HelloWorld echoHelloWorld(HelloWorld world);

    @Packet(Integer.MAX_VALUE)
    HelloWorld notExists();
  }

}
