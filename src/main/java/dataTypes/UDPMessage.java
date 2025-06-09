package dataTypes;

public class UDPMessage {
    private final int ID; // Packet Identifier -A random ID assigned to query packets. Response packets must reply with the same ID.
    private final boolean QR; // Query/Response Indicator - 1 for a reply packet, 0 for a question packet.
    private final byte OPCODE; //Operation Code - Specifies the kind of query in a message.
    private final boolean AA; // Authoritative Answer - 1 if the responding server "owns" the domain queried, i.e., it's authoritative.
    private final boolean TC; //Truncation - 1 if the message is larger than 512 bytes. Always 0 in UDP responses.
    private final boolean RD; // Recursion Desired - Sender sets this to 1 if the server should recursively resolve this query, 0 otherwise.
    private final boolean RA; // Recursion Available - Server sets this to 1 to indicate that recursion is available.
    private final byte Z; // Reserved - Used by DNSSEC queries. At inception, it was reserved for future use.
    private final byte RCODE; // Response Code - Response code indicating the status of the response.
    private final int QDCOUNT; // Question Count - Number of questions in the Question section.
    private final int ANCOUNT; // Answer Record Count - Number of records in the Answer section.
    private final int NSCOUNT; // Authority Record Count  - Number of records in the Authority section.
    private final int ARCOUNT; // Additional Record Count  - Number of records in the Additional section.
    private final UDPQuestion question;

    public UDPMessage (byte[] buff){
        if(buff.length < 12){
            throw new IllegalArgumentException("Invalid header size");
        }

        this.ID = ((buff[0] & 0xFF) << 8) | (buff[1] & 0xFF);

        int flags = ((buff[2] & 0xFF) << 8) | (buff[3] & 0xFF);

        this.QR = (byte)((flags & 0x8000) >> 15) == 1;
        this.OPCODE = (byte)((flags & 0x7800) >> 11);
        this.AA = (byte)((flags & 0x400)>> 10) == 1;
        this.TC = (byte)((flags & 0x200)>>9) == 1;
        this.RD = (byte)((flags & 0x100)>>8) == 1;
        this.RA = (byte)((flags & 0x0080)>>7) == 1;
        this.Z = (byte)((flags & 0x0070)>>4);
        this.RCODE = (byte)((flags & 0x000F));


        this.QDCOUNT = ((buff[4] & 0xFF) << 8) | (buff[5] & 0xFF);
        this.ANCOUNT = ((buff[6] & 0xFF) << 8) | (buff[7] & 0xFF);
        this.NSCOUNT = ((buff[8] & 0xFF) << 8) | (buff[9] & 0xFF);
        this.ARCOUNT = ((buff[10] & 0xFF) << 8) | (buff[11] & 0xFF);

        this.question = new UDPQuestion(buff);
    }

    public static byte[] createUDPHeader(int id, boolean qr, byte opCode, boolean aa,
                                         boolean tc, boolean rd, boolean ra, byte z,
                                         byte rcode, int qdcount, int ancount,
                                         int nscount, int arcount) {

        byte[] header = new byte[12];

        // Bytes 0-1: ID
        header[0] = (byte)(id >> 8);
        header[1] = (byte)(id & 0xFF);

        // Bytes 2-3: Flags
        int flags = 0;

        // Set each flag in its correct bit position
        if (qr) flags |= 0x8000;           // Bit 15
        flags |= (opCode & 0xF) << 11;     // Bits 11-14 (mask to ensure only 4 bits)
        if (aa) flags |= 0x0400;           // Bit 10
        if (tc) flags |= 0x0200;           // Bit 9
        if (rd) flags |= 0x0100;           // Bit 8
        if (ra) flags |= 0x0080;           // Bit 7
        flags |= (z & 0x7) << 4;           // Bits 4-6 (mask to ensure only 3 bits)
        flags |= (rcode & 0xF);            // Bits 0-3 (mask to ensure only 4 bits)

        header[2] = (byte)(flags >> 8);
        header[3] = (byte)(flags & 0xFF);

        // Bytes 4-5: QDCOUNT
        header[4] = (byte)(qdcount >> 8);
        header[5] = (byte)(qdcount & 0xFF);

        // Bytes 6-7: ANCOUNT
        header[6] = (byte)(ancount >> 8);
        header[7] = (byte)(ancount & 0xFF);

        // Bytes 8-9: NSCOUNT
        header[8] = (byte)(nscount >> 8);
        header[9] = (byte)(nscount & 0xFF);

        // Bytes 10-11: ARCOUNT
        header[10] = (byte)(arcount >> 8);
        header[11] = (byte)(arcount & 0xFF);

        return header;
    }


    public int getID() {
        return ID;
    }

    public boolean isQR() {
        return QR;
    }

    public byte getOPCODE() {
        return OPCODE;
    }

    public int getNSCOUNT() {
        return NSCOUNT;
    }

    public boolean isAA() {
        return AA;
    }

    public boolean isTC() {
        return TC;
    }

    public boolean isRD() {
        return RD;
    }

    public byte getZ() {
        return Z;
    }

    public boolean isRA() {
        return RA;
    }

    public byte getRCODE() {
        return RCODE;
    }

    public int getQDCOUNT() {
        return QDCOUNT;
    }

    public int getANCOUNT() {
        return ANCOUNT;
    }

    public int getARCOUNT() {
        return ARCOUNT;
    }

    public UDPQuestion getQuestion() {
        return question;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DNS Message {\n");
        sb.append("  Header:\n");
        sb.append(String.format("    ID: 0x%04X (%d)\n", ID, ID));
        sb.append(String.format("    Flags: %s %s%s%s%s%s\n",
                QR ? "qr" : "",
                AA ? "aa " : "",
                TC ? "tc " : "",
                RD ? "rd " : "",
                RA ? "ra " : "",
                Z != 0 ? "z " : ""
        ));
        sb.append(String.format("    Type: %s\n", QR ? "Response" : "Query"));
        sb.append(String.format("    Opcode: %s (%d)\n", getOpcodeString(OPCODE), OPCODE));
        sb.append(String.format("    Response Code: %s (%d)\n", getRcodeString(RCODE), RCODE));
        sb.append(String.format("    Questions: %d\n", QDCOUNT));
        sb.append(String.format("    Answers: %d\n", ANCOUNT));
        sb.append(String.format("    Authority: %d\n", NSCOUNT));
        sb.append(String.format("    Additional: %d\n", ARCOUNT));

        if (question != null && QDCOUNT > 0) {
            sb.append("  Question Section:\n");
            sb.append("    ").append(question.toString()).append("\n");
        }

        sb.append("}");
        return sb.toString();
    }

    // Helper method for OPCODE strings
    private String getOpcodeString(byte opcode) {
        return switch (opcode) {
            case 0 -> "QUERY";
            case 1 -> "IQUERY";
            case 2 -> "STATUS";
            case 4 -> "NOTIFY";
            case 5 -> "UPDATE";
            default -> "OPCODE" + opcode;
        };
    }

    // Helper method for RCODE strings
    private String getRcodeString(byte rcode) {
        return switch (rcode) {
            case 0 -> "No Error";
            case 1 -> "Format Error";
            case 2 -> "Server Failure";
            case 3 -> "Name Error";
            case 4 -> "Not Implemented";
            case 5 -> "Refused";
            case 6 -> "YXDomain";
            case 7 -> "YXRRSet";
            case 8 -> "NXRRSet";
            case 9 -> "NotAuth";
            case 10 -> "NotZone";
            default -> "RCODE" + rcode;
        };
    }
}
