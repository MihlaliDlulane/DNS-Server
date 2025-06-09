package dataTypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UDPQuestion {

    public static byte[] createUDPQuestion(String domainName,int type,int questionClass){
        if(type < 1 || type > 65535){
            throw new IllegalArgumentException("Invalid question type");
        }
        if(questionClass < 1 || questionClass > 65535){
            throw new IllegalArgumentException("Invalid class type");
        }

        ByteArrayOutputStream question = new ByteArrayOutputStream();

        try {
            byte[] encodedDomainame = domainNameEncode(domainName);
            question.write(encodedDomainame);

            // write type
            question.write((byte)(type >> 8));
            question.write((byte)(type & 0xFF));

            //question class
            question.write((byte)(questionClass >> 8));
            question.write((byte)(questionClass & 0xFF));


        } catch (IOException e){
            throw new RuntimeException(e);
        }

        return question.toByteArray();
    }

    private static byte[] domainNameEncode(String url){
        ByteArrayOutputStream encodeName = new ByteArrayOutputStream();
        String[] labels = url.split("\\.");

        for(String label: labels){
            if(label.length() > 63) {
                throw new IllegalArgumentException("domain name label past character limit");
            }
            encodeName.write((byte) label.length());

            try {
                encodeName.write(label.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e){
                throw new RuntimeException(e);
            }
        }

        if(encodeName.size() > 255){
            throw new IllegalArgumentException("domain name above byte limit");
        }

        encodeName.write(0);

        return encodeName.toByteArray();
    }

}
