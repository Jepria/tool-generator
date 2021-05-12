package org.jepria.tools.generator.cli;

import org.jepria.tools.generator.core.Api;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defaults to generate crud client-react code from mustache sources only.
 * TODO add support for not only crud, not only client-react and not only mustache 
 */
public class Main {

  private static PrintStream out = System.out;
  
  public static void main(String[] args) {

    if (args != null) {
      List<String> argList = Arrays.asList(args);

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

      List<Api.ManualChange> manualChanges = new ArrayList<>();
      
      try {
        Api.run(apiSpecPaths, templateRootMst, partialsRootMst, outputRoot, entityName, s -> {out.println(s);}, manualChanges);
      } catch (Api.ExecutionException e) {
        for (String message : e.getFailMessages()) {
          out.println(message);
        }
        return;
      }
      
      showManualChanges(manualChanges);

    } else {
      out.println("No arguments provided");
      return;
    }
      
    out.println("Done.");
  }

  protected static void showManualChanges(List<Api.ManualChange> manualChanges) {
    if (manualChanges != null && !manualChanges.isEmpty()) {
      out.println("====== The following code parts must be added manually into existing files ======");
      for (int i = 0; i < manualChanges.size(); i++) {
        Api.ManualChange mc = manualChanges.get(i);
        if (!mc.codeParts.isEmpty()) {
          out.println((i + 1) + ") " + mc.targetFile.getAbsolutePath());
          for (Api.CodePart cp: mc.codeParts) {
            out.println();
            out.println(cp.body);
          }
          out.println();
        } 
      }
    }
  }
  
}
