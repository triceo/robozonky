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

package com.github.robozonky.app.daemon;

import java.time.Duration;

import com.github.robozonky.internal.jobs.TenantJob;
import com.github.robozonky.test.AbstractRoboZonkyTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SellingJobTest extends AbstractRoboZonkyTest {

    @Test
    void getters() {
        final TenantJob t = new SellingJob();
        assertThat(t.payload()).isNotNull()
                .isInstanceOf(Selling.class);
        assertThat(t.killIn()).isEqualTo(Duration.ofMinutes(30));
        assertThat(t.repeatEvery()).isEqualTo(Duration.ofHours(2));
    }

}
