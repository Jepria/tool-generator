package org.jepria.tools.generator.core;

import org.jepria.tools.generator.core.parser.ApiSpecMethodExtractorJson;
import org.jepria.tools.generator.core.parser.SpecMethod;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  
  public static void main(String[] args) throws IOException {

    ////////////////////////////
    final File apiSpec = new File("C:/work/rfi/DocumentMark/App/service-rest/src/api-spec/document-mark/swagger.json");

    final File templateRoot = new File("C:/work/tool-generator-crud/src/main/resources/mustache-templates/client-react");
    final File partialsRoot = new File("C:/work/tool-generator-crud/src/main/resources/mustache-templates/partials");

    final File outputRoot = new File("C:/work/rfi/DocumentMark/App");
    
    ////////////////////////////

    

    final String entity_name_dash = apiSpec.getParentFile().getName();
    final String entityName = undashize(entity_name_dash);
    final String entityId = entityName + "Id";
    final String EntityName = capitalize(entityName);
    final String entityname = dashize(entityName).replaceAll("-", "");

    {
      System.out.println("Spell-check:");
      System.out.println("  entity-name: " + entity_name_dash);
      System.out.println("   entityName: " + entityName);
      System.out.println("     entityId: " + entityId);
      System.out.println("   EntityName: " + EntityName);
      System.out.println("   entityname: " + entityname);
    }

    List<SpecMethod> methods;
    try (Reader r = new FileReader(apiSpec)) {
      methods = new ApiSpecMethodExtractorJson().extract(r);
    }
    
    Map<String, Object> m = Templates.prepareTemplate(methods, 
            entityName, entityId, EntityName, entity_name_dash, entityname);
    
    Evaluator ev = new Evaluator(partialsRoot);

    ev.evaluateTemplateTree(templateRoot, outputRoot, m);

    System.out.println("Done!");
  }
}
