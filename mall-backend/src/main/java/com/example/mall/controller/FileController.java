package com.example.mall.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.example.mall.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
public class FileController {

    private static final String ROOT_PATH = System.getProperty("user.dir") + "/files/";

    @Value("${mall.file-host}")
    private String fileHost;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String mainName = IdUtil.fastSimpleUUID();
        String extName = FileUtil.extName(originalFilename);
        String projectPath = ROOT_PATH + mainName + "." + extName;

        File saveFile = new File(projectPath);
        if (!saveFile.getParentFile().exists()) {
            saveFile.getParentFile().mkdirs();
        }
        file.transferTo(saveFile);

        String url = fileHost + "/files/" + mainName + "." + extName;

        return Result.success(url);
    }
}