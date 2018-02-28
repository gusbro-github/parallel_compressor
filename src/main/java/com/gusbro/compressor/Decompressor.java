/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gusbro.compressor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
    private HashMap<Integer, IDecompressor> processors;
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
        IDecompressor decompressor = processors.get(id);
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
        boolean ok = true;
        for(IDecompressor decompressor:processors.values())
            ok &= decompressor.close();
        return ok;
    }
    
    class OutputDecompressor implements IDecompressor
    {
        private File file;
        private BufferedOutputStream output;
        
        @Override
        public boolean initialize()
        {
            return true;
        }

        @Override
        public boolean process(byte cmd, byte[] buffer, int size) throws IOException
        {
            switch(cmd)
            {
                case Constants.CMD_START_FILE:
                {
                    if(output != null)
                    {
                        output.close();
                        output = null;
                    }
                    String subName = new String(buffer, 1, size - 1, StandardCharsets.UTF_8);
                    System.out.println(String.format("FileToDecompress: %s", subName));
                    file = new File(destPath, subName);

                    boolean canRead = (buffer[0] & Constants.MARKER_CAN_READ) != 0;
                    boolean canWrite = (buffer[0] & Constants.MARKER_CAN_WRITE) != 0;
                    boolean canExecute = (buffer[0] & Constants.MARKER_CAN_EXECUTE) != 0;
                    if((buffer[0] & Constants.FOLDER_MARKER) != 0)
                    { // It's a folder
                        file.mkdirs();
                    }else
                    { // It's a file
                        file.getParentFile().mkdirs();
                        output = new BufferedOutputStream(new FileOutputStream(file));
                    }
                    file.setReadable(canRead);
                    file.setWritable(canWrite);
                    file.setExecutable(canExecute);
                    break;
                }
                case Constants.CMD_WRITE_FILE:
                {
                    if(output == null)
                        return false;
                    output.write(buffer, 0, size);
                    break;
                }
                default: return false;
            }
            return true;
        }

        @Override
        public boolean close() throws IOException
        {
            if(output != null)
                output.close();
            output = null;
            return true;
        }
        
    }
}
