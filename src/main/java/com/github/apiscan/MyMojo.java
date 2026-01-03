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

import com.alibaba.fastjson2.JSON;
import com.github.apiscan.constant.JavaType;
import com.github.apiscan.entity.ApiInfo;
import com.github.apiscan.util.FileUtils;
import com.github.apiscan.util.ApiParser;
import com.github.apiscan.util.LogUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
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
public class MyMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}")
    public MavenProject project;

    public void execute() throws MojoExecutionException {
        try {
            LogUtil.setLog(getLog());


            List<String> elements = project.getCompileClasspathElements();

            for (String compileClasspathElement : elements) {
                System.out.println("==>" + compileClasspathElement);
            }

            URL[] urls = new URL[elements.size()];
            int index = 0;
            for (String element : elements) {
                urls[index++] = new File(element).toURI().toURL();
            }
            URLClassLoader projectClassLoader = new URLClassLoader(urls, this.getClass().getClassLoader());

            JavaType.setBeanUtils(projectClassLoader);

            List<File> files = FileUtils.scanClassFile(project.getBuild().getOutputDirectory());
            URL[] CURurls = files.stream().map(file -> {
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }).toList().toArray(new URL[0]);

            List<String> beanNames = new ArrayList<>();
            File base = new File(project.getBuild().getOutputDirectory());
            System.out.println("base outputdir:" + project.getBuild().getOutputDirectory());
            URI baseURI = base.toURI();
            for (URL url : CURurls) {
                beanNames.add(baseURI.relativize(url.toURI()).getPath().replace('\\', '/').replaceAll("[/]+", "/").replaceAll("/", ".").replace(".class", ""));
            }
            List<ApiInfo> apis = new ArrayList<>();
            for (String beanName : beanNames) {
                System.out.println(beanName);
                Class<?> clazz = projectClassLoader.loadClass(beanName);
                apis.addAll(ApiParser.findUrls(clazz));
            }
            apis.sort(Comparator.comparing(ApiInfo::getUrl));

            System.out.println(JSON.toJSONString(apis));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
