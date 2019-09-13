package net.sunxu.website.file.service.service;


import java.io.InputStream;
import net.sunxu.website.file.dto.FileInfoDTO;
import net.sunxu.website.file.dto.FileInfoEditDTO;

public interface FileService {

    String upload(FileInfoEditDTO editDTO, byte[] content);

    void updateFileInfo(String id, FileInfoEditDTO editDTO);

    FileInfoDTO getFileInfo(String id);

    InputStream downFile(String id);

    void deleteFile(String id);

    void makeFilesPublic(String[] ids);
}
