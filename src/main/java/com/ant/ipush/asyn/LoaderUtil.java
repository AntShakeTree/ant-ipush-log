package com.ant.ipush.asyn;

//import org.apache.logging.log4j.util.PropertiesUtil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Objects;

public final class LoaderUtil {
    private static final PrivilegedAction<ClassLoader> TCCL_GETTER = new ThreadContextClassLoaderGetter();
    private static Boolean ignoreTCCL;
    private static final boolean GET_CLASS_LOADER_DISABLED;
    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();

    private Class<?> clazz;
    private LoaderUtil() {
    }

    static {
        if (SECURITY_MANAGER != null) {
            boolean getClassLoaderDisabled;
            try {
                SECURITY_MANAGER.checkPermission(new RuntimePermission("getClassLoader"));
                getClassLoaderDisabled = false;
            } catch (final SecurityException ignored) {
                getClassLoaderDisabled = true;
            }
            GET_CLASS_LOADER_DISABLED = getClassLoaderDisabled;
        } else {
            GET_CLASS_LOADER_DISABLED = false;
        }
    }

    public static Collection<URL> findResources(final String resource) {
        final Collection<UrlResource> urlResources = findUrlResources(resource);
        final Collection<URL> resources = new LinkedHashSet<>(urlResources.size());
        for (final UrlResource urlResource : urlResources) {
            resources.add(urlResource.getUrl());
        }
        return resources;
    }

    public static ClassLoader[] findAllClassloader() {
        final ClassLoader[] candidates = {getThreadContextClassLoader(), LoaderUtil.class.getClassLoader(),
                GET_CLASS_LOADER_DISABLED ? null : ClassLoader.getSystemClassLoader()};
        return candidates;
    }

    static Collection<UrlResource> findUrlResources(final String resource) {
        final ClassLoader[] candidates = {getThreadContextClassLoader(), LoaderUtil.class.getClassLoader(),
                GET_CLASS_LOADER_DISABLED ? null : ClassLoader.getSystemClassLoader()};
        final Collection<UrlResource> resources = new LinkedHashSet<>();
        for (final ClassLoader cl : candidates) {
            if (cl != null) {
                try {
                    final Enumeration<URL> resourceEnum = cl.getResources(resource);
                    while (resourceEnum.hasMoreElements()) {
                        resources.add(new UrlResource(cl, resourceEnum.nextElement()));
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return resources;
    }

    public static ClassLoader getThreadContextClassLoader() {
        if (GET_CLASS_LOADER_DISABLED) {
            // we can at least get this class's ClassLoader regardless of security context
            // however, if this is null, there's really no option left at this point
            return LoaderUtil.class.getClassLoader();
        }
        return SECURITY_MANAGER == null ? TCCL_GETTER.run() : AccessController.doPrivileged(TCCL_GETTER);
    }




    /**
     * {@link URL} and {@link ClassLoader} pair.
     */
    static class UrlResource {
        private final ClassLoader classLoader;
        private final URL url;

        public UrlResource(final ClassLoader classLoader, final URL url) {
            this.classLoader = classLoader;
            this.url = url;
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        public URL getUrl() {
            return url;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final UrlResource that = (UrlResource) o;

            if (classLoader != null ? !classLoader.equals(that.classLoader) : that.classLoader != null) {
                return false;
            }
            if (url != null ? !url.equals(that.url) : that.url != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(classLoader) + Objects.hashCode(url);
        }
    }


    private static class ThreadContextClassLoaderGetter implements PrivilegedAction<ClassLoader> {
        @Override
        public ClassLoader run() {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                return cl;
            }
            final ClassLoader ccl = LoaderUtil.class.getClassLoader();
            return ccl == null && !GET_CLASS_LOADER_DISABLED ? ClassLoader.getSystemClassLoader() : ccl;
        }
    }


    public static void main(String[] args) throws URISyntaxException {
        Collection<URL> urls = LoaderUtil.findResources("");
        for (URL url : urls) {
            File file = Paths.get(url.toURI()).toFile();
            System.out.println(file.listFiles());
            String[] files=file.list((dir, name) -> {
                if (name.startsWith("ipush")){
                    return true;
                }
                return false;
            });

            for (String f:files){
                System.out.println(f);
            }



        }
    }

//    private static boolean isIgnoreTccl() {
//        // we need to lazily initialize this, but concurrent access is not an issue
//        if (ignoreTCCL == null) {
//            final String ignoreTccl = PropertiesUtil.getProperties().getStringProperty(IGNORE_TCCL_PROPERTY, null);
//            ignoreTCCL = ignoreTccl != null && !"false".equalsIgnoreCase(ignoreTccl.trim());
//        }
//        return ignoreTCCL;
//    }
}
