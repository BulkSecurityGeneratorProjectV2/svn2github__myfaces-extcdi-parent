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
package org.apache.myfaces.extensions.cdi.bv.impl;

import org.apache.myfaces.extensions.cdi.core.api.config.CodiCoreConfig;

import static org.apache.myfaces.extensions.cdi.core.impl.util.CodiUtils.injectFields;

import javax.enterprise.inject.Typed;
import javax.validation.ValidatorFactory;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintValidator;

/**
 * @author Gerhard Petracek
 */
@Typed()
public class CdiAwareValidatorFactory implements ValidatorFactory
{
    private ValidatorFactory wrappedValidatorFactory;
    private boolean customContextUsed;
    private boolean advancedQualifierRequiredForDependencyInjection;

    protected CdiAwareValidatorFactory(ValidatorFactory wrappedValidatorFactory, CodiCoreConfig codiCoreConfig)
    {
        this.wrappedValidatorFactory = wrappedValidatorFactory;
        this.advancedQualifierRequiredForDependencyInjection =
                codiCoreConfig.isAdvancedQualifierRequiredForDependencyInjection();
    }

    public Validator getValidator()
    {
        if(this.customContextUsed)
        {
            return this.wrappedValidatorFactory.getValidator();
        }
        return this.usingContext().constraintValidatorFactory(getConstraintValidatorFactory()).getValidator();
    }

    public ValidatorContext usingContext()
    {
        this.customContextUsed = true;
        return wrappedValidatorFactory.usingContext();
    }

    public MessageInterpolator getMessageInterpolator()
    {
        return wrappedValidatorFactory.getMessageInterpolator();
    }

    public TraversableResolver getTraversableResolver()
    {
        return wrappedValidatorFactory.getTraversableResolver();
    }

    public ConstraintValidatorFactory getConstraintValidatorFactory()
    {
        return new ConstraintValidatorFactory()
        {
            @SuppressWarnings({"unchecked"})
            public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> targetClass)
            {
                T validator = wrappedValidatorFactory.getConstraintValidatorFactory().getInstance(targetClass);

                return injectFields(validator, advancedQualifierRequiredForDependencyInjection);
            }
        };
    }

    public <T> T unwrap(Class<T> tClass)
    {
        return wrappedValidatorFactory.unwrap(tClass);
    }
}
