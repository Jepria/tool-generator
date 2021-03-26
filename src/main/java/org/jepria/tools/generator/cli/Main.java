package org.jepria.tools.generator.cli;

import org.jepria.tools.generator.core.Evaluator;
import org.jepria.tools.generator.core.Resource;
import org.jepria.tools.generator.core.parser.ApiSpecMethodExtractorJson;
import org.jepria.tools.generator.core.parser.SpecMethod;
import org.jepria.tools.generator.mustache_templates.client_react.crud.TemplateFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
      String entityName = null;
      
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
          // absolute path to the output project root
          i++;
          outputRoot = argList.get(i);
        } else if (argList.get(i).equals("--entity-name") && i < argList.size() - 1) {
          // target entity name
          i++;
          entityName = argList.get(i);
        }
      }

      try {
        runner = new Runner(apiSpecPaths, templateRootMst, partialsRootMst, outputRoot, entityName);
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
                  String outputRootDirPath,
                  String entity_name_dash) throws PrepareException {

      boolean failed = false;
      List<String> failMessages = new ArrayList<>();

      File apiSpec = null;
      File templateRootMst = null;
      File partialsRootMst = null;
      File outputRootDir = null;

      // TODO add support for multi-spec generation
      if (apiSpecPaths.size() > 1) {
        failed = true;
        failMessages.add("Multi-spec generation not supported yet, provide single spec file");
      }
      
      {
        try {
          apiSpec = Paths.get(apiSpecPaths.get(0)).toFile();
        } catch (Throwable e) {
          e.printStackTrace(); // TODO
          failed = true;
          failMessages.add("Incorrect file path [" + apiSpecPaths.get(0) + "]: resolve exception");
        }
        if (!apiSpec.exists()) {
          failed = true;
          failMessages.add("Incorrect file path [" + apiSpecPaths.get(0) + "]: file does not exist");
        } else if (!apiSpec.isFile()) {
          failed = true;
          failMessages.add("Incorrect file path [" + apiSpecPaths.get(0) + "]: not a regular file");
        }
      }
      

      if (templateRootMstPath != null) {
        try {
          templateRootMst = Paths.get(templateRootMstPath).toFile();
        } catch (Throwable e) {
          e.printStackTrace(); // TODO
          failed = true;
          failMessages.add("Incorrect file path [" + templateRootMstPath + "]: resolve exception");
        }
        if (!templateRootMst.exists()) {
          failed = true;
          failMessages.add("Incorrect file path [" + templateRootMstPath + "]: file does not exist");
        } else if (!templateRootMst.isDirectory()) {
          failed = true;
          failMessages.add("Incorrect file path [" + templateRootMstPath + "]: not a directory");
        }
        
        System.out.println("Custom template root: " + templateRootMst);
      }

      if (partialsRootMstPath != null) {
        try {
          partialsRootMst = Paths.get(partialsRootMstPath).toFile();
        } catch (Throwable e) {
          e.printStackTrace(); // TODO
          failed = true;
          failMessages.add("Incorrect file path [" + partialsRootMstPath + "]: resolve exception");
        }
        if (!partialsRootMst.exists()) {
          failed = true;
          failMessages.add("Incorrect file path [" + partialsRootMstPath + "]: file does not exist");
        } else if (!partialsRootMst.isDirectory()) {
          failed = true;
          failMessages.add("Incorrect file path [" + partialsRootMstPath + "]: not a directory");
        }

        System.out.println("Custom partials root: " + partialsRootMst);
      }
      
      {
        try {
          outputRootDir = Paths.get(outputRootDirPath).toFile();
        } catch (Throwable e) {
          e.printStackTrace(); // TODO
          failed = true;
          failMessages.add("Incorrect file path [" + outputRootDirPath + "]: resolve exception");
        }
      }

      {
        if (entity_name_dash == null || (!entity_name_dash.matches("[^/]+") && !entity_name_dash.matches("/[^/]+/[^/]+"))) {
          failed = true;
          failMessages.add("--entity-name argument is mandatory and must be like 'entity-name' or '/parent-entity-name/child-entity-name'");
        }
      }
      
      if (failed) {
        throw new PrepareException(failMessages);
      }


      this.apiSpec = apiSpec;
      this.templateRootMst = templateRootMst;
      this.partialsRootMst = partialsRootMst;
      this.outputRootDir = outputRootDir;

      this.entity_name_dash = entity_name_dash;
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

      final Map<String, Object> m;
      final Resource templateRootResource;
      final File outputRootDir0;
      
      if (!entity_name_dash.contains("/")) {
        String entityName = undashize(entity_name_dash);
        String entityId = entityName + "Id";
        String EntityName = capitalize(entityName);
        String entityname = dashize(entityName).replaceAll("-", "");

        { // log everything
          out.println("Spell-check:");
          out.println("  * entity-name: " + entity_name_dash);
          out.println("  *  entityName: " + entityName);
          out.println("  *    entityId: " + entityId);
          out.println("  *  EntityName: " + EntityName);
          out.println("  *  entityname: " + entityname);
        }

        m = TemplateFactory.createDataForEntityTemplate(methods,
                entityName, entityId, EntityName, entity_name_dash, entityname);
        templateRootResource = templateRootMst != null ? new Resource.PathResourceImpl(templateRootMst.toPath()) :
                Resource.fromJarResourceRoot("/mustache-templates/client-react/crud/ROOT");
        outputRootDir0 = outputRootDir;
        
      } else {

        Matcher matcher = Pattern.compile("/([^/]+)/([^/]+)").matcher(entity_name_dash);
        if (!matcher.matches()) {
          throw new RuntimeException("The entity_name_dash must match the regex /([^/]+)/([^/]+)");
        }
        
        String parent_entity_name_dash = matcher.group(1);
        String entity_name_dash0 = matcher.group(2);
        
        String entityName = undashize(entity_name_dash0);
        String entityId = entityName + "Id";
        String EntityName = capitalize(entityName);
        String entityname = dashize(entityName).replaceAll("-", "");

        String parentEntityName = undashize(parent_entity_name_dash);
        String parentEntityId = parentEntityName + "Id";
        
        { // log everything
          out.println("Spell-check:");
          out.println("  * parent-entity-name: " + parent_entity_name_dash);
          out.println("  *   parentEntityName: " + parentEntityName);
          out.println("  *     parentEntityId: " + parentEntityId);
          out.println("  *        entity-name: " + entity_name_dash0);
          out.println("  *         entityName: " + entityName);
          out.println("  *           entityId: " + entityId);
          out.println("  *         EntityName: " + EntityName);
          out.println("  *         entityname: " + entityname);
        }

        m = TemplateFactory.createDataForSubEntityTemplate(methods,
                parentEntityName, parentEntityId, parent_entity_name_dash,
                entityName, entityId, EntityName, entity_name_dash0, entityname);
        templateRootResource = templateRootMst != null ? new Resource.PathResourceImpl(templateRootMst.toPath()) :
                Resource.fromJarResourceRoot("/mustache-templates/client-react/dependent-module/ROOT");
        outputRootDir0 = new File(outputRootDir, "src/features");
      }
      
      Resource partialsRootResource = partialsRootMst != null ? new Resource.PathResourceImpl(partialsRootMst.toPath()) :
              Resource.fromJarResourceRoot("/mustache-templates/client-react/partials");
      Evaluator ev = new Evaluator(partialsRootResource);
      
      try {
        ev.evaluateTemplateTree(templateRootResource, outputRootDir0, m);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  
}
