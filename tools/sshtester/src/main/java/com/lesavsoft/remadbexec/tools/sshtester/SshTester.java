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
package com.lesavsoft.remadbexec.tools.sshtester;

import java.io.File;
import java.io.IOException;

import com.lesavsoft.remadbexec.common.consumers.LineConsumerStdout;
import com.lesavsoft.remadbexec.sshcall.*;
import com.lesavsoft.remadbexec.syscall.BlockingSystemCall;
import com.lesavsoft.remadbexec.syscall.SystemCall;

/**
 * This file is a part of remadbexec library.
 * 
 * SshTester tool executes a command locally or over ssh. Provided to be used as a debugging tool.
 * 
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 *
 */

public class SshTester {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("SshTester tool. Copyright 2014 LeSav Software Tmi. ");
        System.out.println("http://www.apache.org/licenses/LICENSE-2.0\n\n"); 

        if( args.length == 0){
            System.out.println("usage: cmd/ssh");
            System.exit(0);
        }
        
        if( args[0].equals("ssh")){
            SSHConnection vCon = new SSHConnection();
            
            if( args.length < 7 ){
                System.out.println("usage: ssh <host> <uname> <key> <pass> <cmd> <to>");
                System.exit(0);
            }
            try {
                vCon.login(args[1], args[2], new File(args[3]), args[4]);
                SSHExecCall aCall = new SSHExecCall(args[5], vCon, Integer.parseInt(args[6]));
                aCall.exec(new LineConsumerStdout("out"), new LineConsumerStdout("err"));
                vCon.logout();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if( args[0].equals("cmd")){
            if( args.length < 3 ){
                System.out.println("Usage: CmdTester cmd \"<cmd>\" <block/noblock> [to_millis]");
                System.exit(0);
            }
            
            SystemCall aCall = null;
            
            if( args[2].equals("block")){
                if( args.length < 4 ){
                    System.out.println("blocking call requires a to value ");
                    System.exit(0);
                }
                aCall = new BlockingSystemCall(args[1], Integer.parseInt(args[3]));
            }else if(args[2].equals("noblock")){
                if( args.length < 4 ){
                    System.out.println("nonblocking call requires a to value how long we will be waiting for output");
                    System.exit(0);
                }
                aCall = new SystemCall(args[1]);
            }else{
                System.out.println("Usage: CmdTester cmd \"<cmd>\" <block/noblock> [to]");
                System.exit(0);
            }
            
            System.out.println("Executing " + args[1] + " ...");
            try {
                aCall.exec(new LineConsumerStdout("out"), new LineConsumerStdout("err"));
                if(args[1].equals("noblock")){
                    try {
                        Thread.sleep(Integer.parseInt(args[3]));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("a command type has to be specified ssh/cmd");
            System.exit(0);
        }
        System.exit(0);
    }

}