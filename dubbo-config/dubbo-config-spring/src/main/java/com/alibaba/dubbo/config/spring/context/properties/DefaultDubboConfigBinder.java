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
package com.alibaba.dubbo.config.spring.context.properties;

import com.alibaba.dubbo.config.AbstractConfig;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;

import java.util.Map;

import static com.alibaba.dubbo.config.spring.util.PropertySourcesUtils.getSubProperties;

/**
 * Default {@link DubboConfigBinder} implementation based on Spring {@link DataBinder}
 */
public class DefaultDubboConfigBinder extends AbstractDubboConfigBinder {

    @Override
    public <C extends AbstractConfig> void bind(String prefix, C dubboConfig) {
        DataBinder dataBinder = new DataBinder(dubboConfig);
        // Set ignored*
        dataBinder.setIgnoreInvalidFields(isIgnoreInvalidFields());
        dataBinder.setIgnoreUnknownFields(isIgnoreUnknownFields());
        // Get properties under specified prefix from PropertySources
        // 获得指定前缀的配置信息 如 dubbo.application.name=aaa  则返回key=name  value=aaa
        Map<String, Object> properties = getSubProperties(getPropertySources(), prefix);
        // Convert Map to MutablePropertyValues
        // 用于将配置绑定
        MutablePropertyValues propertyValues = new MutablePropertyValues(properties);
        // Bind
        // 这个时候propertie配置的信息 将绑定到config对象里面
        dataBinder.bind(propertyValues);
    }

}
