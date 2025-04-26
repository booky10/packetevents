/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2025 retrooper and contributors
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

package com.github.retrooper.packetevents.util.codecs;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

@NullMarked
public abstract class CodecOps<T> {

    protected final PacketWrapper<?> packet;

    public CodecOps(PacketWrapper<?> packet) {
        this.packet = packet;
    }

    public abstract CodecResult<Number> getNumberValue(T input);

    @Contract("_, !null -> !null")
    public @Nullable Number getNumberValueOr(T input, @Nullable Number defaultNumber) {
        Number number = this.getNumberValue(input).getResult();
        return number != null ? number : defaultNumber;
    }

    @SuppressWarnings("unchecked") // doesn't matter when it's an error
    public CodecResult<Boolean> getBooleanValue(T input) {
        CodecResult<Number> number = this.getNumberValue(input);
        Number numberResult = number.getResult();
        if (numberResult != null) {
            return new CodecResult<>(numberResult.byteValue() != 0);
        }
        return (CodecResult<Boolean>) (CodecResult<?>) number;
    }

    public abstract CodecResult<String> getStringValue(T input);

    public abstract CodecResult<Map<String, T>> getMap(T input);

    public abstract CodecResult<List<T>> getList(T input);

    public abstract T createEmpty();

    public abstract T createEmptyMap();

    public abstract T createEmptyList();

    public abstract T createNumber(Number number);

    public abstract T createByte(byte number);

    public abstract T createShort(short number);

    public abstract T createInt(int number);

    public abstract T createLong(long number);

    public abstract T createFloat(float number);

    public abstract T createDouble(double number);

    public abstract T createBoolean(boolean value);

    public abstract T createString(String value);

    public abstract T createMap(Map<String, T> values);

    public abstract T createList(List<T> values);

    public abstract T createByteList(byte[] values);

    public abstract T createIntList(int[] values);

    public abstract T getLongList(long[] values);

    public PacketWrapper<?> getPacket() {
        return this.packet;
    }
}
