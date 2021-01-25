package org.jepria.tools.generator.core;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import java.io.*;
import java.util.Map;

public class Evaluator {
  
  protected final Mustache.Compiler compiler; 
  
  public Evaluator(File partialDirRoot) {
    Mustache.Compiler compiler = Mustache.compiler()
            .withDelims("[@ @]")
            .defaultValue("").escapeHTML(false);
    
    if (partialDirRoot != null) {
      compiler = compiler.withLoader(new Mustache.TemplateLoader() {
        @Override
        public Reader getTemplate(String s) throws Exception {
          return new FileReader(new File(partialDirRoot, s));
        }
      });
    }
    
    this.compiler = compiler;
  }
  
  public Evaluator() {
    this(null);
  }
  
  public Mustache.Compiler getCompiler() {
    return compiler;
  }
  
  public void evaluateTemplateTree(File treeRoot, File outputRoot, Map<String, Object> values) throws IOException {
    
    String fileNameEvaluated = evaluateTemplate(new StringReader(treeRoot.getName()), values);
    File newOutputRoot = new File(outputRoot, fileNameEvaluated);
    
    if (treeRoot.isDirectory()) {
      newOutputRoot.mkdirs();
      
      File[] childFiles = treeRoot.listFiles();

      if (childFiles != null) {
        for (File childFile: childFiles) {
          evaluateTemplateTree(childFile, newOutputRoot, values);
        }
      }
    } else if (treeRoot.isFile()) {
      
      if (fileNameEvaluated.endsWith(".mustache")) { // a '.mustache' file that needs to be evaluated 
        
        String fileNameDemustached = fileNameEvaluated.substring(0, fileNameEvaluated.length() - ".mustache".length());
        newOutputRoot = new File(outputRoot, fileNameDemustached);
        newOutputRoot.createNewFile();
        
        try (Reader r = new FileReader(treeRoot); Writer w = new FileWriter(newOutputRoot)) {
          String contentEvaluated = evaluateTemplate(r, values);
          w.write(contentEvaluated);
        }
        
      } else { // a regular file (possibly binary) that must be copied-as-is

        newOutputRoot.createNewFile();
        
        try (InputStream in = new FileInputStream(treeRoot); OutputStream os = new FileOutputStream(newOutputRoot)) {
          byte[] buffer = new byte[1024];
          int length;
          while ((length = in.read(buffer)) > 0) {
            os.write(buffer, 0, length);
          }
        }
      }
    }
  }

  public String evaluateTemplate(Reader template, Map<String, Object> values) {
    Template t = getCompiler().compile(template);
    return t.execute(values);
  }
}
