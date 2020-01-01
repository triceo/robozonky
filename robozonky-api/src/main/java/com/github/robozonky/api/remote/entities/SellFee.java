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

package com.github.robozonky.api.remote.entities;

import java.time.OffsetDateTime;
import java.util.StringJoiner;
import javax.xml.bind.annotation.XmlElement;

import com.github.robozonky.api.Money;
import io.vavr.Lazy;

public class SellFee extends BaseEntity {

    @XmlElement
    private OffsetDateTime expiresAt;

    // String to be represented as money.
    @XmlElement
    private String value;
    private final Lazy<Money> moneyValue = Lazy.of(() -> Money.from(value));

    SellFee() {
        // for JAXB
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public Money getValue() {
        return moneyValue.get();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SellFee.class.getSimpleName() + "[", "]")
                .add("value='" + value + "'")
                .add("expiresAt=" + expiresAt)
                .toString();
    }
}