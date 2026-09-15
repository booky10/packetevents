/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2024 retrooper and contributors
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

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.ItemStackSerialization;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

public class PotDecorations {

    private @Nullable ItemType back;
    private @Nullable ItemType left;
    private @Nullable ItemType right;
    private @Nullable ItemType front;
    private @Nullable ItemStack backStack, leftStack, rightStack, frontStack;

    private PotDecorations(Queue<Optional<ItemType>> items) {
        this(
                items.isEmpty() ? null : items.remove().orElse(null),
                items.isEmpty() ? null : items.remove().orElse(null),
                items.isEmpty() ? null : items.remove().orElse(null),
                items.isEmpty() ? null : items.remove().orElse(null)
        );
    }

    public PotDecorations(
            @Nullable ItemType back,
            @Nullable ItemType left,
            @Nullable ItemType right,
            @Nullable ItemType front
    ) {
        this.back = back;
        this.left = left;
        this.right = right;
        this.front = front;
    }

    private List<Optional<ItemType>> asList() {
        return Arrays.asList(
                Optional.ofNullable(this.back),
                Optional.ofNullable(this.left),
                Optional.ofNullable(this.right),
                Optional.ofNullable(this.front)
        );
    }

    private static Optional<ItemType> readItem(PacketWrapper<?> wrapper) {
        ItemType type = wrapper.readMappedEntity(ItemTypes::getById);
        return type == ItemTypes.BRICK ? Optional.empty() : Optional.of(type);
    }

    public static PotDecorations read(PacketWrapper<?> wrapper) {
        if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_3)) {
            PotDecorations decorations = new PotDecorations(null, null, null, null);
            decorations.setBackStack(ItemStackSerialization.readOptionalTemplate(wrapper));
            decorations.setLeftStack(ItemStackSerialization.readOptionalTemplate(wrapper));
            decorations.setRightStack(ItemStackSerialization.readOptionalTemplate(wrapper));
            decorations.setFrontStack(ItemStackSerialization.readOptionalTemplate(wrapper));
            return decorations;
        }
        Queue<Optional<ItemType>> items = wrapper.<Optional<ItemType>, Queue<Optional<ItemType>>>
                readCollection(ArrayDeque::new, PotDecorations::readItem);
        return new PotDecorations(items);
    }

    private static void writeItem(PacketWrapper<?> wrapper, Optional<ItemType> type) {
        wrapper.writeMappedEntity(type.orElse(ItemTypes.BRICK));
    }

    public static void write(PacketWrapper<?> wrapper, PotDecorations decorations) {
        if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_3)) {
            ItemStackSerialization.writeOptionalTemplate(wrapper, decorations.getBackStack());
            ItemStackSerialization.writeOptionalTemplate(wrapper, decorations.getLeftStack());
            ItemStackSerialization.writeOptionalTemplate(wrapper, decorations.getRightStack());
            ItemStackSerialization.writeOptionalTemplate(wrapper, decorations.getFrontStack());
            return;
        }
        wrapper.writeList(decorations.asList(), PotDecorations::writeItem);
    }

    public @Nullable ItemType getBack() {
        return this.back;
    }

    public void setBack(@Nullable ItemType back) {
        this.back = back;
        this.backStack = null;
    }

    public @Nullable ItemType getLeft() {
        return this.left;
    }

    public void setLeft(@Nullable ItemType left) {
        this.left = left;
        this.leftStack = null;
    }

    public @Nullable ItemType getRight() {
        return this.right;
    }

    public void setRight(@Nullable ItemType right) {
        this.right = right;
        this.rightStack = null;
    }

    public @Nullable ItemType getFront() {
        return this.front;
    }

    public void setFront(@Nullable ItemType front) {
        this.front = front;
        this.frontStack = null;
    }

    private static ItemStack stack(@Nullable ItemType type, @Nullable ItemStack stack) {
        return stack != null ? stack : type == null ? ItemStack.EMPTY : ItemStack.builder().type(type).build();
    }

    /**
     * @versions 26.3+
     */
    public ItemStack getBackStack() {
        return stack(this.back, this.backStack);
    }
    /**
     * @versions 26.3+
     */
    public ItemStack getLeftStack() {
        return stack(this.left, this.leftStack);
    }
    /**
     * @versions 26.3+
     */
    public ItemStack getRightStack() {
        return stack(this.right, this.rightStack);
    }
    /**
     * @versions 26.3+
     */
    public ItemStack getFrontStack() {
        return stack(this.front, this.frontStack);
    }
    /**
     * @versions 26.3+
     */
    public void setBackStack(ItemStack stack) {
        this.backStack = stack;
        this.back = stack.isEmpty() ? null : stack.getType();
    }
    /**
     * @versions 26.3+
     */
    public void setLeftStack(ItemStack stack) {
        this.leftStack = stack;
        this.left = stack.isEmpty() ? null : stack.getType();
    }
    /**
     * @versions 26.3+
     */
    public void setRightStack(ItemStack stack) {
        this.rightStack = stack;
        this.right = stack.isEmpty() ? null : stack.getType();
    }
    /**
     * @versions 26.3+
     */
    public void setFrontStack(ItemStack stack) {
        this.frontStack = stack;
        this.front = stack.isEmpty() ? null : stack.getType();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PotDecorations)) return false;
        PotDecorations that = (PotDecorations) obj;
        return sameTemplate(this.getBackStack(), that.getBackStack())
                && sameTemplate(this.getLeftStack(), that.getLeftStack())
                && sameTemplate(this.getRightStack(), that.getRightStack())
                && sameTemplate(this.getFrontStack(), that.getFrontStack());
    }

    private static boolean sameTemplate(ItemStack first, ItemStack second) {
        // Templates store patches, independently of lazily initialized default components.
        return first.getType().equals(second.getType()) && first.getAmount() == second.getAmount()
                && first.getComponents().getPatches().equals(second.getComponents().getPatches());
    }

    private static int templateHash(ItemStack stack) {
        return Objects.hash(stack.getType(), stack.getAmount(), stack.getComponents().getPatches());
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateHash(this.getBackStack()), templateHash(this.getLeftStack()),
                templateHash(this.getRightStack()), templateHash(this.getFrontStack()));
    }
}
