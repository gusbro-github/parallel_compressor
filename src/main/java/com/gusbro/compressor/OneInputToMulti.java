/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gusbro.compressor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author gusbro
 */
public class OneInputToMulti
{
    private int BUFFER_SIZE = 16 << 20; // 16Mb
    private int currentFileId;
    private Path sourcePath;
    private String baseName;
    public OneInputToMulti(Path sourcePath, String baseName)
    {
        this.sourcePath = sourcePath;
        this.baseName = baseName;
        currentFileId = 0;
    }
    
    private BufferedInputStream inputStream;
    public boolean process(IOneToMultiProcessor processor) throws IOException
    {
        byte [] buffer = new byte[BUFFER_SIZE];
        byte [] header = new byte[6];
        while(true)
        {
            if(!read(header, header.length))
                return false;
            int id = getId(header);
            int size = getSize(header);
            byte cmd = getCmd(header);
            if(id == 255)
            {
                return processor.close();
            }
            if(size > buffer.length)
                buffer = new byte[size];
            if(!read(buffer, size))
                return false;
            if(!processor.process(id, cmd, buffer, size))
                return false;
        }
    }
    
    private int getId(byte[] header)
    {
        return ((int)header[0])&0xFF;
    }

    private byte getCmd(byte[] header)
    {
        return header[1];
    }
    
    private int getSize(byte [] header)
    {
        return ((int)((int)header[2])&0xFF)+
               ((((int)header[3])&0xFF)<<8) +
               ((((int)header[4])&0xFF)<<16) +
               ((((int)header[5])&0xFF)<<24);
        
    }
    
    private boolean read(byte[] buffer, int len) throws IOException
    {
        int currentSize = 0, currentRead = 0;
        while(len != 0)
        {
            if(inputStream == null || currentRead == -1)
            {
                if(inputStream != null)
                    inputStream.close();
                inputStream = new BufferedInputStream(new FileInputStream(sourcePath.resolve(nextFileName()).toFile()));
            }
            if((currentRead = inputStream.read(buffer, currentSize, len)) != -1)
            {
                currentSize += currentRead;
                len -= currentRead;
            }
        }
        return len == 0;
    }
    
    private String nextFileName()
    {
        return String.format("%s.%03d", baseName, currentFileId++);
    }
}
