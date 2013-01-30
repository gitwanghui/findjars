/**
 * Project: findjars
 * 
 * File Created at Dec 13, 2012
 */
package com.mpfive.util.agent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

/**
 * @author wanghui
 */
public class Attacher {

    private static int PORT = -1;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        if(args == null) {
            System.out.println("Usage: findjars pid [-class your.class.name]");
            return;
        }
        String pid = null;
        String option = null;
        String clazz = null;
        if(args.length == 1) {
            pid = args[0];
        } else if(args.length == 3) {
            pid = args[0];
            option = args[1];
            if(!"-class".equalsIgnoreCase(option)) {
                throw new UnsupportedOperationException("Usage: findjars pid [-class your.class.name]");
            }
            clazz = args[2];
        }
        String jar = Attacher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            startServer(clazz);
            synchronized(Attacher.class) {
                if(PORT == -1) {
                    Attacher.class.wait();
                }
                vm.loadAgent(jar, String.valueOf(PORT));
                vm.detach();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (AttachNotSupportedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AgentLoadException e) {
            e.printStackTrace();
        } catch (AgentInitializationException e) {
            e.printStackTrace();
        }
    }

    private static void startServer(final String clazz) {
        new Thread("findjars"){
            @SuppressWarnings("unchecked")
            public void run() {
                try {
                    ServerSocket server = null;
                    synchronized(Attacher.class) {
                        server = new ServerSocket(0);
                        PORT = server.getLocalPort();
                        Attacher.class.notify();
                    }
                    Socket socket = server.accept();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    Map<String,HashSet<String>> map = (Map<String, HashSet<String>>) ois.readObject();
                    ois.close();
                    server.close();
                    if(clazz != null) {
                        for(Map.Entry<String, HashSet<String>> entry : map.entrySet()) {
                            if(entry.getValue().contains(clazz)) {
                                System.out.println(entry.getKey());
                            }
                        }
                    } else {
                        for(String jarPath : map.keySet()) {
                            System.out.println(jarPath);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }
}
