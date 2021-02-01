package org.jepria.tools.generator.core;

import org.jepria.tools.generator.core.parser.SpecMethod;

import java.util.*;

public class Templates {

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
      String FieldName = capitalize(fieldName);
      field.put("FieldName", FieldName);
      field.put("isTypeOption", true);
    }
    return field;
  }
  
  public static Map<String, Object> prepareTemplate(List<SpecMethod> methods, 
      String entityName, String entityId, String EntityName, String entity_name_dash, String entityname) {
    
    Map<String, Object> m = new HashMap<>();

    m.put("entityName", entityName);
    m.put("entityId", entityId);
    m.put("EntityName", EntityName);
    m.put("entity_name_dash", entity_name_dash);
    m.put("entityname", entityname);
    
    // common field map
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
                  fieldMap.putIfAbsent(fieldName, fieldType);

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
                    field.put("isFieldRequired", true);
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

                    // register field
                    fieldMap.putIfAbsent(fieldName, fieldType);

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
                    field.put("isFieldRequired", true);
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

                    // register field
                    fieldMap.putIfAbsent(fieldName, fieldType);

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

                        // register field
                        fieldMap.putIfAbsent(fieldName, fieldType);

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

    { // collect all options across all forms
      List<Object> allOptions = new ArrayList<>();
      for (String fieldName: fieldMap.keySet()) {
        FieldType type = fieldMap.get(fieldName);
        if (type == FieldType.OPTION) {
          Map<String, Object> field = createField(fieldName, FieldType.OPTION);
          String field_name_dash = dashize(fieldName);
          field.put("field_name_dash", field_name_dash);
          allOptions.add(field);
        }
      }
      m.put("allOptions", allOptions);
    }

    { // collect all field names across all forms
      Set<String> allFieldNames = fieldMap.keySet();
      m.put("allFieldNames", allFieldNames);
    }
    
    return m;
  }

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
}