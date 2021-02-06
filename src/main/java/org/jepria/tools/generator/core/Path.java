package org.jepria.tools.generator.core;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Common adapter for each of {@link java.nio.file.Path}, {@link File} and jar resource interfaces
 */
public interface Path {
  boolean isDirectory();

  boolean isFile();

  Path[] listFiles();

  String getName();

  InputStream newInputStream();
  
  Path append(String child);
  
  class FilePath implements Path {

    protected final File file;

    public FilePath(File file) {
      this.file = file;
    }

    @Override
    public boolean isDirectory() {
      return file.isDirectory();
    }

    @Override
    public boolean isFile() {
      return file.isFile();
    }

    @Override
    public Path[] listFiles() {
      File[] files = file.listFiles();
      if (files == null) {
        return null;
      } else {
        Path[] paths = new Path[files.length];
        for (int i = 0; i < files.length; i++) {
          paths[i] = new FilePath(files[i]);
        }
        return paths;
      }
    }

    @Override
    public String getName() {
      return file.getName();
    }

    @Override
    public InputStream newInputStream() {
      try {
        return new FileInputStream(file);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public Path append(String child) {
      return new FilePath(new File(file, child));
    }
  }

  class JarResourceRoot implements Path {

    protected final String root;
    
    public JarResourceRoot(String root) {
      if (root == null) {
        this.root = "";
      } else if (root.endsWith("/")) {
        this.root = root.substring(0, root.length() - 1);
      } else {
        this.root = root;
      }
    }

    public JarResourceRoot() {
      this(null);
    }
    
    protected final ResourceScanner rs = new ResourceScanner();
    
    @Override
    public boolean isDirectory() {
      return true;
    }

    @Override
    public boolean isFile() {
      return false;
    }

    @Override
    public Path[] listFiles() {
      return new Path[0];
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public InputStream newInputStream() {
      return null;
    }

    @Override
    public Path append(String child) {
      return null;
    }

    // https://stackoverflow.com/questions/3923129/get-a-list-of-resources-from-classpath-directory
    static class ResourceScanner {
      private List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (
                InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
          String resource;

          while ((resource = br.readLine()) != null) {
            filenames.add(resource);
          }
        }

        return filenames;
      }

      private InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? getClass().getResourceAsStream(resource) : in;
      }

      private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
      }
    }
  }
}
