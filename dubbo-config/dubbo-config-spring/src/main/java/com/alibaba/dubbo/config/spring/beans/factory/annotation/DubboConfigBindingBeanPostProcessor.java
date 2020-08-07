/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.config.spring.beans.factory.annotation;

import com.alibaba.dubbo.common.utils.Assert;
import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.spring.context.annotation.DubboConfigBindingRegistrar;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubboConfigBinding;
import com.alibaba.dubbo.config.spring.context.config.DubboConfigBeanCustomizer;
import com.alibaba.dubbo.config.spring.context.properties.DefaultDubboConfigBinder;
import com.alibaba.dubbo.config.spring.context.properties.DubboConfigBinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;

/**
 * Dubbo Config Binding {@link BeanPostProcessor}
 *
 * @see EnableDubboConfigBinding
 * @see DubboConfigBindingRegistrar
 * @since 2.5.8
 */

public class DubboConfigBindingBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware, InitializingBean {

    private final Log log = LogFactory.getLog(getClass());

    /**
     * The prefix of Configuration Properties
     */
    private final String prefix;

    /**
     * Binding Bean Name
     */
    private final String beanName;

    private DubboConfigBinder dubboConfigBinder;

    private ApplicationContext applicationContext;

    private List<DubboConfigBeanCustomizer> configBeanCustomizers = Collections.emptyList();

    /**
     * @param prefix   the prefix of Configuration Properties
     * @param beanName the binding Bean Name
     */
    public DubboConfigBindingBeanPostProcessor(String prefix, String beanName) {
        Assert.notNull(prefix, "The prefix of Configuration Properties must not be null");
        Assert.notNull(beanName, "The name of bean must not be null");
        this.prefix = prefix;
        this.beanName = beanName;
    }

    /**
     * 由spring调度 实现了BeanPostProcessor 接口 在spring 创建bean之前都会使用Processor做前置和后置处理
     *
     * bean创建之后的后置处理
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        /**
         * 当前config是否能被beanProcessor处理
         * 比如处理 dubbo.application.id=ddd  那么初始化这个config 则会被当时初始化的BeanPostProcessor处理
         * beanName 在<9>处初始化一个前缀对应一个config
         */
        if (beanName.equals(this.beanName) && bean instanceof AbstractConfig) {

            AbstractConfig dubboConfig = (AbstractConfig) bean;
            // 进行绑定
            bind(prefix, dubboConfig);
            // 进行绑定
            customize(beanName, dubboConfig);
        }
        return bean;
    }

    private void bind(String prefix, AbstractConfig dubboConfig) {
        // 传入前缀和config对象执行bind逻辑如果没有指定 这里就是调用默认的DefaultDubboConfigBinder
        dubboConfigBinder.bind(prefix, dubboConfig);

        if (log.isInfoEnabled()) {
            log.info("The properties of bean [name : " + beanName + "] have been binding by prefix of " +
                    "configuration properties : " + prefix);
        }
    }

    private void customize(String beanName, AbstractConfig dubboConfig) {
        // 默认初始化了一个 NamePropertyDefaultValueDubboConfigBeanCustomizer
        for (DubboConfigBeanCustomizer customizer : configBeanCustomizers) {
            customizer.customize(beanName, dubboConfig);
        }

    }

    public DubboConfigBinder getDubboConfigBinder() {
        return dubboConfigBinder;
    }

    public void setDubboConfigBinder(DubboConfigBinder dubboConfigBinder) {
        this.dubboConfigBinder = dubboConfigBinder;
    }

    /**
     * bean创建之前的前置处理 并没有做具体实现
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 从容器获取DubboConfigBinder组件 获取不到则使用 DefaultDubboConfigBinder
        initDubboConfigBinder();
        // 从容器获取dubboConfigBeanCustomizer组件，默认NamePropertyDefaultValueDubboConfigBeanCustomizer在<8>处初始化
        // 用于给容器里面的config做初始化
        initConfigBeanCustomizers();

    }

    private void initDubboConfigBinder() {

        if (dubboConfigBinder == null) {
            try {
                // 从容器获得这个类型的bean 这里我们可以做自己的扩展
                dubboConfigBinder = applicationContext.getBean(DubboConfigBinder.class);
            } catch (BeansException ignored) {
                if (log.isDebugEnabled()) {
                    log.debug("DubboConfigBinder Bean can't be found in ApplicationContext.");
                }
                // Use Default implementation
                // 获取不到则使用默认的 并且把环境信息传入供后续使用
                dubboConfigBinder = createDubboConfigBinder(applicationContext.getEnvironment());
            }
        }

    }

    private void initConfigBeanCustomizers() {
        // 从容器获取DubboConfigBeanCustomizer的实现 可以有多个 注意在<8>处默认初始化了NamePropertyDefaultValueDubboConfigBeanCustomizer在
        Collection<DubboConfigBeanCustomizer> configBeanCustomizers =
                beansOfTypeIncludingAncestors(applicationContext, DubboConfigBeanCustomizer.class).values();

        this.configBeanCustomizers = new ArrayList<DubboConfigBeanCustomizer>(configBeanCustomizers);

        AnnotationAwareOrderComparator.sort(this.configBeanCustomizers);
    }

    /**
     * Create {@link DubboConfigBinder} instance.
     *
     * @param environment
     * @return {@link DefaultDubboConfigBinder}
     */
    protected DubboConfigBinder createDubboConfigBinder(Environment environment) {
        DefaultDubboConfigBinder defaultDubboConfigBinder = new DefaultDubboConfigBinder();
        defaultDubboConfigBinder.setEnvironment(environment);

        defaultDubboConfigBinder.setIgnoreUnknownFields(true);
        defaultDubboConfigBinder.setIgnoreInvalidFields(true);

        return defaultDubboConfigBinder;
    }

}
