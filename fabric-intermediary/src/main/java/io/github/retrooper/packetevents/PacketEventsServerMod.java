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

package io.github.retrooper.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.util.reflection.Reflection;
import io.github.retrooper.packetevents.factory.fabric.FabricPacketEventsAPI;
import io.github.retrooper.packetevents.factory.fabric.FabricPlayerManager;
import io.github.retrooper.packetevents.factory.fabric.FabricServerManager;
import io.github.retrooper.packetevents.impl.netty.manager.player.PlayerManagerAbstract;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Method;

@NullMarked
public class PacketEventsServerMod implements PreLaunchEntrypoint {

    private static String getVersionId() {
        SharedConstants.tryDetectVersion();
        // legacy
        Method getIdMethod = Reflection.getMethod(WorldVersion.class, "method_48018");
        if (getIdMethod != null) {
            try {
                return (String) getIdMethod.invoke(SharedConstants.getCurrentVersion());
            } catch (ReflectiveOperationException exception) {
                throw new RuntimeException("Failed to get version id", exception);
            }
        }
        // modern
        return SharedConstants.getCurrentVersion().id();
    }

    public static FabricPacketEventsAPI constructApi(String modid) {
        return new FabricPacketEventsAPI(modid, EnvType.SERVER) {
            @Override
            protected ServerManager constructServerManager() {
                return new FabricServerManager(getVersionId());
            }

            @Override
            protected PlayerManagerAbstract constructPlayerManager() {
                return new FabricPlayerManager();
            }
        };
    }

    @Override
    public void onPreLaunch() {
        PacketEvents.setAPI(constructApi(PacketEventsMod.MOD_ID));
        PacketEvents.getAPI().load();
    }
}
