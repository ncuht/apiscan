package com.github.apiscan.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CircularReferenceDetect {
    private Boolean enableRequestParams;

    private Boolean enableRequestBody;

    private Boolean enableResponse;
}
