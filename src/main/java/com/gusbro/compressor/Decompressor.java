/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gusbro.compressor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 *
 * @author gusbro
 */
public class Decompressor implements IOneToMultiProcessor
{
    public static void main(String [] args)
    {
        String [] sourcePath = new String[1];
        String [] destPath = new String[1];
        if(!parseArgs(args, sourcePath, destPath))
            return;
        Decompressor decompressor = new Decompressor();
        try
        {
            decompressor.decompress(sourcePath[0], destPath[0]);
        }catch(Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
    
    private static boolean parseArgs(String [] args, String[] sourcePath, String []destPath)
    {
        if(args.length < 2)
        {
            System.err.println("Decompressor parameters: SourceDirectory DestinationDirectory");
            return false;
        }
        sourcePath[0] = args[0];
        destPath[0] = args[1];
        return true;
    }
    
    private File sourcePath, destPath;
    private HashMap<Integer, OutputDecompressor> processors;
    public boolean decompress(String iSourcePath, String iDestPath)throws IOException
    {
        sourcePath = new File(iSourcePath).getCanonicalFile();        
        destPath = new File(iDestPath);
        processors = new HashMap<>();
        
        // Validate arguments
        if(!sourcePath.exists() || !sourcePath.isDirectory())
            throw new IllegalArgumentException("Source path does not exist or is not a directory");
        if(!sourcePath.canRead())
            throw new IllegalArgumentException("Source path cannot be read");
        if(destPath.isFile())
            throw new IllegalArgumentException("Destination path is an existing file");
        if(destPath.isDirectory() && !destPath.canWrite())
            throw new IllegalArgumentException("Destination path cannot be written");
        
        OneInputToMulti input = new OneInputToMulti(sourcePath.toPath(), Constants.BASE_NAME);        
        return input.process(this);
    }

    @Override
    public boolean process(int id, byte cmd, byte[] buffer, int size) throws IOException
    {
        OutputDecompressor decompressor = processors.get(id);
        if(decompressor == null)
        {
            decompressor = new OutputDecompressor();
            if(!decompressor.initialize())
                return false;
            processors.put(id, decompressor);
        }
        return decompressor.process(cmd, buffer, size);
    }

    @Override
    public boolean close() throws IOException
    {
        return true;
    }
    
    class OutputDecompressor
    {
        public boolean initialize()
        {
            return true;
        }

        public boolean process(byte cmd, byte[] buffer, int size) throws IOException
        {
            switch(cmd)
            {
                case Constants.CMD_START_FILE:
                {
                    String subName = new String(buffer, 1, size - 1, StandardCharsets.UTF_8);
                    System.out.println(String.format("FileToDecompress: %s", subName));

                    break;
                }
                case Constants.CMD_WRITE_FILE:
                {
                    // Here we would write the contents of the buffer on the current file
                    break;
                }
                default: return false;
            }
            return true;
        }        
    }    
}
