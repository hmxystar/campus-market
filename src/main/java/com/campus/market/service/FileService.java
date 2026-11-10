package com.campus.market.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /** 保存上传文件，返回可访问的相对地址（如 /files/xxx.jpg） */
    String save(MultipartFile file);
}
