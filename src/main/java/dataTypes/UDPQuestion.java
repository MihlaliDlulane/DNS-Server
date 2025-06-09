package dataTypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UDPQuestion {

    private final byte[] domainName;
    private final int type;
    private final int questionClass;

    public UDPQuestion(byte[] buf){
        this.domainName = extractDomainName(buf,12);
        int domainNameLen = getDomainNameLength(buf,12);
        int pos = 12 + domainNameLen;
        this.type = ((buf[pos] & 0xFF) << 8 )| (buf[pos+1] & 0xFF);
        this.questionClass = ((buf[pos+2] & 0xFF) << 8 )| (buf[pos+3] & 0xFF);
    }

    private static byte[] extractDomainName(byte[] data, int offset){
        ByteArrayOutputStream name = new ByteArrayOutputStream();
        int pos = offset;

        while(pos <data.length && data[pos] != 0){
            int len = data[pos] & 0xFF;
            name.write(data[pos]); // Length byte
            pos++;

            //Copy label
            name.write(data,pos,len);
            pos += len;
        }
        name.write(0); // Null terminator

        return name.toByteArray();
    }

    private static int getDomainNameLength(byte[] data, int offset){
        int pos = offset;
        while(pos< data.length && data[pos] !=0){
            int len = data[pos] & 0xFF;
            pos += len +1;
        }
        return (pos - offset) + 1;
    }

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

    public static int parseQuestionLength(byte[] data, int offset) {
        int pos = offset;

        // Skip the domain name
        while (pos < data.length && data[pos] != 0) {
            int labelLength = data[pos] & 0xFF;
            pos += labelLength + 1;
        }
        pos++; // Skip the null terminator

        // Add 4 bytes for QTYPE and QCLASS
        pos += 4;

        return pos - offset;
    }

    public byte[] getDomainName() {
        return domainName;
    }

    public int getQuestionClass() {
        return questionClass;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        // Decode domain name from bytes
        String decodedDomain = decodeDomainName(domainName);

        // Get string representations of type and class
        String typeStr = getTypeString(type);
        String classStr = getClassString(questionClass);

        return String.format("Question: %s %s %s", decodedDomain, classStr, typeStr);
    }

    // Helper method to decode domain name from DNS format
    private String decodeDomainName(byte[] encodedName) {
        StringBuilder domain = new StringBuilder();
        int pos = 0;

        while (pos < encodedName.length && encodedName[pos] != 0) {
            int labelLength = encodedName[pos] & 0xFF;
            pos++;

            if (!domain.isEmpty()) {
                domain.append(".");
            }

            domain.append(new String(encodedName, pos, labelLength, StandardCharsets.UTF_8));
            pos += labelLength;
        }

        return domain.toString();
    }

    // Get human-readable type string
    private String getTypeString(int type) {
        return switch (type) {
            case 1 -> "A";
            case 2 -> "NS";
            case 5 -> "CNAME";
            case 6 -> "SOA";
            case 12 -> "PTR";
            case 15 -> "MX";
            case 16 -> "TXT";
            case 28 -> "AAAA";
            case 33 -> "SRV";
            default -> "TYPE" + type;
        };
    }

    // Get human-readable class string
    private String getClassString(int questionClass) {
        return switch (questionClass) {
            case 1 -> "IN";
            case 2 -> "CS";
            case 3 -> "CH";
            case 4 -> "HS";
            default -> "CLASS" + questionClass;
        };
    }
}
