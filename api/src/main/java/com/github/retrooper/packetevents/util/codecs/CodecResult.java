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

package com.github.retrooper.packetevents.util.codecs;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

@NullMarked
public class CodecResult<T> {

    private final @Nullable T result;
    private final @Nullable Supplier<String> error;

    public CodecResult(T result) {
        this(result, null);
    }

    public CodecResult(Supplier<String> error) {
        this(null, error);
    }

    private CodecResult(@Nullable T result, @Nullable Supplier<String> error) {
        assert (result == null) != (error == null);
        this.result = result;
        this.error = error;
    }

    public T getResultOrThrow() {
        if (this.result != null) {
            return this.result;
        }
        String error = this.error != null ? this.error.get() : null;
        throw new IllegalStateException(error);
    }

    public @Nullable T getResult() {
        return this.result;
    }

    public @Nullable Supplier<String> getError() {
        return this.error;
    }
}
