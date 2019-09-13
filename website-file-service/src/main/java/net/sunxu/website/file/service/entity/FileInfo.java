package net.sunxu.website.file.service.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@ToString
@Document
public class FileInfo implements Serializable {

    @Id
    private String id;

    private String name;

    private String ext;

    private String sha256;

    // 单位是字节
    private Long fileSize;

    private LocalDateTime createTime;

    private Long uploaderId;

    private Boolean publicAvailable;
}
