package org.pj.core.framework.cross;

import com.google.protobuf.ByteString;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pj.boot.CrossServerConfig;
import org.pj.boot.ServerConfig;
import org.pj.core.framework.SpringGameContext;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.msg.Packet;
import org.pj.protocols.Facade;
import org.pj.protocols.hello.HelloWorldProto.HelloWorld;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class CrossClientTest {

  private static GenericApplicationContext localCtx;
  private static GenericApplicationContext crossCtx;

  private static SpringGameContext local;
  private static SpringGameContext cross;

  @BeforeClass
  public static void start() throws Exception {
    localCtx = new AnnotationConfigApplicationContext(ServerConfig.class);
    local = localCtx.getBean(SpringGameContext.class);
    local.start();

    crossCtx = new AnnotationConfigApplicationContext(
        CrossServerConfig.class);
    cross = crossCtx.getBean(SpringGameContext.class);
    cross.start();
  }

  @AfterClass
  public static void close() {
    local.close();
    cross.close();

    localCtx.close();
    crossCtx.close();
  }

  @Test
  public void echoTest() throws Exception {
    CrossGameClient client = new CrossGameClient(local);
    client.connect(URI.create("//localhost:8081"));

    Message request = Message.newBuilder()
        .setModule(client.genMsgId())
        .setModule(2)
        .setBody(ByteString.copyFromUtf8("Hello World")).build();

    CountDownLatch latch = new CountDownLatch(1);
    client.addSocketCallback(request.getSerial(), new SocketCallback<Message>() {
      @Override
      public void accept(Message message) {
        Assert.assertEquals(request.getBody(), message.getBody());
        Assert.assertEquals(request.getSerial(), message.getSerial());
        latch.countDown();
      }
    });
    client.sendMessage(request);

    Assert.assertTrue("echo failed", latch.await(100, TimeUnit.MILLISECONDS));
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
            Assert.assertEquals(helloWorld, message);
            latch.countDown();
          }

        });

    facade.echoHelloWorld(helloWorld);
    Assert.assertTrue("echo failed", latch.await(100, TimeUnit.MILLISECONDS));
  }

  @Test
  public void syncProxyTest() throws Exception {
    CrossGameClient client = new CrossGameClient(local);
    client.connect(URI.create("//localhost:8081"));

    HelloWorld helloWorld = HelloWorld.newBuilder().setStr("HAHAHAHAHAHAHA").build();
    CrossHelloFacade facade = client.syncProxy(CrossHelloFacade.class);

    HelloWorld world = facade.echoHelloWorld(helloWorld);
    Assert.assertEquals(helloWorld, world);
  }

  @Test
  public void syncProxyTestFailed() throws Exception {
    CrossGameClient client = new CrossGameClient(local);
    client.connect(URI.create("//localhost:8081"));

    CrossHelloFacade facade = client.syncProxy(CrossHelloFacade.class);
    Assert.assertNull(facade.notExists());
  }


  @Facade
  public interface CrossHelloFacade {

    @Packet(3)
    HelloWorld echoHelloWorld(HelloWorld world);

    @Packet(Integer.MAX_VALUE)
    HelloWorld notExists();
  }

}
