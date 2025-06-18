package com.example.computershop.controller;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.example.computershop.entity.Categories;
import com.example.computershop.entity.Products;
import com.example.computershop.service.CategoriesService;
import com.example.computershop.service.ProductService;
import com.example.computershop.service.StorageService;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private CategoriesService categoriesService;

    @Mock
    private StorageService storageService;

    @Mock
    private Model model;

    @InjectMocks
    private ProductController productController;

    private Products mockProduct;
    private Categories mockCategory;
    private MultipartFile mockFile;
    private List<Products> mockProductList;
    private List<Categories> mockCategoryList;

    @BeforeEach
    void setUp() {
        // Mock objects setup
        mockProduct = mock(Products.class);
        mockCategory = mock(Categories.class);
        mockFile = new MockMultipartFile("productImage", "test.jpg", "image/jpeg", "test".getBytes());
        mockProductList = Arrays.asList(mockProduct);
        mockCategoryList = Arrays.asList(mockCategory);
    }

    // Test Case 1: index() method
    @Test
    void testIndex_Success() {
        // Arrange
        when(productService.getAll()).thenReturn(mockProductList);

        // Act
        String result = productController.index(model);

        // Assert
        assertEquals("admin/product/product", result);
        verify(productService, times(1)).getAll();
        verify(model, times(1)).addAttribute("product", mockProductList);

        // Analysis: Return = "admin/product/product", Exception = None, LogMessage = None, Result = PASS
    }

    @Test
    void testIndex_EmptyList() {
        // Arrange
        when(productService.getAll()).thenReturn(Arrays.asList());

        // Act
        String result = productController.index(model);

        // Assert
        assertEquals("admin/product/product", result);
        verify(productService, times(1)).getAll();
        verify(model, times(1)).addAttribute("product", Arrays.asList());

        // Analysis: Return = "admin/product/product", Exception = None, LogMessage = None, Result = PASS
    }

    // Test Case 2: add() method
    @Test
    void testAdd_Success() {
        // Arrange
        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        // Act
        String result = productController.add(model);

        // Assert
        assertEquals("admin/product/add", result);
        verify(categoriesService, times(1)).getAll();
        verify(model, times(1)).addAttribute(eq("product"), any(Products.class));
        verify(model, times(1)).addAttribute("categories", mockCategoryList);

        // Analysis: Return = "admin/product/add", Exception = None, LogMessage = None, Result = PASS
    }

    // Test Case 3: showProducts() method
    @Test
    void testShowProducts_Success() {
        // Arrange
        when(productService.getAll()).thenReturn(mockProductList);

        // Act
        String result = productController.showProducts(model);

        // Assert
        assertEquals("admin/product/product", result);
        verify(productService, times(1)).getAll();
        verify(model, times(1)).addAttribute("product", mockProductList);

        // Analysis: Return = "admin/product/product", Exception = None, LogMessage = None, Result = PASS
    }

    // Test Case 4: addProduct() method - Valid Product
    @Test
    void testAddProduct_ValidProduct_Success() {
        // Arrange
        when(mockProduct.getBrand()).thenReturn("Samsung");
        when(mockProduct.getName()).thenReturn("Galaxy Laptop");
        when(mockProduct.getPrice()).thenReturn(BigInteger.valueOf(15000000));
        when(mockProduct.getQuantity()).thenReturn(10);
        when(productService.existsByName("Galaxy Laptop")).thenReturn(false);
        when(productService.create(mockProduct)).thenReturn(true);
        doNothing().when(storageService).store(any());

        // Act
        String result = productController.addProduct(mockProduct, mockFile, model);

        // Assert
        assertEquals("redirect:/admin/product", result);
        verify(productService, times(1)).existsByName("Galaxy Laptop");
        verify(productService, times(1)).create(mockProduct);
        verify(storageService, times(1)).store(mockFile);

        // Analysis: Return = "redirect:/admin/product", Exception = None, LogMessage = None, Result = PASS
    }

    @Test
    void testAddProduct_InvalidBrand_ContainsNumbers() {
        // Arrange
        when(mockProduct.getBrand()).thenReturn("Samsung123");  // Invalid brand
        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        // Act
        String result = productController.addProduct(mockProduct, mockFile, model);

        // Assert
        assertEquals("admin/product/add", result);
        verify(model, times(1)).addAttribute("error", "Thông tin sản phẩm không hợp lệ.");
        verify(categoriesService, times(1)).getAll();
        verify(productService, never()).create(any());

        // Analysis: Return = "admin/product/add", Exception = None, LogMessage = "Thông tin sản phẩm không hợp lệ.", Result = PASS
    }

    @Test
    void testAddProduct_InvalidPrice_Zero() {
        // Arrange
        when(mockProduct.getBrand()).thenReturn("Samsung");  // Valid brand for null check
        when(mockProduct.getPrice()).thenReturn(BigInteger.ZERO);  // Invalid price
        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        // Act
        String result = productController.addProduct(mockProduct, mockFile, model);

        // Assert
        assertEquals("admin/product/add", result);
        verify(model, times(1)).addAttribute("error", "Thông tin sản phẩm không hợp lệ.");
        verify(productService, never()).create(any());

        // Analysis: Return = "admin/product/add", Exception = None, LogMessage = "Thông tin sản phẩm không hợp lệ.", Result = PASS
    }

    @Test
    void testAddProduct_InvalidQuantity_Zero() {
        // Arrange
        when(mockProduct.getBrand()).thenReturn("Samsung");  // Valid brand for null check
        when(mockProduct.getPrice()).thenReturn(BigInteger.valueOf(15000000));  // Valid price for null check
        when(mockProduct.getQuantity()).thenReturn(0);  // Invalid quantity
        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        // Act
        String result = productController.addProduct(mockProduct, mockFile, model);

        // Assert
        assertEquals("admin/product/add", result);
        verify(model, times(1)).addAttribute("error", "Thông tin sản phẩm không hợp lệ.");
        verify(productService, never()).create(any());

        // Analysis: Return = "admin/product/add", Exception = None, LogMessage = "Thông tin sản phẩm không hợp lệ.", Result = PASS
    }

    @Test
    void testAddProduct_ProductAlreadyExists() {
        // Arrange
        when(mockProduct.getBrand()).thenReturn("Samsung");
        when(mockProduct.getName()).thenReturn("Galaxy Laptop");
        when(mockProduct.getPrice()).thenReturn(BigInteger.valueOf(15000000));
        when(mockProduct.getQuantity()).thenReturn(10);
        when(productService.existsByName("Galaxy Laptop")).thenReturn(true);  // Product exists
        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        // Act
        String result = productController.addProduct(mockProduct, mockFile, model);

        // Assert
        assertEquals("admin/product/add", result);
        verify(model, times(1)).addAttribute("error", "Sản phẩm đã tồn tại.");
        verify(productService, never()).create(any());

        // Analysis: Return = "admin/product/add", Exception = None, LogMessage = "Sản phẩm đã tồn tại.", Result = PASS
    }

    @Test
    void testAddProduct_CreateProductFailed() {
        // Arrange
        when(mockProduct.getBrand()).thenReturn("Samsung");
        when(mockProduct.getName()).thenReturn("Galaxy Laptop");
        when(mockProduct.getPrice()).thenReturn(BigInteger.valueOf(15000000));
        when(mockProduct.getQuantity()).thenReturn(10);
        when(productService.existsByName("Galaxy Laptop")).thenReturn(false);
        when(productService.create(mockProduct)).thenReturn(false);  // Create fails
        doNothing().when(storageService).store(any());

        // Act
        String result = productController.addProduct(mockProduct, mockFile, model);

        // Assert
        assertEquals("admin/product/add", result);
        verify(productService, times(1)).create(mockProduct);
        verify(storageService, times(1)).store(mockFile);

        // Analysis: Return = "admin/product/add", Exception = None, LogMessage = None, Result = PASS
    }

    // Test Case 5: editProduct() method
    @Test
    void testEditProduct_Success() {
        // Arrange
        String productID = "prod-123";
        when(productService.findById(productID)).thenReturn(mockProduct);
        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        // Act
        String result = productController.editProduct(model, productID);

        // Assert
        assertEquals("admin/product/edit", result);
        verify(productService, times(1)).findById(productID);
        verify(categoriesService, times(1)).getAll();
        verify(model, times(1)).addAttribute("categories", mockCategoryList);
        verify(model, times(1)).addAttribute("product", mockProduct);

        // Analysis: Return = "admin/product/edit", Exception = None, LogMessage = None, Result = PASS
    }

    @Test
    void testEditProduct_ProductNotFound() {
        // Arrange
        String productID = "non-existent-id";
        when(productService.findById(productID)).thenReturn(null);  // Product not found
        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        // Act
        String result = productController.editProduct(model, productID);

        // Assert
        assertEquals("admin/product/edit", result);
        verify(productService, times(1)).findById(productID);
        verify(model, times(1)).addAttribute("product", null);

        // Analysis: Return = "admin/product/edit", Exception = None, LogMessage = None, Result = PASS
    }

    // Test Case 6: updateProduct() method
    @Test
    void testUpdateProduct_Success() {
        // Arrange
        Products existingProduct = mock(Products.class);
        when(mockProduct.getProductID()).thenReturn("prod-123");
        when(mockProduct.getBrand()).thenReturn("Samsung");
        when(mockProduct.getName()).thenReturn("Updated Galaxy Laptop");
        when(mockProduct.getPrice()).thenReturn(BigInteger.valueOf(15000000));
        when(mockProduct.getQuantity()).thenReturn(10);

        when(productService.findById("prod-123")).thenReturn(existingProduct);
        when(existingProduct.getName()).thenReturn("Old Galaxy Laptop");
        when(productService.existsByName("Updated Galaxy Laptop")).thenReturn(false);
        when(productService.create(mockProduct)).thenReturn(true);
        doNothing().when(storageService).store(any());

        // Act
        String result = productController.updateProduct(mockProduct, mockFile, model);

        // Assert
        assertEquals("redirect:/admin/product", result);
        verify(productService, times(1)).findById("prod-123");
        verify(productService, times(1)).create(mockProduct);

        // Analysis: Return = "redirect:/admin/product", Exception = None, LogMessage = None, Result = PASS
    }

    @Test
    void testUpdateProduct_InvalidData() {
        // Arrange
        Products existingProduct = mock(Products.class);
        when(mockProduct.getProductID()).thenReturn("prod-123");
        when(mockProduct.getBrand()).thenReturn("Samsung123");  // Invalid brand

        when(productService.findById("prod-123")).thenReturn(existingProduct);
        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        // Act
        String result = productController.updateProduct(mockProduct, mockFile, model);

        // Assert
        assertEquals("admin/product/edit", result);
        verify(model, times(1)).addAttribute("error", "Thông tin sản phẩm không hợp lệ.");
        verify(productService, never()).create(any());

        // Analysis: Return = "admin/product/edit", Exception = None, LogMessage = "Thông tin sản phẩm không hợp lệ.", Result = PASS
    }

    // Test Case 7: deleteProduct() method
    @Test
    void testDeleteProduct_Success() {
        // Arrange
        String productID = "prod-123";
        when(productService.findById(productID)).thenReturn(mockProduct);
        when(mockProduct.getImageURL()).thenReturn("image.jpg");
        when(productService.delete(productID)).thenReturn(true);
        doNothing().when(storageService).delete("image.jpg");

        // Act
        String result = productController.deleteProduct(productID);

        // Assert
        assertEquals("redirect:/admin/product", result);
        verify(productService, times(1)).findById(productID);
        verify(productService, times(1)).delete(productID);
        verify(storageService, times(1)).delete("image.jpg");

        // Analysis: Return = "redirect:/admin/product", Exception = None, LogMessage = None, Result = PASS
    }

    @Test
    void testDeleteProduct_ProductNotFound() {
        // Arrange
        String productID = "non-existent-id";
        when(productService.findById(productID)).thenReturn(null);  // Product not found
        when(productService.delete(productID)).thenReturn(true);

        // Act
        String result = productController.deleteProduct(productID);

        // Assert
        assertEquals("redirect:/admin/product", result);
        verify(productService, times(1)).findById(productID);
        verify(storageService, never()).delete(any());

        // Analysis: Return = "redirect:/admin/product", Exception = None, LogMessage = None, Result = PASS
    }

    @Test
    void testDeleteProduct_DeleteFailed() {
        // Arrange
        String productID = "prod-123";
        when(productService.findById(productID)).thenReturn(mockProduct);
        when(mockProduct.getImageURL()).thenReturn("image.jpg");
        when(productService.delete(productID)).thenReturn(false);  // Delete fails
        doNothing().when(storageService).delete("image.jpg");

        // Act
        String result = productController.deleteProduct(productID);

        // Assert
        assertEquals("admin/product/product", result);
        verify(productService, times(1)).delete(productID);
        verify(storageService, times(1)).delete("image.jpg");

        // Analysis: Return = "admin/product/product", Exception = None, LogMessage = None, Result = PASS
    }

    @Test
    void testDeleteProduct_NoImageURL() {
        // Arrange
        String productID = "prod-123";
        when(productService.findById(productID)).thenReturn(mockProduct);
        when(mockProduct.getImageURL()).thenReturn(null);  // No image URL
        when(productService.delete(productID)).thenReturn(true);

        // Act
        String result = productController.deleteProduct(productID);

        // Assert
        assertEquals("redirect:/admin/product", result);
        verify(productService, times(1)).delete(productID);
        verify(storageService, never()).delete(any());

        // Analysis: Return = "redirect:/admin/product", Exception = None, LogMessage = None, Result = PASS
    }
}