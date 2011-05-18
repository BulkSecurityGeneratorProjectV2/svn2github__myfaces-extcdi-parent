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
package org.apache.myfaces.extensions.cdi.core.api.provider;

import org.apache.myfaces.extensions.cdi.core.api.startup.CodiStartupBroadcaster;
import org.apache.myfaces.extensions.cdi.core.api.util.ClassUtils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>This class provides access to the BeanManager
 * by registring the current BeanManager in an extension and
 * making it available via a singleton factory</p>
 * <p>This is really handy if you like to access CDI functionality
 * from places where no injection is available.</p>
 *
 * <p>Usage:<p/>
 * <pre>
 * BeanManager bm = BeanManagerProvider.getInstance().getBeanManager();
 * </pre>
 */
public class BeanManagerProvider implements Extension
{
    private static BeanManagerProvider bmp = null;

    private volatile Map<ClassLoader, BeanManager> bms = new ConcurrentHashMap<ClassLoader, BeanManager>();

    /**
     * Returns if the {@link BeanManagerProvider} has been initialized
     * @return true if the bean-manager-provider is ready to be used
     */
    public static boolean isActive()
    {
        return bmp != null;
    }

    /**
     * Singleton accessor
     * @return the singleton BeanManagerProvider
     */
    public static BeanManagerProvider getInstance()
    {
        if(bmp == null)
        {
            //workaround for Mojarra (in combination with OWB and a custom WebBeansConfigurationListener and a custom
            //StartupBroadcaster for bootstrapping CDI)
            CodiStartupBroadcaster.broadcastStartup();
            //here bmp might not be null (depends on the broadcasters)
        }
        if(bmp == null)
        {
            throw new IllegalStateException("no " + BeanManagerProvider.class.getName() + " in place! " +
                "Please ensure that you configured the CDI implementation of your choice properly. " +
                "If your setup is correct, please clear all caches and compiled artifacts. " +
                "If there is still a problem, try one of the controlled bootstrapping add-ons for the CDI " +
                    "implementation you are using.");
        }
        return bmp;
    }


    /**
     * The active {@link BeanManager} for the current {@link ClassLoader}
     * @return the current bean-manager
     */
    public BeanManager getBeanManager()
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);

        BeanManager result = bms.get(classLoader);

        if (result == null)
        {
            result = resolveBeanManagerViaJndi();

            if(result != null)
            {
                bms.put(classLoader, result);
            }
        }
        return result;
    }

    /**
     * Get a Contextual Reference by it's type and annotation.
     * You can use this method
     *
     * @param type the type of the bean in question
     * @param qualifiers additional qualifiers which further distinct the resolved bean
     * @param <T> target type
     * @return the resolved Contextual Reference
     */
    public <T> T getContextualReference(Class<T> type, Annotation... qualifiers)
    {
        BeanManager beanManager = getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(type, qualifiers);

        return getReference(type, beanManager, beans);
    }

    /**
     * Get a Contextual Reference by it's EL Name.
     * This only works for beans with the &#064;Named annotation.
     *
     * @param type the type of the bean in question - use Object.class if the type is unknown in dyn. use-cases
     * @param name the EL name of the bean
     * @param <T> target type
     * @return the resolved Contextual Reference
     */
    public <T> T getContextualReference(Class<T> type, String name)
    {
        BeanManager beanManager = getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(name);

        return getReference(type, beanManager, beans);
    }

    /**
     * Internal helper method to resolve the right bean and
     * resolve the contextual reference.
     * @param type
     * @param beanManager
     * @param beans
     * @param <T>
     * @return the contextual reference
     */
    private <T> T getReference(Class<T> type, BeanManager beanManager, Set<Bean<?>> beans)
    {
        Bean<?> bean = beanManager.resolve(beans);
        CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

        return (T)beanManager.getReference(bean, type, creationalContext);
    }

    /**
     * Get the BeanManager from the JNDI registry.
     *
     * Workaround for jboss 6 (EXTCDI-74)
     * {@link #setBeanManager(javax.enterprise.inject.spi.AfterBeanDiscovery, javax.enterprise.inject.spi.BeanManager)}
     * is called in context of a different classloader
     *
     * @return current {@link javax.enterprise.inject.spi.BeanManager} which is provided via jndi
     */
    private BeanManager resolveBeanManagerViaJndi()
    {
        try
        {
            return (BeanManager) new InitialContext().lookup("java:comp/BeanManager");
        }
        catch (NamingException e)
        {
            //workaround didn't work -> force NPE
            return null;
        }
    }

    /**
     * It basiscally doesn't matter which of the system events we use,
     * but basically we
     * @param afterBeanDiscovery event which we don't actually use ;)
     * @param beanManager the BeanManager we store and make available.
     */
    public void setBeanManager(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        BeanManagerProvider bmpFirst = setBeanManagerProvider(this);

        ClassLoader cl = ClassUtils.getClassLoader(null);
        bmpFirst.bms.put(cl, beanManager);

        CodiStartupBroadcaster.broadcastStartup();
    }

    /**
     * This function exists to prevent findbugs to complain about
     * setting a static member from a non-static function.
     * @param beanManagerProvider the bean-manager-provider which should be used if there isn't an existing provider
     * @return the first BeanManagerProvider
     */
    private static BeanManagerProvider setBeanManagerProvider(BeanManagerProvider beanManagerProvider)
    {
        if (bmp == null)
        {
            bmp = beanManagerProvider;
        }

        return bmp;
    }
}
