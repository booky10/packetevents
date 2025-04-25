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

import com.github.retrooper.packetevents.util.Crc32CHasher;
import org.jspecify.annotations.NullMarked;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@NullMarked
public class HashCodecOps extends CodecOps<Integer> {

    private static final byte TAG_EMPTY = 1;
    private static final byte TAG_MAP_START = 2;
    private static final byte TAG_MAP_END = 3;
    private static final byte TAG_LIST_START = 4;
    private static final byte TAG_LIST_END = 5;
    private static final byte TAG_BYTE = 6;
    private static final byte TAG_SHORT = 7;
    private static final byte TAG_INT = 8;
    private static final byte TAG_LONG = 9;
    private static final byte TAG_FLOAT = 10;
    private static final byte TAG_DOUBLE = 11;
    private static final byte TAG_STRING = 12;
    private static final byte TAG_BOOLEAN = 13;
    private static final byte TAG_BYTE_ARRAY_START = 14;
    private static final byte TAG_BYTE_ARRAY_END = 15;
    private static final byte TAG_INT_ARRAY_START = 16;
    private static final byte TAG_INT_ARRAY_END = 17;
    private static final byte TAG_LONG_ARRAY_START = 18;
    private static final byte TAG_LONG_ARRAY_END = 19;

    private static final int INITIAL_CRC = 0;
    private static final int EMPTY_HASH = Crc32CHasher.update(INITIAL_CRC, TAG_EMPTY);
    private static final int FALSE_HASH = Crc32CHasher.update(INITIAL_CRC, TAG_BOOLEAN, (byte) 0);
    private static final int TRUE_HASH = Crc32CHasher.update(INITIAL_CRC, TAG_BOOLEAN, (byte) 1);
    private static final int EMPTY_MAP_HASH = Crc32CHasher.update(INITIAL_CRC, TAG_MAP_START, TAG_MAP_END);
    private static final int EMPTY_LIST_HASH = Crc32CHasher.update(INITIAL_CRC, TAG_LIST_START, TAG_LIST_END);

    private static final Comparator<Integer> HASH_COMPARATOR = Comparator.comparingLong(i -> i & 0xFFFFFFFFL);
    private static final Comparator<Map.Entry<Integer, Integer>> MAP_ENTRY_ORDER = Map.Entry
            .<Integer, Integer>comparingByKey(HASH_COMPARATOR)
            .thenComparing(Map.Entry::getValue, HASH_COMPARATOR);

    @Override
    public CodecResult<Number> getNumberValue(Integer input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CodecResult<String> getStringValue(Integer input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CodecResult<Map<String, Integer>> getMap(Integer input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CodecResult<List<Integer>> getList(Integer input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer createEmpty() {
        return EMPTY_HASH;
    }

    @Override
    public Integer createEmptyMap() {
        return EMPTY_MAP_HASH;
    }

    @Override
    public Integer createEmptyList() {
        return EMPTY_LIST_HASH;
    }

    @Override
    public Integer createNumber(Number number) {
        if (number instanceof Byte) {
            return this.createByte(number.byteValue());
        } else if (number instanceof Short) {
            return this.createShort(number.shortValue());
        } else if (number instanceof Integer) {
            return this.createInt(number.intValue());
        } else if (number instanceof Long) {
            return this.createLong(number.longValue());
        } else if (number instanceof Float) {
            return this.createFloat(number.floatValue());
        } else {
            return this.createDouble(number.doubleValue());
        }
    }

    @Override
    public Integer createByte(byte number) {
        return Crc32CHasher.update(INITIAL_CRC, TAG_BYTE, number);
    }

    @Override
    public Integer createShort(short number) {
        int crc = Crc32CHasher.update(INITIAL_CRC, TAG_SHORT);
        return Crc32CHasher.updateShort(crc, number);
    }

    @Override
    public Integer createInt(int number) {
        int crc = Crc32CHasher.update(INITIAL_CRC, TAG_INT);
        return Crc32CHasher.updateInt(crc, number);
    }

    @Override
    public Integer createLong(long number) {
        int crc = Crc32CHasher.update(INITIAL_CRC, TAG_LONG);
        return Crc32CHasher.updateLong(crc, number);
    }

    @Override
    public Integer createFloat(float number) {
        int crc = Crc32CHasher.update(INITIAL_CRC, TAG_FLOAT);
        return Crc32CHasher.updateFloat(crc, number);
    }

    @Override
    public Integer createDouble(double number) {
        int crc = Crc32CHasher.update(INITIAL_CRC, TAG_DOUBLE);
        return Crc32CHasher.updateDouble(crc, number);
    }

    @Override
    public Integer createBoolean(boolean value) {
        return value ? TRUE_HASH : FALSE_HASH;
    }

    @Override
    public Integer createString(String value) {
        return 0; // TODO
    }

    @Override
    public Integer createMap(Map<String, Integer> values) {
        int crc = Crc32CHasher.update(INITIAL_CRC, TAG_MAP_START);
        List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(values.size());
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            Integer stringCrc = this.createString(entry.getKey());
            entries.add(new AbstractMap.SimpleEntry<>(stringCrc, entry.getValue()));
        }
        entries.sort(MAP_ENTRY_ORDER);
        for (Map.Entry<Integer, Integer> entry : entries) {
            crc = Crc32CHasher.updateInt(crc, entry.getKey());
            crc = Crc32CHasher.updateInt(crc, entry.getValue());
        }
        return Crc32CHasher.update(crc, TAG_MAP_END);
    }

    @Override
    public Integer createList(List<Integer> values) {
        return 0; // TODO
    }
}
