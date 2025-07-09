package com.example.computershop.controller;

import com.example.computershop.entity.Products;
import com.example.computershop.entity.ProductVariant;
import com.example.computershop.entity.VariantFieldConfig;
import com.example.computershop.service.ProductService;
import com.example.computershop.service.ProductVariantService;
import com.example.computershop.service.VariantFieldConfigService;
import com.example.computershop.service.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin/product-variants")
@AllArgsConstructor
public class ProductVariantController {
    
    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final VariantFieldConfigService fieldConfigService;
    private final StorageService storageService;

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
        List<VariantFieldConfig> customFields = fieldConfigService.getAllActiveFields();
        
        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("newVariant", new ProductVariant());
        model.addAttribute("customFields", customFields);
        
        return "admin/product/variants";
    }
    
    @PostMapping("/add/{productId}")
    public String addVariant(@PathVariable String productId,
                            @ModelAttribute ProductVariant variant,
                            @RequestParam Map<String, String> allParams,
                            @RequestParam(required = false) MultipartFile variantImage,
                            @RequestParam(required = false) MultipartFile[] variantImages,
                            RedirectAttributes redirectAttributes) {
        try {
            Products product = productService.findById(productId);
            if (product == null) {
                redirectAttributes.addFlashAttribute(ERROR, "Sản phẩm không tồn tại!");
                return PRODUCT;
            }
            
            // XỬ LÝ UPLOAD ẢNH CHÍNH
            if (variantImage != null && !variantImage.isEmpty()) {
                try {
                    String fileName = storageService.store(variantImage);
                    variant.setVariantImageUrl(fileName);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute(ERROR, "Lỗi upload ảnh chính: " + e.getMessage());
                    return PRODUCT_VARIANTS + productId;
                }
            }
            
            // XỬ LÝ UPLOAD NHIỀU ẢNH
            if (variantImages != null && variantImages.length > 0) {
                List<String> imageNames = new ArrayList<>();
                for (MultipartFile image : variantImages) {
                    if (!image.isEmpty()) {
                        try {
                            String fileName = storageService.store(image);
                            imageNames.add(fileName);
                        } catch (Exception e) {
                            redirectAttributes.addFlashAttribute(ERROR, "Lỗi upload ảnh: " + e.getMessage());
                            return PRODUCT_VARIANTS + productId;
                        }
                    }
                }
                if (!imageNames.isEmpty()) {
                    variant.setVariantImagesArray(imageNames.toArray(new String[0]));
                }
            }
            
            // Xử lý custom attributes
            Map<String, String> customAttributes = new HashMap<>();
            List<VariantFieldConfig> customFields = fieldConfigService.getAllActiveFields();
            
            for (VariantFieldConfig field : customFields) {
                String value = allParams.get("custom_" + field.getFieldKey());
                if (value != null && !value.trim().isEmpty()) {
                    customAttributes.put(field.getFieldKey(), value.trim());
                } else if (field.getIsRequired()) {
                    redirectAttributes.addFlashAttribute(ERROR, "Trường " + field.getFieldName() + " là bắt buộc!");
                    return PRODUCT_VARIANTS + productId;
                }
            }
            
            variant.setCustomAttributesMap(customAttributes);
            
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
        
        List<VariantFieldConfig> customFields = fieldConfigService.getAllActiveFields();
        
        model.addAttribute("variant", variant);
        model.addAttribute("product", variant.getProduct());
        model.addAttribute("customFields", customFields);
        
        return "admin/product/edit-variant";
    }
    
    @PostMapping("/update/{variantId}")
    public String updateVariant(@PathVariable String variantId,
                               @ModelAttribute ProductVariant updatedVariant,
                               @RequestParam Map<String, String> allParams,
                               @RequestParam(required = false) MultipartFile image,
                               @RequestParam(required = false) MultipartFile[] variantImages,
                               RedirectAttributes redirectAttributes) {
        try {
            ProductVariant existing = productVariantService.findById(variantId);
            if (existing == null) {
                redirectAttributes.addFlashAttribute(ERROR, "Cấu hình không tồn tại!");
                return PRODUCT;
            }
            
            // XỬ LÝ UPLOAD ẢNH CHÍNH - CẢI THIỆN VỚI CLEANUP
            if (image != null && !image.isEmpty()) {
                try {
                    // Sử dụng storeAndCleanup để xóa ảnh cũ
                    String fileName = storageService.storeAndCleanup(image, existing.getVariantImageUrl());
                    updatedVariant.setVariantImageUrl(fileName);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute(ERROR, "Lỗi upload ảnh chính: " + e.getMessage());
                    return PRODUCT_VARIANTS + existing.getProduct().getProductID();
                }
            } else {
                // Giữ nguyên ảnh cũ nếu không upload ảnh mới
                updatedVariant.setVariantImageUrl(existing.getVariantImageUrl());
            }
            
            // Xử lý custom attributes
            Map<String, String> customAttributes = new HashMap<>();
            List<VariantFieldConfig> customFields = fieldConfigService.getAllActiveFields();
            
            for (VariantFieldConfig field : customFields) {
                String value = allParams.get("custom_" + field.getFieldKey());
                if (value != null && !value.trim().isEmpty()) {
                    customAttributes.put(field.getFieldKey(), value.trim());
                }
            }
            
            updatedVariant.setCustomAttributesMap(customAttributes);
            
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
    
    // ENDPOINTS QUẢN LÝ DYNAMIC FIELDS
    @GetMapping("/field-config")
    public String fieldConfig(Model model) {
        List<VariantFieldConfig> fields = fieldConfigService.getAllFields();
        model.addAttribute("fields", fields);
        model.addAttribute("newField", new VariantFieldConfig());
        return "admin/product/field-config";
    }

    @PostMapping("/field-config/add")
    public String addField(@ModelAttribute VariantFieldConfig fieldConfig,
                          RedirectAttributes redirectAttributes) {
        try {
            fieldConfigService.create(fieldConfig);
            redirectAttributes.addFlashAttribute(SUCCCESS, "Thêm trường thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, LOI + e.getMessage());
        }
        return "redirect:/admin/product-variants/field-config";
    }

    @GetMapping("/field-config/edit/{fieldId}")
    public String editField(@PathVariable String fieldId, Model model, RedirectAttributes redirectAttributes) {
        try {
            VariantFieldConfig field = fieldConfigService.findById(fieldId);
            if (field == null) {
                redirectAttributes.addFlashAttribute(ERROR, "Trường không tồn tại!");
                return "redirect:/admin/product-variants/field-config";
            }
            
            List<VariantFieldConfig> allFields = fieldConfigService.getAllFields();
            model.addAttribute("fields", allFields);
            model.addAttribute("newField", new VariantFieldConfig());
            model.addAttribute("editField", field);
            model.addAttribute("isEdit", true);
            
            return "admin/product/field-config";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, LOI + e.getMessage());
            return "redirect:/admin/product-variants/field-config";
        }
    }

    @PostMapping("/field-config/update/{fieldId}")
    public String updateField(@PathVariable String fieldId,
                             @ModelAttribute VariantFieldConfig fieldConfig,
                             RedirectAttributes redirectAttributes) {
        try {
            VariantFieldConfig updated = fieldConfigService.update(fieldId, fieldConfig);
            if (updated != null) {
                redirectAttributes.addFlashAttribute(SUCCCESS, "Cập nhật trường thành công!");
            } else {
                redirectAttributes.addFlashAttribute(ERROR, "Không thể cập nhật trường!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, LOI + e.getMessage());
        }
        return "redirect:/admin/product-variants/field-config";
    }

    // Toggle functionality removed as per user request

    @GetMapping("/field-config/delete/{fieldId}")
    public String deleteField(@PathVariable String fieldId,
                             RedirectAttributes redirectAttributes) {
        try {
            if (fieldConfigService.delete(fieldId)) {
                redirectAttributes.addFlashAttribute(SUCCCESS, "Xóa trường thành công!");
            } else {
                redirectAttributes.addFlashAttribute(ERROR, "Không thể xóa trường!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, LOI + e.getMessage());
        }
        return "redirect:/admin/product-variants/field-config";
    }
} 