package com.hebn.interfaces;

import com.google.common.collect.Lists;
import com.hebn.application.ErrorLoggingUtils;
import com.hebn.application.ImageDownloadUtils;
import com.hebn.interfaces.adapter.CyworldAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @ResponseBody
    @RequestMapping(value = "/api/cyImageDownload", method = RequestMethod.POST)
    public String cyworldImageDownload(@RequestParam(value = "tid") String tid, @RequestParam(value = "directoryPath") String directoryPath) {
        try {
            Map<String, Collection<String>> totalImageUploadDateAndImageLinkMap = cyworldAdapter.getTotalImageLinkList(tid);

            for (Map.Entry<String, Collection<String>> entry : totalImageUploadDateAndImageLinkMap.entrySet()) {
                String imageName = entry.getKey();
                List<String> imageUrlList = Lists.newArrayList(entry.getValue());

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

}