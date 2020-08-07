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
package com.alibaba.dubbo.config.spring.context.annotation;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationBeanPostProcessor;
import com.alibaba.dubbo.config.spring.util.BeanRegistrar;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * Dubbo {@link DubboComponentScan} Bean Registrar
 *
 * @see Service
 * @see DubboComponentScan
 * @see ImportBeanDefinitionRegistrar
 * @see ServiceAnnotationBeanPostProcessor
 * @see ReferenceAnnotationBeanPostProcessor
 * @since 2.5.7
 */
public class DubboComponentScanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取扫描的包集合
        Set<String> packagesToScan = getPackagesToScan(importingClassMetadata);
        // 创建 ServiceAnnotationBeanPostProcessor Bean 对象，后续扫描 `@Service` 注解的类，创建对应的 Service Bean 对象
        registerServiceAnnotationBeanPostProcessor(packagesToScan, registry);
        // 创建ReferenceAnnotationBeanPostProcessor bean对象 后续扫描 @Reference 的类  创建对应的 Reference Bean 对象
        registerReferenceAnnotationBeanPostProcessor(registry);

    }

    /**
     * Registers {@link ServiceAnnotationBeanPostProcessor}
     *
     * @param packagesToScan packages to scan without resolving placeholders
     * @param registry       {@link BeanDefinitionRegistry}
     * @since 2.5.8
     *
     * 创建扫描@Service的Processor
     */
    private void registerServiceAnnotationBeanPostProcessor(Set<String> packagesToScan, BeanDefinitionRegistry registry) {
        // 获得ServiceAnnotationBeanPostProcessor的Builder 此类实现了spring BeanDefinitionRegistryPostProcessor 我们可以像容器自定义创建bean 调用由spring
        BeanDefinitionBuilder builder = rootBeanDefinition(ServiceAnnotationBeanPostProcessor.class);
        // 告诉spring 通过构造函数初始化 并传入扫描的包集合
        builder.addConstructorArgValue(packagesToScan);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);

    }

    /**
     * Registers {@link ReferenceAnnotationBeanPostProcessor} into {@link BeanFactory}
     *
     * @param registry {@link BeanDefinitionRegistry}
     *
     * 注册处理@Reference的处理器
     */
    private void registerReferenceAnnotationBeanPostProcessor(BeanDefinitionRegistry registry) {

        // Register @Reference Annotation Bean Processor
        // ReferenceAnnotationBeanPostProcessor 这个间接实现了MergedBeanDefinitionPostProcessor
        // spring在创建 注册  因为他间接实现了上面的接口 所以在spring初始化过程中 我们可以做一些事情
        BeanRegistrar.registerInfrastructureBean(registry,
                ReferenceAnnotationBeanPostProcessor.BEAN_NAME, ReferenceAnnotationBeanPostProcessor.class);

    }

    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        // 获得 @DubboComponentScan 注解
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(DubboComponentScan.class.getName()));
        // 获取配置的basePackages值
        String[] basePackages = attributes.getStringArray("basePackages");
        // 获取注解配置的basePackageClasses
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        // 获取注解配置的value值
        String[] value = attributes.getStringArray("value");
        // Appends value array attributes
        // 将配置的value添加到 packagesToScan 集合中
        Set<String> packagesToScan = new LinkedHashSet<String>(Arrays.asList(value));
        // 将配置的basePackages加到集合
        packagesToScan.addAll(Arrays.asList(basePackages));
        // 将配置的class加到 集合中
        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
        }
        // 如果没有配置
        if (packagesToScan.isEmpty()) {
            // 则默认获取注解类所在包
            return Collections.singleton(ClassUtils.getPackageName(metadata.getClassName()));
        }
        return packagesToScan;
    }

}
