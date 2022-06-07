package net.sghill.jenkins.rewrite;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BomLookupTest {

    @Test
    void lookup() {
        BomLookup bomLookup = new BomLookup();
        assertThat(bomLookup.inBom("io.jenkins.plugins", "theme-manager")).isTrue();
    }
}
