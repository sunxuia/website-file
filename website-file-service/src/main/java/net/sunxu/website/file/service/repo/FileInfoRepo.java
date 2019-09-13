package net.sunxu.website.file.service.repo;

import net.sunxu.website.file.service.entity.FileInfo;
import org.springframework.data.repository.CrudRepository;

public interface FileInfoRepo extends CrudRepository<FileInfo, String> {

    boolean existsBySha256(String md5);

}
