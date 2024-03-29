package net.sunxu.website.file.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FileInfoDTO extends FileInfoEditDTO {

    private static final long serialVersionUID = -1L;

    private String id;

    private String ext;

    private String sha256;

    private Long fileSize;

}
