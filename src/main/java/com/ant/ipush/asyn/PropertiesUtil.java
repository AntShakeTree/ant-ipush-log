package com.ant.ipush.asyn;

//import org.apache.logging.log4j.util.LoaderUtil;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public final class PropertiesUtil {

    private static PropertiesUtil LOG4J_PROPERTIES;

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);

    private final Properties props;


    /**
     * Constructs a PropertiesUtil using a given Properties object as its source of defined properties.
     *
     * @param props the Properties to use by default
     */
    public PropertiesUtil(final Properties props) {
        this.props = props;
    }


    /**
     * Constructs a PropertiesUtil for a given properties file name on the classpath. The properties specified in this
     * file are used by default. If a property is not defined in this file, then the equivalent system property is used.
     *
     * @param startWith the location of properties file to load
     */
    public PropertiesUtil(final String... startWith) {

        String[] names = startWithNames(startWith);
        @SuppressWarnings("IOResourceOpenedButNotSafelyClosed") final Properties properties = new Properties();


        for (String name : names) {
            pullAllProps(name, putAllYamlToProps(name, properties));
        }


        this.props = properties;
    }

    private void getYmlInputStream(String starwith, Class<PropertiesUtil> propertiesUtilClass, ClassLoader classLoader) {


    }


    /**
     * Loads and closes the given property input stream. If an error occurs, log to the status logger.
     *
     * @param in     a property input stream.
     * @param source a source object describing the source, like a resource string or a URL.
     * @return a new Properties object
     */
    static Properties loadClose(final InputStream in, final Object source) {
        final Properties props = new Properties();
        if (null != in) {
            try {
                props.load(in);
            } catch (final IOException e) {
                LOGGER.error("Unable to read {}", source, e);
            } finally {
                try {
                    in.close();
                } catch (final IOException e) {
                    LOGGER.error("Unable to close {}", source, e);
                }
            }
        }
        return props;
    }


    private static String[] startWithNames(String... starWith) {
        List<String> strings = new ArrayList<>();
        try {
            Collection<URL> urls = LoaderUtil.findResources("");
            for (URL url : urls) {
                File file = Paths.get(url.toURI()).toFile();
                String[] files = file.list((dir, name) -> (startWith(name, starWith)));
                for (String f : files) {
                    strings.add(f);
                }
            }
        } catch (Exception e) {
            String active = null;
            if (System.getProperty("spring.profiles.active") != null) {
                active = System.getProperty("spring.profiles.active");
            }
            for (String s : starWith) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("/").append(s);
                if (active != null) {
                    stringBuilder.append("-").append(active);
                }
                stringBuilder.append(".properties");
                strings.add(stringBuilder.toString());
            }
            for (String s : starWith) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("/").append(s);
                if (active != null) {
                    stringBuilder.append("-").append(active);
                }
                stringBuilder.append(".yml");
                strings.add(stringBuilder.toString());
            }
            return strings.toArray(new String[]{});
        }
        return strings.toArray(new String[]{});
    }


    static InputStream getInputStream(String path, Class clazz, ClassLoader classLoader) throws IOException {
        InputStream is;
        if (clazz != null) {
            is = clazz.getResourceAsStream(path);
        } else if (classLoader != null) {
            is = classLoader.getResourceAsStream(path);
        } else {
            is = ClassLoader.getSystemResourceAsStream(path);
        }
        if (is == null) {
            return null;
        }
        return is;
    }


    private static boolean startWith(String name, String... startWiths) {

        for (String s : startWiths) {
            if (System.getProperty("spring.profiles.active") != null) {
                String starw = s + "-" + System.getProperty("spring.profiles.active");
                LOGGER.debug(starw);
                if (name.startsWith(starw)) {
                    return true;
                }
            } else {
                if (name.startsWith(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Object lock = new Object();

    /**
     * Returns the PropertiesUtil used by Log4j.
     *
     * @return the main Log4j PropertiesUtil instance.
     */
    public static PropertiesUtil getProperties() {
        if (LOG4J_PROPERTIES == null) {
            synchronized (lock) {
                LOG4J_PROPERTIES = new PropertiesUtil("ipush.asyn", "application");
                return LOG4J_PROPERTIES;
            }
        }
        return LOG4J_PROPERTIES;
    }

    /**
     * Gets the named property as a String.
     *
     * @param name the name of the property to look up
     * @return the String value of the property or {@code null} if undefined.
     */
    public String getStringProperty(final String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (final SecurityException ignored) {
            // Ignore
        }
        return prop == null ? props.getProperty(name) : prop;
    }

    /**
     * Gets the named property as an integer.
     *
     * @param name         the name of the property to look up
     * @param defaultValue the default value to use if the property is undefined
     * @return the parsed integer value of the property or {@code defaultValue} if it was undefined or could not be
     * parsed.
     */
    public int getIntegerProperty(final String name, final int defaultValue) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (final SecurityException ignored) {
            // Ignore
        }
        if (prop == null) {
            prop = props.getProperty(name);
        }
        if (prop != null) {
            try {
                return Integer.parseInt(prop);
            } catch (final Exception ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Gets the named property as a long.
     *
     * @param name         the name of the property to look up
     * @param defaultValue the default value to use if the property is undefined
     * @return the parsed long value of the property or {@code defaultValue} if it was undefined or could not be parsed.
     */
    public long getLongProperty(final String name, final long defaultValue) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (final SecurityException ignored) {
            // Ignore
        }
        if (prop == null) {
            prop = props.getProperty(name);
        }
        if (prop != null) {
            try {
                return Long.parseLong(prop);
            } catch (final Exception ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Gets the named property as a String.
     *
     * @param name         the name of the property to look up
     * @param defaultValue the default value to use if the property is undefined
     * @return the String value of the property or {@code defaultValue} if undefined.
     */
    public String getStringProperty(final String name, final String defaultValue) {
        final String prop = getStringProperty(name);
        return (prop == null) ? defaultValue : prop;
    }

    /**
     * Gets the named property as a boolean value. If the property matches the string {@code "true"} (case-insensitive),
     * then it is returned as the boolean value {@code true}. Any other non-{@code null} text in the property is
     * considered {@code false}.
     *
     * @param name the name of the property to look up
     * @return the boolean value of the property or {@code false} if undefined.
     */
    public boolean getBooleanProperty(final String name) {
        return getBooleanProperty(name, false);
    }

    /**
     * Gets the named property as a boolean value.
     *
     * @param name         the name of the property to look up
     * @param defaultValue the default value to use if the property is undefined
     * @return the boolean value of the property or {@code defaultValue} if undefined.
     */
    public boolean getBooleanProperty(final String name, final boolean defaultValue) {
        final String prop = getStringProperty(name);
        return (prop == null) ? defaultValue : "true".equalsIgnoreCase(prop);
    }

    /**
     * Return the system properties or an empty Properties object if an error occurs.
     *
     * @return The system properties.
     */
    public static Properties getSystemProperties() {
        try {
            return new Properties(System.getProperties());
        } catch (final SecurityException ex) {
            LOGGER.error("Unable to access system properties.", ex);
            // Sandboxed - can't read System Properties
            return new Properties();
        }
    }

    /**
     * Extracts properties that start with or are equals to the specific prefix and returns them in a new Properties
     * object with the prefix removed.
     *
     * @param properties The Properties to evaluate.
     * @param prefix     The prefix to extract.
     * @return The subset of properties.
     */
    public static Properties extractSubset(Properties properties, String prefix) {
        Properties subset = new Properties();

        if (prefix == null || prefix.length() == 0) {
            return subset;
        }

        String prefixToMatch = prefix.charAt(prefix.length() - 1) != '.' ? prefix + '.' : prefix;

        List<String> keys = new ArrayList<>();

        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(prefixToMatch)) {
                subset.setProperty(key.substring(prefixToMatch.length()), properties.getProperty(key));
                keys.add(key);
            }
        }
        for (String key : keys) {
            properties.remove(key);
        }

        return subset;
    }

    /**
     * Returns true if system properties tell us we are running on Windows.
     *
     * @return true if system properties tell us we are running on Windows.
     */
    public boolean isOsWindows() {
        return getStringProperty("os.name").startsWith("Windows");
    }


    private Properties putAllYamlToProps(String yaml, Properties properties) {
        int extensionIndex = yaml.lastIndexOf(".");
        if (extensionIndex == -1) {
            return properties;
        }
        if (!"yml".equals(yaml.substring(extensionIndex + 1))) {
            return properties;
        }
        Yaml yaml1 = new Yaml();
        try {
            for (ClassLoader classLoader : LoaderUtil.findAllClassloader()) {
                if (classLoader != null) {
                    InputStream inputStream = getInputStream(yaml, PropertiesUtil.class, classLoader);
                    YmlUtil ymlUtil = new YmlUtil(new HashMap<>());
                    if(inputStream != null) {
                        ymlUtil.switchToMap(null, yaml1.load(inputStream));
                    }
                    properties.putAll(ymlUtil.getYmlMap());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;

    }

    private Properties pullAllProps(String name, Properties properties) {
        int extensionIndex = name.lastIndexOf(".");
        if (extensionIndex == -1) {
            return properties;
        }

        if (!"properties".equals(name.substring(extensionIndex + 1))) {
            return properties;
        }
        for (ClassLoader classLoader : LoaderUtil.findAllClassloader()) {
            InputStream in = null;
            try {
                in = getInputStream(name, PropertiesUtil.class, classLoader);
                if(in != null) {
                    properties.load(in);
                }
            } catch (final IOException ioe) {
                LOGGER.error("Unable to read {}", name, ioe);
            }
        }


        return properties;
    }

    static class YmlUtil {
        @Getter
        private final Map<String, Object> ymlMap;

        YmlUtil(Map<String, Object> ymlMap) {
            this.ymlMap = ymlMap;
        }

        @SuppressWarnings("unchecked")
        void switchToMap(String myKey, Map<String, Object> map) {
            Iterator<String> it = map.keySet().iterator();
            myKey = myKey == null ? "" : myKey;
            String tmpkey = myKey;
            while (it.hasNext()) {
                String key = it.next();
                myKey = tmpkey + key;
                Object value = map.get(key);
                if (value instanceof Map) {
                    switchToMap(myKey.concat("."), (Map<String, Object>) value);
                } else {
                    if (null != value) {
                        ymlMap.put(myKey, value);
                    }
                }
            }

        }

    }

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "local");
        System.out.println(PropertiesUtil.getProperties().getStringProperty("AsyncLogger.RingBufferSize"));
    }

}