/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2026 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.protocol.color.DyeColor;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @versions 26.3+
 */
public final class ItemSignText {
    private final List<Component> messages;
    private final List<Component> filteredMessages;
    private final DyeColor color;
    private final boolean glowing;

    public ItemSignText(List<Component> messages, List<Component> filteredMessages, DyeColor color, boolean glowing) {
        if (messages.size() != 4 || filteredMessages.size() != 4) throw new IllegalArgumentException("Sign text requires four lines");
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
        this.filteredMessages = Collections.unmodifiableList(new ArrayList<>(filteredMessages));
        this.color = Objects.requireNonNull(color);
        this.glowing = glowing;
    }

    private static List<Component> readLines(PacketWrapper<?> wrapper) {
        List<Component> lines = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) lines.add(wrapper.readComponent());
        return lines;
    }

    public static ItemSignText read(PacketWrapper<?> wrapper) {
        List<Component> messages = readLines(wrapper);
        List<Component> filtered = wrapper.readBoolean() ? readLines(wrapper) : messages;
        return new ItemSignText(messages, filtered, DyeColor.read(wrapper), wrapper.readBoolean());
    }

    public static void write(PacketWrapper<?> wrapper, ItemSignText text) {
        text.messages.forEach(wrapper::writeComponent);
        boolean filtered = !text.messages.equals(text.filteredMessages);
        wrapper.writeBoolean(filtered);
        if (filtered) text.filteredMessages.forEach(wrapper::writeComponent);
        DyeColor.write(wrapper, text.color);
        wrapper.writeBoolean(text.glowing);
    }

    public List<Component> getMessages() {
        return this.messages;
    }
    public List<Component> getFilteredMessages() {
        return this.filteredMessages;
    }
    public DyeColor getColor() {
        return this.color;
    }
    public boolean isGlowing() {
        return this.glowing;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemSignText)) return false;
        ItemSignText other = (ItemSignText) obj;
        return this.messages.equals(other.messages) && this.filteredMessages.equals(other.filteredMessages)
                && this.color == other.color && this.glowing == other.glowing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.messages, this.filteredMessages, this.color, this.glowing);
    }
}
