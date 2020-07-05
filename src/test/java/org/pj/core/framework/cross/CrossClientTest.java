package org.pj.core.framework.cross;

import com.google.protobuf.ByteString;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pj.boot.ServerConfig;
import org.pj.core.framework.SpringGameContext;
import org.pj.core.msg.MessageProto.Message;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class CrossClientTest {

  private static SpringGameContext context;
  private static GenericApplicationContext springCtx;

  @BeforeClass
  public static void start() throws Exception {
    springCtx = new AnnotationConfigApplicationContext(ServerConfig.class);
    context = springCtx.getBean(SpringGameContext.class);
    context.start();
  }

  @AfterClass
  public static void close() {
    context.close();
    springCtx.close();
  }

  @Test
  public void echoTest() throws Exception {
    CrossGameClient client = new CrossGameClient(context);
    client.connect(URI.create("//localhost:8080"));

    Message request = Message.newBuilder()
        .setModule(2)
        .setBody(ByteString.copyFromUtf8("Hello World")).build();

    CountDownLatch latch = new CountDownLatch(1);
    client.sendMessage(request, new SocketCallback<>() {
      @Override
      public void onSuccess(Message message) {
        Assert.assertEquals(request.getBody(), message.getBody());
        latch.countDown();
      }

      @Override
      public void onError(Exception var1) {

      }
    });

    Assert.assertTrue("echo failed", latch.await(100, TimeUnit.MILLISECONDS));
  }


}
