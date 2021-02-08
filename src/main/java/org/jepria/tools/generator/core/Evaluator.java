package org.jepria.tools.generator.core;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import java.io.*;
import java.util.List;
import java.util.Map;

public class Evaluator {
  
  protected final Mustache.Compiler compiler;
  
  public Evaluator(Resource partialDirRoot) {
    Mustache.Compiler compiler = Mustache.compiler()
            .withDelims("[@ @]")
            .defaultValue("").escapeHTML(false);
    
    if (partialDirRoot != null) {
      compiler = compiler.withLoader(new Mustache.TemplateLoader() {
        @Override
        public Reader getTemplate(String s) throws Exception {
          return new InputStreamReader(partialDirRoot.append(s).newInputStream());
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

  /**
   * Evaluates a tree of template files into a tree of output files
   * @param templateTreeRoot an existing directory
   * @param outputTreeRoot existing or non-existing directory (will be created)
   * @param values
   * @throws IOException
   * @throws IllegalArgumentException if templateTreeRoot is not a directory
   * When templateTreeRoot is a directory {@code a/b}, having a template {code a/b/c/d.mustache} inside, 
   * and the outputTreeRoot is {@code x/y},
   * then the evaluated (output) code will be {x/y/c/d.mustache}. 
   * I.e. the outputTreeRoot matches the templateTreeRoot's last path part, preserving the original output path part name.
   */
  public void evaluateTemplateTree(Resource templateTreeRoot, File outputTreeRoot, Map<String, Object> values) throws IOException {

    if (!templateTreeRoot.isDirectory()) {
      throw new IllegalArgumentException("templateTreeRoot must be a directory");
    }

    outputTreeRoot.mkdirs();
    
    List<Resource> templateTreeRootChilds = templateTreeRoot.listChilds();
    
    if (templateTreeRootChilds != null) {
      for (Resource templateTreeRootChild : templateTreeRootChilds) {

        if (templateTreeRootChild.isDirectory()) {
          String childFileNameEvaluated = evaluateTemplateText(new StringReader(templateTreeRootChild.getName()), values);
          File newOutputTreeRoot = new File(outputTreeRoot, childFileNameEvaluated);
          newOutputTreeRoot.mkdirs();

          evaluateTemplateTree(templateTreeRootChild, newOutputTreeRoot, values);
        } else {
          evaluateTemplateFile(templateTreeRootChild, outputTreeRoot, values);
        }
      }
    }
  }

  /**
   * Evaluates a single template file into a single output file
   * @param templateTreeFile an existing file
   * @param outputTreeRoot existing or non-existing directory (will be created)
   * @param values
   * @throws IOException
   * @throws IllegalArgumentException if templateTreeRoot is not a file
   * When templateTreeRoot is a directory {@code a/b}, having a template {code a/b/c/d.mustache} inside, 
   * and the outputTreeRoot is {@code x/y},
   * then the evaluated (output) code will be {x/y/c/d.mustache}. 
   * I.e. the outputTreeRoot matches the templateTreeRoot's last path part, preserving the original output path part name.
   */
  public void evaluateTemplateFile(Resource templateTreeFile, File outputTreeRoot, Map<String, Object> values) throws IOException {

    if (!templateTreeFile.isFile()) {
      throw new IllegalArgumentException("templateTreeFile must be a file");
    }

    String fileNameEvaluated = evaluateTemplateText(new StringReader(templateTreeFile.getName()), values);
    
    if (fileNameEvaluated.endsWith(".mustache")) { // a '.mustache' file that needs to be evaluated

      String fileNameDemustached = fileNameEvaluated.substring(0, fileNameEvaluated.length() - ".mustache".length());
      File outputFile = new File(outputTreeRoot, fileNameDemustached);
      outputFile.createNewFile();
      
      try (Reader r = new InputStreamReader(templateTreeFile.newInputStream()); Writer w = new FileWriter(outputFile)) {
        String contentEvaluated = evaluateTemplateText(r, values);
        w.write(contentEvaluated);
      }

    } else { // a regular file (possibly binary) that must be copied-as-is

      File outputFile = new File(outputTreeRoot, fileNameEvaluated);
      outputFile.createNewFile();
      
      try (InputStream in = templateTreeFile.newInputStream(); OutputStream os = new FileOutputStream(outputFile)) {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
          os.write(buffer, 0, length);
        }
      }
    }
  }
    
  public String evaluateTemplateText(Reader template, Map<String, Object> values) {
    Template t = getCompiler().compile(template);
    return t.execute(values);
  }
}
