package com.greymass.esr.util;

import com.greymass.esr.ESRException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionUtil {
    public static byte[] decompressByteArray(byte[] data) throws DataFormatException, IOException, ESRException {
        Inflater inflater = new Inflater(true);
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        return outputStream.toByteArray();

    }

    public static byte[] compressByteArray(byte[] bytes){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater dfl = new Deflater(Deflater.BEST_COMPRESSION, true);
        dfl.setInput(bytes);
        dfl.finish();
        byte[] tmp = new byte[4*1024];
        try{
            while(!dfl.finished()){
                int size = dfl.deflate(tmp);
                baos.write(tmp, 0, size);
            }
        } catch (Exception ex){

        } finally {
            try{
                if(baos != null) baos.close();
            } catch(Exception ex){}
        }

        return baos.toByteArray();
    }
}
