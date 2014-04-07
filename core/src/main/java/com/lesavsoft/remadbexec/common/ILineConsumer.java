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

/**
 * This file is a part of remadbexec library.
 * 
 * The ILineConsumer Interface defines methods that are triggered stream readers within command line executors.
 * 
 *
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 */
public interface ILineConsumer {
    /**
     * Method called when a new line is read from the stream.
     * @param line
     */
    public void consume(String line);
    
    /**
     * Called when end of stream is reached, or reading is interrupted.
     */
    public void eos();
}
