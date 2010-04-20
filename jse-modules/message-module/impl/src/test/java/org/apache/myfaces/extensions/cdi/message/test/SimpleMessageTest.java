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
package org.apache.myfaces.extensions.cdi.message.test;

import org.apache.myfaces.extensions.cdi.message.api.LocaleResolver;
import org.apache.myfaces.extensions.cdi.message.api.Message;
import org.apache.myfaces.extensions.cdi.message.impl.DefaultMessage;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Locale;

/**
 * @author Gerhard Petracek
 */
public class SimpleMessageTest extends AbstractTest
{
    @Test
    public void resolveInlineMessageTest()
    {
        Message message = this.messageContext.message().text("hello open message").create();

        assertEquals("hello open message", message.toString(this.messageContext));
    }

    @Test
    public void resolveMessageTest()
    {
        Message message = this.messageContext.message().text("{hello}").create();

        checkDefaultHelloMessage(message.toString(this.messageContext));
    }

    @Test
    public void resolveMessageTextTest()
    {
        Message message = this.messageContext.message().text("{hello}").create();

        String messageText = this.messageContext.message().toText(message); //TODO use a different config

        checkDefaultHelloMessage(messageText);
    }

    @Test
    public void resolveGermanMessageTextTest()
    {
        String messageText = this.messageContext.config()
                .use().localeResolver(createGermanLocaleResolver())
                .create().message().text("{hello}").toText();

        checkGermanHelloMessage(messageText);
    }

    @Test
    public void directMessage2TextResolvingTest()
    {
        Message message = new DefaultMessage("inline message");

        assertEquals("inline message", message.toString());
    }

    @Test
    public void manuallyCreateInlineMessageTest()
    {
        Message message = new DefaultMessage("inline message");

        assertEquals("inline message", message.toString(this.messageContext));
    }

    @Test
    public void manuallyCreateMessageWithConfigTest()
    {
        Message message = manuallyCreateDefaultMessage();
        checkDefaultHelloMessage(message.toString());
    }

    private Message manuallyCreateDefaultMessage()
    {
        return new DefaultMessage("{hello}").setMessageContextConfig(this.messageContext.config());
    }

    @Test
    public void createInvalidMessageTest()
    {
        String messageText = this.messageContext.message().text("{xyz123}").toText();

        assertEquals("???xyz123???", messageText);
    }

    @Test
   public void createInvalidMessageWithArgumentsTest()
   {
       String messageText = this.messageContext.message().text("{xyz123}").argument("123").argument("456").argument("789").toText();

       assertEquals("???xyz123??? (123,456,789)", messageText);
   }
    
    @Test
   public void createMessageWithHiddenArgumentsTest()
   {
       String messageText = this.messageContext.message().text("{xyz123}").argument(new String[]{"123", "456", "789"}).toText();

       assertEquals("???xyz123??? (123,456,789)", messageText);
   }

    @Test
    public void resolveTextTest()
    {
        String messageText = this.messageContext.message().text("{hello}").toText();

        checkDefaultHelloMessage(messageText);
    }

    @Test
    public void directTextResolvingTest()
    {
        Message message = this.messageContext.message().text("{hello}").create();
        String messageText = this.messageContext.getMessageText(message);

        checkDefaultHelloMessage(messageText);
    }

    private LocaleResolver createGermanLocaleResolver()
    {
        return new LocaleResolver()
        {
            public Locale getLocale()
            {
                return Locale.GERMAN;
            }
        };
    }

    private void checkDefaultHelloMessage(String messageText)
    {
        assertEquals("test message", messageText);
    }

    private void checkGermanHelloMessage(String messageText)
    {
        assertEquals("Test Nachricht", messageText);
    }
}
