package com.github.apiscan.util;

import com.github.apiscan.entity.ApiInfo;
import com.github.apiscan.entity.BaseInfo;
import com.github.apiscan.entity.ParamInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MarkdownWriter {
    private static String LS = System.lineSeparator();

    private static char SPACE = ' ';

    private static String DOT = ". ";

    private static String SEPARATOR = LS + LS + "---" + LS + LS;

    private static String EMPTY_OBJECT = JsonUtils.toPrettyString(new Object());

    public static void write(List<ApiInfo> apis, BaseInfo baseInfo) {
        StringBuilder sb = new StringBuilder();
        createBaseInfo(sb, baseInfo);
        Map<String, List<ApiInfo>> locations = groupByLocation(apis);
        int titleIndex = 1;
        for (Map.Entry<String, List<ApiInfo>> entry : locations.entrySet()) {
            String currentIndex = titleIndex + DOT;
            createTitle(sb, entry.getKey(), currentIndex, 2);
            List<ApiInfo> value = entry.getValue();
            for (int index = 0; index < value.size(); index++) {
                ApiInfo apiInfo = value.get(index);
                String secondTitleIndex = currentIndex + (index + 1) + DOT;
                createTitle(sb, apiInfo.getUrl(), secondTitleIndex, 3);
                createMethod(sb, apiInfo.getHttpMethods());
                createParams(sb, apiInfo.getRequestParams());
                createRequestBody(sb, apiInfo.getBodyText());
                createResponse(sb, apiInfo.getResponseText());
            }
            titleIndex++;
        }
        FileUtils.write(baseInfo.getOutputFilePath(), sb.toString());
    }

    private static void createBaseInfo(StringBuilder sb, BaseInfo baseInfo) {
        createTitle(sb, "接口文档", "-1", 1);
        sb.append("**groupId:** ").append(baseInfo.getGroupId()).append(LS).append(LS)
                .append("**artifactId:** ").append(baseInfo.getArtifactId()).append(LS).append(LS)
                .append("**version:** ").append(baseInfo.getVersion()).append(LS).append(LS)
                .append(SEPARATOR);
    }

    private static void createResponse(StringBuilder sb, String response) {
        sb.append("**Response:").append("**").append(LS).append(LS)
                .append("```").append(LS)
                .append(StringUtils.isBlank(response) ? "" : response).append(LS)
                .append("```").append(LS).append(LS);
    }

    private static void createRequestBody(StringBuilder sb, String body) {
        if (StringUtils.isBlank(body) || EMPTY_OBJECT.equals(body)) {
            return;
        }
        sb.append("**Request Body:").append("**").append(LS).append(LS)
                .append("```").append(LS)
                .append(body).append(LS)
                .append("```").append(LS).append(LS);
    }

    private static void createParams(StringBuilder sb, List<ParamInfo> requestParams) {
        if (requestParams == null || requestParams.isEmpty()) {
            return;
        }
        sb.append("**Request Params:").append("**").append(LS).append(LS);
        sb.append("| 参数名 | java类型 | 是否必填 | 默认值 |").append(LS);
        sb.append("| --- | --- | --- | --- |").append(LS);
        for (ParamInfo requestParam : requestParams) {
            sb.append("| ").append(requestParam.getName())
                    .append(" | ").append(requestParam.getJavaType())
                    .append(" | ").append(requestParam.getRequired())
                    .append(" | ").append(requestParam.getDefaultValue() == null ? "" : requestParam.getDefaultValue())
                    .append(" |").append(LS);
        }
        sb.append(LS);
    }

    private static void createMethod(StringBuilder sb, Set<String> methods) {
        sb.append("**Method: ").append(String.join(", ", methods)).append("**").append(LS).append(LS);
    }

    private static void createTitle(StringBuilder sb, String title, String index, int level) {
        while (level-- > 0) {
            sb.append('#');
        }
        sb.append(SPACE).append(index.startsWith("-") ? "" : index).append(title).append(LS).append(LS);
    }

    private static Map<String, List<ApiInfo>> groupByLocation(List<ApiInfo> apis) {
        Map<String, List<ApiInfo>> locations = apis.stream()
                .collect(Collectors.groupingBy(ApiInfo::getFile,
                        TreeMap::new,
                        Collectors.toList()));
        locations.forEach((k, v) -> v.sort(Comparator.comparing(ApiInfo::getUrl)));
        return locations;
    }
}
