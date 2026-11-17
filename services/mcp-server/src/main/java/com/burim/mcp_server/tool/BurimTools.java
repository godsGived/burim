package com.burim.mcp_server.tool;

import com.burim.mcp_server.dto.PageResponse;
import com.burim.mcp_server.dto.ProductDto;
import com.burim.mcp_server.dto.ReviewDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class BurimTools {

    private static final Logger log = LoggerFactory.getLogger(BurimTools.class);

    private final RestClient productRestClient;
    private final RestClient reviewRestClient;

    public BurimTools(
            @Qualifier("productRestClient") RestClient productRestClient,
            @Qualifier("reviewRestClient") RestClient reviewRestClient) {
        this.productRestClient = productRestClient;
        this.reviewRestClient = reviewRestClient;
    }

    /**
     * Инструмент 1: Поиск товаров с фильтрами.
     * Лимит по спеке: по умолчанию 5, максимум 10.
     */
    @McpTool(
            name = "searchProducts",
            description = "Поиск товаров в магазине Burim по ключевым словам, цене и наличию. Возвращает список найденных товаров.",
            annotations = @McpTool.McpAnnotations(
                    title = "Поиск товаров",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    public List<ProductDto> searchProducts(
            @McpToolParam(description = "Поисковый запрос (название товара, категория, бренд, например 'keyboard' или 'кофемашина')", required = false)
            String query,

            @McpToolParam(description = "Максимальная цена товара в USD (например, 150)", required = false)
            BigDecimal maxPrice,

            @McpToolParam(description = "Искать только товары в наличии (true/false)", required = false)
            Boolean inStock,

            @McpToolParam(description = "Лимит количества товаров (по умолчанию 5, максимум 10)", required = false)
            Integer limit
    ) {
        long start = System.currentTimeMillis();
        int effectiveLimit = (limit == null || limit <= 0) ? 5 : Math.min(limit, 10);
        log.info("MCP Tool [searchProducts] START - query='{}', maxPrice={}, inStock={}, requestedLimit={}, effectiveLimit={}",
                query, maxPrice, inStock, limit, effectiveLimit);

        PageResponse<ProductDto> response = productRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/v1/products");
                    if (query != null && !query.isBlank()) {
                        uriBuilder.queryParam("query", query);
                    }
                    if (maxPrice != null) {
                        uriBuilder.queryParam("maxPrice", maxPrice);
                    }
                    if (inStock != null) {
                        uriBuilder.queryParam("inStock", inStock);
                    }
                    uriBuilder.queryParam("size", effectiveLimit);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<PageResponse<ProductDto>>() {});

        List<ProductDto> results = (response != null && response.content() != null) ? response.content() : List.of();
        long duration = System.currentTimeMillis() - start;
        log.info("MCP Tool [searchProducts] FINISHED in {} ms - returned {} products", duration, results.size());
        return results;
    }

    /**
     * Инструмент 2: Детали конкретного товара.
     * По спеке: если 404 — возвращаем структурированную ошибку, а не падаем со стек-трейсом.
     */
    @McpTool(
            name = "getProduct",
            description = "Получить детальную информацию о конкретном товаре по его уникальному идентификатору (ID).",
            annotations = @McpTool.McpAnnotations(
                    title = "Карточка товара",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    public Object getProduct(
            @McpToolParam(description = "Числовой ID товара в магазине", required = true)
            Long productId
    ) {
        long start = System.currentTimeMillis();
        log.info("MCP Tool [getProduct] START - productId={}", productId);
        try {
            ProductDto product = productRestClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .body(ProductDto.class);
            long duration = System.currentTimeMillis() - start;
            log.info("MCP Tool [getProduct] FINISHED in {} ms - found product: id={}, name='{}'",
                    duration, productId, product != null ? product.name() : null);
            return product;
        } catch (HttpClientErrorException.NotFound e) {
            long duration = System.currentTimeMillis() - start;
            log.warn("MCP Tool [getProduct] 404 NOT FOUND in {} ms - productId={}", duration, productId);
            return Map.of("error", "Товар не найден", "productId", productId);
        }
    }

    /**
     * Инструмент 3: Отзывы к товару.
     * Лимит по спеке: по умолчанию 5, максимум 20.
     * Публичный эндпоинт, userId отсутствует.
     */
    @McpTool(
            name = "getProductReviews",
            description = "Получить список отзывов покупателей о товаре по его ID. В отзывах содержатся оценка, плюсы, минусы и текст.",
            annotations = @McpTool.McpAnnotations(
                    title = "Отзывы о товаре",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    public List<ReviewDto> getProductReviews(
            @McpToolParam(description = "Числовой ID товара, отзывы к которому нужно получить", required = true)
            Long productId,

            @McpToolParam(description = "Количество отзывов (по умолчанию 5, максимум 20)", required = false)
            Integer limit
    ) {
        long start = System.currentTimeMillis();
        int effectiveLimit = (limit == null || limit <= 0) ? 5 : Math.min(limit, 20);
        log.info("MCP Tool [getProductReviews] START - productId={}, requestedLimit={}, effectiveLimit={}",
                productId, limit, effectiveLimit);

        try {
            List<ReviewDto> reviews = reviewRestClient.get()
                    .uri("/api/v1/reviews/products/{productId}", productId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ReviewDto>>() {});

            if (reviews == null || reviews.isEmpty()) {
                long duration = System.currentTimeMillis() - start;
                log.info("MCP Tool [getProductReviews] FINISHED in {} ms - returned 0 reviews for productId={}", duration, productId);
                return List.of();
            }

            List<ReviewDto> limited = reviews.stream()
                    .limit(effectiveLimit)
                    .toList();
            long duration = System.currentTimeMillis() - start;
            log.info("MCP Tool [getProductReviews] FINISHED in {} ms - returned {} reviews (total: {}) for productId={}",
                    duration, limited.size(), reviews.size(), productId);
            return limited;
        } catch (HttpClientErrorException.NotFound e) {
            long duration = System.currentTimeMillis() - start;
            log.warn("MCP Tool [getProductReviews] 404 NOT FOUND in {} ms - productId={}", duration, productId);
            return List.of();
        }
    }
}
