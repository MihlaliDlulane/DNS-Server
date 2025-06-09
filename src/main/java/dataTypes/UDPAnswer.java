package dataTypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UDPAnswer {

    public static byte[] createAnswer(byte[] domainName,int type,int questionClass,int ttl,byte[] rData ){

        int rdLength = rData.length;
        // Validate rdLength
        if(rdLength > 65535) {
            throw new IllegalArgumentException("RDATA too long");
        }
        if(ttl < 0) {
            throw new IllegalArgumentException("TTL cannot be negative");
        }

        if(type < 1 || type > 65535){
            throw new IllegalArgumentException("Invalid question type");
        }
        if(questionClass < 1 || questionClass > 65535){
            throw new IllegalArgumentException("Invalid class type");
        }

        ByteArrayOutputStream answer = new ByteArrayOutputStream();

        try {
            answer.write(domainName);

            // write type
            answer.write((byte)(type >> 8));
            answer.write((byte)(type & 0xFF));

            //question class
            answer.write((byte)(questionClass >> 8));
            answer.write((byte)(questionClass & 0xFF));

            //Time to live
            answer.write((byte)(ttl >> 24));
            answer.write((byte)(ttl >> 16));
            answer.write((byte)(ttl >> 8));
            answer.write((byte)(ttl & 0xFF));

            //RD Length
            answer.write((byte)(rdLength >> 8));
            answer.write((byte)(rdLength & 0xFF));

            answer.write(rData);


        } catch (IOException e){
            throw new RuntimeException(e);
        }

        return answer.toByteArray();
    }
}
