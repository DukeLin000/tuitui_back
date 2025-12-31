package org.example.tuitui.commerce;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/commerce")
@RequiredArgsConstructor
public class CartController {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    // --- 商品區 (先寫一個簡單的建立商品，方便測試) ---

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // --- 購物車區 ---

    // 1. 加入購物車
    @PostMapping("/cart")
    public CartItem addToCart(@RequestBody AddToCartRequest request) {
        Product product = productRepository.findById(request.productId)
                .orElseThrow(() -> new RuntimeException("找不到商品"));

        CartItem item = new CartItem();
        item.setUserId(request.userId);
        item.setProduct(product);
        item.setQuantity(request.quantity);

        return cartItemRepository.save(item);
    }

    // 2. 查看我的購物車 (圖二：需要計算總金額)
    @GetMapping("/cart/{userId}")
    public CartDto getMyCart(@PathVariable String userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        // 計算總金額 (Java Stream 流式計算)
        BigDecimal total = items.stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDto(items, total);
    }

    // --- DTO (資料傳輸物件) ---
    // 簡單定義在裡面即可

    @Data
    static class AddToCartRequest {
        public String userId;
        public String productId;
        public int quantity;
    }

    @Data
    static class CartDto {
        public List<CartItem> items;
        public BigDecimal totalAmount;

        public CartDto(List<CartItem> items, BigDecimal totalAmount) {
            this.items = items;
            this.totalAmount = totalAmount;
        }
    }
}