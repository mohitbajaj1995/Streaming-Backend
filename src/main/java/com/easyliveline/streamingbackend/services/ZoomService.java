package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.dto.ZoomSignatureResponse;
import com.easyliveline.streamingbackend.dto.ZoomWithMeetingSize;
import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.exceptions.PermissionDeniedDataAccessExceptionWithRole;
import com.easyliveline.streamingbackend.interfaces.*;
import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import com.easyliveline.streamingbackend.util.JwtUtil;
import com.easyliveline.streamingbackend.util.PlatformUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ZoomService {

    private final ZoomRepository zoomRepository;
    private final UserRepository userRepository;
    private final MeetingService meetingService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;


    private static final long EXPIRATION_TIME = 4 * 3600 * 1000L; // 4 hours in milliseconds
//    static long expirationEpoch = (System.currentTimeMillis() + EXPIRATION_TIME) / 1000;
//    private static final String API_URL = "https://zoom.us/oauth/token";
//    private static final String ACCOUNT_ID = "pkQ13dQMS5q65JZHnHUorw";
//    private static final String API_KEY = "iJLVcepySni5kR6zhIPaPQ";
//    private static final String API_SECRET = "1N8sdiR645I1mp8Ru0tVtIeT0jdk85hA";

    @Autowired
    public ZoomService(ZoomRepository zoomRepository,
                       UserRepository userRepository,
                       MeetingService meetingService,
                       RedisService redisService, ObjectMapper objectMapper) {
        this.zoomRepository = zoomRepository;
        this.userRepository = userRepository;
        this.meetingService = meetingService;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    public Zoom createZoom(ZoomCreateRequest requestBody) {
        return ExceptionWrapper.handle(() -> {
            log.info("Creating new Zoom configuration with email: {}", requestBody.getEmail());
            Zoom zoom = new Zoom();
            zoom.setEmail(requestBody.getEmail());
            zoom.setPassword(requestBody.getPassword());
            zoom.setSdkKey(requestBody.getSdkKey());
            zoom.setSdkSecret(requestBody.getSdkSecret());
            zoom.setApiKey(requestBody.getApiKey());
            zoom.setApiSecret(requestBody.getApiSecret());
            zoom.setAccountId(requestBody.getAccountId());

            Zoom savedZoom = zoomRepository.save(zoom);
            log.info("Successfully created Zoom configuration with ID: {}", savedZoom.getId());
            return savedZoom;
        });
    }

    @Transactional
    public void deleteZoomById(Long id) {
        ExceptionWrapper.handleVoid(() -> {
            log.info("Deleting Zoom configuration with ID: {}", id);
            try {
                zoomRepository.deleteByZoomId(id);
                log.info("Successfully deleted Zoom configuration with ID: {}", id);
            } catch (Exception e) {
                log.error("Failed to delete Zoom configuration with ID: {}", id, e);
                throw e;
            }
        });
    }

    public Zoom updateZoom(Long id, ZoomUpdateRequest requestBody) {
        return ExceptionWrapper.handle(() -> {
            log.info("Updating Zoom configuration with ID: {}", id);

            Zoom zoom = zoomRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Zoom configuration not found with ID: {}", id);
                        return new RuntimeException("Zoom not found");
                    });

            log.debug("Found Zoom configuration with ID: {}, current email: {}", id, zoom.getEmail());

            zoom.setEmail(requestBody.getEmail());
            zoom.setPassword(requestBody.getPassword());
            zoom.setSdkKey(requestBody.getSdkKey());
            zoom.setSdkSecret(requestBody.getSdkSecret());
            zoom.setApiKey(requestBody.getApiKey());
            zoom.setApiSecret(requestBody.getApiSecret());
            zoom.setAccountId(requestBody.getAccountId());

            Zoom updatedZoom = zoomRepository.save(zoom);
            log.info("Successfully updated Zoom configuration with ID: {}, new email: {}", id, updatedZoom.getEmail());

            return updatedZoom;
        });
    }


    public List<ZoomWithMeetingSize> getAllZooms() {
        return zoomRepository.findZoomsWithMeetingSize();
    }

    public String getEncodedSignature(Meeting meeting, RoleType userRole, Long userId, Map<String, String> response) {
        return ExceptionWrapper.handle(() -> {
            log.info("Generating encoded signature for meeting: {}, user role: {}, user ID: {}",
                    meeting.getMeetingNumber(), userRole, userId);

            String usernameOfUser = userRepository.findUsernameById(userId);

            String platform = PlatformUtils.detectPlatform();
            log.info("Detected platform: {}", platform);

            Map<String, Object> root = new HashMap<>();

            switch (platform) {
                case "Web Browser":
                    log.debug("Generating web configuration for user: {}", usernameOfUser);
                    root.put("web", getWebConfig(meeting, usernameOfUser, response));
                    break;
                case "Android":
                case "Android WebView":
                    log.debug("Generating Android configuration for user: {}", usernameOfUser);
                    root.put("android", getAndroidConfig(meeting, usernameOfUser, response));
                    break;
                case "Windows":
                    log.debug("Generating Windows configuration for user: {}", usernameOfUser);
                    root.put("windows", getWindowsConfig(meeting, usernameOfUser, response));
                    break;
                default:
                    log.error("Unsupported platform detected: {}", platform);
                    throw new RuntimeException("Unsupported platform: " + platform);
            }

            log.debug("Successfully generated configuration for platform: {}", platform);
            return encodeToJson(root);
        });
    }


    private Map<String, Object> getWebConfig(Meeting meeting, String usernameOfUser, Map<String, String> response) {
        log.debug("Creating web configuration for meeting: {}, user: {}", meeting.getMeetingNumber(), usernameOfUser);
        Map<String, Object> webRoot = new HashMap<>();
        webRoot.put("signature", response.get("signature"));
        webRoot.put("userName", usernameOfUser);
        webRoot.put("userEmail", usernameOfUser + "@streaming.com");
        webRoot.put("sdkKey", meeting.getZoom().getSdkKey());
        webRoot.put("meetingNumber", meeting.getMeetingNumber());
        webRoot.put("passWord", meeting.getMeetingPassword());
        if (response.get("zak") != null) {
            webRoot.put("zak", response.get("zak"));
            log.debug("ZAK token included in web configuration");
        }
        log.debug("Web configuration created successfully");
        return webRoot;
    }

    private Map<String, Object> getAndroidConfig(Meeting meeting, String usernameOfUser, Map<String, String> response) {
        log.debug("Creating Android configuration for meeting: {}, user: {}, role: {}",
                meeting.getMeetingNumber(), usernameOfUser, JwtUtil.getRoleFromJWT());
        Map<String, Object> androidRoot = new HashMap<>();
        String role = JwtUtil.getRoleFromJWT();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", usernameOfUser);
        userMap.put("expiryNotice", "Your account is expiring in 10 days");

        Map<String, Object> appMap = new HashMap<>();
        appMap.put("notice", "Welcome to MyApp");
        appMap.put("fixturesHtml", "");
        appMap.put("enableWatermark", false);
        appMap.put("watermarkDelay", 30000);
        appMap.put("isSensorOrientationEnabledWhenForced", false);
        appMap.put("noTitlebar", true);
        appMap.put("zoomCloudRecordingsUrl", "https://us02web.zoom.us/recording");
        appMap.put("mediaRecorderAudioSource", 7);

        Map<String, Object> meetingMap = new HashMap<>();
        meetingMap.put("name", "Live Meeting");
        meetingMap.put("notice", "");
        meetingMap.put("contactInfo", "");
        meetingMap.put("backgroundUrl", "");
        meetingMap.put("logoUrl", "");
        meetingMap.put("colorDark", "");
        meetingMap.put("colorLight", "");
        meetingMap.put("colorHostUsersBackground", "");
//        if ("streaming".equals(webType)) {
//            if(!SecurityUtil.hasAuthority("GENERATE_ZOOM_SIGNATURE",user, null)) throw new PermissionDeniedDataAccessExceptionWithRole("Unauthorized Access");
//            meetingMap.put("isVideoMeeting", true);
//            meetingMap.put("isAudioMeeting", false);
//            meetingMap.put("isAudioBroadcast", false);
//            meetingMap.put("isAudioBroadcastWithFeedback", false);
//            meetingMap.put("isHost", false);
//            meetingMap.put("isHostOfAudioBroadcastWithFeedback", false);
//        } else if ("cutting_line".equals(webType)) {
        if (role.equals("SUB_HOST")) {
            meetingMap.put("isVideoMeeting", false);
            meetingMap.put("isAudioMeeting", true);
            meetingMap.put("isAudioBroadcast", false);  // for GroundLine
            meetingMap.put("isAudioBroadcastWithFeedback", true);
            meetingMap.put("isHost", true);
            meetingMap.put("isHostOfAudioBroadcastWithFeedback", true);
            meetingMap.put("doShowAllUsersAudioBroadcastWithFeedback", true);
        } else if (role.equals("PARTICIPANT")) {
            meetingMap.put("isVideoMeeting", false);
            meetingMap.put("isAudioMeeting", true);
            meetingMap.put("isAudioBroadcast", false);
            meetingMap.put("isAudioBroadcastWithFeedback", true);
            meetingMap.put("isHost", false);
            meetingMap.put("isHostOfAudioBroadcastWithFeedback", false);
            meetingMap.put("doShowAllUsersAudioBroadcastWithFeedback", false);
        }
//        }else if ("ground_line".equals(webType)) {
//        meetingMap.put("isVideoMeeting", false);
//        meetingMap.put("isAudioMeeting", true);
//        meetingMap.put("isAudioBroadcast", true);
//        meetingMap.put("isAudioBroadcastWithFeedback", false);
//        meetingMap.put("isHost", false);
//        meetingMap.put("isHostOfAudioBroadcastWithFeedback", false);
//        meetingMap.put("doShowAllUsersAudioBroadcastWithFeedback", false);
//        }
        meetingMap.put("isAudioMeetingCanSpeak", true);
        meetingMap.put("isEnableEarSpeakerListen", false);
        meetingMap.put("enableBlockAudioSourceTypes", false);
        meetingMap.put("blockedAudioSourceTypes", "1,2,3");
        meetingMap.put("notListeningAudioSourceTypes", "");
        meetingMap.put("showGuarantorName", true);
        meetingMap.put("doLocalRecording", false);
        meetingMap.put("localRecordingDays", 3);
        meetingMap.put("usersViewSize", "30,18,6,20,50");
        meetingMap.put("usersFlexboxProps", "0,1,4,2");
        meetingMap.put("muteDelay", 500);
        meetingMap.put("animatedAudio", true);


        Map<String, Object> reflectionAndroid = new HashMap<>();

        Map<String, Object> zoomSDKInitParams = new HashMap<>();
        zoomSDKInitParams.put("jwtToken", response.get("signature"));
        zoomSDKInitParams.put("autoRetryVerifyApp", true);
        zoomSDKInitParams.put("enableLog", false);

        // For Host
        Map<String, Object> startMeetingParams = new HashMap<>();
        Map<String, Object> joinMeetingParams = new HashMap<>();
//        if (role.equals("SUB_HOST")) {
//            startMeetingParams.put("displayName", nameOfUser);
//            startMeetingParams.put("meetingNo", meeting.meetingNumber());
//            startMeetingParams.put("zoomAccessToken", response.get("zak"));
//            reflectionAndroid.put("StartMeetingParamsWithoutLogin", startMeetingParams);
//        } else if (role.equals("PARTICIPANT")) {
//            joinMeetingParams.put("displayName", nameOfUser);
//            joinMeetingParams.put("meetingNo", meeting.meetingNumber());
//            joinMeetingParams.put("password", meeting.meetingPassword());
//            reflectionAndroid.put("JoinMeetingParams", joinMeetingParams);
//        }
        joinMeetingParams.put("displayName", usernameOfUser);
        joinMeetingParams.put("meetingNo", meeting.getMeetingNumber());
        joinMeetingParams.put("password", meeting.getMeetingPassword());
        reflectionAndroid.put("JoinMeetingParams", joinMeetingParams);

        Map<String, Object> joinMeetingOptions = new HashMap<>();
        joinMeetingOptions.put("no_bottom_toolbar", true);

        Map<String, Object> meetingSettingsHelper = new HashMap<>();
        meetingSettingsHelper.put("setHideNoVideoUsersEnabled", true);

        Map<String, Object> ZoomUIService = new HashMap<>();
        Map<String, Object> InMeetingVideoController = new HashMap<>();
        Map<String, Object> InMeetingAudioController = new HashMap<>();
        Map<String, Object> InMeetingAICompanionController = new HashMap<>();
        Map<String, Object> InMeetingChatController = new HashMap<>();
        Map<String, Object> InMeetingWhiteboardController = new HashMap<>();
        Map<String, Object> InMeetingShareController = new HashMap<>();


//        if(role.equals("SUB_HOST")){
        Map<String, Object> inMeetingService = new HashMap<>();
//            inMeetingService.put("_claimHostWithHostKey", "hostkey");
//            inMeetingService.put("allowParticipantsToRename", false);
        reflectionAndroid.put("InMeetingService", inMeetingService);
//        }

        reflectionAndroid.put("ZoomSDKInitParams", zoomSDKInitParams);
        reflectionAndroid.put("JoinMeetingOptions", joinMeetingOptions);
        reflectionAndroid.put("MeetingSettingsHelper", meetingSettingsHelper);
        reflectionAndroid.put("ZoomUIService", ZoomUIService);
        reflectionAndroid.put("InMeetingVideoController", InMeetingVideoController);
        reflectionAndroid.put("InMeetingAudioController", InMeetingAudioController);
        reflectionAndroid.put("InMeetingAICompanionController", InMeetingAICompanionController);
        reflectionAndroid.put("InMeetingChatController", InMeetingChatController);
        reflectionAndroid.put("InMeetingWhiteboardController", InMeetingWhiteboardController);
        reflectionAndroid.put("InMeetingShareController", InMeetingShareController);

        androidRoot.put("user", userMap);
        androidRoot.put("app", appMap);
        androidRoot.put("meeting", meetingMap);
        androidRoot.put("reflection", reflectionAndroid);

        log.debug("Android configuration created successfully for user: {}", usernameOfUser);
        return androidRoot;
    }

    private Map<String, Object> getWindowsConfig(Meeting meeting, String usernameOfUser, Map<String, String> response) {
        log.debug("Creating Windows configuration for meeting: {}, user: {}", meeting.getMeetingNumber(), usernameOfUser);
        Map<String, Object> windowsRoot = new HashMap<>();
        Map<String, Object> reflection = new HashMap<>();

        Map<String, Object> authContext = new HashMap<>();
        authContext.put("jwt_token", response.get("signature"));
        log.debug("Setting up auth context for Windows configuration");

        Map<String, Object> joinParam = new HashMap<>();
        joinParam.put("meetingNumber", meeting.getMeetingNumber());
        joinParam.put("userName", usernameOfUser);
        joinParam.put("psw", meeting.getMeetingPassword());
        log.debug("Join parameters configured for meeting: {}", meeting.getMeetingNumber());

        Map<String, Object> meetingConfig = new HashMap<>();
        meetingConfig.put("SetBottomFloatToolbarWndVisibility", false);

        Map<String, Object> videoController = new HashMap<>();
        videoController.put("HideOrShowNoVideoUserOnVideoWall", true);

        Map<String, Object> meetingService = new HashMap<>();
        Map<String, Object> meetingVideoController = new HashMap<>();
        meetingVideoController.put("HideOrShowNoVideoUserOnVideoWall", true);
        meetingService.put("GetMeetingVideoController", meetingVideoController);
        log.debug("Video and meeting controllers configured");

        reflection.put("AuthContext", authContext);
        reflection.put("JoinParam4WithoutLogin", joinParam);
        reflection.put("IMeetingConfigurationDotNetWrap", meetingConfig);
        reflection.put("IMeetingVideoControllerDotNetWrap", videoController);
        reflection.put("IMeetingAudioControllerDotNetWrap", new HashMap<>()); // Empty map
        reflection.put("IMeetingUIControllerDotNetWrap", new HashMap<>()); // Empty map
        reflection.put("IMeetingChatControllerDotNetWrap", new HashMap<>()); // Empty map
        reflection.put("IMeetingRecordingControllerDotNetWrap", new HashMap<>()); // Empty map
        reflection.put("IMeetingServiceDotNetWrap", meetingService);

        windowsRoot.put("reflection", reflection);
        log.debug("Windows configuration created successfully");

        return windowsRoot;
    }

    private String encodeToJson(Map<String, Object> data) {
        return ExceptionWrapper.handle(() -> {
            log.debug("Encoding data to JSON and Base64");
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(data);
                String encodedString = Base64.getEncoder().encodeToString(jsonString.getBytes());
                log.debug("Successfully encoded data to Base64 (length: {})", encodedString.length());
                return encodedString;
            } catch (Exception e) {
                log.error("Error encoding data to JSON and Base64", e);
                throw e;
            }
        });
    }

    private long getValidityMinutesFromSignature(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid JWT token");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(payloadJson, Map.class);

            long exp = ((Number) payload.get("exp")).longValue();
            long iat = ((Number) payload.get("iat")).longValue();

            return (exp - iat) / 60; // minutes
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode ZAK token", e);
        }
    }

    public String generateSignature(Long meetingId) {
        return ExceptionWrapper.handle(() -> {
            RoleType role = RoleType.valueOf(JwtUtil.getRoleFromJWT());
            Long userId = JwtUtil.getUserIdFromJWT();
            int meetingRole = 0;

            Meeting meeting = meetingService.findMeetingWithZoomById(meetingId);

            if (meeting == null) {
                log.error("Meeting not found for user ID: {} with role: {}", userId, role);
                throw new PermissionDeniedDataAccessExceptionWithRole("Meeting not found");
            }

            ZoomSignatureResponse response;
            String cacheZoomSignatureTokenKey = "zoomSignature:" + "_user_" + userId + "_meeting_" + meeting.getId() + "_zoom_" + meeting.getZoom().getId();
            String cachedJson = redisService.getString(cacheZoomSignatureTokenKey);

            if (cachedJson != null) {
                log.debug("Using cached token: {}", cachedJson);
//                System.out.println("Expires in: " + redisService.getExpire(cacheZoomSignatureTokenKey, TimeUnit.SECONDS) + " seconds");
                response = objectMapper.readValue(cachedJson, ZoomSignatureResponse.class);
            } else {
                long currentTimeMillis = System.currentTimeMillis() - 60000; // 1 minute before
                long expirationTimeSecond = (currentTimeMillis + EXPIRATION_TIME) / 1000; // Convert to seconds
                Key signingKey = Keys.hmacShaKeyFor(meeting.getZoom().getSdkSecret().getBytes(StandardCharsets.UTF_8));

                // Create claims
                Map<String, Object> claims = new HashMap<>();
                claims.put("sdkKey", meeting.getZoom().getSdkKey());
                claims.put("appKey", meeting.getZoom().getSdkKey());
                claims.put("mn", meeting.getMeetingNumber());
                claims.put("role", meetingRole);
                claims.put("tokenExp", expirationTimeSecond); // Expiration in seconds

                Header header = Jwts.header()
                        .add("alg", "HS256")
                        .type("JWT")
                        .build();

                String signature = Jwts.builder()
                        .header()
                        .add(header)
                        .and()
                        .claims(claims)
                        .issuedAt(new Date(currentTimeMillis))
                        .expiration(new Date(currentTimeMillis + EXPIRATION_TIME))
                        .signWith(signingKey)
                        .compact();

                log.info("Generated new token: {}", signature);
                System.out.println("New Signature: " + signature);

                response = new ZoomSignatureResponse(signature, null);
                int cacheTimeInMinutesAsPerRole = (int) getValidityMinutesFromSignature(signature);
                redisService.setString(cacheZoomSignatureTokenKey, objectMapper.writeValueAsString(response), cacheTimeInMinutesAsPerRole, TimeUnit.MINUTES);
            }
            return getEncodedSignature(meeting, role, userId, Map.of(
                    "signature", response.getSignature(),
                    "zak", response.getZak() != null ? response.getZak() : ""
            ));
        });
    }
}
