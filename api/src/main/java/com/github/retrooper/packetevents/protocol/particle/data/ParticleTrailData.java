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

package com.github.retrooper.packetevents.protocol.particle.data;

import com.github.retrooper.packetevents.protocol.color.Color;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class ParticleTrailData extends ParticleData {

    private Vector3d target;
    private Color color;

    public ParticleTrailData(Vector3d target, Color color) {
        this.target = target;
        this.color = color;
    }

    public static ParticleTrailData read(PacketWrapper<?> wrapper) {
        Vector3d target = Vector3d.read(wrapper);
        Color color = new Color(wrapper.readInt());
        return new ParticleTrailData(target, color);
    }

    public static void write(PacketWrapper<?> wrapper, ParticleTrailData data) {
        Vector3d.write(wrapper, data.target);
        wrapper.writeInt(data.color.asRGB());
    }

    public static ParticleTrailData decode(NBTCompound compound, ClientVersion version) {
        Vector3d target = Vector3d.decode(compound.getTagOrThrow("target"), version);
        Color color = Color.decode(compound.getTagOrThrow("color"), version);
        return new ParticleTrailData(target, color);
    }

    public static void encode(ParticleTrailData data, ClientVersion version, NBTCompound compound) {
        compound.setTag("target", Vector3d.encode(data.target, version));
        compound.setTag("color", Color.encode(data.color, version));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public Vector3d getTarget() {
        return this.target;
    }

    public void setTarget(Vector3d target) {
        this.target = target;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
