package org.pj.core.framework.cross;

import java.util.concurrent.CompletableFuture;
import org.pj.core.msg.MessageProto.Message;

public class CompleteSocketCallBack implements ResultCallBack {

  private CompletableFuture<Object> future;

  public CompleteSocketCallBack(CompletableFuture<?> future) {
    this.future = (CompletableFuture<Object>) future;
  }

  @Override
  public void accept(Object message) {
    future.complete(message);
  }

  @Override
  public void acceptErr(Message message) {
    future.complete(null);
  }

  @Override
  public void onException(Exception exp) {
    future.completeExceptionally(exp);
  }
}
