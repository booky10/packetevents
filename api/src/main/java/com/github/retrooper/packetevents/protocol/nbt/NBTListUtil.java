/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2025 retrooper and contributors
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

package com.github.retrooper.packetevents.protocol.nbt;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public final class NBTListUtil {

    private NBTListUtil() {
    }

    // because of useless type cast
    @SuppressWarnings("unchecked")
    public static NBT constructList(List<NBT> tags) {
        if (tags.isEmpty()) {
            return new NBTList<>(NBTType.END, 0);
        }
        NBTType<?> commonType = getCommonType(tags);
        if (commonType != null) {
            return new NBTList<>((NBTType<NBT>) commonType, tags);
        }
        NBTList<NBTCompound> list = new NBTList<>(NBTType.COMPOUND, tags.size());
        for (NBT tag : tags) {
            list.addTag(wrapIfNeeded(tag));
        }
        return list;
    }

    // because of commonList cast from NBT -> NBTList<NBT>
    // to add the nbt tag we checked before
    @SuppressWarnings("unchecked")
    public static NBT constructListOrArray(List<NBT> tags) {
        if (tags.isEmpty()) {
            return new NBTList<>(NBTType.END, 0);
        }
        NBTList<NBTCompound> mixedList = null; // if this is present, the input contains mixed tags
        NBT commonList = null; // can be both homogenous list or byte/int/long array
        NBTType<?> listType = null; // the type of the nbt tag above
        NBTType<?> listContentType = null; // if above is == NBTType.LIST, this is the list content type
        for (int i = 0, len = tags.size(); i < len; i++) {
            NBT tag = tags.get(i);
            if (mixedList != null) {
                mixedList.addTag(wrapIfNeeded(tag));
                continue;
            }
            if (listType == null) {
                // check if this can be an array type, default to list
                listType = getArrayTypeOr(tag.getType(), NBTType.LIST);
                // save more data if this is a list
                if (listType == NBTType.LIST) {
                    if (tag instanceof NBTCompound) {
                        // directly assume mixed list if this is a compound
                        mixedList = new NBTList<>(NBTType.COMPOUND, len);
                        mixedList.addTag(wrapIfNeeded(tag));
                        continue;
                    }
                    // assume homogenous list, save content type
                    listContentType = tag.getType();
                }
            } else {
                // check if the array/list type is still valid
                NBTType<?> arrayType = getArrayTypeOr(tag.getType(), NBTType.LIST);
                if (listType != arrayType || (arrayType == NBTType.LIST && listContentType != tag.getType())) {
                    // mixed values, "migrate" to mixed list
                    mixedList = new NBTList<>(NBTType.COMPOUND, len);
                    i = -1; // reset index to move all values into the list
                    commonList = null; // allow gc to free homogenous list
                    continue;
                }
            }
            // homogenous type has been determined, create list tag if not already
            // created and set element
            if (listType == NBTType.LIST) {
                if (commonList == null) {
                    commonList = new NBTList<>(listContentType, len);
                }
                ((NBTList<? super NBT>) commonList).addTag(tag);
            } else if (listType == NBTType.BYTE_ARRAY) {
                if (commonList == null) {
                    commonList = new NBTByteArray(new byte[len]);
                }
                ((NBTByteArray) commonList).getValue()[i] = ((NBTNumber) tag).getAsByte();
            } else if (listType == NBTType.INT_ARRAY) {
                if (commonList == null) {
                    commonList = new NBTIntArray(new int[len]);
                }
                ((NBTIntArray) commonList).getValue()[i] = ((NBTNumber) tag).getAsInt();
            } else if (listType == NBTType.LONG_ARRAY) {
                if (commonList == null) {
                    commonList = new NBTLongArray(new long[len]);
                }
                ((NBTLongArray) commonList).getValue()[i] = ((NBTNumber) tag).getAsLong();
            } else {
                throw new AssertionError();
            }
        }
        return mixedList != null ? mixedList : commonList;
    }

    public static boolean isWrapper(NBTCompound compound) {
        return compound.size() == 1 && compound.tags.containsKey("");
    }

    public static NBTCompound wrapIfNeeded(NBT tag) {
        if (tag instanceof NBTCompound
                && !isWrapper((NBTCompound) tag)) {
            return (NBTCompound) tag;
        }
        return wrap(tag);
    }

    public static NBTCompound wrap(NBT tag) {
        NBTCompound wrapped = new NBTCompound(1);
        wrapped.setTag("", tag);
        return wrapped;
    }

    @Contract("_, !null -> !null")
    public static @Nullable NBTType<?> getArrayTypeOr(NBTType<?> type, @Nullable NBTType<?> defaultType) {
        if (type == NBTType.BYTE) {
            return NBTType.BYTE_ARRAY;
        } else if (type == NBTType.INT) {
            return NBTType.INT_ARRAY;
        } else if (type == NBTType.LONG) {
            return NBTType.LONG_ARRAY;
        }
        return defaultType;
    }

    public static @Nullable NBTType<?> getCommonType(List<NBT> values) {
        NBTType<?> type = null;
        for (NBT value : values) {
            if (type == null) {
                type = value.getType();
            } else if (type != value.getType()) {
                return null; // no common type found
            }
        }
        return type;
    }
}
