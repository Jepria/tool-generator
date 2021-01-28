package org.jepria.tools.generator.core;

import org.jepria.tools.generator.core.parser.ApiSpecMethodExtractorJson;
import org.jepria.tools.generator.core.parser.SpecMethod;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

  enum FieldType {
    INTEGER, STRING, DATE, OPTION
  }

  protected static String capitalize(String s) {
    if (s == null) {
      return null;
    } else {
      return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
  }

  protected static Map<String, Object> createField(String fieldName, FieldType fieldType) {
    Map<String, Object> field = new HashMap<>();
    field.put("fieldName", fieldName);
    if (fieldType == FieldType.INTEGER) {
      field.put("isTypeInt", true);
    } else if (fieldType == FieldType.DATE) {
      field.put("isTypeDate", true);
    } else if (fieldType == FieldType.STRING) {
      field.put("isTypeText", true);
    } else if (fieldType == FieldType.OPTION) {
      field.put("isTypeOption", true);
    }
    return field;
  }


  public static void main(String[] args) throws IOException {

    ////////////////////////////
    final File apiSpec = new File("C:/work/rfi/DocumentMark/App/service-rest/src/api-spec/document-mark/swagger.json");

    final String entityName = "documentMark";
    final String entityId = "documentMarkId";
    final String EntityName = "DocumentMark";
    final String entity_name_dash = "document-mark";
    final String entityname = "documentmark";

    final File templateRoot = new File("C:/work/tool-generator-crud/src/main/resources/mustache-templates/client-react");
    final File outputRoot = new File("C:/work/rfi/DocumentMark/App");
    final File partialsRoot = new File("C:/work/tool-generator-crud/src/main/resources/mustache-templates/partials");
    ////////////////////////////

    Map<String, Object> m = new HashMap<>();

    m.put("entityName", entityName);
    m.put("entityId", entityId);
    m.put("EntityName", EntityName);
    m.put("entity_name_dash", entity_name_dash);
    m.put("entityname", entityname);

    List<SpecMethod> methods;
    try (Reader r = new FileReader(apiSpec)) {
      methods = new ApiSpecMethodExtractorJson().extract(r);
    }

    Map<String, FieldType> fieldMap = new HashMap<>();

    {
      List<Object> fieldsList = new ArrayList<>();
      m.put("fieldsList", fieldsList);
      List<Object> fieldsDetail = new ArrayList<>();
      m.put("fieldsDetail", fieldsDetail);

      for (SpecMethod method : methods) {
        if (method.path().equals("/" + entity_name_dash + "/{recordId}") && "get".equals(method.httpMethod())) {
          Map<String, Object> responseBodySchema = method.responseBodySchema();
          if (responseBodySchema != null) {
            Map<String, Object> propertiesMap = (Map) responseBodySchema.get("properties");
            if (propertiesMap != null) {
              for (String fieldName : propertiesMap.keySet()) {

                if (fieldName.equals(entityId)) { // skip field for entityId
                  continue;
                }
                
                Map<String, Object> propertyMap = (Map) propertiesMap.get(fieldName);
                if (propertyMap != null) {

                  FieldType fieldType = null;

                  String fieldTypeStr = (String) propertyMap.get("type");
                  if ("integer".equals(fieldTypeStr)) {
                    fieldType = FieldType.INTEGER;
                  } else if ("string".equals(fieldTypeStr)) {
                    String format = (String) propertyMap.get("format");
                    if ("date-time".equals(format)) {
                      fieldType = FieldType.DATE;
                    } else {
                      fieldType = FieldType.STRING;
                    }
                  } else if ("object".equals(fieldTypeStr)) {
                    Map<String, Object> fieldPropertyMap = (Map) propertyMap.get("properties");
                    if (fieldPropertyMap != null) {
                      Map<String, Object> nameMap = (Map) fieldPropertyMap.get("name");
                      Map<String, Object> valueMap = (Map) fieldPropertyMap.get("value");
                      if (nameMap != null && valueMap != null) {
                        if ("string".equals(nameMap.get("type"))) {
                          String valueType = (String) valueMap.get("type");
                          if ("string".equals(valueType)) {
                            fieldType = FieldType.OPTION;
                          } else if ("integer".equals(valueType)) {
                            fieldType = FieldType.OPTION;
                          }
                        }
                      }
                    }
                  }

                  Map<String, Object> field = createField(fieldName, fieldType);

                  // register field
                  fieldMap.put(fieldName, fieldType);

                  fieldsList.add(field);
                  fieldsDetail.add(field);
                }
              }
            }
          }
        }
      }
    }

    {
      List<Object> fieldsCreate = new ArrayList<>();
      m.put("fieldsCreate", fieldsCreate);

      for (SpecMethod method : methods) {
        if (method.path().equals("/" + entity_name_dash) && "post".equals(method.httpMethod())) {
          Map<String, Object> requestBodySchema = method.requestBodySchema();
          if (requestBodySchema != null) {
            Map<String, Object> propertiesMap = (Map) requestBodySchema.get("properties");
            if (propertiesMap != null) {
              for (String fieldName : propertiesMap.keySet()) {

                if (fieldName.equals(entityId)) { // skip field for entityId
                  continue;
                }
                
                // some fields (options) might be truncated (value only), so look at the field map built on getRecordById method
                if (fieldName.endsWith("Code")) {
                  String optionFieldName = fieldName.substring(0, fieldName.length() - "Code".length());
                  FieldType fieldType = fieldMap.get(optionFieldName);
                  if (fieldType == FieldType.OPTION) {
                    // this is a truncated options field
                    Map<String, Object> field = createField(optionFieldName, fieldType);
                    String FieldName = capitalize(optionFieldName);
                    field.put("isFieldRequired", true);
                    field.put("FieldName", FieldName);

                    fieldsCreate.add(field);
                  }
                } else {

                  Map<String, Object> propertyMap = (Map) propertiesMap.get(fieldName);
                  if (propertyMap != null) {

                    FieldType fieldType = null;

                    String fieldTypeStr = (String) propertyMap.get("type");
                    if ("integer".equals(fieldTypeStr)) {
                      fieldType = FieldType.INTEGER;
                    } else if ("string".equals(fieldTypeStr)) {
                      String format = (String) propertyMap.get("format");
                      if ("date-time".equals(format)) {
                        fieldType = FieldType.DATE;
                      } else {
                        fieldType = FieldType.STRING;
                      }
                    }

                    Map<String, Object> field = createField(fieldName, fieldType);
                    field.put("isFieldRequired", true);
                    fieldsCreate.add(field);
                  }
                }
              }
            }
          }
        }
      }
    }

    {
      List<Object> fieldsEdit = new ArrayList<>();
      m.put("fieldsEdit", fieldsEdit);

      for (SpecMethod method : methods) {
        if (method.path().equals("/" + entity_name_dash + "/{recordId}") && "put".equals(method.httpMethod())) {
          Map<String, Object> requestBodySchema = method.requestBodySchema();
          if (requestBodySchema != null) {
            Map<String, Object> propertiesMap = (Map) requestBodySchema.get("properties");
            if (propertiesMap != null) {
              for (String fieldName : propertiesMap.keySet()) {

                if (fieldName.equals(entityId)) { // skip field for entityId
                  continue;
                }
                
                // some fields (options) might be truncated (value only), so look at the field map built on getRecordById method
                if (fieldName.endsWith("Code")) {
                  String optionFieldName = fieldName.substring(0, fieldName.length() - "Code".length());
                  FieldType fieldType = fieldMap.get(optionFieldName);
                  if (fieldType == FieldType.OPTION) {
                    // this is a truncated options field
                    Map<String, Object> field = createField(optionFieldName, fieldType);
                    String FieldName = capitalize(optionFieldName);
                    field.put("isFieldRequired", true);
                    field.put("FieldName", FieldName);

                    fieldsEdit.add(field);
                  }
                } else {

                  Map<String, Object> propertyMap = (Map) propertiesMap.get(fieldName);
                  if (propertyMap != null) {

                    FieldType fieldType = null;

                    String fieldTypeStr = (String) propertyMap.get("type");
                    if ("integer".equals(fieldTypeStr)) {
                      fieldType = FieldType.INTEGER;
                    } else if ("string".equals(fieldTypeStr)) {
                      String format = (String) propertyMap.get("format");
                      if ("date-time".equals(format)) {
                        fieldType = FieldType.DATE;
                      } else {
                        fieldType = FieldType.STRING;
                      }
                    }

                    Map<String, Object> field = createField(fieldName, fieldType);
                    field.put("isFieldRequired", true);
                    fieldsEdit.add(field);
                  }
                }
              }
            }
          }
        }
      }
    }

    {
      List<Object> fieldsSearch = new ArrayList<>();
      m.put("fieldsSearch", fieldsSearch);

      for (SpecMethod method : methods) {
        if (method.path().equals("/" + entity_name_dash + "/search") && "post".equals(method.httpMethod())) {
          Map<String, Object> requestBodySchema = method.requestBodySchema();
          if (requestBodySchema != null) {
            Map<String, Object> propertiesMap0 = (Map) requestBodySchema.get("properties");
            if (propertiesMap0 != null) {
              Map<String, Object> templateMap = (Map) propertiesMap0.get("template");
              if (templateMap != null) {
                Map<String, Object> propertiesMap = (Map) templateMap.get("properties");
                if (propertiesMap != null) {
                  for (String fieldName : propertiesMap.keySet()) {

                    if (fieldName.equals(entityId)) { // skip field for entityId
                      continue;
                    }
                    
                    // some fields (options) might be truncated (value only), so look at the field map built on getRecordById method
                    if (fieldName.endsWith("Code")) {
                      String optionFieldName = fieldName.substring(0, fieldName.length() - "Code".length());
                      FieldType fieldType = fieldMap.get(optionFieldName);
                      if (fieldType == FieldType.OPTION) {
                        // this is a truncated options field
                        Map<String, Object> field = createField(optionFieldName, fieldType);
                        String FieldName = capitalize(optionFieldName);
                        field.put("FieldName", FieldName);

                        fieldsSearch.add(field);
                      }
                    } else {

                      Map<String, Object> propertyMap = (Map) propertiesMap.get(fieldName);
                      if (propertyMap != null) {

                        FieldType fieldType = null;

                        String fieldTypeStr = (String) propertyMap.get("type");
                        if ("integer".equals(fieldTypeStr)) {
                          fieldType = FieldType.INTEGER;
                        } else if ("string".equals(fieldTypeStr)) {
                          String format = (String) propertyMap.get("format");
                          if ("date-time".equals(format)) {
                            fieldType = FieldType.DATE;
                          } else {
                            fieldType = FieldType.STRING;
                          }
                        }

                        Map<String, Object> field = createField(fieldName, fieldType);
                        field.put("isFieldRequired", true);
                        fieldsSearch.add(field);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }


    Evaluator ev = new Evaluator(partialsRoot);

    ev.evaluateTemplateTree(templateRoot, outputRoot, m);

    System.out.println("Done!");
  }
}
