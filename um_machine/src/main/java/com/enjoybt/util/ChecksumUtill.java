package com.enjoybt.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ChecksumUtill {

//    public long getCRC32Value(String filename, byte[] filesize) {
    public long getCRC32Value(String filename) {
        Checksum crc = (Checksum) new CRC32();
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
            File f = new File(filename);
            byte[] buffer = new byte[(int) f.length()];
//            byte[] buffer = filesize;
            int length = 0;
            
            while ((length = in.read(buffer)) >= 0) {
                crc.update(buffer, 0, length);
            }
            in.close();
            
        } catch (IOException e) {
            System.err.println(e);
            System.exit(2);
        }
        System.out.println("checksum :" + crc.getValue());
        return crc.getValue();
    }

    public long getRemoteCRC32Value(InputStream in,long fileSize) {
        Checksum crc = (Checksum) new CRC32();
        
        try {
            BufferedInputStream bf = new BufferedInputStream(in);
            byte[] buffer = new byte[(int) fileSize];
//            byte[] buffer = filesize;
            int length = 0;
            
            while ((length = bf.read(buffer)) >= 0) {
                crc.update(buffer, 0, length);
            }
            bf.close();
            
        } catch (IOException e) {
            System.err.println(e);
            System.exit(2);
        }
        System.out.println("checksum :" + crc.getValue());
        return crc.getValue();
    }

}
