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

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Codec<V> extends CodecReader<V>, CodecWriter<V> {

    Codec<Integer> INTEGER = new Codec<Integer>() {
        @Override
        public <T> Integer read(CodecOps<T> ops, T input) {
            return ops.getNumberValue(input).getResultOrThrow().intValue();
        }

        @Override
        public <T> T write(CodecOps<T> ops, Integer input, T prefix) {
            return ops.createInt(input);
        }
    };

    static <V> Codec<V> codec(CodecReader<V> reader, CodecWriter<V> writer) {
        return new Codec<V>() {
            @Override
            public <T> V read(CodecOps<T> ops, T input) {
                return reader.read(ops, input);
            }

            @Override
            public <T> T write(CodecOps<T> ops, V input, T prefix) {
                return writer.write(ops, input, prefix);
            }
        };
    }
}
