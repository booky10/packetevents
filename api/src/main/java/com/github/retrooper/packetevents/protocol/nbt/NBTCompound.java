/*
 * This file is part of ProtocolSupport - https://github.com/ProtocolSupport/ProtocolSupport
 * Copyright (C) 2021 ProtocolSupport
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.retrooper.packetevents.protocol.nbt;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class NBTCompound extends NBT {

    private static final Pattern UNQUOTED_KEY_MATCH = Pattern.compile("[A-Za-z._]+[A-Za-z0-9._+-]*");

    protected final Map<String, NBT> tags;

    public NBTCompound() {
        this(new LinkedHashMap<>());
    }

    public NBTCompound(int expectedSize) {
        this(new LinkedHashMap<>(expectedSize));
    }

    @ApiStatus.Internal
    public NBTCompound(Map<String, NBT> tags) {
        this.tags = tags;
    }

    @Override
    public NBTType<NBTCompound> getType() {
        return NBTType.COMPOUND;
    }

    public boolean isEmpty() {
        return tags.isEmpty();
    }

    public Set<String> getTagNames() {
        return Collections.unmodifiableSet(tags.keySet());
    }

    public Map<String, NBT> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    public int size() {
        return tags.size();
    }

    public NBT getTagOrThrow(String key) {
        NBT tag = getTagOrNull(key);
        if (tag == null) {
            throw new IllegalStateException(MessageFormat.format("NBT {0} does not exist", key));
        }
        return tag;
    }

    public @Nullable NBT getTagOrNull(String key) {
        return tags.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T extends NBT> T getTagOfTypeOrThrow(String key, Class<T> type) {
        NBT tag = getTagOrThrow(key);
        if (type.isInstance(tag)) {
            return (T) tag;
        } else {
            throw new IllegalStateException(MessageFormat.format("NBT {0} has unexpected type, expected {1}, but got {2}", key, type, tag.getClass()));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends NBT> @Nullable T getTagOfTypeOrNull(String key, Class<T> type) {
        NBT tag = getTagOrNull(key);
        if (type.isInstance(tag)) {
            return (T) tag;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends NBT> NBTList<T> getTagListOfTypeOrThrow(String key, Class<T> type) {
        NBTList<? extends NBT> list = getTagOfTypeOrThrow(key, NBTList.class);
        if (!type.isAssignableFrom(list.getTagsType().getNBTClass())) {
            throw new IllegalStateException(MessageFormat.format("NBTList {0} tags type has unexpected type, expected {1}, but got {2}", key, type, list.getTagsType().getNBTClass()));
        }
        return (NBTList<T>) list;
    }

    @SuppressWarnings("unchecked")
    public <T extends NBT> @Nullable NBTList<T> getTagListOfTypeOrNull(String key, Class<T> type) {
        NBTList<? extends NBT> list = getTagOfTypeOrNull(key, NBTList.class);
        if ((list != null) && type.isAssignableFrom(list.getTagsType().getNBTClass())) {
            return (NBTList<T>) list;
        }
        return null;
    }

    public NBTCompound getCompoundTagOrThrow(String key) {
        return getTagOfTypeOrThrow(key, NBTCompound.class);
    }

    public @Nullable NBTCompound getCompoundTagOrNull(String key) {
        return getTagOfTypeOrNull(key, NBTCompound.class);
    }

    public NBTNumber getNumberTagOrThrow(String key) {
        return getTagOfTypeOrThrow(key, NBTNumber.class);
    }

    public @Nullable NBTNumber getNumberTagOrNull(String key) {
        return getTagOfTypeOrNull(key, NBTNumber.class);
    }

    public NBTString getStringTagOrThrow(String key) {
        return getTagOfTypeOrThrow(key, NBTString.class);
    }

    public @Nullable NBTString getStringTagOrNull(String key) {
        return getTagOfTypeOrNull(key, NBTString.class);
    }

    public NBTList<NBTCompound> getCompoundListTagOrThrow(String key) {
        return getTagListOfTypeOrThrow(key, NBTCompound.class);
    }

    public @Nullable NBTList<NBTCompound> getCompoundListTagOrNull(String key) {
        return getTagListOfTypeOrNull(key, NBTCompound.class);
    }

    public NBTList<NBTNumber> getNumberTagListTagOrThrow(String key) {
        return getTagListOfTypeOrThrow(key, NBTNumber.class);
    }

    public @Nullable NBTList<NBTNumber> getNumberListTagOrNull(String key) {
        return getTagListOfTypeOrNull(key, NBTNumber.class);
    }

    public NBTList<NBTString> getStringListTagOrThrow(String key) {
        return getTagListOfTypeOrThrow(key, NBTString.class);
    }

    public @Nullable NBTList<NBTString> getStringListTagOrNull(String key) {
        return getTagListOfTypeOrNull(key, NBTString.class);
    }

    public String getStringTagValueOrThrow(String key) {
        return getStringTagOrThrow(key).getValue();
    }

    public @Nullable String getStringTagValueOrNull(String key) {
        NBT tag = getTagOrNull(key);
        if (tag instanceof NBTString) {
            return ((NBTString) tag).getValue();
        }
        return null;
    }

    public String getStringTagValueOrDefault(String key, String defaultValue) {
        NBT tag = getTagOrNull(key);
        if (tag instanceof NBTString) {
            return ((NBTString) tag).getValue();
        }
        return defaultValue;
    }

    public NBT removeTag(String key) {
        return tags.remove(key);
    }

    @SuppressWarnings("unchecked")
    public <T extends NBT> T removeTagAndReturnIfType(String key, Class<T> type) {
        NBT tag = removeTag(key);
        if (type.isInstance(tag)) {
            return (T) tag;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends NBT> NBTList<T> removeTagAndReturnIfListType(String key, Class<T> type) {
        NBTList<?> list = removeTagAndReturnIfType(key, NBTList.class);
        if ((list != null) && type.isAssignableFrom(list.getTagsType().getNBTClass())) {
            return (NBTList<T>) list;
        }
        return null;
    }

    public void setTag(String key, NBT tag) {
        if (tag != null) {
            tags.put(key, tag);
        } else {
            tags.remove(key);
        }
    }

    public NBTCompound copy() {
        NBTCompound clone = new NBTCompound();
        for (Map.Entry<String, NBT> entry : tags.entrySet()) {
            clone.setTag(entry.getKey(), entry.getValue().copy());
        }
        return clone;
    }

    public boolean getBoolean(String string) {
        NBTNumber nbtByte = this.getTagOfTypeOrNull(string, NBTNumber.class);
        // Empty byte tags are considered 0
        return nbtByte != null && nbtByte.getAsByte() != 0;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof NBTCompound) {
            if (isEmpty() && ((NBTCompound) other).isEmpty()) {
                return true;
            }
            return tags.equals(((NBTCompound) other).tags);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return tags.hashCode();
    }

    @Override
    public String toSnbtString(ClientVersion version) {
        boolean v1215 = version.isNewerThanOrEquals(ClientVersion.V_1_21_5);
        StringBuilder builder = new StringBuilder("{");
        List<Map.Entry<String, NBT>> entries = new ArrayList<>(this.tags.entrySet());
        entries.sort(Map.Entry.comparingByKey());
        for (int i = 0, len = entries.size(); i < len; i++) {
            if (i != 0) {
                builder.append(',');
            }
            Map.Entry<String, NBT> entry = entries.get(i);
            String key = entry.getKey();
            // check if quotations are needed
            if (v1215 && (key.equalsIgnoreCase("true") || key.equalsIgnoreCase("false"))
                    || UNQUOTED_KEY_MATCH.matcher(key).matches()) {
                // can be added with quotations
                builder.append(key);
            } else {
                builder.append(NBTString.quoteAndEscape(key, version));
            }
            builder.append(':').append(entry.getValue().toSnbtString(version));
        }
        return builder.append('}').toString();
    }

    @Override
    public String toString() {
        return "Compound{" + tags + "}";
    }
}
