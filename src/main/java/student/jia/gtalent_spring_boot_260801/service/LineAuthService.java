 package student.jia.gtalent_spring_boot_260801.service;

  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.http.MediaType;
  import org.springframework.stereotype.Service;
  import org.springframework.util.LinkedMultiValueMap;
  import org.springframework.util.MultiValueMap;
  import org.springframework.web.client.RestClient;
  import org.springframework.web.client.RestClientException;

  import student.jia.gtalent_spring_boot_260801.constant.ResponseMessages;
  import student.jia.gtalent_spring_boot_260801.exception.AuthException;

  @Service
  public class LineAuthService {

      private static final String LINE_VERIFY_URL =
              "https://api.line.me/oauth2/v2.1/verify";

      private final RestClient restClient;
      private final String channelId;


     public LineAuthService(
          @Value("${line.login.channel-id}") String channelId) {
      this.restClient = RestClient.create();
      this.channelId = channelId;

  }

      public String verifyIdToken(String idToken) {
          if (idToken == null || idToken.isBlank()) {
              throw new AuthException(
                      "idToken",
                      ResponseMessages.TOKEN_INVALID
              );
          }

          MultiValueMap<String, String> formData =
                  new LinkedMultiValueMap<>();

          formData.add("id_token", idToken.trim());
          formData.add("client_id", channelId);

          try {
              LineVerifyResponse response = restClient.post()
                      .uri(LINE_VERIFY_URL)
                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .body(formData)
                      .retrieve()
                      .body(LineVerifyResponse.class);

              if (response == null
                      || response.sub() == null
                      || response.sub().isBlank()) {
                  throw new AuthException(
                          "idToken",
                          ResponseMessages.TOKEN_INVALID
                  );
              }

              return response.sub();

          } catch (AuthException exception) {
              throw exception;

          } catch (RestClientException exception) {
              throw new AuthException(
                      "idToken",
                      ResponseMessages.TOKEN_INVALID
              );
          }
      }

      private record LineVerifyResponse(
              String iss,
              String sub,
              String aud,
              Long exp,
              Long iat,
              String name,
              String picture,
              String email) {
      }
  }
