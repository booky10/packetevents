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
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.UserConnectEvent;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.PacketEventsImplHelper;
import io.github.retrooper.packetevents.handler.PacketDecoder;
import io.github.retrooper.packetevents.handler.PacketEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import net.minecraft.SharedConstants;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.network.Connection.class)
public class ConnectionMixin {

    // doesn't account for mods like ViaFabric
    @Unique
    private static final ClientVersion CLIENT_VERSION =
            ClientVersion.getById(SharedConstants.getProtocolVersion());

    @Inject(method = "configureSerialization", at = @At("TAIL"))
    private static void configureSerialization(
            ChannelPipeline pipeline, PacketFlow flow, boolean memoryOnly,
            BandwidthDebugMonitor bandwithDebugMonitor, CallbackInfo ci
    ) {
        PacketEventsAPI<?> api = switch (flow) {
            case CLIENTBOUND -> PacketEvents.getClientAPI();
            case SERVERBOUND -> PacketEvents.getServerAPI();
        };
        api.getLogManager().debug("Game connected on " + flow);

        Channel channel = pipeline.channel();
        User user = new User(channel, ConnectionState.HANDSHAKING,
                CLIENT_VERSION, new UserProfile(null, null));
        ProtocolManager.USERS.put(channel, user);

        UserConnectEvent connectEvent = new UserConnectEvent(user);
        api.getEventManager().callEvent(connectEvent);
        if (connectEvent.isCancelled()) {
            channel.unsafe().closeForcibly();
            return;
        }

        PacketSide side = flow == PacketFlow.CLIENTBOUND ? PacketSide.CLIENT : PacketSide.SERVER;
        channel.pipeline().addAfter("splitter", PacketEvents.DECODER_NAME,
                new PacketDecoder(api, side, user));
        channel.pipeline().addAfter("prepender", PacketEvents.ENCODER_NAME,
                new PacketEncoder(api, side, user));
        channel.closeFuture().addListener((ChannelFutureListener) future ->
                PacketEventsImplHelper.handleDisconnection(user.getChannel(), user.getUUID()));
    }
}
