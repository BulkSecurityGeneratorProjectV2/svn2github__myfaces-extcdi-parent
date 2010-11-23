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
package org.apache.myfaces.extensions.cdi.test.webapp.events.config;

import org.apache.myfaces.extensions.cdi.core.api.config.CodiConfig;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ConversationConfig;

/**
 * Custom ConversationConfig to enable/disable certain events for the test cases.
 * Subclasses of this class are installed via @Alternative and an entry in beans.xml.
 *
 * @author Jakob Korherr
 */
public class DefaultConversationConfig implements ConversationConfig, CodiConfig
{

    public int getConversationTimeoutInMinutes()
    {
        return 30;
    }

    public boolean isScopeBeanEventEnabled()
    {
        return false;
    }

    public boolean isAccessBeanEventEnabled()
    {
        return false;
    }

    public boolean isUnscopeBeanEventEnabled()
    {
        return false;
    }

    public boolean isStartConversationEventEnabled()
    {
        return false;
    }

    public boolean isCloseConversationEventEnabled()
    {
        return false;
    }

    public boolean isRestartConversationEventEnabled()
    {
        return false;
    }

}
