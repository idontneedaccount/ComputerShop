package com.example.computershop.controller;

import com.example.computershop.entity.Products;
import com.example.computershop.entity.ProductVariant;
import com.example.computershop.service.ProductService;
import com.example.computershop.service.ProductVariantService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin/product-variants")
@AllArgsConstructor
public class ProductVariantController {
    
    private final ProductService productService;
    private final ProductVariantService productVariantService;

    private static final String SUCCCESS = "success";
    private static final String ERROR = "error";
    private static final String LOI = "Lỗi: ";
    private static final String PRODUCT_VARIANTS = "redirect:/admin/product-variants/";
    private static final String PRODUCT = "redirect:/admin/product";
    
    @GetMapping("/{productId}")
    public String listVariants(@PathVariable String productId, Model model) {
        Products product = productService.findById(productId);
        if (product == null) {
            return PRODUCT;
        }
        
        List<ProductVariant> variants = productVariantService.getVariantsByProduct(productId);
        
        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("newVariant", new ProductVariant());
        
        return "admin/product/variants";
    }
    
    @PostMapping("/add/{productId}")
    public String addVariant(@PathVariable String productId,
                            @ModelAttribute ProductVariant variant,
                            RedirectAttributes redirectAttributes) {
        try {
            Products product = productService.findById(productId);
            if (product == null) {
                redirectAttributes.addFlashAttribute(ERROR, "Sản phẩm không tồn tại!");
                return PRODUCT;
            }
            
            // Check if variant with same specs already exists
            if (productVariantService.existsBySpecs(productId, variant.getCpu(), 
                    variant.getRam(), variant.getStorage())) {
                redirectAttributes.addFlashAttribute(ERROR, "Cấu hình này đã tồn tại!");
                return PRODUCT_VARIANTS + productId;
            }
            
            variant.setProduct(product);
            variant.setIsActive(true);
            
            // Generate display name if not provided
            if (variant.getVariantName() == null || variant.getVariantName().isEmpty()) {
                variant.setVariantName(variant.getDisplayName());
            }
            
            productVariantService.create(variant);
            redirectAttributes.addFlashAttribute(SUCCCESS, "Thêm cấu hình thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, LOI + e.getMessage());
        }
        
        return PRODUCT_VARIANTS + productId;
    }
    
    @GetMapping("/edit/{variantId}")
    public String editVariant(@PathVariable String variantId, Model model) {
        ProductVariant variant = productVariantService.findById(variantId);
        if (variant == null) {
            return PRODUCT;
        }
        
        model.addAttribute("variant", variant);
        model.addAttribute("product", variant.getProduct());
        
        return "admin/product/edit-variant";
    }
    
    @PostMapping("/update/{variantId}")
    public String updateVariant(@PathVariable String variantId,
                               @ModelAttribute ProductVariant updatedVariant,
                               RedirectAttributes redirectAttributes) {
        try {
            ProductVariant existing = productVariantService.findById(variantId);
            if (existing == null) {
                redirectAttributes.addFlashAttribute(ERROR, "Cấu hình không tồn tại!");
                return PRODUCT;
            }
            
            ProductVariant updated = productVariantService.update(variantId, updatedVariant);
            if (updated != null) {
                redirectAttributes.addFlashAttribute(SUCCCESS, "Cập nhật cấu hình thành công!");
                return PRODUCT_VARIANTS + existing.getProduct().getProductID();
            } else {
                redirectAttributes.addFlashAttribute(ERROR, "Không thể cập nhật cấu hình!");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, LOI + e.getMessage());
        }
        
        return PRODUCT;
    }
    
    @GetMapping("/delete/{variantId}")
    public String deleteVariant(@PathVariable String variantId,
                               RedirectAttributes redirectAttributes) {
        try {
            ProductVariant variant = productVariantService.findById(variantId);
            if (variant == null) {
                redirectAttributes.addFlashAttribute(ERROR, "Cấu hình không tồn tại!");
                return PRODUCT;
            }
            
            String productId = variant.getProduct().getProductID();
            
            if (productVariantService.delete(variantId)) {
                redirectAttributes.addFlashAttribute(SUCCCESS, "Xóa cấu hình thành công!");
            } else {
                redirectAttributes.addFlashAttribute(ERROR, "Không thể xóa cấu hình!");
            }
            
            return PRODUCT_VARIANTS + productId;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, LOI + e.getMessage());
            return PRODUCT;
        }
    }
    
    @PostMapping("/toggle-status/{variantId}")
    @ResponseBody
    public String toggleStatus(@PathVariable String variantId) {
        try {
            ProductVariant variant = productVariantService.findById(variantId);
            if (variant != null) {
                variant.setIsActive(!variant.getIsActive());
                productVariantService.save(variant);
                return SUCCCESS;
            }
            return ERROR;
        } catch (Exception e) {
            return ERROR;
        }
    }
} 