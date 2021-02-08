package org.jepria.tools.generator.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a readable resource in a tree-structured environment (like a usual file or a directory).
 * Common adapter for each of {@link java.nio.file.Path}, {@link File} and jar resource interfaces
 */
public interface Resource {
  boolean isDirectory();

  boolean isFile();

  List<Resource> listChilds();

  String getName();

  InputStream newInputStream();
  
  Resource append(String child);

  /**
   * An implementation based on a {@link java.nio.file.Path}
   */
  class PathResourceImpl implements Resource {

    protected final Path path;

    public PathResourceImpl(Path path) {
      this.path = path;
    }

    @Override
    public boolean isDirectory() {
      return Files.isDirectory(path);
    }

    @Override
    public boolean isFile() {
      return Files.isRegularFile(path);
    }

    @Override
    public List<Resource> listChilds() {
      try {
        return Files.walk(path, 1)
                .filter(anotherPath -> !anotherPath.equals(path)) // exclude the file tree root itself
                .map(path -> new PathResourceImpl(path)).collect(Collectors.toList());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public String getName() {
      return path.getFileName().toString();
    }

    @Override
    public InputStream newInputStream() {
      try {
        return Files.newInputStream(path);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public Resource append(String child) {
      return new PathResourceImpl(path.resolve(child));
    }

    @Override
    public String toString() {
      return path.toString();
    }
  }

  class JarResourceRoot implements Resource {

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
    public List<Resource> listChilds() {
      return null;
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
    public Resource append(String child) {
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
