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

import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Either;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import java.util.Objects;

/**
 * Cooking burn time or brewing uses, followed by the speed multiplier.
 * @versions 26.3+
 */
public final class ItemFuel {
    private final Either<Integer, ResourceLocation> amount;
    private final Either<Float, ResourceLocation> speedMultiplier;

    public ItemFuel(Either<Integer, ResourceLocation> amount, Either<Float, ResourceLocation> speedMultiplier) {
        this.amount = Objects.requireNonNull(amount);
        this.speedMultiplier = Objects.requireNonNull(speedMultiplier);
    }

    public static ItemFuel read(PacketWrapper<?> wrapper) {
        return new ItemFuel(wrapper.readEither(PacketWrapper::readInt, PacketWrapper::readIdentifier),
                wrapper.readEither(PacketWrapper::readFloat, PacketWrapper::readIdentifier));
    }

    public static void write(PacketWrapper<?> wrapper, ItemFuel fuel) {
        wrapper.writeEither(fuel.amount, PacketWrapper::writeInt, PacketWrapper::writeIdentifier);
        wrapper.writeEither(fuel.speedMultiplier, PacketWrapper::writeFloat, PacketWrapper::writeIdentifier);
    }

    public Either<Integer, ResourceLocation> getAmount() {
        return this.amount;
    }
    public Either<Float, ResourceLocation> getSpeedMultiplier() {
        return this.speedMultiplier;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemFuel)) return false;
        ItemFuel other = (ItemFuel) obj;
        return this.amount.equals(other.amount) && this.speedMultiplier.equals(other.speedMultiplier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.amount, this.speedMultiplier);
    }
}
