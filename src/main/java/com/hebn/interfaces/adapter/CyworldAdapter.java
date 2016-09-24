package com.hebn.interfaces.adapter;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by greg.lee on 2016. 9. 22..
 */

@Slf4j
@Component
public class CyworldAdapter {

    private static final int CYWORLD_IMAGE_FOLDER_NUMBER_SIZE = 25;
    private static final String CYWORLD_IMAGE_FOLDER_BASE_NUMBER = "32000000000";

    /**
     * 로그인 후 쿠키 획득
     * <p>
     * TODO passwd_rsa 구현
     *
     * @param email
     * @param passwd
     * @return
     * @throws IOException
     */
    public Map<String, String> getCookieAfterLogin(String email, String passwd) throws IOException {
        if (email == null || passwd == null)
            return Maps.newHashMap();

        // TODO - make passwd_rsa bases passwd
        String passwd_rsa = makePasswdRsaBy(passwd);

        // NOTE: 로그인 페이지 접근. 초기 쿠키 획득을 위해
        Connection.Response loginLoadingResponse = Jsoup.connect("http://cyxso.cyworld.com/Login.sk")
                .method(Connection.Method.GET)
                .timeout(0)
                .execute();

        // NOTE: 로그인 실행
        Connection.Response responseAfterLogin = Jsoup.connect("https://cyxso.cyworld.com/LoginAuth.sk")
                .data("email", email, "passwd_rsa", passwd_rsa)
                .method(Connection.Method.POST)
                .cookies(loginLoadingResponse.cookies())
                .referrer("http://cyxso.cyworld.com/Login.sk")
                .timeout(0)
                .execute();

        return responseAfterLogin.cookies();
    }

    private String makePasswdRsaBy(String passwd) {
        return passwd;
    }

    /**
     * 싸이월드 로그인 시 해당 유저의 내부 PK 값 획득
     * 이를 기반으로 해당 유저의 사진첩 폴더 내 이미지 url 획득함
     *
     * @param cookieAfterLogin
     * @return
     * @throws IOException
     */
    public String getCyworldPersonalTid(Map<String, String> cookieAfterLogin) throws IOException {
        Document homePage = Jsoup.connect("http://www.cyworld.com/cymain/?f=cymain")
                .cookies(cookieAfterLogin)
                .timeout(0)
                .get();

        String tid = parseCyworldTid(homePage);
        return tid;
    }

    /**
     * 싸이월드의 html 내 script 구조가 변경되면 정상 동작하지 않을 것임
     *
     * @param homePage
     * @return
     */
    private String parseCyworldTid(Document homePage) {
        String stringHomePage = homePage.toString();
        int startIdxTid = stringHomePage.indexOf("var tid=") + 1;
        int endIdxTid = stringHomePage.indexOf(";", startIdxTid) - 1;

        return stringHomePage.substring(startIdxTid + "var tid=".length(), endIdxTid);
    }

    /**
     * 싸이월드 로그인 후 사진첩 폴더 내의 이미지 링크 url 획득
     * 첫 페이지 로딩 후, [더보기] 버튼을 순차적으로 눌러가며 paging 이후의 로딩되는 html 에서 이미지 url 파싱함
     * [더보기] 버튼 순차적인 클릭은 recursive 로 구현함
     *
     * @param tid
     * @return
     */
    public Map<String, Collection<String>> getTotalImageLinkList(String tid, Map<String, String> cookiesAfterLogin) throws IOException {
        HashMultimap<String, String> imageUploadDateAndImageLinkMap = HashMultimap.create();

        // NOTE: 이미지 폴더 처음 접근 시
        String firstAccessImageFolderLink = "http://cy.cyworld.com/home/" + tid + "/postlist?folderid=" + getImageFolder(tid) + "&_=" + getUnixTimeAtExecution();
        Document firstAccessImageFolderDoc = getDocumentByConnectTo(firstAccessImageFolderLink, cookiesAfterLogin);
        Elements articleWhenFirstAccessImages = firstAccessImageFolderDoc.select("article");

        if (StringUtils.isEmpty(articleWhenFirstAccessImages.toString()))
            return Maps.newHashMap();

        Elements firstAccessDocElements = firstAccessImageFolderDoc.select("article figure"); // NOTE: 싸이월드 페이지 내의 이미지 존재 elements
        imageLinkParsingAndSave(imageUploadDateAndImageLinkMap, firstAccessDocElements);

        String lastArticleId = articleWhenFirstAccessImages.last().attr("id");
        // NOTE: <input type="hidden" name="morePostCnt" value="24">
        String listSize = firstAccessImageFolderDoc.select("article").last().nextElementSibling().attr("value");

        String[] lastIdAndLastDate = lastArticleId.split("_");
        String lastId = lastIdAndLastDate[0];
        String lastDate = lastIdAndLastDate[1];

        // NOTE: 이미지 더보기 클릭으로 추가 접근 시 - 이미지가 존재하지 않을 때까지 recursive call
        getImageLinkMoreAccessImageFolder(tid, lastId, lastDate, listSize, cookiesAfterLogin, imageUploadDateAndImageLinkMap);

        return imageUploadDateAndImageLinkMap.asMap();
    }

