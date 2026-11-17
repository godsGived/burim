package com.burim.mcp_server.tool;

import com.burim.mcp_server.dto.ProductDto;
import com.burim.mcp_server.dto.ReviewDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BurimToolsTest {

    private MockRestServiceServer mockProductServer;
    private MockRestServiceServer mockReviewServer;
    private BurimTools tools;

    @BeforeEach
    void setUp() {
        // Создаем builders для RestClient с тестовыми URL
        RestClient.Builder productBuilder = RestClient.builder().baseUrl("http://localhost:8081");
        mockProductServer = MockRestServiceServer.bindTo(productBuilder).build();
        RestClient productClient = productBuilder.build();

        RestClient.Builder reviewBuilder = RestClient.builder().baseUrl("http://localhost:8084");
        mockReviewServer = MockRestServiceServer.bindTo(reviewBuilder).build();
        RestClient reviewClient = reviewBuilder.build();

        // Передаем клиенты в наш сервис инструментов
        tools = new BurimTools(productClient, reviewClient);
    }

    @Test
    @DisplayName("searchProducts: отправляет правильные query параметры и парсит ответ")
    void searchProducts_shouldPassCorrectQueryParams() {
        String jsonResponse = """
                {
                  "content": [
                    {
                      "id": 1,
                      "name": "Keychron K2",
                      "description": "Mechanical keyboard",
                      "price": 89.99,
                      "stock": 10,
                      "rating": 4.8,
                      "reviewsCount": 12
                    }
                  ]
                }
                """;

        mockProductServer.expect(requestTo("http://localhost:8081/api/v1/products?query=keyboard&maxPrice=150&inStock=true&size=3"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<ProductDto> result = tools.searchProducts("keyboard", BigDecimal.valueOf(150), true, 3);

        mockProductServer.verify();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Keychron K2");
        assertThat(result.get(0).price()).isEqualByComparingTo(BigDecimal.valueOf(89.99));
    }

    @Test
    @DisplayName("searchProducts: лимит жестко ограничивается максимумом 10")
    void searchProducts_shouldCapLimitAt10() {
        mockProductServer.expect(requestTo("http://localhost:8081/api/v1/products?size=10"))
                .andRespond(withSuccess("{\"content\":[]}", MediaType.APPLICATION_JSON));

        List<ProductDto> result = tools.searchProducts(null, null, null, 50);

        mockProductServer.verify();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("searchProducts: если лимит не указан, по умолчанию запрашивается 5")
    void searchProducts_shouldUseDefaultLimitOf5() {
        mockProductServer.expect(requestTo("http://localhost:8081/api/v1/products?size=5"))
                .andRespond(withSuccess("{\"content\":[]}", MediaType.APPLICATION_JSON));

        List<ProductDto> result = tools.searchProducts(null, null, null, null);

        mockProductServer.verify();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("searchProducts: длинное описание товара усекается до 500 символов + '...'")
    void searchProducts_shouldTruncateDescriptionLongerThan500() {
        String longDescription = "A".repeat(600);
        String jsonResponse = String.format("""
                {
                  "content": [
                    {
                      "id": 1,
                      "name": "Keyboard",
                      "description": "%s",
                      "price": 100
                    }
                  ]
                }
                """, longDescription);

        mockProductServer.expect(requestTo("http://localhost:8081/api/v1/products?size=5"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<ProductDto> result = tools.searchProducts(null, null, null, null);

        mockProductServer.verify();
        assertThat(result).hasSize(1);
        String desc = result.get(0).description();
        assertThat(desc).hasSize(503); // 500 символов + "..."
        assertThat(desc).endsWith("...");
    }

    @Test
    @DisplayName("getProduct: возвращает товар при 200 OK")
    void getProduct_shouldReturnProduct_whenFound() {
        String jsonResponse = """
                {
                  "id": 42,
                  "name": "Philips Coffee Maker",
                  "description": "Great coffee maker",
                  "price": 299.99,
                  "stock": 5,
                  "rating": 4.5,
                  "reviewsCount": 8
                }
                """;

        mockProductServer.expect(requestTo("http://localhost:8081/api/v1/products/42"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        Object result = tools.getProduct(42L);

        mockProductServer.verify();
        assertThat(result).isInstanceOf(ProductDto.class);
        ProductDto product = (ProductDto) result;
        assertThat(product.id()).isEqualTo(42L);
        assertThat(product.name()).isEqualTo("Philips Coffee Maker");
    }

    @Test
    @DisplayName("getProduct: при 404 возвращает понятную ошибку вместо исключения")
    void getProduct_shouldReturnStructuredError_whenNotFound() {
        mockProductServer.expect(requestTo("http://localhost:8081/api/v1/products/999"))
                .andRespond(withResourceNotFound());

        Object result = tools.getProduct(999L);

        mockProductServer.verify();
        assertThat(result).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> errorMap = (Map<String, Object>) result;
        assertThat(errorMap).containsEntry("error", "Товар не найден");
        assertThat(errorMap).containsEntry("productId", 999L);
    }

    @Test
    @DisplayName("getProductReviews: ограничивает лимит отзывов максимумом 20")
    void getProductReviews_shouldCapLimitAt20() {
        // Генерируем 25 отзывов
        StringBuilder jsonBuilder = new StringBuilder("[");
        for (int i = 1; i <= 25; i++) {
            jsonBuilder.append(String.format("{\"id\":%d,\"productId\":1,\"rating\":5,\"title\":\"Review %d\"},", i, i));
        }
        jsonBuilder.deleteCharAt(jsonBuilder.length() - 1); // убираем последнюю запятую
        jsonBuilder.append("]");

        mockReviewServer.expect(requestTo("http://localhost:8084/api/v1/reviews/products/1"))
                .andRespond(withSuccess(jsonBuilder.toString(), MediaType.APPLICATION_JSON));

        List<ReviewDto> reviews = tools.getProductReviews(1L, 50);

        mockReviewServer.verify();
        // Запросили 50, но код должен ограничить максимумом 20
        assertThat(reviews).hasSize(20);
    }

    @Test
    @DisplayName("getProductReviews: если лимит не указан, возвращается максимум 5 отзывов")
    void getProductReviews_shouldUseDefaultLimitOf5() {
        StringBuilder jsonBuilder = new StringBuilder("[");
        for (int i = 1; i <= 10; i++) {
            jsonBuilder.append(String.format("{\"id\":%d,\"productId\":1,\"rating\":5,\"title\":\"Review %d\"},", i, i));
        }
        jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        jsonBuilder.append("]");

        mockReviewServer.expect(requestTo("http://localhost:8084/api/v1/reviews/products/1"))
                .andRespond(withSuccess(jsonBuilder.toString(), MediaType.APPLICATION_JSON));

        List<ReviewDto> reviews = tools.getProductReviews(1L, null);

        mockReviewServer.verify();
        assertThat(reviews).hasSize(5);
    }

    @Test
    @DisplayName("getProductReviews: при 404 возвращает пустой список")
    void getProductReviews_shouldReturnEmptyList_whenNotFound() {
        mockReviewServer.expect(requestTo("http://localhost:8084/api/v1/reviews/products/999"))
                .andRespond(withResourceNotFound());

        List<ReviewDto> reviews = tools.getProductReviews(999L, 5);

        mockReviewServer.verify();
        assertThat(reviews).isEmpty();
    }

    @Test
    @DisplayName("getProductReviews: текстовые поля отзывов усекаются до 500 символов")
    void getProductReviews_shouldTruncateTextFields() {
        String longText = "B".repeat(600);
        String jsonResponse = String.format("""
                [
                  {
                    "id": 1,
                    "productId": 1,
                    "rating": 5,
                    "title": "%s",
                    "description": "%s",
                    "advantages": "%s",
                    "disadvantages": "%s"
                  }
                ]
                """, longText, longText, longText, longText);

        mockReviewServer.expect(requestTo("http://localhost:8084/api/v1/reviews/products/1"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<ReviewDto> reviews = tools.getProductReviews(1L, 5);

        mockReviewServer.verify();
        assertThat(reviews).hasSize(1);
        ReviewDto review = reviews.get(0);
        assertThat(review.title()).hasSize(503).endsWith("...");
        assertThat(review.description()).hasSize(503).endsWith("...");
        assertThat(review.advantages()).hasSize(503).endsWith("...");
        assertThat(review.disadvantages()).hasSize(503).endsWith("...");
    }

    @Test
    @DisplayName("Безопасность: ReviewDto не должен содержать поле userId")
    void reviewDto_shouldNotContainUserIdField() {
        List<String> fieldNames = Arrays.stream(ReviewDto.class.getDeclaredFields())
                .map(Field::getName)
                .toList();

        assertThat(fieldNames)
                .doesNotContain("userId")
                .doesNotContain("user_id");
    }
}
