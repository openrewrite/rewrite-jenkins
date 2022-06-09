package net.sghill.jenkins.rewrite;

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
