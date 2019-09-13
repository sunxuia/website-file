package net.sunxu.website.file.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FileInfoEditDTO {

    private static final long serialVersionUID = -1L;

    private String name;

    private Long uploaderId;

    private Boolean publicAvailable;
}
