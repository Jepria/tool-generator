package org.jepria.tools.generator.cli;

import org.jepria.tools.generator.core.Evaluator;
import org.jepria.tools.generator.core.parser.ApiSpecMethodExtractorJson;
import org.jepria.tools.generator.core.parser.SpecMethod;
import org.jepria.tools.generator.mustache_templates.client_react.crud.TemplateFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Defaults to generate crud client-react code from mustache sources only.
 * TODO add support for not only crud, not only client-react and not only mustache 
 */
public class Main {

  protected static String capitalize(String s) {
    if (s == null) {
      return null;
    } else {
      return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
  }

  /**
   *
   * @param field_name field-name to fieldName
   * @return
   */
  protected static String undashize(String field_name) {
    StringBuilder sb = new StringBuilder();
    boolean shift = false;
    for (int i = 0; i < field_name.length(); i++) {
      char c =  field_name.charAt(i);
      if (c == '-') {
        shift = true;
      } else {
        if (shift) {
          sb.append(Character.toUpperCase(c));
          shift = false;
        } else {
          sb.append(c);
        }
      }
    }
    return sb.toString();
  }

  /**
   *
   * @param fieldName fieldName to field-name
   * @return
   */
  protected static String dashize(String fieldName) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < fieldName.length(); i++) {
      char c =  fieldName.charAt(i);
      if (Character.isUpperCase(c)) {
        if (i > 0) {
          sb.append('-');
        }
        sb.append(Character.toLowerCase(c));
      } else if (Character.isLowerCase(c)) {
        sb.append(c);
      } else {
        throw new IllegalArgumentException("Neither lowercase nor uppercase letter [" + c + "] in the word \"" + fieldName + "\", at " + i);
      }
    }
    return sb.toString();
  }

  private static PrintStream out = System.out;
  
  public static void main(String[] args) throws IOException {

    if (args != null) {
      List<String> argList = Arrays.asList(args);

      final Runner runner;
      
      final List<String> apiSpecPaths = new ArrayList<>();
      String templateRootMst = null;
      String partialsRootMst = null;
      String outputRoot = null;
      
      // read cmd args
      for (int i = 0; i < argList.size(); i++) {

        if (argList.get(i).equals("--api-specs") && i < argList.size() - 1) {
          // coma separated list of absolute paths to the api spec (e.g. swagger.json) files that need to be matched
          i++;
          String apiSpecPathsArg = argList.get(i);
          apiSpecPaths.addAll(Arrays.asList(apiSpecPathsArg.split("\\s*;\\s*")));
        } else if (argList.get(i).equals("--template-root-mst") && i < argList.size() - 1) {
          // absolute path to the mustache template root folder
          i++;
          templateRootMst = argList.get(i);
        } else if (argList.get(i).equals("--partials-root-mst") && i < argList.size() - 1) {
          // absolute path to the mustache partials root folder
          i++;
          partialsRootMst = argList.get(i);
        } else if (argList.get(i).equals("--output-root") && i < argList.size() - 1) {
          // absolute path to the ouptput project root
          i++;
          outputRoot = argList.get(i);
        }
      }

      try {
        runner = new Runner(apiSpecPaths, templateRootMst, partialsRootMst, outputRoot);
      } catch (Runner.PrepareException e) {
        for (String message : e.getMessages()) {
          out.println(message);
        }
        return;
      }




      runner.run();

    } else {
      out.println("No arguments provided");
      return;
    }
      
    out.println("Done.");
  }

  protected static class Runner implements Runnable {

    final File apiSpec;
    final File templateRootMst;
    final File partialsRootMst;
    final File outputRootDir;

    final String entity_name_dash;
    final String entityName;
    final String entityId;
    final String EntityName;
    final String entityname;
    
    public static class PrepareException extends Exception {
      private final List<String> messages;

      public PrepareException(List<String> messages) {
        this.messages = messages;
      }

      public List<String> getMessages() {
        return messages;
      }
    }

    public Runner(List<String> apiSpecPaths,
                  String templateRootMstPath,
                  String partialsRootMstPath,
                  String outputRootDirPath) throws PrepareException {

      boolean failed = false;
      List<String> failMessages = new ArrayList<>();


      // TODO add support for multi-spec generation
      if (apiSpecPaths.size() > 1) {
        failed = true;
        failMessages.add("Multi-spec generation not supported yet, provide single spec file");
      }
      
      {
        File file = null;
        Path path = null;
        try {
          path = Paths.get(apiSpecPaths.get(0));
        } catch (Throwable e) {
          e.printStackTrace(); // TODO
          failed = true;
          failMessages.add("Incorrect file path [" + apiSpecPaths.get(0) + "]: resolve exception");
        }
        if (!Files.exists(path)) {
          failed = true;
          failMessages.add("Incorrect file path [" + apiSpecPaths.get(0) + "]: file does not exist");
        } else if (!Files.isRegularFile(path)) {
          failed = true;
          failMessages.add("Incorrect file path [" + apiSpecPaths.get(0) + "]: not a regular file");
        } else {
          file = path.toFile();
        }

        apiSpec = file;
      }
      

      {
        File file = null;
        Path path = null;
        try {
          path = Paths.get(templateRootMstPath);
        } catch (Throwable e) {
          e.printStackTrace(); // TODO
          failed = true;
          failMessages.add("Incorrect file path [" + templateRootMstPath + "]: resolve exception");
        }
        if (!Files.exists(path)) {
          failed = true;
          failMessages.add("Incorrect file path [" + templateRootMstPath + "]: file does not exist");
        } else if (!Files.isDirectory(path)) {
          failed = true;
          failMessages.add("Incorrect file path [" + templateRootMstPath + "]: not a directory");
        } else {
          file = path.toFile();
        }
        
        templateRootMst = file;
      }

      {
        File file = null;
        Path path = null;
        try {
          path = Paths.get(partialsRootMstPath);
        } catch (Throwable e) {
          e.printStackTrace(); // TODO
          failed = true;
          failMessages.add("Incorrect file path [" + partialsRootMstPath + "]: resolve exception");
        }
        if (!Files.exists(path)) {
          failed = true;
          failMessages.add("Incorrect file path [" + partialsRootMstPath + "]: file does not exist");
        } else if (!Files.isDirectory(path)) {
          failed = true;
          failMessages.add("Incorrect file path [" + partialsRootMstPath + "]: not a directory");
        } else {
          file = path.toFile();
        }

        partialsRootMst = file;
      }
      
      {
        Path path = null;
        try {
          path = Paths.get(outputRootDirPath);
        } catch (Throwable e) {
          e.printStackTrace(); // TODO
          failed = true;
          failMessages.add("Incorrect file path [" + outputRootDirPath + "]: resolve exception");
        }

        outputRootDir = path.toFile();
      }
      
      if (failed) {
        throw new PrepareException(failMessages);
      }

      
      
      
      
      entity_name_dash = apiSpec.getParentFile().getName();
      entityName = undashize(entity_name_dash);
      entityId = entityName + "Id";
      EntityName = capitalize(entityName);
      entityname = dashize(entityName).replaceAll("-", "");

      { // log everything
        out.println("Spell-check:");
        out.println("  * entity-name: " + entity_name_dash);
        out.println("  *  entityName: " + entityName);
        out.println("  *    entityId: " + entityId);
        out.println("  *  EntityName: " + EntityName);
        out.println("  *  entityname: " + entityname);
      }
      
    }

    @Override
    public void run() {
      List<SpecMethod> methods;
      try {
        try (Reader r = new FileReader(apiSpec)) {
          methods = new ApiSpecMethodExtractorJson().extract(r);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      Map<String, Object> m = TemplateFactory.createDataForTemplate(methods,
              entityName, entityId, EntityName, entity_name_dash, entityname);

      Evaluator ev = new Evaluator(partialsRootMst);

      try {
        ev.evaluateTemplateTree(templateRootMst, outputRootDir, m);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  
}
