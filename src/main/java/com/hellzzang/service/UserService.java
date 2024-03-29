package com.hellzzang.service;


import com.hellzzang.dto.UserDto;
import com.hellzzang.dto.myInfo.MyInfoResponseDto;
import com.hellzzang.dto.myInfo.UserEditRequestDto;
import com.hellzzang.entity.CommunityFile;
import com.hellzzang.entity.Email;
import com.hellzzang.entity.FileInfo;
import com.hellzzang.entity.User;
import com.hellzzang.jwt.TokenProvider;
import com.hellzzang.repository.EmailRepository;
import com.hellzzang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private static final String FROM_ADDRESS = "gidwns617@naver.com";
    private final SpringTemplateEngine thymeleafTemplateEngine;
    private final TokenProvider tokenProvider;
    private final FileService fileService;

    @Value("${app_url}")
    private String appUrl;

    @Value("${thumbnail.url}")
    private String thumbnailUrl;

    /**
    * @methodName : signup
    * @date : 2023-04-20 오후 1:02
    * @author : hj
    * @Description: 회원가입 시 호출되는 메서드
    **/
    public User signup(UserDto userDto) {  //userId을 통해 이미 가입되어 있는지 확인

        //이미 가입되어 있는경우 예외 처리
        if(userRepository.existsByUserId(userDto.getUserId())){
            throw new RuntimeException("이미 가입되어 있는 유저입니다.");
        }

        User user = User.builder() //유저 정보 빌드
                .userId(userDto.getUserId())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .userName(userDto.getUserName())  //이름
                .nickName(userDto.getNickName())
                .address(userDto.getAddress())
                .addressDetail(userDto.getAddressDetail())
                .phone(userDto.getPhone())
                .build();

        //동록된 이메일 인증코드 전부삭제
        emailRepository.deleteByUserId(userDto.getUserId());

        return userRepository.save(user);  // save = DB에 insert
    }

    /**
    * @methodName : sendEmail
    * @date : 2023-05-02 오후 4:46
    * @author : hj
    * @Description: 가입 요청 이메일 전송
    **/
    public void sendEmail(String userId) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(userId);
        helper.setFrom(FROM_ADDRESS);
        helper.setSubject("<Hellzzang> 회원가입을 위해 이메일 인증을 진행해주세요.");

        // create the Thymeleaf context object and add the name variable
        Context thymeleafContext = new Context();

        //유저 확인 코드 생성
        String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"; // 영어 대문자와 숫자
        int CODE_LENGTH = 15;

        Random random = new Random();
        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        System.out.println(code);
        String checkCode = code.toString();

        Email email = Email.builder()
                .userId(userId)
                .checkCode(checkCode)
                .build();

        //회원가입 url
        String joinUrl = appUrl+"/userJoin/"+checkCode;

        thymeleafContext.setVariable("userId", userId);
        thymeleafContext.setVariable("joinUrl", joinUrl);
        emailRepository.save(email);

        // generate the HTML content from the Thymeleaf template
        String htmlContent = thymeleafTemplateEngine.process("email.html", thymeleafContext);

        helper.setText(htmlContent, true);
        mailSender.send(message);
        System.out.println("메일 전송 완료 ----------------------------------------");

    }

    /**
     * @methodName : userIdCheck
     * @date : 2023-05-02 오후 3:24
     * @author : hj
     * @Description: 로그인 시 userId 체크 후 회원 존재 시 메인 이동, 없으면 회원가입 이동
     **/
    @Transactional(readOnly = true)
    public boolean userIdCheck(String userid){

        Optional<User> optionalUser = userRepository.findByUserId(userid);

        //기존에 사용자가 존재할경우 true, 아닐경우 false
        return optionalUser.isPresent();
    }

    /**
     * @methodName : emailCheck
     * @date : 2023-05-03 오후 5:22
     * @author : hj
     * @Description: 회원가입 시 부여된 랜덤 코드를 통해 유저 id 확인
     **/
    @Transactional(readOnly = true)
    public String emailCheck(String checkCode){
        Optional<Email> optionalEmail = emailRepository.findByCheckCode(checkCode);

        optionalEmail.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Email email = optionalEmail.get();
        return email.getUserId();
    }

    @Transactional(readOnly = true)
    public MyInfoResponseDto getMyInfo(String token){
        User user = userRepository.findById(tokenProvider.getJwtTokenId(token)).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new MyInfoResponseDto(user, thumbnailUrl);
    }

    //사용자 수정
    public Long userEdit(UserEditRequestDto userEditRequestDto, String token, MultipartFile thumbnailFile, HttpServletRequest request) {

        //사용자 조회
        User user = userRepository.findById(tokenProvider.getJwtTokenId(token)).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateUserInfo(userEditRequestDto);

        if(thumbnailFile != null){
            //파일 업로드 (공통 파일 이력 테이블 insert)
            FileInfo fileInfo = null;
            try {
                fileInfo = fileService.uploadFile(request, thumbnailFile);
                user.setThumbnailIdx(fileInfo.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return user.getId();
    }
}