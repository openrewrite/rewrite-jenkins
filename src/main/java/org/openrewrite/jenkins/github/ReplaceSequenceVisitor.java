/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.jenkins.github;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.marker.Markers;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.tree.Yaml;

import java.util.List;

import static org.openrewrite.Tree.randomId;

/**
 * Adapted from {@link org.openrewrite.yaml.AppendToSequenceVisitor}
 */
class ReplaceSequenceVisitor extends YamlIsoVisitor<ExecutionContext> {
    private final JsonPathMatcher matcher;
    private final List<String> values;

    ReplaceSequenceVisitor(JsonPathMatcher matcher, List<String> values) {
        this.matcher = matcher;
        this.values = values;
    }

    @Override
    public Yaml.Sequence visitSequence(Yaml.Sequence existingSeq, ExecutionContext executionContext) {
        Cursor parent = getCursor().getParent();
        if (matcher.matches(parent) && !alreadyVisited(existingSeq, executionContext)) {
            setVisited(existingSeq, executionContext);
            Yaml.Sequence newSeq = replaceSequence(existingSeq, this.values);
            setVisited(newSeq, executionContext);
            return newSeq;
        }
        return super.visitSequence(existingSeq, executionContext);
    }

    private Yaml.Sequence replaceSequence(Yaml.Sequence existingSequence, List<String> values) {
        Yaml.Sequence newSequence = existingSequence.copyPaste();
        List<Yaml.Sequence.Entry> entries = newSequence.getEntries();
        boolean hasDash = false;
        Yaml.Scalar.Style style = Yaml.Scalar.Style.PLAIN;
        String entryPrefix = "";
        String entryTrailingCommaPrefix = null;
        String itemPrefix = "";
        if (!entries.isEmpty()) {
            final int lastEntryIndex = entries.size() - 1;
            Yaml.Sequence.Entry existingEntry = entries.get(lastEntryIndex);
            hasDash = existingEntry.isDash();
            entryPrefix = existingEntry.getPrefix();
            entryTrailingCommaPrefix = existingEntry.getTrailingCommaPrefix();
            Yaml.Sequence.Block block = existingEntry.getBlock();
            if (block instanceof Yaml.Sequence.Mapping) {
                Yaml.Sequence.Mapping mapping = (Yaml.Sequence.Mapping) block;
                List<Yaml.Mapping.Entry> mappingEntries = mapping.getEntries();
                if (!mappingEntries.isEmpty()) {
                    Yaml.Mapping.Entry entry = mappingEntries.get(0);
                    itemPrefix = entry.getPrefix();
                }
            }
            else if (block instanceof Yaml.Sequence.Scalar) {
                itemPrefix = block.getPrefix();
                style = ((Yaml.Sequence.Scalar) block).getStyle();
            }
            if (!existingEntry.isDash()) {
                entries.set(lastEntryIndex, existingEntry.withTrailingCommaPrefix(""));
            }
        }
        entries.clear();
        for (String value : values) {
            Yaml.Scalar newItem = new Yaml.Scalar(randomId(), itemPrefix, Markers.EMPTY, style, null, value);
            Yaml.Sequence.Entry newEntry = new Yaml.Sequence.Entry(randomId(), entryPrefix, Markers.EMPTY, newItem, hasDash, entryTrailingCommaPrefix);
            entries.add(newEntry);
        }
        return newSequence;
    }

    private static void setVisited(Yaml.Sequence seq, ExecutionContext context) {
        context.putMessage(makeAlreadyVisitedKey(seq), Boolean.TRUE);
    }

    private static boolean alreadyVisited(Yaml.Sequence seq, ExecutionContext context) {
        return context.getMessage(makeAlreadyVisitedKey(seq), Boolean.FALSE);
    }

    private static String makeAlreadyVisitedKey(Yaml.Sequence seq) {
        return org.openrewrite.yaml.AppendToSequenceVisitor.class.getName() + ".alreadyVisited." + seq.getId().toString();
    }
}
