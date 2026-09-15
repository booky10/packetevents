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

package com.github.retrooper.packetevents.test;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.component.builtin.item.PotDecorations;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.vector.vecdelta.SteppedVecDelta;
import com.github.retrooper.packetevents.test.base.BaseDummyAPITest;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Minecraft26_3Test extends BaseDummyAPITest {
    @Test
    public void minecraftPacketFixturesRoundTrip() throws Exception {
        User user = new User(null, ConnectionState.PLAY, ClientVersion.V_26_3,
                new UserProfile(new UUID(0, 1), "CodecProbe"));
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/26.3-packets.tsv"), StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                String[] fields = line.split("\t");
                byte[] expected = Base64.getDecoder().decode(fields[2]);
                PacketType.Play.Server type = PacketType.Play.Server.valueOf(fields[1]);
                ByteBuf input = Unpooled.wrappedBuffer(expected);
                ByteBuf output = Unpooled.buffer();
                try {
                    PacketWrapper<?> wrapper = type.getWrapperClass().getConstructor(PacketSendEvent.class)
                            .newInstance(new TestSendEvent(type, input, user));
                    assertEquals(0, input.readableBytes(), fields[0]);
                    wrapper.setBuffer(output);
                    wrapper.write();
                    assertArrayEquals(expected, ByteBufUtil.getBytes(output), fields[0]);
                    count++;
                } finally {
                    input.release();
                    output.release();
                }
            }
        }
        assertTrue(count >= 12);
    }

    @Test
    public void potDecorationEqualitySurvivesTemplateEncoding() {
        PotDecorations original = new PotDecorations(ItemTypes.ANGLER_POTTERY_SHERD, null, ItemTypes.BRICK, null);
        ByteBuf buffer = Unpooled.buffer();
        try {
            PacketWrapper<?> wrapper = PacketWrapper.createUniversalPacketWrapper(buffer, ServerVersion.V_26_3);
            PotDecorations.write(wrapper, original);
            PotDecorations decoded = PotDecorations.read(wrapper);
            assertEquals(0, buffer.readableBytes());
            assertEquals(original, decoded);
            assertEquals(original.hashCode(), decoded.hashCode());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void legacyMovementSettersUpdate26_3Delta() {
        WrapperPlayServerEntityRelativeMove move = new WrapperPlayServerEntityRelativeMove(1, 1, 2, 3, true);
        move.getDelta();
        move.setDeltaX(4);
        move.setDeltaY(5);
        move.setDeltaZ(6);
        assertEquals(new Vector3d(4, 5, 6), move.getDelta().apply(Vector3d.zero()));

        WrapperPlayServerEntityRelativeMoveAndRotation rotated =
                new WrapperPlayServerEntityRelativeMoveAndRotation(1, 1, 2, 3, 0, 0, true);
        rotated.getDelta();
        rotated.setDeltaX(4);
        rotated.setDeltaY(5);
        rotated.setDeltaZ(6);
        assertEquals(new Vector3d(4, 5, 6), rotated.getDelta().apply(Vector3d.zero()));
    }

    @Test
    public void malformedMovementStepCountsAreRejectedBeforeAllocation() {
        ByteBuf input = Unpooled.buffer();
        try {
            PacketWrapper<?> wrapper = PacketWrapper.createUniversalPacketWrapper(input, ServerVersion.V_26_3);
            assertThrows(IllegalArgumentException.class, () -> SteppedVecDelta.read(wrapper, Integer.MAX_VALUE));
            assertThrows(IllegalArgumentException.class, () -> SteppedVecDelta.read(wrapper, -1));
            input.writeZero(6);
            assertThrows(IllegalArgumentException.class, () -> SteppedVecDelta.read(wrapper, 1));
        } finally {
            input.release();
        }
    }

    @Test
    public void protocol777ResolvesTo26_3() {
        assertEquals(ClientVersion.V_26_3, ClientVersion.getById(777));
        assertEquals(ServerVersion.V_26_3, ClientVersion.V_26_3.toServerVersion());
        assertEquals(ClientVersion.V_26_3, ServerVersion.V_26_3.toClientVersion());
    }

    private static final class TestSendEvent extends PacketSendEvent {
        TestSendEvent(PacketType.Play.Server type, ByteBuf buffer, User user) {
            super(type.getId(ClientVersion.V_26_3), type, ServerVersion.V_26_3, null, user, null, buffer);
        }
    }
}
