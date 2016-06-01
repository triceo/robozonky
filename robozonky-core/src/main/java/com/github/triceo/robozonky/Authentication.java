/*
 * Copyright 2016 Lukáš Petrovický
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

package com.github.triceo.robozonky;

import com.github.triceo.robozonky.remote.Token;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

public abstract class Authentication {

    public static Authentication withCredentials(final String username, final String password) {
        return new CredentialBasedAuthentication(username, password);
    }

    public static Authentication withRefreshToken(final String username, final Token token) {
        return new RefreshTokenBasedAuthentication(username, token);
    }

    public abstract Token authenticate(final ResteasyClientBuilder clientBuilder);

}
