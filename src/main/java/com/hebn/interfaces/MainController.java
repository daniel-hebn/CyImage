package com.hebn.interfaces;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hebn.application.ErrorLoggingUtils;
import com.hebn.application.ImageDownloadUtils;
import com.hebn.interfaces.adapter.CyworldAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Created by greg.lee on 2016. 9. 22..
 */
@Slf4j
@Controller
public class MainController {

    @Autowired
    private CyworldAdapter cyworldAdapter;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String cyworldAccountInfoForm() {
        return "cy-account-form";
    }

    @ResponseBody
    @RequestMapping(value = "/api/simple-CyImageDownload", method = RequestMethod.POST)
    public String cyworldImageDownloadByTid(@RequestBody ImageDownloadDto.Simple simpleParam) {
        String tid = simpleParam.getTid();
        String directoryPath = simpleParam.getDirectoryPath();

        try {
            Map<String, String> emptyCookie = Maps.newHashMap();
            Map<String, Collection<String>> totalImageUploadDateAndImageLinkMap = cyworldAdapter.getTotalImageLinkList(tid, emptyCookie);

            for (Map.Entry<String, Collection<String>> entry : totalImageUploadDateAndImageLinkMap.entrySet()) {
                String imageName = entry.getKey();
                List<String> imageUrlList = Lists.newArrayList(entry.getValue());

                // NOTE: 동일한 이름을 가질 경우 [1], [2] 등을 이미지명 뒤에 붙여 처리함
                IntStream.range(0, imageUrlList.size())
                        .forEach(idx ->
                                ImageDownloadUtils.fileDownloadByUrl(directoryPath, imageUrlList.get(idx), imageName, idx)
                        );
            }
            return "이미지 다운로드가 완료되었습니다.";
        } catch (Exception e) {
            ErrorLoggingUtils.errorLogging(log, e, tid, directoryPath);
            throw new RuntimeException(e);
        }
    }


    @ResponseBody
    @RequestMapping(value = "/api/complex-CyImageDownload", method = RequestMethod.POST)
    public String cyworldImageDownloadByAccountInfo(@RequestBody ImageDownloadDto.Complex complexParam) {
        String email = complexParam.getEmail();
        String passwdRsa = complexParam.getPasswdRsa();
        String directoryPath = complexParam.getDirectoryPath();

        try {
            Map<String, String> cookieAfterLogin = cyworldAdapter.getCookieAfterLogin(email, passwdRsa);
            String tid = cyworldAdapter.getCyworldPersonalTid(cookieAfterLogin);
            Map<String, Collection<String>> totalImageUploadDateAndImageLinkMap = cyworldAdapter.getTotalImageLinkList(tid, cookieAfterLogin);

            for (Map.Entry<String, Collection<String>> entry : totalImageUploadDateAndImageLinkMap.entrySet()) {
                String imageName = entry.getKey();
                List<String> imageUrlList = Lists.newArrayList(entry.getValue());

                // NOTE: 동일한 이름을 가질 경우 [1], [2] 등을 이미지명 뒤에 붙여 처리함
                IntStream.range(0, imageUrlList.size())
                        .forEach(idx ->
                                ImageDownloadUtils.fileDownloadByUrl(directoryPath, imageUrlList.get(idx), imageName, idx)
                        );
            }
            return "이미지 다운로드가 완료되었습니다.";
        } catch (Exception e) {
            ErrorLoggingUtils.errorLogging(log, e, email, directoryPath);
            throw new RuntimeException(e);
        }
    }

}
