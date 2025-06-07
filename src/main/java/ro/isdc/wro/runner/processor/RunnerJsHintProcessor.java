package ro.isdc.wro.runner.processor;

import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.AbstractLinter;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.model.resource.Resource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class RunnerJsHintProcessor extends JsHintProcessor {
    public static String ALIAS = JsHintProcessor.ALIAS;
    private final File contextFolder; // <-- AGREGA ESTA LÍNEA

    public RunnerJsHintProcessor(File contextFolder) {
        super();
        this.contextFolder = contextFolder;
    }

    public RunnerJsHintProcessor() {
        this(new File(System.getProperty("user.dir")));
    }

    @Override
    protected void onLinterException(final LinterException e, final Resource resource) {
        // super.onLinterException(e, resource);
        System.out.println("The following resource: " + resource + " has " + e.getErrors().size() + " errors.");
        for (Object err : e.getErrors()) {
            String errStr = err.toString();
            if (errStr.contains("reason")) {
                System.out.println(err);
            } else {
                System.err.println(err);
            }
        }

        // Detecta si estamos en un entorno de test (JUnit/Surefire)
        boolean inTest = false;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            String cls = ste.getClassName();
            if (cls.startsWith("org.junit.") || cls.startsWith("org.apache.maven.surefire.")) {
                inTest = true;
                break;
            }
        }
        if (inTest) {
            throw e;
        }
    }

    private File getResourceFile(Resource resource) {
        if (resource == null || resource.getUri() == null) {
            return null;
        }
        File file = new File(resource.getUri());
        if (file.exists()) {
            return file;
        }
        if (this.contextFolder != null) {
            String relativePath = resource.getUri().replaceFirst("^/", "");
            file = new File(this.contextFolder, relativePath);
            if (file.exists()) {
                return file;
            }
        } else {
            System.out.println("[JSHint] contextFolder not defined in System properties");
        }
        System.out.println("[JSHint] getResourceFile: No file found for resource.getUri(): " + resource.getUri());
        return null;
    }

    /**
     * Busca .jshintrc desde el directorio inicial hacia arriba hasta contextFolder
     * o la raíz.
     */
    private Map<String, Object> findAndLoadJshintrc(File startDir) {
        File dir = startDir.getAbsoluteFile(); // <-- Asegura que sea absoluto
        while (dir != null) {
            File jshintrc = new File(dir, ".jshintrc");
            if (jshintrc.exists()) {
                FileReader reader = null;
                try {
                    reader = new FileReader(jshintrc);
                    Gson gson = new Gson();
                    Type type = new TypeToken<Map<String, Object>>() {
                    }.getType();
                    System.out.println("[JShint] .jshintrc loaded: " + jshintrc.getAbsolutePath());
                    return gson.fromJson(reader, type);
                } catch (IOException e) {
                    System.err.println("[JShint] No se pudo leer .jshintrc en " + jshintrc.getAbsolutePath() + ": "
                            + e.getMessage());
                    break;
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            }
            dir = dir.getParentFile();
        }
        return null;
    }

    // Convierte el Map de opciones a CSV: key=value,key2=value2
    private String mapToCsvOptions(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    @Override
    public void process(final Resource resource, final Reader reader, final Writer writer)
            throws IOException {
        String resourceUri = (resource != null ? resource.getUri() : "null");
        System.out.println("\n========== Processing resource: " + resourceUri + " ==========\n");
        final String content = org.apache.commons.io.IOUtils.toString(reader);
        final AbstractLinter linter = newLinter();
        try {
            File resourceFile = getResourceFile(resource);
            String options = null;
            File searchDir = null;
            if (resourceFile != null && resourceFile.exists()) {
                searchDir = resourceFile.getParentFile();
            } else {
                if (this.contextFolder != null) {
                    searchDir = this.contextFolder.getAbsoluteFile();
                }
            }
            if (searchDir != null) {
                Map<String, Object> optionsMap = findAndLoadJshintrc(searchDir);
                if (optionsMap != null) {
                    options = mapToCsvOptions(optionsMap);
                    System.out.println("[JShint] Using .jshintrc configuration in: " + searchDir.getAbsolutePath());
                    System.out.println("[JShint] Options: " + options);
                } else {
                    System.out.println("[JShint] No .jshintrc found in the hierarchy starting from: "
                            + searchDir.getAbsolutePath());
                }
            } else {
                System.out.println("[JShint] Could not determine the directory to search for .jshintrc");
            }
            if (options == null) {
                options = createDefaultOptions();
                System.out.println("[JShint] using default options: " + options);
            }
            linter.setOptions(options).validate(content);
        } catch (final LinterException e) {
            onLinterException(e, resource);
        } catch (final ro.isdc.wro.WroRuntimeException e) {
            System.err.println("WroRuntimeException in " + resourceUri + ": " + e.getMessage());
            onException(e);
            System.err.println(
                    "Exception while applying " + getClass().getSimpleName() + " processor on the [" + resourceUri
                            + "] resource, no processing applied...");
        } finally {
            writer.write(content);
            reader.close();
            writer.close();
            System.out.println("Finished processing: " + resourceUri);
        }
    }
}