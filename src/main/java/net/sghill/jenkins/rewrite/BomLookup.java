package net.sghill.jenkins.rewrite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class BomLookup {
    private boolean initialized = false;
    private final Map<String, String> groupByArtifact = new HashMap<>();
    
    public boolean inBom(String groupId, String artifactId) {
        if (!initialized) {
            initialize();
            initialized = true;
        }
        String foundGroup = groupByArtifact.get(artifactId);
        return groupId.equals(foundGroup);
    }
    
    private void initialize() {
        try (InputStream is = BomLookup.class.getResourceAsStream("/bom-2.303.x.csv");
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader r = new BufferedReader(isr);
             Stream<String> lines = r.lines()) {
            lines.filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(l -> !l.isEmpty())
                    .map(l -> l.split(",", 2))
                    .forEach(parts -> {
                        String group = parts[0];
                        String artifact = parts[1];
                        groupByArtifact.put(artifact, group);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
