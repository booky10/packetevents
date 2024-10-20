package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus;

/**
 * Teleporting a player directly with packets will cause
 * issues on most server implementations and is discouraged!
 */
public class WrapperPlayServerPlayerPositionAndLook extends PacketWrapper<WrapperPlayServerPlayerPositionAndLook> {

    private int teleportId;
    private Vector3d position;
    /**
     * Added with 1.21.2
     */
    private Vector3d deltaMovement;
    private float yaw;
    private float pitch;
    private RelativeFlag relativeFlags;

    /**
     * Removed in 1.19.3
     */
    @ApiStatus.Obsolete
    private boolean dismountVehicle = false;

    public WrapperPlayServerPlayerPositionAndLook(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerPlayerPositionAndLook(
            double x, double y, double z, float yaw, float pitch,
            byte flags, int teleportId, boolean dismountVehicle
    ) {
        this(new Vector3d(x, y, z), yaw, pitch, flags, teleportId, dismountVehicle);
    }

    public WrapperPlayServerPlayerPositionAndLook(
            Vector3d position, float yaw, float pitch,
            byte flags, int teleportId, boolean dismountVehicle
    ) {
        this(position, yaw, pitch, flags, teleportId);
        this.dismountVehicle = dismountVehicle;
    }

    public WrapperPlayServerPlayerPositionAndLook(
            Vector3d position, float yaw, float pitch,
            byte flags, int teleportId
    ) {
        this(teleportId, position, Vector3d.zero(), yaw, pitch, flags);
    }

    public WrapperPlayServerPlayerPositionAndLook(
            int teleportId, Vector3d position, Vector3d deltaMovement,
            float yaw, float pitch, byte flags
    ) {
        this(teleportId, position, deltaMovement, yaw, pitch, null);
        this.relativeFlags = new RelativeFlag(flags);
    }

    public WrapperPlayServerPlayerPositionAndLook(
            int teleportId, Vector3d position, Vector3d deltaMovement,
            float yaw, float pitch, RelativeFlag flags
    ) {
        super(PacketType.Play.Server.PLAYER_POSITION_AND_LOOK);
        this.teleportId = teleportId;
        this.position = position;
        this.deltaMovement = deltaMovement;
        this.yaw = yaw;
        this.pitch = pitch;
        this.relativeFlags = flags;
    }

    @Override
    public void read() {
        if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
            this.teleportId = this.readVarInt();
            this.position = Vector3d.read(this);
            this.deltaMovement = Vector3d.read(this);
            this.yaw = this.readFloat();
            this.pitch = this.readFloat();
            this.relativeFlags = new RelativeFlag(this.readInt());
        } else {
            this.position = Vector3d.read(this);
            this.deltaMovement = Vector3d.zero();
            this.yaw = this.readFloat();
            this.pitch = this.readFloat();
            this.relativeFlags = new RelativeFlag(this.readUnsignedByte());
            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
                this.teleportId = this.readVarInt();
                if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17)
                        && this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_19_3)) {
                    this.dismountVehicle = this.readBoolean();
                }
            }
        }
    }

    @Override
    public void write() {
        if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
            this.writeVarInt(this.teleportId);
            Vector3d.write(this, this.position);
            Vector3d.write(this, this.deltaMovement);
            this.writeFloat(this.yaw);
            this.writeFloat(this.pitch);
            this.writeInt(this.relativeFlags.getFullMask());
        } else {
            Vector3d.write(this, this.position);
            this.writeFloat(this.yaw);
            this.writeFloat(this.pitch);
            this.writeByte(this.relativeFlags.getFullMask());
            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
                this.writeVarInt(this.teleportId);
                if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17)
                        && this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_19_3)) {
                    this.writeBoolean(this.dismountVehicle);
                }
            }
        }
    }

    @Override
    public void copy(WrapperPlayServerPlayerPositionAndLook wrapper) {
        this.teleportId = wrapper.teleportId;
        this.position = wrapper.position;
        this.deltaMovement = wrapper.deltaMovement;
        this.yaw = wrapper.yaw;
        this.pitch = wrapper.pitch;
        this.relativeFlags = wrapper.relativeFlags;
        this.dismountVehicle = wrapper.dismountVehicle;
    }

    public int getTeleportId() {
        return this.teleportId;
    }

    public void setTeleportId(int teleportId) {
        this.teleportId = teleportId;
    }

    public Vector3d getPosition() {
        return this.position;
    }

    public void setPosition(Vector3d position) {
        this.position = position;
    }

    public double getX() {
        return this.position.x;
    }

    public void setX(double x) {
        this.position = new Vector3d(x, this.getY(), this.getZ());
    }

    public double getY() {
        return this.position.y;
    }

    public void setY(double y) {
        this.position = new Vector3d(this.getX(), y, this.getZ());
    }

    public double getZ() {
        return this.position.z;
    }

    public void setZ(double z) {
        this.position = new Vector3d(this.getX(), this.getY(), z);
    }

    public Vector3d getDeltaMovement() {
        return this.deltaMovement;
    }

    public void setDeltaMovement(Vector3d deltaMovement) {
        this.deltaMovement = deltaMovement;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * @deprecated The mask no longer fits in a byte since 1.21.2
     */
    @Deprecated
    public byte getRelativeMask() {
        return this.relativeFlags.getMask();
    }

    /**
     * @deprecated The mask no longer fits in a byte since 1.21.2
     */
    @Deprecated
    public void setRelativeMask(byte relativeMask) {
        this.relativeFlags = new RelativeFlag(relativeMask);
    }

    public boolean isRelativeFlag(RelativeFlag flag) {
        return this.relativeFlags.has(flag);
    }

    public void setRelative(RelativeFlag flag, boolean relative) {
        this.relativeFlags = this.relativeFlags.set(flag, relative);
    }

    public RelativeFlag getRelativeFlags() {
        return this.relativeFlags;
    }

    public void setRelativeFlags(RelativeFlag flags) {
        this.relativeFlags = flags;
    }

    /**
     * Removed with 1.19.3
     */
    @ApiStatus.Obsolete
    public boolean isDismountVehicle() {
        return this.dismountVehicle;
    }

    /**
     * Removed with 1.19.3
     */
    @ApiStatus.Obsolete
    public void setDismountVehicle(boolean dismountVehicle) {
        this.dismountVehicle = dismountVehicle;
    }
}
