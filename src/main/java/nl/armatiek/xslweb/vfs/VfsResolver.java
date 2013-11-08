package nl.armatiek.xslweb.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;

public class VfsResolver {

  private FileSystemManager fsManager = null;
  private FileObject root = null;

  public VfsResolver(FileObject root) {
    this.root = root;
  }

  public VfsResolver(FileSystemManager fsManager) {
    this.fsManager = fsManager;
  }

  public FileSystemManager getFilesystemManager() {
    return this.fsManager;
  }

  public FileObject getRoot() {
    return this.root;
  }

  public FileObject resolveFile(String name) throws FileSystemException {
    if (root == null) {
      return fsManager.resolveFile(name);
    } else {
      return root.getFileSystem().getFileSystemManager().resolveFile(root, name);
    }
  }
}
