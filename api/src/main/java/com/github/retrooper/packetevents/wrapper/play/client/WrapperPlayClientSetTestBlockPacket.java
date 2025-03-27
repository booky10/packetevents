package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.tests.TestBlockMode;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientSetTestBlockPacket extends PacketWrapper<WrapperPlayClientSetTestBlockPacket> {

  private Vector3i blockPosition;
  private TestBlockMode mode;
  private String message;

  public WrapperPlayClientSetTestBlockPacket(PacketReceiveEvent event) {
    super(event);
  }

  @Override
  public void read() {
    blockPosition = readBlockPosition();
    mode = readEnum(TestBlockMode.class);
    message = readString();
  }

  @Override
  public void write() {
    writeBlockPosition(blockPosition);
    writeEnum(mode);
    writeString(message);
  }

  @Override
  public void copy(WrapperPlayClientSetTestBlockPacket wrapper) {
    this.blockPosition = wrapper.blockPosition;
    this.mode = wrapper.mode;
    this.message = wrapper.message;
  }

  public Vector3i getBlockPosition() {
    return blockPosition;
  }

  public TestBlockMode getMode() {
    return mode;
  }

  public String getMessage() {
    return message;
  }
}

