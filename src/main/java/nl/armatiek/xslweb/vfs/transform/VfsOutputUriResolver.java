package nl.armatiek.xslweb.vfs.transform;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import net.sf.saxon.lib.OutputURIResolver;
import nl.armatiek.xslweb.vfs.VfsResolver;

public class VfsOutputUriResolver implements OutputURIResolver {
  
  private VfsResolver vfsResolver;
  private FileObject vfsResult;

  public VfsOutputUriResolver(VfsResolver vfsResolver) {
    this.vfsResolver = vfsResolver;
  }

  @Override
  public void close(Result result) throws TransformerException {
    try {
      vfsResult.close();
    } catch (FileSystemException e) {
      throw new TransformerException("Could not close file", e);
    }        
  }

  @Override
  public Result resolve(String href, String base) throws TransformerException {  
    try { 
      if (base == null) {
        vfsResult = vfsResolver.resolveFile(href);
      } else {
        FileObject vfsBase = null;
        try {
          vfsBase = vfsResolver.resolveFile(base);
        } catch (FileSystemException e) {
          throw new TransformerException(String.format("Could not resolve base \"%s\"", base), e);
        }

        if (vfsBase != null && vfsBase.exists()) {
          if (vfsBase.getType().equals(FileType.FOLDER)) {
            vfsResult = vfsBase.resolveFile(href);
          } else {
            vfsResult = vfsBase.getParent().resolveFile(href);
          }
        } else {
          vfsResult = vfsResolver.resolveFile(href);          
        }
      }

      if (vfsResult.exists()) {
        return new VfsStreamSource(vfsSource);
      } else {
        return null;
      }

    } catch (FileSystemException e) {
      throw new TransformerException(e);
    }
  }

}
