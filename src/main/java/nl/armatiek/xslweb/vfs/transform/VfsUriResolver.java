package nl.armatiek.xslweb.vfs.transform;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import nl.armatiek.xslweb.vfs.VfsResolver;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

public class VfsUriResolver implements URIResolver {

  private VfsResolver vfsResolver;

  public VfsUriResolver(VfsResolver vfsResolver) {
    this.vfsResolver = vfsResolver;
  }

  /**
   * Called by the processor when it encounters an xsl:include, xsl:import, or
   * document() function. It first tries to resolve base, if it fails it uses
   * the FileSystemManager or the FileObject passed in the constructor as
   * context in which href is resolved.
   * 
   * @param href
   *          An href attribute, which may be relative or absolute.
   * @param base
   *          The base URI in effect when the href attribute was encountered.
   * @return A Source object, or null if the href cannot be resolved, and the
   *         processor should try to resolve the URI itself.
   */
  public Source resolve(String href, String base) throws TransformerException {

    try {
      FileObject vfsSource;

      if (base == null) {
        vfsSource = vfsResolver.resolveFile(href);
      } else {
        FileObject vfsBase = null;
        try {
          vfsBase = vfsResolver.resolveFile(base);
        } catch (FileSystemException e) {
          // could not resolve base
        }

        if (vfsBase != null && vfsBase.exists()) {
          if (vfsBase.getType().equals(FileType.FOLDER)) {
            vfsSource = vfsBase.resolveFile(href);
          } else {
            vfsSource = vfsBase.getParent().resolveFile(href);
          }
        } else {
          vfsSource = vfsResolver.resolveFile(href);          
        }
      }

      if (vfsSource.exists()) {
        return new VfsStreamSource(vfsSource);
      } else {
        return null;
      }

    } catch (FileSystemException e) {
      throw new TransformerException(e);
    }

  }

}
