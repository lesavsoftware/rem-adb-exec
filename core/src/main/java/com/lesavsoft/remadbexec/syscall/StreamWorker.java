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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.lesavsoft.remadbexec.common.ILineConsumer;


/**
 * This file is a part of remadbexec library.
 * 
 * Internal utility class. Objects of type StreamWorker are intended to be used as a parallelized 
 * buffered stream readers. When the stream ends, the StreamWorker sends EoS notification to the 
 * consumer and terminates.
 *
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 *
 */
public final class StreamWorker implements Runnable{
    BufferedReader iReader;
    ILineConsumer iConsumer;
    
    protected static Logger logger = Logger.getLogger(StreamWorker.class);
    
    /**
     * Constructor
     * @param aReader stream reader object created on the target stream
     * @param aConsumer consumer object that will get date from the stream.
     */
    public StreamWorker(InputStreamReader aReader, ILineConsumer aConsumer){
        iReader = new BufferedReader(aReader);
        iConsumer = aConsumer;
    }
    
    @Override
    public void run() {
        String line = "";
            try {
                while ((line = iReader.readLine()) != null ) {
                    iConsumer.consume(line);
                }
                // notify the consumer about end of stream
                iConsumer.eos();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
    }

}
