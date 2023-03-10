package shareYourFashion.main.controller.api;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import shareYourFashion.main.config.auth.PrincipalDetails;
import shareYourFashion.main.constant.ImageType;
import shareYourFashion.main.domain.*;
import shareYourFashion.main.domain.valueTypeClass.Image;
import shareYourFashion.main.dto.RequestImageDTO;
import shareYourFashion.main.exception.DoNotFoundImageObjectException;
import shareYourFashion.main.service.FileService;
import shareYourFashion.main.service.FileUtils;
import shareYourFashion.main.service.UserService;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Blob;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FIleController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private UserService userService;

    private static final Logger log = LoggerFactory.getLogger(FIleController.class);


    /* ?????? ????????? ?????? api */
    @GetMapping(value = "/images/{imageType}/{fileName}")
    public ResponseEntity<Resource> responseUserImageEntity(@PathVariable("imageType") String imageType, @PathVariable("fileName") String fileName) throws IOException {

        System.out.println( "fileController : responseUserImageEntity" + imageType);

        ImageEntity image;

        if(imageType.equals(ImageType.USER_PROFILE_IMAGE.toString())) {
            image = fileService.findProfileImageByImageFileName(fileName);
            return fileUtils.getProfileResponseEntity((UserProfileImage) image);

        } else if (imageType.equals(ImageType.USER_BACKGROUND_PROFILE_IMAGE.toString())) {
            image = fileService.findBackgroundImageByFileName(fileName);
            return fileUtils.getBGProfileResponseEntity((BackgroundProfileImage) image);
        }

        // profile , background profile image ??? ?????? ?????? 404 ?????? ??????
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // user image upload (profile , background image)
    @PostMapping(value = "/images/upload")
    public void saveImage(@RequestParam Map<String  , Object> paramMap , MultipartHttpServletRequest multipartRequest
                          ) throws IOException , DoNotFoundImageObjectException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("authentication = " + authentication);

        MultipartFile file;

        try {
            // ?????? ?????? blob image data??? multipartFile type ?????? ??????
            file = multipartRequest.getFile("blob");

            // blob image ??? null ??? ?????? ?????? ?????? ?????????.
            if(file.isEmpty()) {
                throw new DoNotFoundImageObjectException(" do not found blob image multipartFile Object (byte size : 0)");
            }
        } catch(Exception e) {
            throw e;
        }


        String imageType = (String) paramMap.get("imageType");

        // ?????? ??????(image upload folder)??? ????????? image ????????? ?????? image ?????? ????????????.
        Image image = fileUtils.saveImage(file, imageType);

        // db??? ????????? Image entity ??????(????????? ????????? ?????? , ?????? ???????????? ?????? ????????? ??????)
        Optional<Object> imageEntity = Optional.empty();
        System.out.println("imageEntity = " + imageEntity);
            // ????????? ????????? ?????? ??????
//        PrincipalDetails userDetails = (PrincipalDetails)authentication.getPrincipal();
        // ????????? ?????? ????????? ?????? entity ????????????

//        User principal = userService.findByEmail(userDetails.getEmail());
//        System.out.println("principal = " + principal);

        if(image.getImageType().equals(ImageType.USER_PROFILE_IMAGE)) {
            Object img = imageEntity.orElse(fileService.createUserProfileEntity(image));
            System.out.println("img = " + img);

            // ????????? ????????? ???????????? ??????
//            principal.userToProfileImage((UserProfileImage) img);
        }
        else if(image.getImageType().equals(ImageType.USER_BACKGROUND_PROFILE_IMAGE)) {
            Object img = imageEntity.orElse(fileService.createBackgroundProfileImageEntity(image));

            // ????????? ????????? ???????????? ??????
//            principal.userToBDProfileImage( (BackgroundProfileImage) img);
        }
        else {
            throw new DoNotFoundImageObjectException("image Entity is null");
        }


    }


}