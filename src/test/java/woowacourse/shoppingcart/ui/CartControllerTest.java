package woowacourse.shoppingcart.ui;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
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
import woowacourse.shoppingcart.dto.cart.CartItemRequest;
import woowacourse.shoppingcart.dto.cart.CartItemUpdateRequest;

public class CartControllerTest extends AcceptanceTest {

    private static final long CUSTOMER_ID = 1L;

    @Test
    @DisplayName("없는 상품 ID로 아이템 추가 요청할 경우 404 응답을 반환한다.")
    void addCartItem_notFound() {
        //given
        String token = 로그인_요청_및_토큰발급(new LoginRequest("puterism@naver.com", "12349053145"));

        //when
        ExtractableResponse<Response> response = 장바구니_아이템_추가_요청(token, CUSTOMER_ID, 99L, 7);

        //then
        Assertions.assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value()),
            () -> assertThat(response.body().jsonPath().getString("message")).isEqualTo("존재하지 않는 상품 ID입니다.")
        );
    }

    @Test
    @DisplayName("이미 추가된 상품을 다시 추가 요청할 경우 400 응답을 반환한다.")
    void addCartItem_alreadyInStock() {
        //given
        String token = 로그인_요청_및_토큰발급(new LoginRequest("puterism@naver.com", "12349053145"));

        //when
        장바구니_아이템_추가_요청(token, CUSTOMER_ID, 1L, 7);
        ExtractableResponse<Response> response = 장바구니_아이템_추가_요청(token, CUSTOMER_ID, 1L, 7);

        //then
        Assertions.assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
            () -> assertThat(response.body().jsonPath().getString("message")).isEqualTo("이미 담겨있는 상품입니다.")
        );
    }

    @Test
    @DisplayName("아이템 추가 요청이 재고보다 클 경우 400 응답을 반환한다.")
    void addCartItem_overQuantity() {
        //given
        String token = 로그인_요청_및_토큰발급(new LoginRequest("puterism@naver.com", "12349053145"));

        //when
        ExtractableResponse<Response> response = 장바구니_아이템_추가_요청(token, CUSTOMER_ID, 1L, 11);

        //then
        Assertions.assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
            () -> assertThat(response.body().jsonPath().getString("message")).isEqualTo("재고가 부족합니다.")
        );
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID를 삭제 요청 하는 경우 404 응답을 반환한다.")
    void deleteCartItem_notFound() {
        //given
        String token = 로그인_요청_및_토큰발급(new LoginRequest("puterism@naver.com", "12349053145"));

        //when
        ExtractableResponse<Response> response = 장바구니_아이템_삭제_요청(token, CUSTOMER_ID, 99L);

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());

        //then
        Assertions.assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value()),
            () -> assertThat(response.body().jsonPath().getString("message")).isEqualTo("존재하지 않는 상품 ID입니다.")
        );
    }

    @Test
    @DisplayName("구매 수량 업데이트 요청이 재고보다 클 경우 400 응답을 반환한다.")
    void updateCount_overQuantity() {
        //given
        String token = 로그인_요청_및_토큰발급(new LoginRequest("puterism@naver.com", "12349053145"));

        //when
        ExtractableResponse<Response> response = 장바구니_아이템_구매_수_업데이트(token, CUSTOMER_ID, 1L, 11);

        //then
        Assertions.assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
            () -> assertThat(response.body().jsonPath().getString("message")).isEqualTo("재고가 부족합니다.")
        );
    }

    @Test
    @DisplayName("없는 상품 ID로 구매 수량 업데이트 요청할 경우 404 응답을 반환한다.")
    void updateCount_notFound() {
        //given
        String token = 로그인_요청_및_토큰발급(new LoginRequest("puterism@naver.com", "12349053145"));

        //when
        ExtractableResponse<Response> response = 장바구니_아이템_구매_수_업데이트(token, CUSTOMER_ID, 99L, 7);

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());

        //then
        Assertions.assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value()),
            () -> assertThat(response.body().jsonPath().getString("message")).isEqualTo("존재하지 않는 상품 ID입니다.")
        );
    }

    private String 로그인_요청_및_토큰발급(LoginRequest request) {
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

    public static ExtractableResponse<Response> 장바구니_아이템_추가_요청(String token, long customerId, Long productId,
        int count) {
        CartItemRequest request = new CartItemRequest(productId, count);

        return RestAssured
            .given().log().all()
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .when().post("/api/customers/{customerId}/carts", customerId)
            .then().log().all()
            .extract();
    }

    private ExtractableResponse<Response> 장바구니_아이템_구매_수_업데이트(String token, long customerId, Long productId, int count) {
        CartItemUpdateRequest request = new CartItemUpdateRequest(count);
        return RestAssured
            .given().log().all()
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .when().patch("/api/customers/{customerId}/carts?productId={productId}", customerId, productId)
            .then().log().all()
            .extract();
    }

    public static ExtractableResponse<Response> 장바구니_아이템_삭제_요청(String token, long customerId, long productId) {
        return RestAssured
            .given().log().all()
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().delete("/api/customers/{customerId}/carts?productId={productId}", customerId, productId)
            .then().log().all()
            .extract();
    }
}
