# Random Password Generator - Spring Shell

## Overview

A **production-ready** password generator built with Spring Shell that uses cryptographically secure random number generation. This application demonstrates modern Java 25 (LTS) features, Spring Framework 3.5+ best practices, and security-conscious design.

## Features

### Security
- **Cryptographically Secure**: Uses `java.security.SecureRandom` instead of predictable PRNGs
- **Proper Entropy**: No character position restrictions that reduce randomness
- **Password Strength Feedback**: Calculates and displays entropy bits for generated passwords
- **Input Validation**: Comprehensive validation with clear, user-friendly error messages

### Technical Excellence
- **Spring Dependency Injection**: Proper instance-based architecture with singleton SecureRandom bean
- **Production-Ready Code**: No memory wastage from repeated RNG instantiation
- **Comprehensive Testing**: Full test coverage including validation, security, and edge cases
- **Modern Java**: Leverages Java 25 LTS features (switch expressions, pattern matching, enhanced APIs)
- **Latest Spring Stack**: Built on Spring Boot 3.5.5 and Spring Shell 3.4.1

## Commands

The application provides two main commands with convenient aliases:

### Generate Single Password
```bash
generate-password --length <8-1024> --complexity <LOW|MEDIUM|HIGH>
# or use the shorthand:
gp --length 16 --complexity HIGH
```

### Generate Password List
```bash
generate-password-list --list-length <1-10000> --length <8-1024> --complexity <LOW|MEDIUM|HIGH>
# or use the shorthand:
gpl --list-length 10 --length 16 --complexity MEDIUM
```

## Password Complexity Modes

| Complexity Mode | Character Set | Character Count | Use Case |
|-----------------|---------------|-----------------|----------|
| **LOW** | Uppercase + Lowercase letters | 52 characters | Basic applications with limited character support |
| **MEDIUM** | Letters + Digits | 62 characters | Standard web applications |
| **HIGH** | Letters + Digits + Special chars | 84 characters | High-security applications |

Special characters include: `! @ # $ % ^ & * ( ) ~ - _ = + < > ? { } [ ]`

## Password Strength Guide

The application calculates password entropy and provides strength ratings:

- **Very Weak** (< 28 bits): Not recommended for any use
- **Weak** (28-36 bits): Vulnerable to attacks
- **Reasonable** (36-60 bits): Acceptable for low-security applications
- **Strong** (60-128 bits): Suitable for most applications
- **Very Strong** (≥ 128 bits): Suitable for high-security applications

### Example Output
```
Password: Kx8#mP@9qL2*wN4y
Strength: Strong (91.52 bits of entropy)
```

## Technology Stack (Latest 2025 Versions)

This project uses the latest Long-Term Support and stable releases:

- **Java 25 LTS**: Released September 2025, provides 8+ years of Oracle support
- **Spring Boot 3.5.5**: Latest stable release (August 2025)
- **Spring Shell 3.4.1**: Latest stable release (August 2025)
- **Logback 1.5.21**: Patched version addressing CVE-2025-11226 (ACE vulnerability)
- **Lombok 1.18.40**: Latest with full Java 25 compatibility
- **Apache Commons Lang3 3.19.0**: Latest stable release (November 2025)
- **Maven Surefire 3.5.4**: Latest testing plugin

### Security Vulnerability Mitigation

This project explicitly overrides transitive dependencies to address known vulnerabilities:

- **CVE-2025-11226** (CVSS 6.9 - Medium): Arbitrary Code Execution in logback-core ≤ 1.5.18
  - **Impact**: Allows attackers to execute arbitrary code through compromised configuration files
  - **Mitigation**: Upgraded to logback-classic 1.5.21 (includes patched logback-core)
  - **Fixed in**: Version 1.5.19+ (disallows "new" operator in conditional configuration)

## Security Improvements (v1.0.0)

This version includes critical security and quality improvements:

1. ✅ **SecureRandom**: Replaced Apache Commons RNG with cryptographically secure `java.security.SecureRandom`
2. ✅ **Input Validation**: Prevents crashes from invalid inputs (length, complexity, list size)
3. ✅ **Resource Efficiency**: Single RNG instance managed by Spring (no repeated instantiation)
4. ✅ **Removed Entropy Reduction**: First character can now be any valid character type
5. ✅ **Proper Architecture**: Instance-based Spring component with dependency injection
6. ✅ **Password Strength Calculation**: Real-time entropy feedback
7. ✅ **Comprehensive Tests**: 15+ test cases covering security, validation, and edge cases
8. ✅ **Production Documentation**: Clear usage guidelines and security context

## Requirements

- Java 25 (LTS) or higher
- Maven 3.6+
- Spring Boot 3.5.5
- Spring Shell 3.4.1

## Building and Running

```bash
# Build the application
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/rand-pass-gen-spring-1.0.0-SNAPSHOT.jar
```

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

## Example Usage

```bash
# Generate a strong 16-character password
shell:> gp --length 16 --complexity HIGH
Password: 8#mKx@pL2*qN4wYz
Strength: Strong (91.52 bits of entropy)

# Generate 5 medium-complexity passwords
shell:> gpl --list-length 5 --length 12 --complexity MEDIUM
Generated 5 passwords
Strength: Strong (71.40 bits of entropy)

1. a7M2n9K4p1Xb
2. R3q8T5j6W2mn
3. P9k4L7x2M5ab
4. N6w3Q8r1Y4kp
5. J2m7K9x4L3nq
```

## Future Enhancements

- [ ] File output support for password lists
- [ ] Custom character set configuration
- [ ] Exclude ambiguous characters option (0/O, 1/l/I)
- [ ] Minimum character type requirements
- [ ] Password history and duplicate prevention
- [ ] Configurable entropy thresholds

## License

This project is provided as-is for educational and production use.