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

import org.apache.commons.javaflow.spi.ResourceLoader;
import org.apache.commons.javaflow.spi.ResourceTransformer;

public class Asm5ResourceTransformationFactory extends AbstractResourceTransformationFactory {

    protected ResourceTransformer createTransformer(ResourceLoader resourceLoader,
                                                    ContinuableClassInfoResolver resolver,
                                                    ClassHierarchy classHierarchy) {
        
        return new ContinuableClassTransformer(classHierarchy, (IContinuableClassInfoResolver)resolver);
    }

}