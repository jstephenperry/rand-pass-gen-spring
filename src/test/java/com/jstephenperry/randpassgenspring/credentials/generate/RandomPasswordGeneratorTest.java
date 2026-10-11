package com.jstephenperry.randpassgenspring.credentials.generate;

import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RandomPasswordGeneratorTest {
    @ParameterizedTest
    @CsvSource({"32,43", "33,44", "64,86", "128,171"})
    void returnsExactRandomBytesWithoutPadding(int bytes, int characters) {
        SecureRandom deterministic = new SecureRandom() {
            @Override public void nextBytes(byte[] output) {
                for (int i = 0; i < output.length; i++) output[i] = (byte) i;
            }
        };
        var response = new RandomPasswordGenerator(deterministic).generate(bytes, 2);
        assertEquals(bytes * 8, response.entropyBits());
        assertEquals("base64url", response.encoding());
        for (String value : response.credentials()) {
            assertEquals(characters, value.length());
            assertTrue(value.matches("[A-Za-z0-9_-]+"));
            byte[] decoded = Base64.getUrlDecoder().decode(value);
            assertEquals(bytes, decoded.length);
            for (int i = 0; i < bytes; i++) assertEquals((byte) i, decoded[i]);
        }
    }

    @ParameterizedTest
    @CsvSource({"31,1", "129,1", "0,1", "32,0", "32,101", "-1,-1"})
    void rejectsInvalidParametersBeforeUsingRandom(int length, int count) {
        var generator = new RandomPasswordGenerator(new SecureRandom() {
            @Override public void nextBytes(byte[] bytes) { fail("Must validate before generation"); }
        });
        assertThrows(IllegalArgumentException.class, () -> generator.generate(length, count));
    }

    @Test
    void clearsTemporaryBytesOnSuccessAndFailure() {
        class CapturingRandom extends SecureRandom {
            byte[] buffer;
            boolean fail;
            @Override public void nextBytes(byte[] output) {
                buffer = output;
                java.util.Arrays.fill(output, (byte) 0x7f);
                if (fail) throw new IllegalStateException("RNG unavailable");
            }
        }
        var random = new CapturingRandom();
        var generator = new RandomPasswordGenerator(random);
        generator.generate(32, 1);
        assertArrayEquals(new byte[32], random.buffer);
        random.fail = true;
        assertThrows(IllegalStateException.class, () -> generator.generate(32, 1));
        assertArrayEquals(new byte[32], random.buffer);
    }

    @Test
    void safeToUseConcurrentlyAndDoesNotAccidentallyReuseCredentials() throws Exception {
        var generator = new RandomPasswordGenerator();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var jobs = IntStream.range(0, 100).<Callable<GeneratedCredentials>>mapToObj(
                    i -> () -> generator.generate(32, 10)).toList();
            var seen = new HashSet<String>();
            for (var future : executor.invokeAll(jobs)) {
                for (var value : future.get().credentials()) assertTrue(seen.add(value));
            }
            assertEquals(1000, seen.size());
        }
    }

    @Test
    void responseCannotBeMutatedAndToStringDoesNotRevealCredentials() {
        var response = new RandomPasswordGenerator().generate(32, 1);
        assertFalse(response.toString().contains(response.credentials().getFirst()));
        assertThrows(UnsupportedOperationException.class, () -> response.credentials().clear());
    }
}
