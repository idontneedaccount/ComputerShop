package com.example.computershop.service.image;


import com.example.computershop.dto.ImageDto;
import com.example.computershop.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImageService {
    Image getImageById(Long id);
    void deleteImageById(Long id);
    List<ImageDto> saveImages(List<MultipartFile> file, Long productId);
    void  updateImage(MultipartFile file, Long productId);
}
