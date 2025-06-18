package com.example.computershop.controller;

import com.example.computershop.entity.ProductSpecifications;
import com.example.computershop.entity.Products;
import com.example.computershop.service.ProductSpecificationService;
import com.example.computershop.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/admin")
public class ProductSpecificationController {
    private ProductSpecificationService specificationService;
    private ProductService productService;


    @GetMapping("/product/{productId}/specification")
    public String editSpecification(@PathVariable String productId, Model model) {
        Products product = productService.findById(productId);
        ProductSpecifications specification = specificationService.findByProductId(productId);
        if (specification == null) {
            specification = new ProductSpecifications();
            specification.setProduct(product);
        }
        model.addAttribute("product", product);
        model.addAttribute("specification", specification);
        return "admin/product/specification";
    }

    @PostMapping("/product/{productId}/specification")
    public String saveSpecification(@PathVariable String productId,
                                  @ModelAttribute ProductSpecifications specification,
                                  RedirectAttributes redirectAttributes) {
        try {
            Products product = productService.findById(productId);
            specification.setProduct(product);
            specificationService.save(specification);
            redirectAttributes.addFlashAttribute("success", "Cấu hình sản phẩm đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật cấu hình sản phẩm!");
        }
        return "redirect:/admin/product";
    }
}
