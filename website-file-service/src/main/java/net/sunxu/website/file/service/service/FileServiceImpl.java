package net.sunxu.website.file.service.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import net.sunxu.website.config.feignclient.exception.ServiceException;
import net.sunxu.website.file.dto.FileInfoDTO;
import net.sunxu.website.file.dto.FileInfoEditDTO;
import net.sunxu.website.file.service.entity.FileInfo;
import net.sunxu.website.file.service.repo.FileInfoRepo;
import net.sunxu.website.help.util.ExceptionHelpUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${website.file.dir}")
    private String fileDir;

    @Autowired
    private FileInfoRepo repo;

    @Override
    public String upload(FileInfoEditDTO editDTO, byte[] content) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(editDTO.getName());
        fileInfo.setUploaderId(editDTO.getUploaderId());
        fileInfo.setPublicAvailable(editDTO.getPublicAvailable());

        String sha256 = sha256ToHashCode(content);
        fileInfo.setSha256(sha256);
        String ext = editDTO.getName().substring(editDTO.getName().lastIndexOf(".") + 1);
        fileInfo.setExt(ext);
        fileInfo.setFileSize((long) content.length);
        fileInfo.setCreateTime(LocalDateTime.now());

        boolean fileExist = repo.existsBySha256(sha256);
        if (!fileExist) {
            doSaveFile(sha256, content);
        }
        repo.save(fileInfo);
        return fileInfo.getId();
    }

    private String sha256ToHashCode(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(content, 0, content.length);
            byte[] md5Bytes = md.digest();
            var hexValue = new StringBuilder(64);
            for (byte md5Byte : md5Bytes) {
                int val = ((int) md5Byte) & 0xff;
                if (val < 0x10) {
                    hexValue.append("0");
                }
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw ServiceException.wrapException(e);
        }
    }

    private void doSaveFile(String sha256, byte[] content) {
        String dirLevel1 = sha256.substring(0, 2);
        String dirLevel2 = sha256.substring(2, 4);
        String dirPath = fileDir + (fileDir.endsWith("/") ? "" : "/")
                + dirLevel1 + "/" + dirLevel2;
        String filePath = dirPath + "/" + sha256;
        File file = new File(filePath);
        if (!file.exists()) {
            FileOutputStream output = null;
            try {
                File dir = new File(dirPath);
                dir.mkdirs();
                file.createNewFile();
                output = new FileOutputStream(file);
                output.write(content);
            } catch (Exception err) {
                throw ServiceException.wrapException(err);
            } finally {
                if (output != null) {
                    final FileOutputStream o = output;
                    ExceptionHelpUtils.wrapException(o::close);
                }
            }
        }
    }

    @Override
    public void updateFileInfo(String id, FileInfoEditDTO editDTO) {
        var fileInfo = repo.findById(id)
                .orElseThrow(() -> ServiceException.newException("文件不存在"));
        BeanUtils.copyProperties(editDTO, fileInfo);
        repo.save(fileInfo);
    }

    @Override
    public FileInfoDTO getFileInfo(String id) {
        return repo.findById(id).map(f -> {
            FileInfoDTO res = new FileInfoDTO();
            BeanUtils.copyProperties(f, res);
            return res;
        }).orElse(null);
    }

    @Override
    public InputStream downFile(String id) {
        return repo.findById(id)
                .map(FileInfo::getSha256)
                .map(this::doGetFileInputStream)
                .orElse(null);
    }

    private InputStream doGetFileInputStream(String sha256) {
        String filePath = doGetFilePath(sha256);
        var file = new File(filePath);
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (Exception err) {
                log.warn("error while get file {}: {}", sha256, err.toString());
            }
        }
        return null;
    }

    private String doGetFilePath(String sha256) {
        String dirLevel1 = sha256.substring(0, 2);
        String dirLevel2 = sha256.substring(2, 4);
        return fileDir + (fileDir.endsWith("/") ? "" : "/") + dirLevel1 + "/" + dirLevel2 + "/" + sha256;
    }

    @Override
    public void deleteFile(String id) {
        FileInfo fileInfo = repo.findById(id).orElse(null);
        if (fileInfo != null) {
            repo.deleteById(id);
            String sha256 = fileInfo.getSha256();
            if (!repo.existsBySha256(sha256)) {
                doDeleteFile(sha256);
            }
        }
    }

    private void doDeleteFile(String sha256) {
        String filePath = doGetFilePath(sha256);
        var file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void makeFilesPublic(String[] ids) {
        for (String id : ids) {
            var fileInfo = repo.findById(id).orElse(null);
            if (fileInfo != null) {
                fileInfo.setPublicAvailable(true);
                repo.save(fileInfo);
            }
        }
    }
}
