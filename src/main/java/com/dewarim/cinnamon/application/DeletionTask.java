package com.dewarim.cinnamon.application;

import com.dewarim.cinnamon.api.content.ContentMetadataLight;
import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.dao.DeletionDao;
import com.dewarim.cinnamon.model.Deletion;
import com.dewarim.cinnamon.provider.ContentProviderService;
import com.dewarim.cinnamon.provider.DefaultContentProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class DeletionTask implements Runnable {
    private static final Logger log = LogManager.getLogger(DeletionTask.class);

    private static ReentrantLock  lock = new ReentrantLock();
    private final  List<Deletion> deletions;

    public DeletionTask(List<Deletion> deletions) {
        this.deletions = deletions;
    }

    @Override
    public void run() {
        try {
            boolean hasLock = lock.tryLock();
            if (!hasLock) {
                // another thread is already working on the Deletions.
                return;
            }
            try {
                DeletionDao deletionDao = new DeletionDao();
                ContentProvider contentProvider     = ContentProviderService.getInstance().getContentProvider(DefaultContentProvider.FILE_SYSTEM.name());
                int             successfulDeletions = 0;
                for (Deletion deletion : deletions) {
                    ContentMetadataLight metadataLight = new ContentMetadataLight();
                    metadataLight.setContentPath(deletion.getContentPath());
                    if (contentProvider.deleteContent(metadataLight)) {
                        successfulDeletions++;
                        deletion.setDeleted(true);
                    }
                    else{
                        deletion.setDeleteFailed(true);
                        deletionDao.update(List.of(deletion));
                    }
                }
                deletionDao.delete(deletions.stream().filter(Deletion::isDeleted).map(Deletion::getId).collect(Collectors.toList()));
                ThreadLocalSqlSession.getSqlSession().commit();
                CinnamonServer.cinnamonStats.getDeletions().addAndGet(successfulDeletions);
            } catch (Exception e) {
                log.warn("Failed to delete content: ", e);
            }
        } finally {
            lock.unlock();
        }
    }
}
