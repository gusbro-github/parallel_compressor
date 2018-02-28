/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gusbro.compressor;

/**
 *
 * @author gusbro
 */
public class Constants
{
    public static int DEFAULT_WORKER_THREADS_COUNT = 5;  // Default number of compressor threads
    public static String BASE_NAME = "Archive";
    public static final byte FOLDER_MARKER = 1;
    public static final byte FILE_MARKER = 2;
    public static final byte MARKER_CAN_READ = 4;
    public static final byte MARKER_CAN_WRITE = 8;
    public static final byte MARKER_CAN_EXECUTE = 16;
    public static final byte MARKER_NONE = 0;    
    
    public static final byte CMD_START_FILE = 1;
    public static final byte CMD_WRITE_FILE = 2;
    public static final byte CMD_NONE = 0;
}
