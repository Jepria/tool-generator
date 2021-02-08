package org.jepria.tools.generator.core;

import org.jepria.tools.generator.cli.Main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
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

  static Resource fromJarResourceRoot(String resourceRoot) {
    if (resourceRoot != null && !resourceRoot.startsWith("/")) {
      resourceRoot = "/" + resourceRoot;
    }

    Path path;

    try {
      URI uri = Main.class.getResource(resourceRoot).toURI();
      if (uri.getScheme().equals("jar")) {
        FileSystem fileSystem;
        
        // TODO fragile solution: 
        //  when creating more than one FileSystem from a single jar
        //  (e.g. for partials-root and template-root resource directories),
        //  the other throws FileSystemAlreadyExistsException. In that case, use the FileSystem that must have been already created.
        //  But what is the owner of the first FileSystem closes it?
        //  see https://stackoverflow.com/questions/55892386/how-to-handle-filesystemalreadyexistsexception
        try {
          fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
        } catch (FileSystemAlreadyExistsException e) {
          fileSystem = FileSystems.getFileSystem(uri);
        }
        path = fileSystem.getPath(resourceRoot);
      } else {
        path = Paths.get(uri);
      }

    } catch (Throwable e) {
      throw new RuntimeException(e);
    }

    return new PathResourceImpl(path);
  }

}
