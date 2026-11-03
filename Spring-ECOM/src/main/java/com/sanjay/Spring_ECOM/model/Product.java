package com.sanjay.Spring_ECOM.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data //will automatically create getters or setters
@NoArgsConstructor
@AllArgsConstructor
public class Product{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY) //Primary key to be generated automatically
    private Long id;
    private String name;
    private String description;
    private String brand;
    private BigDecimal price;
    private String category;
//    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd-MM-yyyy")  //for date
    private Date releaseDate;
    private boolean productAvailable;
    private int stockQuantity;
    private String imageName;
    private String imageType;
    @Lob //tells JPA to map the field as a large object (LOB)
    private byte[] imageData;
}
