# Remote Adb Execution Library
_remadbexec library by LeSav Software Tmi_

## Intro
The purpose of this library is to abstract the usage of adb both: local and remote. Depending on the
target device, the commands will be executed locally with system calls, or remotely over the SSH, using the
Ganymed (http://www.ganymed.ethz.ch/ssh2/) library. 

In a nutshell, this library should enable developers to create configurable distributed systems, 
where device is no longer required to be locally connected to the host machine.

The functionality of this library is a limited set of what adb tool is capable of. However, it was enough
for our purpose that has defined the scope. Current functionality includes:
* adb push [path_to_local_file] [path_to_remote_file]
* adb pull [path_to_remote_file] [path_to_local_file]
* adb install [path_to_adb]
* adb [any_arbitrary_command]

Optionally, target device id is set, so the command will become:
* adb -s xxxxxxxx push etc.


## License

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

## Usage
The whole library was designed with the ease of usage in mind. "RemAdbTester" tool source code could be used as a good reference of how the library is to be used. It is located in tools/remadbtester folder.

First of all, one needs to create an instance of AdbWrapperHost
```java
        AdbWrapperHost adbHost = null;
        try {
            adbHost = new AdbWrapperHost(AdbWrapperHost.readPropertiesFromFile(args[0]));
            // setting the temporary file names to be static
            // remove the following line to have a new name for each command.
            adbHost.setFileName(adbHost.getDeviceId()+"_tmp", true);
        } catch (EInvalidParameters | IOException e) {
            System.out.println("Unable to instanciate AdbWrapperHost object: " + e.getMessage());
        }
```
It accepts Property set as a constructor parameter. 

By default, when sending/receiving files over to/from the remote device, each file on the remote host machine will be saved under some generated name, which is attempted to be unique. If you don't need this functionality, then make the call:
```java
            adbHost.setFileName(adbHost.getDeviceId()+"_tmp", true);
```
to fix all the transient files to a single name on the device host machine.

After the creation of AdbWrapperHost, calls to the actual adb functionality could be made:
```java
    // calling install with 10sec timeout
    adbHost.install(pathToApk, new LineConsumerStdout("std: "), new LineConsumerStdout("err: "), 10000);
    
    // calling push with 10sec timeout
    adbHost.push(localFile, remoteFile, new LineConsumerStdout("std: "), new LineConsumerStdout("err: "), 10000);

    // calling pull with 10sec timeout    
    adbHost.pull(remoteFile, localFile, new LineConsumerStdout("std: "), new LineConsumerStdout("err: "), 10000);

    // calling arbitrary adb command with 10sec timeout
    adbHost.exec(vCmd.trim(), new LineConsumerStdout("std: "), new LineConsumerStdout("err: "), 10000);
```

## Setting up remote host and security considerations

Connection to the remote host is created over SSHv2 protocol using key authentication. So in order to enable one or another host to be part of the system, you need to create a key pair and public key should be added to the "authorized_hosts" file on the target system. Private key should be copied to the system where you run your code with remadbexec library and used  as one of the Properties parameters.

For more information on ssh key exchange read, for example, (https://help.ubuntu.com/community/SSH/OpenSSH/Keys).

Note, however, that while using of ssh connection protects the destination host from unauthorized access outside of your application, your application will be authorized to do as much as permitted to the user with the given key. And the commands that are executed  remotely are not marshaled in any way, hence anyone who has authorization to run your application will have authorization to run an arbitrary command on the remote system. 

## Sample property files:

### for the local generic adb:
```
#local device type - device is connected to local USB
device.type = local

#device id should be the one from devices list, or empty for no -s switch 
device.id = 
```

### for the local device: 
```
#local device type - device is connected to local USB
device.type = local

#device id should be the one from devices list, or empty for no -s switch 
device.id = abcabcabcabcabcd
```


### for the remote ssh generic adb:
```
#local device type - device is connected to local USB
device.type = ssh

#device id should be the one from devices list, or empty for no -s switch 
device.id = 

device.key.file = ../conf/a_key_rsa
device.key.user = auser
device.key.pwd =
device.host = 192.168.1.2
```

### for the remote ssh device:
```
#local device type - device is connected to local USB
device.type = ssh

#device id should be the one from devices list, or empty for no -s switch 
device.id = abcabcabcabcabcd

device.key.file = ../conf/a_key_rsa
device.key.user = auser
device.key.pwd =
device.host = 192.168.1.34
```


## AoB

This is a very first version of the remote adb execution library. It was tailored to meet internal needs.  For now there are two items that might be addressed:
* Based on the feedback I might explore new use cases and expand the functionality. 
* Due to the exploratory nature of the project, there QA side was delayed. Some unit/integration tests will be beneficial for the future library extensions. 

## Verified to be working
* on Win7 locally connected device
* on remote Win7 with bitvise ssh server (https://www.bitvise.com/ssh-server)