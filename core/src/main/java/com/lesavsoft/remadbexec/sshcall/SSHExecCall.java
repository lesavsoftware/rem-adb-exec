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

package com.lesavsoft.remadbexec.sshcall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;


import org.apache.log4j.Logger;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import com.lesavsoft.remadbexec.common.ICall;
import com.lesavsoft.remadbexec.common.ILineConsumer;
import com.lesavsoft.remadbexec.common.StatusWrapper;

/**
 * This file is a part of remadbexec library.
 * 
 * SSHExecCall implements remote call execution ICall interface over the SSHConnection.
 * 
 * Sample usage of this class could be found in:
 *      com.lesavsoft.remadbexec.tools.sshtester.SshTester.java
 * 
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 */
public class SSHExecCall implements ICall{
    protected static Logger logger = Logger.getLogger(SSHExecCall.class);
    protected String iCmd = "";
    protected SSHConnection iConnection = null;
    protected int iTo = com.lesavsoft.remadbexec.syscall.BlockingSystemCall.KDEFAULT_BLOCKING_TIMEOUT;
    protected Session iSession = null;
    protected StatusWrapper iStatus = null;
    
    /**
     * Basic constructor
     * @param cmd command that will be executed remotely
     * @param con initialized and open SSH connection
     * @param timeout timeout for the command execution (0 - forever)
     */
    public SSHExecCall( String cmd, SSHConnection con, int timeout ){
        iCmd = cmd;
        iConnection = con;
        iTo = timeout;
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
        
        iSession = iConnection.getSession();
        iSession.execCommand(iCmd);
        
        // conditions for processing the streams. Such processing will not exit the call execution.
        int conditions = ChannelCondition.EXIT_STATUS|ChannelCondition.STDOUT_DATA|ChannelCondition.STDERR_DATA;
        
        // condition for exiting the call processing
        int conditionsExit = ChannelCondition.EXIT_STATUS;
        
        int status;
        BufferedReader stdbr = null;
        BufferedReader errbr = null;
        
        Timer timer = new Timer();
        iStatus = new StatusWrapper();
        iStatus.isRunning = true;
            

        // if iTo is specified, then we will create timer task to terminate the call by closing session.
            if( iTo != 0 ){
                timer.schedule((new TimerTask() {
                @Override
                public void run() {
                    if(null!=iSession ){
                        iSession.close();
                        iStatus.isRunning = false;
                        }
                    }
                }), iTo);
            }
            
            // start the stream processing
            do{
                status = iSession.waitForCondition(conditions, iTo*2);
                if( (status&ChannelCondition.STDOUT_DATA) != 0 ){
                    stdbr = reopenBR(stdbr, iSession.getStdout());
                    readStream(out, stdbr);
                }
    
                if( (status&ChannelCondition.STDERR_DATA) != 0 ){
                    errbr = reopenBR(errbr, iSession.getStderr());
                    readStream(err, errbr);
                }
            }
            while((((conditionsExit)&status)==0)&&iStatus.isRunning);
            
        // processing has ended, we try to clean up the streams, that are stil open.
        try{
            stdbr.close();
        }catch(Exception ex){
            // consume exception
        }
        try{
            errbr.close();
        }catch(Exception ex){
            // consume exception
        }
        out.eos();
        err.eos();
        
        // at this point we check if the execution has exited due to timeout or normaly.
        if( !iStatus.isRunning ){
            throw new IOException("Command timed out at "+ iTo);
        }
        // calling interrupt to close the session
        interrupt();
    }
    
    protected void readStream(ILineConsumer cm, BufferedReader br) throws IOException{
        try{
            String line = br.readLine();
            while( null != line ){
                // logger.info("**" + line);
                cm.consume(line);
                line = br.readLine();
            }
        }catch( Exception ex ){
            // just in case we have some exception and br remains to be open
            br.close();
            throw ex;
        }
   }
    
    protected BufferedReader reopenBR(BufferedReader br, InputStream src) throws IOException{
        if( br != null ){
            br.close();
        }
        return new BufferedReader(new InputStreamReader(new StreamGobbler(src)));
    }

    /**
     * The exec 'call' is considered to be blocking. It should be interruptable by the 'interrupt' call. 
     */
    @Override
    public void interrupt() {
        if(null!=iSession ){
            iSession.close();
            iStatus.isRunning = false;
        }
    }

}
