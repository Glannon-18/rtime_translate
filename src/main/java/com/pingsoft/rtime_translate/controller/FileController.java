package com.pingsoft.rtime_translate.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {


    @RequestMapping("/upload")
    public Map<String, Object> upload(MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        HashMap<String, Object> map = new HashMap<>();

        return map;
    }


}
