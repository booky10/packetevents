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

package com.github.retrooper.packetevents.protocol.world.attributes.modifiers;

import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.world.attributes.EnvironmentAttribute;
import com.github.retrooper.packetevents.protocol.world.biome.BiomeEffects.ParticleSettings;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

/**
 * @versions 26.3+
 */
@NullMarked
@FunctionalInterface
public interface ParticleModifier extends AttributeModifier<List<ParticleSettings>, List<ParticleSettings>> {
    ParticleModifier APPEND = (value, argument) -> {
        List<ParticleSettings> result = new ArrayList<>(value.size() + argument.size());
        result.addAll(value);
        result.addAll(argument);
        return result;
    };

    @Override
    default NbtCodec<List<ParticleSettings>> argumentCodec(EnvironmentAttribute<List<ParticleSettings>> attribute) {
        return ParticleSettings.CODEC.applyList();
    }
}
