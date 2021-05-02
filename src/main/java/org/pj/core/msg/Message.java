package org.pj.core.msg;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

public class Message {

  /** 状态码 */
  private int states;
  /** 模块 */
  private int module;
  /** 操作序号 */
  private int opt;
  /** 内容 */
  private byte[] body = ArrayUtils.EMPTY_BYTE_ARRAY;

  public static Message valueOf() {
    return new Message();
  }

  public static Message readFrom(ByteBuf bytes) {
    Message message = new Message();
    message.setStates(bytes.readInt())
        .setModule(bytes.readInt())
        .setOpt(bytes.readInt())
        .setBody(ArrayUtils.EMPTY_BYTE_ARRAY);
    if (0 < bytes.readableBytes()) {
      byte[] body = new byte[bytes.readableBytes()];
      bytes.readBytes(body);
      message.setBody(body);
    }
    return message;
  }

  public static Message readFrom(ByteBuffer bytes) {
    Message message = new Message();
    message.setStates(bytes.getInt())
        .setModule(bytes.getInt())
        .setOpt(bytes.getInt())
        .setBody(ArrayUtils.EMPTY_BYTE_ARRAY);
    if (0 < bytes.remaining()) {
      byte[] body = new byte[bytes.remaining()];
      bytes.get(body);
      message.setBody(body);
    }
    return message;
  }

  public byte[] toByteArray() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES * 3 + body.length);
    byteBuffer.putInt(states)
        .putInt(module)
        .putInt(opt)
        .put(body);
    return byteBuffer.array();
  }

  public void toByteArray(ByteBuf buf) {
    buf.writeInt(this.getStates())
        .writeInt(this.getModule())
        .writeInt(this.getOpt())
        .writeBytes(ArrayUtils.EMPTY_BYTE_ARRAY);
    if (ObjectUtils.isNotEmpty(this.getBody())) {
      buf.writeBytes(this.getBody());
    }
  }

  public Message() {
  }

  public int getStates() {
    return states;
  }

  public Message setStates(int states) {
    this.states = states;
    return this;
  }

  public int getModule() {
    return module;
  }

  public Message setModule(int module) {
    this.module = module;
    return this;
  }

  public int getOpt() {
    return opt;
  }

  public Message setOpt(int opt) {
    this.opt = opt;
    return this;
  }

  public byte[] getBody() {
    return body;
  }

  public Message setBody(byte[] body) {
    this.body = body;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Message message = (Message) o;
    return states == message.states &&
        module == message.module &&
        opt == message.opt &&
        Arrays.equals(body, message.body);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(states, module, opt);
    result = 31 * result + Arrays.hashCode(body);
    return result;
  }
}
