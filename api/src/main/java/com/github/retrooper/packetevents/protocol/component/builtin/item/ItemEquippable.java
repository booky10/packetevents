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

package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ItemEquippable {

    private EquipmentSlot slot;
    private Sound equipSound;
    private @Nullable ResourceLocation model;
    private @Nullable ResourceLocation cameraOverlay;
    private @Nullable MappedEntitySet<EntityType> allowedEntities;
    private boolean dispensable;
    private boolean swappable;
    private boolean damageOnHurt;

    public ItemEquippable(
            EquipmentSlot slot,
            Sound equipSound,
            @Nullable ResourceLocation model,
            @Nullable ResourceLocation cameraOverlay,
            @Nullable MappedEntitySet<EntityType> allowedEntities,
            boolean dispensable,
            boolean swappable,
            boolean damageOnHurt
    ) {
        this.slot = slot;
        this.equipSound = equipSound;
        this.model = model;
        this.cameraOverlay = cameraOverlay;
        this.allowedEntities = allowedEntities;
        this.dispensable = dispensable;
        this.swappable = swappable;
        this.damageOnHurt = damageOnHurt;
    }

    public static ItemEquippable read(PacketWrapper<?> wrapper) {
        EquipmentSlot slot = wrapper.readEnum(EquipmentSlot.values());
        Sound equipSound = Sound.read(wrapper);
        ResourceLocation model = wrapper.readOptional(PacketWrapper::readIdentifier);
        ResourceLocation cameraOverlay = wrapper.readOptional(PacketWrapper::readIdentifier);
        MappedEntitySet<EntityType> allowedEntities = wrapper.readOptional(
                ew -> MappedEntitySet.read(ew, EntityTypes::getById));
        boolean dispensable = wrapper.readBoolean();
        boolean swappable = wrapper.readBoolean();
        boolean damageOnHurt = wrapper.readBoolean();
        return new ItemEquippable(slot, equipSound, model,
                cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt);
    }

    public static void write(PacketWrapper<?> wrapper, ItemEquippable equippable) {
        wrapper.writeEnum(equippable.slot);
        Sound.write(wrapper, equippable.equipSound);
        wrapper.writeOptional(equippable.model, PacketWrapper::writeIdentifier);
        wrapper.writeOptional(equippable.cameraOverlay, PacketWrapper::writeIdentifier);
        wrapper.writeOptional(equippable.allowedEntities, MappedEntitySet::write);
        wrapper.writeBoolean(equippable.dispensable);
        wrapper.writeBoolean(equippable.swappable);
        wrapper.writeBoolean(equippable.damageOnHurt);
    }

    public EquipmentSlot getSlot() {
        return this.slot;
    }

    public void setSlot(EquipmentSlot slot) {
        this.slot = slot;
    }

    public Sound getEquipSound() {
        return this.equipSound;
    }

    public void setEquipSound(Sound equipSound) {
        this.equipSound = equipSound;
    }

    public @Nullable ResourceLocation getModel() {
        return this.model;
    }

    public void setModel(@Nullable ResourceLocation model) {
        this.model = model;
    }

    public @Nullable ResourceLocation getCameraOverlay() {
        return this.cameraOverlay;
    }

    public void setCameraOverlay(@Nullable ResourceLocation cameraOverlay) {
        this.cameraOverlay = cameraOverlay;
    }

    public @Nullable MappedEntitySet<EntityType> getAllowedEntities() {
        return this.allowedEntities;
    }

    public void setAllowedEntities(@Nullable MappedEntitySet<EntityType> allowedEntities) {
        this.allowedEntities = allowedEntities;
    }

    public boolean isDispensable() {
        return this.dispensable;
    }

    public void setDispensable(boolean dispensable) {
        this.dispensable = dispensable;
    }

    public boolean isSwappable() {
        return this.swappable;
    }

    public void setSwappable(boolean swappable) {
        this.swappable = swappable;
    }

    public boolean isDamageOnHurt() {
        return this.damageOnHurt;
    }

    public void setDamageOnHurt(boolean damageOnHurt) {
        this.damageOnHurt = damageOnHurt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ItemEquippable)) return false;
        ItemEquippable that = (ItemEquippable) obj;
        if (this.dispensable != that.dispensable) return false;
        if (this.swappable != that.swappable) return false;
        if (this.damageOnHurt != that.damageOnHurt) return false;
        if (this.slot != that.slot) return false;
        if (!Objects.equals(this.equipSound, that.equipSound)) return false;
        if (!Objects.equals(this.model, that.model)) return false;
        if (!Objects.equals(this.cameraOverlay, that.cameraOverlay)) return false;
        return Objects.equals(this.allowedEntities, that.allowedEntities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.slot, this.equipSound, this.model, this.cameraOverlay, this.allowedEntities, this.dispensable, this.swappable, this.damageOnHurt);
    }

    @Override
    public String toString() {
        return "ItemEquippable{slot=" + this.slot + ", equipSound=" + this.equipSound + ", model=" + this.model + ", cameraOverlay=" + this.cameraOverlay + ", allowedEntities=" + this.allowedEntities + ", dispensable=" + this.dispensable + ", swappable=" + this.swappable + ", damageOnHurt=" + this.damageOnHurt + '}';
    }
}
