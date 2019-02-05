/*
 * Copyright 2019 The RoboZonky Project
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

package com.github.robozonky.api.strategies;

public enum ReservationStrategyType {

    /**
     * Read reservations and decide whether or not to accept them.
     */
    ONLY_PROCESS,
    /**
     * On top of {@link #ONLY_PROCESS}, this will also have the power to reconfigure the reservation system in any way
     * that the robot sees fit.
     */
    FULL_OWNERSHIP

}
