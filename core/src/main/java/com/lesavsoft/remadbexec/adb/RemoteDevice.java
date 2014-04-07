/*
Copyright 2014 LeSav Software Tmi


Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.lesavsoft.remadbexec.adb;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.lesavsoft.remadbexec.common.ICall;
import com.lesavsoft.remadbexec.sshcall.SSHConnection;
import com.lesavsoft.remadbexec.sshcall.SSHExecCall;


/**
 * This file is a part of remadbexec library.
 * 
 * LocalDevice class implements the methods defined in IAdbDeviceCommands for the remote device over ssh. 
 *
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 *
 */
public class RemoteDevice extends Device {
    protected static Logger logger = Logger.getLogger(RemoteDevice.class);
    protected SSHConnection iConnection = null;
    
    public RemoteDevice(String id, SSHConnection con){
        super(id);
        iConnection = con;
    }

    @Override
    public ICall prepareCall(String cmd) {
        return new SSHExecCall(prepCommand(cmd),iConnection,0);
    }

    @Override
    public String push(String local, String remote) {
        String vFileName = new File(local).getName();
        try {
            iConnection.getSCPClient().put(local, "");
        } catch (IOException e) {
            logger.error(String.format("Unable to copy the file %s to remote %s: ", local, remote ) + e.getLocalizedMessage());
        }
        return String.format("push %s %s", vFileName,remote); 
    }

    @Override
    public String pull(String remote, String local, String finalDest) {
        String vFileName = new File(local).getName();
        return String.format("pull %s %s", remote, vFileName);
    }


    @Override
    public void completePull(String remote, String local) {
        File remoteFile = new File(remote);
        File localFile = new File(local);
        String absPath = localFile.getAbsolutePath();
        String localDest = absPath.substring(0,absPath.lastIndexOf(File.separator));
        logger.info("SCP:copy remote " +remote + " to proxy "+local + " to local " + localDest);
        try {
            iConnection.getSCPClient().get(remote,localDest);
            File newPath = new File(localDest+File.separator+remoteFile.getName());
            logger.info(String.format("Renaming %s to %s", newPath.toPath() , localFile.toPath()));
            if(localFile.exists()){
                localFile.delete();
            }
            newPath.renameTo(localFile);
        } catch (IOException e) {
            logger.error(String.format("Unable to copy the remote file %s to local %s: ", remote, local ) + e.getLocalizedMessage());
        }
    }

    @Override
    public String install(String path) {
        File vFile = new File(path);
        try {
            iConnection.getSCPClient().put(path, "");
        } catch (IOException e) {
            logger.error(String.format("Unable to copy to the remote local file %s: ",path ) + e.getLocalizedMessage());
        }
        return String.format("install %s", vFile.getName()); 
    }
}
