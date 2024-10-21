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

package io.github.retrooper.packetevents.mixin;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.PacketEventsServerMod;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {

    @Inject(method = "initServer", at = @At("HEAD"))
    private void preServerInit(CallbackInfoReturnable<Boolean> cir) {
        PacketEvents.setServerAPI(PacketEventsServerMod.constructApi("packetevents"));
        PacketEvents.getServerAPI().load();
    }

    @Inject(method = "initServer", at = @At("RETURN"))
    private void postServerInit(CallbackInfoReturnable<Boolean> cir) {
        PacketEvents.getServerAPI().init();
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    private void postServerStop(CallbackInfo ci) {
        PacketEvents.getServerAPI().terminate();
    }
}
