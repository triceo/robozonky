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

import java.util.StringJoiner;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import com.github.robozonky.api.Money;
import com.github.robozonky.api.Ratio;
import com.github.robozonky.api.remote.entities.SellFee;
import com.github.robozonky.api.remote.entities.SellPriceInfo;

public class SellPriceInfoImpl extends BaseEntity implements SellPriceInfo {

    @XmlElement
    private String sellPrice;

    @XmlElement
    private SellFee fee;

    // Strings to be represented as money.
    @XmlElement
    private String boughtFor;

    @XmlElement
    private String remainingPrincipal;

    @XmlElement
    private String discount;

    SellPriceInfoImpl() {
        // For JAXB.
    }

    @Override
    public SellFee getFee() {
        return fee;
    }

    @Override
    @XmlTransient
    public Money getSellPrice() {
        return Money.from(sellPrice);
    }

    @Override
    @XmlTransient
    public Money getBoughtFor() {
        return Money.from(boughtFor);
    }

    @Override
    @XmlTransient
    public Money getRemainingPrincipal() {
        return Money.from(remainingPrincipal);
    }

    @Override
    @XmlTransient
    public Ratio getDiscount() {
        return Ratio.fromRaw(discount);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SellPriceInfoImpl.class.getSimpleName() + "[", "]")
            .add("boughtFor='" + boughtFor + "'")
            .add("sellPrice='" + sellPrice + "'")
            .add("discount='" + discount + "'")
            .add("fee=" + fee)
            .add("remainingPrincipal='" + remainingPrincipal + "'")
            .toString();
    }
}
