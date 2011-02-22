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
package org.apache.myfaces.extensions.cdi.jsf.impl.config.view;

import org.apache.myfaces.extensions.cdi.jsf.api.config.view.InitView;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.PostRenderView;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.PrePageAction;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.PreRenderView;
import org.apache.myfaces.extensions.cdi.jsf.api.listener.phase.AfterPhase;
import org.apache.myfaces.extensions.cdi.jsf.api.listener.phase.BeforePhase;
import org.apache.myfaces.extensions.cdi.jsf.impl.config.view.spi.PageBeanConfigEntry;
import org.apache.myfaces.extensions.cdi.jsf.impl.config.view.spi.RequestLifecycleCallbackEntry;

import javax.faces.event.PhaseId;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static javax.faces.event.PhaseId.*;
import static org.apache.myfaces.extensions.cdi.jsf.impl.util.ExceptionUtils.unsupportedPhasesLifecycleCallback;

/**
 * @author Gerhard Petracek
 */
class DefaultPageBeanConfigEntry implements PageBeanConfigEntry
{
    private static final long serialVersionUID = -6578882497923715372L;

    private final String beanName;

    private final Class beanClass;

    private final Map<PhaseId, RequestLifecycleCallbackEntry> phasesLifecycleCallbacks;

    private List<Method> initViewMethods = new ArrayList<Method>();
    private List<Method> prePageActionMethods = new ArrayList<Method>();
    private List<Method> preRenderViewMethods = new ArrayList<Method>();
    private List<Method> postRenderViewMethods = new ArrayList<Method>();

    DefaultPageBeanConfigEntry(String beanName, Class beanClass)
    {
        this.beanName = beanName;
        this.beanClass = beanClass;
        this.phasesLifecycleCallbacks = Collections.unmodifiableMap(findCallbackDefinitions(beanClass));
    }

    public String getBeanName()
    {
        return beanName;
    }

    public Class getBeanClass()
    {
        return beanClass;
    }

    public RequestLifecycleCallbackEntry getPhasesLifecycleCallback(PhaseId phaseId)
    {
        return phasesLifecycleCallbacks.get(phaseId);
    }


    public List<Method> getInitViewMethods()
    {
        return Collections.unmodifiableList(this.initViewMethods);
    }

    public List<Method> getPrePageActionMethods()
    {
        return Collections.unmodifiableList(this.prePageActionMethods);
    }

    public List<Method> getPreRenderViewMethods()
    {
        return Collections.unmodifiableList(this.preRenderViewMethods);
    }

    public List<Method> getPostRenderViewMethods()
    {
        return Collections.unmodifiableList(this.postRenderViewMethods);
    }

    private Map<PhaseId, RequestLifecycleCallbackEntry> findCallbackDefinitions(Class beanClass)
    {
        Class currentClass = beanClass;

        PhasesLifecycleCallbackEntryHelper beforeCallbackEntryHelper = new PhasesLifecycleCallbackEntryHelper();
        PhasesLifecycleCallbackEntryHelper afterCallbackEntryHelper = new PhasesLifecycleCallbackEntryHelper();

        while(!(currentClass.getName().equals(Object.class.getName())))
        {
            for(Method currentMethod : currentClass.getDeclaredMethods())
            {
                if(currentMethod.isAnnotationPresent(BeforePhase.class))
                {
                    beforeCallbackEntryHelper.add(
                            currentMethod.getAnnotation(BeforePhase.class).value(), currentMethod);
                }
                else if(currentMethod.isAnnotationPresent(AfterPhase.class))
                {
                    afterCallbackEntryHelper.add(
                            currentMethod.getAnnotation(AfterPhase.class).value(), currentMethod);
                }
                else if(currentMethod.isAnnotationPresent(InitView.class))
                {
                    this.initViewMethods.add(currentMethod);
                }
                else if(currentMethod.isAnnotationPresent(PrePageAction.class))
                {
                    this.prePageActionMethods.add(currentMethod);
                }
                else if(currentMethod.isAnnotationPresent(PreRenderView.class))
                {
                    this.preRenderViewMethods.add(currentMethod);
                }
                else if(currentMethod.isAnnotationPresent(PostRenderView.class))
                {
                    this.postRenderViewMethods.add(currentMethod);
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return createPhasesLifecycleCallbackMap(beforeCallbackEntryHelper, afterCallbackEntryHelper);
    }

    private Map<PhaseId, RequestLifecycleCallbackEntry> createPhasesLifecycleCallbackMap(
            PhasesLifecycleCallbackEntryHelper beforeCallbackEntryHelper,
            PhasesLifecycleCallbackEntryHelper afterCallbackEntryHelper)
    {
        Map<PhaseId, RequestLifecycleCallbackEntry> result = new HashMap<PhaseId, RequestLifecycleCallbackEntry>(6);

        result.put(RESTORE_VIEW,
                createCallbackEntry(RESTORE_VIEW, beforeCallbackEntryHelper, afterCallbackEntryHelper));

        result.put(APPLY_REQUEST_VALUES,
                createCallbackEntry(APPLY_REQUEST_VALUES, beforeCallbackEntryHelper, afterCallbackEntryHelper));

        result.put(PROCESS_VALIDATIONS,
                createCallbackEntry(PROCESS_VALIDATIONS, beforeCallbackEntryHelper, afterCallbackEntryHelper));

        result.put(UPDATE_MODEL_VALUES,
                createCallbackEntry(UPDATE_MODEL_VALUES, beforeCallbackEntryHelper, afterCallbackEntryHelper));

        result.put(INVOKE_APPLICATION,
                createCallbackEntry(INVOKE_APPLICATION, beforeCallbackEntryHelper, afterCallbackEntryHelper));

        result.put(RENDER_RESPONSE,
                createCallbackEntry(RENDER_RESPONSE, beforeCallbackEntryHelper, afterCallbackEntryHelper));

        return result;
    }

    private RequestLifecycleCallbackEntry createCallbackEntry(
            PhaseId phaseId,
            PhasesLifecycleCallbackEntryHelper beforeCallbackEntryHelper,
            PhasesLifecycleCallbackEntryHelper afterCallbackEntryHelper)
    {
        List<Method> beforePhaseCallbacks = beforeCallbackEntryHelper.getMethodsFor(phaseId);
        List<Method> afterPhaseCallbacks = afterCallbackEntryHelper.getMethodsFor(phaseId);

        if(ANY_PHASE.equals(phaseId) ||
                (RESTORE_VIEW.equals(phaseId) && beforePhaseCallbacks != null && !beforePhaseCallbacks.isEmpty()))
        {
            throw unsupportedPhasesLifecycleCallback();
        }

        return new DefaultRequestLifecycleCallbackEntry(beforePhaseCallbacks, afterPhaseCallbacks);
    }
}
