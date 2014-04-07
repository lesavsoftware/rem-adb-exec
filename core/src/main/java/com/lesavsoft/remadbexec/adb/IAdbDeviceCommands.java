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

import com.lesavsoft.remadbexec.common.ICall;

/**
 * This file is a part of remadbexec library.
 * 
 * The IAdbDeviceCommands Interface defines methods that are to be implemented by any device wrapper classes.
 * 
 *
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 */
public interface IAdbDeviceCommands {
    /**
     * Pushing a file to the remote device with adb push
     * @param local local file path
     * @param remote file path on the device
     * @return
     */
    public String push(String local, String remote);
    /**
     * Starting to pull the file with adb pull
     * @param remote file path on the device
     * @param local file on the local/proxy
     * @param finalDest final destination path on the local system
     * @return
     */
    public String pull(String remote, String local, String finalDest);
    /**
     * Completes the pull command. If we are using remote device, then pulling the file will only create a temporary 
     * file on proxy. Now we need to copy it over to the local path.
     * @param remote file path on the proxy
     * @param local file path on the local system
     */
    public void completePull(String remote, String local);
    
    /**
     * Issues adb install call, copying the file over to proxy, if required
     * @param path
     * @return
     */
    public String install(String path);
    
    /**
     * Prepares the adb call based on the deviceId and cmd line
     * @param cmd
     * @return
     */
    public ICall prepareCall(String cmd);

}
