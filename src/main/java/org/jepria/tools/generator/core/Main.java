package org.jepria.tools.generator.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
  public static void main(String[] args) throws IOException {
    
    File projectRoot = new File("C:/work/tool-generator-crud");
    
    File root = new File(projectRoot, "src/main/resources/mustache-templates/client-react");  
    File outp = new File(projectRoot,"src/main/resources/output-project");
    File part = new File(projectRoot, "src/main/resources/mustache-templates/partials");
    
    Map<String, Object> m = new HashMap<>();
    m.put("entityName", "documentMark");
    m.put("entityId", "documentMarkId");
    m.put("EntityName", "DocumentMark");
    m.put("entity_name_dash", "document-mark");
    m.put("entityname", "documentmark");

    
    List<Object> fieldsCreate = new ArrayList<>();
    {
      {
        Map<String, Object> fieldDirectionType = new HashMap<>();
        fieldDirectionType.put("fieldName", "directionType");
        fieldDirectionType.put("FieldName", "DirectionType"); // for editable options only
        fieldDirectionType.put("isFieldRequired", true);
        fieldDirectionType.put("isTypeOption", true);
        fieldsCreate.add(fieldDirectionType);
      }
      {
        Map<String, Object> fieldDocumentMarkName = new HashMap<>();
        fieldDocumentMarkName.put("fieldName", "documentMarkName");
        fieldDocumentMarkName.put("isFieldRequired", true);
        fieldDocumentMarkName.put("isTypeText", true);
        fieldsCreate.add(fieldDocumentMarkName);
      }
      {
        Map<String, Object> fieldBeginDate = new HashMap<>();
        fieldBeginDate.put("fieldName", "beginDate");
        fieldBeginDate.put("isFieldRequired", true);
        fieldBeginDate.put("isTypeDate", true);
        fieldsCreate.add(fieldBeginDate);
      }
    }
    m.put("fieldsCreate", fieldsCreate);

    List<Object> fieldsDetail = new ArrayList<>();
    {
      {
        Map<String, Object> fieldDirectionType = new HashMap<>();
        fieldDirectionType.put("fieldName", "directionType");
        fieldDirectionType.put("isTypeOption", true);
        fieldsDetail.add(fieldDirectionType);
      }
      {
        Map<String, Object> fieldDocumentMarkName = new HashMap<>();
        fieldDocumentMarkName.put("fieldName", "documentMarkName");
        fieldDocumentMarkName.put("isTypeText", true);
        fieldsDetail.add(fieldDocumentMarkName);
      }
      {
        Map<String, Object> fieldBeginDate = new HashMap<>();
        fieldBeginDate.put("fieldName", "beginDate");
        fieldBeginDate.put("isTypeDate", true);
        fieldsDetail.add(fieldBeginDate);
      }
      {
        Map<String, Object> fieldEndDate = new HashMap<>();
        fieldEndDate.put("fieldName", "endDate");
        fieldEndDate.put("isTypeDate", true);
        fieldsDetail.add(fieldEndDate);
      }
    }
    m.put("fieldsDetail", fieldsDetail);

    List<Object> fieldsEdit = new ArrayList<>();
    {
      {
        Map<String, Object> fieldDirectionType = new HashMap<>();
        fieldDirectionType.put("fieldName", "directionType");
        fieldDirectionType.put("FieldName", "DirectionType"); // for editable options only
        fieldDirectionType.put("isFieldRequired", true);
        fieldDirectionType.put("isTypeOption", true);
        fieldsEdit.add(fieldDirectionType);
      }
      {
        Map<String, Object> fieldDocumentMarkName = new HashMap<>();
        fieldDocumentMarkName.put("fieldName", "documentMarkName");
        fieldDocumentMarkName.put("isFieldRequired", true);
        fieldDocumentMarkName.put("isTypeText", true);
        fieldsEdit.add(fieldDocumentMarkName);
      }
      {
        Map<String, Object> fieldBeginDate = new HashMap<>();
        fieldBeginDate.put("fieldName", "beginDate");
        fieldBeginDate.put("isFieldRequired", true);
        fieldBeginDate.put("isTypeDate", true);
        fieldsEdit.add(fieldBeginDate);
      }
      {
        Map<String, Object> fieldEndDate = new HashMap<>();
        fieldEndDate.put("fieldName", "endDate");
        fieldEndDate.put("isTypeDate", true);
        fieldsEdit.add(fieldEndDate);
      }
    }
    m.put("fieldsEdit", fieldsEdit);

    List<Object> fieldsSearch = new ArrayList<>();
    {
      {
        Map<String, Object> fieldDirectionType = new HashMap<>();
        fieldDirectionType.put("fieldName", "directionType");
        fieldDirectionType.put("FieldName", "DirectionType"); // for editable options only
        fieldDirectionType.put("isFieldRequired", true);
        fieldDirectionType.put("isTypeOption", true);
        fieldsSearch.add(fieldDirectionType);
      }
      {
        Map<String, Object> fieldDocumentMarkName = new HashMap<>();
        fieldDocumentMarkName.put("fieldName", "documentMarkName");
        fieldDocumentMarkName.put("isTypeText", true);
        fieldsSearch.add(fieldDocumentMarkName);
      }
      {
        Map<String, Object> fieldMaxRowCount = new HashMap<>();
        fieldMaxRowCount.put("fieldName", "maxRowCount");
        fieldMaxRowCount.put("isFieldRequired", true);
        fieldMaxRowCount.put("isTypeInt", true);
        fieldsSearch.add(fieldMaxRowCount);
      }
    }
    m.put("fieldsSearch", fieldsSearch);

    List<Object> fieldsList = new ArrayList<>();
    {
      {
        Map<String, Object> fieldDirectionType = new HashMap<>();
        fieldDirectionType.put("fieldName", "directionType");
        fieldDirectionType.put("isTypeOption", true);
        fieldsList.add(fieldDirectionType);
      }
      {
        Map<String, Object> fieldDocumentMarkName = new HashMap<>();
        fieldDocumentMarkName.put("fieldName", "documentMarkName");
        fieldDocumentMarkName.put("isTypeText", true);
        fieldsList.add(fieldDocumentMarkName);
      }
      {
        Map<String, Object> fieldBeginDate = new HashMap<>();
        fieldBeginDate.put("fieldName", "beginDate");
        fieldBeginDate.put("isTypeDate", true);
        fieldsList.add(fieldBeginDate);
      }
      {
        Map<String, Object> fieldEndDate = new HashMap<>();
        fieldEndDate.put("fieldName", "endDate");
        fieldEndDate.put("isTypeDate", true);
        fieldsList.add(fieldEndDate);
      }
    }
    m.put("fieldsList", fieldsList);
    
    Evaluator ev = new Evaluator(part);
            
    ev.evaluateTemplateTree(root, outp, m);
    
    System.out.println("///done2");
  }
}
