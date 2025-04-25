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

package com.github.retrooper.packetevents.protocol.nbt;

import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NullMarked
public interface NBTCollection {

    default List<? extends NBT> getTags() {
        int len = this.size();
        List<NBT> tags = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            tags.add(this.getTag(i));
        }
        return Collections.unmodifiableList(tags);
    }

    NBT getTag(int index);

    int size();

    default boolean isEmpty() {
        return this.size() == 0;
    }
}
