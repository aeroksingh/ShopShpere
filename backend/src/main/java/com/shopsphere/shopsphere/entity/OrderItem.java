package com.shopsphere.shopsphere.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    // IMPORTANT: snapshot the price at time of purchase.
    // Never rely on product.getPrice() later — prices change, order history shouldn't.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

	public void setOrder(Order order) {
		this.order = order;
		
		
	}
}
