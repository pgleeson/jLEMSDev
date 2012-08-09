package org.lemsml.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
 
public class JUtil {

   static Class<?> rootClass = ResourceRoot.class;

   static String fileSep;
   static String rootPath;

   static String osarch;

   static {
      fileSep = "/"; // System.getProperty("file.separator");
      rootPath = "org" + fileSep + "psics";
   }



   public static String getRelativeResource(Object obj, String path) throws ContentError {
      return getRelativeResource(obj.getClass(), path);
   }


   public static String getRelativeResource(String s) throws ContentError {
      return getRelativeResource(rootClass, s);
   }

   public static String getRelativeResource(Class<?> cls, String path) throws ContentError {
      String sret = null;

      try {
         InputStream fis = cls.getResourceAsStream(path);
         sret = readInputStream(fis);

      } catch (Exception ex) {
         throw new ContentError("ResourceAccess - cant get resource " + path + " relative to " + cls + ": " + ex);
      }
      return sret;
   }






   public static void copyResource(Object obj, String fnm, File fdest) throws IOException, ContentError {
	   String s = getRelativeResource(obj, fnm);
	   FileUtil.writeStringToFile(s, new File(fdest, fnm));
   }


   public static String getXMLResource(String path) throws ContentError {
      String sp = null;
      if (path.endsWith(".xml") || path.indexOf(".") < 0) {
         E.warning("getXMLReousrce should have a dot path, not " + path);
         sp = path;
      } else {
     //    E.info("replacing dots in " + path + " with " + fileSep);

         sp = path.replaceAll("\\.", fileSep) + ".xml";
      }
       return getResource(sp);
   }


   public static String getFileResource(String path, String fnm) throws ContentError {
      String sp = path.replaceAll("\\.", fileSep) + fileSep + fnm;
      return getResource(sp);
   }


   private static String getResource(String pathin) throws ContentError {
	   String path = pathin;
      String sret = null;

      try {
         if (path.startsWith(rootPath)) {
            path = path.substring(rootPath.length()+1, path.length());

         //   E.info("seeking stream rel to root class " + path + " " + rootClass.getName());
            InputStream fis = rootClass.getResourceAsStream(path);
            sret = readInputStream(fis);

         } else {
            E.warning("reading foreign resource from class path?");
            InputStream fis = ClassLoader.getSystemResourceAsStream(path);
            sret = readInputStream(fis);
         }

      } catch (Exception ex) {
        throw new ContentError("ResourceAccess - cant get " + path + " " + ex);
        
      }
      return sret;
   }



   private static String readInputStream(InputStream fis)
         throws NullPointerException, IOException {
      String sret = null;

      InputStreamReader insr = new InputStreamReader(fis);
      BufferedReader fr = new BufferedReader(insr);

      StringBuffer sb = new StringBuffer();
      while (fr.ready()) {
         sb.append(fr.readLine());
         sb.append("\n");
      }
      fr.close();
      sret = sb.toString();

      return sret;
   }



   public static void copyBinaryResource(String respathin, File dest) {
      String respath = respathin;
	   if (dest.exists()) {
      //   E.info("destination file already exists - not copying " + dest);
         return;
      }

     // E.info("installing " + dest);


      try {
         if (respath.startsWith(rootPath)) {
            respath = respath.substring(rootPath.length()+1,respath.length());
         }
         extractRelativeResource(rootClass, respath, dest);

      } catch (Exception ex) {
         E.warning("ResourceAccess - cant get " + respath + " " + ex);
         ex.printStackTrace();
      }
   }



