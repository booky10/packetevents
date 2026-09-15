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

package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

/**
 * @versions 26.3+
 */
public class WrapperPlayServerAddTransientBlock extends PacketWrapper<WrapperPlayServerAddTransientBlock> {
    private Vector3i blockPosition;
    private int blockId;

    public WrapperPlayServerAddTransientBlock(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerAddTransientBlock(Vector3i blockPosition, int blockId) {
        super(PacketType.Play.Server.ADD_TRANSIENT_BLOCK);
        this.blockPosition = blockPosition;
        this.blockId = blockId;
    }

    @Override
    public void read() {
        this.blockPosition = readBlockPosition();
        this.blockId = readVarInt();
    }

    @Override
    public void write() {
        writeBlockPosition(this.blockPosition);
        writeVarInt(this.blockId);
    }

    @Override
    public void copy(WrapperPlayServerAddTransientBlock wrapper) {
        this.blockPosition = wrapper.blockPosition;
        this.blockId = wrapper.blockId;
    }

    public Vector3i getBlockPosition() {
        return this.blockPosition;
    }
    public void setBlockPosition(Vector3i position) {
        this.blockPosition = position;
    }
    public int getBlockId() {
        return this.blockId;
    }
    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }
    public WrappedBlockState getBlockState() {
        return WrappedBlockState.getByGlobalId(this.serverVersion.toClientVersion(), this.blockId);
    }
    public void setBlockState(WrappedBlockState state) {
        this.blockId = state.getGlobalId();
    }
}
