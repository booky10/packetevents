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

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import java.util.Objects;

/**
 * @versions 26.3+
 */
public final class ItemMobVisibility {
    private final MappedEntitySet<EntityType> targetingEntityTypes;
    private final float visibility;

    public ItemMobVisibility(MappedEntitySet<EntityType> targetingEntityTypes, float visibility) {
        this.targetingEntityTypes = Objects.requireNonNull(targetingEntityTypes);
        this.visibility = visibility;
    }

    public static ItemMobVisibility read(PacketWrapper<?> wrapper) {
        return new ItemMobVisibility(MappedEntitySet.read(wrapper, EntityTypes.getRegistry()), wrapper.readFloat());
    }

    public static void write(PacketWrapper<?> wrapper, ItemMobVisibility value) {
        MappedEntitySet.write(wrapper, value.targetingEntityTypes);
        wrapper.writeFloat(value.visibility);
    }

    public MappedEntitySet<EntityType> getTargetingEntityTypes() {
        return this.targetingEntityTypes;
    }
    public float getVisibility() {
        return this.visibility;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemMobVisibility)) return false;
        ItemMobVisibility other = (ItemMobVisibility) obj;
        return this.targetingEntityTypes.equals(other.targetingEntityTypes) && Float.compare(this.visibility, other.visibility) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.targetingEntityTypes, this.visibility);
    }
}
