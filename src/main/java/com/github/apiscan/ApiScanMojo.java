package com.github.apiscan;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.apiscan.constant.JavaType;
import com.github.apiscan.entity.ApiInfo;
import com.github.apiscan.entity.BaseInfo;
import com.github.apiscan.entity.CircularReferenceDetect;
import com.github.apiscan.util.ApiParser;
import com.github.apiscan.util.FileUtils;
import com.github.apiscan.util.LogUtils;
import com.github.apiscan.util.MarkdownWriter;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Goal which touches a timestamp file.
 *
 * @requiresDependencyResolution compile
 * @goal touch
 * @phase process-sources
 */

@Mojo(name = "scan", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class ApiScanMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}")
    public MavenProject project;

    public void execute() throws MojoExecutionException {
        try {
            BaseInfo baseInfo = init();

            List<ApiInfo> apis = new ArrayList<>();
            for (String beanName : baseInfo.getBeanNames()) {
                Class<?> clazz = baseInfo.getUrlClassLoader().loadClass(beanName);
                apis.addAll(ApiParser.findUrls(clazz, baseInfo.getDetect()));
            }
            apis.sort(Comparator.comparing(ApiInfo::getUrl));
            MarkdownWriter.write(apis, baseInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BaseInfo init() throws DependencyResolutionRequiredException, MalformedURLException {
        // 设置日志
        LogUtils.setLog(getLog());
        // 加载所有涉及类
        List<String> elements = project.getCompileClasspathElements();
        URL[] urls = new URL[elements.size()];
        for (int index = 0; index < elements.size(); index++) {
            urls[index] = new File(elements.get(index)).toURI().toURL();
        }
        URLClassLoader projectClassLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
        // 获取Spring的BeanUtils
        JavaType.setBeanUtils(projectClassLoader);
        // 获取项目BeanName
        List<File> files = FileUtils.scanClassFile(project.getBuild().getOutputDirectory());
        URI baseURI = new File(project.getBuild().getOutputDirectory()).toURI();
        List<String> beanNames = new ArrayList<>();
        for (File file : files) {
            beanNames.add(baseURI.relativize(file.toURI())
                    .getPath()
                    .replace('\\', '/')
                    .replaceAll("[/]+", "/")
                    .replaceAll("/", ".")
                    .replace(".class", ""));
        }

        CircularReferenceDetect detect = new CircularReferenceDetect();
        detect.setEnableResponse(false);
        detect.setEnableRequestParams(false);
        detect.setEnableRequestBody(false);

        return BaseInfo.builder()
                .groupId(project.getGroupId())
                .artifactId(project.getArtifactId())
                .version(project.getVersion())
                .basePath(project.getBasedir().getPath())
                .outputFilePath(project.getBasedir().getPath() + "/API文档.md")
                .urlClassLoader(projectClassLoader)
                .beanNames(beanNames)
                .detect(detect)
                .build();
    }
}
