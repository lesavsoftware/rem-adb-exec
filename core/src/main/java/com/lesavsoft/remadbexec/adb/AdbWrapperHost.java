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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.lesavsoft.remadbexec.common.EInvalidParameters;
import com.lesavsoft.remadbexec.common.ICall;
import com.lesavsoft.remadbexec.common.ILineConsumer;
import com.lesavsoft.remadbexec.sshcall.SSHConnection;

/**
 * This file is a part of remadbexec library.
 * 
 * AdbWrapperHost acts as a facade for the accessing both remote and local devices via adb.
 * Due to the number of parameters that are to be supplied for the setup to be completed, the initialization is done with the 
 * Properties collection. 
 *
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 *
 */
public class AdbWrapperHost{
    protected Device iDevice;
    
    protected static Logger logger = Logger.getLogger(AdbWrapperHost.class);
    
    // Constants for constructor for Keys and Values
    public static final String P_TXT_DEVICE_TYPE_KEY = "device.type";
    public static final String P_TXT_DEVICE_TYPE_VALUE_LOCAL = "local";
    public static final String P_TXT_DEVICE_TYPE_VALUE_SSH = "ssh";
    public static final String P_TXT_DEVICE_ID_KEY = "device.id";
    public static final String P_TXT_DEVICE_KEY_FILE_KEY = "device.key.file";
    public static final String P_TXT_DEVICE_KEY_USER_KEY = "device.key.user";
    public static final String P_TXT_DEVICE_KEY_PWD_KEY = "device.key.pwd";
    public static final String P_TXT_DEVICE_HOST_KEY = "device.host";

    // HashMap cParamHelp contains description of the possible constructor parameters
    public static final Map<String,String> cParamHelp = new HashMap<String,String>(){
        private static final long serialVersionUID = 1L;
        {
            put(P_TXT_DEVICE_TYPE_KEY,"String property defines the type of the device." +
                        "Supported types are: " + P_TXT_DEVICE_TYPE_VALUE_LOCAL + 
                        "," + P_TXT_DEVICE_TYPE_VALUE_SSH);
            put(P_TXT_DEVICE_ID_KEY,"String property identifies the device. Same as ids listed with" +
                        "'adb devices' command. If left empty, then no device will be specified for the " +
                        "adb command.");
            put(P_TXT_DEVICE_KEY_FILE_KEY,"String specifies the local path to the ssh key file.");
            put(P_TXT_DEVICE_KEY_USER_KEY,"String specifies user for the remote connection.");
            put(P_TXT_DEVICE_KEY_PWD_KEY,"String specifies the password for the key file (not the user on the remote system).");
            put(P_TXT_DEVICE_HOST_KEY,"String specifies the address of the remote system.");
        };
    };

    
    /**
     * Default constructor depending on properties supplied, it will create appropriate instance of the Device object.
     * @param props
     * @throws EInvalidParameters
     */
    public AdbWrapperHost( Properties props ) throws EInvalidParameters{
        switch(props.getProperty(P_TXT_DEVICE_TYPE_KEY)){
        case P_TXT_DEVICE_TYPE_VALUE_LOCAL:
            verifyConstructorParams_Local(props);
            iDevice = new LocalDevice(props.getProperty(P_TXT_DEVICE_ID_KEY));
            break;
        case P_TXT_DEVICE_TYPE_VALUE_SSH:
            verifyConstructorParams_Ssh(props);
            try {
                SSHConnection vCon = new SSHConnection();
                
                vCon.login(props.getProperty(P_TXT_DEVICE_HOST_KEY),
                        props.getProperty(P_TXT_DEVICE_KEY_USER_KEY),
                        new File(props.getProperty(P_TXT_DEVICE_KEY_FILE_KEY)),
                        props.getProperty(P_TXT_DEVICE_KEY_PWD_KEY)
                        );
                iDevice = new RemoteDevice(props.getProperty(P_TXT_DEVICE_ID_KEY), vCon);
            } catch (IOException e) {
                throw new EInvalidParameters("Unable to open a remote session: " + e.getMessage());
            }
            break;
            default:
                throw new EInvalidParameters("Unknown device type: " + props.getProperty(P_TXT_DEVICE_TYPE_KEY));
        }
        return;
    }
    
    /**
     * Forwarding to the generic Device.setFileName
     * @param name
     * @param isFixed
     */
    public void setFileName(String name, boolean isFixed){
        iDevice.setFileName(name, isFixed);
    }
    
