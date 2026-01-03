package com.github.apiscan.util;

import com.github.apiscan.entity.AnnotationInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnnotationUtils {
    private static final String ANNOTATION_PACKAGE = "java.lang.annotation.";

    public static List<AnnotationInfo> getAnnotations(Annotation[] annotations) {
        return getAnnotations(annotations, false);
    }

    public static List<AnnotationInfo> getAnnotations(Annotation[] annotations, boolean expand) {
        if (annotations == null) {
            annotations = new Annotation[0];
        }
        List<AnnotationInfo> annotationInfos = new ArrayList<>();
        if (expand) {
            List<Annotation> allAnnotation = new ArrayList<>();
            doGetAnnotation(annotations, allAnnotation);
            for (Annotation annotation : allAnnotation) {
                annotationInfos.add(getAnnotationProperties(annotation));
            }
        } else {
            for (Annotation annotation : annotations) {
                annotationInfos.add(getAnnotationProperties(annotation));
            }
        }
        return annotationInfos;
    }

    private static void doGetAnnotation(Annotation[] annotations, List<Annotation> allAnnotation) {
        for (Annotation annotation : annotations) {
            allAnnotation.add(annotation);
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType.getName().startsWith(ANNOTATION_PACKAGE)) {
                continue;
            }
            doGetAnnotation(annotationType.getAnnotations(), allAnnotation);
        }
    }

    public static AnnotationInfo getAnnotationProperties(Annotation annotation) {
        if (annotation == null) {
            return null;
        }
        Class<? extends Annotation> annotationType = annotation.annotationType();
        AnnotationInfo annotationInfo = AnnotationInfo.builder()
                .annotation(annotation)
                .name(annotationType.getName())
                .simpleName(annotationType.getSimpleName())
                .properties(new HashMap<>())
                .build();
        Method[] declaredMethods = annotationType.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            try {
                Object invoke = declaredMethod.invoke(annotation);
                annotationInfo.getProperties().put(declaredMethod.getName(), invoke);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new RuntimeException(exception);
            }
        }
        return annotationInfo;
    }
}
