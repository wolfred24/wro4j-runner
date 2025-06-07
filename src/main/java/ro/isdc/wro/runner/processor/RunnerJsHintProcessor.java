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

  public RunnerJsHintProcessor() {
    super();
  }

  @Override
  protected void onLinterException(final LinterException e, final Resource resource) {
    super.onLinterException(e, resource);
    System.err.println("The following resource: " + resource + " has " + e.getErrors().size() + " errors.");
    System.err.println(e.getErrors());
    throw e;
  }

  private File getResourceFile(Resource resource) {
    if (resource == null || resource.getUri() == null) {
      return null;
    }
    File file = new File(resource.getUri());
    if (file.exists()) {
      System.out.println("[JSHint][DEBUG] getResourceFile: Encontrado por ruta directa: " + file.getAbsolutePath());
      return file;
    }
    String contextFolder = System.getProperty("contextFolder");
    if (contextFolder != null) {
      String relativePath = resource.getUri().replaceFirst("^/", "");
      file = new File(contextFolder, relativePath);
      if (file.exists()) {
        System.out.println("[JSHint][DEBUG] getResourceFile: Encontrado relativo a contextFolder: " + file.getAbsolutePath());
        return file;
      }
    } else {
      System.out.println("[JSHint][DEBUG] contextFolder no definido en System properties");
    }
    System.out.println("[JSHint][DEBUG] getResourceFile: No se encontró archivo para resource.getUri(): " + resource.getUri());
    return null;
  }

  /**
   * Busca .jshintrc desde el directorio inicial hacia arriba hasta contextFolder o la raíz.
   */
  private Map<String, Object> findAndLoadJshintrc(File startDir) {
    File dir = startDir.getAbsoluteFile(); // <-- Asegura que sea absoluto
    while (dir != null) {
      File jshintrc = new File(dir, ".jshintrc");
      System.out.println("[JSHint][DEBUG] Revisando: " + jshintrc.getAbsolutePath());
      if (jshintrc.exists()) {
        FileReader reader = null;
        try {
          reader = new FileReader(jshintrc);
          Gson gson = new Gson();
          Type type = new TypeToken<Map<String, Object>>(){}.getType();
          System.out.println("[JSHint][DEBUG] .jshintrc encontrado y cargado: " + jshintrc.getAbsolutePath());
          return gson.fromJson(reader, type);
        } catch (IOException e) {
          System.err.println("No se pudo leer .jshintrc en " + jshintrc.getAbsolutePath() + ": " + e.getMessage());
          break;
        } finally {
          if (reader != null) {
            try { reader.close(); } catch (IOException ignore) {}
          }
        }
      }
      dir = dir.getParentFile();
      System.out.println("[JSHint][DEBUG] Subiendo a directorio padre: " + (dir != null ? dir.getAbsolutePath() : "null"));
    }
    return null;
  }

  // Convierte el Map de opciones a CSV: key=value,key2=value2
  private String mapToCsvOptions(Map<String, Object> map) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      if (sb.length() > 0) sb.append(",");
      sb.append(entry.getKey()).append("=").append(entry.getValue());
    }
    return sb.toString();
  }

  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    System.out.println("[JSHint][DEBUG] Iniciando process para recurso: " + (resource != null ? resource.getUri() : "null"));
    final String content = org.apache.commons.io.IOUtils.toString(reader);
    final AbstractLinter linter = newLinter();
    try {
      File resourceFile = getResourceFile(resource);
      System.out.println("[JSHint][DEBUG] resourceFile: " + (resourceFile != null ? resourceFile.getAbsolutePath() : "null"));
      String options = null;
      File searchDir = null;
      if (resourceFile != null && resourceFile.exists()) {
        searchDir = resourceFile.getParentFile();
      } else {
        String contextFolder = System.getProperty("contextFolder");
        if (contextFolder != null) {
          searchDir = new File(contextFolder).getAbsoluteFile(); // <-- AQUÍ
        }
      }
      if (searchDir != null) {
        System.out.println("[JSHint][DEBUG] Buscando .jshintrc desde: " + searchDir.getAbsolutePath());
        Map<String, Object> optionsMap = findAndLoadJshintrc(searchDir);
        if (optionsMap != null) {
          options = mapToCsvOptions(optionsMap);
          System.out.println("[JSHint] Usando configuración de .jshintrc encontrada en jerarquía.");
          System.out.println("[JSHint][DEBUG] Opciones CSV: " + options);
        } else {
          System.out.println("[JSHint] No se encontró .jshintrc en la jerarquía para: " + searchDir.getAbsolutePath());
        }
      } else {
        System.out.println("[JSHint][DEBUG] searchDir es null, no se buscará .jshintrc");
      }
      if (options == null) {
        options = createDefaultOptions();
        System.out.println("[JSHint][DEBUG] Usando opciones por defecto: " + options);
      }
      linter.setOptions(options).validate(content);
    } catch (final LinterException e) {
      System.out.println("[JSHint][DEBUG] LinterException capturada: " + e.getMessage());
      onLinterException(e, resource);
    } catch (final ro.isdc.wro.WroRuntimeException e) {
      System.out.println("[JSHint][DEBUG] WroRuntimeException capturada: " + e.getMessage());
      onException(e);
      final String resourceUri = resource == null ? "" : "[" + resource.getUri() + "]";
      System.err.println("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...");
    } finally {
      writer.write(content);
      reader.close();
      writer.close();
      System.out.println("[JSHint][DEBUG] Finalizó process para recurso: " + (resource != null ? resource.getUri() : "null"));
    }
  }
}