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

package com.lesavsoft.remadbexec.common.consumers;

import org.apache.log4j.Logger;

import com.lesavsoft.remadbexec.common.ILineConsumer;

/**
 * This file is a part of remadbexec library.
 * 
 * LineConsumerErrlog implements ILineConsumer interface and directs all the lines to the log4j logger.
 *  
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 *
 */
public class LineConsumerErrlog implements ILineConsumer{
    protected static Logger logger = Logger.getLogger(LineConsumerErrlog.class);
    @Override
    public void consume(String line) {
        logger.error(line);
    }
    @Override
    public void eos() {
        logger.error("(EOS event)");
    }
}
