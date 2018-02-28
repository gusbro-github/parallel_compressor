/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gusbro.compressor;

import java.io.IOException;

/**
 *
 * @author gusbro
 */
public interface IOneToMultiProcessor
{
    public boolean process(int id, byte cmd, byte [] buffer, int size) throws IOException;
    public boolean close() throws IOException;
}
