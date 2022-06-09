package net.sghill.jenkins.rewrite;

import org.junit.jupiter.api.Test;

public class ApiPluginIndexTest {

    @Test
    void findApiPlugins() {
        ApiPluginIndex index = new ApiPluginIndex();
        index.updateIndex();
        index.searchIndex();
    }
}
