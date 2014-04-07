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

package com.lesavsoft.remadbexec.common;

import java.io.IOException;


/**
 * This file is a part of remadbexec library.
 * 
 * The ICall Interface defines methods that should be implemented by different remote/local execution classes
 * 
 *
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 */
public interface ICall {
    /**
     * Start executing the method. Method name, call parameters, and other environment variables should be already setup 
     * before this call is made. 
     * @param out implementation of LineConsumer for standard output stream. 
     * @param err implementation of LineConsumer for error output stream.
     * @throws IOException
     */
    public void exec( ILineConsumer out, ILineConsumer err) throws IOException;

    /**
     * The exec 'call' is considered to be blocking. It should be interruptable by the 'interrupt' call. 
     */
    public void interrupt();
}
