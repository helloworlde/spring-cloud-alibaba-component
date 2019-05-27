package io.github.helloworlde.oss.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import org.apache.commons.codec.CharEncoding;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author HelloWood
 */
@Controller
public class OssController {

    @Autowired
    private OSS oss;

    @Value("${spring.cloud.alicloud.bucket}")
    private String BUCKET_NAME;

    @RequestMapping(
            value = "/upload",
            method = RequestMethod.POST,
            produces = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String upload(@RequestParam("fileName") MultipartFile multipartFile) {

        try {
            oss.putObject(BUCKET_NAME, multipartFile.getOriginalFilename(), multipartFile.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/?message=success";
    }

    @GetMapping("/download")
    @ResponseBody
    public void download(@RequestParam String fileName, HttpServletResponse response) {
        OSSObject ossObject = oss.getObject(BUCKET_NAME, fileName);

        if (ossObject != null) {
            try {
                response.setCharacterEncoding(CharEncoding.UTF_8);
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
                IOUtils.copy(ossObject.getObjectContent(), response.getOutputStream());
                response.flushBuffer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}