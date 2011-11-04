/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.extensions.cdi.core.api.security;

/**
 * Base class for decision-voters
 */
public abstract class AbstractDecisionVoter
{
    /**
     * Creates an instance of {@link org.apache.myfaces.extensions.cdi.core.api.security.SecurityViolation} for a given
     * string which will be used as reason to describe the violation.
     *
     * @param reason description of the violation
     * @return A new instance of {@link org.apache.myfaces.extensions.cdi.core.api.security.SecurityViolation}
     * which provides details about the found restriction.
     */
    protected SecurityViolation newSecurityViolation(final String reason)
    {
        return new SecurityViolation()
        {
            private static final long serialVersionUID = -382472852735452131L;

            /**
             * {@inheritDoc}
             */
            public String getReason()
            {
                return reason;
            }
        };
    }
}
