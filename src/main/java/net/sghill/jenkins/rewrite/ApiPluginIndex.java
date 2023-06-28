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
package net.sghill.jenkins.rewrite;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.FlatSearchRequest;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexUtils;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.apache.maven.index.updater.*;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagonAuthenticator;
import org.eclipse.sisu.space.DefaultClassFinder;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * Proof-of-concept class used only in tests at the moment.
 * Dynamically builds an index of plugins with api in the name.
 * API Plugins allow Jenkins to share libraries.
 */
@Slf4j
public class ApiPluginIndex {
    private final Injector injector;
    private final IndexingContext context;
    private final Indexer indexer;

    /**
     * Creates an index in ~/.jenkins/index
     */
    public ApiPluginIndex() {
        this.injector = Guice.createInjector(new WireModule(new SpaceModule(new URLClassSpace(
                ApiPluginIndex.class.getClassLoader()), new DefaultClassFinder("org.apache.maven.*"))));
        this.indexer = injector.getInstance(Indexer.class);

        List<IndexCreator> indexCreators = Collections.singletonList(injector.getInstance(MinimalArtifactInfoIndexCreator.class));

        Path jenkinsRepoOnDisk = Paths.get(System.getProperty("user.home")).resolve(".jenkins");
        if (!jenkinsRepoOnDisk.toFile().exists()) {
            if (!jenkinsRepoOnDisk.toFile().mkdirs() ||
                    !jenkinsRepoOnDisk.resolve("index").toFile().mkdirs()) {
                throw new IllegalStateException("Unable to create jenkins repo on disk");
            }
        }

        try {
            this.context = indexer.createIndexingContext(
                    "jenkins-context",
                    "jenkins",
                    jenkinsRepoOnDisk.toFile(),
                    jenkinsRepoOnDisk.resolve(".index").toFile(),
                    "https://repo.jenkins-ci.org/public",
                    null,
                    true,
                    false,
                    indexCreators);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Updates the index, incrementally if possible
     */
    public void updateIndex() {
        try {
            IndexUpdater indexUpdater = injector.getInstance(DefaultIndexUpdater.class);
            LightweightHttpWagon wagon = new LightweightHttpWagon();
            wagon.setAuthenticator(new LightweightHttpWagonAuthenticator());

            ResourceFetcher resourceFetcher = new WagonHelper.WagonFetcher(wagon,
                    new LoggingTransferListener(), null, null);
            IndexUpdateRequest updateRequest = new IndexUpdateRequest(context, resourceFetcher);
            updateRequest.setIncrementalOnly(false);
            updateRequest.setForceFullUpdate(false);
            IndexUpdateResult updateResult = indexUpdater.fetchAndUpdateIndex(updateRequest);

            if (updateResult.isFullUpdate()) {
                log.info("Fully updated index for repository [{}] - [{}]", context.getId(), context.getRepositoryUrl());
            } else {
                log.info("Incrementally updated index for repository [{}}] - [{}}]", context.getId(), context.getRepositoryUrl());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Demonstrates searching through the index for a term.
     * Prints to stdout.
     */
    public void searchIndex() {
        try {
            IndexSearcher searcher = context.acquireIndexSearcher();

            IndexReader reader = searcher.getIndexReader();
            IndexWriter writer = context.getIndexWriter();

            for(int i = 0; i < reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                ArtifactInfo ai = IndexUtils.constructArtifactInfo(doc, context);
                if(ai != null) {
                    for (IndexCreator indexCreator : context.getIndexCreators()) {
                        indexCreator.updateDocument(ai, doc);
                    }
                }
            }

            writer.commit();
            writer.flush();

            reader = DirectoryReader.open(context.getIndexDirectory());
            for(int i = 0; i < reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                // FIXME how do we populate GAV on this index correctly?
                System.out.println(doc.getFields());
            }

            Query q = new TermQuery(new Term(ArtifactInfo.ARTIFACT_ID, "api"));
            try (FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(q))) {
                for (ArtifactInfo result : response.getResults()) {
                    System.out.println(result);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
