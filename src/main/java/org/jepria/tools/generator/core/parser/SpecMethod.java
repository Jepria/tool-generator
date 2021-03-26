package org.jepria.tools.generator.core.parser;

import java.util.List;
import java.util.Map;

/**
 * A method represented in an api spec resource
 */
public interface SpecMethod {
  /**
   * One of GET, POST, PUT, DELETE, HEAD, OPTIONS
   *
   * @return
   */
  String httpMethod();

  /**
   * URL path
   * @return
   */
  // TODO better Path than String?
  String path();

  interface Parameter {
    /**
     * OpenAPI schema, as-is from json spec or built from the java type
     */
    Map<String, Object> schema();
    /**
     * NonNull
     * One of Query, Path, Header, Cookie
     */
    String in();

    /**
     * NonNull
     * Parameter functional name related to its 'in' type
     */
    String name();
  }

  /**
   * NotNull
   * @return at least empty list
   */
  List<Parameter> params();

  /**
   * OpenAPI schema, as-is from json spec or built from the java type, {@code null} if the method has no request body
   * @return
   */
  Map<String, Object> requestBodySchema();

  /**
   * OpenAPI schema, as-is from json spec or built from the java type,
   * {@code null} if the method has no response body
   * or if the method does have response body but its runtime type remained undetermined
   * // TODO distinguish those two cases
   * @return
   */
  Map<String, Object> responseBodySchema();

  String operationId();
  
  default String asString() {
    StringBuilder sb = new StringBuilder();
    sb.append(httpMethod());
    sb.append(':');
    sb.append(path());
    sb.append('(');
    int params = 0;
    for (Parameter p: params()) {
      if (params++ > 0) {
        sb.append(", ");
      }
      sb.append(p.in()).append(':').append(p.name()).append(':').append("<...>");
    }
    if (requestBodySchema() != null) {
      if (params++ > 0) {
        sb.append(", ");
      }
      sb.append("Body:").append("<...>");
    }
    sb.append(')');

    return sb.toString();
  }
}
