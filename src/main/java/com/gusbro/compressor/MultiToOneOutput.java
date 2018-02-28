/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gusbro.compressor;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author gusbro
 */
public class MultiToOneOutput
{
    private int currentFileId;
    private int maxSize;
    private Path destPath;
    private String baseName;
    private int currentSize;
    private BufferedOutputStream currentOutputStream = null;
    public MultiToOneOutput(Path destPath, String baseName, int maxSizeMB)
    {
        this.destPath = destPath;
        this.baseName = baseName;
        this.maxSize = maxSizeMB << 20;
        currentFileId = 0;
        currentSize = maxSize;
        destPath.toFile().mkdirs();
        closed = false;
    }
    
    private boolean closed;
    public void close()throws IOException
    {
        if(currentOutputStream != null)
        {
            write(255, Constants.CMD_NONE, new byte[0], 0);
            currentOutputStream.close();
            closed = true;
        }
    }
    
    public void write(int id, byte cmd, byte[] buffer, int size) throws IOException
    {
        if(closed)
            throw new IllegalArgumentException("Output is closed");
        byte [] header = new byte[]{ (byte)(id&0xFF), cmd, (byte)(size&0xFF), (byte)((size>>8)&0xFF), (byte)((size>>16)&0xFF), (byte)((size>>24)&0xFF) };
        
        write(header, header.length);
        write(buffer, size);
    }
    
    private void write(byte [] buffer, int size)throws IOException
    {
        int offset = 0;
        while(size > 0)
        {
            if(currentSize == maxSize)
            {
                if(currentOutputStream != null)
                    currentOutputStream.close();
                currentOutputStream = new BufferedOutputStream(new FileOutputStream(destPath.resolve(nextFileName()).toFile()));
                currentSize = 0;
            }
            int currentChunk = size > (maxSize-currentSize) ? (maxSize-currentSize) : size;
            currentOutputStream.write(buffer, offset, currentChunk);
            offset += currentChunk;
            currentSize += currentChunk;
            size -= currentChunk;
        }
    }
    
    private String nextFileName()
    {
        return String.format("%s.%03d", baseName, currentFileId++);
    }
    
}
