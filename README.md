
# DNS Server Implementation 🌐

A lightweight DNS server implementation in Java, built as part of the CodeCrafters DNS Server Challenge. This educational project demonstrates the inner workings of the Domain Name System (DNS) protocol by building a functional DNS server from scratch.

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Technical Details](#technical-details)
- [Future Enhancements](#future-enhancements)
- [Learning Resources](#learning-resources)

## 🎯 Overview

This project implements a basic DNS server that can:
- Parse DNS query packets
- Understand DNS message structure
- Respond to A record queries with hardcoded IP addresses
- Handle the UDP protocol on port 2053

## ✨ Features

- **DNS Message Parsing**: Complete parsing of DNS headers and questions
- **A Record Support**: Responds to IPv4 address queries
- **UDP Protocol**: Implements DNS over UDP (port 2053)
- **Binary Protocol Handling**: Proper bit manipulation for DNS wire format
- **Extensible Design**: Easy to add support for more record types

## 🏗️ Architecture

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Client    │ ──DNS──>│  DNS Server  │ ──UDP──>│   Main      │
│   (dig)     │  Query  │  Port 2053   │ Socket  │   Loop      │
└─────────────┘         └──────────────┘         └─────────────┘
                                │
                    ┌───────────┴───────────┐
                    │                       │
              ┌─────▼─────┐          ┌─────▼─────┐
              │UDPMessage │          │UDPQuestion│
              │  Parser   │          │  Parser   │
              └───────────┘          └───────────┘
```

## 🚀 Getting Started

### Prerequisites
- Java 11 or higher
- Basic understanding of networking concepts
- (Optional) `dig` or `nslookup` for testing

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/dns-server.git
cd dns-server
```

2. Compile the project:
```bash
javac *.java
```

3. Run the server:
```bash
java Main
```

## 📖 Usage

### Starting the Server
```bash
java Main
```
The server will start listening on port 2053.

### Testing with dig
```bash
# Query for an A record
dig @127.0.0.1 -p 2053 example.com

# Query with additional details
dig @127.0.0.1 -p 2053 google.com +norecurse +comments
```

### Testing with nslookup
```bash
nslookup example.com 127.0.0.1 -port=2053
```

## 🧪 Testing

The server includes debug output for each query received:
- Packet details (ID, flags, counts)
- Question section parsing
- Response construction

Example output:
```
Received data
Data:
DNS Message {
  Header:
    ID: 0x1234 (4660)
    Flags: rd 
    Type: Query
    Opcode: QUERY (0)
    Response Code: No Error (0)
    Questions: 1
    Answers: 0
  Question Section:
    Question: example.com IN A
}
```

## 📁 Project Structure

```
dns-server/
├── Main.java           # Server entry point and main loop
├── UDPMessage.java     # DNS header parsing and creation
├── UDPQuestion.java    # DNS question section handling
├── UDPAnswer.java      # DNS answer section creation
├── RData.java          # Resource data formatting (A, AAAA, MX, etc.)
└── README.md           # This file
```

## 🔧 Technical Details

### DNS Message Format
```
+---------------------+
|        Header       | 12 bytes
+---------------------+
|       Question      | Variable
+---------------------+
|        Answer       | Variable
+---------------------+
|      Authority      | Variable
+---------------------+
|      Additional     | Variable
+---------------------+
```

### Supported Features
- ✅ DNS Header parsing (12 bytes)
- ✅ Question section parsing
- ✅ A record responses
- ✅ Proper flag handling
- ✅ Transaction ID matching
- ❌ Recursive resolution
- ❌ Caching
- ❌ Multiple questions/answers

## 🚧 Future Enhancements
 that i will get to 
### 1. **Additional Record Types**
```java
// Add to RData.java
public static byte[] createMX(int preference, String mailServer)
public static byte[] createTXT(String text)
public static byte[] createCNAME(String canonical)
```

### 2. **Simple In-Memory Cache**
```java
public class DNSCache {
    private Map<String, CachedRecord> cache = new ConcurrentHashMap<>();
    
    public byte[] lookup(String domain, int type) {
        // Check cache with TTL expiration
    }
}
```

### 3. **Configuration File Support**
```properties
# dns.properties
server.port=2053
server.timeout=5000
response.ttl=300
zones.file=zones.txt
```

### 4. **Zone File Parser**
```
; zones.txt
example.com.    IN  A     192.168.1.100
example.com.    IN  MX    10 mail.example.com.
```

### 5. **Statistics and Monitoring**
```java
public class DNSStats {
    private AtomicLong queryCount = new AtomicLong();
    private Map<Integer, Long> queryTypes = new ConcurrentHashMap<>();
    
    public void recordQuery(int type) {
        queryCount.incrementAndGet();
        queryTypes.merge(type, 1L, Long::sum);
    }
}
```

### 6. **Basic Forwarding/Recursion**
```java
public class DNSForwarder {
    private static final String UPSTREAM_DNS = "8.8.8.8";
    
    public byte[] forward(byte[] query) {
        // Forward to real DNS server
    }
}
```

### 7. **TCP Support**
```java
// Add TCP handler for large responses
ServerSocket tcpSocket = new ServerSocket(2053);
```

### 8. **DNSSEC Validation**
- Add RRSIG record support
- Implement basic signature validation

### 9. **Rate Limiting**
```java
public class RateLimiter {
    private Map<InetAddress, AtomicInteger> requests = new ConcurrentHashMap<>();
    
    public boolean allowRequest(InetAddress client) {
        // Implement token bucket algorithm
    }
}
```

### 10. **Web Dashboard**
- Simple HTTP server on port 8080
- Show query statistics
- Display cache contents

## 📚 Learning Resources

- [RFC 1035 - Domain Names](https://www.ietf.org/rfc/rfc1035.txt)
- [DNS Message Format](https://www.firewall.cx/networking-topics/protocols/domain-name-system-dns/160-protocols-dns-response.html)
- [CodeCrafters DNS Challenge](https://codecrafters.io/challenges/dns)

## 🤝 Contributing

This is an educational project. Feel free to:
- Add new record types
- Improve error handling
- Add more comprehensive logging
- Implement any of the suggested enhancements

## 📝 License

This project is open source and available under the MIT License.

---

**Note**: This is a simplified DNS server for educational purposes. For production use, consider established DNS servers like BIND, Unbound, or CoreDNS.