package com.ecoinsight.bdsp.asd.web;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import com.ecoinsight.bdsp.core.Constants;
import com.ecoinsight.bdsp.core.MemberActivityConstants;
import com.ecoinsight.bdsp.core.authentication.AuthConstants;
import com.ecoinsight.bdsp.core.authentication.JwtTokenManager;
import com.ecoinsight.bdsp.core.entity.MemberActivityLog;
import com.ecoinsight.bdsp.core.entity.MemberAuthToken;
import com.ecoinsight.bdsp.core.repository.IMemberAuthTokenRepository;
import com.ecoinsight.bdsp.core.repository.IMemberRepository;

@RestController
@RequestMapping(path = "/api/v1/security")
public class SecurityController extends AsdBaseApiController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    public static final String SYSTEM_ID = "systemId";
    public static final String DEFAULT_SYSTEM_ID = "ASD";
    public static final String ASD_TOKEN  = "ASD_TOKEN";
    public static final String ECRF_TOKEN = "ECRF_TOKEN";
    @Value("${ecrf.apiplatform.url}") 
    private String targetUrl;
    // public static final String TARGET_URL = "https://ecrf.aiplatform101.com/sso?"+SYSTEM_ID+"=%s";
    public static final String COOKIE_DOMAIN = "aiplatform101.com";

    @Value("${authentication.jwt.refresh_token_exp}")
    private int JWT_REFRESH_TOKEN_MIN;

    @Autowired
    private HttpServletRequest request;
    @Autowired
	private HttpServletResponse response;
	@Autowired
	@Qualifier("jwtTokenManager")
	private JwtTokenManager _jwtTokenManager;
    @Autowired
	@Qualifier("identityUserManager")
    private UserDetailsService _userDetailService;
	@Autowired
    private IMemberRepository _userRepository;

    @Autowired
    private IMemberAuthTokenRepository _memberAuthTokenRepository;

    @GetMapping("/sso/redirect")
    public ResponseEntity<?> redirect() throws IOException {
        String headerAuth = request.getHeader(AuthConstants.HTTP_HEADER_AUTHORIZATION);
	    if (!StringUtils.hasText(headerAuth) || !headerAuth.startsWith(AuthConstants.JWT_TOKEN_BEARER)) {
            throw new IOException("Unauthorized access.");    
        }
        LOGGER.info("headerAuth"+String.format(headerAuth));
       final String TARGET_URL = targetUrl+"/sso";
        final String token = headerAuth.substring(7);
        String systemId = super.getSystemId();
        
        Cookie ck = new Cookie(ASD_TOKEN,token);
		ck.setPath("/");
        ck.setMaxAge(300);  // 5 minutes
		ck.setDomain(COOKIE_DOMAIN); 
		response.addCookie(ck);
        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("redirect to "+String.format(TARGET_URL, systemId));
        }

        String redirectUrl = String.format(TARGET_URL, systemId);

        // Instead of sending a redirect, return the redirect URL
        return ResponseEntity.status(HttpStatus.OK).body(redirectUrl);
    
    }

    @GetMapping("/sso")
    public void handleSSO() throws IOException {
        String jwtToken = null;
        String systemId = request.getParameter(SYSTEM_ID);
        if(systemId==null) {
            LOGGER.warn("The systemId parameter is null. set systemId by default systemId. - "+DEFAULT_SYSTEM_ID);
            systemId= DEFAULT_SYSTEM_ID;
        }
        Cookie[] cks = request.getCookies();
        if(cks!=null) {
            for(Cookie ck : cks) {
                String name = ck.getName();
                if(ECRF_TOKEN.equals(name)) {
                    jwtToken = ck.getValue();
                    break;
                }
            }
        }

        if(jwtToken==null) {
            throw new IOException("Unauthorized access.");   
        }

        String authUserName = _jwtTokenManager.getUserName(jwtToken);
        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("username="+authUserName);
        }
        // auto authentication
        // 1. user validation from database
        var members = this._userRepository.queryByMemberId(authUserName, systemId);
					
        // 2. JWT and Refresh Token creation
        var member = members.get(0);
        final String memberKey = member.getMemberKey();
        final String localHost = getRemoteAddress();
        
        final UserDetails userDetails = _userDetailService.loadUserByUsername(memberKey);        
        final String token = _jwtTokenManager.generateToken(userDetails);
        final MemberAuthToken refreshToken = createRefreshToken(memberKey);
        final String refresh = refreshToken.getToken();

        LocalDateTime now = LocalDateTime.now();
        // update the last activity time and client host        
        member.setLastActivityTime(now);
        member.setLastLoginTime(now);
        member.setLocalHost(localHost);
        member.setUserUpdated(memberKey);
        member.setDateUpdated(now);
        member.setRecordStatus(Constants.USER_STATUS_ACTIVE);
        member.setPasswordErrorTry(0); // update the password-error-retry count with 0
        member.setLastTimePasswordError(null);
        
        this._memberRepository.change(member);

        // Write activity log.
        writeActivityLog(new MemberActivityLog(systemId, memberKey, MemberActivityConstants.TYPE_AUTH, MemberActivityConstants.ACT_LOGIN, LocalDateTime.now(), localHost ));

        // 3. frontend app initialization on web browser : redirect /index.html?token=xxx&refresh_token=xxxx
        String targetURL = String.format("http://www.aiplatform.com/sso?token=%$s&refresh_token=%s", token, refresh);
        response.sendRedirect(targetURL);
    }

    /**
     * refresh token 생성
     * @param memberkey
     * @return
     */
    private MemberAuthToken createRefreshToken(final String memberkey) {
        MemberAuthToken refreshToken = new MemberAuthToken();

        // Delete the old token before creating new one if it exists.
        _memberAuthTokenRepository.findByMemberKey(memberkey).ifPresent(t->{
            _memberAuthTokenRepository.delete(memberkey);
        });
    
        refreshToken.setMemberKey(memberkey);
        refreshToken.setExpiryDate(Instant.now().plusMillis(JWT_REFRESH_TOKEN_MIN * 1000));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUserCreated(memberkey);
        refreshToken.setDateCreated(LocalDateTime.now());
    
        _memberAuthTokenRepository.add(refreshToken);
        return refreshToken;
    }
}
