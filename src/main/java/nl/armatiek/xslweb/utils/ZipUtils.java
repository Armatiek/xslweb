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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
  
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
  
  public static void unzipStream(InputStream is, File extractTo) throws IOException {
    byte[] buffer = new byte[8192];
    int size;
    ZipInputStream zis = new ZipInputStream(is);
    ZipEntry entry;
    try {
      while ((entry = zis.getNextEntry()) != null) {
        File file = new File(extractTo, entry.getName());
        if (entry.isDirectory()) {
          if (!file.exists()) {
            file.mkdirs();
          }
        } else {
          if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
          }
          BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
          try {
            while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
              bos.write(buffer, 0, size);
            }
            bos.flush();
          } finally {
            zis.closeEntry();
            bos.close();
          }
        }
      }
    } finally {
      zis.close();
    }
  }
  
}