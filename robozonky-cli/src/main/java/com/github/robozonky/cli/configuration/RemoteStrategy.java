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

package com.github.robozonky.cli.configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

final class RemoteStrategy implements StrategyConfiguration {

    private final String strategyLocationUrl;

    public RemoteStrategy(String strategyLocationUrl) {
        this.strategyLocationUrl = strategyLocationUrl;
    }

    @Override
    public String getFinalLocation() {
        return strategyLocationUrl;
    }

    @Override
    public void accept(Path distributionRoot, Path installationRoot) {
        try {
            new URL(strategyLocationUrl);
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Wrong strategy location: " + strategyLocationUrl, ex);
        }
    }
}
