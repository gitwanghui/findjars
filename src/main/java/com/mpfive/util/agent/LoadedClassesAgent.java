/**
 * Project: findjars
 * 
 * File Created at Dec 13, 2012
 * 
 */
package com.mpfive.util.agent;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.instrument.Instrumentation;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author wanghui
 */
public class LoadedClassesAgent {
    
    private static Map<String, HashSet<String>> classJarMap = new HashMap<String, HashSet<String>>();

    public static void agentmain(String agentArgs, Instrumentation inst)
            throws ClassNotFoundException, UnknownHostException, IOException {
//        ClassLoader cl = Thread.currentThread().getContextClassLoader();
//        if (cl instanceof URLClassLoader) {
//            URL[] urls = ((URLClassLoader) cl).getURLs();
//            System.out.println(urls[0]);
//        }
        
        Class<?>[] classes = inst.getAllLoadedClasses();
        for (Class<?> cls : classes) {
            String jarPath = null;
            try {
                jarPath = cls.getProtectionDomain().getCodeSource().getLocation().toString();
            } catch(Exception e) {
                // ignore
            }
            if(jarPath == null /*|| !jarPath.startsWith("file")*/) continue;
            HashSet<String> classSet = classJarMap.get(jarPath);
            if(classSet == null) {
                classSet = new HashSet<String>();
                classSet.add(cls.getName());
                classJarMap.put(jarPath, classSet);
            } else {
                classSet.add(cls.getName());
            }
        }
        
        Socket socket = new Socket("127.0.0.1", Integer.parseInt(agentArgs));
        
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(classJarMap);
//        oos.close();
//        socket.close();
    }
}
