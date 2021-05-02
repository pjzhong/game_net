package org.pj.core.msg;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;
import org.pj.core.msg.adp.ContextAdapter;

public class IAdapterTest {

  @Test
  public void contextFieldTest() {
    ContextAdapter adapter = ContextAdapter.getInstance();

    assertFalse(adapter.isContextField(String.class));

    assertTrue(adapter.isContextField(Message.class));
    assertTrue(adapter.isContextField(Channel.class));
    assertTrue(adapter.isContextField(Channel.class));
  }


}
