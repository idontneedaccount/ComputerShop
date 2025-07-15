

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
    @Test
void testAddProduct_InvalidPrice_Zero() {
    Products realProduct = new Products();
    realProduct.setBrand("Samsung");  // Valid brand for null check
    realProduct.setName("Galaxy Laptop");
    realProduct.setPrice(BigInteger.ZERO);  // Invalid price
    realProduct.setQuantity(10);

    when(categoriesService.getAll()).thenReturn(mockCategoryList);

    String result = productController.addProduct(realProduct, mockFile, model);

    assertEquals("admin/product/add", result);
    verify(model, times(1)).addAttribute("error", "Thông tin sản phẩm không hợp lệ.");
    verify(productService, never()).create(any());

    // Analysis: Return = "admin/product/add", Exception = None, LogMessage = "Thông tin sản phẩm không hợp lệ.", Result = PASS
}
    @Test
    void testAddProduct_ProductAlreadyExists() {
        Products realProduct = new Products();
        realProduct.setBrand("Samsung");
        realProduct.setName("Galaxy Laptop");
        realProduct.setPrice(BigInteger.valueOf(15000000));
        realProduct.setQuantity(10);

        when(productService.existsByName("Galaxy Laptop")).thenReturn(true);  // Product exists
        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        String result = productController.addProduct(realProduct, mockFile, model);

        assertEquals("admin/product/add", result);
        verify(model, times(1)).addAttribute("error", "Sản phẩm đã tồn tại.");
        verify(productService, never()).create(any());

        // Analysis: Return = "admin/product/add", Exception = None, LogMessage = "Sản phẩm đã tồn tại.", Result = PASS
    }
    // Test Case 4: addProduct() method - Valid Product
    @Test
    void testAddProduct_ValidProduct_Success() {
        Products realProduct = new Products();
        realProduct.setBrand("Samsung");
        realProduct.setName("Galaxy Laptop");
        realProduct.setPrice(BigInteger.valueOf(15000000));
        realProduct.setQuantity(10);

        when(productService.existsByName("Galaxy Laptop")).thenReturn(false);
        when(productService.create(realProduct)).thenReturn(true);
        when(storageService.store(any())).thenReturn("test.jpg");

        String result = productController.addProduct(realProduct, mockFile, model);

        assertEquals("redirect:/admin/product", result);
        verify(productService, times(1)).existsByName("Galaxy Laptop");
        verify(productService, times(1)).create(realProduct);
        verify(storageService, times(1)).store(mockFile);

        // Analysis: Return = "redirect:/admin/product", Exception = None, LogMessage = None, Result = PASS
    }
    @Test
    void testAddProduct_CreateProductFailed() {
        Products realProduct = new Products();
        realProduct.setBrand("Samsung");
        realProduct.setName("Galaxy Laptop");
        realProduct.setPrice(BigInteger.valueOf(15000000));
        realProduct.setQuantity(10);

        when(productService.existsByName("Galaxy Laptop")).thenReturn(false);
        when(productService.create(realProduct)).thenReturn(false);  // Create fails
        when(storageService.store(any())).thenReturn("test.jpg");

        String result = productController.addProduct(realProduct, mockFile, model);

        assertEquals("admin/product/add", result);
        verify(productService, times(1)).create(realProduct);
        verify(storageService, times(1)).store(mockFile);

        // Analysis: Return = "admin/product/add", Exception = None, LogMessage = None, Result = PASS
    }

    // Test Case 1: index() method
    @Test
    void testIndex_Success() {
        when(productService.getAll()).thenReturn(mockProductList);
        String result = productController.index(model);

        assertEquals("admin/product/product", result);
        verify(productService, times(1)).getAll();
        verify(model, times(1)).addAttribute("product", mockProductList);

    }

    @Test
    void testIndex_EmptyList() {
        when(productService.getAll()).thenReturn(Arrays.asList());

        String result = productController.index(model);

        assertEquals("admin/product/product", result);
        verify(productService, times(1)).getAll();
        verify(model, times(1)).addAttribute("product", Arrays.asList());

        // Analysis: Return = "admin/product/product", Exception = None, LogMessage = None, Result = PASS
    }

    // Test Case 2: add() method
    @Test
    void testAdd_Success() {
        when(categoriesService.getAll()).thenReturn(mockCategoryList);
        String result = productController.add(model);
        assertEquals("admin/product/add", result);
        verify(categoriesService, times(1)).getAll();
        verify(model, times(1)).addAttribute(eq("product"), any(Products.class));
        verify(model, times(1)).addAttribute("categories", mockCategoryList);

    }

    // Test Case 3: showProducts() method
    @Test
    void testShowProducts_Success() {
        when(productService.getAll()).thenReturn(mockProductList);
        String result = productController.showProducts(model);
        assertEquals("admin/product/product", result);
        verify(productService, times(1)).getAll();
        verify(model, times(1)).addAttribute("product", mockProductList);

        // Analysis: Return = "admin/product/product", Exception = None, LogMessage = None, Result = PASS
    }



    @Test
    void testAddProduct_InvalidBrand_ContainsNumbers() {
        Products realProduct = new Products();
        realProduct.setBrand("Samsung123");  // Invalid brand
        realProduct.setName("Galaxy Laptop");
        realProduct.setPrice(BigInteger.valueOf(15000000));
        realProduct.setQuantity(10);

        when(categoriesService.getAll()).thenReturn(mockCategoryList);
        String result = productController.addProduct(realProduct, mockFile, model);
        assertEquals("admin/product/add", result);
        verify(model, times(1)).addAttribute("error", "Thông tin sản phẩm không hợp lệ.");
        verify(categoriesService, times(1)).getAll();
        verify(productService, never()).create(any());
    }

    @Test
    void testAddProduct_InvalidQuantity_Negative() {
        Products realProduct = new Products();
        realProduct.setBrand("Samsung");  // Valid brand
        realProduct.setName("Galaxy Laptop");
        realProduct.setPrice(BigInteger.valueOf(15000000));  // Valid price
        realProduct.setQuantity(-1);  // Invalid quantity (negative)

        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        String result = productController.addProduct(realProduct, mockFile, model);

        assertEquals("admin/product/add", result);
        verify(model, times(1)).addAttribute("error", "Thông tin sản phẩm không hợp lệ.");
        verify(productService, never()).create(any());

    }

    // Test Case 5: editProduct() method
    @Test
    void testEditProduct_Success() {
        String productID = "prod-123";
        when(productService.findById(productID)).thenReturn(mockProduct);
        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        String result = productController.editProduct(model, productID);

        assertEquals("admin/product/edit", result);
        verify(productService, times(1)).findById(productID);
        verify(categoriesService, times(1)).getAll();
        verify(model, times(1)).addAttribute("categories", mockCategoryList);
        verify(model, times(1)).addAttribute("product", mockProduct);

        // Analysis: Return = "admin/product/edit", Exception = None, LogMessage = None, Result = PASS
    }

    @Test
    void testEditProduct_ProductNotFound() {
        String productID = "non-existent-id";
        when(productService.findById(productID)).thenReturn(null);  // Product not found
        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        String result = productController.editProduct(model, productID);

        assertEquals("admin/product/edit", result);
        verify(productService, times(1)).findById(productID);
        verify(model, times(1)).addAttribute("product", null);

        // Analysis: Return = "admin/product/edit", Exception = None, LogMessage = None, Result = PASS
    }

    // Test Case 6: updateProduct() method
    @Test
    void testUpdateProduct_Success() {
        Products existingProduct = new Products();
        existingProduct.setProductID("prod-123");
        existingProduct.setName("Old Galaxy Laptop");
        existingProduct.setImageURL("old-image.jpg");

        Products updateProduct = new Products();
        updateProduct.setProductID("prod-123");
        updateProduct.setBrand("Samsung");
        updateProduct.setName("Updated Galaxy Laptop");
        updateProduct.setPrice(BigInteger.valueOf(15000000));
        updateProduct.setQuantity(10);

        when(productService.findById("prod-123")).thenReturn(existingProduct);
        when(productService.existsByName("Updated Galaxy Laptop")).thenReturn(false);
        when(productService.create(updateProduct)).thenReturn(true);
        when(storageService.store(any())).thenReturn("test.jpg");

        String result = productController.updateProduct(updateProduct, mockFile, model);

        assertEquals("redirect:/admin/product", result);
        verify(productService, times(1)).findById("prod-123");
        verify(productService, times(1)).create(updateProduct);

        // Analysis: Return = "redirect:/admin/product", Exception = None, LogMessage = None, Result = PASS
    }

    @Test
    void testUpdateProduct_InvalidData() {
        // Arrange
        Products existingProduct = new Products();
        existingProduct.setProductID("prod-123");
        existingProduct.setName("Old Galaxy Laptop");
        existingProduct.setImageURL("old-image.jpg");

        Products updateProduct = new Products();
        updateProduct.setProductID("prod-123");
        updateProduct.setBrand("Samsung123");  // Invalid brand
        updateProduct.setName("Galaxy Laptop");
        updateProduct.setPrice(BigInteger.valueOf(15000000));
        updateProduct.setQuantity(10);

        when(productService.findById("prod-123")).thenReturn(existingProduct);
        when(categoriesService.getAll()).thenReturn(mockCategoryList);

        String result = productController.updateProduct(updateProduct, mockFile, model);

        assertEquals("admin/product/edit", result);
        verify(model, times(1)).addAttribute("error", "Thông tin sản phẩm không hợp lệ.");
        verify(productService, never()).create(any());

    }
}

