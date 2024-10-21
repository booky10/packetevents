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

package com.github.retrooper.packetevents.manager.protocol;

import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
public abstract class SimpleProtocolManager implements ProtocolManager {

    public final Map<UUID, Object> channels = new ConcurrentHashMap<>(); // user -> channel
    public final Map<Object, User> users = new ConcurrentHashMap<>(); // pipeline -> user

    protected final PacketEventsAPI<?> api;

    public SimpleProtocolManager(PacketEventsAPI<?> api) {
        this.api = api;
    }

    @Override
    public Collection<User> getUsers() {
        return this.users.values();
    }

    @Override
    public Collection<Object> getChannels() {
        return this.channels.values();
    }

    @Override
    public User getUser(Object channel) {
        return this.users.get(this.api.getNettyManager().getChannelOperator().getPipeline(channel));
    }

    @Override
    public User removeUser(Object channel) {
        return this.users.remove(this.api.getNettyManager().getChannelOperator().getPipeline(channel));
    }

    @Override
    public void setUser(Object channel, User user) {
        this.users.put(this.api.getNettyManager().getChannelOperator().getPipeline(channel), user);
        this.api.getInjector().updateUser(channel, user);
    }

    @Override
    public @Nullable Object getChannel(UUID playerId) {
        return this.channels.get(playerId);
    }
}
