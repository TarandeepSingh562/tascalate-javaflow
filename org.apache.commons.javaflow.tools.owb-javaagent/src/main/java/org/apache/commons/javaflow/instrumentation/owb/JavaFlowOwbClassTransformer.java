package org.apache.commons.javaflow.instrumentation.owb;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;

import org.apache.commons.javaflow.providers.asm5.Asm5ResourceTransformationFactory;
import org.apache.commons.javaflow.providers.asm5.ClassNameResolver;
import org.apache.commons.javaflow.spi.ContinuableClassInfoResolver;
import org.apache.commons.javaflow.spi.ExtendedClasspathResourceLoader;
import org.apache.commons.javaflow.spi.ResourceTransformationFactory;
import org.apache.commons.javaflow.spi.StopException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class JavaFlowOwbClassTransformer implements ClassFileTransformer {
    private static final Log log = LogFactory.getLog(JavaFlowOwbClassTransformer.class);

    private final ResourceTransformationFactory resourceTransformationFactory = new Asm5ResourceTransformationFactory();

    // @Override
    public byte[] transform(
            ClassLoader classLoader, 
            final String className, 
            final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain, 
            final byte[] classfileBuffer) throws IllegalClassFormatException {

        classLoader = getSafeClassLoader(classLoader);
        final ContinuableClassInfoResolver resolver = getCachedResolver(classLoader);
        synchronized (resolver) {
            final ClassNameResolver.Result currentTarget = ClassNameResolver.resolveClassName(className, classBeingRedefined, classfileBuffer);
            try {
                // Execute with current class as extra resource (in-memory)
                // Mandatory for Java8 lambdas and alike
                return ExtendedClasspathResourceLoader.runWithInMemoryResources(
                    new Callable<byte[]>() {
                        public byte[] call() {
                            ClassReader reader = new ClassReader(classfileBuffer);
                            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
                            reader.accept(new OwbProxyClassAdapter(writer, resolver), ClassReader.EXPAND_FRAMES);
                            return writer.toByteArray();
                        }
                    }, 
                    currentTarget.asResource()
                );
            } catch (StopException ex) {
                return null;
            } catch (RuntimeException ex) {
                log.error(ex);
                return null;
            } catch (Error ex) {
                log.error(ex);
                throw ex;
            }
        }
    }

    protected ClassLoader getSafeClassLoader(ClassLoader classLoader) {
        return null != classLoader ? classLoader : ClassLoader.getSystemClassLoader();
    }

    protected ContinuableClassInfoResolver getCachedResolver(ClassLoader classLoader) {
        synchronized (classLoader2resolver) {
            ContinuableClassInfoResolver cachedResolver = classLoader2resolver.get(classLoader);
            if (null == cachedResolver) {
                ContinuableClassInfoResolver newResolver = resourceTransformationFactory
                        .createResolver(new ExtendedClasspathResourceLoader(classLoader));
                classLoader2resolver.put(classLoader, newResolver);
                return newResolver;
            } else {
                return cachedResolver;
            }
        }
    }

    private static final Map<ClassLoader, ContinuableClassInfoResolver> classLoader2resolver = new WeakHashMap<ClassLoader, ContinuableClassInfoResolver>();
}
