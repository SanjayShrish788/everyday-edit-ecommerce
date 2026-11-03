import { API_BASE_URL } from "../axios";
import React, { useContext, useState, useEffect } from "react";
import AppContext from "../Context/Context";
import axios from "axios";
import CheckoutPopup from "./CheckoutPopup";
import { Button } from "react-bootstrap";

/** Displays API-backed bag images and the current inventory-update checkout. */

const Cart = () => {
  const { cart, removeFromCart, clearCart } = useContext(AppContext);
  const [cartItems, setCartItems] = useState([]);
  const [totalPrice, setTotalPrice] = useState(0);
  const [cartImage, setCartImage] = useState([]);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    /** Excludes deleted products and attaches browser image URLs to bag lines. */
    const fetchImagesAndUpdateCart = async () => {
      console.log("Cart", cart);
      try {
        const response = await axios.get(`${API_BASE_URL}/products`);
        const backendProductIds = response.data.map((product) => product.id);

        const updatedCartItems = cart.filter((item) =>
          backendProductIds.includes(item.id),
        );
        const cartItemsWithImages = await Promise.all(
          updatedCartItems.map(async (item) => {
            try {
              const response = await axios.get(
                `${API_BASE_URL}/product/${item.id}/image`,
                { responseType: "blob" },
              );
              const imageFile = await converUrlToFile(
                response.data,
                response.data.imageName,
              );
              setCartImage(imageFile);
              const imageUrl = URL.createObjectURL(response.data);
              return { ...item, imageUrl };
            } catch (error) {
              console.error("Error fetching image:", error);
              return { ...item, imageUrl: "placeholder-image-url" };
            }
          }),
        );
        console.log("cart", cart);
        setCartItems(cartItemsWithImages);
      } catch (error) {
        console.error("Error fetching product data:", error);
      }
    };

    if (cart.length) {
      fetchImagesAndUpdateCart();
    }
  }, [cart]);

  useEffect(() => {
    const total = cartItems.reduce(
      (acc, item) => acc + item.price * item.quantity,
      0,
    );
    setTotalPrice(total);
  }, [cartItems]);

  /** Wraps image bytes in a File for the existing multipart checkout flow. */

  const converUrlToFile = async (blobData, fileName) => {
    const file = new File([blobData], fileName, { type: blobData.type });
    return file;
  };

  /** Caps displayed quantity at the stock captured for this bag line. */

  const handleIncreaseQuantity = (itemId) => {
    const newCartItems = cartItems.map((item) => {
      if (item.id === itemId) {
        if (item.quantity < item.stockQuantity) {
          return { ...item, quantity: item.quantity + 1 };
        } else {
          alert("Cannot add more than available stock");
        }
      }
      return item;
    });
    setCartItems(newCartItems);
  };

  /** Keeps displayed quantity at a minimum of one. */

  const handleDecreaseQuantity = (itemId) => {
    const newCartItems = cartItems.map((item) =>
      item.id === itemId
        ? { ...item, quantity: Math.max(item.quantity - 1, 1) }
        : item,
    );
    setCartItems(newCartItems);
  };

  /** Removes the line from shared bag state and this screen. */

  const handleRemoveFromCart = (itemId) => {
    removeFromCart(itemId);
    const newCartItems = cartItems.filter((item) => item.id !== itemId);
    setCartItems(newCartItems);
  };

  /** Updates stock using separate product requests, then clears the bag. This legacy flow does not call /orders/place, collect payment, or create an atomic order transaction. */

  const handleCheckout = async () => {
    try {
      for (const item of cartItems) {
        const { imageUrl, imageName, imageData, imageType, quantity, ...rest } =
          item;
        const updatedStockQuantity = item.stockQuantity - item.quantity;

        const updatedProductData = {
          ...rest,
          stockQuantity: updatedStockQuantity,
        };
        console.log("updated product data", updatedProductData);

        const cartProduct = new FormData();
        cartProduct.append("imageFile", cartImage);
        cartProduct.append(
          "product",
          new Blob([JSON.stringify(updatedProductData)], {
            type: "application/json",
          }),
        );

        await axios
          .put(`${API_BASE_URL}/product/${item.id}`, cartProduct, {
            headers: {
              "Content-Type": "multipart/form-data",
            },
          })
          .then((response) => {
            console.log("Product updated successfully:", cartProduct);
          })
          .catch((error) => {
            console.error("Error updating product:", error);
          });
      }
      clearCart();
      setCartItems([]);
      setShowModal(false);
    } catch (error) {
      console.log("error during checkout", error);
    }
  };

  return (
    <div className="cart-container">
      <div className="shopping-cart">
        <div className="title">Shopping Bag</div>
        {cartItems.length === 0 ? (
          <div className="empty" style={{ textAlign: "left", padding: "2rem" }}>
            <h4>Your cart is empty</h4>
          </div>
        ) : (
          <>
            {cartItems.map((item) => (
              <li key={item.id} className="cart-item">
                <div
                  className="item"
                  style={{ display: "flex", alignContent: "center" }}
                  key={item.id}
                >
                  <div>
                    <img
                      src={item.imageUrl}
                      alt={item.name}
                      className="cart-item-image"
                    />
                  </div>
                  <div className="description">
                    <span>{item.brand}</span>
                    <span>{item.name}</span>
                  </div>

                  <div className="quantity">
                    <button
                      className="plus-btn"
                      type="button"
                      name="button"
                      onClick={() => handleIncreaseQuantity(item.id)}
                    >
                      <i className="bi bi-plus-square-fill"></i>
                    </button>
                    <input
                      type="button"
                      name="name"
                      value={item.quantity}
                      readOnly
                    />
                    <button
                      className="minus-btn"
                      type="button"
                      name="button"
                      onClick={() => handleDecreaseQuantity(item.id)}
                    >
                      <i className="bi bi-dash-square-fill"></i>
                    </button>
                  </div>

                  <div className="total-price " style={{ textAlign: "center" }}>
                    ${item.price * item.quantity}
                  </div>
                  <button
                    className="remove-btn"
                    onClick={() => handleRemoveFromCart(item.id)}
                  >
                    <i className="bi bi-trash3-fill"></i>
                  </button>
                </div>
              </li>
            ))}
            <div className="total">Total: ${totalPrice}</div>
            <Button
              className="btn btn-primary"
              style={{ width: "100%" }}
              onClick={() => setShowModal(true)}
            >
              Checkout
            </Button>
          </>
        )}
      </div>
      <CheckoutPopup
        show={showModal}
        handleClose={() => setShowModal(false)}
        cartItems={cartItems}
        totalPrice={totalPrice}
        handleCheckout={handleCheckout}
      />
    </div>
  );
};

export default Cart;
