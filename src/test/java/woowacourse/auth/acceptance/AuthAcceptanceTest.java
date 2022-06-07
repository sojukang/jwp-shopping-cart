package woowacourse.auth.acceptance;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import woowacourse.auth.dto.LoginRequest;
import woowacourse.auth.dto.LoginTokenResponse;
import woowacourse.shoppingcart.acceptance.AcceptanceTest;
import woowacourse.shoppingcart.dto.customer.CustomerResponse;

@DisplayName("인증 관련 기능")
public class AuthAcceptanceTest extends AcceptanceTest {

    @DisplayName("Bearer Auth 로그인 성공")
    @Test
    void myInfoWithBearerAuth() {
        // given
        // 회원이 등록되어 있고
        // email, password를 사용해 토큰을 발급받고
        LoginRequest loginRequest = new LoginRequest("puterism@naver.com", "12349053145");
        String accessToken = 로그인_요청_및_토큰발급(loginRequest);

        // when
        // 발급 받은 토큰을 사용하여 내 정보 조회를 요청하면
        ExtractableResponse<Response> response = 회원조회_요청(accessToken, 1L);

        // then
        // 내 정보가 조회된다
        CustomerResponse result = response.as(CustomerResponse.class);
        CustomerResponse expected = new CustomerResponse(1L, "puterism@naver.com", "puterism");
        assertThat(result).usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @DisplayName("Bearer Auth 로그인 실패")
    @Test
    void myInfoWithBadBearerAuth() {
        // when
        // 잘못된 id, password를 사용해 토큰을 요청하면
        LoginRequest loginRequest = new LoginRequest("puterism@naver.com", "123456789");
        ExtractableResponse<Response> loginResponse = 로그인_요청(loginRequest);

        // then
        // 토큰 발급 요청이 거부된다
        assertThat(loginResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @DisplayName("Bearer Auth 유효하지 않은 토큰")
    @Test
    void myInfoWithWrongBearerAuth() {
        // when
        // 유효하지 않은 토큰을 사용하여 내 정보 조회를 요청하면
        ExtractableResponse<Response> response = 회원조회_요청("testToken", 1L);

        // then
        // 내 정보 조회 요청이 거부된다
        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    private ExtractableResponse<Response> 로그인_요청(LoginRequest request) {
        return RestAssured
            .given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .when().post("/api/auth/login")
            .then().log().all()
            .extract();
    }

    public String 로그인_요청_및_토큰발급(LoginRequest request) {
        ExtractableResponse<Response> loginResponse = RestAssured
            .given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .when().post("/api/auth/login")
            .then().log().all()
            .extract();

        LoginTokenResponse loginTokenResponse = loginResponse.body().as(LoginTokenResponse.class);
        return loginTokenResponse.getAccessToken();
    }

    public ExtractableResponse<Response> 회원조회_요청(String token, Long id) {
        return RestAssured
            .given().log().all()
            .header("Authorization", "Bearer " + token)
            .when().get("/api/customers/" + id)
            .then().log().all()
            .extract();
    }
}
