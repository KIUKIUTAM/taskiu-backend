// com/tavinki/taskiu/common/annotations/attribute/TeamId.java
package com.tavinki.taskiu.common.annotations.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface TeamId {
}
