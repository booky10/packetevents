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

package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.stream.NetStreamInput;
import com.github.retrooper.packetevents.protocol.stream.NetStreamOutput;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.NetworkChunkData;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_16.Chunk_v1_9;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_7.Chunk_v1_7;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_8.Chunk_v1_8;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v_1_18.Chunk_v1_18;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.ChunkReader;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.impl.ChunkReader_v1_16;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.impl.ChunkReader_v1_18;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.impl.ChunkReader_v1_7;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.impl.ChunkReader_v1_8;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.impl.ChunkReader_v1_9;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.BitSet;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class WrapperPlayServerChunkData extends PacketWrapper<WrapperPlayServerChunkData> {
    private static ChunkReader_v1_7 chunkReader_v1_7 = new ChunkReader_v1_7();
    private static ChunkReader_v1_8 chunkReader_v1_8 = new ChunkReader_v1_8();
    private static ChunkReader_v1_9 chunkReader_v1_9 = new ChunkReader_v1_9();
    private static ChunkReader_v1_16 chunkReader_v1_16 = new ChunkReader_v1_16();
    private static ChunkReader_v1_18 chunkReader_v1_18 = new ChunkReader_v1_18();

    private Column column;
    private boolean ignoreOldData; // 1.16-1.16.1
    private boolean trustEdges; // 1.18-1.19.3
    private BitSet blockLightMask; // since 1.18
    private BitSet skyLightMask; // since 1.18
    private BitSet emptyBlockLightMask; // since 1.18
    private BitSet emptySkyLightMask; // since 1.18
    private int skyLightCount; // since 1.18
    private int blockLightCount; // since 1.18
    private byte[][] skyLightArray; // since 1.18
    private byte[][] blockLightArray; // since 1.18

    public WrapperPlayServerChunkData(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerChunkData(Column column) {
        this(column, false);
    }

    public WrapperPlayServerChunkData(Column column, boolean ignoreOldData) {
        this(column, ignoreOldData, true, null,
                null, null, null,
                0, 0, null, null);
    }

    public WrapperPlayServerChunkData(
            Column column, BitSet blockLightMask, BitSet skyLightMask,
            BitSet emptyBlockLightMask, BitSet emptySkyLightMask,
            int skyLightCount, int blockLightCount,
            byte[][] skyLightArray, byte[][] blockLightArray
    ) {
        this(column, false, true, blockLightMask, skyLightMask,
                emptyBlockLightMask, emptySkyLightMask, skyLightCount, blockLightCount,
                skyLightArray, blockLightArray);
    }

    public WrapperPlayServerChunkData(
            Column column, boolean trustEdges,
            BitSet blockLightMask, BitSet skyLightMask,
            BitSet emptyBlockLightMask, BitSet emptySkyLightMask,
            int skyLightCount, int blockLightCount,
            byte[][] skyLightArray, byte[][] blockLightArray
    ) {
        this(column, false, trustEdges, blockLightMask, skyLightMask,
                emptyBlockLightMask, emptySkyLightMask, skyLightCount, blockLightCount,
                skyLightArray, blockLightArray);
    }

    public WrapperPlayServerChunkData(
            Column column, boolean ignoreOldData,
            boolean trustEdges, BitSet blockLightMask, BitSet skyLightMask,
            BitSet emptyBlockLightMask, BitSet emptySkyLightMask,
            int skyLightCount, int blockLightCount,
            byte[][] skyLightArray, byte[][] blockLightArray
    ) {
        super(PacketType.Play.Server.CHUNK_DATA);
        this.column = column;
        this.ignoreOldData = ignoreOldData;
        this.trustEdges = trustEdges;
        this.blockLightMask = blockLightMask;
        this.skyLightMask = skyLightMask;
        this.emptyBlockLightMask = emptyBlockLightMask;
        this.emptySkyLightMask = emptySkyLightMask;
        this.skyLightCount = skyLightCount;
        this.blockLightCount = blockLightCount;
        this.skyLightArray = skyLightArray;
        this.blockLightArray = blockLightArray;
    }

    private long[] readBitSetLongs() {
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17)) {
            //Read primary bit mask
            return readLongArray();
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
            //Read primary bit mask
            return new long[]{readVarInt()};
        } else {
            //Read primary bit mask
            return new long[]{readUnsignedShort()};
        }
    }

    private BitSet readChunkMask() {
        return BitSet.valueOf(readBitSetLongs());
    }

    private void writeChunkMask(BitSet chunkMask) {
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17)) {
            if (chunkMask == null) {
                this.writeVarInt(0);
            } else {
                //Write primary bit mask
                long[] longArray = chunkMask.toLongArray();
                writeLongArray(longArray);
            }
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
            //Write primary bit mask
            writeVarInt((int) chunkMask.toLongArray()[0]);
        } else {
            writeShort((int) chunkMask.toLongArray()[0]);
        }
    }

    @Override
    public void read() {
        int chunkX = readInt();
        int chunkZ = readInt();

        // All chunks are full chunks in 1.17 and above to avoid issues with arbitrary world height
        boolean checkFullChunk = serverVersion.isOlderThan(ServerVersion.V_1_17);
        // Don't read a boolean if there isn't a boolean to be read
        boolean fullChunk = !checkFullChunk || readBoolean();

        if (serverVersion == ServerVersion.V_1_16 || serverVersion == ServerVersion.V_1_16_1) {
            ignoreOldData = readBoolean();
        }

        // There is no bitset on 1.18 and above, instead the SingletonPalette is used to represent a chunk with all air
        BitSet chunkMask = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18) ? null : readChunkMask();
        boolean hasHeightMaps = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14);
        NBTCompound heightMaps = null;
        if (hasHeightMaps) {
            heightMaps = readNBT();
        }

        // 1.7 sends a secondary bit mask for the block metadata
        BitSet secondaryChunkMask = null;
        if (serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
            secondaryChunkMask = readChunkMask();
        }

        int chunkSize = 16;
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17)) {
            chunkSize = user.getTotalWorldHeight() >> 4;
        }

        // 1.7 logic is the same
        // 1.8 logic is the same, however, MCProtocolLib checks for remaining bytes... is this needed?
        // 1.9 logic is the same
        // 1.12 logic is the same
        // 1.14 logic for having biome data - is full chunk
        // 1.16 logic is the same
        // 1.17 logic ALWAYS sends biome data because it is always a full chunk
        // 1.18 logic makes it all a palette type system for biome data...
        boolean hasBiomeData = fullChunk && serverVersion.isOlderThan(ServerVersion.V_1_18);

        boolean bytesInsteadOfInts = serverVersion.isOlderThan(ServerVersion.V_1_13);
        int[] biomeDataInts = null;
        byte[] biomeDataBytes = null;

        // 1.7 sends the chunk data as a byte array of size 256 when it is a full chunk
        // This also applies to 1.8 through 1.12
        //
        // 1.13 uses an integer array of size 256 at the end of chunk data
        // This applies from 1.14
        //
        // 1.16.2+ send a var int array instead of an int array
        if (hasBiomeData && serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16_2)) {
            biomeDataInts = readVarIntArray();
        } else if (hasBiomeData && serverVersion.isNewerThanOrEquals(ServerVersion.V_1_15)) {
            biomeDataInts = new int[1024];
            for (int i = 0; i < biomeDataInts.length; i++) {
                biomeDataInts[i] = readInt();
            }
        }

        byte[] data = readByteArray();
        data = deflate(data, chunkMask, fullChunk);

        boolean hasBlocklight = (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16) || serverVersion.isOlderThan(ServerVersion.V_1_14))
                && !serverVersion.isOlderThanOrEquals(ServerVersion.V_1_8_8);
        boolean checkForSky = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16) || serverVersion.isOlderThanOrEquals(ServerVersion.V_1_8_8) || user.getDimension().getId() == 0;

        // 1.7/1.8 don't use this NetStreamInput
        NetStreamInput dataIn = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9) ? new NetStreamInput(new ByteArrayInputStream(data)) : null;
        BaseChunk[] chunks = getChunkReader().read(user.getDimension(), chunkMask, secondaryChunkMask, fullChunk, hasBlocklight, checkForSky, chunkSize, data, dataIn);

        if (hasBiomeData && serverVersion.isOlderThan(ServerVersion.V_1_15)) {
            if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_13)) { // Uses ints
                biomeDataInts = new int[256];
                for (int i = 0; i < biomeDataInts.length; i++) {
                    biomeDataInts[i] = dataIn.readInt();
                }
            } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) { // Uses bytes
                biomeDataBytes = new byte[256];
                for (int i = 0; i < biomeDataBytes.length; i++) {
                    biomeDataBytes[i] = dataIn.readByte();
                }
            } else if (data.length == 0) {
                // if cache-chunk-maps is enabled in paper, paper doesn't send any biome data on chunk unload
                biomeDataBytes = data; // empty array
            } else {
                biomeDataBytes = Arrays.copyOfRange(data, data.length - 256, data.length);
            }
        }

        // Tile entities are not sent with this packet on 1.8 and below
        // on 1.9 and above for all versions, tile entities are sent with the chunk data
        // (And can be sent with their own packet too!)
        int tileEntityCount = serverVersion.isOlderThan(ServerVersion.V_1_9) ? 0 : readVarInt();
        TileEntity[] tileEntities = new TileEntity[tileEntityCount];

        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18)) {
            for (int i = 0; i < tileEntities.length; i++) {
                tileEntities[i] = new TileEntity(readByte(), readShort(), readVarInt(), readNBT());
            }
        } else {
            for (int i = 0; i < tileEntities.length; i++) {
                tileEntities[i] = new TileEntity(readNBT());
            }
        }

        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18)) {
            if (serverVersion.isOlderThanOrEquals(ServerVersion.V_1_19_4)) {
                trustEdges = readBoolean();
            }

            skyLightMask = readChunkMask();
            blockLightMask = readChunkMask();
            emptySkyLightMask = readChunkMask();
            emptyBlockLightMask = readChunkMask();

            skyLightCount = readVarInt();
            this.skyLightArray = new byte[skyLightCount][];
            for (int x = 0; x < skyLightCount; x++) {
                skyLightArray[x] = readByteArray();
            }

            blockLightCount = readVarInt();
            this.blockLightArray = new byte[blockLightCount][];
            for (int x = 0; x < blockLightCount; x++) {
                blockLightArray[x] = readByteArray();
            }
        }

        if (hasBiomeData) {
            if (hasHeightMaps) {
                if (bytesInsteadOfInts) {
                    column = new Column(chunkX, chunkZ, true, chunks, tileEntities, heightMaps, biomeDataBytes);
                } else {
                    column = new Column(chunkX, chunkZ, true, chunks, tileEntities, heightMaps, biomeDataInts);
                }
            } else {
                if (bytesInsteadOfInts) {
                    column = new Column(chunkX, chunkZ, true, chunks, tileEntities, biomeDataBytes);
                } else {
                    column = new Column(chunkX, chunkZ, true, chunks, tileEntities, biomeDataInts);
                }
            }
        } else {
            if (hasHeightMaps) {
                column = new Column(chunkX, chunkZ, fullChunk, chunks, tileEntities, heightMaps);
            } else {
                column = new Column(chunkX, chunkZ, fullChunk, chunks, tileEntities);
            }
        }
    }

    private byte[] deflate(byte[] toDeflate, BitSet mask, boolean fullChunk) {
        // The data is already decompressed! (step only needed for 1.7.x)
        if (serverVersion.isNewerThan(ServerVersion.V_1_7_10)) {
            return toDeflate;
        }

        // Determine inflated data length.
        int chunkCount = 0;

        for (int count = 0; count < 16; count++) {
            chunkCount += mask.get(count) ? 1 : 0;
        }

        int len = 12288 * chunkCount;
        if (fullChunk) {
            len += 256;
        }

        byte[] data = new byte[len];
        // Inflate chunk data.
        Inflater inflater = new Inflater();
        inflater.setInput(toDeflate, 0, toDeflate.length);

        try {
            inflater.inflate(data);
        } catch (DataFormatException e) {
            e.printStackTrace();
        } finally {
            inflater.end();
        }

        return data;
    }

    @Override
    public void write() {
        writeInt(column.getX());
        writeInt(column.getZ());

        boolean v1_18 = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18);
        boolean v1_17 = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17);
        boolean v1_9 = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9);
        boolean v1_8 = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_8);

        if (!v1_17) {
            writeBoolean(column.isFullChunk());
        }

        boolean hasWrittenBiomeData = false;

        if (serverVersion == ServerVersion.V_1_16 ||
                serverVersion == ServerVersion.V_1_16_1) {
            //Ignore old data = true, use existing lighting
            //TODO See what we can do with this field
            writeBoolean(ignoreOldData);
        }

        //TODO Decompress data on 1.7.10
        //https://github.com/retrooper/packetevents/blob/794ad6b042c1c89a931d322f4f83317b573e891a/src/main/java/io/github/retrooper/packetevents/wrapper/play/server/WrapperPlayServerChunkData.java

        ByteArrayOutputStream dataBytes = new ByteArrayOutputStream();
        NetStreamOutput dataOut = new NetStreamOutput(dataBytes);

        BitSet chunkMask = new BitSet();
        BaseChunk[] chunks = column.getChunks();

        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
            for (int index = 0; index < chunks.length; index++) {
                BaseChunk chunk = chunks[index];
                if (v1_18) {
                    Chunk_v1_18.write(dataOut, (Chunk_v1_18) chunk);
                } else if (v1_9 && chunk != null) {
                    chunkMask.set(index);
                    Chunk_v1_9.write(dataOut, (Chunk_v1_9) chunk);
                }
            }
        } else if (v1_8) {
            NetworkChunkData data = ChunkReader_v1_8.chunksToData((Chunk_v1_8[]) chunks, column.getBiomeDataBytes());
            writeShort(data.getMask());
            writeByteArray(data.getData());
            return;
        } else {
            NetworkChunkData data = ChunkReader_v1_7.chunksToData((Chunk_v1_7[]) chunks, column.getBiomeDataBytes());
            Deflater deflater = new Deflater(-1);

            byte deflated[] = new byte[data.getData().length];
            int len = deflated.length;
            try {
                deflater.setInput(data.getData(), 0, data.getData().length);
                deflater.finish();
                len = deflater.deflate(deflated);
            } finally {
                deflater.end();
            }
            writeShort(data.getMask());
            writeShort(data.getExtendedChunkMask());
            writeInt(len);
            for (int i = 0; i < len; i++) {
                dataOut.writeByte(deflated[i]);
            }
            return;
        }

        if (column.isFullChunk() && serverVersion.isOlderThan(ServerVersion.V_1_15)) {
            if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_13)) {
                for (int i : column.getBiomeDataInts()) {
                    dataOut.writeInt(i);
                }
            } else {
                for (byte i : column.getBiomeDataBytes()) {
                    dataOut.writeByte(i);
                }
            }
            hasWrittenBiomeData = true;
        }

        if (!v1_18) {
            writeChunkMask(chunkMask);
        }

        boolean hasHeightMaps = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14);
        if (hasHeightMaps) {
            writeNBT(column.getHeightMaps());
        }

        if (column.hasBiomeData() && serverVersion.isNewerThanOrEquals(ServerVersion.V_1_15) && !v1_18) {
            boolean bytesInsteadOfInts = serverVersion.isOlderThan(ServerVersion.V_1_13);
            int[] biomeDataInts = column.getBiomeDataInts();
            byte[] biomeDataByes = column.getBiomeDataBytes();

            if (bytesInsteadOfInts) {
                for (byte biomeDataBye : biomeDataByes) {
                    writeByte(biomeDataBye);
                }
            } else {
                if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16_2)) {
                    writeVarIntArray(biomeDataInts);
                } else {
                    for (int biomeDataInt : biomeDataInts) {
                        writeInt(biomeDataInt);
                    }
                }
            }

            hasWrittenBiomeData = true;
        }

        byte[] data = dataBytes.toByteArray();
        writeByteArray(data);

        if (column.hasBiomeData() && !hasWrittenBiomeData) {
            byte[] biomeDataBytes = new byte[256];
            int[] biomeData = column.getBiomeDataInts();
            for (int i = 0; i < biomeDataBytes.length; i++) {
                biomeDataBytes[i] = (byte) biomeData[i];
            }
            writeByteArray(biomeDataBytes);
        }

        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18)) {
            writeVarInt(column.getTileEntities().length);
            for (TileEntity tileEntity : column.getTileEntities()) {
                writeByte(tileEntity.getPackedByte());
                writeShort(tileEntity.getYShort());
                writeVarInt(tileEntity.getType());
                writeNBT(tileEntity.getNBT());
            }
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
            TileEntity[] tileEntities = column.getTileEntities();
            writeVarInt(tileEntities.length);

            for (TileEntity tileEntity : tileEntities) {
                writeNBT(tileEntity.getNBT());
            }
        }

        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18)) {
            if (serverVersion.isOlderThanOrEquals(ServerVersion.V_1_19_4)) {
                writeBoolean(trustEdges);
            }
            writeChunkMask(skyLightMask);
            writeChunkMask(blockLightMask);
            writeChunkMask(emptySkyLightMask);
            writeChunkMask(emptyBlockLightMask);

            writeVarInt(skyLightCount);
            for (int x = 0; x < skyLightCount; x++) {
                writeByteArray(skyLightArray[x]);
            }

            writeVarInt(blockLightCount);
            for (int x = 0; x < blockLightCount; x++) {
                writeByteArray(blockLightArray[x]);
            }
        }
    }

    @Override
    public void copy(WrapperPlayServerChunkData wrapper) {
        this.column = wrapper.column;
        this.ignoreOldData = wrapper.ignoreOldData;
        this.trustEdges = wrapper.trustEdges;
        this.blockLightMask = wrapper.blockLightMask;
        this.skyLightMask = wrapper.skyLightMask;
        this.emptyBlockLightMask = wrapper.emptyBlockLightMask;
        this.emptySkyLightMask = wrapper.emptySkyLightMask;
        this.skyLightCount = wrapper.skyLightCount;
        this.blockLightCount = wrapper.blockLightCount;
        this.skyLightArray = wrapper.skyLightArray;
        this.blockLightArray = wrapper.blockLightArray;
    }

    private ChunkReader getChunkReader() {
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18)) {
            return chunkReader_v1_18;
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16)) {
            return chunkReader_v1_16;
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
            return chunkReader_v1_9;
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_8)) {
            return chunkReader_v1_8;
        } else {
            return chunkReader_v1_7;
        }
    }

    public Column getColumn() {
        return this.column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public boolean isIgnoreOldData() {
        return this.ignoreOldData;
    }

    public void setIgnoreOldData(boolean ignoreOldData) {
        this.ignoreOldData = ignoreOldData;
    }

    public boolean isTrustEdges() {
        return this.trustEdges;
    }

    public void setTrustEdges(boolean trustEdges) {
        this.trustEdges = trustEdges;
    }

    public BitSet getBlockLightMask() {
        return this.blockLightMask;
    }

    public void setBlockLightMask(BitSet blockLightMask) {
        this.blockLightMask = blockLightMask;
    }

    public BitSet getSkyLightMask() {
        return this.skyLightMask;
    }

    public void setSkyLightMask(BitSet skyLightMask) {
        this.skyLightMask = skyLightMask;
    }

    public BitSet getEmptyBlockLightMask() {
        return this.emptyBlockLightMask;
    }

    public void setEmptyBlockLightMask(BitSet emptyBlockLightMask) {
        this.emptyBlockLightMask = emptyBlockLightMask;
    }

    public BitSet getEmptySkyLightMask() {
        return this.emptySkyLightMask;
    }

    public void setEmptySkyLightMask(BitSet emptySkyLightMask) {
        this.emptySkyLightMask = emptySkyLightMask;
    }

    public int getSkyLightCount() {
        return this.skyLightCount;
    }

    public void setSkyLightCount(int skyLightCount) {
        this.skyLightCount = skyLightCount;
    }

    public int getBlockLightCount() {
        return this.blockLightCount;
    }

    public void setBlockLightCount(int blockLightCount) {
        this.blockLightCount = blockLightCount;
    }

    public byte[][] getSkyLightArray() {
        return this.skyLightArray;
    }

    public void setSkyLightArray(byte[][] skyLightArray) {
        this.skyLightArray = skyLightArray;
    }

    public byte[][] getBlockLightArray() {
        return this.blockLightArray;
    }

    public void setBlockLightArray(byte[][] blockLightArray) {
        this.blockLightArray = blockLightArray;
    }
}