    /**
     * 25 자리의 싸이월드 사진첩 폴더 번호 획득
     *
     * @param tid
     * @return
     */
    private String getImageFolder(String tid) {
        // 25 자리 - 사진첩 폴더 번호.length
        int paddingSize = CYWORLD_IMAGE_FOLDER_NUMBER_SIZE - CYWORLD_IMAGE_FOLDER_BASE_NUMBER.length();
        return Strings.padEnd(tid, paddingSize, '0') + CYWORLD_IMAGE_FOLDER_BASE_NUMBER;
    }

    private long getUnixTimeAtExecution() {
        return System.currentTimeMillis();
    }

    private Document getDocumentByConnectTo(String documentUrl, Map<String, String> cookiesAfterLogin) throws IOException {
        return Jsoup.connect(documentUrl)
                .cookies(cookiesAfterLogin)
                .timeout(0)
                .get();
    }

    private void imageLinkParsingAndSave(HashMultimap<String, String> imageUploadDateAndImageLinkMap, Elements elements) {
        for (Element element : elements) {
            String cyImageName = element.attr("alt");  // NOTE: 이미지 명
            String originImageUrl = element.attr("style"); // NOTE: 이미지 경로

            String stringStartIndex = "background-image:url('";
            String stringEndIndex = "');";
            String imageUrl = originImageUrl.substring(originImageUrl.indexOf(stringStartIndex) + stringStartIndex.length(), originImageUrl.indexOf(stringEndIndex));

            String normalFileType = "common/file_down.asp?";
//            String swfFileType = "common/file_down_swf.asp?";
            if (imageUrl.contains(normalFileType))   // NOTE: 확장 가능한 타입이라면.. 작은 이미지 크게 다운 받기
                imageUrl = imageUrl.replace("269x269", "810x0");

            imageUploadDateAndImageLinkMap.put(cyImageName, imageUrl);
        }
    }

    private void getImageLinkMoreAccessImageFolder(String tid, String lastId, String lastDate, String listSize,
                                                   Map<String, String> cookiesAfterLogin,
                                                   HashMultimap<String, String> imageUploadDateAndImageLinkMap) throws IOException {
        if (Integer.parseInt(listSize) < 24) {   // NOTE: 마지막 페이지 도달 시
            return;
        }

        String linkMoreAccessImageFolder =
                "http://cy.cyworld.com/home/" + tid + "/postmore?lastid=" + lastId + "&lastdate=" + lastDate +
                        "&startdate=&enddate=&folderid=" + getImageFolder(tid) + "&tagname=&listsize=" + listSize +
                        "&_=" + getUnixTimeAtExecution();

        Document moreAccessImageFolderDoc = getDocumentByConnectTo(linkMoreAccessImageFolder, cookiesAfterLogin);

        Elements moreAccessDocElements = moreAccessImageFolderDoc.select("article figure"); // NOTE: 싸이월드 페이지 내의 이미지 존재 elements
        imageLinkParsingAndSave(imageUploadDateAndImageLinkMap, moreAccessDocElements);

        Element lastArticle = moreAccessImageFolderDoc.select("article").last();

        String lastArticleId = lastArticle.attr("id");
        listSize = lastArticle.nextElementSibling().attr("value"); // NOTE: <input type="hidden" name="morePostCnt" value="24">

        String[] lastIdAndLastDate = lastArticleId.split("_");
        lastId = lastIdAndLastDate[0];
        lastDate = lastIdAndLastDate[1];
        getImageLinkMoreAccessImageFolder(tid, lastId, lastDate, listSize, cookiesAfterLogin, imageUploadDateAndImageLinkMap);
    }

}
