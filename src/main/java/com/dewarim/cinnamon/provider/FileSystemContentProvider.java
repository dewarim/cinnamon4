package com.dewarim.cinnamon.provider;

import com.dewarim.cinnamon.api.content.ContentMetadata;
import com.dewarim.cinnamon.api.content.ContentMetadataLight;
import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.application.CinnamonServer;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileSystemContentProvider implements ContentProvider {

    private static final Logger log = LogManager.getLogger(FileSystemContentProvider.class);


    static final String SEP = File.separator;

    private final String dataRootPath;

    public FileSystemContentProvider() {
        dataRootPath = CinnamonServer.config.getServerConfig().getDataRoot();
    }

    @Override
    public InputStream getContentStream(ContentMetadata metadata) throws IOException {
        String path = dataRootPath + SEP + metadata.getContentPath();
        return new FileInputStream(path);
    }

    @Override
    public ContentMetadata writeContentStream(ContentMetadata metadata, InputStream inputStream) throws IOException {
        String targetName    = UUID.randomUUID().toString();
        String subfolderName = getSubFolderName(targetName);
        String subfolderPath = dataRootPath + SEP + subfolderName;
        File   subfolder     = new File(subfolderPath);

        boolean result = subfolder.mkdirs();
        log.debug("created subfolder {}: {}", subfolderPath, result);
        String contentPath  = subfolderPath + SEP + targetName;
        Path   contentFile  = Paths.get(subfolderPath, targetName);
        long   bytesWritten = Files.copy(inputStream, contentFile);

        // we could just update the existing metadata, but that's bad style.
        ContentMetadata lightMeta = new ContentMetadataLight();
        lightMeta.setContentSize(bytesWritten);
        lightMeta.setContentPath(subfolderName + SEP + targetName);

        // calculate hash:
        String sha256Hex = DigestUtils.sha256Hex(new FileInputStream(contentFile.toFile()));
        lightMeta.setContentHash(sha256Hex);

        log.info("Stored new content @ {}", contentFile.toAbsolutePath());

        return lightMeta;
    }

    private static String getSubFolderName(String f) {
        return f.substring(0, 2) + SEP + f.substring(2, 4) + SEP + f.substring(4, 6);
    }

    @Override
    public String getName() {
        return DefaultContentProvider.FILE_SYSTEM.name();
    }
}
