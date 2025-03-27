package com.github.retrooper.packetevents.protocol.chat.message.reader.impl;

import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.LastSeenMessages;
import com.github.retrooper.packetevents.protocol.chat.filter.FilterMask;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_3;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_21_5;
import com.github.retrooper.packetevents.protocol.chat.message.reader.ChatMessageProcessor;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.time.Instant;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ChatMessageProcessor_v1_21_5 implements ChatMessageProcessor {
    @Override
    public ChatMessage readChatMessage(@NotNull PacketWrapper<?> wrapper) {
        int globalIndex = wrapper.readVarInt();
        UUID senderUUID = wrapper.readUUID();
        int index = wrapper.readVarInt();
        byte[] signature = wrapper.readOptional((w) -> w.readBytes(256));
        String plainContent = wrapper.readString(256);
        Instant timestamp = wrapper.readTimestamp();
        long salt = wrapper.readLong();
        LastSeenMessages.Packed lastSeenMessagesPacked = wrapper.readLastSeenMessagesPacked();
        Component unsignedChatContent = wrapper.readOptional(PacketWrapper::readComponent);
        FilterMask filterMask = wrapper.readFilterMask();
        ChatType.Bound chatType = wrapper.readChatTypeBoundNetwork();

        return new ChatMessage_v1_21_5(globalIndex, senderUUID, index, signature, plainContent, timestamp, salt, lastSeenMessagesPacked, unsignedChatContent, filterMask, chatType);
    }

    @Override
    public void writeChatMessage(@NotNull PacketWrapper<?> wrapper, @NotNull ChatMessage data) {
        ChatMessage_v1_21_5 newData = (ChatMessage_v1_21_5) data;
        wrapper.writeVarInt(newData.getGlobalIndex());
        wrapper.writeUUID(newData.getSenderUUID());
        wrapper.writeVarInt(newData.getIndex());
        wrapper.writeOptional(newData.getSignature(), PacketWrapper::writeBytes);
        wrapper.writeString(newData.getPlainContent());
        wrapper.writeTimestamp(newData.getTimestamp());
        wrapper.writeLong(newData.getSalt());
        wrapper.writeLastSeenMessagesPacked(newData.getLastSeenMessagesPacked());
        wrapper.writeOptional(newData.getUnsignedChatContent().orElse(null), PacketWrapper::writeComponent);
        wrapper.writeFilterMask(newData.getFilterMask());
        wrapper.writeChatTypeBoundNetwork(newData.getChatFormatting());
    }
}
