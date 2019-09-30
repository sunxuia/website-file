package net.sunxu.website.file.service.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.sunxu.website.config.security.authentication.SecurityHelpUtils;
import net.sunxu.website.config.security.rbac.annotation.AccessResource;
import net.sunxu.website.file.dto.FileInfoDTO;
import net.sunxu.website.file.dto.FileInfoEditDTO;
import net.sunxu.website.file.service.service.FileService;
import net.sunxu.website.help.dto.ResultDTO;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/file")
@AccessResource("file")
public class FileController {

    @Autowired
    private FileService fileService;

    @AccessResource("upload")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        return uploadFile(file, SecurityHelpUtils.getCurrentUserId());
    }

    @AccessResource("service-upload")
    @PostMapping(path = "",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String uploadFile(@RequestParam("file") MultipartFile multipartFile,
            @RequestParam("userId") Long userId) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        InputStream inputStream = multipartFile.getInputStream();
        int fileLength = inputStream.available();
        byte[] content = new byte[fileLength];
        inputStream.read(content);
        inputStream.close();

        FileInfoEditDTO dto = new FileInfoEditDTO();
        dto.setName(fileName);
        dto.setUploaderId(userId);
        try {
            return fileService.upload(dto, content);
        } catch (Exception e) {
            throw new RuntimeException("upload file Exception!", e);
        }
    }

    @AccessResource("upload")
    @PostMapping(path = "/multiply",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces =
            MediaType.TEXT_PLAIN_VALUE)
    public List<String> uploadFiles(@RequestParam("files") MultipartFile[] files, Long userId) throws IOException {
        List<String> res = new ArrayList<>(files.length);
        for (MultipartFile file : files) {
            String id = uploadFile(file, userId);
            res.add(id);
        }
        return res;
    }

    @AccessResource("download")
    @GetMapping(value = "/{id:[0-9a-f]+}/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadFile(@PathVariable("id") String id, HttpServletResponse response) throws IOException {
        var inputStream = fileService.downFile(id);
        if (inputStream == null) {
            response.setStatus(404);
        } else {
            try {
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                var outputStream = response.getOutputStream();
                IOUtils.copy(inputStream, outputStream);
                outputStream.flush();
            } finally {
                inputStream.close();
            }
        }
    }

    @AccessResource("download")
    @GetMapping(value = "/{id:[0-9a-f]+}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadFileWoFileName(@PathVariable("id") String id, HttpServletResponse response) throws IOException {
        downloadFile(id, response);
    }

    @AccessResource("info")
    @GetMapping("/{id:[0-9a-f]+}/info")
    public ResponseEntity<FileInfoDTO> getFileInfo(@PathVariable("id") String id) {
        var file = fileService.getFileInfo(id);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(file);
    }

    @AccessResource("info")
    @GetMapping("/info")
    public List<FileInfoDTO> getFileInfo(@RequestParam("ids") String[] ids) {
        List<FileInfoDTO> res = new ArrayList<>(ids.length);
        for (String id : ids) {
            var fileInfo = fileService.getFileInfo(id);
            if (fileInfo != null) {
                res.add(fileInfo);
            }
        }
        return res;
    }

    @AccessResource("delete")
    @DeleteMapping("/{id}")
    public ResultDTO deleteFile(@PathVariable("id") String id) {
        try {
            fileService.deleteFile(id);
            return ResultDTO.success();
        } catch (Error err) {
            log.warn("error while delete file: {}", err.toString());
            return ResultDTO.fail(err.getMessage());
        }
    }

    @AccessResource("delete")
    @DeleteMapping("")
    public List<ResultDTO> deleteFile(@RequestParam("ids") String[] ids) {
        List<ResultDTO> res = new ArrayList<>(ids.length);
        for (String id : ids) {
            res.add(deleteFile(id));
        }
        return res;
    }

    @AccessResource("update")
    @PutMapping("/{id}/info")
    public FileInfoDTO updateFileInfo(@RequestParam("id") String id, @RequestBody FileInfoEditDTO dto) {
        fileService.updateFileInfo(id, dto);
        return fileService.getFileInfo(id);
    }

    @AccessResource("update")
    @PutMapping("/make-public")
    public void makeFilePublic(@RequestParam("ids") String[] ids) {
        fileService.makeFilesPublic(ids);
    }
}
