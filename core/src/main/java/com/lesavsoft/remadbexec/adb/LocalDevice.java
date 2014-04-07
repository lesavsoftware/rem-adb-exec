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

import com.lesavsoft.remadbexec.common.ICall;
import com.lesavsoft.remadbexec.syscall.BlockingSystemCall;

public class LocalDevice extends Device {
    public LocalDevice( String id ){
        super(id);
    }

    @Override
    public ICall prepareCall(String cmd) {
        return new BlockingSystemCall(prepCommand(cmd),0);
    }

    @Override
    public String push(String local, String remote) {
        return String.format("push %s %s", local, remote);
    }

    @Override
    public String pull(String remote, String local, String finalDest) {
        return String.format("pull %s %s", remote, finalDest); 
    }

    @Override
    public void completePull(String remote, String local) {
        // nothing to do for local pull
    }

    @Override
    public String install(String path) {
        return String.format("install %s", path); 
    }

}
