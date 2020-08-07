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
package org.apache.dubbo.config;

import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.support.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.dubbo.common.constants.CommonConstants.EXPORTER_LISTENER_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.SERVICE_FILTER_KEY;

/**
 * AbstractServiceConfig
 *
 * @export
 */
public abstract class AbstractServiceConfig extends AbstractInterfaceConfig {

    private static final long serialVersionUID = 1L;

    /**
     * The service version
     *
     * 服务版本，通常在不兼容的情况下需要使用到
     */
    protected String version;

    /**
     * The service group
     *
     * 服务分组，当一个接口有多个实现，可以用分组区分
     */
    protected String group;

    /**
     * whether the service is deprecated
     *
     * 服务是否过时，如果为true，消费方引用时将打印服务过时警告
     */
    protected Boolean deprecated = false;

    /**
     * The time delay register service (milliseconds)
     *
     * 延迟注册，默认是0，也就是不延迟，如果设置为-1，那么表示延迟到Spring容器初始化完成再暴露服务
     */
    protected Integer delay;

    /**
     * Whether to export the service
     */
    protected Boolean export;

    /**
     * The service weight
     *
     * 服务权重
     */
    protected Integer weight;

    /**
     * Document center
     *
     * 服务文档URL
     */
    protected String document;

    /**
     * Whether to register as a dynamic service or not on register center, the value is true, the status will be enabled
     * after the service registered,and it needs to be disabled manually; if you want to disable the service, you also need
     * manual processing
     *
     * 服务是否动态注册，默认为true，如果为false，注册后显示disable状态，需要人工启用禁用
     */
    protected Boolean dynamic = true;

    /**
     * Whether to use token
     * 是否开启令牌验证，默认为false，作用是为了防止消费者绕过注册中心直接访问，因为开启以后只有注册中心具有授权token的功能
     */
    protected String token;

    /**
     * Whether to export access logs to logs
     *
     * 是否在logger中输出访问日志，默认为false
     */
    protected String accesslog;

    /**
     * The protocol list the service will export with
     * Also see {@link #protocolIds}, only one of them will work.
     *
     * 使用指定的协议暴露服务，在多协议时使用，值为<dubbo:protocol>的id属性，多个协议ID用逗号分隔
     */
    protected List<ProtocolConfig> protocols;

    /**
     * The id list of protocols the service will export with
     * Also see {@link #protocols}, only one of them will work.
     */
    protected String protocolIds;

    // max allowed execute times
    /**
     * 服务提供者每服务每方法最大可并行执行请求数	默认为0
     */
    private Integer executes;

    /**
     * Whether to register
     *是否向注册中心注册，多个注册中心ID使用逗号隔开
     *
     */
    private Boolean register;

    /**
     * Warm up period
     */
    private Integer warmup;

    /**
     * The serialization type
     */
    private String serialization;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Boolean getExport() {
        return export;
    }

    public void setExport(Boolean export) {
        this.export = export;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Parameter(escaped = true)
    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getToken() {
        return token;
    }

    public void setToken(Boolean token) {
        if (token == null) {
            setToken((String) null);
        } else {
            setToken(String.valueOf(token));
        }
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
    }

    public List<ProtocolConfig> getProtocols() {
        return protocols;
    }

    @SuppressWarnings({"unchecked"})
    public void setProtocols(List<? extends ProtocolConfig> protocols) {
        this.protocols = (List<ProtocolConfig>) protocols;
    }

    public ProtocolConfig getProtocol() {
        return CollectionUtils.isEmpty(protocols) ? null : protocols.get(0);
    }

    public void setProtocol(ProtocolConfig protocol) {
        setProtocols(new ArrayList<>(Arrays.asList(protocol)));
    }

    @Parameter(excluded = true)
    public String getProtocolIds() {
        return protocolIds;
    }

    public void setProtocolIds(String protocolIds) {
        this.protocolIds = protocolIds;
    }

    public String getAccesslog() {
        return accesslog;
    }

    public void setAccesslog(Boolean accesslog) {
        if (accesslog == null) {
            setAccesslog((String) null);
        } else {
            setAccesslog(String.valueOf(accesslog));
        }
    }

    public void setAccesslog(String accesslog) {
        this.accesslog = accesslog;
    }

    public Integer getExecutes() {
        return executes;
    }

    public void setExecutes(Integer executes) {
        this.executes = executes;
    }

    @Override
    @Parameter(key = SERVICE_FILTER_KEY, append = true)
    public String getFilter() {
        return super.getFilter();
    }

    @Override
    @Parameter(key = EXPORTER_LISTENER_KEY, append = true)
    public String getListener() {
        return listener;
    }

    @Override
    public void setListener(String listener) {
        this.listener = listener;
    }

    public Boolean isRegister() {
        return register;
    }

    public void setRegister(Boolean register) {
        this.register = register;
    }

    public Integer getWarmup() {
        return warmup;
    }

    public void setWarmup(Integer warmup) {
        this.warmup = warmup;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }
}
