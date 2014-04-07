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

import java.io.File;
import java.io.IOException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.ConnectionInfo;
import ch.ethz.ssh2.ServerHostKeyVerifier;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.SCPClient;

/**
 * This file is a part of remadbexec library.
 * 
 * SSHConnection class provides a convenience API wrapper with reduced capabilities around the Ganymed SSH-2 for Java library.
 * 
 * @author Vladimir Moltchanov (vladimir.moltchanov@lesavsoft​.com) 
 */
public class SSHConnection {
    protected Connection conn = null;
    protected ConnectionInfo info = null;
    
    protected String iHost = null;
    protected String iPwd = null;
    protected File iKey = null;
    protected String iUser = null;
    protected int iTo = 0;
    protected int iKexTo = 0;
    public static int DEFAULT_TO = 60000;
    public static int DEFAULT_KEXTO = 60000;

    
    /**
     *  local positive host key verifier to remove the need of first time login to the servers
     */
    protected class CPositiveKeyVerifier implements ServerHostKeyVerifier{
        @Override
        public boolean verifyServerHostKey(String arg0, int arg1, String arg2,
                byte[] arg3) throws Exception {
            // always accept
            return true;
        }
        
    }
    
    /**
     * Default constructor
     */
    public SSHConnection(){
        setTOs(DEFAULT_TO, DEFAULT_KEXTO);
    }
    
    /**
     * Default constructor with custom timeout values
     * @param timeout connection timeout value
     * @param kex_timeout key exchange timeout value
     */
    public SSHConnection(int timeout, int kex_timeout){
        setTOs(timeout, kex_timeout);
    }
    
    protected void setTOs(int timeout, int kex_timeout){
        iTo = timeout;
        iKexTo = kex_timeout;
    }
    
    /**
     * Get connection's destination host 
     * @return host name/ip
     */
    public String getHost(){
        return iHost;
    }

    protected ConnectionInfo connect() throws IOException {
        return conn.connect(
                new CPositiveKeyVerifier(),iTo,iKexTo);
    }

    /**
     * Attempt to login to host
     * @param host host name/ip
     * @param user user name
     * @param pwd user password
     * @throws IOException
     */
    public void login(String host, String user, String pwd ) throws IOException {
        setParams(host, user, pwd, null);
        conn = new Connection(host);
        info = conn.connect();
        
        if (!conn.authenticateWithPassword(user, pwd)){
            throw  new IOException("Cant authenticate with user " + user + " and password " + pwd);
        }
    }

    /**
     * Attempt to login to host
     * @param host host name/ip
     * @param user user name
     * @param aFile key file
     * @param pwd password for the key file ("" if none)
     * @throws IOException
     */
    public void login(String host, String user, File aFile, String pwd ) throws IOException {
        setParams(host, user, pwd, aFile);
        if( !aFile.exists()){
            throw  new IOException("Cant read key file " + aFile.getAbsolutePath());
        }
        conn = new Connection(host);
        info = conn.connect();
        
        if(!conn.authenticateWithPublicKey(user, aFile, pwd)){
            throw  new IOException("Cant authenticate with user " + user + " and password " + pwd);
        }
    }
    
    
    protected void setParams(String host, String user, String pwd, File key){
        iHost = host;
        iUser = user;
        iPwd = pwd;
        iKey = key;
    }

    /**
     * Try to re-login (might be tried if connection has timed out)
     * @throws IOException
     */
    public void login( ) throws IOException {
        if( conn.isAuthenticationComplete() ){
            return; // nothing to re-attempt, we should be connected
        }
        
        if( ( iHost == null) || (iPwd == null) || (iUser == null) ){
            throw new IOException("Server is conenction parameters are not set!");
        }
        
        if( iKey == null ){
            login(iHost, iUser, iPwd );
        }else{
            login(iHost, iUser, iKey ,iPwd );
        }
    }

    /**
     * Close the connection
     */
    public void logout(){
        conn.close();
    }

    /**
     * Check if connected to server
     * @return true if connected
     */
    public boolean isConnected(){
        return ( conn.isAuthenticationComplete() );
    }
    
    /**
     * Creates a new "session" within the connection. Note, that each new call to the host requires a new session.
     * @return new session object
     * @throws IOException
     */
    public Session getSession() throws IOException {
        if(!isConnected()){
            throw new IOException("Trying to get a session, when not connected!");
        }
        return conn.openSession();
    }
    
    /**
     * Gets a new SCPClient object
     * @return SCPClient object
     */
    public SCPClient getSCPClient(){
        return new SCPClient(conn);
    }
}
