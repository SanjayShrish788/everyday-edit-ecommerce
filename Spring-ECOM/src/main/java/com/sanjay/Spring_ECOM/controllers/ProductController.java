
package com.sanjay.Spring_ECOM.controllers;

import com.sanjay.Spring_ECOM.model.Product;
import com.sanjay.Spring_ECOM.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;

    // Get all products
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // Get product by ID
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable int id) {

        Product product = productService.getProductById(id);

        if (product != null) {
            return ResponseEntity.ok(product);
        }

        return ResponseEntity.notFound().build();
    }

    // Add product
    @PostMapping("/product")
    public ResponseEntity<?> addProduct(
            @RequestPart("product") Product product,
            @RequestPart("imageFile") MultipartFile imageFile) {

        try {
            Product savedProduct =
                    productService.addOrUpdateProduct(product, imageFile);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savedProduct);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing product image: " + e.getMessage());
        }
    }

    // Update product
    @PutMapping("/product/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable int id,
            @RequestPart("product") Product product,
            @RequestPart(value = "imageFile", required = false)
            MultipartFile imageFile) {

        try {
            Product existingProduct = productService.getProductById(id);

            if (existingProduct == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Product not found");
            }

            // Always update the existing product ID
            product.setId((long) id);

            if (imageFile != null && !imageFile.isEmpty()) {
                product.setImageName(imageFile.getOriginalFilename());
                product.setImageType(imageFile.getContentType());
                product.setImageData(imageFile.getBytes());
            } else {
                // Preserve existing image
                product.setImageName(existingProduct.getImageName());
                product.setImageType(existingProduct.getImageType());
                product.setImageData(existingProduct.getImageData());
            }

            Product updatedProduct =
                    productService.addOrUpdateProductWithoutImage(product);

            return ResponseEntity.ok(updatedProduct);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing image: " + e.getMessage());
        }
    }

    // Get product image
    @GetMapping("/product/{productId}/image")
    public ResponseEntity<byte[]> getImageByProductId(
            @PathVariable int productId) {

        Product product = productService.getProductById(productId);

        if (product != null && product.getImageData() != null) {
            return ResponseEntity.ok()
                    .header(
                            "Content-Type",
                            product.getImageType() != null
                                    ? product.getImageType()
                                    : "application/octet-stream"
                    )
                    .body(product.getImageData());
        }

        return ResponseEntity.notFound().build();
    }

    // Delete product
    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable int id) {

        Product product = productService.getProductById(id);

        if (product != null) {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Deleted");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Product not found");
    }

    // Search products
    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam String keyword) {

        List<Product> products =
                productService.searchProducts(keyword);

        System.out.println("Searching with: " + keyword);

        return ResponseEntity.ok(products);
    }
}
