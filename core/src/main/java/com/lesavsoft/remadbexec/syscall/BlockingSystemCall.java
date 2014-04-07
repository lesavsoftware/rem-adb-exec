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
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.lesavsoft.remadbexec.common.ICall;
import com.lesavsoft.remadbexec.common.ILineConsumer;
import com.lesavsoft.remadbexec.common.StatusWrapper;

/**
 * This file is a part of remadbexec library.
 *
 * Defines the blocking system call object. When the call is issued, the object method tries to wait for the
 * process and worker threads to finish.  
 * 
 * sample usage of this class could be found in:
 *      com.lesavsoft.remadbexec.tools.cmdtester.CmdTester.java
 * 
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 *
 */
public final class BlockingSystemCall extends SystemCall implements ICall{
    protected static Logger logger = Logger.getLogger(BlockingSystemCall.class);
    public static final int KDEFAULT_BLOCKING_TIMEOUT = 60000; // 1 minute timeout

    protected int iTo = KDEFAULT_BLOCKING_TIMEOUT;
    
    /**
     * basic constructor
     * 
     * @param cmd
     * @param timeout 0 - never timeout, timeout in milliseconds 
     */
    public BlockingSystemCall(String cmd, int timeout) {
        super(cmd);
        iTo = timeout;
    }
    
    /**
     * executes the command (that was passed in the constructor)
     * @param out listener of the output lines produced by the allocated StreamWorker thread
     * @param err listener of the error lines produced by the allocated StreamWorker thread
     * @throws IOException
     */
    public void exec(ILineConsumer out, ILineConsumer err) throws IOException{
        // running the process in a non-blocking mode
        super.exec(out, err);
        
        // creating logic to interrupt the process if
        // it will run longer then given time
        final StatusWrapper status = new StatusWrapper();
        status.isRunning = true;
    
        Timer timer = new Timer();

        try {
                if( iTo != 0 ){
                    timer.schedule((new TimerTask() {
                    @Override
                    public void run() {
                        if(null!=iProcess){
                            iProcess.destroy();
                            status.isRunning = false;
                            }
                        }
                    }), iTo);
            }
            logger.info("waiting for the process and readers to complete...");
            
            // block for normal exit, but will be interrupted, when timeout Task activates
            waitFor();
            logger.info("Process is completed");
        } catch (InterruptedException e) {
            throw new IOException("Process was interrupted!");
        }
        if( !status.isRunning ){
            throw new IOException("Command timed out at " + iTo);
        }
    }

}
