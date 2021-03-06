/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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

package org.wildfly.security.sasl.util;

import static org.wildfly.common.Assert.checkNotNullParam;
import static org.wildfly.common.math.HashMath.multiHashOrdered;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import javax.net.ssl.SSLSession;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;
import javax.security.sasl.SaslException;

/**
 * A SASL client factory which provides information about the security layer of the connection to the callback handler.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class SSLSaslClientFactory extends AbstractDelegatingSaslClientFactory implements SaslClientFactory {
    private final Supplier<SSLSession> sslSession;

    /**
     * Construct a new instance.
     *
     * @param delegate the delegate SASL client factory
     * @param sslSession supplier of the current SSLSession
     */
    public SSLSaslClientFactory(final SaslClientFactory delegate, final Supplier<SSLSession> sslSession) {
       super(delegate);
       this.sslSession = checkNotNullParam("sslSession", sslSession);
    }

    public SaslClient createSaslClient(final String[] mechanisms, final String authorizationId, final String protocol, final String serverName, final Map<String, ?> props, final CallbackHandler cbh) throws SaslException {
        final SSLQueryCallbackHandler newHandler = new SSLQueryCallbackHandler(cbh, sslSession);
        SaslClient saslClient = super.createSaslClient(mechanisms, authorizationId, protocol, serverName, props, newHandler);
        newHandler.activate();

        return saslClient;
    }

    @SuppressWarnings("checkstyle:equalshashcode")
    public boolean equals(final Object other) {
        return other instanceof SSLSaslClientFactory && equals((SSLSaslClientFactory) other);
    }

    @SuppressWarnings("checkstyle:equalshashcode")
    public boolean equals(final AbstractDelegatingSaslClientFactory other) {
        return other instanceof SSLSaslClientFactory && equals((SSLSaslClientFactory) other);
    }

    @SuppressWarnings("checkstyle:equalshashcode")
    public boolean equals(final SSLSaslClientFactory other) {
        return super.equals(other) && Objects.equals(sslSession, other.sslSession);
    }

    protected int calculateHashCode() {
        return multiHashOrdered(multiHashOrdered(super.calculateHashCode(), getClass().hashCode()), Objects.hashCode(sslSession));
    }
}
