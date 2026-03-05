package com.example.stdio.tool;

import com.example.stdio.domain.Product;
import com.example.stdio.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * MCP Tools for Product domain
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductTools {

    private final ProductService productService;

    @Tool(description = "모든 상품 정보를 조회합니다.")
    public List<Product> getAllProducts() {
        log.info("MCP Tool: getAllProducts called");
        return productService.getAllProducts();
    }

    @Tool(description = "ID로 특정 상품을 조회합니다.")
    public Product getProductById(
            @ToolParam(description = "상품 ID", required = true) Long id
    ) {
        log.info("MCP Tool: getProductById called with id={}", id);
        return productService.getProduct(id);
    }

    @Tool(description = "상품명으로 상품을 검색합니다. 부분 일치를 지원합니다.")
    public List<Product> searchProductsByName(
            @ToolParam(description = "검색할 상품명", required = true) String name
    ) {
        log.info("MCP Tool: searchProductsByName called with name={}", name);
        return productService.searchProductsByName(name);
    }

    @Tool(description = "새로운 상품을 생성합니다.")
    public Product createProduct(
            @ToolParam(description = "상품명", required = true) String name,
            @ToolParam(description = "상품 설명") String description,
            @ToolParam(description = "가격", required = true) BigDecimal price,
            @ToolParam(description = "재고 수량", required = true) Integer stock
    ) {
        log.info("MCP Tool: createProduct called with name={}, price={}, stock={}", name, price, stock);
        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .build();
        return productService.createProduct(product);
    }

    @Tool(description = "상품 정보를 수정합니다.")
    public Product updateProduct(
            @ToolParam(description = "상품 ID", required = true) Long id,
            @ToolParam(description = "새로운 상품명", required = true) String name,
            @ToolParam(description = "새로운 상품 설명") String description,
            @ToolParam(description = "새로운 가격", required = true) BigDecimal price,
            @ToolParam(description = "새로운 재고 수량", required = true) Integer stock
    ) {
        log.info("MCP Tool: updateProduct called with id={}, name={}, price={}, stock={}", id, name, price, stock);
        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .build();
        return productService.updateProduct(id, product);
    }

    @Tool(description = "상품을 삭제합니다.")
    public String deleteProduct(
            @ToolParam(description = "상품 ID", required = true) Long id
    ) {
        log.info("MCP Tool: deleteProduct called with id={}", id);
        productService.deleteProduct(id);
        return "Product deleted successfully: id=" + id;
    }
}