   public static void extractRelativeResource(Class<?> c, String path, File dest) throws
        FileNotFoundException, IOException {
	   InputStream in = c.getResourceAsStream(path);
 	   
       OutputStream out = new FileOutputStream(dest);
       byte[] buf = new byte[1024];
       int len;
       while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
       }
       in.close();
       out.close();
   }




   public static void extractStaticFieldResources(Class<?> c, File dest) {
	   try {
		   Field f = c.getField("RESOURCES");
		   String[] sa = (String[])(f.get(c.newInstance()));
		   for (String s : sa) {
			   extractRelativeResource(c, s, new File(dest, s));
		   }

	   } catch (NoSuchFieldException ex) {
		   // ignore - normal case if there are no additional RESOURCES;

	   } catch (Exception ex) {
		   E.info("exception : " + ex);
	   }
   }



   public static void extractResources(String pathin, File dest) throws ContentError {
	   String path = pathin;
      path = path.replaceAll("\\.", fileSep);
      String sl = getFileResource(path, "_files.txt");
      StringTokenizer st = new StringTokenizer(sl, " \n\r\t");
      while (st.hasMoreTokens()) {
         String tok = st.nextToken().trim();
         if (tok.length() > 0) {

            String respath = path + "/" + tok;
            File destfile = new File(dest, tok);
            copyBinaryResource(respath, destfile);
         }
      }


      String sld = getFileResource( path, "_directories.txt");
      StringTokenizer std = new StringTokenizer(sld, " \n\r\t");
      while (std.hasMoreTokens()) {
         String tok = std.nextToken().trim();
         if (tok.length() > 0) {
            File fsub = new File(dest, tok);
            fsub.mkdir();
            extractResources(path + fileSep + tok, fsub);
         }
      }
   }



   public static Object newInstance(String sin) throws ContentError {
	   String s = sin;
      Object ret = null;

      if (s.startsWith("org.")) {
         // OK;
      } else {
         s = "org.catcmb." + s; // ADHOC
      }

      try {
         // Class<?> c = Class.forName(s);
         Class<?> c = ClassLoader.getSystemClassLoader().loadClass(s);
         ret = c.newInstance();
      } catch (Exception ex) {
         throw new ContentError("cant instantiate " + s + " " + ex);
        
      }
      return ret;
   }



   public static String shortClassName(Object ov) {
      String cnm = ov.getClass().getName();
      cnm = cnm.substring(cnm.lastIndexOf(".") + 1, cnm.length());
      return cnm;
   }


   public static void extractMissingResources(String path, File dir) throws ContentError {
      extractResources(path, dir);
   }





   public static void unpackJar(File fjar, File fout) throws IOException {
    
      JarFile jf = new JarFile(fjar);
      Enumeration<JarEntry> en = jf.entries();

      while (en.hasMoreElements()) {
         JarEntry je = en.nextElement();
         java.io.File f = new File(fout,  je.getName());
         if (je.isDirectory()) {
            f.mkdirs();
            continue;

         } else {
            // f.getParentFile().mkdirs();

            if (f.getPath().indexOf("META-INF") >= 0) {
               // skip it
            } else {
            f.getParentFile().mkdirs();
            java.io.InputStream is = jf.getInputStream(je);
            java.io.FileOutputStream fos = new FileOutputStream(f);

            // EFF - buffering, file channels??
            while (is.available() > 0) {
               fos.write(is.read());
            }
            fos.close();
            is.close();
         }
         }
      }

    //  E.info("unpacked jar to " + fout);

       
   }



   public static String absPath(Class<?> base, String pckgname) {
	   String rcnm = base.getName();
	    rcnm = rcnm.substring(0, rcnm.lastIndexOf("."));
	    String ppath = rcnm.replace(".", "/");
	    String path = ppath + "/" + pckgname;

	    if (path.endsWith("/.")) {
	    	path = path.substring(0, path.length()-2);
	    }

	    return path;
   }



   public static String[] getResourceList(Class<?> base, String pckgname, String ext) {

	    String path = absPath(base, pckgname);

	   	ArrayList<String> als = new ArrayList<String>();
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			URL resource = cld.getResource(path);
			File dir = new File(resource.getFile());

			if (dir.exists()) {
				// we're running from the file system;
				for (String fnm : dir.list()) {
					if (ext == null || fnm.endsWith(ext)) {
						als.add(fnm);
					}
				}

			} else {
				// we're running from a jar file;
				JarURLConnection conn = (JarURLConnection) resource.openConnection();
				String starts = conn.getEntryName();
				JarFile jfile = conn.getJarFile();
				Enumeration<JarEntry> e = jfile.entries();
				while (e.hasMoreElements()) {
					ZipEntry entry = e.nextElement();
					String entryname = entry.getName();
					if (entryname.startsWith(starts)
							&& (entryname.lastIndexOf('/') <= starts.length())) {
						String rnm = entryname.substring(starts.length()+1, entryname.length());
						if (rnm.length() > 1 && (ext == null || rnm.endsWith(ext))) {
							als.add(rnm);
						}
					}

				}

			}

		} catch (Exception ex) {
			E.error("cant list resources? " + ex);
		}
		return als.toArray(new String[0]);
	}



   	public static void unpackPackage(Class<?> base, String pkgname, File dir) throws FileNotFoundException, IOException {
   		 
   		String[] sa = getResourceList(base, pkgname, null);
   		for (String s : sa) {
   			E.info("resource to unpack " + s);
   		}

   		for (String s : sa) {
   			File fdest = new File(dir, s);
   			extractRelativeResource(base, pkgname + "/" + s, fdest);
   		}
   	 
   	}







public static void showThreads() {

	   // Find the root thread group
    ThreadGroup root = Thread.currentThread().getThreadGroup();
    while (root.getParent() != null) {
        root = root.getParent();
    }

    // Visit each thread group
    visit(root, 0);

}

    // This method recursively visits all thread groups under `group'.
    public static void visit(ThreadGroup group, int level) {
        // Get threads in `group'
        int numThreads = group.activeCount();
        Thread[] threads = new Thread[numThreads*2];
        numThreads = group.enumerate(threads, false);

        // Enumerate each thread in `group'
        for (int i=0; i<numThreads; i++) {
            // Get thread
            Thread thread = threads[i];
            E.info("Thread: " + thread.isDaemon() + " " + thread);
        }

        // Get thread subgroups of `group'
        int numGroups = group.activeGroupCount();
        ThreadGroup[] groups = new ThreadGroup[numGroups*2];
        numGroups = group.enumerate(groups, false);

        // Recursively visit each subgroup
        for (int i=0; i<numGroups; i++) {
            visit(groups[i], level+1);
        }
    }


	public static String getOSArchitecture() throws ContentError {
		if (osarch == null) {
			String osn = System.getProperty("os.name").toLowerCase();

			if (osn.startsWith("linux")) {
				osarch = "linux";
			} else if (osn.startsWith("mac")) {
				osarch = "mac";
			} else if (osn.startsWith("windows")) {
				osarch = "windows";
			} else {
				throw new ContentError("unrecognized os " + osn);
			}
		}
		return osarch;
	}
}

