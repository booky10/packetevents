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

package io.github.retrooper.packetevents.sponge.manager.server;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.PEVersion;
import org.spongepowered.api.Sponge;
import org.spongepowered.plugin.PluginContainer;

import java.util.regex.Pattern;

public class ServerManagerImpl implements ServerManager {

    private static final Pattern VERSION_REGEX = Pattern.compile("^\\d+\\.\\d+(?:\\.\\d+)?$");

    private ServerVersion serverVersion;

    private ServerVersion resolveVersionNoCache() {
        PluginContainer plugin = (PluginContainer) PacketEvents.getAPI().getPlugin();
        String minecraftRelease = Sponge.platform().minecraftVersion().name();
        ServerVersion fallbackVersion = ServerVersion.getLatest();

        if (!VERSION_REGEX.matcher(minecraftRelease).matches()) {
            plugin.logger().warn("[packetevents] Don't know how to handle Minecraft Version {}, will use latest available version {} instead", minecraftRelease, fallbackVersion);
            return fallbackVersion;
        }

        // Our PEVersion class can parse this version and detect if it is a newer version than what is currently supported
        // and account for that properly
        PEVersion version = PEVersion.fromString(minecraftRelease);
        PEVersion latestVersion = PEVersion.fromString(fallbackVersion.getReleaseName());
        if (version.isNewerThan(latestVersion)) {
            //We do not support this version yet, so let us warn the user
            plugin.logger().warn("[packetevents] We currently do not support the minecraft version {}," +
                    " so things might break. " +
                    "PacketEvents will behave as if the minecraft version were {}!", version, fallbackVersion);
            return fallbackVersion;
        }

        for (final ServerVersion val : ServerVersion.reversedValues()) {
            // For example "V_1_18" -> "1.18"
            if (minecraftRelease.contains(val.getReleaseName())) {
                return val;
            }
        }

        plugin.logger().warn("[packetevents] Your server software is preventing us from checking the server version. This is what we found: {}. We will assume the server version is {}...", minecraftRelease, fallbackVersion.name());
        return fallbackVersion;
    }

    @Override
    public ServerVersion getVersion() {
        if (serverVersion == null) {
            serverVersion = resolveVersionNoCache();
        }
        return serverVersion;
    }
}
