package com.campus.market.service.impl;

import com.campus.market.common.BusinessException;
import com.campus.market.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-prefix}")
    private String accessPrefix;

    @Override
    public String save(MultipartFile file) {
        if (file.isEmpty()) throw new BusinessException("文件为空");

        String original = file.getOriginalFilename();
        String ext = original == null ? "" : original.substring(original.lastIndexOf('.'));
        String allow = ".jpg.jpeg.png.webp.gif";
        if (!allow.contains(ext.toLowerCase())) {
            throw new BusinessException("只允许 jpg/png/webp/gif");
        }

        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Path dir = Paths.get(uploadPath);
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(name).toAbsolutePath().toFile());
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException("文件保存失败");
        }
        return accessPrefix + name;
    }
}
