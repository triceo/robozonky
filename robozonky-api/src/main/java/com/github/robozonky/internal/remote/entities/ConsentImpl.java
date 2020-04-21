/*
 * Copyright 2020 The RoboZonky Project
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

package com.github.robozonky.internal.remote.entities;

import java.time.OffsetDateTime;

import javax.xml.bind.annotation.XmlElement;

import com.github.robozonky.api.remote.entities.Consent;

public class ConsentImpl extends BaseEntity implements Consent {

    @XmlElement
    private OffsetDateTime agreedOn;

    private ConsentImpl() {
        // For JAXB.
    }

    ConsentImpl(OffsetDateTime agreedOn) {
        this.agreedOn = agreedOn;
    }

    @Override
    public OffsetDateTime getAgreedOn() {
        return agreedOn;
    }

}
