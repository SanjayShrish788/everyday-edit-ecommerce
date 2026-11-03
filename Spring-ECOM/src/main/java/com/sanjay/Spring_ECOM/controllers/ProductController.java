
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

/** Multipart catalog API; metadata and image uploads are separate request parts. */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;

    /** Lists the catalog, including availability and stock quantities. */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /** Loads a product for its detail or edit screen; unknown IDs return HTTP 404. */
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable int id) {

        Product product = productService.getProductById(id);

        if (product != null) {
            return ResponseEntity.ok(product);
        }

        return ResponseEntity.notFound().build();
    }

    /** Accepts a JSON product part and an imageFile part, returning HTTP 201. */
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

    /** Replaces product metadata and optionally its image, keeping the path ID authoritative. */
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

            // Never let a mismatched ID in the JSON body update a different row.
            product.setId((long) id);

            if (imageFile != null && !imageFile.isEmpty()) {
                product.setImageName(imageFile.getOriginalFilename());
                product.setImageType(imageFile.getContentType());
                product.setImageData(imageFile.getBytes());
            } else {
                // A metadata-only edit must not erase the stored image bytes or MIME type.
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

    /** Streams stored image bytes with their MIME type; absent images return HTTP 404. */
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

    /** Removes an existing product, or reports HTTP 404 when it is already absent. */
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

    /** Returns case-insensitive matches across the searchable catalog fields. */
    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam String keyword) {

        List<Product> products =
                productService.searchProducts(keyword);

        System.out.println("Searching with: " + keyword);

        return ResponseEntity.ok(products);
    }
}
