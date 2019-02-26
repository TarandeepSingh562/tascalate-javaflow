/**
 * ﻿Copyright 2013-2019 Valery Silaev (http://vsilaev.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.javaflow.providers.asm5;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

class MaybeContinuableAnnotationVisitor extends ClassVisitor {
    private final ContinuableClassInfoResolver cciResolver; 
    private boolean classContinuableAnnotationFound = false;
    private boolean isAnnotation = false;

    MaybeContinuableAnnotationVisitor(ContinuableClassInfoResolver cciResolver) {
        super(AsmVersion.CURRENT);
        this.cciResolver = cciResolver;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        isAnnotation = (access & Opcodes.ACC_ANNOTATION) > 0;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String description, boolean visible) {
        if (isAnnotation && !classContinuableAnnotationFound) {
            classContinuableAnnotationFound = cciResolver.isContinuableAnnotation(description);
        }
        return null;
    }

    boolean isContinuable() { 
        return classContinuableAnnotationFound && isAnnotation; 
    }
}