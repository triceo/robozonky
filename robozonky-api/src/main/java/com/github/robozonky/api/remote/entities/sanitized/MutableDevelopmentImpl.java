/*
 * Copyright 2018 The RoboZonky Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robozonky.api.remote.entities.sanitized;

import java.time.OffsetDateTime;
import java.util.Optional;

import com.github.robozonky.api.remote.entities.DateDescriptor;
import com.github.robozonky.api.remote.entities.RawDevelopment;
import com.github.robozonky.api.remote.enums.DevelopmentTpe;
import com.github.robozonky.internal.api.ToStringBuilder;

final class MutableDevelopmentImpl implements DevelopmentBuilder {

    private String publicNote;
    private DevelopmentTpe type;
    private OffsetDateTime dateFrom, dateTo;

    MutableDevelopmentImpl() {

    }

    MutableDevelopmentImpl(final RawDevelopment original) {
        this.publicNote = original.getPublicNote();
        this.type = original.getBusinessCode();
        this.dateFrom = DateDescriptor.toOffsetDateTime(original.getDateFrom());
        if (original.getDateTo() != null) {
            this.dateTo = DateDescriptor.toOffsetDateTime(original.getDateTo());
        }
    }

    @Override
    public DevelopmentBuilder setType(final DevelopmentTpe type) {
        this.type = type;
        return this;
    }

    @Override
    public DevelopmentBuilder setPublicNote(final String publicNote) {
        this.publicNote = publicNote;
        return this;
    }

    @Override
    public DevelopmentBuilder setDateFrom(final OffsetDateTime dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    @Override
    public DevelopmentBuilder setDateTo(final OffsetDateTime dateTo) {
        this.dateTo = dateTo;
        return this;
    }

    @Override
    public DevelopmentTpe getType() {
        return type;
    }

    @Override
    public Optional<String> getPublicNote() {
        return Optional.ofNullable(publicNote);
    }

    @Override
    public OffsetDateTime getDateFrom() {
        return dateFrom;
    }

    @Override
    public Optional<OffsetDateTime> getDateTo() {
        return Optional.ofNullable(dateTo);
    }

    @Override
    public final String toString() {
        return new ToStringBuilder(this).toString();
    }
}
