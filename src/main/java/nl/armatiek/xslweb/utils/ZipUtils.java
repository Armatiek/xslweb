package nl.armatiek.xslweb.utils;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
  
  private static final int BUFFER_SIZE = 8192;
  
  public static void zip(File directory, File base, ZipOutputStream zos) throws IOException {
    File[] files = directory.listFiles();
    byte[] buffer = new byte[8192];
    int read = 0;
    for (int i=0, n=files.length; i<n; i++) {
      if (files[i].isDirectory()) {
        zip(files[i], base, zos);
      } else {
        InputStream in = new BufferedInputStream(new FileInputStream(files[i]));
        try {
          ZipEntry entry = new ZipEntry(files[i].getPath().substring(base.getPath().length() + 1).replace('\\', '/'));          
          zos.putNextEntry(entry);
          while (-1 != (read = in.read(buffer))) {
            zos.write(buffer, 0, read);
          }
          zos.closeEntry();
        } finally {
          in.close();
        }
      }
    }
  }
  
  public static void zipDirectory(File directory, File zip) throws IOException {
    ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip)));
    try {
      zip(directory, directory, zos);
    } finally {
      zos.flush();
      zos.close();
    }
  }
  
  public static void unzipFile(File zip, File extractTo) throws IOException {
    ZipFile archive = new ZipFile(zip);
    try {
      Enumeration<? extends ZipEntry> e = archive.entries();
      while (e.hasMoreElements()) {
        ZipEntry entry = (ZipEntry) e.nextElement();
        File file = new File(extractTo, entry.getName());
        if (entry.isDirectory()) {
          if (!file.exists()) {
            file.mkdirs();
          } // else nothing to do
        } else {
          if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
          }
          InputStream in = archive.getInputStream(entry);
          BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
          try {
            byte[] buffer = new byte[8192];
            int read;
            while (-1 != (read = in.read(buffer))) {
              out.write(buffer, 0, read);
            }
          } finally {
            in.close();
            out.close();
          }
        }
      }
    } finally {
      archive.close();
    }
  }
  
  /**
   * Unzips all entries from the given input stream into the destination directory.
   * The method guards against Zip Slip by validating each entry's resolved path.
   */
  public static void unzipStream(InputStream input, File destinationDir) throws IOException {
    if (destinationDir == null) {
      throw new IOException("Destination directory must not be null");
    }
    // Ensure destination directory exists
    if (!destinationDir.exists() && !destinationDir.mkdirs()) {
      throw new IOException("Could not create destination directory: " + destinationDir);
    }

    Path destRoot = destinationDir.toPath().toRealPath();
    boolean hasEntry = false;

    try (ZipInputStream zipIn = new ZipInputStream(input)) {
      ZipEntry entry;
      byte[] buffer = new byte[BUFFER_SIZE];

      while ((entry = zipIn.getNextEntry()) != null) {
        hasEntry = true;

        Path targetPath = resolveEntryPath(destRoot, entry);

        if (entry.isDirectory()) {
          Files.createDirectories(targetPath);
        } else {
          Path parent = targetPath.getParent();
          if (parent != null) {
            Files.createDirectories(parent);
          }
          try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(targetPath))) {
            int len;
            while ((len = zipIn.read(buffer)) != -1) {
              os.write(buffer, 0, len);
            }
          }
        }
        zipIn.closeEntry();
      }
    }

    if (!hasEntry) {
      throw new IOException("Empty ZIP archive");
    }
  }

  /**
   * Resolves and validates the output path for a ZipEntry to prevent Zip Slip.
   */
  private static Path resolveEntryPath(Path destRoot, ZipEntry entry) throws IOException {
    // Normalize ensures that any ../ segments are removed before validation
    Path resolved = destRoot.resolve(entry.getName()).normalize();

    if (!resolved.startsWith(destRoot)) {
      throw new IOException("Zip Slip detected for entry: " + entry.getName());
    }
    return resolved;
  }
  
}