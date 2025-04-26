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

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTByte;
import com.github.retrooper.packetevents.protocol.nbt.NBTByteArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTCollection;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTDouble;
import com.github.retrooper.packetevents.protocol.nbt.NBTEnd;
import com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.nbt.NBTIntArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTListUtil;
import com.github.retrooper.packetevents.protocol.nbt.NBTLong;
import com.github.retrooper.packetevents.protocol.nbt.NBTLongArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTNumber;
import com.github.retrooper.packetevents.protocol.nbt.NBTShort;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.nbt.NBTType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Map;

@NullMarked
public class NbtCodecOps extends CodecOps<NBT> {

    public NbtCodecOps(PacketWrapper<?> packet) {
        super(packet);
    }

    @Override
    public CodecResult<Number> getNumberValue(NBT input) {
        if (input instanceof NBTNumber) {
            Number num = ((NBTNumber) input).getAsNumber();
            return new CodecResult<>(num);
        }
        return new CodecResult<>(() -> "Not a number");
    }

    @Override
    public CodecResult<String> getStringValue(NBT input) {
        if (input instanceof NBTString) {
            String str = ((NBTString) input).getValue();
            return new CodecResult<>(str);
        }
        return new CodecResult<>(() -> "Not a string");
    }

    @Override
    public CodecResult<Map<String, NBT>> getMap(NBT input) {
        if (!(input instanceof NBTCompound)) {
            return new CodecResult<>(() -> "Not a map: " + input);
        }
        return new CodecResult<>(((NBTCompound) input).getTags());
    }

    @SuppressWarnings("unchecked") // because of cast from (? extends NBT) -> (NBT)
    @Override
    public CodecResult<List<NBT>> getList(NBT input) {
        if (input instanceof NBTCollection) {
            List<? extends NBT> tags = ((NBTCollection) input).getTags();
            return new CodecResult<>((List<NBT>) tags);
        }
        return new CodecResult<>(() -> "Not a list: " + input);
    }

    @Override
    public NBT createEmpty() {
        return NBTEnd.INSTANCE;
    }

    @Override
    public NBT createEmptyMap() {
        return new NBTCompound(0);
    }

    @Override
    public NBT createEmptyList() {
        return new NBTList<>(NBTType.END, 0);
    }

    @Override
    public NBT createNumber(Number number) {
        return new NBTDouble(number.doubleValue());
    }

    @Override
    public NBT createByte(byte number) {
        return new NBTByte(number);
    }

    @Override
    public NBT createShort(short number) {
        return new NBTShort(number);
    }

    @Override
    public NBT createInt(int number) {
        return new NBTInt(number);
    }

    @Override
    public NBT createLong(long number) {
        return new NBTLong(number);
    }

    @Override
    public NBT createFloat(float number) {
        return new NBTFloat(number);
    }

    @Override
    public NBT createDouble(double number) {
        return new NBTDouble(number);
    }

    @Override
    public NBT createBoolean(boolean value) {
        return new NBTByte(value);
    }

    @Override
    public NBT createString(String value) {
        return new NBTString(value);
    }

    @Override
    public NBT createMap(Map<String, NBT> values) {
        return new NBTCompound(values);
    }

    @Override
    public NBT createList(List<NBT> values) {
        if (this.packet.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
            return NBTListUtil.constructList(values);
        }
        return NBTListUtil.constructListOrArray(values);
    }

    @Override
    public NBT createByteList(byte[] values) {
        return new NBTByteArray(values);
    }

    @Override
    public NBT createIntList(int[] values) {
        return new NBTIntArray(values);
    }

    @Override
    public NBT getLongList(long[] values) {
        return new NBTLongArray(values);
    }
}
