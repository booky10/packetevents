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

package com.github.retrooper.packetevents.test;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.codecs.Codec;
import com.github.retrooper.packetevents.util.codecs.CodecOps;
import com.github.retrooper.packetevents.util.codecs.HashCodecOps;
import com.github.retrooper.packetevents.util.codecs.NbtCodecOps;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public class CodecTest {

    @Test
    public void testHashCodec() {
        // TODO confirm with vanilla
        HashCodecOps ops = new HashCodecOps(null);
        Integer crc = ExampleClass.CODEC.write(ops, ExampleClass.EXAMPLE);
        System.err.println(crc);
    }

    @Test
    public void testNbtCodec() {
        // TODO confirm with vanilla
        NbtCodecOps ops1214 = new NbtCodecOps(PacketWrapper.createUniversalPacketWrapper(null, ServerVersion.V_1_21_4));
        System.out.println(ExampleClass.CODEC.write(ops1214, ExampleClass.EXAMPLE));
        NbtCodecOps ops1215 = new NbtCodecOps(PacketWrapper.createUniversalPacketWrapper(null, ServerVersion.V_1_21_5));
        System.out.println(ExampleClass.CODEC.write(ops1215, ExampleClass.EXAMPLE));
    }

    public static final class ExampleClass {

        private static final Codec<ExampleClass> CODEC = Codec.codec(ExampleClass::read, ExampleClass::write);

        private static final ExampleClass EXAMPLE = new ExampleClass(
                "hello world", 0, (short) 42,
                Collections.singletonList(new ExampleClass(
                        "...", 42, (short) 1337,
                        Collections.emptyList(), Arrays.asList(42, 42, 42, 42, 42))),
                Arrays.asList(1234, 1234, 4321)
        );

        private final String name;
        private final int count;
        private final short stuff;
        private final List<ExampleClass> childs;
        private final List<Integer> numbers;

        public ExampleClass(String name, int count, short stuff, List<ExampleClass> childs, List<Integer> numbers) {
            this.name = name;
            this.count = count;
            this.stuff = stuff;
            this.childs = childs;
            this.numbers = numbers;
        }

        public static <T> ExampleClass read(CodecOps<T> ops, T input) {
            Map<String, T> map = ops.getMap(input).getResultOrThrow();
            String name = ops.getStringValue(map.get("name")).getResultOrThrow();
            int count = ops.getNumberValueOr(map.get("count"), 0).intValue();
            short stuff = ops.getNumberValue(map.get("stuff")).getResultOrThrow().shortValue();
            List<ExampleClass> childs = ops.getList(map.get("childs"), CODEC).getResultOrThrow();
            List<Integer> numbers = ops.getList(map.get("numbers"), Codec.INTEGER).getResultOrThrow();
            return new ExampleClass(name, count, stuff, childs, numbers);
        }

        public static <T> T write(CodecOps<T> ops, ExampleClass example, T prefix) {
            Map<String, T> map = new LinkedHashMap<>();
            map.put("name", ops.createString(example.name));
            if (example.count != 0) {
                map.put("count", ops.createInt(example.count));
            }
            map.put("stuff", ops.createShort(example.stuff));
            map.put("childs", ops.createList(example.childs, CODEC));
            map.put("numbers", ops.createList(example.numbers, Codec.INTEGER));
            return ops.createMap(map);
        }
    }
}
