
package com.sanjay.Spring_ECOM.service;

import com.sanjay.Spring_ECOM.model.Product;
import com.sanjay.Spring_ECOM.repo.ProductRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/** Catalog persistence operations shared by the product HTTP endpoints. */
@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    // Get all products
    /** Returns persisted catalog entries; an empty database produces an empty list. */
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    // Get product by ID
    /** Returns the matching product, or null so the controller can send HTTP 404. */
    public Product getProductById(int id) {
        return productRepo.findById((long) id).orElse(null);
    }

    // Add a new product or save a product with an image
    /** Copies uploaded image bytes and MIME metadata into the entity before saving. */
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
    /** Saves an entity whose image has already been supplied or preserved by the controller. */
    public Product addOrUpdateProductWithoutImage(Product product) {
        return productRepo.save(product);
    }

    // Delete product
    /** Deletes the product by its database ID; callers check existence first. */
    public void deleteProduct(int id) {
        productRepo.deleteById((long) id);
    }

    // Search products
    /** Searches name, description, brand, and category without case sensitivity. */
    public List<Product> searchProducts(String keyword) {
        return productRepo.searchProducts(keyword);
    }
}
