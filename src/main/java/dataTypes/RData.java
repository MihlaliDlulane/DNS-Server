package dataTypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class RData {


    public static final int TYPE_A = 1;
    public static final int TYPE_NS = 2;
    public static final int TYPE_CNAME = 5;
    public static final int TYPE_MX = 15;
    public static final int TYPE_TXT = 16;
    public static final int TYPE_AAAA = 28;

    // A Record - IPv4 address (4 bytes)
    public static byte[] createA(String ipAddress) {
        String[] octets = ipAddress.split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException("Invalid IPv4 address");
        }

        byte[] rdata = new byte[4];
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(octets[i]);
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("Invalid octet value: " + octet);
            }
            rdata[i] = (byte) octet;
        }
        return rdata;
    }

    // AAAA Record - IPv6 address (16 bytes)
    public static byte[] createAAAA(String ipv6Address) {
        try {
            InetAddress addr = InetAddress.getByName(ipv6Address);
            if (addr instanceof Inet6Address) {
                return addr.getAddress();
            }
            throw new IllegalArgumentException("Not a valid IPv6 address");
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPv6 address", e);
        }
    }

    // NS Record - Name Server (domain name)
    public static byte[] createNS(String nameServer) {
        return encodeDomainName(nameServer);
    }

    // CNAME Record - Canonical name (domain name)
    public static byte[] createCNAME(String canonicalName) {
        return encodeDomainName(canonicalName);
    }

    // MX Record - Mail Exchange (preference + domain name)
    public static byte[] createMX(int preference, String mailServer) {
        byte[] domainBytes = encodeDomainName(mailServer);
        byte[] rdata = new byte[2 + domainBytes.length];

        // Preference (2 bytes, big-endian)
        rdata[0] = (byte) (preference >> 8);
        rdata[1] = (byte) (preference & 0xFF);

        // Mail server domain
        System.arraycopy(domainBytes, 0, rdata, 2, domainBytes.length);

        return rdata;
    }

    // TXT Record - Text data
    public static byte[] createTXT(String text) {
        // TXT records use character strings (length byte + data)
        // Can be multiple strings, but we'll do single string for simplicity
        if (text.length() > 255) {
            throw new IllegalArgumentException("TXT string too long (max 255)");
        }

        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] rdata = new byte[1 + textBytes.length];

        rdata[0] = (byte) textBytes.length;
        System.arraycopy(textBytes, 0, rdata, 1, textBytes.length);

        return rdata;
    }

    // Helper method to encode domain names
    private static byte[] encodeDomainName(String domainName) {
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        String[] labels = domainName.split("\\.");

        for (String label : labels) {
            if (label.length() > 63) {
                throw new IllegalArgumentException("Label too long: " + label);
            }
            encoded.write((byte) label.length());
            try {
                encoded.write(label.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        encoded.write(0); // Null terminator
        return encoded.toByteArray();
    }

    // Create RDATA based on type
    public static byte[] create(int type, String... params) {
        return switch (type) {
            case TYPE_A -> createA(params[0]);
            case TYPE_AAAA -> createAAAA(params[0]);
            case TYPE_NS -> createNS(params[0]);
            case TYPE_CNAME -> createCNAME(params[0]);
            case TYPE_MX -> createMX(Integer.parseInt(params[0]), params[1]);
            case TYPE_TXT -> createTXT(params[0]);
            default -> throw new IllegalArgumentException("Unsupported record type: " + type);
        };
    }
}