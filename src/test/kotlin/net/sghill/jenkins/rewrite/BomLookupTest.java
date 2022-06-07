package net.sghill.jenkins.rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.maven.tree.GroupArtifact;

import static org.assertj.core.api.Assertions.assertThat;

public class BomLookupTest {
    static final BomLookup bomLookup = new BomLookup();

    @Test
    void lookup() {
        assertThat(bomLookup.inBom("io.jenkins.plugins", "theme-manager")).isTrue();
    }

    @Test
    void allPlugins() {
        bomLookup.getGroupArtifacts().stream()
                .filter(groupArtifact -> groupArtifact.getGroupId().equals("io.jenkins.plugins"))
                .map(GroupArtifact::getArtifactId)
                .sorted()
                .forEach(System.out::println);
    }
}
