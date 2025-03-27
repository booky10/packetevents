package com.github.retrooper.packetevents.protocol.tests;

import net.kyori.adventure.text.Component;

public enum TestBlockMode {
  START(0, "start"),
  LOG(1, "log"),
  FAIL(2, "fail"),
  ACCEPT(3, "accept");

  private final int id;
  private final String name;
  private final Component displayName;
  private final Component detailedMessage;

  TestBlockMode(final int id, final String mode) {
    this.id = id;
    this.name = mode;
    this.displayName = Component.translatable("test_block.mode." + mode);
    this.detailedMessage = Component.translatable("test_block.mode_info." + mode);
  }

  public int getId() {
    return id;
  }

  public String getSerializedName() {
    return this.name;
  }

  public Component getDisplayName() {
    return this.displayName;
  }

  public Component getDetailedMessage() {
    return this.detailedMessage;
  }
}