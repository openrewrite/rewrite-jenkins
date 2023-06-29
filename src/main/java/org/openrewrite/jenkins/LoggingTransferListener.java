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
package org.openrewrite.jenkins;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.observers.AbstractTransferListener;

@Slf4j
class LoggingTransferListener extends AbstractTransferListener {
    private int percent = 0;
    private int progress = 0;

    public void transferStarted(TransferEvent transferEvent) {
        log.info(transferEvent.getResource().getName() + " download beginning.");
    }

    @Override
    public void transferProgress(TransferEvent transferEvent, byte[] buffer, int length) {
        progress += length;
        long contentLength = transferEvent.getResource().getContentLength();
        long percentComplete = (long) (progress * 100.0 / contentLength);

        if (((int) percentComplete / 10) == percent) {
            log.info(transferEvent.getResource().getName() + " - " + percentComplete + "% (" + progress
                    + "/" + contentLength + ")");
            percent++;
        }
    }

    public void transferCompleted(TransferEvent transferEvent) {
        log.info(transferEvent.getResource().getName() + " downloaded.");
        log.info("Indexing and sorting metadata may take some time...");
    }
}
