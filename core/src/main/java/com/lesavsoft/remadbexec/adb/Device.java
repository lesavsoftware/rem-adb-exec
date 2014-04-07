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

import java.util.Date;

/**
 * This file is a part of remadbexec library.
 * 
 * Device class implements the common functionality for devices with both: local and remote access. 
 *
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 *
 */

abstract public class Device implements IAdbDeviceCommands{
    protected String iDeviceId ="";
    protected String iTmpFileName = "undefined";
    protected boolean iTmpFileNameFixed = false;
    
    /**
     * Default constructor. If deviceId is not supplied, then following commands will be executed with no specific target device. 
     * It is suitable for generic adb commands such as 'adb devices'. 
     * @param deviceId device id, as it is seen by 'adb devices'
     */
    public Device( String deviceId ){
        iDeviceId = deviceId;
    }
    
    /**
     * get the deviceId used by this object's instance
     * @return
     */
    public String getId(){
        return iDeviceId;
    }
    
    /**
     * Prepares the command, by augmenting it with adb and device id, if present
     * @param cmd
     * @return
     */
    protected String prepCommand(String cmd){
        String vAugmentedCmd;
        if(iDeviceId.equals("")){
            vAugmentedCmd = String.format("adb %s" ,cmd);
        }else{
            vAugmentedCmd = String.format("adb -s %s %s", iDeviceId ,cmd);
        }
        System.out.println("Call: " + vAugmentedCmd);
        return vAugmentedCmd;
    }
    
    /**
     * File name generation routines are used by RemoteDevice class, but could be useful for any other device implementation that
     * that requires intermediate storage for the file transfer. Functionality allows for generating pretty unique file name for each
     * file transfer, as well as using the same file name for all the transfers.
     */
    
    /**
     * Generates a temporary file name with good degree of uniqueness.  
     * @return
     */
    public String setFileName(){
        Date aDate = new Date();
        if(!iTmpFileNameFixed){
            iTmpFileName = String.format("%s_%d_%d", iDeviceId, aDate.getTime(), Math.round(Math.random()*100));
        }
        return getFileName();
    }
    
    /**
     * Sets a file name, and makes it permanent if required
     * Alternatively, the file name will generated for each remote command.
     * @param name
     * @param isFixed
     * @return
     */
    public String setFileName(String name, boolean isFixed){
        iTmpFileName = name;
        iTmpFileNameFixed = isFixed;
        return getFileName();
    }
    
    /**
     * returns preset file name
     * @return
     */
    public String getFileName(){
        return iTmpFileName;
    }
}
