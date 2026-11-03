
package com.sanjay.Spring_ECOM.service;

import com.sanjay.Spring_ECOM.model.Product;
import com.sanjay.Spring_ECOM.repo.ProductRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    // Get all products
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    // Get product by ID
    public Product getProductById(int id) {
        return productRepo.findById((long) id).orElse(null);
    }

    // Add a new product or save a product with an image
    public Product addOrUpdateProduct(
            Product product,
            MultipartFile imageFile) throws IOException {

        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageName(imageFile.getOriginalFilename());
            product.setImageType(imageFile.getContentType());
            product.setImageData(imageFile.getBytes());
        }

        return productRepo.save(product);
    }

    // Update product while preserving its existing image
    public Product addOrUpdateProductWithoutImage(Product product) {
        return productRepo.save(product);
    }

    // Delete product
    public void deleteProduct(int id) {
        productRepo.deleteById((long) id);
    }

    // Search products
    public List<Product> searchProducts(String keyword) {
        return productRepo.searchProducts(keyword);
    }
}
