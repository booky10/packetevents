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

import java.util.Objects;

public class NBTString extends NBT {

    private static final String[] CONTROL_CHARACTER_ESCAPES = new String[' '];

    static {
        // see 1.21.5+ SnbtGrammer#escapeControlCharacters
        for (char c = 0; c < CONTROL_CHARACTER_ESCAPES.length; c++) {
            CONTROL_CHARACTER_ESCAPES[c] = String.format("x%02X", (byte) c);
        }
        CONTROL_CHARACTER_ESCAPES['\b'] = "b";
        CONTROL_CHARACTER_ESCAPES['\t'] = "t";
        CONTROL_CHARACTER_ESCAPES['\n'] = "n";
        CONTROL_CHARACTER_ESCAPES['\f'] = "f";
        CONTROL_CHARACTER_ESCAPES['\r'] = "r";
    }

    protected final String string;

    public NBTString(String string) {
        this.string = string;
    }

    public static String quoteAndEscape(String string, ClientVersion version) {
        boolean v1215 = version.isNewerThanOrEquals(ClientVersion.V_1_21_5);
        StringBuilder builder = new StringBuilder().append(' ');
        char quote = 0;
        for (int i = 0, len = string.length(); i < len; i++) {
            char c = string.charAt(i);
            if (c == '\\') {
                builder.append(v1215 ? "\\\\" : "\\");
            } else if (c != '"' && c != '\'') {
                if (v1215 && c < CONTROL_CHARACTER_ESCAPES.length) {
                    builder.append('\\').append(CONTROL_CHARACTER_ESCAPES[c]);
                } else {
                    builder.append(c); // append normal char
                }
            } else {
                if (quote == 0) {
                    quote = c == '"' ? '\'' : '"';
                }
                if (quote == c) {
                    builder.append('\\');
                }
                builder.append(c);
            }
        }
        if (quote == 0) {
            quote = '"';
        }
        builder.setCharAt(0, quote);
        return builder.append(quote).toString();
    }

    @Override
    public NBTType<NBTString> getType() {
        return NBTType.STRING;
    }

    public String getValue() {
        return string;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NBTString other = (NBTString) obj;
        return Objects.equals(string, other.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string);
    }

    @Override
    public NBTString copy() {
        return this;
    }

    @Override
    public String toSnbtString(ClientVersion version) {
        return quoteAndEscape(this.string, version);
    }

    @Override
    public String toString() {
        return "String(" + string + ")";
    }
}
