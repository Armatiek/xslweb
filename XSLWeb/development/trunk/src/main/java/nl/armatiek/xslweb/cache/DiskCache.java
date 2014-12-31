package nl.armatiek.xslweb.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskCache {
  
  private static final Logger logger = LoggerFactory.getLogger(DiskCache.class);
  
  private File rootDir;
  
  public DiskCache(File rootDir) {
    this.rootDir = rootDir;
  }
  
  public void readFromCache(String key, OutputStream os) {
    String fileName = getValidFileName(key);
    File f = new File(rootDir, getHashedDir(fileName) + File.separatorChar + fileName);
    FileInputStream fis = null;
    try {
      fis = FileUtils.openInputStream(f);
      boolean read = false;
      do {
        try {
          FileLock lock = fis.getChannel().lock();
          try {
            IOUtils.copy(fis, os);
            read = true;
          } finally {
            lock.release();
          }
        } catch (OverlappingFileLockException ofle) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException ex) {
            throw new InterruptedIOException ("Interrupted waiting for a file lock.");
          }
        }
      } while (!read);
    } catch (IOException ex) {
      logger.warn("Failed to lock \"" + fileName + "\"", ex);
    } finally {
      IOUtils.closeQuietly(fis);
    }
  }
  
  public void writeToCache(String key, InputStream is) {
    String fileName = getValidFileName(key);
    File f = new File(rootDir, getHashedDir(fileName) + File.separatorChar + fileName);
    FileOutputStream fos = null;
    try {
      fos = FileUtils.openOutputStream(f);
      boolean written = false;
      do {
        try {
          FileLock lock = fos.getChannel().lock();
          try {
            IOUtils.copy(is, fos);
            written = true;
          } finally {
            lock.release();
          }
        } catch (OverlappingFileLockException ofle) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException ex) {
            throw new InterruptedIOException ("Interrupted waiting for a file lock.");
          }
        }
      } while (!written);
    } catch (IOException ex) {
      logger.warn("Failed to lock \"" + fileName + "\"", ex);
    } finally {
      IOUtils.closeQuietly(fos);
    }
  }
  
  private String getHashedDir(String name) { 
    int hashcode = name.hashCode();
    int mask = 255;
    int firstDir = hashcode & mask;
    int secondDir = (hashcode >> 8) & mask;
    StringBuilder sb = new StringBuilder(File.separatorChar);
    sb.append(String.format("%03d", firstDir));
    sb.append(File.separatorChar);
    sb.append(String.format("%03d", secondDir));    
    return sb.toString();
  }
  
  private String getValidFileName(String key) {
    char fileSep = File.separatorChar;
    char escape = '%';
    int len = key.length();
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      char ch = key.charAt(i);
      if (ch < ' ' || ch >= 0x7F || ch == fileSep || (ch == '.' && i == 0) || ch == escape) {
        sb.append(escape);
        if (ch < 0x10) {
          sb.append('0');
        }
        sb.append(Integer.toHexString(ch));
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

}