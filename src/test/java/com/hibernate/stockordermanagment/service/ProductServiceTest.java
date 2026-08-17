package com.hibernate.stockordermanagment.service;

import com.hibernate.stockordermanagment.dto.request.CreateProductRequest;
import com.hibernate.stockordermanagment.dto.response.ProductResponse;
import com.hibernate.stockordermanagment.entity.Product;
import com.hibernate.stockordermanagment.enums.ProductCategory;
import com.hibernate.stockordermanagment.enums.ProductStatus;
import com.hibernate.stockordermanagment.exception.DuplicateProductException;
import com.hibernate.stockordermanagment.exception.ProductNotFoundException;
import com.hibernate.stockordermanagment.mapper.ProductMapper;
import com.hibernate.stockordermanagment.repository.ProductRepository;
import com.hibernate.stockordermanagment.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private CreateProductRequest createRequest;
    private Product product;

    @BeforeEach
    void setUp() {
        createRequest = new CreateProductRequest();
        createRequest.setName("Kablosuz Kulaklık");
        createRequest.setDescription("Bluetooth destekli kulaklık");
        createRequest.setCategory(ProductCategory.ELECTRONICS);
        createRequest.setPrice(new BigDecimal("1500.00"));
        createRequest.setStockQuantity(20);
        createRequest.setMinimumStockLevel(5);

        product = Product.builder()
                .id(1L)
                .name("Kablosuz Kulaklık")
                .description("Bluetooth destekli kulaklık")
                .category(ProductCategory.ELECTRONICS)
                .price(new BigDecimal("1500.00"))
                .stockQuantity(20)
                .minimumStockLevel(5)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void urunBasariylaOlusturulur() {
        when(productRepository.existsByNameAndStatus(anyString(), any(ProductStatus.class)))
                .thenReturn(false);
        when(productMapper.toEntity(createRequest)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("Kablosuz Kulaklık");
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.createProduct(createRequest);

        assertEquals("Kablosuz Kulaklık", result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void ayniIsimleAktifUrunVarsaExceptionFirlatilir() {
        when(productRepository.existsByNameAndStatus(anyString(), any(ProductStatus.class)))
                .thenReturn(true);

        assertThrows(DuplicateProductException.class,
                () -> productService.createProduct(createRequest));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void olmayanUrunSorgulandigindaExceptionOlusur() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById(999L));
    }

    @Test
    void urunBasariylaGetirilir() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("Kablosuz Kulaklık");
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.getProductById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Kablosuz Kulaklık", result.getName());
    }
}