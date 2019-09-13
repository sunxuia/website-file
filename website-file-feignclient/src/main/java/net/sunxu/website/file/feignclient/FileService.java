package net.sunxu.website.file.feignclient;

import java.util.List;
import net.sunxu.website.file.dto.FileInfoDTO;
import net.sunxu.website.file.dto.FileInfoEditDTO;
import net.sunxu.website.help.dto.ResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(serviceId = "file-service")
public interface FileService {

    @RequestMapping(path = "/file",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("file") MultipartFile multipartFile,
            @RequestParam("userId") Long userId);

    @RequestMapping(path = "/file/multiply",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<String> uploadFiles(@RequestPart("files") MultipartFile[] multipartFiles,
            @RequestParam("userId") Long userId);


    @RequestMapping(path = "/file/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    byte[] downloadFile(@PathVariable("id") String id);

    @RequestMapping(path = "/file/{id}/info")
    FileInfoDTO getFileInfo(@PathVariable("id") String id);

    @RequestMapping("/file/info")
    List<FileInfoDTO> getFileInfos(@RequestParam("ids") String... ids);

    @RequestMapping(path = "/file/{id}", method = RequestMethod.DELETE)
    ResultDTO deleteFile(@PathVariable("id") String id);

    @RequestMapping(params = "/file", method = RequestMethod.DELETE)
    List<ResultDTO> deleteFiles(@RequestParam("ids") String... ids);

    @RequestMapping(path = "/file/{id}", method = RequestMethod.PUT)
    FileInfoDTO updateFileInfo(@RequestParam("id") String id, @RequestBody FileInfoEditDTO dto);

    @RequestMapping(path = "/file/make-public")
    void makeFilesPublic(@RequestParam("ids") String... ids);

}
