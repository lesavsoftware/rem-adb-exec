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
 * It is possible that some adb access functionality will produce lots of
 * output that could be pre-filtered already at the low levels of the communication stack.
 * However, exact filtering parameters are only known by the end-application. For that reason
 * many of the low level access routines are using this simple interface as a dependency injection.  
 * 
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 *
 */
public interface ILineFilter {
    /**
     * Implementing class will return true if this line should be accepted, false if the line should be ignored
     * @param line
     * @return should this line be accepted or dropped
     */
    public boolean filter( String line );
}
