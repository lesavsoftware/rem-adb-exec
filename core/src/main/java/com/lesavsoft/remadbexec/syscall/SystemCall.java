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

package com.lesavsoft.remadbexec.syscall;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.lesavsoft.remadbexec.common.ICall;
import com.lesavsoft.remadbexec.common.ILineConsumer;

/**
 * This file is a part of remadbexec library.
 * 
 * Non-blocking system call object. When the call is issued, the object method instantly returns
 * It is up to the caller to make sure that output of the command is properly processed.
 * 
 * Within library it is used as a base class for blocking call implementation.
 * 
 * Sample usage of this class could be found in:
 *      com.lesavsoft.remadbexec.tools.cmdtester.CmdTester.java
 * 
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 *
 */

public class SystemCall implements ICall{
    protected String iCmd;
    protected Process iProcess = null;
    protected static Logger logger = Logger.getLogger(BlockingSystemCall.class);
    protected Thread outReader = null;
    protected Thread errReader = null;
    
    /**
     * Basic constructor
     * @param cmd
     */
    public SystemCall( String cmd ){
        iCmd = cmd;
    }
    
    /**
     * Start executing the method. Method name, call parameters, and other environment variables should be already setup 
     * before this call is made. 
     * @param out implementation of LineConsumer for standard output stream. 
     * @param err implementation of LineConsumer for error output stream.
     * @throws IOException
     */
    public void exec(ILineConsumer out, ILineConsumer err) throws IOException{
        logger.info("Command to execute :" + iCmd);
        iProcess = Runtime.getRuntime().exec(iCmd);
        
        // creating stream workers
        (outReader = new Thread(new StreamWorker(getInputStreamReader(), out))).start();
        (errReader = new Thread(new StreamWorker(getErrorStreamReader(), err))).start();
        logger.info("Executed, and started outReader: " + outReader + " and errReader:"+ errReader);
    }
    
    protected InputStreamReader getInputStreamReader(){
        if( null == iProcess ){
            return null;
        }
        return new InputStreamReader(iProcess.getInputStream());
    }
    
    protected InputStreamReader getErrorStreamReader(){
        if( null == iProcess ){
            return null;
        }
        return new InputStreamReader(iProcess.getErrorStream());
    }
    
    /**
     * Same as waiting for the process and worker threads to end
     * @throws InterruptedException
     */
    public void waitFor() throws InterruptedException{
        iProcess.waitFor();
        outReader.join();
        errReader.join();
    }

    @Override
    public void interrupt() {
        if(iProcess != null){
            iProcess.destroy();
        }
    }
}
