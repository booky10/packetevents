/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
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

package com.github.retrooper.packetevents;

import com.github.retrooper.packetevents.protocol.PacketSide;
import org.jetbrains.annotations.ApiStatus;

public final class PacketEvents {

    private static PacketEventsAPI<?> CLIENT_API;
    private static PacketEventsAPI<?> SERVER_API;

    // used for injectors
    @ApiStatus.Internal
    public static String IDENTIFIER;
    @ApiStatus.Internal
    public static String ENCODER_NAME;
    @ApiStatus.Internal
    public static String DECODER_NAME;
    @ApiStatus.Internal
    public static String CONNECTION_HANDLER_NAME;
    @ApiStatus.Internal
    public static String SERVER_CHANNEL_HANDLER_NAME;
    @ApiStatus.Internal
    public static String TIMEOUT_HANDLER_NAME;

    private PacketEvents() {
    }

    // getters

    public static PacketEventsAPI<?> getServerAPI() {
        return SERVER_API;
    }

    public static PacketEventsAPI<?> getClientAPI() {
        return CLIENT_API;
    }

    public static PacketEventsAPI<?> getAPI(PacketSide side) {
        return side == PacketSide.SERVER ? getServerAPI() : getClientAPI();
    }

    public static PacketEventsAPI<?> getAPI() {
        return getServerAPI();
    }

    // setters

    public static void setServerAPI(PacketEventsAPI<?> api) {
        if (!api.getInjector().isServerBound()) {
            throw new IllegalStateException("Can't set non-server api as server api");
        }
        SERVER_API = api;
    }

    public static void setClientAPI(PacketEventsAPI<?> api) {
        if (api.getInjector().isServerBound()) {
            throw new IllegalStateException("Can't set non-client api as client api");
        }
        CLIENT_API = api;
    }

    public static void setAPI(PacketSide side, PacketEventsAPI<?> api) {
        if (side == PacketSide.SERVER) {
            setServerAPI(api);
        } else {
            setClientAPI(api);
        }
    }

    public static void setAPI(PacketEventsAPI<?> api) {
        setServerAPI(api);
    }
}