    /**
     * Forwarding to the generic Device.getId
     * @return
     */
    public String getDeviceId(){
        return iDevice.getId();
    }
    
    /**
     * Reads properties from file. Could be used as a library method.
     * @param path_to_properties
     * @return
     * @throws IOException
     */
    public static Properties readPropertiesFromFile( String path_to_properties ) throws IOException{
        Properties prop = new Properties();
        prop.load(new FileInputStream(path_to_properties));
        return prop;
    }
    
    /**
     * Executes adb push
     * @param local local file
     * @param remote remote file
     * @param out stdout consumer
     * @param err stderr consumer
     * @param to timeout value
     * @throws IOException
     */
    public synchronized void push(String local, String remote, ILineConsumer out, ILineConsumer err, int to) throws IOException{
        logger.info(String.format("Call to push(to: %d) : %s > %s", to, local, remote));
        exec(iDevice.push(local, remote), out, err, to);
    }

    /**
     * Executes adb pull 
     * @param remote remote file
     * @param local local file
     * @param out stdout consumer
     * @param err stderr consumer
     * @param to timeout value
     * @throws IOException
     */
    public synchronized void pull(String remote, String local, ILineConsumer out, ILineConsumer err, int to) throws IOException{
        logger.info(String.format("Call to pull(to: %d) : %s > %s", to, remote, local));
        exec(iDevice.pull(remote, iDevice.setFileName(), local), out, err, to);
        iDevice.completePull(iDevice.getFileName(), local);
    }

    /**
     * Executes adb install 
     * @param path path to apk
     * @param out stdout consumer
     * @param err stderr consumer
     * @param to timeout value
     * @throws IOException
     */
    public synchronized void install(String path, ILineConsumer out, ILineConsumer err, int to) throws IOException{
        logger.info(String.format("Call to install(to: %d) : %s ", to, path));
        exec(iDevice.install(path), out, err, to);
    }

    /**
     * Executes an arbitrary adb command
     * @param cmd command string w/o adb or -s <device> 
     * @param out stdout consumer
     * @param err stderr consumer
     * @param to timeout value
     * @throws IOException
     */
    public synchronized void exec(String cmd, ILineConsumer out, ILineConsumer err, int to) throws IOException{
        logger.info(String.format("Call to exec(to: %d) : %s ", to, cmd));
        Timer timer = new Timer();
        final ICall vCmd = iDevice.prepareCall(cmd);
        if( to != 0 ){
            timer.schedule((new TimerTask() {
            @Override
            public void run() {
                vCmd.interrupt();
                }
            }), to);
        }
        
        vCmd.exec(out, err);
    }
    
    //=========================== Constructor Verification Methods ========================================
    protected void verifyConstructorParams_Local(Properties prop) throws EInvalidParameters{
        verifyPropertiesContain(prop, (List<String>)Arrays.asList(
                P_TXT_DEVICE_TYPE_KEY,
                P_TXT_DEVICE_ID_KEY
                ));
    }
    
    protected void verifyConstructorParams_Ssh(Properties prop) throws EInvalidParameters{
        verifyPropertiesContain(prop, (List<String>)Arrays.asList(
                P_TXT_DEVICE_TYPE_KEY,
                P_TXT_DEVICE_ID_KEY,
                P_TXT_DEVICE_KEY_FILE_KEY,
                P_TXT_DEVICE_KEY_USER_KEY,
                P_TXT_DEVICE_KEY_PWD_KEY,
                P_TXT_DEVICE_HOST_KEY
                ));
    }
    
    protected void verifyPropertiesContain(Properties prop, List<String> k) throws EInvalidParameters{
        // list properties to detect if we have any unknown parameters
        ArrayList<String> keys = new ArrayList<String>(k);
        for(Object pKey:prop.keySet()){
            if(!keys.contains(pKey)){
                logger.warn("Unknown property key: " + pKey);
            }else{
                keys.remove(pKey);
            }
        }
        // then we check that no required fields are left in the list
        for(String pKey:keys){
            logger.error(String.format("Missing required field: %s - %s", pKey, cParamHelp.get(pKey)));
        }
        if( keys.size() != 0){
            throw new EInvalidParameters("Missing required key(s):" + keys.toString());
        }
        return;
    }

}
