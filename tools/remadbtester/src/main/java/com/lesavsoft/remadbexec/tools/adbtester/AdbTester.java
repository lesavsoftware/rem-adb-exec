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

package com.lesavsoft.remadbexec.tools.adbtester;

import java.io.IOException;

import com.lesavsoft.remadbexec.adb.AdbWrapperHost;
import com.lesavsoft.remadbexec.common.EInvalidParameters;
import com.lesavsoft.remadbexec.common.consumers.LineConsumerStdout;

/**
 * This file is a part of remadbexec library.
 *
 * AdbTester tool demonstrates the usage of the remadbexec library.
 * It has descriptive help on stdout, except for the property files which are described here.
 * 
 * Sample property files:
 * 
 * for the local generic adb:

#local device type - device is connected to local USB
device.type = local

#device id should be the one from devices list, or empty for no -s switch 
device.id = 

 * 
 * for the local device:
 
#local device type - device is connected to local USB
device.type = local

#device id should be the one from devices list, or empty for no -s switch 
device.id = abcabcabcabcabcd

 * 
 * for the remote ssh generic adb:

#local device type - device is connected to local USB
device.type = ssh

#device id should be the one from devices list, or empty for no -s switch 
device.id = 

device.key.file = ../conf/a_key_rsa
device.key.user = auser
device.key.pwd =
device.host = 192.168.1.2

 * 
 * for the remote ssh device:
 #local device type - device is connected to local USB
device.type = ssh

#device id should be the one from devices list, or empty for no -s switch 
device.id = abcabcabcabcabcd

device.key.file = ../conf/a_key_rsa
device.key.user = auser
device.key.pwd =
device.host = 192.168.1.34

 
 * 
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 *
 */

public class AdbTester {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("AdbTester tool. Copyright 2014 LeSav Software Tmi. ");
        System.out.println("http://www.apache.org/licenses/LICENSE-2.0\n\n"); 
        
        if( args.length < 2){
            System.out.println("usage: AdbTester <path_to_config> <cmd> [arguments]");
            System.out.println("usage:    cmd: install <path_to_apk>");
            System.out.println("usage:    cmd: push <path_to_local> <path_to_remote>");
            System.out.println("usage:    cmd: pull <path_to_remote> <path_to_local>");
            System.out.println("usage:    cmd: exec <args...> (args will be concatenated to a cmd to be passed to adb)");
            System.exit(0);
        }

        AdbWrapperHost adbHost = null;
        try {
            adbHost = new AdbWrapperHost(AdbWrapperHost.readPropertiesFromFile(args[0]));
            // setting the temporary file names to be static
            // remove the following line to have a new name for each command.
            adbHost.setFileName(adbHost.getDeviceId()+"_tmp", true);
        } catch (EInvalidParameters | IOException e) {
            System.out.println("Unable to instanciate AdbWrapperHost object: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            switch(args[1]){
            case "install":
                if(args.length != 3){
                    System.out.println("usage:    cmd: install <path_to_apk>");
                }else{
                    adbHost.install(args[2], new LineConsumerStdout("std: "), new LineConsumerStdout("err: "), 10000);
                }
                break;
            case "push":
                if(args.length != 4){
                    System.out.println("usage:    cmd: push <path_to_local> <path_to_remote>");
                }else{
                    adbHost.push(args[2], args[3], new LineConsumerStdout("std: "), new LineConsumerStdout("err: "), 10000);
                }
                break;
            case "pull":
                if(args.length != 4){
                    System.out.println("usage:    cmd: pull <path_to_remote> <path_to_local>");
                }else{
                    adbHost.pull(args[2], args[3], new LineConsumerStdout("std: "), new LineConsumerStdout("err: "), 10000);
                }
                break;
            default:
                String vCmd ="";
                for(int vCkl = 1; vCkl < args.length; vCkl++){
                    vCmd = vCmd + " " + args[vCkl];
                }
                adbHost.exec(vCmd.trim(), new LineConsumerStdout("std: "), new LineConsumerStdout("err: "), 10000);
                break;
            }
        } catch (IOException e) {
            System.out.println("Error while executing " + args[1] + ":" + e.getMessage());
            e.printStackTrace();
        }
        System.exit(0);
    }

}