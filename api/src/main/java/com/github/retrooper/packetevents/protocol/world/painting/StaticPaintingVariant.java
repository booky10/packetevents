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

package com.github.retrooper.packetevents.protocol.world.painting;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;

public class StaticPaintingVariant extends AbstractMappedEntity implements PaintingVariant {

    private final int width;
    private final int height;
    private final ResourceLocation assetId;

    public StaticPaintingVariant(int width, int height, ResourceLocation assetId) {
        this(null, width, height, assetId);
    }

    public StaticPaintingVariant(@Nullable TypesBuilderData data, int width, int height, ResourceLocation assetId) {
        super(data);
        this.width = width;
        this.height = height;
        this.assetId = assetId;
    }

    @Override
    public PaintingVariant copy(@Nullable TypesBuilderData newData) {
        return new StaticPaintingVariant(newData, this.width, this.height, this.assetId);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public ResourceLocation getAssetId() {
        return this.assetId;
    }
}
